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
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO
import com.sapienter.jbilling.server.user.contact.db.ContactDTO
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.db.LanguageDTO
import com.sapienter.jbilling.server.user.ContactTypeWS
import com.sapienter.jbilling.server.util.db.InternationalDescription
import com.sapienter.jbilling.server.util.InternationalDescriptionWS
import com.sapienter.jbilling.common.SessionInternalError

/**
 * ContactTypeConfigController 
 *
 * @author Brian Cowdery
 * @since 27-Jan-2011
 */
@Secured(["MENU_99"])
class ContactTypeConfigController {

    static pagination = [ max: 10, offset: 0 ]

    def webServicesSession
    def viewUtils
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def list = {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset

        def types = ContactTypeDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            eq('entity', new CompanyDTO(session['company_id']))
            order('id', 'asc')
        }

        def selected = params.id ? ContactTypeDTO.get(params.int("id")) : null

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'))

        [ types: types, selected: selected, languages: languages ]
    }

    /**
     * Shows details of the selected contact type.
     */
    def show = {
        def selected = ContactTypeDTO.get(params.int('id'))
        breadcrumbService.addBreadcrumb(controllerName, 'list', null, params.int('id'))

        render template: 'show', model: [ selected: selected, languages: languages ]
    }

    /**
     * Show the "_edit.gsp" panel to create a new contact.
     */
    def edit = {
        render template: 'edit', model: [ languages: languages ]
    }

    /**
     * Saves a new contact type.
     */
    def save = {
        def contactType = new ContactTypeWS()
        contactType.companyId = session['company_id']
        contactType.primary = params.int('isPrimary')

        params.language.each { id, value ->
            contactType.descriptions.add(new InternationalDescriptionWS(id as Integer, value))
        }

        try {
            log.debug("creating new contact type ${contactType}")
            contactType.id = webServicesSession.createContactTypeWS(contactType)

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
        }

        redirect action: list, id: contactType.id
    }

    def getLanguages() {
        return LanguageDTO.list()
    }
}
