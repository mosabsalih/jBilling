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

/*
 * Created on Jul 9, 2005
 *
 */
package com.sapienter.jbilling.server.user;

/**
 * @author Emil
 *
 */
public interface EntitySQL {
    // needed for the billing process, to avoid starting a transaction
    // since J2EE Collections have always to be in a transaction :(
    static final String listAll = 
        "select id" +
        "  from entity" +
        " order by 1";
    
    // another query that should not exist. Please remove when entities
    // are replaced by JPAs
    static final String getTables = 
        "select name, id " +
        "  from jbilling_table";
 
    static final String findRoot = 
        "select id " +
        "  from base_user b, user_role_map m" +
        " where entity_id = ? " +
        "   and m.user_id = b.id " +
        "   and m.role_id = 2 " +
        " order by 1";
}
