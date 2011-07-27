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

package com.sapienter.jbilling.server.customer;

import com.sapienter.jbilling.server.util.Constants;

/**
 * @author Emil
 */
public interface CustomerSQL {

    // ROOT: all the STAFF users within its entity
    static final String listRoot = 
        "select c.id, c.id, a.organization_name, a.last_name, a.first_name, " +
        "       c.user_name " +
        "from contact a, contact_map b, base_user c, jbilling_table d, " +
        "     contact_type ct, user_role_map urm " +
        "where a.id = b.contact_id" +
        "  and b.foreign_id = c.id" +
        "  and b.table_id = d.id" +
        "  and b.type_id = ct.id " +
        "  and ct.is_primary = 1 " +
        "  and d.name = 'base_user'" +
        "  and c.deleted = 0 " +
        "  and a.deleted = 0 " +
        "  and c.id = urm.user_id " +
        "  and urm.role_id in (2,3,4) " + // no customers or internals
        "  and c.entity_id = ? " +
        " order by 3,4,5";


    // CLERK: same as root, but restricted to customers and partners
    static final String listClerk = 
        "select c.id, c.id, a.organization_name, a.last_name, a.first_name, " +
        "       c.user_name " +
        "from contact a, contact_map b, base_user c, jbilling_table d, " +
        "     contact_type ct, user_role_map urm  " +
        "where a.id = b.contact_id" +
        "  and b.foreign_id = c.id" +
        "  and b.type_id = ct.id " +
        "  and ct.is_primary = 1 " +
        "  and b.table_id = d.id" +
        "  and d.name = 'base_user'" +
        "  and c.id = urm.user_id " +
        "  and urm.role_id in (3,4) " + // no customers or internals or admins
        "  and c.deleted = 0 " +
        "  and a.deleted = 0 " +
        "  and c.entity_id = ? " +
        " order by 3,4,5";

    // PARTNER: will show only customers that belong to this partner
    static final String listPartner = 
        "select c.id, c.id, a.organization_name, a.last_name, a.first_name, " +
        "       c.user_name " +
        "from contact a, contact_map b, base_user c, jbilling_table d, " +
        "     customer cu, partner pa, " +
        "     contact_type ct " +
        "where a.id = b.contact_id" +
        "  and b.foreign_id = c.id" +
        "  and b.table_id = d.id" +
        "  and d.name = 'base_user'" +
        "  and c.deleted = 0 " +
        "  and b.type_id = ct.id " +
        "  and ct.is_primary = 1 " +
        "  and a.deleted = 0 " +
        "  and c.entity_id = ? " +
        "  and cu.partner_id = pa.id " +
        "  and pa.user_id = ? " +
        "  and cu.user_id = c.id ";
    
    // customer list, takes only customers.
    // it excluded sub-accounts (child customers)
    static final String listCustomers = 
        "select c.id, c.id, a.organization_name, a.last_name, a.first_name, " +
        "       c.user_name " +
        "from contact a, contact_map b, base_user c, jbilling_table d, " +
        "     user_role_map urm, customer cu, " +
        "     contact_type ct " +
        "where a.id = b.contact_id" +
        "  and b.foreign_id = c.id" +
        "  and b.table_id = d.id" +
        "  and b.type_id = ct.id " +
        "  and ct.is_primary = 1 " +
        "  and d.name = 'base_user'" +
        "  and c.entity_id = ? " +
        "  and c.deleted = 0 " +
        "  and a.deleted = 0 " +
        "  and c.status_id != 8 " +
        "  and c.id = urm.user_id " +
        "  and urm.role_id = " + Constants.TYPE_CUSTOMER +
        "  and cu.user_id = c.id " +
        "  and ( cu.parent_id is null or cu.invoice_child = 1)";
    
    static final String listCustomersCCFiler = 
        "and c.id in ( " +
        "   select ma.user_id " +
        "     from user_credit_card_map ma, credit_card cc " +
        "    where cc.id = ma.credit_card_id " +
        "      and cc.cc_number_plain like ? " +
        ")";

    // sub-accounts: all the customers belonigng to another customer
    static final String listSubaccounts = 
        "select c.id, c.id, a.organization_name, a.last_name, a.first_name, " +
        "       c.user_name " +
        "from contact a, contact_map b, base_user c, jbilling_table d, " +
        "     user_role_map urm, customer cu, " +
        "     contact_type ct " +
        "where a.id = b.contact_id" +
        "  and b.foreign_id = c.id" +
        "  and b.table_id = d.id" +
        "  and b.type_id = ct.id " +
        "  and ct.is_primary = 1 " +
        "  and d.name = 'base_user'" +
        "  and cu.parent_id = ? " +
        "  and c.deleted = 0 " +
        "  and a.deleted = 0 " +
        "  and c.status_id != 8 " +
        "  and c.id = urm.user_id " +
        "  and urm.role_id = " + Constants.TYPE_CUSTOMER +
        "  and cu.user_id = c.id " +
        " order by 3,4,5";

}
