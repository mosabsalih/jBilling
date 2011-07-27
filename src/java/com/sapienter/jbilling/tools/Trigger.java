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
 * Created on Jul 14, 2004
 *
 */
package com.sapienter.jbilling.tools;

import java.util.Calendar;
import java.util.Date;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.IInvoiceSessionBean;
import com.sapienter.jbilling.server.order.IOrderSessionBean;
import com.sapienter.jbilling.server.process.IBillingProcessSessionBean;
import com.sapienter.jbilling.server.user.IUserSessionBean;

/**
 * @author Emil
 *
 */
public class Trigger {



    public static void main(String[] args) {
        IBillingProcessSessionBean remoteBillingProcess = null;
        
        
        try {
        	// TODO, change this to use the standard API
            // get a session for the remote interfaces
 /*           remoteBillingProcess = (IBillingProcessSessionBean)
                    RemoteContext.getBean(
                    RemoteContext.Name.BILLING_PROCESS_REMOTE_SESSION);
            IUserSessionBean remoteUser = (IUserSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.USER_REMOTE_SESSION);
            IOrderSessionBean remoteOrder = (IOrderSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.ORDER_REMOTE_SESSION);
            IInvoiceSessionBean remoteInvoice = (IInvoiceSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.INVOICE_REMOTE_SESSION);
            IListSessionBean remoteList = (IListSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.LIST_REMOTE_SESSION);
 */
        	remoteBillingProcess = null;
        	IUserSessionBean remoteUser = null;
        	IOrderSessionBean remoteOrder = null;
        	IInvoiceSessionBean remoteInvoice = null;

            // determine the date for this run
            Date today = Calendar.getInstance().getTime();
            Integer step = null; //means all
            if (args.length > 0) {
                today = Util.parseDate(args[0]);
                if (args.length >= 2) {
                    step = Integer.valueOf(args[1]);
                }
            }
            today = Util.truncateDate(today);

            Integer entityId = Integer.valueOf(args[3]);

            // run the billing process
            if (step == null || step.intValue() == 1) {
                System.out.println("Running trigger for " + today);
                System.out.println("Starting billing process at " + 
                        Calendar.getInstance().getTime());
                remoteBillingProcess.trigger(today);
                System.out.println("Ended billing process at " + 
                        Calendar.getInstance().getTime());
            }

            // now the ageing process
            if (step == null || step.intValue() == 2) {
                if (entityId == null) {
                    System.out.println("Cannot start ageing process without an entity ID.");
                    return;
                }

                System.out.println("Starting ageing process at " +
                        Calendar.getInstance().getTime());
                remoteBillingProcess.reviewUsersStatus(entityId, today);
                System.out.println("Ended ageing process at " +
                        Calendar.getInstance().getTime());
            }

            // now the partner payout process
            if (step == null || step.intValue() == 3) {
                System.out.println("Starting partner process at " + 
                        Calendar.getInstance().getTime());
                remoteUser.processPayouts(today);
                System.out.println("Ended partner process at " + 
                        Calendar.getInstance().getTime());
            }
            
            // finally the orders about to expire notification
            if (step == null || step.intValue() == 4) {
                System.out.println("Starting order notification at " + 
                        Calendar.getInstance().getTime());
                remoteOrder.reviewNotifications(today);
                System.out.println("Ended order notification at " + 
                        Calendar.getInstance().getTime());
            }
            
            // the invoice reminders
            if (step == null || step.intValue() == 5) {
                System.out.println("Starting invoice reminders at " + 
                        Calendar.getInstance().getTime());
                remoteInvoice.sendReminders(today);
                System.out.println("Ended invoice reminders at " + 
                        Calendar.getInstance().getTime());
            }

            // the invoice penalties
            if (step == null || step.intValue() == 6) {
                // Penalty processing removed, now handled as an internal event
                // based of the ageing NewUserStatusEvent
            }
            
            // send credit card expiration emails
            if (step == null || step.intValue() == 8) {
                System.out.println("Starting credit card expiration at " + 
                        Calendar.getInstance().getTime());
                remoteUser.notifyCreditCardExpiration(today);
                System.out.println("Ended credit card expiration at " + 
                        Calendar.getInstance().getTime());
            }

        } catch (ClassCastException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SessionInternalError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
}
