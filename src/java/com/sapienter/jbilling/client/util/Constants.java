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


package com.sapienter.jbilling.client.util;

import com.sapienter.jbilling.common.CommonConstants;
import com.sapienter.jbilling.common.PermissionConstants;

/**
 * Constants for the client
 *
 */

public final class Constants implements PermissionConstants, CommonConstants {

    /*
     * Application wide attributes
     */
    // this is an array of beans that have both the symbol and code of the currency
    public static final String APP_CURRENCY_SYMBOLS = "app_currency_symbols";
    
    /*
     * Session keys
     */
    // Login information (static during the whole session
    
    // Integer
    public static final String SESSION_LOGGED_USER_ID = "sys_user";
    // Integer
    public static final String SESSION_ENTITY_ID_KEY = "sys_entity";
    // Integer
    public static final String SESSION_LANGUAGE = "sys_language";
    // Integer, the currency of the logged user
    public static final String SESSION_CURRENCY = "sys_currency";
    // The whole UserDTOEx of the logged .. needed for the permissions
    public static final String SESSION_USER_DTO = "sys_user_dto";
    // The location of the css file
    public static final String SESSION_CSS_LOCATION = "sys_css_location";
    // The location of the logo graphic
    public static final String SESSION_LOGO_LOCATION = "sys_logo_location";
    
    
    // order fields
    
    // OrderDTO for new orders
    public static final String SESSION_ORDER_SUMMARY = "newOrderSummary";
    // OrderDTO
    public static final String SESSION_ORDER_DTO = "orderDto";
    // Mediation events that affected an order
    public static final String SESSION_ORDER_CDR = "orderCDR";
    
    
    // customer/user fields
    
    // contact dto info 
    public static final String SESSION_CUSTOMER_CONTACT_DTO = "contact_dto";
    // Integer - customer id. This is setup when a selection of a user from the generic list
    public static final String SESSION_USER_ID = "user_id";
    // Integer - user id of the user that the contact information will be displayed
    public static final String SESSION_CONTACT_USER_ID = "contact_user_id";
    // UserDTOEx. A user dto, but can't be USER_DTO because that's the logged
    // user. This is a selected user for display
    public static final String SESSION_CUSTOMER_DTO = "customer_dto";
    
    
    // partner fields
    
    // the id of a partner
    public static final String SESSION_PARTNER_ID = "partner_id";
    // a Partner
    public static final String SESSION_PARTNER_DTO = "partner_dto";
    // a PartnerPayoutDTOEx
    public static final String SESSION_PAYOUT_DTO = "payout_dto";

    // invoice fields
    
    // one invoice dto
    public static final String SESSION_INVOICE_DTO = "invoiceDto";
    
    
    // payment fields
    // dto ex
    public static final String SESSION_PAYMENT_DTO = "paymentDto";
    public static final String SESSION_PAYMENT_DTO_REFUND = "paymentDtoRefund";
    
    //item
    public static final String SESSION_ITEM_DTO = "itemDto";
    public static final String SESSION_ITEM_ID = "itemId"; 
    public static final String SESSION_ITEM_PRICE_ID = "itemPriceId";
    public static final String SESSION_PROMOTION_DTO = "promotionDto";
    

    //lists     
    // to which page should I forward if there are errors
    public static final String SESSION_FORWARD_FROM = "forward_from";
    // to which page should I forward if everythings is ok
    public static final String SESSION_FORWARD_TO = "forward_to";
    // CachedRowSet - The prefix of the generic list
    public static final String SESSION_LIST_KEY = "list";
    // Integer - the id of the row selected by the user
    public static final String SESSION_LIST_ID_SELECTED = "listIdSelected";
    // PagedListDTO - the info for paged lists
    public static final String SESSION_PAGED_LIST = "pagedListDTO";
    public static final String SESSION_LIST_LAST_ID = "pagedListLastId";
    public static final String SESSION_PAGED_IS_PREV = "pagesIsPrev";
    public static final String SESSION_PAGED_IS_NEXT = "pagesIsNext";
    public static final String SESSION_LIST_ROWS = "totalRows";
    
    
    // reports
    public static final String SESSION_REPORT_DTO = "report_dto";
    public static final String SESSION_REPORT_FORM = "reportForm";
    public static final String SESSION_REPORT_LIST = "reportList";
    public static final String SESSION_REPORT_LIST_USER = "reportListUser";
    public static final String SESSION_REPORT_LIST_TYPE = "reportListType";
    public static final String SESSION_REPORT_RESULT = "reportResult";
    public static final String SESSION_REPORT_TITLE = "reportTitle";
    public static final String SESSION_REPORT_LINK = "reportLink";
    public static final String SESSION_MESSAGES = "sessionMessages";

    // process
    // BillingProcessDTOEx
    public static final String SESSION_PROCESS_DTO = "processDto";
    public static final String SESSION_PROCESS_CONFIGURATION_DTO = "processConfigurationDto";
    
