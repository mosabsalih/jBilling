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

import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO
import com.sapienter.jbilling.server.payment.IPaymentSessionBean
import com.sapienter.jbilling.server.util.Context
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.user.UserBL
import grails.plugins.springsecurity.Secured

@Secured(["CUSTOMER_14"])
class BlacklistController {

    def index = {
        redirect action: list, params: params
    }

    def getFilteredList(GrailsParameterMap params) {
        def blacklist = BlacklistDTO.createCriteria().list() {

            if (params.filterBy && params.filterBy != message(code: 'blacklist.filter.by.default')) {
                or {
                    eq('user.id', params.int('filterBy'))
                    user {
                        ilike('userName', "%${params.filterBy}%")
                    }
                    creditCard {
                        ilike('ccNumberPlain', "%${params.filterBy}%")
                    }
                }
            }

            eq('company.id', session['company_id'])
            order('id', 'asc')
        }

    }

    def list = {
        def blacklist = getFilteredList(params)
        def selected = params.id ? BlacklistDTO.get(params.int('id')) : null

        [ blacklist: blacklist, selected: selected ]
    }

    def filter = {
        def blacklist = getFilteredList(params)

        render template: 'entryList', model: [ blacklist: blacklist ]
    }

    def show = {
        def entry = BlacklistDTO.get(params.int('id'))

        render template: 'show', model: [ selected: entry ]
    }

    def save = {
        def replace = params.csvUpload == 'modify'
        def file = request.getFile('csv');

        if (!file.empty) {
            def csvFile = File.createTempFile("blacklist", ".csv")
            file.transferTo(csvFile)

            IPaymentSessionBean paymentSession = Context.getBean(Context.Name.PAYMENT_SESSION)
            def added = paymentSession.processCsvBlacklist(csvFile.getAbsolutePath(), replace, (Integer) session['company_id'])

            flash.message = replace ? 'blacklist.updated' : 'blacklist.added'
            flash.args = [ added ]
        }

        redirect view: 'list'
    }

    def user = {
        if (params.id) {
            def bl = new UserBL(params.int('id'))
            bl.setUserBlacklisted((Integer) session['user_id'], true)

            flash.message = 'user.blacklisted'
            flash.args = [ params.id ]
        }

        redirect controller: 'customerInspector', action: 'inspect', id: params.id
    }

}
