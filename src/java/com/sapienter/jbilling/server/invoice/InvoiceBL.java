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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.List;

import org.apache.log4j.Logger;

import javax.sql.rowset.CachedRowSet;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.item.CurrencyBL;
import com.sapienter.jbilling.server.item.ItemBL;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDAS;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDAS;
import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDTO;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.Util;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import java.util.ArrayList;
import org.springframework.dao.EmptyResultDataAccessException;

public class InvoiceBL extends ResultList implements Serializable, InvoiceSQL {

    private InvoiceDAS invoiceDas = null;
    private InvoiceDTO invoice = null;
    private static final Logger LOG = Logger.getLogger(InvoiceBL.class);
    private EventLogger eLogger = null;

    public InvoiceBL(Integer invoiceId) {
        init();
        set(invoiceId);
    }

    public InvoiceBL() {
        init();
    }

    public InvoiceBL(InvoiceDTO invoice) {
        init();
        set(invoice.getId());
    }

    private void init() {
        eLogger = EventLogger.getInstance();
        invoiceDas = new InvoiceDAS();
    }

    public InvoiceDTO getEntity() {
        return invoice;
    }

    public InvoiceDAS getHome() {
        return invoiceDas;
    }

    public void set(Integer id) {
        invoice = invoiceDas.find(id);
    }

    public void set(InvoiceDTO invoice) {
        this.invoice = invoice;
    }

