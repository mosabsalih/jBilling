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

import grails.plugins.springsecurity.Secured;
import java.util.Iterator;
import java.math.BigDecimal;
import com.sapienter.jbilling.server.process.BillingProcessRunTotalDTOEx;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.process.db.BillingProcessDAS;
import com.sapienter.jbilling.server.process.db.ProcessRunDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS
import com.sapienter.jbilling.client.util.SortableCriteria
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

/**
* BillingController
*
* @author Vikas Bodani
* @since 07/01/11
*/
@Secured(["MENU_94"])
class BillingController {

	static pagination = [ max: 10, offset: 0, sort: 'id', order: 'desc' ]

	def webServicesSession
	def recentItemService
	def breadcrumbService
	def filterService

	def index = {
		redirect action: list, params: params
	}
	
	/*
	 * Renders/display list of Billing Processes Ordered by Process Id descending
	 * so that the lastest process shows first.
	 */
	def list = {
		Map dataHashMap = new HashMap()

		def filters = filterService.getFilters(FilterType.BILLINGPROCESS, params)
		def filteredList= filterProcesses(filters)

        for (BillingProcessDTO dto : filteredList) {
            Iterator countIterator = new BillingProcessDAS().getCountAndSum(dto.getId())
            if (countIterator != null) {
                while (countIterator.hasNext()) {
                    Object[] row = (Object[]) countIterator.next();
                    row[2] = new CurrencyDAS().find(row[2] as Integer)
                    dataHashMap.put (dto.getId(), row)
                }
            }
        }

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)
		if (params.applyFilter || params.partial) {
			render template: 'list', model: [lstBillingProcesses: filteredList, dataHashMap:dataHashMap, filters:filters]
		} else {
			render view: "index", model: [lstBillingProcesses: filteredList, dataHashMap:dataHashMap, filters:filters]
		}
	}

	/*
	 * Filter the process results based on the parameter filter values
	 */
	def filterProcesses(filters) {
		params.max = (params?.max?.toInteger()) ?: pagination.max
		params.offset = (params?.offset?.toInteger()) ?: pagination.offset
        params.sort = params?.sort ?: pagination.sort
        params.order = params?.order ?: pagination.order

		
		return BillingProcessDTO.createCriteria().list(
			max:    params.max,
			offset: params.offset
			) {
				and {
					filters.each { filter ->
						if (filter.value) {
							addToCriteria(filter.getRestrictions());
						}
					}
					//eq('isReview', 0)
					eq('entity', new CompanyDTO(session['company_id']))
				}

                // apply sorting
                SortableCriteria.sort(params, delegate)
			}
	}

	/*
	 * To display the run details of a given Process Id
	 */
	def show = {
		Integer processId = params.int('id')
		BillingProcessDTO process = new BillingProcessDAS().find(processId);

        def das = new BillingProcessDAS()
		def genInvoices = new InvoiceDAS().findByProcess(process)
		def invoicesGenerated = genInvoices?.size() ?: 0

        log.debug("Fetching count and sum by currency")

		def countAndSumByCurrency= new ArrayList()
        Iterator countIterator = das.getCountAndSum(processId)

		while (countIterator.hasNext()) {
			Object[] row = (Object[]) countIterator.next();
			row[2] = CurrencyDTO.get(row[2] as Integer)
			countAndSumByCurrency.add(row)
		}

        log.debug("Fetching payment list by currency")

		def mapOfPaymentListByCurrency= new HashMap()
		Iterator currencyIterator = das.getSuccessfulProcessCurrencyMethodAndSum(processId)

		while (currencyIterator.hasNext()) {
			Object[] row = (Object[]) currencyIterator.next();

			def payments = mapOfPaymentListByCurrency.get(row[0] as Integer) ?: new ArrayList()
            payments.add([new PaymentMethodDTO(row[1]), new BigDecimal(row[2])] as Object[])
            mapOfPaymentListByCurrency.put(row[0], payments)
		}

        log.debug("Fetching failed amounts by currency")

		def failedAmountsByCurrency= new ArrayList()
		Iterator failedIterator = das.getFailedProcessCurrencyAndSum(processId)

		while (failedIterator.hasNext()) {
			Object[] row = (Object[]) failedIterator.next();
			failedAmountsByCurrency.add(row[1])
		}

		recentItemService.addRecentItem(processId, RecentItemType.BILLINGPROCESS)
		breadcrumbService.addBreadcrumb(controllerName, actionName, null, processId)
		[process:process, invoicesGenerated:invoicesGenerated, countAndSumByCurrency: countAndSumByCurrency, mapOfPaymentListByCurrency: mapOfPaymentListByCurrency, failedAmountsByCurrency: failedAmountsByCurrency, reviewConfiguration: webServicesSession.getBillingProcessConfiguration()] 
	}

	def showInvoices = {
		def _processId= params.int('id')
		log.debug "redirecting to invoice controller for process id=${_processId}"
		//def filter =  new Filter(type: FilterType.INVOICE, constraintType: FilterConstraint.EQ, field: 'billingProcess.id', template: 'id', visible: false, integerValue: _processId)
		//filterService.setFilter(FilterType.INVOICE, filter)
		redirect controller: 'invoice', action: 'byProcess', id:_processId
	}
	
	def showOrders = {
		def _processId= params.int('id')
		log.debug "redirect to order controller for processId $_processId"
		redirect controller: 'order', action: 'byProcess', id:_processId
	}

    @Secured(["BILLING_80"])
	def approve = {
		try {
			webServicesSession.setReviewApproval(Boolean.TRUE)
		} catch (Exception e) {
			throw new SessionInternalError(e)
		}
		flash.message = 'billing.review.approve.success'
		redirect action: 'list'
	}

    @Secured(["BILLING_80"])
	def disapprove = {
		try {
			webServicesSession.setReviewApproval(Boolean.FALSE)
		} catch (Exception e) {
			throw new SessionInternalError(e)
		}
		flash.message = 'billing.review.disapprove.success'
		redirect action: 'list'
	}
}
