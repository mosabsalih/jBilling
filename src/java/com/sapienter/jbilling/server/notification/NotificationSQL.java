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

package com.sapienter.jbilling.server.notification;

import com.sapienter.jbilling.server.util.Constants;

/**
 * @author Emil
 */
public interface NotificationSQL {

    static final String listTypes = 
        "select nmt.id, i.content " +
        "  from notification_message_type nmt, international_description i, " +
        "       jbilling_table bt " + 
        " where i.table_id = bt.id " +
        "   and bt.name = 'notification_message_type' " + 
        "   and i.foreign_id = nmt.id " + 
        "   and i.language_id = ? " +
        "   and i.psudo_column = 'description'";

    static final String allEmails = 
        "select c.email " +
        "  from base_user a, contact_map b, contact c, jbilling_table d, " +
        "       contact_type ct, user_role_map urm " +
        " where a.id = b.foreign_id " +
        "   and b.type_id = ct.id " +
        "   and a.id = urm.user_id " +
        "   and urm.role_id = " + Constants.TYPE_CUSTOMER +
        "   and ct.is_primary = 1 " +
        "   and b.table_id = d.id " +
        "   and b.contact_id = c.id " +
        "   and d.name = 'base_user' " +
        "   and c.email is not null " +
        "   and a.entity_id = ?";

}