    /**
     * 
     * @param userId
     * @param newInvoice
     * @param process
     *            It can be null.
     */
    public void create(Integer userId, NewInvoiceDTO newInvoice,
            BillingProcessDTO process) {
        // find out the entity id
        PreferenceBL pref = new PreferenceBL();
        UserBL user = null;
        Integer entityId;
        if (process != null) {
            entityId = process.getEntity().getId();
        } else {
            // this is a manual invoice, there's no billing process
            user = new UserBL(userId);
            entityId = user.getEntityId(userId);
        }

        // verify if this entity is using the 'continuous invoice date'
        // preference
        try {
            pref.set(entityId, Constants.PREFERENCE_CONTINUOUS_DATE);
            Date lastDate = com.sapienter.jbilling.common.Util.parseDate(pref.getString());
            if (lastDate.after(newInvoice.getBillingDate())) {
                newInvoice.setBillingDate(lastDate);
            } else {
                // update the lastest date only if this is not a review
                if (newInvoice.getIsReview() == null || newInvoice.getIsReview().intValue() == 0) {
                    pref.createUpdateForEntity(entityId,
                                               Constants.PREFERENCE_CONTINUOUS_DATE,
                                               com.sapienter.jbilling.common.Util.parseDate(newInvoice.getBillingDate()));
                }
            }
        } catch (EmptyResultDataAccessException e) {
        // not interested, ignore
        }

        // in any case, ensure that the due date is => that invoice date
        if (newInvoice.getDueDate().before(newInvoice.getBillingDate())) {
            newInvoice.setDueDate(newInvoice.getBillingDate());
        }
        // ensure that there are only two decimals in the invoice
        if (newInvoice.getTotal() != null) {
            newInvoice.setTotal(newInvoice.getTotal().setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
        }
        if (newInvoice.getBalance() != null) {
            newInvoice.setBalance(newInvoice.getBalance().setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
        }

        // create the invoice row
        invoice = invoiceDas.create(userId, newInvoice, process);

        // add delegated/included invoice links
        if (newInvoice.getIsReview() == 0) {
            for (InvoiceDTO dto : newInvoice.getInvoices()) {
                dto.setInvoice(invoice);
            }
        }

        // add the customer notes if it applies
        try {
            pref.set(entityId, Constants.PREFERENCE_SHOW_NOTE_IN_INVOICE);
        } catch (EmptyResultDataAccessException e) {
            // use the default then
        }

        if (pref.getInt() == 1) {
            if (user == null) {
                user = new UserBL(userId);
            }
            if (user.getEntity().getCustomer() != null && user.getEntity().getCustomer().getNotes() != null) {
                // append the notes if there's some text already there
                newInvoice.setCustomerNotes((newInvoice.getCustomerNotes() == null) ? user.getEntity().getCustomer().getNotes()
                        : newInvoice.getCustomerNotes() + " " + user.getEntity().getCustomer().getNotes());
            }
        }
        // notes might come from the customer, the orders, or both
        if (newInvoice.getCustomerNotes() != null && newInvoice.getCustomerNotes().length() > 0) {
            invoice.setCustomerNotes(newInvoice.getCustomerNotes());
        }

        // calculate/compose the number
        String numberStr = null;
        if (newInvoice.getIsReview() != null && newInvoice.getIsReview().intValue() == 1) {
            // invoices for review will be seen by the entity employees
            // so the entity locale will be used
            EntityBL entity = new EntityBL(entityId);
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "entityNotifications", entity.getLocale());
            numberStr = bundle.getString("invoice.review.number");
        } else if (newInvoice.getPublicNumber() == null || newInvoice.getPublicNumber().length() == 0) {
            String prefix;
            try {
                pref.set(entityId, Constants.PREFERENCE_INVOICE_PREFIX);
                prefix = pref.getString();
                if (prefix == null) {
                    prefix = "";
                }
            } catch (EmptyResultDataAccessException e) {
                prefix = "";
            }
            int number;
            try {
                pref.set(entityId, Constants.PREFERENCE_INVOICE_NUMBER);
                number = pref.getInt();
            } catch (EmptyResultDataAccessException e1) {
                number = 1;
            }

            numberStr = prefix + number;
            // update for the next time
            number++;
            pref.createUpdateForEntity(entityId, Constants.PREFERENCE_INVOICE_NUMBER, number);
        } else { // for upload of legacy invoices
            numberStr = newInvoice.getPublicNumber();
        }

        invoice.setPublicNumber(numberStr);

        // set the invoice's contact info with the current user's primary
        ContactBL contact = new ContactBL();
        contact.set(userId);
        contact.createForInvoice(contact.getDTO(), invoice.getId());

        // add a log row for convenience
        eLogger.auditBySystem(entityId, userId, Constants.TABLE_INVOICE, 
                invoice.getId(), EventLogger.MODULE_INVOICE_MAINTENANCE,
                EventLogger.ROW_CREATED, null, null, null);

    }

    public void createLines(NewInvoiceDTO newInvoice) {
        Collection invoiceLines = invoice.getInvoiceLines();

        // Now create all the invoice lines, from the lines in the DTO
        // put there by the invoice composition pluggable tasks
        InvoiceLineDAS invoiceLineDas = new InvoiceLineDAS();

        // get the result DTO lines
        Iterator dueInvoiceLines = newInvoice.getResultLines().iterator();
        // go over the DTO lines, creating one invoice line for each

        while (dueInvoiceLines.hasNext()) {
            InvoiceLineDTO lineToAdd = (InvoiceLineDTO) dueInvoiceLines.next();
            // define if the line is a percentage or not
            lineToAdd.setIsPercentage(new Integer(0));
            if (lineToAdd.getItem() != null) {
                try {
                    ItemBL item = new ItemBL(lineToAdd.getItem());
                    if (item.getEntity().getPercentage() != null) {
                        lineToAdd.setIsPercentage(new Integer(1));
                    }
                } catch (SessionInternalError e) {
                    LOG.error("Could not find item to create invoice line " + lineToAdd.getItem().getId());
                }
            }
            // create the database row
            InvoiceLineDTO newLine = invoiceLineDas.create(lineToAdd.getDescription(), lineToAdd.getAmount(), lineToAdd.getQuantity(), lineToAdd.getPrice(),
                    lineToAdd.getTypeId(), lineToAdd.getItem(), lineToAdd.getSourceUserId(), lineToAdd.getIsPercentage());

            // update the invoice-lines relationship
            newLine.setInvoice(invoice);
            invoiceLines.add(newLine);
        }
        getHome().save(invoice);
        EventManager.process(new NewInvoiceEvent(invoice));
    }

    /**
     * This will remove all the records (sql delete, not just flag them). It
     * will also update the related orders if applicable
     */
    public void delete(Integer executorId) throws SessionInternalError {
        if (invoice == null) {
            throw new SessionInternalError("An invoice has to be set before " + "delete");
        }
        // start by updating purchase_order.next_billable_day if applicatble
        // for each of the orders included in this invoice
        for (OrderProcessDTO orderProcess : (Collection<OrderProcessDTO>) invoice.getOrderProcesses()) {
            OrderDTO order = orderProcess.getPurchaseOrder();
            if (order.getNextBillableDay() == null) {
                // the next billable day doesn't need updating
                if (order.getStatusId().equals(Constants.ORDER_STATUS_FINISHED)) {
                    OrderBL orderBL = new OrderBL(order);
                    orderBL.setStatus(null, Constants.ORDER_STATUS_ACTIVE);
                }
                continue;
            }
            // only if this invoice is the responsible for the order's
            // next billable day
            if (order.getNextBillableDay().equals(orderProcess.getPeriodEnd())) {
                order.setNextBillableDay(orderProcess.getPeriodStart());
                if (order.getStatusId().equals(Constants.ORDER_STATUS_FINISHED)) {
                    OrderBL orderBL = new OrderBL(order);
                    orderBL.setStatus(null, Constants.ORDER_STATUS_ACTIVE);
                }
            }

        }

        // go over the order process records again just to delete them
        // we are done with this order, delete the process row
        for (OrderProcessDTO orderProcess : (Collection<OrderProcessDTO>) invoice.getOrderProcesses()) {
            OrderDTO order = orderProcess.getPurchaseOrder();
            OrderProcessDAS das = new OrderProcessDAS();
            order.getOrderProcesses().remove(orderProcess);
            das.delete(orderProcess);
        }
        invoice.getOrderProcesses().clear();

        // get rid of the contact associated with this invoice
        try {
            ContactBL contact = new ContactBL();
            if (contact.setInvoice(invoice.getId())) {
                contact.delete();
            }
        } catch (Exception e1) {
            LOG.error("Exception deleting the contact of an invoice", e1);
        }

        // remove the payment link/s
        PaymentBL payment = new PaymentBL();
        Iterator<PaymentInvoiceMapDTO> it = invoice.getPaymentMap().iterator();
        while (it.hasNext()) {
            PaymentInvoiceMapDTO map = it.next();
            payment.removeInvoiceLink(map.getId());
            invoice.getPaymentMap().remove(map);
            // needed because the collection has changed
            it = invoice.getPaymentMap().iterator();
        }

        // log that this was deleted, otherwise there will be no trace
        if (executorId != null) {
            eLogger.audit(executorId, invoice.getBaseUser().getId(), 
                    Constants.TABLE_INVOICE, invoice.getId(),
                    EventLogger.MODULE_INVOICE_MAINTENANCE,
                    EventLogger.ROW_DELETED, null, null, null);
        }

        // before delete the invoice most delete the reference in table
        // PAYMENT_INVOICE
        new PaymentInvoiceMapDAS().deleteAllWithInvoice(invoice);

        // now delete the invoice itself
        getHome().delete(invoice);
        getHome().flush();
    }

    public void update(NewInvoiceDTO addition) {
        // add the lines to the invoice first
        createLines(addition);
        // update the inoice record considering the new lines
        invoice.setTotal(calculateTotal()); // new total
        // adjust the balance
        addition.calculateTotal();
        BigDecimal balance = invoice.getBalance();
        balance = balance.add(addition.getTotal());
        invoice.setBalance(balance);
//?        if (invoice.getBalance().floatValue() <= 0.001F) {
        if (invoice.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setToProcess(new Integer(0));
        }
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = new BigDecimal(0);
        for (Iterator it = invoice.getInvoiceLines().iterator(); it.hasNext();) {
            InvoiceLineDTO line = (InvoiceLineDTO) it.next();
            total = total.add(line.getAmount());
        }
        return total;
    }

    public CachedRowSet getPayableInvoicesByUser(Integer userId)
            throws SQLException, Exception {

        prepareStatement(InvoiceSQL.payableByUser);
        cachedResults.setInt(1, userId.intValue());

        execute();
        conn.close();
        return cachedResults;
    }

    public BigDecimal getTotalPaid() {
        BigDecimal retValue = new BigDecimal(0);
        for (Iterator<PaymentInvoiceMapDTO> it = invoice.getPaymentMap().iterator(); it.hasNext();) {
            PaymentInvoiceMapDTO paymentMap = it.next();
            retValue = retValue.add(paymentMap.getAmount());
        }
        return retValue;
    }

    public CachedRowSet getList(Integer orderId) throws SQLException, Exception {
        prepareStatement(InvoiceSQL.customerList);

        // find out the user from the order
        Integer userId;
        OrderBL order = new OrderBL(orderId);
        if (order.getEntity().getUser().getCustomer().getParent() == null) {
            userId = order.getEntity().getUser().getUserId();
        } else {
            userId = order.getEntity().getUser().getCustomer().getParent().getBaseUser().getUserId();
        }
        cachedResults.setInt(1, userId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getList(Integer entityId, Integer userRole,
            Integer userId) throws SQLException, Exception {

        if (userRole.equals(Constants.TYPE_INTERNAL)) {
            prepareStatement(InvoiceSQL.internalList);
        } else if (userRole.equals(Constants.TYPE_ROOT) || userRole.equals(Constants.TYPE_CLERK)) {
            prepareStatement(InvoiceSQL.rootClerkList);
            cachedResults.setInt(1, entityId.intValue());
        } else if (userRole.equals(Constants.TYPE_PARTNER)) {
            prepareStatement(InvoiceSQL.partnerList);
            cachedResults.setInt(1, entityId.intValue());
            cachedResults.setInt(2, userId.intValue());
        } else if (userRole.equals(Constants.TYPE_CUSTOMER)) {
            prepareStatement(InvoiceSQL.customerList);
            cachedResults.setInt(1, userId.intValue());
        } else {
            throw new Exception("The invoice list for the type " + userRole + " is not supported");
        }

        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getInvoicesByProcessId(Integer processId)
            throws SQLException, Exception {

        prepareStatement(InvoiceSQL.processList);
        cachedResults.setInt(1, processId.intValue());

        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getInvoicesToPrintByProcessId(Integer processId)
            throws SQLException, Exception {

        prepareStatement(InvoiceSQL.processPrintableList);
        cachedResults.setInt(1, processId.intValue());

        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getInvoicesByUserId(Integer userId)
            throws SQLException, Exception {

        prepareStatement(InvoiceSQL.custList);
        cachedResults.setInt(1, userId.intValue());

        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getInvoicesByIdRange(Integer from, Integer to,
            Integer entityId) throws SQLException, Exception {

        prepareStatement(InvoiceSQL.rangeList);
        cachedResults.setInt(1, from.intValue());
        cachedResults.setInt(2, to.intValue());
        cachedResults.setInt(3, entityId.intValue());

        execute();
        conn.close();
        return cachedResults;
    }

    public Integer[] getInvoicesByCreateDateArray(Integer entityId, Date since,
            Date until) throws SQLException, Exception {

        cachedResults = getInvoicesByCreateDate(entityId, since, until);

        // get ids for return
        List ids = new ArrayList();
        while (cachedResults.next()) {
            ids.add(new Integer(cachedResults.getInt(1)));
        }
        Integer[] retValue = new Integer[ids.size()];
        if (retValue.length > 0) {
            ids.toArray(retValue);
        }

        return retValue;
    }

    public CachedRowSet getInvoicesByCreateDate(Integer entityId, Date since,
            Date until) throws SQLException, Exception {

        prepareStatement(InvoiceSQL.getByDate);
        cachedResults.setInt(1, entityId.intValue());
        cachedResults.setDate(2, new java.sql.Date(since.getTime()));
        // add a day to include the until date
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(until);
        cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
        cachedResults.setDate(3, new java.sql.Date(cal.getTime().getTime()));

        execute();

        conn.close();
        return cachedResults;
    }

    public Integer convertNumberToID(Integer entityId, String number)
            throws SQLException, Exception {

        prepareStatement(InvoiceSQL.getIDfromNumber);
        cachedResults.setInt(1, entityId.intValue());
        cachedResults.setString(2, number);

        execute();

        conn.close();
        if (cachedResults.wasNull()) {
            return null;
        } else {
            cachedResults.next();
            return new Integer(cachedResults.getInt(1));
        }
    }

    public Integer getLastByUser(Integer userId) throws SQLException {

        Integer retValue = null;
        if (userId == null) {
            return null;
        }
        prepareStatement(InvoiceSQL.lastIdbyUser);
        cachedResults.setInt(1, userId.intValue());

        execute();
        if (cachedResults.next()) {
            int value = cachedResults.getInt(1);
            if (!cachedResults.wasNull()) {
                retValue = new Integer(value);
            }
        }
        conn.close();
        return retValue;
    }

    public Integer getLastByUserAndItemType(Integer userId, Integer itemTypeId) 
            throws SQLException {

        Integer retValue = null;
        if (userId == null) {
            return null;
        }            
        prepareStatement(InvoiceSQL.lastIdbyUserAndItemType);
        cachedResults.setInt(1, userId.intValue());
        cachedResults.setInt(2, itemTypeId.intValue());
        
        execute();
        if (cachedResults.next()) {
            int value = cachedResults.getInt(1);
            if (!cachedResults.wasNull()) {
                retValue = new Integer(value);
            }
        } 
        cachedResults.close();
        conn.close();
        return retValue;
    }

    public Boolean isUserWithOverdueInvoices(Integer userId, Date today,
            Integer excludeInvoiceId) throws SQLException {

        Boolean retValue;
        prepareStatement(InvoiceSQL.getOverdueForAgeing);
        cachedResults.setDate(1, new java.sql.Date(today.getTime()));
        cachedResults.setInt(2, userId.intValue());
        if (excludeInvoiceId != null) {
            cachedResults.setInt(3, excludeInvoiceId.intValue());
        } else {
            // nothing to exclude, use an imposible ID (zero)
            cachedResults.setInt(3, 0);
        }

        execute();
        if (cachedResults.next()) {
            retValue = new Boolean(true);
            LOG.debug("user with invoice:" + cachedResults.getInt(1));
        } else {
            retValue = new Boolean(false);
        }
        conn.close();
        LOG.debug("user with overdue: " + retValue);
        return retValue;
    }

    public Integer[] getUsersOverdueInvoices(Integer userId, Date date) {
        List<Integer> result = new InvoiceDAS().findIdsOverdueForUser(userId, date);
        return result.toArray(new Integer[result.size()]);
    }

    public Integer[] getUserInvoicesByDate(Integer userId, Date since, 
            Date until) {
        // add a day to include the until date
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(until);
        cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
        until = cal.getTime();

        List<Integer> result = new InvoiceDAS().findIdsByUserAndDate(
                userId, since, until);

        return result.toArray(new Integer[result.size()]);
    }

    public Integer[] getManyWS(Integer userId, Integer number)
            throws SessionInternalError {
        List<Integer> result = new InvoiceDAS().findIdsByUserLatestFirst(
                userId, number);
        return result.toArray(new Integer[result.size()]);
    }

    public Integer[] getManyByItemTypeWS(Integer userId, Integer itemTypeId, Integer number)
            throws SessionInternalError {
        List<Integer> result = new InvoiceDAS().findIdsByUserAndItemTypeLatestFirst(userId, itemTypeId, number);
        return result.toArray(new Integer[result.size()]);
    }


    public InvoiceWS[] DTOtoWS(List dtos) {
        InvoiceWS retValue[] = new InvoiceWS[dtos.size()];
        for (int f = 0; f < retValue.length; f++) {
            retValue[f] = InvoiceBL.getWS((InvoiceDTO) dtos.get(f));
        }
        LOG.debug("converstion " + retValue.length);

        return retValue;
    }

    

    public void sendReminders(Date today) throws SQLException,
            SessionInternalError {
        GregorianCalendar cal = new GregorianCalendar();

        for (Iterator it = new CompanyDAS().findEntities().iterator(); it.hasNext();) {
            CompanyDTO thisEntity = (CompanyDTO) it.next();
            Integer entityId = thisEntity.getId();
            PreferenceBL pref = new PreferenceBL();
            try {
                pref.set(entityId, Constants.PREFERENCE_USE_INVOICE_REMINDERS);
            } catch (EmptyResultDataAccessException e1) {
            // let it use the defaults
            }
            if (pref.getInt() == 1) {
                prepareStatement(InvoiceSQL.toRemind);

                cachedResults.setDate(1, new java.sql.Date(today.getTime()));
                cal.setTime(today);
                pref.set(entityId, Constants.PREFERENCE_FIRST_REMINDER);
                cal.add(GregorianCalendar.DAY_OF_MONTH, -pref.getInt());
                cachedResults.setDate(2, new java.sql.Date(cal.getTimeInMillis()));
                cal.setTime(today);
                pref.set(entityId, Constants.PREFERENCE_NEXT_REMINDER);
                cal.add(GregorianCalendar.DAY_OF_MONTH, -pref.getInt());
                cachedResults.setDate(3, new java.sql.Date(cal.getTimeInMillis()));

                cachedResults.setInt(4, entityId.intValue());

                execute();
                while (cachedResults.next()) {
                    int invoiceId = cachedResults.getInt(1);
                    set(new Integer(invoiceId));
                    NotificationBL notif = new NotificationBL();
                    long mils = invoice.getDueDate().getTime() - today.getTime();
                    int days = Math.round(mils / 1000 / 60 / 60 / 24);

                    try {
                        MessageDTO message = notif.getInvoiceRemainderMessage(
                                entityId, invoice.getBaseUser().getUserId(),
                                new Integer(days), invoice.getDueDate(),
                                invoice.getPublicNumber(), invoice.getTotal(),
                                invoice.getCreateDatetime(), invoice.getCurrency().getId());

                        INotificationSessionBean notificationSess = 
                                (INotificationSessionBean) Context.getBean(
                                Context.Name.NOTIFICATION_SESSION);

                        notificationSess.notify(invoice.getBaseUser(), message);

                        invoice.setLastReminder(today);
                    } catch (NotificationNotFoundException e) {
                        LOG.warn("There are invoice to send reminders, but " + "the notification message is missing for " + "entity " + entityId);
                    }
                }
            }
        }

        if (conn != null) { // only if somthing run
            conn.close();
        }

    }

    public InvoiceWS getWS() {
        return getWS(invoice);
    }

    public static InvoiceWS getWS(InvoiceDTO i) {
        if (i == null) {
            return null;
        }
        InvoiceWS retValue = new InvoiceWS();
        retValue.setId(i.getId());
        retValue.setCreateDateTime(i.getCreateDatetime());
        retValue.setCreateTimeStamp(i.getCreateTimestamp());
        retValue.setLastReminder(i.getLastReminder());
        retValue.setDueDate(i.getDueDate());
        retValue.setTotal(i.getTotal());
        retValue.setToProcess(i.getToProcess());
        retValue.setStatusId(i.getInvoiceStatus().getId());
        retValue.setBalance(i.getBalance());
        retValue.setCarriedBalance(i.getCarriedBalance());
        retValue.setInProcessPayment(i.getInProcessPayment());
        retValue.setDeleted(i.getDeleted());
        retValue.setPaymentAttempts(i.getPaymentAttempts());
        retValue.setIsReview(i.getIsReview());
        retValue.setCurrencyId(i.getCurrency().getId());
        retValue.setCustomerNotes(i.getCustomerNotes());
        retValue.setNumber(i.getPublicNumber());
        retValue.setOverdueStep(i.getOverdueStep());
        retValue.setUserId(i.getBaseUser().getId());

        Integer delegatedInvoiceId = i.getInvoice() == null ? null : i.getInvoice().getId();
        Integer userId = i.getBaseUser().getId();
        Integer payments[] = new Integer[i.getPaymentMap().size()];
        com.sapienter.jbilling.server.entity.InvoiceLineDTO invoiceLines[] = 
                new com.sapienter.jbilling.server.entity.InvoiceLineDTO[i.getInvoiceLines().size()];
        Integer orders[] = new Integer[i.getOrderProcesses().size()];

        int f;
        f = 0;
        for (PaymentInvoiceMapDTO p : i.getPaymentMap()) {
            payments[f++] = p.getPayment().getId();
        }
        f = 0;
        for (OrderProcessDTO orderP : i.getOrderProcesses()) {
            orders[f++] = orderP.getPurchaseOrder().getId();
        }
        f = 0;
        for (InvoiceLineDTO line : i.getInvoiceLines()) {
            invoiceLines[f++] = new com.sapienter.jbilling.server.entity.InvoiceLineDTO(line.getId(), 
                    line.getDescription(), line.getAmount(), line.getPrice(), line.getQuantity(), 
                    line.getDeleted(), line.getItem() == null ? null : line.getItem().getId(), 
                    line.getSourceUserId(), line.getIsPercentage());
        }

        retValue.setDelegatedInvoiceId(delegatedInvoiceId);
        retValue.setUserId(userId);
        retValue.setPayments(payments);
        retValue.setInvoiceLines(invoiceLines);
        retValue.setOrders(orders);

        return retValue;
    }

    public InvoiceDTO getDTOEx(Integer languageId, boolean forDisplay) {
        
        if (!forDisplay) {
            return invoice;
        }

        InvoiceDTO invoiceDTO = new InvoiceDTO(invoice);
        // make sure that the lines are properly ordered
        List<InvoiceLineDTO> orderdLines = new ArrayList<InvoiceLineDTO>(invoiceDTO.getInvoiceLines());
        Collections.sort(orderdLines, new InvoiceLineComparator());
        invoiceDTO.setInvoiceLines(orderdLines);
        
        UserBL userBl = new UserBL(invoice.getBaseUser());
        Locale locale = userBl.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("entityNotifications", locale);

        // now add headres and footers if this invoices has subaccount
        // lines
        if (invoiceDTO.hasSubAccounts()) {
            addHeadersFooters(orderdLines, bundle);
        }
        // add a grand total final line
        InvoiceLineDTO total = new InvoiceLineDTO();
        total.setDescription(bundle.getString("invoice.line.total"));
        total.setAmount(invoice.getTotal());
        total.setIsPercentage(0);
        invoiceDTO.getInvoiceLines().add(total);

        // add some currency info for the human
        CurrencyBL currency = new CurrencyBL(invoice.getCurrency().getId());
        if (languageId != null) {
            invoiceDTO.setCurrencyName(currency.getEntity().getDescription(
                    languageId));
        }
        invoiceDTO.setCurrencySymbol(currency.getEntity().getSymbol());

        return invoiceDTO;

    }

    /**
     * Will add lines with headers and footers to make an invoice with
     * subaccounts more readable. The lines have to be already sorted.
     * 
     * @param lines
     */
    private void addHeadersFooters(List<InvoiceLineDTO> lines, ResourceBundle bundle) {
        Integer nowProcessing = new Integer(-1);
        BigDecimal total = null;
        int totalLines = lines.size();
        int subaccountNumber = 0;

        LOG.debug("adding headers & footers." + totalLines);

        for (int idx = 0; idx < totalLines; idx++) {
            InvoiceLineDTO line = (InvoiceLineDTO) lines.get(idx);
            
            if (line.getTypeId() == Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT && !line.getSourceUserId().equals(nowProcessing)) {
                // line break
                nowProcessing = line.getSourceUserId();
                subaccountNumber++;
                // put the total first
                if (total != null) { // it could be the first subaccount
                    InvoiceLineDTO totalLine = new InvoiceLineDTO();
                    totalLine.setDescription(bundle.getString("invoice.line.subAccount.footer"));
                    totalLine.setAmount(total);
                    lines.add(idx, totalLine);
                    idx++;
                    totalLines++;
                }
                total = BigDecimal.ZERO;

                // now the header anouncing a new subaccout
                InvoiceLineDTO headerLine = new InvoiceLineDTO();
                try {
                    ContactBL contact = new ContactBL();
                    contact.set(nowProcessing);
                    StringBuffer text = new StringBuffer();
                    text.append(subaccountNumber + " - ");
                    text.append(bundle.getString("invoice.line.subAccount.header1"));
                    text.append(" " + bundle.getString("invoice.line.subAccount.header2") + " " + nowProcessing);
                    if (contact.getEntity().getFirstName() != null) {
                        text.append(" " + contact.getEntity().getFirstName());
                    }
                    if (contact.getEntity().getLastName() != null) {
                        text.append(" " + contact.getEntity().getLastName());
                    }
                    headerLine.setDescription(text.toString());
                    lines.add(idx, headerLine);
                    idx++;
                    totalLines++;
                } catch (Exception e) {
                    LOG.error("Exception", e);
                    return;
                }
            }

            // update the total
            if (total != null) {
                // there had been at least one sub-account processed
                if (line.getTypeId() ==
                        Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT) {
                    total = total.add(line.getAmount());
                } else {
                    // this is the last total to display, from now on the
                    // lines are not of subaccounts
                    InvoiceLineDTO totalLine = new InvoiceLineDTO();
                    totalLine.setDescription(bundle.getString("invoice.line.subAccount.footer"));
                    totalLine.setAmount(total);
                    lines.add(idx, totalLine);
                    total = null; // to avoid repeating
                }
            }
        }
        // if there are no lines after the last subaccount, we need
        // a total for it
        if (total != null) { // only if it wasn't added before
            InvoiceLineDTO totalLine = new InvoiceLineDTO();
            totalLine.setDescription(bundle.getString("invoice.line.subAccount.footer"));
            totalLine.setAmount(total);
            lines.add(totalLine);
        }

        LOG.debug("done " + lines.size());
    }

    public InvoiceDTO getDTO() {
        return invoice;

    }

    // given the current invoice, it will 'rewind' to the previous one
    public void setPrevious() throws SQLException,
            EmptyResultDataAccessException {

        prepareStatement(InvoiceSQL.previous);
        cachedResults.setInt(1, invoice.getBaseUser().getUserId().intValue());
        cachedResults.setInt(2, invoice.getId());
        boolean found = false;

        execute();
        if (cachedResults.next()) {
            int value = cachedResults.getInt(1);
            if (!cachedResults.wasNull()) {
                set(new Integer(value));
                found = true;
            }
        }
        conn.close();

        if (!found) {
            throw new EmptyResultDataAccessException("No previous invoice found", 1);
        }
    }

    /*
    public static InvoiceWS getWS(InvoiceDTO dto) {
        InvoiceWS ret = new InvoiceWS();
        ret.setBalance(dto.getBalance());
        ret.setCarriedBalance(dto.getCarriedBalance());
        ret.setCreateDateTime(dto.getCreateDatetime());
        ret.setCreateTimeStamp(dto.getCreateTimestamp());
        ret.setCurrencyId(dto.getCurrency().getId());
        ret.setCustomerNotes(dto.getCustomerNotes());
        ret.setDelegatedInvoiceId(dto.getDelegatedInvoiceId());
        ret.setDeleted(dto.getDeleted());
        ret.setDueDate(dto.getDueDate());
        ret.setInProcessPayment(dto.getInProcessPayment());
        ret.setUserId(dto.getUserId());
        ret.setIsReview(dto.getIsReview());
        ret.setLastReminder(dto.getLastReminder());
        ret.setNumber(dto.getPublicNumber());
        ret.setOverdueStep(dto.getOverdueStep());
        ret.setPaymentAttempts(dto.getOverdueStep());
        ret.setToProcess(dto.getToProcess());
        ret.setTotal(dto.getTotal());
        ret.setUserId(dto.getUserId());
        
        Integer payments[] = new Integer[dto.getPaymentMap().size()];
        Integer orders[] = new Integer[dto.getOrders().size()];

        int f;
        for (f = 0; f < dto.getPaymentMap().size(); f++) {
            PaymentInvoiceMapDTOEx map = (PaymentInvoiceMapDTOEx) dto.getPaymentMap().get(f);
            payments[f] = map.getPaymentId();
        }
        ret.setPayments(payments);
        for (f = 0; f < dto.getOrders().size(); f++) {
            OrderDTO order = (OrderDTO) dto.getOrders().get(f);
            orders[f] = order.getId();
        }
        ret.setOrders(orders);
        
        com.sapienter.jbilling.server.entity.InvoiceLineDTO lines[] = 
                new com.sapienter.jbilling.server.entity.InvoiceLineDTO[dto.getInvoiceLines().size()];
        
        f=0;
        for (InvoiceLineDTO line : dto.getInvoiceLines()) {
            lines[f++] = new com.sapienter.jbilling.server.entity.InvoiceLineDTO(line.getId(), 
                    line.getDescription(), line.getAmount(), line.getPrice(), line.getQuantity(), 
                    line.getDeleted(), line.getItem() == null ? null : line.getItem().getId(), 
                    line.getSourceUserId(), line.getIsPercentage());
        }
        
        return ret;
    }
     * */
}
