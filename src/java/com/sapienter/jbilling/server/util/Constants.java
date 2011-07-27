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

package com.sapienter.jbilling.server.util;

import com.sapienter.jbilling.common.CommonConstants;

/**
 * @author emilc
 *
 */
public final class Constants implements CommonConstants {
    /*
     * DATA BASE CONSTANTS
     * These values are in the database, should be initialized by the
     * InitDataBase program and remain static.
     */
    // the agreed maximum length for a varchar.
    public static final int MAX_VARCHAR_LENGTH = 1000;
    // this should be equal to hibernate.jdbc.batch_size
    public static final int HIBERNATE_BATCH_SIZE = 100;
    // tables
    public static final String TABLE_ITEM = "item";
    public static final String TABLE_PUCHASE_ORDER = "purchase_order";
    public static final String TABLE_ORDER_PROCESSING_RULE = "order_processing_rule";
    public static final String TABLE_ORDER_PERIOD = "order_period";
    public static final String TABLE_ORDER_LINE_TYPE = "order_line_type";
    public static final String TABLE_BILLING_PROCESS = "billing_process";
    public static final String TABLE_BILLING_PROCESS_RUN = "process_run";
    public static final String TABLE_BILLING_PROCESS_RUN_TOTAL = "process_run_total";
    public static final String TABLE_BILLING_PROCESS_RUN_TOTAL_PM = "process_run_total_pm";
    public static final String TABLE_BILLING_PROCESS_CONFIGURATION = "billing_process_configuration";
    public static final String TABLE_INVOICE = "invoice";
    public static final String TABLE_INVOICE_STATUS = "invoice_status";
    public static final String TABLE_INVOICE_LINE= "invoice_line";
    public static final String TABLE_EVENT_LOG = "event_log";
    public static final String TABLE_INTERNATIONAL_DESCRIPTION = "international_description";
    public static final String TABLE_LANGUAGE = "language";
    public static final String TABLE_ENTITY = "entity";
    public static final String TABLE_USER_TYPE = "user_type";
    public static final String TABLE_BASE_USER = "base_user";
    public static final String TABLE_CUSTOMER = "customer";
    public static final String TABLE_PERIOD_UNIT = "period_unit";
    public static final String TABLE_ORDER_BILLING_TYPE = "order_billing_type";
    public static final String TABLE_ORDER_STATUS = "order_status";
    public static final String TABLE_ORDER_LINE = "order_line";
    public static final String TABLE_PLUGGABLE_TASK_TYPE_CATEGORY = "pluggable_task_type_category";
    public static final String TABLE_PLUGGABLE_TASK_TYPE = "pluggable_task_type";
    public static final String TABLE_PLUGGABLE_TASK = "pluggable_task";
    public static final String TABLE_PLUGGABLE_TASK_PARAMETER = "pluggable_task_parameter";
    public static final String TABLE_CONTACT = "contact";
    public static final String TABLE_CONTACT_FIELD = "contact_field";
    public static final String TABLE_CONTACT_FIELD_TYPE = "contact_field_type";
    public static final String TABLE_CONTACT_TYPE = "contact_type";
    public static final String TABLE_CONTACT_MAP = "contact_map";
    public static final String TABLE_INVOICE_LINE_TYPE = "invoice_line_type";
    public static final String TABLE_PAYMENT = "payment";
    public static final String TABLE_PAYMENT_INFO_CHEQUE = "payment_info_cheque";
    public static final String TABLE_PAYMENT_RESULT = "payment_result";
    public static final String TABLE_PAYMENT_METHOD = "payment_method";
    public static final String TABLE_PAYMENT_INVOICE_MAP = "payment_invoice";
    public static final String TABLE_EVENT_LOG_MODULE = "event_log_module";
    public static final String TABLE_EVENT_LOG_MESSAGE = "event_log_message";
    public static final String TABLE_ORDER_PROCESS = "order_process";
    public static final String TABLE_PREFERENCE = "preference";
    public static final String TABLE_PREFERENCE_TYPE = "preference_type";
    public static final String TABLE_NOTIFICATION_MESSAGE = "notification_message";
    public static final String TABLE_NOTIFICATION_MESSAGE_SECTION = "notification_message_section";
    public static final String TABLE_NOTIFICATION_MESSAGE_TYPE = "notification_message_type";
    public static final String TABLE_NOTIFICATION_MESSAGE_LINE = "notification_message_line";
    public static final String TABLE_NOTIFICATION_MESSAGE_ARCHIVE = "notification_message_arch";
    public static final String TABLE_NOTIFICATION_MESSAGE_ARCHIVE_LINE = "notification_message_arch_line";
    public static final String TABLE_REPORT = "report";
    public static final String TABLE_REPORT_TYPE = "report_type";
    public static final String TABLE_PERMISSION = "permission";
    public static final String TABLE_PERMISSION_TYPE = "permission_type";
    public static final String TABLE_ROLE= "role";
    public static final String TABLE_PERMISSION_ROLE_MAP= "permission_role_map";
    public static final String TABLE_USER_ROLE_MAP= "user_role_map";
    public static final String TABLE_MENU_OPTION = "menu_option";
    public static final String TABLE_COUNTRY = "country";
    public static final String TABLE_PARTNER = "partner";
    public static final String TABLE_PARTNER_RANGE = "partner_range";
    public static final String TABLE_PARTNER_PAYOUT = "partner_payout";
    public static final String TABLE_USER_STATUS = "user_status";
    public static final String TABLE_USER_SUBSCRIBER_STATUS = "subscriber_status";
    public static final String TABLE_ITEM_TYPE = "item_type";
    public static final String TABLE_ITEM_USER_PRICE= "item_user_price";
    public static final String TABLE_PROMOTION= "promotion";
    public static final String TABLE_CREDIT_CARD= "credit_card";
    public static final String TABLE_USER_CREDIT_CARD_MAP= "user_credit_card_map";
    public static final String TABLE_PAYMENT_AUTHORIZATION="payment_authorization";
    public static final String TABLE_ENTITY_PAYMENT_METHOD_MAP = "entity_payment_method_map";
    public static final String TABLE_CURRENCY = "currency";
    public static final String TABLE_CURRENCY_ENTITY_MAP = "currency_entity_map";
    public static final String TABLE_CURRENCY_EXCHANGE= "currency_exchange";
    public static final String TABLE_ITEM_PRICE = "item_price";
    public static final String TABLE_AGEING_ENTITY_STEP = "ageing_entity_step";
    public static final String TABLE_INVOICE_DELIVERY_METHOD = "invoice_delivery_method";
    public static final String TABLE_ENTITY_DELIVERY_METHOD_MAP = "entity_delivery_method_map";
    public static final String TABLE_PAPER_INVOICE_BATCH = "paper_invoice_batch";
    public static final String TABLE_ACH = "ach";
    public static final String TABLE_LIST_ENTITY = "list_entity";
    public static final String TABLE_LIST_FIELD_ENTITY = "list_field_entity";
    public static final String TABLE_MEDIATION_CFG = "mediation_cfg";
    public static final String TABLE_BLACKLIST = "blacklist";
    public static final String TABLE_GENERIC_STATUS_TYPE = "generic_status_type";
    public static final String TABLE_GENERIC_STATUS = "generic_status";
    public static final String TABLE_ORDER_LINE_PROVISIONING_STATUS = "order_line_provisioning_status";
    public static final String TABLE_MEDIATION_RECORD_STATUS = "mediation_record_status";
    public static final String TABLE_PROCESS_RUN_STATUS = "process_run_status";
    public static final String TABLE_NOTIFICATION_CATEGORY = "notification_category";

