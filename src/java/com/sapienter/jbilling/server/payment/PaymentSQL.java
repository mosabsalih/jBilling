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

package com.sapienter.jbilling.server.payment;

import com.sapienter.jbilling.common.Constants;

public interface PaymentSQL {
        
    // Root-Clerk gets all the entity's payments
    static final String rootClerkList = 
        "select p.id, p.id, u.user_name, co.organization_name, c.symbol, " +
        "       p.amount, p.create_datetime, i.content, i2.content " +
        "  from payment p, base_user u, international_description i, " + 
        "       international_description i2, payment_method pm, " +
        "       jbilling_table bt, jbilling_table bt2, currency c, contact co, " + 
        "       payment_result pr " +
        " where p.user_id = u.id " +
        "   and p.is_refund = ?" +
        "   and p.is_preauth = 0" +
        "   and p.method_id = pm.id " +
        "   and p.currency_id = c.id " +
        "   and u.entity_id = ? " +
        "   and i.table_id = bt.id " +
        "   and bt.name = 'payment_method' " +
        "   and i.foreign_id = pm.id " +
        "   and i.language_id = ? " +
        "   and i.psudo_column = 'description' " +
        "   and co.user_id = u.id " +
        "   and p.id not in (select payment_id from partner_payout where payment_id is not null) " +
        "   and p.deleted = 0 " +
        "   and i2.table_id = bt2.id" +
        "   and i2.language_id = i.language_id " +
        "   and i2.psudo_column = 'description' " +
        "   and bt2.name= 'payment_result'" +
        "   and pr.id = p.result_id" +
        "   and pr.id = i2.foreign_id";

    // The partner get's only its users
    static final String partnerList = 
        "select p.id, p.id, u.user_name, co.organization_name, c.symbol, " +
        "       p.amount, p.create_datetime, i.content " +
        "  from payment p, base_user u, international_description i, " +
        "       payment_method pm, jbilling_table bt, partner pa, " +
        "       customer cu, currency c, contact co " +
        " where p.user_id = u.id " +
        "   and p.is_refund = ?" +
        "   and p.is_preauth = 0" +
        "   and p.method_id = pm.id " +
        "   and p.currency_id = c.id " +
        "   and u.entity_id = ? " +
        "   and cu.partner_id = pa.id " +
        "   and pa.user_id = ? " +
        "   and cu.user_id = u.id " +        
        "   and i.table_id = bt.id " +
        "   and bt.name = 'payment_method' " +
        "   and i.foreign_id = pm.id " +
        "   and i.language_id = ? " +
        "   and i.psudo_column = 'description' " +
        "   and p.id not in (select payment_id from partner_payout where payment_id is not null) " +
        "   and co.user_id = u.id " +
        "   and p.deleted = 0 ";        

    // A customer only sees its own
    static final String customerList = 
        "select p.id, p.id, u.user_name, co.organization_name, c.symbol, " +
        "       p.amount, p.create_datetime, i.content, i2.content " +
        "  from payment p, base_user u, international_description i, " +
        "       international_description i2, payment_method pm, " +
        "       jbilling_table bt, jbilling_table bt2, currency c, contact co, " +  
        "       payment_result pr " +
        " where p.user_id = u.id " +
        "   and p.is_refund = ?" +
        "   and p.is_preauth = 0" +
        "   and p.method_id = pm.id " +
        "   and p.currency_id = c.id " +
        "   and u.id = ? " +
        "   and i.table_id = bt.id " +
        "   and bt.name = 'payment_method' " +
        "   and i.foreign_id = pm.id " +
        "   and i.language_id = ? " +
        "   and i.psudo_column = 'description' " +
        "   and co.user_id = u.id " +
        "   and p.deleted = 0 " +
        "   and (p.result_id = " + Constants.RESULT_OK  + 
        "         or p.result_id=" + Constants.RESULT_ENTERED + ")" +
        "   and i2.table_id = bt2.id" +
        "   and i2.language_id = i.language_id " +
        "   and i2.psudo_column = 'description' " +
        "   and bt2.name= 'payment_result'" +
        "   and pr.id = p.result_id" +
        "   and pr.id = i2.foreign_id";

    // The refundable payments are those only of a customer (like customerList)
    // but that have been not refunded previously
    static final String refundableList = 
        "select p.id, p.id, u.user_name, c.symbol, p.amount, " +
        "       p.create_datetime, i.content, i2.content " +
        "  from payment p, base_user u, international_description i, " +
        "       payment_method pm, jbilling_table bt, currency c," +
        "       international_description i2, jbilling_table bt2, " +
        "       payment_result pr " +
        " where p.user_id = u.id " +
        "   and p.is_refund = ?" +
        "   and p.is_preauth = 0" +
        "   and p.method_id = pm.id " +
        "   and p.currency_id = c.id " +
        "   and u.id = ? " +
        "   and i.table_id = bt.id " +
        "   and bt.name = 'payment_method' " +
        "   and i.foreign_id = pm.id " +
        "   and i.language_id = ? " +
        "   and i.psudo_column = 'description' " +
        "   and i2.table_id = bt2.id" +
        "   and i2.language_id = i.language_id " +
        "   and i2.psudo_column = 'description' " +
        "   and bt2.name= 'payment_result'" +
        "   and pr.id = p.result_id" +
        "   and pr.id = i2.foreign_id" +
        "   and p.deleted = 0 " +    
        "   and p.id not in ( " +
        "        select payment_id " +
        "          from payment " +
        "         where is_refund = 1 " +
        "           and payment_id is not null " +
        "   )" +
        " order by 1 desc";    
    
    static final String getLatest = 
        "select max(id) " +
        "  from payment " +
        " where deleted = 0 " +
        "   and user_id = ?";
    
}




