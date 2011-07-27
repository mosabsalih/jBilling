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

package com.sapienter.jbilling.server.util.audit;

import java.util.Date;


import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.audit.db.EventLogDAS;
import com.sapienter.jbilling.server.util.audit.db.EventLogDTO;
import com.sapienter.jbilling.server.util.audit.db.EventLogMessageDAS;
import com.sapienter.jbilling.server.util.audit.db.EventLogModuleDAS;
import com.sapienter.jbilling.server.util.db.JbillingTableDAS;

public class EventLogger {

    // these are the messages constants, in synch with the db (event_log_message)
    // billing process
    public static final Integer BILLING_PROCESS_UNBILLED_PERIOD = new Integer(1);
    public static final Integer BILLING_PROCESS_NOT_ACTIVE_YET = new Integer(2);
    public static final Integer BILLING_PROCESS_ONE_PERIOD_NEEDED = new Integer(3);
    public static final Integer BILLING_PROCESS_RECENTLY_BILLED = new Integer(4);
    public static final Integer BILLING_PROCESS_WRONG_FLAG_ON = new Integer(5);
    public static final Integer BILLING_PROCESS_EXPIRED = new Integer(6);
    public static final Integer BILLING_REVIEW_NOT_APPROVED = new Integer(10);
    public static final Integer BILLING_REVIEW_NOT_GENERATED = new Integer(11);
    // user maintenance
    public static final Integer PASSWORD_CHANGE = new Integer(8);
    public static final Integer STATUS_CHANGE = new Integer(12);
    public static final Integer NO_FURTHER_STEP = new Integer(14);
    public static final Integer CANT_PAY_PARTNER = new Integer(15);
    public static final Integer SUBSCRIPTION_STATUS_CHANGE = new Integer(20);
    public static final Integer SUBSCRIPTION_STATUS_NO_CHANGE = new Integer(32);
    public static final Integer ACCOUNT_LOCKED = new Integer(21);
    public static final Integer DYNAMIC_BALANCE_CHANGE = new Integer(33);
    public static final Integer INVOICE_IF_CHILD_CHANGE = new Integer(34);
    // order maintenance
    public static final Integer ORDER_STATUS_CHANGE = new Integer(13);
    public static final Integer ORDER_LINE_UPDATED = new Integer(17);
    public static final Integer ORDER_NEXT_BILL_DATE_UPDATED = new Integer(18);
    public static final Integer ORDER_MAIN_SUBSCRIPTION_UPDATED = new Integer(22);
    public static final Integer ORDER_CANCEL_AND_CREDIT = new Integer(26);
    // payment
    public static final Integer PAYMENT_INSTRUMENT_NOT_FOUND = new Integer(24);
    // invoice related message
    public static final Integer INVOICE_ORDER_APPLIED = new Integer(16);
    // mediation
    public static final Integer CURRENT_ORDER_FINISHED = new Integer(23);
    // blacklist
    public static final Integer BLACKLIST_USER_ID_ADDED = new Integer(27);
    public static final Integer BLACKLIST_USER_ID_REMOVED = new Integer(28);
    //provisioning
    public static final Integer PROVISIONING_UUID = new Integer(29);
    public static final Integer PROVISIONING_COMMAND=new Integer(30);
    public static final Integer PROVISIONING_STATUS_CHANGE=new Integer(31);

    // others
    public static final Integer ROW_CREATED = new Integer(25);
    public static final Integer ROW_DELETED = new Integer(7);
    public static final Integer ROW_UPDATED= new Integer(9); // field not specified
    public static final Integer USER_TRANSITIONS_LIST = new Integer(19);


    // event log modules in synch with db (event_log_module)
    public static final Integer MODULE_BILLING_PROCESS = new Integer(1);
    public static final Integer MODULE_USER_MAINTENANCE = new Integer(2);
    public static final Integer MODULE_ITEM_MAINTENANCE = new Integer(3);
    public static final Integer MODULE_ITEM_TYPE_MAINTENANCE = new Integer(4);
    public static final Integer MODULE_ITEM_USER_PRICE_MAINTENANCE = new Integer(5);
    public static final Integer MODULE_PROMOTION_MAINTENANCE = new Integer(6);
    public static final Integer MODULE_ORDER_MAINTENANCE = new Integer(7);
    public static final Integer MODULE_CREDIT_CARD_MAINTENANCE = new Integer(8);
    public static final Integer MODULE_INVOICE_MAINTENANCE = new Integer(9);
    public static final Integer MODULE_PAYMENT_MAINTENANCE = new Integer(10);
    public static final Integer MODULE_TASK_MAINTENANCE = new Integer(11);
    public static final Integer MODULE_WEBSERVICES = new Integer(12);
    public static final Integer MODULE_MEDIATION = new Integer(13);
    public static final Integer MODULE_BLACKLIST = new Integer(14);
    public static final Integer MODULE_PROVISIONING=new Integer(15);


