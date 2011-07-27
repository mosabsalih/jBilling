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

import com.sapienter.jbilling.client.util.Constants
import com.sapienter.jbilling.client.util.DownloadHelper
import com.sapienter.jbilling.common.SessionInternalError
import com.sapienter.jbilling.server.customer.CustomerBL
import com.sapienter.jbilling.server.invoice.InvoiceBL
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS
import com.sapienter.jbilling.server.item.CurrencyBL
import com.sapienter.jbilling.server.order.OrderBL
import com.sapienter.jbilling.server.order.OrderWS
import com.sapienter.jbilling.server.order.db.OrderDAS
import com.sapienter.jbilling.server.order.db.OrderDTO
import com.sapienter.jbilling.server.order.db.OrderPeriodDAS
import com.sapienter.jbilling.server.order.db.OrderStatusDAS
import com.sapienter.jbilling.server.user.UserWS
import com.sapienter.jbilling.server.user.db.CustomerDTO
import com.sapienter.jbilling.server.user.db.UserDAS
import com.sapienter.jbilling.server.user.db.UserDTO
import com.sapienter.jbilling.server.util.csv.CsvExporter
import com.sapienter.jbilling.server.util.csv.Exporter
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.server.user.db.CompanyDTO
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Criterion
import org.hibernate.Criteria
import com.sapienter.jbilling.client.util.SortableCriteria
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 *
 * @author vikas bodani
 * @since  20-Jan-2011
 *
 */

@Secured(["MENU_92"])
class OrderController {

    static pagination = [ max: 10, offset: 0, sort: 'id', order: 'desc' ]