    // order line types
    public static final Integer ORDER_LINE_TYPE_ITEM = new Integer(1);
    public static final Integer ORDER_LINE_TYPE_TAX = new Integer(2);
    public static final Integer ORDER_LINE_TYPE_PENALTY = new Integer(3);
    
    // order periods. This are those NOT related with any single entity
    public static final Integer ORDER_PERIOD_ONCE = new Integer(1);
    
    // period unit types
    public static final Integer PERIOD_UNIT_MONTH = new Integer(1);
    public static final Integer PERIOD_UNIT_WEEK = new Integer(2);
    public static final Integer PERIOD_UNIT_DAY = new Integer(3);
    public static final Integer PERIOD_UNIT_YEAR= new Integer(4);
    
    // order billing types
    public static final Integer ORDER_BILLING_PRE_PAID = new Integer(1);
    public static final Integer ORDER_BILLING_POST_PAID = new Integer(2);
    
    // pluggable tasks categories
    public static final Integer PLUGGABLE_TASK_PROCESSING_ORDERS = new Integer(1);
    public static final Integer PLUGGABLE_TASK_ORDER_FILTER = new Integer(2);
    public static final Integer PLUGGABLE_TASK_INVOICE_FILTER = new Integer(3);
    public static final Integer PLUGGABLE_TASK_INVOICE_COMPOSITION = new Integer(4);
    public static final Integer PLUGGABLE_TASK_ORDER_PERIODS = new Integer(5);
    public static final Integer PLUGGABLE_TASK_PAYMENT = new Integer(6);
    public static final Integer PLUGGABLE_TASK_NOTIFICATION = new Integer(7);
    public static final Integer PLUGGABLE_TASK_PAYMENT_INFO = new Integer(8);
    public static final Integer PLUGGABLE_TASK_PENALTY = new Integer(9);
    public static final Integer PLUGGABLE_TASK_PROCESSOR_ALARM = new Integer(10);
    public static final Integer PLUGGABLE_TASK_SUBSCRIPTION_STATUS = new Integer(11);
    public static final Integer PLUGGABLE_TASK_ASYNC_PAYMENT_PARAMS = new Integer(12);
    public static final Integer PLUGGABLE_TASK_ITEM_MANAGER = new Integer(13);
    public static final Integer PLUGGABLE_TASK_ITEM_PRICING = new Integer(14);
    public static final Integer PLUGGABLE_TASK_MEDIATION_READER = new Integer(15);
    public static final Integer PLUGGABLE_TASK_MEDIATION_PROCESS = new Integer(16);
    public static final Integer PLUGGABLE_TASK_INTERNAL_EVENT = new Integer(17);
    public static final Integer PLUGGABLE_TASK_EXTERNAL_PROVISIONING = new Integer(18);
    public static final Integer PLUGGABLE_TASK_VALIDATE_PURCHASE = new Integer(19);
    public static final Integer PLUGGABLE_TASK_BILL_PROCESS_FILTER = new Integer(20);
    public static final Integer PLUGGABLE_TASK_MEDIATION_ERROR_HANDLER = new Integer(21);
    public static final Integer PLUGGABLE_TASK_SCHEDULED = new Integer(22);
    public static final Integer PLUGGABLE_TASK_RULES_GENERATOR = new Integer(23);
    public static final Integer PLUGGABLE_TASK_AGEING = new Integer(24);
    
