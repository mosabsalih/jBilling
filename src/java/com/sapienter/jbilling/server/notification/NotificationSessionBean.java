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

package com.sapienter.jbilling.server.notification;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.notification.db.NotificationMessageArchDAS;
import com.sapienter.jbilling.server.notification.db.NotificationMessageArchDTO;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.pluggableTask.NotificationTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional( propagation = Propagation.REQUIRED )
public class NotificationSessionBean implements INotificationSessionBean {

    private static final Logger LOG = Logger.getLogger(NotificationSessionBean.class);

    
    /**
     * Sends an email with the invoice to a customer.
     * This is used to manually send an email invoice from the GUI
     * @param userId
     * @param invoiceId
     * @return
    */
    public Boolean emailInvoice(Integer invoiceId) 
            throws SessionInternalError {
        Boolean retValue;
        try {
            InvoiceBL invoice = new InvoiceBL(invoiceId);
            UserBL user = new UserBL(invoice.getEntity().getBaseUser());
            Integer entityId = user.getEntity().getEntity().getId();
            Integer languageId = user.getEntity().getLanguageIdField();
            NotificationBL notif = new NotificationBL();
            MessageDTO message = notif.getInvoiceEmailMessage(entityId, 
                    languageId, invoice.getEntity());
            retValue = notify(user.getEntity(), message);
        
        } catch (NotificationNotFoundException e) {
            retValue = new Boolean(false);
        } 
//        catch (FinderException e) {
//            log.error("Exception sending email invoice", e);
//            throw new SessionInternalError(e);
//        } 
        
        return retValue;
    }
    
    /**
     * Sends an email with the invoice to a customer.
     * This is used to manually send an email invoice from the GUI
     * @param userId
     * @param invoiceId
     * @return
    */
    public Boolean emailPayment(Integer paymentId) 
            throws SessionInternalError {
        Boolean retValue;
        try {
            PaymentBL payment = new PaymentBL(paymentId);
            UserBL user = new UserBL(payment.getEntity().getBaseUser());
            Integer entityId = user.getEntity().getEntity().getId();
            NotificationBL notif = new NotificationBL();
            MessageDTO message = notif.getPaymentMessage(entityId, 
                    payment.getDTOEx(user.getEntity().getLanguageIdField()),
                    new Integer(payment.getEntity().getPaymentResult().getId()).equals(
                            Constants.RESULT_ENTERED) ||
                    new Integer(payment.getEntity().getPaymentResult().getId()).equals(
                            Constants.RESULT_OK));
            retValue = notify(user.getEntity(), message);
        } catch (NotificationNotFoundException e) {
            retValue = new Boolean(false);
        } 
        
        return retValue;
    }

    public void notify(Integer userId, MessageDTO message) 
            throws SessionInternalError {

        try {
            UserBL user = new UserBL(userId);
            notify(user.getEntity(), message);            
        } catch (Exception e) {
            throw new SessionInternalError("Problems getting user entity" +
                    " for id " + userId + "." + e.getMessage());
        } 
    }
    
   /**
    * Sends a notification to a user. Returns true if no exceptions were
    * thrown, otherwise false. This return value could be considered
    * as if this message was sent or not for most notifications (emails).
    */
    public Boolean notify(UserDTO user, MessageDTO message) 
            throws SessionInternalError {
        
        Boolean retValue = new Boolean(true);
        try {
            // verify that the message is good
            if (message.validate() == false) {
                throw new SessionInternalError("Invalid message");
            }
            // parse this message contents with the parameters
            MessageSection sections[] = message.getContent();
            for (int f=0; f < sections.length; f++) {
                MessageSection section = sections[f];
                section.setContent(NotificationBL.parseParameters(
                        section.getContent(), message.getParameters()));
            }
            // now do the delivery with the pluggable tasks
            PluggableTaskManager taskManager =
                new PluggableTaskManager(
                    user.getEntity().getId(),
                    Constants.PLUGGABLE_TASK_NOTIFICATION);
            NotificationTask task =
                (NotificationTask) taskManager.getNextClass();

            NotificationMessageArchDAS messageHome =
                    new NotificationMessageArchDAS();
            
            while (task != null) {
                NotificationMessageArchDTO messageRecord =
                        messageHome.create(message.getTypeId(), sections);
                messageRecord.setBaseUser(user);
                try {
                    task.deliver(user, message);
                } catch (TaskException e) {
                    messageRecord.setResultMessage(Util.truncateString(
                            e.getMessage(), 200));
                    LOG.error(e);
                    retValue = new Boolean(false);
                }
                task = (NotificationTask) taskManager.getNextClass();
            }
        } catch (Exception e) {
            LOG.error("Exception in notify", e);
            throw new SessionInternalError(e);
        }   
        
        return retValue;
    }

    public MessageDTO getDTO(Integer typeId, Integer languageId,
            Integer entityId) throws SessionInternalError {
        try {
            NotificationBL notif = new NotificationBL();
            MessageDTO retValue = null;
            int plugInSections = notif.getSections(entityId);
            notif.set(typeId, languageId, entityId);
            if (notif.getEntity() != null) {
                retValue = notif.getDTO();
            } else {
                retValue = new MessageDTO();
                retValue.setTypeId(typeId);
                retValue.setLanguageId(languageId);
                MessageSection sections[] =
                        new MessageSection[plugInSections];
                for (int f = 0; f < sections.length; f++) {
                    sections[f] = new MessageSection(new Integer(f + 1), "");
                }
                retValue.setContent(sections);
            }
            
            if (retValue.getContent().length < plugInSections) {
                // pad any missing sections, due to changes to a new plug-in with more sections
                for (int f = retValue.getContent().length ; f < plugInSections; f++) {
                    retValue.addSection(new MessageSection(new Integer(f + 1), ""));
                }
            } else if (retValue.getContent().length > plugInSections) {
                // remove excess sections 
                retValue.setContentSize(plugInSections);
            }


            return retValue;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Integer createUpdate(MessageDTO dto, 
            Integer entityId) throws SessionInternalError {
        try {
            NotificationBL notif = new NotificationBL();
            
            return notif.createUpdate(entityId, dto);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public String getEmails(Integer entityId, String separator) 
            throws SessionInternalError {
        try {
            NotificationBL notif = new NotificationBL();
            
            return notif.getEmails(separator, entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }        
}
