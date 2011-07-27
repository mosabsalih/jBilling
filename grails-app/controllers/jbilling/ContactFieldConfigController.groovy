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
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.db.LanguageDTO
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO
import com.sapienter.jbilling.server.user.contact.ContactFieldTypeWS
import com.sapienter.jbilling.server.util.db.InternationalDescription
import com.sapienter.jbilling.server.util.InternationalDescriptionWS
import com.sapienter.jbilling.common.SessionInternalError

/**
 * ContactFieldConfigController 
 *
 * @author Vikas Bodani
 * @since 31-Jan-2011
 */


@Secured(["MENU_99"])
class ContactFieldConfigController {
	
	def webServicesSession
	def viewUtils
	def recentItemService
	def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def list = {
        def types = ContactFieldTypeDTO.createCriteria().list() {
            eq('entity', new CompanyDTO(session['company_id']))
            order('id', 'asc')
        }

        def selected = params.id ? ContactFieldTypeDTO.get(params.int("id")) : null

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'))

        [ types: types, selected: selected, languages: languages ]
    }
	
	def save = {
		def cnt = params.recCnt.toInteger()
		log.debug "Records Count: ${cnt}"
		
		List<ContactFieldTypeWS> fields= new ArrayList<ContactFieldTypeWS>(cnt+1);
		for (int i=0; i < cnt; i++) {
			ContactFieldTypeWS ws= new ContactFieldTypeWS()
			bindData(ws, params["obj["+i+"]"])
			log.debug "Params: ${params['obj[' + i + '].description']}"
			InternationalDescriptionWS descr= 
				new InternationalDescriptionWS(session['language_id'] as Integer, params['obj[' + i + '].description'] as String)
			ws.descriptions.add descr
			fields.add (ws)
		}
		
		log.debug "${params.description} And ${params.dataType}"
		if (params.description && params.dataType) {
			ContactFieldTypeWS ws= new ContactFieldTypeWS()
			bindData(ws, params);
			InternationalDescriptionWS descr=
			new InternationalDescriptionWS(session['language_id'] as Integer, params.description as String)
			ws.descriptions.add(descr)
			fields.add (ws)
		}
		
		log.debug "Fields size= ${fields.size()}"
		
		for (ContactFieldTypeWS ws: fields) {
			log.debug ws.toString()
		}
		
		try {
			webServicesSession.saveCustomContactFields((ContactFieldTypeWS[])fields.toArray())
		} catch (SessionInternalError e) {
			log.error e
			viewUtils.resolveException(flash, session.locale, e)
		}
		flash.message = 'custom.fields.save.success'
		redirect action: 'list'
	}
	
	def getLanguages() {
		return LanguageDTO.list()
	}
}