    def webServicesSession
    def viewUtils
    def filterService
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def getFilteredOrders(filters, GrailsParameterMap params) {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset
        params.sort = params?.sort ?: pagination.sort
        params.order = params?.order ?: pagination.order

        return OrderDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            createAlias('baseUserByUserId', 'u', Criteria.LEFT_JOIN)
            and {
                filters.each { filter ->
                    if (filter.value) {
                        //handle orderStatus & orderPeriod separately
                        if (filter.constraintType == FilterConstraint.STATUS) {
                            if (filter.field == 'orderStatus') {
                                def statuses = new OrderStatusDAS().findAll()
                                eq("orderStatus", statuses.find{ it.id == filter.integerValue })
                            } else if (filter.field == 'orderPeriod') {
                                def periods = new OrderPeriodDAS().findAll()
                                eq("orderPeriod", periods.find{ it.id == filter.integerValue })
                            }
                        } else {
                            addToCriteria(filter.getRestrictions());
                        }
                    }
                }
                eq('u.company', new CompanyDTO(session['company_id']))
                eq('deleted', 0)

                // limit list to only this customer's orders
                if (SpringSecurityUtils.ifNotGranted("ORDER_28")) {
                    eq('u.id', session['user_id'])
                }
            }

            // apply sorting
            SortableCriteria.sort(params, delegate)
        }
    }

    def list = {
        def filters = filterService.getFilters(FilterType.ORDER, params)
        def orders = getFilteredOrders (filters, params)

        def selected = params.id ? webServicesSession.getOrder(params.int("id")) : null
        def user = params.id ? webServicesSession.getUserWS(selected.userId) : null
        def currencies = params.id ? getCurrencies() : null

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, selected?.id)

        if (params.applyFilter || params.partial) {
            render template: 'orders', model: [ orders: orders, order: selected, user: user, currencies: currencies, filters: filters ]
        } else {
            [ orders: orders, order: selected, user: user, currencies: currencies, filters: filters ]
        }
    }

    @Secured(["ORDER_24"])
    def show = {
        OrderWS order = webServicesSession.getOrder(params.int('id'))
        UserWS user = webServicesSession.getUserWS(order.getUserId())

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, order.id)
        recentItemService.addRecentItem(order.id, RecentItemType.ORDER)

        render template:'show', model: [order: order, user: user, currencies: currencies]
    }

    /**
     * Applies the set filters to the order list, and exports it as a CSV for download.
     */
    @Secured(["ORDER_25"])
    def csv = {
        def filters = filterService.getFilters(FilterType.ORDER, params)

        params.max = CsvExporter.MAX_RESULTS
        def orders = getFilteredOrders(filters, params)

        if (orders.totalCount > CsvExporter.MAX_RESULTS) {
            flash.error = message(code: 'error.export.exceeds.maximum')
            flash.args = [ CsvExporter.MAX_RESULTS ]
            redirect action: 'list', id: params.id

        } else {
            DownloadHelper.setResponseHeader(response, "orders.csv")
            Exporter<OrderDTO> exporter = CsvExporter.createExporter(OrderDTO.class);
            render text: exporter.export(orders), contentType: "text/csv"
        }
    }

    /**
     * Convenience shortcut, this action shows all invoices for the given user id.
     */
    def user = {
        def filter = new Filter(type: FilterType.ALL, constraintType: FilterConstraint.EQ, field: 'baseUserByUserId.id', template: 'id', visible: true, integerValue: params.int('id'))
        filterService.setFilter(FilterType.ORDER, filter)
        redirect action: 'list'
    }

    @Secured(["ORDER_23"])
    def generateInvoice = {
        log.debug "generateInvoice for order ${params.id}"

        def orderId = params.id?.toInteger()

        Integer invoiceID= null;
        try {
            invoiceID = webServicesSession.createInvoiceFromOrder(orderId, null)

        } catch (SessionInternalError e) {
            flash.error= 'order.error.generating.invoice'
            redirect action: 'list', params: [ id: params.id ]
            return
        }

        if ( null != invoiceID) {
            flash.message ='order.geninvoice.success'
            flash.args = [orderId]
            redirect controller: 'invoice', action: 'list', params: [id: invoiceID]

        } else {
            flash.error ='order.error.geninvoice.inactive'
            redirect action: 'list', params: [ id: params.id ]
        }
    }

    @Secured(["ORDER_23"])
    def applyToInvoice = {
        def invoices = getApplicableInvoices(params.int('userId'))

        if (!invoices || invoices.size() == 0) {
            flash.error = 'order.error.invoices.not.found'
            flash.args = [params.userId]
            redirect (action: 'list', params: [ id: params.id ])
        }

        session.applyToInvoiceOrderId = params.int('id')
        [ invoices:invoices, currencies: currencies, orderId: params.id ]
    }

    @Secured(["ORDER_23"])
    def apply = {
        log.debug "apply: for order ${params.id}"
        Integer invoiceID= params.int('invoiceId')

        try {
            OrderDTO order= new OrderDAS().find(params.int('id'))
            if (!order.getStatusId().equals(
                    Constants.ORDER_STATUS_ACTIVE)) {
                throw new Exception('order.error.status.not.active')
            } else if ( !invoiceID ) {
                throw new Exception('order.error.invoice.is.null')
            }

            def invoice= webServicesSession.createInvoiceFromOrder(order.getId(), invoiceID)
            if ( !invoice ) {
                throw new Exception('order.error.apply.invoice')
            }
            flash.message = 'order.succcessfully.applied.to.invoice'
            flash.args = [params.id, invoice]
        } catch (SessionInternalError e){
            flash.error ='order.error.apply.invoice'
            viewUtils.resolveException(flash, session.locale, e);
        } catch (Exception e) {
            log.error e
            flash.error= e.getMessage()
        }
        redirect action: 'list', params: [ id: params.id ]
    }

    def getApplicableInvoices(Integer userId) {

        CustomerDTO payingUser
        Integer _userId
        UserDTO user= new UserDAS().find(userId)
        if (user.getCustomer()?.getParent()) {
            payingUser= new CustomerBL(user.getCustomer().getId()).getInvoicableParent()
            _userId=payingUser.getBaseUser().getId()
        } else {
            _userId= user.getId()
        }
        InvoiceDAS das= new InvoiceDAS()
        List invoices =  new ArrayList()
        for (Iterator it= das.findAllApplicableInvoicesByUser(_userId ).iterator(); it.hasNext();) {
            invoices.add InvoiceBL.getWS(das.find (it.next()))
        }

        log.debug "Found ${invoices.size()} for user ${_userId}"

        invoices as List
    }

    def getCurrencies() {
        def currencies = new CurrencyBL().getCurrencies(session['language_id'].toInteger(), session['company_id'].toInteger())
        return currencies.findAll{ it.inUse }
    }

    def byProcess = {
        OrderBL bl= new OrderBL();
        List<Integer> orderIds= bl.getOrdersByProcess(params.id.toInteger())

        log.debug "Expecting ${orderIds.size()} orders."

        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset
        def filters=filterService.getFilters(FilterType.ORDER, params)

        def orders = OrderDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            and {
                'in'('id', orderIds.toArray(new Integer[orderIds.size()]))
                //eq('deleted', 0)
            }
            order("id", "desc")
        }
        log.debug("Found ${orders.size()} orders.")
        render view: 'list', model: [orders:orders, filters:filters]
    }

    @Secured(["ORDER_22"])
    def deleteOrder = {
        try {
            webServicesSession.deleteOrder(params.int('id'))
            flash.message = 'order.delete.success'
            flash.args = [params.id, params.id]
        } catch (SessionInternalError e){
            flash.error ='order.error.delete'
            viewUtils.resolveException(flash, session.locale, e);
        } catch (Exception e) {
            log.error e
            flash.error= e.getMessage()
        }
        redirect action: 'list'
    }

}