    // pluggable task
    public static final String SESSION_PLUGGABLE_TASK_DTO = "pluggableTaskDto";
    /*
     * Request attributes
     */
    // customer id from selection
    public static final String REQUEST_CUSTOMER_ID = "customer_id";
    // invoice id from selection
    public static final String REQUEST_INVOICE_ID = "invoice_id";
    // order period (has to be the set/get of NewOrderDTO.period
    public static final String REQUEST_ORDER_PERIOD = "period";
    // report id, or which report to run
    public static final String REQUEST_REPORT_ID = "report_id";
    // user report id, for running a user saved report
    public static final String REQUEST_USER_REPORT_ID = "user_report_id";
    // a row selected in the generic list
    public static final String REQUEST_SELECTION_ID = "selection_id";
    // event log list
    public static final String REQUEST_EVENT_LOG = "event_log";
    /*
     * for lists
     */
    // the type of the generic list
    public static final String REQUEST_LIST_TYPE = "list_type";
    // String - the list method: if this will use jdbc or ejb for
    // the query
    public static final String REQUEST_LIST_METHOD= "listMethod";
    // this is defined in the top, to be read by the generic list
    public static final String REQUEST_LIST_COLUMNS = "list_columns";
    // to which page should I forward if there are errors
    public static final String REQUEST_FORWARD_FROM = "forward_from";
    // to which page should I forward if everythings is ok
    public static final String REQUEST_FORWARD_TO = "forward_to";
    // indicate if the list is paged or not
    public static final String REQUEST_LIST_IS_PAGED = "listPaged";
    
    /*
     * Page attributes
     */
    // the collection of look up data to display in the UI
    public static final String PAGE_ORDER_PERIODS = "order_periods";
    public static final String PAGE_PAYMENT_SENT_ERROR = "payment_error";
    public static final String PAGE_COUNTRIES = "countries";
    public static final String PAGE_USER_DTO = "page_user_dto";
    public static final String PAGE_USER_TYPES = "user_types";
    public static final String PAGE_LANGUAGES = "languages";
    public static final String PAGE_USER_STATUS = "user_status";
    public static final String PAGE_ITEM_TYPES= "item_types";
    public static final String PAGE_BILLING_TYPE= "billing_types";
    public static final String PAGE_GENERAL_PERIODS= "general_periods";
    public static final String PAGE_CURRENCIES= "currencies";
    public static final String PAGE_CONTACT_TYPES = "contact_types";
    public static final String PAGE_DELIVERY_METHOD = "delivery_methods";
    public static final String PAGE_ORDER_LINE_TYPES = "order_line_types";
    public static final String PAGE_TASK_CLASSES = "task_classes";
    public static final String PAGE_SUBSCRIBER_STATUS = "subscriberStatus";
    public static final String PAGE_PROVISIONING_STATUS = "provisioningStatus";
    public static final String PAGE_BALANCE_TYPE = "balanceType";

    /*
     * Forwards as parameters
     */
    public static final String FORWARD_NEW_ORDER = "orderNew";
    public static final String FORWARD_NEW_ORDER_ITEMS = "orderNewItems";
    public static final String FORWARD_ORDER_VIEW = "orderView";
    public static final String FORWARD_ORDER_EDIT = "orderEdit";
    public static final String FORWARD_LIST_OR_ORDER_EDIT = "listOrOrderEdit";
    public static final String FORWARD_USER_MAINTAIN = "userMaintain";
    public static final String FORWARD_USER_EDIT = "userEdit";
    public static final String FORWARD_USER_VIEW = "userView";
    public static final String FORWARD_ITEM_TYPE_LIST = "itemTypeList";
    public static final String FORWARD_ITEM_TYPE_EDIT = "itemTypeEdit";
    public static final String FORWARD_ITEM_LIST = "itemList";
    public static final String FORWARD_ITEM_EDIT = "itemEdit";
    public static final String FORWARD_ITEM_PRICE_CREATE= "itemPriceCreate";
    public static final String FORWARD_ITEM_PRICE_LIST = "itemPriceList";
    public static final String FORWARD_ITEM_PRICE_EDIT = "itemPriceEdit";
    public static final String FORWARD_PROMOTION_EDIT = "promotionEdit";
    public static final String FORWARD_PROMOTION_LIST= "promotionList";
    public static final String FORWARD_PAYMENT_LIST= "paymentList";
    public static final String FORWARD_PAYMENT_CREATE= "paymentCreate";
    public static final String FORWARD_PAYMENT_VIEW= "paymentView";
    public static final String FORWARD_INVOICE_VIEW= "invoiceView";
    public static final String FORWARD_PROCESS_VIEW= "processView";
    public static final String FORWARD_NOTIFICATION_VIEW= "notificationView";
    public static final String FORWARD_PARTNER_VIEW= "partnerView";
    public static final String FORWARD_PAYOUT_VIEW="payoutView";
    public static final String FORWARD_APPLY_ORDER_CONFIRM="applyConfirm";

}
