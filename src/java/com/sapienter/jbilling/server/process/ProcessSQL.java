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

package com.sapienter.jbilling.server.process;

public interface ProcessSQL {
    // Internal gets all the invoices ever
    static final String generalList = 
        "select id, id, billing_date " +
        "  from billing_process " +
        " where entity_id = ? " +
        "   and is_review = 0 " +
        " order by 1";

    static final String lastId =
        "select max(id) " +
        "  from billing_process" +
        " where entity_id = ?" +
        "   and is_review = 0 ";
    
    // needed to avoid getting into a trasaction in the billingProcess.trigger
    // since Collections have to be in transactions
    static String findToRetry =
        "select id " +
        " from billing_process " + 
        "where entity_id = ? " +
        "  and is_review = 0 " +
        "  and retries_to_do > 0";

    static String findProcessRunUsersBase =
            "select u.id, u.id, c.organization_name, c.last_name, c.first_name, u.user_name " +
            "from process_run_user pru inner join base_user u on pru.user_id = u.id " +
            "inner join contact_map cm on u.id = cm.foreign_id " +
            "inner join contact c on c.id = cm.contact_id " +
            "where pru.process_run_id = ? and u.deleted = 0 and c.deleted = 0 ";

    static String findSucceededUsers =
            findProcessRunUsersBase +
            "and pru.status = 1";

    static String findFailedUsers =
            findProcessRunUsersBase +
            "and pru.status = 0";
}