    // levels of logging
    public static final Integer LEVEL_DEBUG = new Integer(1);
    public static final Integer LEVEL_INFO = new Integer(2);
    public static final Integer LEVEL_WARNING = new Integer(3);
    public static final Integer LEVEL_ERROR = new Integer(4);
    public static final Integer LEVEL_FATAL = new Integer(5);

    private EventLogDAS eventLogDAS = null;
    private EventLogMessageDAS eventLogMessageDAS = null;
    private EventLogModuleDAS eventLogModuleDAS = null;
    private JbillingTableDAS jbDAS = null;

    //private static final Logger LOG = Logger.getLogger(EventLogger.class);

    public EventLogger() {
        eventLogDAS = new EventLogDAS();
        eventLogMessageDAS = new EventLogMessageDAS();
        eventLogModuleDAS = new EventLogModuleDAS();
        jbDAS = (JbillingTableDAS) Context.getBean(Context.Name.JBILLING_TABLE_DAS);
    }

    public static EventLogger getInstance() {
        return new EventLogger();
    }

    public void log(Integer level, Integer entity, Integer userAffectedId,
            Integer rowId, Integer module, Integer message, String table)  {
        CompanyDAS company = new CompanyDAS();
        UserDAS user= new UserDAS();
        EventLogDTO dto = new EventLogDTO(null, jbDAS.findByName(table), null,
                user.find(userAffectedId), eventLogMessageDAS.find(message),
                eventLogModuleDAS.find(module), company.find(entity), rowId,
                level, null, null, null);
        eventLogDAS.save(dto);
    }

    public void debug(Integer entity, Integer userAffectedId, Integer rowId,
            Integer module, Integer message, String table)   {
        log(LEVEL_DEBUG, entity, userAffectedId, rowId, module, message, table);
    }

    public void info(Integer entity, Integer userAffectedId, Integer rowId,
            Integer module, Integer message, String table) {
        log(LEVEL_INFO, entity, userAffectedId, rowId, module, message, table);
    }

    public void warning(Integer entity, Integer userAffectedId, Integer rowId,
            Integer module, Integer message, String table)   {
        log(LEVEL_WARNING, entity, userAffectedId, rowId, module, message,
                table);
    }

    public void error(Integer entity, Integer userAffectedId, Integer rowId,
            Integer module, Integer message, String table)   {
        log(LEVEL_ERROR, entity, userAffectedId, rowId, module, message, table);
    }

    public void fatal(Integer entity, Integer userAffectedId, Integer rowId,
            Integer module, Integer message, String table)   {
        log(LEVEL_FATAL, entity, userAffectedId, rowId, module, message, table);
    }

    /*
     * This is intended for loggin a change in the database, where we want to
     * keep track of what changed
     */
    public void audit(Integer userExecutingId, Integer userAffectedId,
            String table, Integer rowId, Integer module, Integer message,
            Integer oldInt, String oldStr, Date oldDate) {

        UserDAS user= new UserDAS();

        EventLogDTO dto = new EventLogDTO(null, jbDAS.findByName(table),
                user.find(userExecutingId), (userAffectedId == null) ? null : user.find(userAffectedId),
                eventLogMessageDAS.find(message), eventLogModuleDAS.find(module),
                user.find(userExecutingId).getCompany(), rowId, LEVEL_INFO, oldInt, oldStr, oldDate);
        eventLogDAS.save(dto);

    }


    /*
     * Same as previous but the change its not being done by any given user
     * (no executor) but by a batch process.
     */
    public void auditBySystem(Integer entityId, Integer userAffectedId,
            String table, Integer rowId, Integer module, Integer message,
            Integer oldInt, String oldStr, Date oldDate) {
        CompanyDAS company = new CompanyDAS();
        UserDAS user= new UserDAS();
        EventLogDTO dto = new EventLogDTO(null, jbDAS.findByName(table), null,
                user.find(userAffectedId), eventLogMessageDAS.find(message),
                eventLogModuleDAS.find(module), company.find(entityId), rowId,
                LEVEL_INFO, oldInt, oldStr, oldDate);
        eventLogDAS.save(dto);

    }

    /**
     * Queries the event_log table to determine the position where the last query
     * of the user transitions ended. This is called if the user passes
     * <code>null</code> as the <code>from</code> parameter to the getUserTransitions
     * webservice call.
     * @return the id of the last queried transitions list.
     */
    public Integer getLastTransitionEvent(Integer entityId)  {
        return eventLogDAS.getLastTransitionEvent(entityId);
    }
}
