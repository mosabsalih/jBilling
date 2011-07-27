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
package com.sapienter.jbilling.server.util.audit.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class EventLogDAS extends AbstractDAS<EventLogDTO> {
    
    private static final Logger LOG = Logger.getLogger(EventLogDAS.class);
    
    // QUERIES
    private static final String findLastTransition =
        "SELECT max(id) from EventLogDTO" +
        " WHERE eventLogModule.id = " + EventLogger.MODULE_WEBSERVICES +
        " AND eventLogMessage.id = " + EventLogger.USER_TRANSITIONS_LIST +
        " AND company.id = :entity";

    public Integer getLastTransitionEvent(Integer entityId) {
        Query query = getSession().createQuery(findLastTransition);
        query.setParameter("entity", entityId);
        Integer id = (Integer) query.uniqueResult();
        if (id == null) {
            LOG.warn("Can not find max value.");
            // it means that this is the very first time the web service
            // method is called with 'null,null'. Return all then.
            return 0;
        } 
        EventLogDTO latest = find(id);
        return latest.getOldNum();
    }

    public List<EventLogDTO> getEventsByAffectedUser(Integer userId) {
        Criteria criteria = getSession().createCriteria(EventLogDTO.class)
                .add(Restrictions.eq("affectedUser.id", userId))
                .addOrder(Order.desc("createDatetime"));

        return criteria.list();
    }
}
