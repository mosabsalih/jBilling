/*
 jBilling - The Enterprise Open Source Billing System
 Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde

 This file is part of jbilling.

 jbilling is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jbilling is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */

package jbilling

import grails.plugins.springsecurity.Secured
import com.sapienter.jbilling.server.payment.db.PaymentDTO
import com.sapienter.jbilling.server.user.db.CompanyDTO

import org.hibernate.Criteria
import com.sapienter.jbilling.server.payment.PaymentWS
import com.sapienter.jbilling.server.entity.CreditCardDTO
import com.sapienter.jbilling.server.entity.AchDTO
import com.sapienter.jbilling.server.entity.PaymentInfoChequeDTO
import com.sapienter.jbilling.common.SessionInternalError
import com.sapienter.jbilling.server.item.CurrencyBL
import com.sapienter.jbilling.server.invoice.InvoiceWS

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.common.Util
import com.sapienter.jbilling.common.Constants
import com.sapienter.jbilling.server.util.csv.CsvExporter
import com.sapienter.jbilling.server.util.csv.Exporter
import com.sapienter.jbilling.client.util.DownloadHelper
import com.sapienter.jbilling.client.util.SortableCriteria
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * PaymentController 
 *
 * @author Brian Cowdery
 * @since 04/01/11
 */
@Secured(["MENU_90"])
class PaymentController {

    static pagination = [ max: 10, offset: 0, sort: 'id', order: 'desc' ]

