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
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Criterion
import org.hibernate.Criteria
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.audit.db.EventLogDTO
import jbilling.FilterType

@Secured(["isAuthenticated()"])
class AuditLogController {

    static pagination = [ max: 10, offset: 0 ]

    def webServicesSession
    def viewUtils
    def filterService
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    /**
     * Gets a list of logs and renders the the list page. If the "applyFilters" parameter is given,
     * the partial "_logs.gsp" template will be rendered instead of the complete logs list page.
     */
    def list = {
        def filters = filterService.getFilters(FilterType.LOGS, params)

        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset

        def logs = EventLogDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            createAlias('affectedUser', 'u', Criteria.LEFT_JOIN)
            createAlias('jbillingTable', 'table', Criteria.LEFT_JOIN)

            and {
                filters.each { filter ->
                    //log.debug("Now processing filter " + filter);
                    if (filter.getValue() != null) {
                        // avoid adding a filter for no table selection
                        if (!(filter.getField().equals("table.name") && filter.getStringValue().trim().length() == 0)) {
                            //log.debug("Adding restriction " + filter.getRestrictions());
                            addToCriteria(filter.getRestrictions());
                        }
                    }
                }

                eq('company', new CompanyDTO(session['company_id']))
            }

            order("id", "desc")
        }

        def selected = params.id ? EventLogDTO.get(params.int("id")) : null

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, params.int('id'))

        if (params.applyFilter || params.partial) {
            render template: 'logs', model: [ logs: logs, selected: selected, filters: filters ]
        } else {
            [ logs: logs, selected: selected, filters: filters ]
        }
    }

    /**
     * Show details of the selected log.
     */
    def show = {
        EventLogDTO log = EventLogDTO.get(params.int('id'))
        breadcrumbService.addBreadcrumb(controllerName, 'list', params.template ?: null, params.int('id'))

        render template: 'show', model: [ selected: log ]
    }

    /**
     * Convenience shortcut, this action shows all logs for the given user id.
     */
    def user = {
        Filter filter =  new Filter(type: FilterType.LOGS, constraintType: FilterConstraint.EQ,
                                    field: 'affectedUser.id', template: 'id', visible: true, integerValue: params.id)

        filterService.setFilter(FilterType.LOGS, filter)

        redirect action: list
    }

}
