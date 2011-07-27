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

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDTO;
import com.sapienter.jbilling.server.pluggableTask.PaperInvoiceNotificationTask;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.process.BillingProcessBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.PreferenceBL;
import java.util.Set;

/**
 *
 * This is the session facade for the invoices in general. It is a statless
 * bean that provides services not directly linked to a particular operation
 *
 * @author emilc
 **/
@Transactional( propagation = Propagation.REQUIRED )
public class InvoiceSessionBean implements IInvoiceSessionBean {

    private static final Logger LOG = Logger.getLogger(
            InvoiceSessionBean.class);

    public InvoiceDTO getInvoice(Integer invoiceId) throws SessionInternalError {
        InvoiceDTO dto =  new InvoiceDAS().findNow(invoiceId);
        if (dto != null) dto.getBalance(); // touch
        return dto;
    }

    public void create(Integer entityId, Integer userId,
            NewInvoiceDTO newInvoice)
            throws SessionInternalError {
        try {
            InvoiceBL invoice = new InvoiceBL();
            UserBL user = new UserBL();
            if (user.getEntityId(userId).equals(entityId)) {
                invoice.create(userId, newInvoice, null);
                invoice.createLines(newInvoice);
            } else {
                throw new SessionInternalError("User " + userId + " doesn't " +
                        "belong to entity " + entityId);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public String getFileName(Integer invoiceId) throws SessionInternalError {
        try {
            InvoiceBL invoice = new InvoiceBL(invoiceId);
            UserBL user = new UserBL(invoice.getEntity().getBaseUser());
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "entityNotifications", user.getLocale());

            String ret = bundle.getString("invoice.file.name") + '-' +
                    invoice.getEntity().getPublicNumber().replaceAll(
                    "[\\\\~!@#\\$%\\^&\\*\\(\\)\\+`=\\]\\[';/\\.,<>\\?:\"{}\\|]", "_");
            LOG.debug("name = " + ret);
            return ret;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * The transaction requirements of this are not big. The 'atom' is 
     * just a single invoice. If the next one fails, it's ok that the
     * previous ones got updated. In fact, they should, since the email
     * has been sent.
     */
    public void sendReminders(Date today) throws SessionInternalError {
        try {
            InvoiceBL invoice = new InvoiceBL();
            invoice.sendReminders(today);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public InvoiceDTO getInvoiceEx(Integer invoiceId, Integer languageId)  {
        if (invoiceId == null) {
            return null;
        }
        InvoiceBL invoice = new InvoiceBL(invoiceId);
        InvoiceDTO ret = invoice.getDTOEx(languageId, true);
        for (PaymentInvoiceMapDTO map : ret.getPaymentMap()) {
            map.getPayment().getCreateDatetime(); // thouch
        }
        for (OrderProcessDTO process : ret.getOrderProcesses()) {
            process.getPurchaseOrder().getCreateDate(); // thouch
        }
        return ret;
    }

    public byte[] getPDFInvoice(Integer invoiceId)
            throws SessionInternalError {
        try {
            if (invoiceId == null) {
                return null;
            }
            NotificationBL notification = new NotificationBL();
            InvoiceBL invoiceBl = new InvoiceBL(invoiceId);
            Integer entityId = invoiceBl.getEntity().getBaseUser().
                    getEntity().getId();
            // the language doesn't matter when getting a paper invoice
            MessageDTO message = notification.getInvoicePaperMessage(
                    entityId, null, invoiceBl.getEntity().getBaseUser().
                    getLanguageIdField(), invoiceBl.getEntity());
            PaperInvoiceNotificationTask task =
                    new PaperInvoiceNotificationTask();
            PluggableTaskBL taskBL = new PluggableTaskBL();
            taskBL.set(entityId, Constants.PLUGGABLE_TASK_T_PAPER_INVOICE);
            task.initializeParamters(taskBL.getDTO());
            return task.getPDF(invoiceBl.getEntity().getBaseUser(), message);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void delete(Integer invoiceId, Integer executorId)
            throws SessionInternalError {
        InvoiceBL invoice = new InvoiceBL(invoiceId);
        invoice.delete(executorId);
    }

    /**
     * The real path is known only to the web server
     * It should have the token _FILE_NAME_ to be replaced by the generated file
     */
    public String generatePDFFile(java.util.Map map, String realPath) throws SessionInternalError {
        Integer operationType = (Integer) map.get("operationType");

        try {
            InvoiceBL invoiceBL = new InvoiceBL();
            javax.sql.rowset.CachedRowSet cachedRowSet = null;
            Integer entityId = (Integer) map.get("entityId");

            if (operationType.equals(com.sapienter.jbilling.common.Constants.OPERATION_TYPE_CUSTOMER)) {
                Integer customer = (Integer) map.get("customer");

                //security check is done here for speed
                UserBL customerUserBL = null;
                customerUserBL = new UserBL(customer);
                if ((customerUserBL != null) && customerUserBL.getEntity().getEntity().getId() == entityId) {
                    cachedRowSet = invoiceBL.getInvoicesByUserId(customer);
                }
            } else if (operationType.equals(com.sapienter.jbilling.common.Constants.OPERATION_TYPE_RANGE)) {
                //security check is done in SQL
                cachedRowSet = invoiceBL.getInvoicesByIdRange(
                        (Integer) map.get("from"),
                        (Integer) map.get("to"),
                        entityId);
            } else if (operationType.equals(com.sapienter.jbilling.common.Constants.OPERATION_TYPE_PROCESS)) {
                Integer process = (Integer) map.get("process");

                //security check is done here for speed
                BillingProcessBL billingProcessBL = null;
                billingProcessBL = new BillingProcessBL(process);
                if ((billingProcessBL != null) && new Integer(billingProcessBL.getEntity().getEntity().getId()).equals(entityId)) {
                    cachedRowSet = invoiceBL.getInvoicesToPrintByProcessId(process);
                }
            } else if (operationType.equals(com.sapienter.jbilling.common.Constants.OPERATION_TYPE_DATE)) {
                Date from = (Date) map.get("date_from");
                Date to = (Date) map.get("date_to");

                cachedRowSet = invoiceBL.getInvoicesByCreateDate(entityId, from, to);
            } else if (operationType.equals(com.sapienter.jbilling.common.Constants.OPERATION_TYPE_NUMBER)) {
                String from = (String) map.get("number_from");
                String to = (String) map.get("number_to");
                Integer from_id = invoiceBL.convertNumberToID(entityId, from);
                Integer to_id = invoiceBL.convertNumberToID(entityId, to);

                if (from_id != null && to_id != null &&
                        from_id.compareTo(to_id) <= 0) {
                    cachedRowSet = invoiceBL.getInvoicesByIdRange(
                            from_id, to_id, entityId);
                }
            }

            if (cachedRowSet == null) {
                return null;
            } else {
                PaperInvoiceBatchBL paperInvoiceBatchBL = new PaperInvoiceBatchBL();
                return paperInvoiceBatchBL.generateFile(cachedRowSet, entityId, realPath);
            }

        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Set<InvoiceDTO> getAllInvoices(Integer userId) {
        Set<InvoiceDTO>  ret = new UserBL(userId).getEntity().getInvoices();
        ret.iterator().next().getDueDate(); // touch
        return ret;
    }
}    