    def webServicesSession
    def webServicesValidationAdvice
    def viewUtils
    def filterService
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def getList(filters, GrailsParameterMap params) {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset
        params.sort = params?.sort ?: pagination.sort
        params.order = params?.order ?: pagination.order

        return PaymentDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            createAlias('baseUser', 'u')

            // create alias only if applying invoice filters to prevent duplicate results
            if (filters.find{ it.field.startsWith('i.') && it.value })
                createAlias('invoicesMap', 'i', Criteria.LEFT_JOIN)

            and {
                filters.each { filter ->
                    if (filter.value != null) {
                        addToCriteria(filter.getRestrictions());
                    }
                }

                eq('u.company', new CompanyDTO(session['company_id']))
                eq('deleted', 0)

                // limit list to only this customer's payments
                if (SpringSecurityUtils.ifNotGranted("PAYMENT_36")) {
                    eq('u.id', session['user_id'])
                }
            }

            // apply sorting
            SortableCriteria.sort(params, delegate)
        }
    }

    /**
     * Gets a list of payments and renders the the list page. If the "applyFilters" parameter is given,
     * the partial "_payments.gsp" template will be rendered instead of the complete payments list page.
     */
    def list = {
        def filters = filterService.getFilters(FilterType.PAYMENT, params)
        def payments = getList(filters, params)

        def selected = params.id ? PaymentDTO.get(params.int("id")) : null

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, params.int('id'))

        if (params.applyFilter || params.partial) {
            render template: 'payments', model: [ payments: payments, selected: selected, filters: filters ]
        } else {
            [ payments: payments, selected: selected, filters: filters ]
        }
    }

    /**
     * Applies the set filters to the payment list, and exports it as a CSV for download.
     */
    @Secured(["PAYMENT_34"])
    def csv = {
        def filters = filterService.getFilters(FilterType.PAYMENT, params)

        params.max = CsvExporter.MAX_RESULTS
        def payments = getList(filters, params)

        if (payments.totalCount > CsvExporter.MAX_RESULTS) {
            flash.error = message(code: 'error.export.exceeds.maximum')
            flash.args = [ CsvExporter.MAX_RESULTS ]
            redirect action: 'list', id: params.id

        } else {
            DownloadHelper.setResponseHeader(response, "payments.csv")
            Exporter<PaymentDTO> exporter = CsvExporter.createExporter(PaymentDTO.class);
            render text: exporter.export(payments), contentType: "text/csv"
        }
    }

    /**
     * Show details of the selected payment.
     */
    @Secured(["PAYMENT_35"])
    def show = {
        PaymentDTO payment = PaymentDTO.get(params.int('id'))
        recentItemService.addRecentItem(params.int('id'), RecentItemType.PAYMENT)
        breadcrumbService.addBreadcrumb(controllerName, 'list', params.template ?: null, params.int('id'))

        render template: 'show', model: [ selected: payment ]
    }

    /**
     * Convenience shortcut, this action shows all payments for the given user id.
     */
    def user = {
        def filter =  new Filter(type: FilterType.PAYMENT, constraintType: FilterConstraint.EQ, field: 'u.id', template: 'id', visible: true, integerValue: params.id)
        filterService.setFilter(FilterType.PAYMENT, filter)

        redirect action: list
    }

    /**
     * Delete the given payment id
     */
    @Secured(["PAYMENT_32"])
    def delete = {
        if (params.id) {
            webServicesSession.deletePayment(params.int('id'))

            log.debug("Deleted payment ${params.id}.")

            flash.message = 'payment.deleted'
            flash.args = [ params.id ]
        }

        // render the partial payments list
        params.applyFilter = true
        list()
    }

    /**
     * Shows the payment link screen for the given payment ID showing a list of un-paid invoices
     * that the payment can be applied to.
     */
    @Secured(["PAYMENT_33"])
    def link = {
        def payment = webServicesSession.getPayment(params.int('id'))
        def user = webServicesSession.getUserWS(payment?.userId ?: params.int('userId'))
        def invoices = getUnpaidInvoices(user.userId)

        render view: 'link', model: [ payment: payment, user: user, invoices: invoices, currencies: currencies, invoiceId: params.invoiceId ]
    }

    /**
     * Applies a given payment ID to the given invoice ID.
     */
    @Secured(["PAYMENT_33"])
    def applyPayment = {
        def payment = webServicesSession.getPayment(params.int('id'))

        if (payment && params.invoiceId) {
            try {
                log.debug("appling payment ${payment} to invoice ${params.invoiceId}")
                webServicesSession.createPaymentLink(params.int('invoiceId'), payment.id)

                flash.message = 'payment.link.success'
                flash.args = [ payment.id, params.invoiceId ]

            } catch (SessionInternalError e) {
                viewUtils.resolveException(flash, session.local, e)
                link()
                return
            }

        } else {
            flash.warn = 'payment.link.failed'
            flash.args = [ payment.id, params.invoiceId ]
        }

        redirect action: list, id: payment.id
    }

    /**
     * Un-links the given payment ID from the given invoice ID and re-renders
     * the "show payment" view panel.
     */
    @Secured(["PAYMENT_33"])
    def unlink = {
		try {
			webServicesSession.removePaymentLink(params.int('invoiceId'), params.int('id'))
			flash.message = "payment.unlink.success"

		} catch (Exception e) {
			flash.error = "error.invoice.unlink.payment"
		}

		show()
    }

    /**
     * Redirects to the user list and sets a flash message.
     */
    @Secured(["PAYMENT_30"])
    def create = {
        flash.info = 'payment.select.customer'
        redirect controller: 'customer', action: 'list'
    }

    /**
     * Gets the payment to be edited and shows the "edit.gsp" view. This edit action cannot be used
     * to create a new payment, as creation requires a wizard style flow where the user is selected first.
     */
    @Secured(["hasAnyRole('PAYMENT_30', 'PAYMENT_31')"])
    def edit = {
        def payment
        def user

        try {
            payment = params.id ? webServicesSession.getPayment(params.int('id')) : new PaymentWS()
            user = webServicesSession.getUserWS(payment?.userId ?: params.int('userId'))
        } catch (SessionInternalError e) {
            log.error("Could not fetch WS object", e)

            flash.error = 'payment.not.found'
            flash.args = [ params.id ]

            redirect controller: 'payment', action: 'list'
            return
        }


        def invoices = getUnpaidInvoices(user.userId)
        def paymentMethods = CompanyDTO.get(session['company_id']).getPaymentMethods()

        // set default payment instrument for new payments
        if (!params.id) {
            def instrument = webServicesSession.getUserPaymentInstrument(user.userId)
            payment.setCreditCard(instrument?.creditCard)
            payment.setAch(instrument?.ach)
        }

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'))

        [ payment: payment, user: user, invoices: invoices, currencies: currencies, paymentMethods: paymentMethods, invoiceId: params.int('invoiceId') ]
    }

    def getUnpaidInvoices(Integer userId) {
        def invoiceIds = webServicesSession.getUnpaidInvoices(userId);

        List<InvoiceWS> invoices = new ArrayList<InvoiceWS>(invoiceIds.size());
        for (Integer id : invoiceIds)
            invoices.add(webServicesSession.getInvoiceWS(id))
        return invoices;
    }

    /**
     * Shows a summary of the created/edited payment to be confirmed before saving.
     */
    @Secured(["hasAnyRole('PAYMENT_30', 'PAYMENT_31')"])
    def confirm = {
        def payment = new PaymentWS()
        bindPayment(payment, params)

        // make sure the user still exists before
        def users
        try {
            user = webServicesSession.getUserWS(payment?.userId ?: params.int('userId'))
        } catch (SessionInternalError e) {
            log.error("Could not fetch WS object", e)

            flash.error = 'customer.not.found'
            flash.args = [ params.id ]

            redirect controller: 'payment', action: 'list'
            return
        }

        def invoices = getUnpaidInvoices(user.userId)
        def paymentMethods = CompanyDTO.get(session['company_id']).getPaymentMethods()

        // validate before showing the confirmation page
        try {
            webServicesValidationAdvice.validateObject(payment)

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.local, e)
            render view: 'edit', model: [ payment: payment, user: user, invoices: invoices, currencies: currencies, paymentMethods: paymentMethods, invoiceId: params.int('invoiceId') ]
            return
        }

        // validation passed, render the confirmation page
        def processNow = params.processNow ? true : false
        [ payment: payment, user: user, invoices: invoices, currencies: currencies, processNow: processNow, invoiceId: params.invoiceId ]
    }

    /**
     * Validate and save payment.
     */
    @Secured(["hasAnyRole('PAYMENT_30', 'PAYMENT_31')"])
    def save = {
        def payment = new PaymentWS()
        bindPayment(payment, params)

        def invoiceId = params.int('invoiceId')


        // save or update
        try {
            if (!payment.id || payment.id == 0) {
                if (SpringSecurityUtils.ifAllGranted("PAYMENT_30")) {
                    def processNow = params.boolean('processNow') && payment.methodId != Constants.PAYMENT_METHOD_CHEQUE

                    log.debug("creating payment ${payment} for invoice ${invoiceId}")

                    if (processNow) {
                        log.debug("processing payment in real time")

                        def authorization = webServicesSession.processPayment(payment, invoiceId)
                        payment.id = authorization.paymentId

                        if (authorization.result) {
                            flash.message = getSuccessMessageKey(payment, processNow)
                            flash.args = [ payment.id ]

                        } else {
                            flash.error = 'payment.failed'
                            flash.args = [ payment.id, authorization.responseMessage ]
                        }

                    } else {
                        log.debug("entering payment")
                        payment.id = webServicesSession.applyPayment(payment, invoiceId)

                        if (payment.id) {
                            flash.info = getSuccessMessageKey(payment, processNow)
                            flash.args = [ payment.id ]

                        } else {
                            flash.info = 'payment.entered.failed'
                            flash.args = [ payment.id ]
                        }
                    }

                } else {
                    render view: '/login/denied'
                    return
                }

            } else {
                if (SpringSecurityUtils.ifAllGranted("PAYMENT_31")) {
                    log.debug("saving changes to payment ${payment.id}")
                    webServicesSession.updatePayment(payment)

                    if (invoiceId) {
                        log.debug("appling payment ${payment} to invoice ${invoiceId}")
                        webServicesSession.createPaymentLink(invoiceId, payment.id)
                    }

                    flash.message = 'payment.updated'
                    flash.args = [ payment.id ]

                } else {
                    render view: '/login/denied'
                }
            }

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.local, e)

            def user = webServicesSession.getUserWS(payment.userId)
            def invoices = getUnpaidInvoices(user.userId)
            def paymentMethods = CompanyDTO.get(session['company_id']).getPaymentMethods()

            render view: 'edit', model: [ payment: payment, user: user, invoices: invoices, currencies: currencies, paymentMethods: paymentMethods, invoiceId: params.int('invoiceId') ]
            return
        }

        chain action: 'list', params: [ id: payment.id ]
    }

    def bindPayment(PaymentWS payment, GrailsParameterMap params) {
        bindData(payment, params, 'payment')

        payment.isRefund = params.boolean('isRefund') ? 1 : 0

        // bind credit card object if parameters present
        if (params.creditCard.any { key, value -> value }) {
            def creditCard = new CreditCardDTO()
            bindData(creditCard, params, 'creditCard')
            bindExpiryDate(creditCard, params)
            payment.setCreditCard(creditCard)

            payment.setMethodId(Util.getPaymentMethod(creditCard.number))
        }

        // bind ach object if parameters present
        if (params.ach.any { key, value -> value }) {
            def ach = new AchDTO()
            bindData(ach, params, 'ach')
            payment.setAch(ach)

            payment.setMethodId(Constants.PAYMENT_METHOD_ACH)
        }

        // bind cheque object if parameters present
        if (params.cheque.any { key, value -> value }) {
            def cheque = new PaymentInfoChequeDTO()
            bindData(cheque, params, 'cheque')
            payment.setCheque(cheque)

            payment.setMethodId(Constants.PAYMENT_METHOD_CHEQUE)

            // no processing for cheques
            params.processNow = false
        }

        return payment
    }

    def bindExpiryDate(CreditCardDTO creditCard, GrailsParameterMap params) {
        Integer expiryMonth = params.int('expiryMonth')
        Integer expiryYear = params.int('expiryYear')

        if (expiryMonth && expiryYear)  {
            Calendar calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(Calendar.MONTH, expiryMonth - 1) // calendar API months start at 0
            calendar.set(Calendar.YEAR, expiryYear)

            creditCard.expiry = calendar.getTime()
        }
    }

    def getSuccessMessageKey(PaymentWS payment, processRealTime) {
        if (payment.getCreditCard()) {
            return processRealTime ? 'payment.credit.card.processed' : 'payment.credit.card.entered'
        }

        if (payment.getAch()) {
            return processRealTime ? 'payment.ach.processed' : 'payment.ach.entered'
        }

        if (payment.getCheque()) {
            return processRealTime ? 'payment.cheque.processed' : 'payment.cheque.entered'
        }

        return 'payment.successful'
    }

    def getCurrencies() {
        def currencies = new CurrencyBL().getCurrencies(session['language_id'].toInteger(), session['company_id'].toInteger())
        return currencies.findAll{ it.inUse }
    }
}
