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
package com.sapienter.jbilling.server.user.event;

import com.sapienter.jbilling.server.order.event.NewActiveUntilEvent;
import com.sapienter.jbilling.server.payment.event.PaymentFailedEvent;
import com.sapienter.jbilling.server.payment.event.PaymentProcessorUnavailableEvent;
import com.sapienter.jbilling.server.payment.event.PaymentSuccessfulEvent;
import com.sapienter.jbilling.server.process.event.NoNewInvoiceEvent;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.system.event.EventProcessor;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.tasks.ISubscriptionStatusManager;
import com.sapienter.jbilling.server.util.Constants;
import org.apache.log4j.Logger;

public class SubscriptionStatusEventProcessor extends EventProcessor<ISubscriptionStatusManager> {
    
    private static final Logger LOG = Logger.getLogger(SubscriptionStatusEventProcessor.class);

    public void process(Event event) {
        // get an instance of the pluggable task
        ISubscriptionStatusManager task = getPluggableTask(event.getEntityId(),
                Constants.PLUGGABLE_TASK_SUBSCRIPTION_STATUS);
        
        if (task == null) {
            LOG.debug("There isn't a task configured to handle subscription status");
            return;
        }
        
        // depending on the event, call the right method of the task
        if (event instanceof PaymentFailedEvent) {
            PaymentFailedEvent pfEvent = (PaymentFailedEvent) event;
            task.paymentFailed(pfEvent.getEntityId(), pfEvent.getPayment());
        } else if (event instanceof PaymentProcessorUnavailableEvent) {
            PaymentProcessorUnavailableEvent puEvent = (PaymentProcessorUnavailableEvent) event;
            task.paymentFailed(puEvent.getEntityId(), puEvent.getPayment());
        } else if (event instanceof PaymentSuccessfulEvent) {
            PaymentSuccessfulEvent psEvent = (PaymentSuccessfulEvent) event;
            task.paymentSuccessful(psEvent.getEntityId(), psEvent.getPayment());
        } else if (event instanceof NewActiveUntilEvent) {
            NewActiveUntilEvent auEvent = (NewActiveUntilEvent) event;
            // process the event only if the order is not a one timer
            // and is active
            if (!auEvent.getOrderType().equals(Constants.ORDER_PERIOD_ONCE) &&
                    auEvent.getStatusId().equals(Constants.ORDER_STATUS_ACTIVE)) {
                task.subscriptionEnds(auEvent.getUserId(), 
                        auEvent.getNewActiveUntil(), auEvent.getOldActiveUntil());
            }
        } else if (event instanceof NoNewInvoiceEvent) {
            NoNewInvoiceEvent sEvent = (NoNewInvoiceEvent) event;
            // this event needs handling only if the user status is pending unsubscription
            if (sEvent.getSubscriberStatus().equals(
                    UserDTOEx.SUBSCRIBER_PENDING_UNSUBSCRIPTION)) {
                task.subscriptionEnds(sEvent.getUserId(), sEvent.getBillingProcess());
            }
        }
    }
}
