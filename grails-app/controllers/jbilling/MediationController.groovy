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
import com.sapienter.jbilling.server.mediation.db.MediationProcess
import com.sapienter.jbilling.server.mediation.MediationRecordWS
import com.sapienter.jbilling.server.util.Constants
import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO
import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDTO
import com.sapienter.jbilling.server.order.db.OrderDTO
import org.hibernate.criterion.Restrictions
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.FetchMode
import org.hibernate.Criteria
import com.sapienter.jbilling.client.util.SortableCriteria

/**
* MediationController
*
* @author Vikas Bodani
* @since 17/02/2011
*/
@Secured(["MENU_95"])
class MediationController {

    static pagination = [ max: 10, offset: 0, sort: 'id', order: 'desc']

	def webServicesSession
	def recentItemService
	def breadcrumbService
	def filterService

	def index = {
		redirect action: list, params: params
	}

	def list = {
		def filters = filterService.getFilters(FilterType.MEDIATIONPROCESS, params)
		def processes = getFilteredProcesses(filters, params)

		breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)

		if (params.applyFilter || params.partial) {
			render template: 'processes', model: [processes: processes,filters:filters]
		} else {
			render view: "list", model: [processes: processes, filters:filters]
		}
    }
	
	def getFilteredProcesses (filters, GrailsParameterMap params) {
		params.max = (params?.max?.toInteger()) ?: pagination.max
		params.offset = (params?.offset?.toInteger()) ?: pagination.offset
        params.sort = params?.sort ?: pagination.sort
        params.order = params?.order ?: pagination.order

		def processes = new HashMap<MediationProcess, Integer>()

        MediationProcess.createCriteria().list(
			max:    params.max,
			offset: params.offset
		) {
			and {
				filters.each { filter ->
					if (filter.value != null) {
						addToCriteria(filter.getRestrictions());
					}
				}
			}

            configuration {
                eq("entityId", session['company_id'])
            }

            // apply sorting
            SortableCriteria.sort(params, delegate)

        }.each { process ->
            processes.put(process, getRecordCount(process))
        }

        return processes
	}

    def Integer getRecordCount(MediationProcess process) {
        return MediationRecordDTO.createCriteria().get() {
            eq('process.id', process.id)

            projections {
                rowCount()
            }
        }
    }

	def show = {
        def process = MediationProcess.get(params.int('id'))
        def recordCount = getRecordCount(process)

		recentItemService.addRecentItem(process.id, RecentItemType.MEDIATIONPROCESS)
		breadcrumbService.addBreadcrumb(controllerName, actionName, null, process.id)

		if (params.template) {
			render template: params.template, model: [ selected: process, recordCount: recordCount ]

		} else {
			def filters = filterService.getFilters(FilterType.MEDIATIONPROCESS, params)
			def processes = getFilteredProcesses(filters, params)

			render view: 'list', model: [ selected: process, recordCount: recordCount, processes: processes, filters: filters ]
		}
	}

    def invoice = {
        def invoice = InvoiceDTO.get(params.int('id'))

        def records = MediationRecordDTO.createCriteria().listDistinct {
            lines {
                orderLine {
                    purchaseOrder {
                        orderProcesses {
                            eq("invoice.id", invoice.id)
                        }
                    }
                }
            }

            recordStatus {
                eq("id", Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE)
            }
        }

        render view: 'events', model: [ invoice: invoice, records: records ]
    }

    def order = {
        def order = OrderDTO.get(params.int('id'))

        def records = MediationRecordDTO.createCriteria().listDistinct {
            lines {
                orderLine {
                    eq("purchaseOrder.id", order.id)
                }
            }

            recordStatus {
                eq("id", Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE)
            }
        }

        render view: 'events', model: [ order: order, records: records ]
    }
	
}