    // pluggable task types (belongs to a category)
    public static final Integer PLUGGABLE_TASK_T_PAPER_INVOICE = new Integer(12);
    
    // invoice line types
    public static final Integer INVOICE_LINE_TYPE_ITEM_RECURRING = new Integer(1);
    public static final Integer INVOICE_LINE_TYPE_TAX = new Integer(2);
    public static final Integer INVOICE_LINE_TYPE_DUE_INVOICE = new Integer(3);
    public static final Integer INVOICE_LINE_TYPE_PENALTY = new Integer(4);
    public static final Integer INVOICE_LINE_TYPE_SUB_ACCOUNT = new Integer(5);
    public static final Integer INVOICE_LINE_TYPE_ITEM_ONETIME = new Integer(6);

    // permission types - this should be moved to PermissionConstant.java
    public static final Integer PERMISSION_TYPE_MENU= new Integer(1);
    
    // languages - when the project is a big company, we can do this right ! :p
    public static final Integer LANGUAGE_ENGLISH_ID = new Integer(1);
    public static final String LANGUAGE_ENGLISH_STR = "English";
    public static final Integer LANGUAGE_SPANISH_ID = new Integer(2);
    public static final String LANGUAGE_SPANISH_STR = "Spanish";    

    public static final Integer ORDER_PROCESS_ORIGIN_PROCESS = new Integer(1);
    public static final Integer ORDER_PROCESS_ORIGIN_MANUAL = new Integer(2);

