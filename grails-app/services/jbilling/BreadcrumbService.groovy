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

import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpSession
import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: brian
 * Date: 14-Dec-2010
 * Time: 10:17:10 AM
 * To change this template use File | Settings | File Templates.
 */
class BreadcrumbService implements InitializingBean, Serializable {

    public static final String SESSION_BREADCRUMBS = "breadcrumbs"
    public static final Integer MAX_ITEMS = 7

    static scope = "session"

    def void afterPropertiesSet() {
        if (session['user_id'])
            session[SESSION_BREADCRUMBS] = getBreadcrumbs()
    }

    /**
     * Returns a list of recently viewed items for the currently logged in user.
     *
     * @return list of recently viewed items.
     */
    def Object getBreadcrumbs() {
        return Breadcrumb.withCriteria {
            eq("userId", session["user_id"])
            order("id", "asc")
        }
    }

    /**
     * Add a new breadcrumb to the breadcrumb list for the currently logged in user and
     * update the session list.
     *
     * The resulting breadcrumb link is generated using the 'g:link' grails tag. The same
     * parameter requirements for g:link apply here as well. A breadcrumb MUST have a controller,
     * but action and ID are optional. the name parameter is used to control the translated breadcrumb
     * message and is optional.
     *
     * @param controller breadcrumb controller (required)
     * @param action breadcrumb action, may be null
     * @param name breadcrumb message key name, may be null
     * @param objectId breadcrumb entity id, may be null.
     */
    def void addBreadcrumb(String controller, String action, String name, Integer objectId) {
        addBreadcrumb(new Breadcrumb(controller: controller, action: action, name: name, objectId: objectId))
    }

    def void addBreadcrumb(String controller, String action, String name, Integer objectId, String description) {
        addBreadcrumb(new Breadcrumb(controller: controller, action: action, name: name, objectId: objectId, description: description))
    }

    /**
     * Add a new breadcrumb to the recent breadcrumb list for the currently logged in user and
     * update the session list.
     *
     * @param crumb breadcrumb to add
     */
    def void addBreadcrumb(Breadcrumb crumb) {
        def crumbs = getBreadcrumbs()
        def lastItem = !crumbs.isEmpty() ? crumbs.getAt(-1) : null

        // add breadcrumb only if it is different from the last crumb added
        try {
            if (!lastItem || !lastItem.equals(crumb)) {
                crumb.userId = session['user_id']
                crumb.save()

                crumbs << crumb

                if (crumbs.size() > MAX_ITEMS) {
                    def remove = crumbs.subList(5, crumbs.size())
                    remove.each{ it.delete(flush: true) }
                    remove.clear()
                }

                session[SESSION_BREADCRUMBS] = crumbs
            }

        } catch (Throwable t) {
            log.error("Exception caught adding breadcrumb", t)
            session.error = 'breadcrumb.failed'
        }
    }

    /**
     * Returns the HTTP session
     *
     * @return http session
     */
    def HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
    }

}
