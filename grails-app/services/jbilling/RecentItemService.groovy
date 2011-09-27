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

import javax.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.beans.factory.InitializingBean
import java.io.Serializable

/**
 * RecentItemService
 
 * @author Brian Cowdery
 * @since  07-12-2010
 */
class RecentItemService implements InitializingBean, Serializable {

    public static final String SESSION_RECENT_ITEMS = "recent_items"
    public static final Integer MAX_ITEMS = 5

    static scope = "session"
    
    def void afterPropertiesSet() {
        if (session['user_id'])
            session[SESSION_RECENT_ITEMS] = getRecentItems()
    }

    /**
     * Returns a list of recently viewed items for the currently logged in user.
     *
     * @return list of recently viewed items.
     */
    def Object getRecentItems() {
        return RecentItem.withCriteria {
            eq("userId", session["user_id"])
            order("id", "asc")
        }
    }

    /**
     * Add a new item to the recent items list for the currently logged in user and
     * update the session list.
     *
     * This method will not add a recent item if either the ID or recent item type is null.
     *
     * @param objectId object id
     * @param type recent item type
     */
    def void addRecentItem(Integer objectId, RecentItemType type) {
        if (objectId && type) {
            addRecentItem(new RecentItem(objectId: objectId, type: type))
        }
    }

    /**
     * Add a new item to the recent items list for the currently logged in user and
     * update the session list.
     *
     * @param item recent item
     */
    def void addRecentItem(RecentItem item) {
        def items = getRecentItems()
        def lastItem = !items.isEmpty() ? items.getAt(-1) : null
        
        // add item only if it is different from the last item added
        try {
            if (!lastItem || !lastItem.equals(item)) {
                item.userId = session['user_id']
                item.save()

                items << item

                if (items.size() > MAX_ITEMS) {
                    def remove = items.subList(5, items.size())
                    remove.each{ it.delete(flush: true) }
                    remove.clear()
                }

                session[SESSION_RECENT_ITEMS] = items
            }

        } catch (Throwable t) {
            log.error("Exception caught adding recent item", t)
            session.error = 'recent.item.failed'
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
