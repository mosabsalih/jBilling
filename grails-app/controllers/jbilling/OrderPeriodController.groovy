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

import java.util.List;

import grails.plugins.springsecurity.Secured
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.db.LanguageDTO
import com.sapienter.jbilling.server.util.db.InternationalDescription
import com.sapienter.jbilling.server.util.InternationalDescriptionWS
import com.sapienter.jbilling.common.SessionInternalError
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO
import com.sapienter.jbilling.server.order.OrderPeriodWS

/**
 * OrderPeriodController 
 *
 * @author Vikas Bodani
 * @since 09-Mar-2011
 */


@Secured(["isAuthenticated()", "MENU_99"])
class OrderPeriodController {

	static pagination = [ max: 10, offset: 0 ]
	def breadcrumbService
	def webServicesSession
	def viewUtils
	
    def index = {
        redirect action: list, params: params
    }

    def list = {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset
		
		def periods= getPeriodsForEntity()
		
        breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)
		
		if (params.template) {
			flash.message=flash.message
			render template: params.template, model:[periods: periods]
		} else {
			render view: 'list', model:[periods: periods]
		}
	}
	
	def getPeriodsForEntity () {
		return OrderPeriodDTO.createCriteria().list(
			max:    params.max,
			offset: params.offset
		) {
			eq('company', new CompanyDTO(session['company_id']))
			order("id", "desc")
		}
	}
	
	def save = {
		int cnt = params.recCnt as int
		log.debug "Records Count: ${cnt}"
		
		def periods= getPeriodsForEntity()
		
		List <OrderPeriodWS> wsList= new ArrayList<OrderPeriodWS>(cnt+1)
		for (OrderPeriodDTO periodDto: periods) {
			OrderPeriodWS ws= new OrderPeriodWS(periodDto)
			bindData(ws, params["obj[${ws.id}]"])
			log.debug ws
			InternationalDescriptionWS descr=
			new InternationalDescriptionWS(session['language_id'] as Integer, params["obj[${ws.id}]"].description)
			log.debug descr
			ws.descriptions.add descr
			log.debug ws
			wsList.add(ws)
		}
		
		log.debug "New Value: ${params.value} & Description: ${params.description}"
		if (params.value && params.description) {
			OrderPeriodWS ws= new OrderPeriodWS()
			bindData(ws, params);
			ws.setEntityId(session['company_id'].toInteger())
			InternationalDescriptionWS descr=
			new InternationalDescriptionWS(session['language_id'] as Integer, params.description as String)
			log.debug descr
			ws.descriptions.add descr
			wsList.add ws
			log.debug ws
		}
		
		try {
			boolean retVal= webServicesSession.updateOrderPeriods(wsList.toArray(new OrderPeriodWS[wsList.size()]));
			flash.message= 'config.periods.updated'
		} catch (SessionInternalError e){
			viewUtils.resolveException(flash, session.locale, e);
		} catch (Exception e) {
			log.error e.getMessage()
			flash.error = 'config.periods.saving.error'
		}
		redirect (action: 'list')
		
	}
	
	def remove = {
		log.debug "ID: ${params.id}"
		if (params.id) {
			try {
				boolean retVal= webServicesSession.deleteOrderPeriod(params.id?.toInteger());
				if (retVal) { 
					flash.message= 'config.periods.delete.success'
				} else {
					flash.info = 'config.periods.delete.failure'
				}
			} catch (SessionInternalError e){
				viewUtils.resolveException(flash, session.locale, e);
			} catch (Exception e) {
				log.error e.getMessage()
				flash.error = 'config.periods.delete.error'
			}
		}
		redirect (action: 'list', params: [template: 'periods'])
	}
	
}
