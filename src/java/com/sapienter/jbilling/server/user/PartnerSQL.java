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

package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.server.util.Constants;

/**
 * @author Emil
 */
public interface PartnerSQL {
    // I would do this with an entity finder if ESQL would support dates!
    static final String duePayout = 
            "select p.id " +
            "  from " + Constants.TABLE_PARTNER + " p, " + 
                        Constants.TABLE_BASE_USER + " bu " +
            " where p.user_id = bu.id " +
            "   and bu.status_id = " + UserDTOEx.STATUS_ACTIVE +
            "   and next_payout_date <= ? " +
            " order by 1 desc";
    
    static final String lastPayout =
            "select max(pp.id) " +
            "  from " + Constants.TABLE_PARTNER_PAYOUT + " pp, " +
                        Constants.TABLE_PAYMENT + " pa " +
            " where partner_id = ? " +
            "   and pp.payment_id = pa.id " +
            "   and pa.result_id in (" + Constants.RESULT_OK + "," +
                                         Constants.RESULT_ENTERED + ")";
            
    // this query is a real pity that is not doable with esql because of the
    // date restriction
    static final String paymentsInPayout = 
            "select pa.id " +
            "  from " + Constants.TABLE_PAYMENT + " pa, " +
                        Constants.TABLE_BASE_USER + " bu, " +
                        Constants.TABLE_CUSTOMER + " cu " +
            " where pa.user_id = bu.id " +
            "   and bu.id = cu.user_id " +
            "   and cu.partner_id = ? " +
            "   and pa.result_id in (" + Constants.RESULT_OK + "," +
                                         Constants.RESULT_ENTERED + ")" +
            "   and pa.create_datetime >= ? " +
            "   and pa.create_datetime < ? " +
            "   and pa.deleted = 0";

    static final String list = 
            "select bu.id, pa.id, bu.user_name, pa.next_payout_date, pa.due_payout " +
            "  from " + Constants.TABLE_PARTNER + " pa, " + 
                        Constants.TABLE_BASE_USER + " bu " +
            " where pa.user_id = bu.id " +
            "   and bu.deleted = 0 " +
            "   and bu.entity_id = ? " +
            " order by 1 desc";
 
    static final String listPayouts =
            "select pp.id, pp.id, pp.starting_date, pp.ending_date, pa.amount " +
            "  from " + Constants.TABLE_PARTNER_PAYOUT + " pp, " +
                        Constants.TABLE_PAYMENT + " pa " +
            " where partner_id = ? " +
            "   and pp.payment_id = pa.id " +
            "   and pa.result_id in (" + Constants.RESULT_OK + "," +
                                         Constants.RESULT_ENTERED + ")" +
            " order by 1 desc";
    
    /*
     * Count the customers owned by a partner, excluding those deleted
     * or suspended
     */
    static final String countCustomers =
            "select count(*) " +
            "  from " + Constants.TABLE_CUSTOMER + " c, " +
                        Constants.TABLE_BASE_USER + " bu " +
            " where c.user_id = bu.id " +
            "   and c.partner_id = ? " +
            "   and bu.deleted = 0 " +
            "   and bu.status_id < 5";
           
}
