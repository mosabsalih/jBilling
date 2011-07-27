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
package com.sapienter.jbilling.server.user.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.common.CommonConstants;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

              
    
public class UserDAS extends AbstractDAS<UserDTO> {
    private static final Logger LOG = Logger.getLogger(UserDAS.class);
   
    private static final String findByCustomField =
        "SELECT a " + 
        "  FROM UserDTO a, ContactMap cm " +
        " WHERE a.company.id = :entity " +
        "   AND a.id = cm.foreignId " +
        "   AND cm.jbillingTable.id = 10 " +
        "   AND cm.contactType.isPrimary = 1 " +
        "   AND cm.contact.contactFields.contactFieldType.id = :type " +
        "   AND cm.contact.contactFields.contactFieldType.content = :content " +
        "   AND a.deleted = 0";

     private static final String findInStatusSQL = 
         "SELECT a " + 
         "  FROM UserDTO a " + 
         " WHERE a.userStatus.id = :status " +
         "   AND a.company.id = :entity " +
         "   AND a.deleted = 0" ;

     private static final String findNotInStatusSQL =
         "SELECT a " +
         "  FROM UserDTO a " + 
         " WHERE a.userStatus.id <> :status " +
         "   AND a.company.id = :entity " +
         "   AND a.deleted = 0";

     private static final String findAgeingSQL = 
         "SELECT a " + 
         "  FROM UserDTO a " + 
         " WHERE a.userStatus.id > " + UserDTOEx.STATUS_ACTIVE +
         "   AND a.userStatus.id <> " + UserDTOEx.STATUS_DELETED +
         "   AND a.customer.excludeAging = 0 " +
         "   AND a.company.id = :entity " +
         "   AND a.deleted = 0";

    public UserDTO findRoot(String username) {
        if (username == null || username.length() == 0) {
            LOG.error("can not find an empty root: " + username);
            return null;
        }
        // I need to access an association, so I can't use the parent helper class
        Criteria criteria = getSession().createCriteria(UserDTO.class)
            .add(Restrictions.eq("userName", username))
            .add(Restrictions.eq("deleted", 0))
            .createAlias("roles", "r")
                .add(Restrictions.eq("r.id", CommonConstants.TYPE_ROOT));
        
        criteria.setCacheable(true); // it will be called over an over again
        
        return (UserDTO) criteria.uniqueResult();
    }

    public UserDTO findWebServicesRoot(String username) {
        if (username == null || username.length() == 0) {
            LOG.error("can not find an empty root: " + username);
            return null;
        }
        // I need to access an association, so I can't use the parent helper class
        Criteria criteria = getSession().createCriteria(UserDTO.class)
            .add(Restrictions.eq("userName", username))
            .add(Restrictions.eq("deleted", 0))
            .createAlias("roles", "r")
                .add(Restrictions.eq("r.id", CommonConstants.TYPE_ROOT))
            .createAlias("permissions", "p")
                .add(Restrictions.eq("p.permission.id", 120));
        
        criteria.setCacheable(true); // it will be called over an over again
        
        return (UserDTO) criteria.uniqueResult();
    }

    public UserDTO findByUserName(String username, Integer entityId) {
        // I need to access an association, so I can't use the parent helper class
        Criteria criteria = getSession().createCriteria(UserDTO.class)
                .add(Restrictions.eq("userName", username))
                .add(Restrictions.eq("deleted", 0))
                .createAlias("company", "e")
                    .add(Restrictions.eq("e.id", entityId));
        
        return (UserDTO) criteria.uniqueResult();
    }
    
    public List<UserDTO> findInStatus(Integer entityId, Integer statusId) {
        Query query = getSession().createQuery(findInStatusSQL);
        query.setParameter("entity", entityId);
        query.setParameter("status", statusId);
        return query.list();
    }
    
    public List<UserDTO> findNotInStatus(Integer entityId, Integer statusId) {
        Query query = getSession().createQuery(findNotInStatusSQL);
        query.setParameter("entity", entityId);
        query.setParameter("status", statusId);
        return query.list();
    }

    public List<UserDTO> findByCustomField(Integer entityId, Integer typeId, String value) {
        Query query = getSession().createQuery(findByCustomField);
        query.setParameter("entity", entityId);
        query.setParameter("type", typeId);
        query.setParameter("content", value);
        return query.list();
    }
    
    public List<UserDTO> findAgeing(Integer entityId) {
        Query query = getSession().createQuery(findAgeingSQL);
        query.setParameter("entity", entityId);
        return query.list();
    }
    

}
