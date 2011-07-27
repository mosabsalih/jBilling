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
package com.sapienter.jbilling.server.user.contact.db;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class ContactDAS extends AbstractDAS<ContactDTO> {
    public static final String findContactSQL =  
        "SELECT c " +
        "  FROM ContactDTO c, JbillingTable d " +
        " WHERE c.contactMap.jbillingTable.id = d.id " +
        "   AND d.name = :tableName " +
        "   AND c.contactMap.contactType.id = :typeId " +
        "   AND c.contactMap.foreignId = :userId ";

    public static final String findSimpleContactSQL =  
        "SELECT c " +
        "  FROM ContactDTO c, JbillingTable d " +
        " WHERE c.contactMap.jbillingTable.id = d.id " +
        "   AND d.name = :tableName " +
        "   AND c.contactMap.foreignId = :id ";

    public ContactDTO findPrimaryContact(Integer userId) {
        return findByCriteriaSingle(Restrictions.eq("userId", userId));
    }
    
    public ContactDTO findContact(Integer userId, Integer typeId) {
        Query query = getSession().createQuery(findContactSQL);
        query.setParameter("typeId", typeId);
        query.setParameter("userId", userId);
        query.setParameter("tableName", Constants.TABLE_BASE_USER);
        return (ContactDTO) query.uniqueResult();
    }

    public ContactDTO findEntityContact(Integer entityId) {
        Query query = getSession().createQuery(findSimpleContactSQL);
        query.setParameter("id", entityId);
        query.setParameter("tableName", Constants.TABLE_ENTITY);
        query.setCacheable(true);
        return (ContactDTO) query.uniqueResult();
    }

    public ContactDTO findInvoiceContact(Integer invoiceId) {
        Query query = getSession().createQuery(findSimpleContactSQL);
        query.setParameter("id", invoiceId);
        query.setParameter("tableName", Constants.TABLE_INVOICE);
        return (ContactDTO) query.uniqueResult();
    }

}
