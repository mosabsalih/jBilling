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

package com.sapienter.jbilling.server.invoice;


public interface InvoiceSQL {

    static final String payableByUser = 
       "select i.id, i.public_number, i.id, i.create_datetime, i.due_date, " +
        "       c.symbol, i.total, i.balance " +
        "  from invoice i, currency c " +
        " where i.user_id = ? " +
        "   and (i.balance >= 0.01 or i.balance <= -0.01) " +
        "   and i.is_review = 0 " +
        "   and i.currency_id = c.id " +
        "   and i.deleted = 0 " +
        " order by 1 desc";

    // Internal gets all the invoices ever
    static final String internalList = 
        "select i.id, i.public_number, bu.user_name, i.id, i.create_datetime, i.due_date, " +
        "       c.symbol, i.total, i.balance, i.status_id " +
        "  from invoice i, base_user bu, currency c " +
        " where i.user_id = bu.id " +
        "   and i.currency_id = c.id " +
        "   and i.is_review = 0 " +
        "   and i.deleted = 0 " +
        " order by 1 desc";
        
    // Root-Clerk gets all the entity's invoices
    static final String rootClerkList = 
        "select i.id, i.public_number, bu.user_name, co.organization_name,i.id, i.create_datetime, " + 
        "       c.symbol, i.total, i.balance " +
        "  from invoice i, base_user bu, currency c , contact co " +
        " where i.user_id = bu.id " +
        "   and i.currency_id = c.id " +
        "   and bu.entity_id = ? " +
        "   and i.is_review = 0 " +
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id ";

    // The partner get's only its users
    static final String partnerList = 
        "select i.id, i.public_number, bu.user_name, co.organization_name,i.id, i.create_datetime, " + 
        "       c.symbol, i.total, i.balance " +
        "  from invoice i, base_user bu, partner pa, contact co, " +
        "       customer cu, currency c " +
        " where i.user_id = bu.id " +
        "   and i.currency_id = c.id " +
        "   and bu.entity_id = ? " +
        "   and cu.partner_id = pa.id " +
        "   and pa.user_id = ? " +
        "   and i.is_review = 0 " +
        "   and cu.user_id = bu.id " +        
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id ";


    // A customer only sees its own
    static final String customerList = 
        "select i.id, i.public_number, bu.user_name, co.organization_name,i.id, i.create_datetime, " + 
        "       c.symbol, i.total, i.balance " +
        "  from invoice i, base_user bu, currency c, contact co " +
        " where i.user_id = bu.id " +
        "   and i.currency_id = c.id " +
        "   and bu.id = ? " +
        "   and i.is_review = 0 " +
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id ";
        

    // Invoices generated in a billing process
    static final String processList = 
        "select i.id, i.public_number, i.id, bu.user_name, co.organization_name, " +
        "       i.due_date, c.symbol, i.total, i.status_id " +
        "  from invoice i, base_user bu, currency c, contact co " +
        " where i.billing_process_id = ? " +
        "   and bu.id = i.user_id " +
        "   and i.currency_id = c.id " +
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id " +
        " order by 5, 1";
    
    static final String processPrintableList = 
        "select i.id, i.public_number, i.id, bu.user_name, co.organization_name, " +
        "       i.due_date, c.symbol, i.total, i.status_id " +
        "  from invoice i, base_user bu, currency c, contact co, customer cu " +
        " where i.billing_process_id = ? " +
        "   and bu.id = i.user_id " +
        "   and i.currency_id = c.id " +
        "   and i.deleted = 0 " +
        "   and cu.user_id = bu.id " +
        "   and cu.invoice_delivery_method_id in (2,3) " +
        "   and co.user_id = bu.id " +
        " order by 5, 1";

    // Invoices generated in a range
    static final String rangeList = 
        "select i.id " +
        "  from invoice i, base_user bu, currency c, contact co " +
        " where i.id between ? and ? " +
        "   and bu.id = i.user_id " +
        "   and bu.entity_id = ? " +        
        "   and i.is_review = 0 " +
        "   and i.currency_id = c.id " +
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id " +
        " order by i.id";
    
    // Invoices generated for a customer
    static final String custList = 
        "select i.id " +
        "  from invoice i, base_user bu, currency c, contact co " +
        " where i.user_id = ? " +
        "   and bu.id = i.user_id " +
        "   and i.is_review = 0 " +
        "   and i.currency_id = c.id " +
        "   and i.deleted = 0 " +
        "   and co.user_id = bu.id " +
        " order by i.id";

    // Last invoice id for a user
    static final String lastIdbyUser =
    "select max(i.id) " +
    "  from invoice i " +
    " where i.user_id = ? " +
    "   and i.deleted = 0 " +
    "   and i.is_review = 0";

    // Last invoice id for a user that contains a line item w/ particular type id
    static final String lastIdbyUserAndItemType =
    "select max(i.id) " +
    "  from invoice i " +
    " inner join invoice_line on invoice_line.invoice_id = i.id" +
    " inner join item_type_map on item_type_map.item_id = invoice_line.item_id " +
    " where i.user_id = ? " +
    "   and item_type_map.type_id = ? " +
    "   and i.deleted = 0 " +
    "   and i.is_review = 0";
    
    static final String previous = 
    "select max(i.id) " +
    "  from invoice i " +
    " where i.user_id = ? " +
    "   and i.deleted = 0 " +
    "   and i.is_review = 0" +
    "   and i.id < ?";
        
    
    // All the invoices to send reminders
    static final String toRemind  = 
    "select i.id " +
    "  from invoice i, base_user b " +
    " where i.user_id = b.id " +
    "   and b.deleted = 0 " +
    "   and i.deleted = 0 " +
    "   and i.is_review = 0 " +
    "   and i.status_id = 27 " +
    "   and i.due_date > ? " +
    "   and i.create_datetime <= ? " +
    "   and (i.last_reminder is null or " +
    "        i.last_reminder <= ?)" +
    "   and b.entity_id = ?";
    
    // Invoice in ageing: any invoices that make this user applicable 
    // to the ageing process (then what happends depends on the ageing config)
    static final String getOverdueForAgeing =
    "select i.id " +
    "  from invoice i " +
    "  where i.is_review = 0 " +
    "  and i.due_date < ? " +
    "  and i.deleted = 0 " +
    "  and i.user_id = ? " +
    "  and i.status_id != 26 " +
    "  and i.id != ?";
 

    // All the invoices created for a period of time
    static final String getByDate =
    "select i.id " +
    "  from invoice i, base_user b " +
    " where b.entity_id = ? " +
    "   and i.user_id = b.id " +
    "   and i.is_review = 0 " +
    "   and i.deleted = 0 " +
    "   and i.create_timestamp >= ? " +
    "   and i.create_timestamp < ? " +
    " order by i.id";

    static final String getIDfromNumber = 
        "select min(i.id) " +
        "  from invoice i, base_user b " +
        " where b.entity_id = ? " +
        "   and i.user_id = b.id " +
        "   and i.is_review = 0 " +
        "   and i.deleted = 0 " +
        "   and i.public_number = ? ";

}


