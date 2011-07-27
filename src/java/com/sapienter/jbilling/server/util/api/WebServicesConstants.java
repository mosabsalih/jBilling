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
package com.sapienter.jbilling.server.util.api;

/**
 * Constants used by AxisAPI.
 * @author Narinder
 *
 */
public class WebServicesConstants {
    public final static String APPLY_PAYMENT = "applyPayment";
    public final static String CREATE = "create";
    public final static String CREATE_ITEM = "createItem";
    public final static String CREATE_ORDER = "createOrder";
    public final static String CREATE_ORDER_AND_INVOICE = "createOrderAndInvoice";
    public final static String CREATE_USER = "createUser";
    public final static String DELETE_ORDER = "deleteOrder";
    public final static String DELETE_USER = "deleteUser";
    public final static String DELETE_INVOICE = "deleteInvoice";
    public final static String GET_ALL_ITEMS = "getAllItems";
    public final static String GET_INVOICES_BY_DATE = "getInvoicesByDate";
    public final static String GET_INVOICE = "getInvoiceWS";
    public final static String GET_LAST_INVOICES = "getLastInvoices";
    public final static String GET_LAST_INVOICES_BY_ITEM_TYPE = "getLastInvoicesByItemType";
    public final static String GET_LATEST_INVOICE = "getLatestInvoice";
    public final static String GET_LATEST_INVOICE_BY_ITEM_TYPE = "getLatestInvoiceByItemType";
    public final static String GET_LAST_ORDERS = "getLastOrders";
    public final static String GET_LAST_ORDERS_BY_ITEM_TYPE = "getLastOrdersByItemType";
    public final static String GET_LATEST_ORDER = "getLatestOrder";
    public final static String GET_LATEST_ORDER_BY_ITEM_TYPE = "getLatestOrderByItemType";
    public final static String GET_LAST_PAYMENTS = "getLastPayments";
    public final static String GET_LATEST_PAYMENT = "getLatestPayment";
    public final static String GET_ORDER = "getOrder";
    public final static String GET_ORDER_LINE = "getOrderLine";
    public final static String GET_ORDER_BY_PERIOD = "getOrderByPeriod";
    public final static String GET_PAYMENT = "getPayment";
    public final static String GET_USER_TRANSITIONS = "getUserTransitions";
    public final static String GET_USER_TRANSITIONS_AFTER_ID = "getUserTransitionsAfterId";
    public final static String GET_USER = "getUserWS";
    public final static String GET_USER_ID = "getUserId";
    public final static String GET_USERS_BY_CUSTOM_FIELD = "getUsersByCustomField";
    public final static String GET_USERS_IN_STATUS = "getUsersInStatus";
    public final static String GET_USERS_NOT_IN_STATUS = "getUsersNotInStatus";
    public final static String CREATE_ORDER_PREAUTH = "createOrderPreAuthorize";
    public final static String UPDATE_CREDIT_CARD = "updateCreditCard";
    public final static String UPDATE_USER = "updateUser";
    public final static String UPDATE_ORDER = "updateOrder";
    public final static String UPDATE_ORDERLINE = "updateOrderLine";
    public final static String UPDATE_USER_CONTACT = "updateUserContact";
    public final static String PAY_INVOICE = "payInvoice";
    public final static String AUTHENTICATE = "authenticate";
    public final static String GET_USERS_BY_CCNUMBER = "getUsersByCreditCard";
    public final static String GET_ITEM = "getItem";
    public final static String RATE_ORDER = "rateOrder";
    public final static String UPDATE_ITEM = "updateItem";
    
    /*
     *  return values for authentication method
     */
    // ok
    public final static Integer AUTH_OK = new Integer(0);
    // invalid user name or password
    public final static Integer AUTH_WRONG_CREDENTIALS = new Integer(1);
    // same as previous, but on this attempt the password has be changed
    // to lock the account
    public final static Integer AUTH_LOCKED = new Integer(2);
    // the password is good, but too old. Needs to call update
    public final static Integer AUTH_EXPIRED = new Integer(3);
}