    //Notification Preference Types
    public static final Integer PREFERENCE_TYPE_SELF_DELIVER_PAPER_INVOICES = new Integer(13);
    public static final Integer PREFERENCE_TYPE_INCLUDE_CUSTOMER_NOTES = new Integer(14);
    public static final Integer PREFERENCE_TYPE_DAY_BEFORE_ORDER_NOTIF_EXP = new Integer(15);
    public static final Integer PREFERENCE_TYPE_DAY_BEFORE_ORDER_NOTIF_EXP2 = new Integer(16);
    public static final Integer PREFERENCE_TYPE_DAY_BEFORE_ORDER_NOTIF_EXP3 = new Integer(17);
    public static final Integer PREFERENCE_TYPE_USE_INVOICE_REMINDERS = new Integer(21);
    public static final Integer PREFERENCE_TYPE_NO_OF_DAYS_INVOICE_GEN_1_REMINDER = new Integer(22);
    public static final Integer PREFERENCE_TYPE_NO_OF_DAYS_NEXT_REMINDER = new Integer(23);

    // notification message types
    public static final Integer NOTIFICATION_TYPE_INVOICE_EMAIL = 1;
    public static final Integer NOTIFICATION_TYPE_USER_REACTIVATED = 2;
    public static final Integer NOTIFICATION_TYPE_USER_OVERDUE = 3;
    public static final Integer NOTIFICATION_TYPE_USER_OVERDUE_2 = 4;
    public static final Integer NOTIFICATION_TYPE_USER_OVERDUE_3 = 5;
    public static final Integer NOTIFICATION_TYPE_USER_SUSPENDED = 6;
    public static final Integer NOTIFICATION_TYPE_USER_SUSPENDED_2 = 7;
    public static final Integer NOTIFICATION_TYPE_USER_SUSPENDED_3 = 8;
    public static final Integer NOTIFICATION_TYPE_USER_DELETED = 9;
    public static final Integer NOTIFICATION_TYPE_PAYOUT_REMINDER = 10;
    public static final Integer NOTIFICATION_TYPE_PARTNER_PAYOUT = 11;
    public static final Integer NOTIFICATION_TYPE_INVOICE_PAPER = 12;
    public static final Integer NOTIFICATION_TYPE_ORDER_EXPIRE_1 = 13;
    public static final Integer NOTIFICATION_TYPE_ORDER_EXPIRE_2 = 14;
    public static final Integer NOTIFICATION_TYPE_ORDER_EXPIRE_3 = 15;
    public static final Integer NOTIFICATION_TYPE_PAYMENT_SUCCESS = 16;
    public static final Integer NOTIFICATION_TYPE_PAYMENT_FAILED = 17;
    public static final Integer NOTIFICATION_TYPE_INVOICE_REMINDER = 18;
    public static final Integer NOTIFICATION_TYPE_CREDIT_CARD_UPDATE = 19;
    public static final Integer NOTIFICATION_TYPE_LOST_PASSWORD = 20;

    // contact type
    public static final Integer ENTITY_CONTACT_TYPE = new Integer(1);

    //Jbilling Table Ids
    public static final Integer ENTITY_TABLE_ID = new Integer(5);
}
