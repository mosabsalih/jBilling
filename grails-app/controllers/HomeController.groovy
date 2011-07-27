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

import grails.plugins.springsecurity.Secured
import jbilling.Breadcrumb

/**
 * Shows the user's home page after login.
 *
 * Mapped to "/", see UrlMappings.groovy
 *
 * @author Brian Cowdery
 * @since  22-11-2010
 */
class HomeController {

    def recentItemService
    def breadcrumbService

    @Secured(["isAuthenticated()"])
    def index = {        
        def breadcrumb = Breadcrumb.findByUserId(session['user_id'], [sort:'id', order:'desc'])

        if (breadcrumb) {
            // show last page viewed
            redirect(controller: breadcrumb.controller, action: breadcrumb.action, id: breadcrumb.objectId)
        } else {
            // show default page
            redirect(controller: 'customer')
        }
    }
}
