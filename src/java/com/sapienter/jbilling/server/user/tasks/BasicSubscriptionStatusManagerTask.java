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
package com.sapienter.jbilling.server.user.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.process.ConfigurationBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.audit.EventLogger;

public class BasicSubscriptionStatusManagerTask extends PluggableTask implements
        ISubscriptionStatusManager {
    
    private static final Logger LOG = Logger.getLogger(BasicSubscriptionStatusManagerTask.class);
    
    public static final ParameterDescription PARAMETER_ITEM_TYPE_ID = 
    	new ParameterDescription("item_type_id", true, ParameterDescription.Type.STR);
    
    private PaymentDTOEx payment;
    private Integer entityId;
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_ITEM_TYPE_ID);
    }
    
    public void paymentFailed(Integer entityId, PaymentDTOEx payment) {
        this.payment = payment;
        this.entityId = entityId;
        
        if (!isPaymentApplicable(true)) {
            LOG.debug("This payment can't be processed " + payment);
            return;
        }
        
        LOG.debug("A payment failed " + payment);
        
        UserBL user = getUser(null);
        Integer status = user.getEntity().getSubscriberStatus().getId();

        if (isLastRetry()) {
            if (status.equals(UserDTOEx.SUBSCRIBER_PENDING_EXPIRATION)) {
                user.updateSubscriptionStatus(UserDTOEx.SUBSCRIBER_EXPIRED);
            } else {
                LOG.warn("Last retry, but user not in pending expariation. Status = " + status);
            }
        } else {
            // not paying is not good
            if (status.equals(UserDTOEx.SUBSCRIBER_ACTIVE)) {
                user.updateSubscriptionStatus(UserDTOEx.SUBSCRIBER_PENDING_EXPIRATION);
            } else if (!status.equals(UserDTOEx.SUBSCRIBER_PENDING_EXPIRATION)) {
                LOG.warn("Not clear what to do with a customer in status " + status);
            }
        }
    }
    
    public void paymentSuccessful(Integer entityId, PaymentDTOEx payment) {
        this.payment = payment;
        this.entityId = entityId;
        
        if (!isPaymentApplicable(false)) {
            return;
        }

        UserBL user = getUser(null);

        // currently, any payment get's you to active, regardless of the amount.
        // hence, this is not supporting partial payments ... event a partial 
        // payment will take you to active.
        user.updateSubscriptionStatus(UserDTOEx.SUBSCRIBER_ACTIVE);
    }
    
    public void subscriptionEnds(Integer userId, Date newActiveUntil, 
            Date oldActiveUntil) {
        UserBL user = null;
        // it is known that both are different
        if (oldActiveUntil == null || (newActiveUntil != null && 
                newActiveUntil.after(oldActiveUntil))) {
            user = getUser(userId);
            if (user.getEntity().getSubscriberStatus().getId() ==
                    UserDTOEx.SUBSCRIBER_ACTIVE) {
                user.updateSubscriptionStatus(
                        UserDTOEx.SUBSCRIBER_PENDING_UNSUBSCRIPTION);
            } else {
                LOG.info("Should go to pending unsubscription, but is in " + 
                        user.getEntity().getSubscriberStatus().getDescription(1));
            }
        } else if (newActiveUntil == null) { // it's going back to on-going (subscribed)
            user = getUser(userId);
            if (user.getEntity().getSubscriberStatus().getId() ==
                    UserDTOEx.SUBSCRIBER_PENDING_UNSUBSCRIPTION) {
                user.updateSubscriptionStatus(
                        UserDTOEx.SUBSCRIBER_ACTIVE);
            } else {
                LOG.info("Should go to active, but is in " + 
                        user.getEntity().getSubscriberStatus().getDescription(1));
            }
        }
    }
    
    public void subscriptionEnds(Integer userId, Date date) {
        UserBL user = getUser(userId);
        if (!user.isCurrentlySubscribed(date)) {
            user.updateSubscriptionStatus(UserDTOEx.SUBSCRIBER_UNSUBSCRIBED);
        } 
    }
    
    private boolean isPaymentApplicable(boolean failed) {

        // no payment? then it's not applicable
        if( payment == null )
            return false;
        
        // If a payment is a refund, then it's not applicable
        if( payment.getIsRefund() != 0 )
            return false;
        
        // failed payments that don't have an "attempt" set (is this some kind of counter?)
        // don't apply
        if( failed && payment.getAttempt() == null )
            return false;
  
        String typeStr = (String) parameters.get(PARAMETER_ITEM_TYPE_ID.getName());
        
        if (typeStr == null || typeStr.length() == 0) {
            throw new SessionInternalError("parameter " + PARAMETER_ITEM_TYPE_ID.getName() + 
                    " is required");
        }

        boolean retValue = false;
        
        if (payment.getInvoiceIds() == null || payment.getInvoiceIds().size() == 0) {
            // If you don't have an invoice, it doesn't change subscription status,
            // unless it's a preauthorization.
            // We're assuming that ALL preauthorizations without invoices are being
            // used to start a subscription.
            
            boolean isPreAuth = (payment.getIsPreauth() != null) && (payment.getIsPreauth() != 0);
            return isPreAuth; // don't bother looking for the item category (you can't get to it anyway, this is a "naked" pre-auth payment).
        }
        else 
        {
            // validate that this payment is for a subscription item
            for(Integer invoiceId : payment.getInvoiceIds()) {
                if (new InvoiceDAS().isReleatedToItemType(invoiceId, Integer.valueOf(typeStr))) {
                    retValue = true;
                }
            }
        }
        
        if (retValue == false) {
            new EventLogger().auditBySystem(entityId, payment.getUserId(),
                Constants.TABLE_BASE_USER, payment.getUserId(), 
                EventLogger.MODULE_USER_MAINTENANCE,
                EventLogger.SUBSCRIPTION_STATUS_NO_CHANGE,
                payment.getId(), typeStr, null);
            LOG.debug("Payment did not change subscription status to active." +
                    "Invoice with item category " + typeStr + " not found");
        }
        
        return retValue;
    }
    
    private boolean isLastRetry() {
        ConfigurationBL config = null;
        try {
            config = new ConfigurationBL(entityId);
        } catch (Exception e) {
            throw new SessionInternalError("Processing payment to change status", 
                    BasicSubscriptionStatusManagerTask.class, e);
        }
        
        // it is the number of retries plus one for the initial process
        if (payment.getAttempt().intValue() >= 
                config.getEntity().getRetries().intValue() + 1) { // 
            return true;
        } else {
            return false;
        }
    }
    
    private UserBL getUser(Integer userId) {
        // find the user, and its status
        UserBL user = null; 
        try {
            if (userId == null) {
                user = new UserBL(payment.getUserId());
            } else {
                user = new UserBL(userId);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }        
        return user;
    }
}
