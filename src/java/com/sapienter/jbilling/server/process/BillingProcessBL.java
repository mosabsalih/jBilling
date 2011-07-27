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

package com.sapienter.jbilling.server.process;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import com.sapienter.jbilling.server.process.event.InvoicesGeneratedEvent;
import org.apache.log4j.Logger;

import javax.sql.rowset.CachedRowSet;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceStatusDAS;
import com.sapienter.jbilling.server.item.CurrencyBL;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.TimePeriod;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDAS;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.order.event.OrderAddedOnInvoiceEvent;
import com.sapienter.jbilling.server.order.event.OrderToInvoiceEvent;
import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
import com.sapienter.jbilling.server.pluggableTask.InvoiceCompositionTask;
import com.sapienter.jbilling.server.pluggableTask.InvoiceFilterTask;
import com.sapienter.jbilling.server.pluggableTask.OrderFilterTask;
import com.sapienter.jbilling.server.pluggableTask.OrderPeriodTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.db.*;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.Context;
import java.util.ArrayList;
import org.springframework.dao.EmptyResultDataAccessException;

public class BillingProcessBL extends ResultList
        implements ProcessSQL {

    private BillingProcessDAS billingProcessDas = null;
    private BillingProcessDTO billingProcess = null;
    private ProcessRunDAS processRunHome = null;
    private ProcessRunDTO processRun = null;
    private static final Logger LOG = Logger.getLogger(BillingProcessBL.class);
    private EventLogger eLogger = null;

    public BillingProcessBL(Integer billingProcessId) {
        init();
        set(billingProcessId);
    }

    public BillingProcessBL() {
        init();
    }

    public BillingProcessBL(BillingProcessDTO row) {
        init();
        billingProcess = row;
    }

    private void init() {
        eLogger = EventLogger.getInstance();
        billingProcessDas = new BillingProcessDAS();

        // now create the run info row
        processRunHome = new ProcessRunDAS();
    }

    public BillingProcessDTO getEntity() {
        return billingProcess;
    }

    public ProcessRunDTO getProcessRun() {
        return processRun;
    }    

    public BillingProcessDAS getHome() {
        return billingProcessDas;
    }

    public void set(Integer id) {
        billingProcess = billingProcessDas.find(id);
    }

    public void set(BillingProcessDTO pEntity) {
        billingProcess = pEntity;
    }

    public Integer findOrCreate(BillingProcessDTO dto) {
        billingProcess = billingProcessDas.isPresent(dto.getEntity().getId(), dto.getIsReview(),
                dto.getBillingDate());
        if (billingProcess == null) {
            create(dto);
        }

        return billingProcess.getId();
    }

    public Integer create(BillingProcessDTO dto) {
        // create the record 
        billingProcess = billingProcessDas.create(dto.getEntity(),
                dto.getBillingDate(), dto.getPeriodUnit().getId(),
                dto.getPeriodValue(), dto.getRetriesToDo());
        billingProcess.setIsReview(dto.getIsReview());
        processRun = processRunHome.create(
                billingProcess, dto.getBillingDate(), 0,
                new ProcessRunStatusDAS().find(Constants.PROCESS_RUN_STATUS_RINNING));        

        if (dto.getIsReview() == 1) {
            ConfigurationBL config = new ConfigurationBL(dto.getEntity().getId());
            config.getEntity().setReviewStatus(Constants.REVIEW_STATUS_GENERATED);
        }

        return billingProcess.getId();
    }

    /**
     * Generates one single invoice for one single purchase order. This is
     * meant to be called outside the billing process.
     * @param orderId
     * @return
     * @throws PluggableTaskException
     * @throws SessionInternalError
     */
    public InvoiceDTO generateInvoice(Integer orderId,
            Integer invoiceId)
            throws PluggableTaskException, SessionInternalError,
            SQLException {
        InvoiceDTO retValue = null;
        // find the order
        OrderBL order = new OrderBL(orderId);
        // define some data
        Integer entityId = order.getEntity().getUser().getEntity().getId();
        ConfigurationBL config = new ConfigurationBL(entityId);
        int maxPeriods = config.getEntity().getMaximumPeriods();
        boolean paymentApplication = config.getEntity().
                getAutoPaymentApplication() == 1;
        // The user could be the parent of a sub-account
        Integer userId = findUserId(order.getEntity());
        Date processDate = Calendar.getInstance().getTime();
        processDate = Util.truncateDate(processDate);
        // create the my invoice
        NewInvoiceDTO newInvoice = new NewInvoiceDTO();
        newInvoice.setDate(processDate);
        newInvoice.setIsReview(new Integer(0));
        // find the due date that applies
        TimePeriod period = order.getDueDate();
        newInvoice.setDueDatePeriod(period);
        // this is an isolated invoice that doesn't care about previous
        // overdue invoices
        newInvoice.setCarriedBalance(BigDecimal.ZERO);
        newInvoice.setInvoiceStatus(new InvoiceStatusDAS().find(Constants.INVOICE_STATUS_UNPAID));

        try {
            // put the order in the invoice using all the pluggable taks stuff
            addOrderToInvoice(entityId, order.getEntity(), newInvoice,
                    processDate, maxPeriods);

            // this means that the user is trying to generate an invoice from
            // an order that the configurated tasks have rejected. Therefore
            // either this is the case an generating this invoice doesn't make
            // sense, or some business rules in the tasks have to be changed
            // (probably with a personalized task for this entity)
            if (newInvoice.getOrders().size() == 0) {
                return null;
            }

            // process events before orders added to invoice
            processOrderToInvoiceEvents(newInvoice, entityId);

            // generate the invoice lines
            composeInvoice(entityId, userId, newInvoice);

            // process events after orders added to invoice
            processOrderAddedOnInvoiceEvents(newInvoice, entityId);

            // put the resulting invoice in the database
            if (invoiceId == null) {
                // it is a new invoice from a singe order
                retValue = generateDBInvoice(userId, newInvoice, null,
                        Constants.ORDER_PROCESS_ORIGIN_MANUAL);
                // try to get this new invioce paid by previously unlinked 
                // payments
                if (paymentApplication) {
                    PaymentBL pBL = new PaymentBL();
                    pBL.automaticPaymentApplication(retValue);
                }
            } else {
                // it is an order going into an existing invoice
                InvoiceBL invoice = new InvoiceBL(invoiceId);
                boolean isUnpaid = invoice.getEntity().getToProcess() == 1;
                invoice.update(newInvoice);
                retValue = invoice.getEntity();
                createOrderProcess(newInvoice, retValue, null,
                        Constants.ORDER_PROCESS_ORIGIN_MANUAL);
                eLogger.info(entityId, userId, invoiceId,
                        EventLogger.MODULE_INVOICE_MAINTENANCE,
                        EventLogger.INVOICE_ORDER_APPLIED,
                        Constants.TABLE_INVOICE);
                // if the invoice is now not payable, take the user
                // out of ageing
                if (isUnpaid && retValue.getToProcess() == 0) {
                    AgeingBL ageing = new AgeingBL();
                    ageing.out(retValue.getBaseUser(), null);
                }
            }
        } catch (TaskException e) {
            // this means that the user is trying to generate an invoice from
            // an order that the configurated tasks have rejected. Therefore
            // either this is the case an generating this invoice doesn't make
            // sense, or some business rules in the tasks have to be changed
            // (probably with a personalized task for this entity)
            LOG.warn("Exception in generate invoice ", e);
        }

        if (retValue != null) {
            InvoicesGeneratedEvent generatedEvent = new InvoicesGeneratedEvent(entityId, null);
            generatedEvent.getInvoiceIds().add(retValue.getId());
            EventManager.process(generatedEvent);
        }

        return retValue;
    }

    public InvoiceDTO[] generateInvoice(
            BillingProcessDTO process, UserDTO user,
            boolean isReview, boolean onlyRecurring)
            throws SessionInternalError {

        Integer userId = user.getUserId();
        Integer entityId = user.getEntity().getId();

        // get the configuration
        boolean useProcessDateForInvoice = true;
        int maximumPeriods = 1;
        boolean paymentApplication = false;
        try {
            ConfigurationBL config = new ConfigurationBL(process.getEntity().getId());
            useProcessDateForInvoice = config.getEntity().getInvoiceDateProcess() == 1;
            maximumPeriods = config.getEntity().getMaximumPeriods();
            paymentApplication = config.getEntity().getAutoPaymentApplication() == 1;
        } catch (Exception e) {
            // swallow exception
        }

        // this contains the generated invoices, one per due date
        // found in the applicable purchase orders.
        // The key is the object TimePeriod
        Hashtable<TimePeriod, NewInvoiceDTO> newInvoices = new Hashtable<TimePeriod, NewInvoiceDTO>();
        InvoiceDTO[] retValue = null;

        LOG.debug("In generateInvoice for user " + userId + " process date:" + process.getBillingDate());

        /*
         * Go through the orders first
         * This method will recursively call itself to find sub-accounts in any
         * level
         */
        boolean includedOrders = processOrdersForUser(user, entityId, process,
                                                      isReview, onlyRecurring, useProcessDateForInvoice,
                                                      maximumPeriods, newInvoices);

        if (!includedOrders || newInvoices.size() == 0) {
            // check if invoices without orders are allowed
            PreferenceBL preferenceBL = new PreferenceBL();
            try {
                preferenceBL.set(entityId, Constants.PREFERENCE_ALLOW_INVOICES_WITHOUT_ORDERS);
            } catch (EmptyResultDataAccessException fe) {
                // use default
            }

            if (preferenceBL.getInt() == 0) {
                LOG.debug("No applicable orders. No invoice generated (skipping invoices).");
                return null;
            }
        }

        if (!isReview) {
            for (Map.Entry<TimePeriod, NewInvoiceDTO> newInvoiceEntry : newInvoices.entrySet()) {
                // process events before orders added to invoice
                processOrderToInvoiceEvents(newInvoiceEntry.getValue(), entityId);
            }
        }

        /*
         * Include those invoices that should've been paid
         * (or have negative balance, as credits)
         */
        LOG.debug("Considering overdue invoices");
        // find the invoice home interface
        InvoiceDAS invoiceDas = new InvoiceDAS();
        // any of the new invoices being created could hold the overdue invoices
        NewInvoiceDTO holder = newInvoices.isEmpty() ? null : (NewInvoiceDTO) newInvoices.elements().nextElement();

        Collection dueInvoices =
                invoiceDas.findWithBalanceByUser(user);
        LOG.debug("Processing invoices for user " + user.getUserId());
        // go through each of them, and update the DTO if it applies

        for (Iterator it = dueInvoices.iterator(); it.hasNext();) {
            InvoiceDTO invoice = (InvoiceDTO) it.next();
            LOG.debug("Processing invoice " + invoice.getId());
            // apply any invoice processing filter pluggable task
            try {
                PluggableTaskManager taskManager
                    = new PluggableTaskManager(entityId, Constants.PLUGGABLE_TASK_INVOICE_FILTER);
                InvoiceFilterTask task = (InvoiceFilterTask) taskManager.getNextClass();
                boolean isProcessable = true;
                while (task != null) {
                    isProcessable = task.isApplicable(invoice, process);
                    if (!isProcessable) {
                        break; // no need to keep doing more tests
                    }
                    task = (InvoiceFilterTask) taskManager.getNextClass();
                }

                // include this invoice only if it complies with all the rules
                if (isProcessable) {
                    // check for an invoice
                    if (holder == null) {
                        // Since there are no new invoices (therefore no orders),
                        // don't let invoices with positive balances generate
                        // an invoice.
                        if (BigDecimal.ZERO.compareTo(invoice.getBalance()) < 0) {
                            continue;
                        }
                        
                        // no invoice/s yet (no applicable orders), so create one
                        holder = new NewInvoiceDTO();
                        holder.setDate(process.getBillingDate());
                        holder.setIsReview(isReview ? new Integer(1) : new Integer(0));
                        holder.setCarriedBalance(BigDecimal.ZERO);
                        holder.setInvoiceStatus(new InvoiceStatusDAS().find(Constants.INVOICE_STATUS_UNPAID));

                        // need to set a due date, so use the order default
                        OrderBL orderBl = new OrderBL();
                        OrderDTO order = new OrderDTO();
                        order.setBaseUserByUserId(user);
                        orderBl.set(order);
                        TimePeriod dueDatePeriod = orderBl.getDueDate();

                        holder.setDueDatePeriod(dueDatePeriod);
                        newInvoices.put(dueDatePeriod, holder);
                    }

                    InvoiceBL ibl = new InvoiceBL(invoice);
                    holder.addInvoice(ibl.getDTO());
                    // for those invoices wiht only overdue invoices, the
                    // currency has to be initialized

                    if (holder.getCurrency() == null) {
                        holder.setCurrency(invoice.getCurrency());
                    } else if (holder.getCurrency().getId() != invoice.getCurrency().getId()) {
                        throw new SessionInternalError("An invoice with different " +
                                "currency is not supported. " +
                                "Currency = " + holder.getCurrency().getId() +
                                "invoice = " + invoice.getId());
                    }
                    // update the amount of the new invoice that is due to
                    // previously unpaid overdue invoices

                    // carry the remaining balance, plus the previously carried balance to the new invoice
                    BigDecimal balance = (invoice.getBalance() == null) ? BigDecimal.ZERO : invoice.getBalance();
                    BigDecimal carried = balance.add(holder.getCarriedBalance());
                    holder.setCarriedBalance(carried);
                }

                LOG.debug("invoice " + invoice.getId() + " result " + isProcessable);

            } catch (PluggableTaskException e) {
                LOG.fatal("Problems handling task invoice filter.", e);
                throw new SessionInternalError("Problems handling task invoice filter.");
            } catch (TaskException e) {
                LOG.fatal("Problems excecuting task invoice filter.", e);
                throw new SessionInternalError("Problems executing task invoice filter.");
            }

        }


        if (newInvoices.size() == 0) {
            // no orders or invoices for this invoice
            LOG.debug("No applicable orders or invoices. No invoice generated (skipping invoices).");
            return null;
        }

        try {
            retValue = new InvoiceDTO[newInvoices.size()];
            int index = 0;
            for (NewInvoiceDTO invoice : newInvoices.values()) {
                /*
                 * Apply invoice composition tasks to the new invoices object
                 */
                composeInvoice(entityId, user.getUserId(), invoice);

                if (!isReview) {
                    // process events after orders added to invoice
                    processOrderAddedOnInvoiceEvents(invoice, entityId);
                    for (InvoiceDTO oldInvoice : invoice.getInvoices()) {
                        // since this invoice is being delegated, mark it as being carried forward
                        // so that it is not re-processed later. do not clear the old balance!
                        oldInvoice.setInvoiceStatus(new InvoiceStatusDAS().find(Constants.INVOICE_STATUS_UNPAID_AND_CARRIED));
                    }
                }

                /*
                 * apply this object to the DB, generating the actual rows 
                 */
                // only if the invoice generated actually has some lines in it
                if (invoice.areLinesGeneratedEmpty()) {
                    LOG.warn("User " + user.getUserId() + " had orders or invoices but" +
                            " the invoice composition task didn't generate any lines.");
                    continue;
                }

                // If this is a web services API call, the billing 
                // process id is 0. Don't link to the billing process 
                // object for API calls.
                retValue[index] = generateDBInvoice(user.getUserId(),
                                                    invoice,
                                                    (process.getId() != 0 ? process : null),
                                                    Constants.ORDER_PROCESS_ORIGIN_PROCESS);

                // try to get this new invioce paid by previously unlinked 
                // payments
                if (paymentApplication && !isReview) {
                    PaymentBL pBL = new PaymentBL();
                    pBL.automaticPaymentApplication(retValue[index]);
                }

                index++;
            }
        } catch (PluggableTaskException e1) {
            LOG.error("Error handling pluggable tasks when composing an invoice");
            throw new SessionInternalError(e1);
        } catch (TaskException e1) {
            LOG.error("Task exception when composing an invoice");
            throw new SessionInternalError(e1);
        } catch (Exception e1) {
            LOG.error("Error, probably linking payments", e1);
            throw new SessionInternalError(e1);
        }

        InvoicesGeneratedEvent generatedEvent = new InvoicesGeneratedEvent(entityId, process.getId());
        generatedEvent.addInvoices(retValue);
        EventManager.process(generatedEvent);

        return retValue;
    }

    private boolean processOrdersForUser(UserDTO user, Integer entityId,BillingProcessDTO process, 
            boolean isReview, boolean onlyRecurring, boolean useProcessDateForInvoice,
        int maximumPeriods, Hashtable<TimePeriod, NewInvoiceDTO> newInvoices) {
        
        boolean includedOrders = false;
        Integer userId = user.getUserId();

        LOG.debug("Processing orders for user " + userId);

        // initialize the subaccounts iterator if this user is a parent
        Iterator subAccountsIt = null;
        if (user.getCustomer().getIsParent() != null &&
                user.getCustomer().getIsParent().intValue() == 1) {
            UserBL parent = new UserBL(userId);
            subAccountsIt = parent.getEntity().getCustomer().getChildren().
                    iterator();
        }

        // get the orders that might be processable for this user
        OrderDAS orderDas = new OrderDAS();
        Collection<OrderDTO> orders = orderDas.findByUser_Status(userId,
                Constants.ORDER_STATUS_ACTIVE);

        // go through each of them, and update the DTO if it applies
        for (OrderDTO order : orders) {
            LOG.debug("Processing order :" + order.getId());
            // apply any order processing filter pluggable task
            try {
                PluggableTaskManager taskManager = new PluggableTaskManager(
                        entityId,
                        Constants.PLUGGABLE_TASK_ORDER_FILTER);
                OrderFilterTask task = (OrderFilterTask) taskManager.getNextClass();
                boolean isProcessable = true;
                while (task != null) {
                    isProcessable = task.isApplicable(order, process);
                    if (!isProcessable) {
                        break; // no need to keep doing more tests
                    }
                    task = (OrderFilterTask) taskManager.getNextClass();
                }

                // include this order only if it complies with all the
                // rules
                if (isProcessable) {

                    LOG.debug("Order processable");

                    if (onlyRecurring) {
                        if (order.getOrderPeriod().getId() != Constants.ORDER_PERIOD_ONCE) {
                            includedOrders = true;
                            LOG.debug("It is not one-timer. " +
                                    "Generating invoice");
                        }
                    } else {
                        includedOrders = true;
                    }
                    /*
                     * now find if there is already an invoice being
                     * generated for the given due date period
                     */
                    // find the due date that applies to this order
                    OrderBL orderBl = new OrderBL();
                    orderBl.set(order);
                    TimePeriod dueDatePeriod = orderBl.getDueDate();
                    // look it up in the hashtable
                    NewInvoiceDTO thisInvoice = (NewInvoiceDTO) newInvoices.get(dueDatePeriod);
                    if (thisInvoice == null) {
                        LOG.debug("Adding new invoice for period " + dueDatePeriod + " process date:" + process.getBillingDate());
                        // we need a new one with this period
                        // define the invoice date
                        thisInvoice = new NewInvoiceDTO();
                        if (useProcessDateForInvoice) {
                            thisInvoice.setDate(process.getBillingDate());
                        } else {
                            thisInvoice.setDate(orderBl.getInvoicingDate(),
                                    order.getOrderPeriod().getId() != Constants.ORDER_PERIOD_ONCE);
                        }
                        thisInvoice.setIsReview(isReview ? new Integer(1) : new Integer(0));
                        thisInvoice.setCarriedBalance(BigDecimal.ZERO);
                        thisInvoice.setDueDatePeriod(dueDatePeriod);
                    } else {
                        LOG.debug("invoice found for period " + dueDatePeriod);
                        if (!useProcessDateForInvoice) {
                            thisInvoice.setDate(orderBl.getInvoicingDate(),
                                    order.getOrderPeriod().getId() != Constants.ORDER_PERIOD_ONCE);
                        }
                    }
                    /*
                     * The order periods plug-in might not add any period. This should not happen
                     * but if it does, the invoice should not be included
                     */
                    if (addOrderToInvoice(entityId, order, thisInvoice,
                            process.getBillingDate(), maximumPeriods)) {
                        // add or replace
                        newInvoices.put(dueDatePeriod, thisInvoice);
                    }
                    LOG.debug("After putting period there are " + newInvoices.size() + " periods.");

                // here it would be easy to update this order
                // to_process and
                // next_billable_time. I can't do that because these
                // fields
                // will be read by the following tasks, and they
                // will asume
                // they are not modified.
                }

            } catch (PluggableTaskException e) {
                LOG.fatal("Problems handling order filter task.", e);
                throw new SessionInternalError(
                        "Problems handling order filter task.");
            } catch (TaskException e) {
                LOG.fatal("Problems excecuting order filter task.", e);
                throw new SessionInternalError(
                        "Problems executing order filter task.");
            }
        } // for - all the orders for this user

        // see if there is any subaccounts to include in this invoice
        while (subAccountsIt != null) {  // until there are no more subaccounts (subAccountsIt != null) {
            CustomerDTO customer = null;
            while (subAccountsIt.hasNext()) {
                customer = (CustomerDTO) subAccountsIt.next();
                if (customer.getInvoiceChild() == null ||
                        customer.getInvoiceChild().intValue() == 0) {
                    break;
                } else {
                    LOG.debug("Subaccount not included in parent's invoice " +
                            customer.getId());
                    customer = null;
                }
            }
            if (customer != null) {
                userId = customer.getBaseUser().getUserId();
                // if the child does not have any orders to invoice, this should
                // not affect the current value of includedOrders
                if (processOrdersForUser(customer.getBaseUser(),
                        entityId, process, isReview, onlyRecurring,
                        useProcessDateForInvoice, maximumPeriods,
                        newInvoices)) {
                    // if ANY child has orders to invoice, it is enough for includedOrders to be true
                    includedOrders = true;
                }
                LOG.debug("Now processing subaccount " + userId);

            } else {
                subAccountsIt = null;
                LOG.debug("No more subaccounts to process");
            }
        } 

        return includedOrders;
    }

    private InvoiceDTO generateDBInvoice(Integer userId,
            NewInvoiceDTO newInvoice, BillingProcessDTO process,
            Integer origin)
            throws SessionInternalError {
        // The invoice row is created first
        // all that fits in the DTO goes there
        newInvoice.calculateTotal();
        
        if (newInvoice.getCarriedBalance() != null) {
            newInvoice.setBalance(newInvoice.getTotal().subtract(newInvoice.getCarriedBalance()));
        } else {
            newInvoice.setBalance(newInvoice.getTotal());
        }

        newInvoice.setInProcessPayment(new Integer(1));
        InvoiceBL invoiceBL = new InvoiceBL();

        try {
            invoiceBL.create(userId, newInvoice, process);
            invoiceBL.createLines(newInvoice);
        } catch (Exception e) {
            LOG.fatal("CreateException creating invoice record", e);
            throw new SessionInternalError("Couldn't create the invoice record");
        }

        createOrderProcess(newInvoice, invoiceBL.getEntity(), process,
                origin);

        return invoiceBL.getEntity();

    }

    private void createOrderProcess(NewInvoiceDTO newInvoice,
            InvoiceDTO invoice, BillingProcessDTO process,
            Integer origin)
            throws SessionInternalError {
        LOG.debug("Generating order process records...");
        // update the orders involved, now that their old data is not needed
        // anymore
        for (int f = 0; f < newInvoice.getOrders().size(); f++) {

            OrderDTO order = (OrderDTO) newInvoice.getOrders().get(f);

            LOG.debug(" ... order " + order.getId());
            // this will help later
            List<PeriodOfTime> periodsList = newInvoice.getPeriods().get(f);
            Date startOfBillingPeriod = (Date) periodsList.get(0).getStart();
            Date endOfBillingPeriod = periodsList.get(periodsList.size() - 1).getEnd();
            Integer periods = (Integer) newInvoice.getPeriods().get(f).size();

            // We don't update orders if this is just a review
            if (newInvoice.getIsReview().intValue() == 0) {
                // update the to_process if applicable
                updateStatusFinished(order, startOfBillingPeriod,
                        endOfBillingPeriod);

                // update this order process field
                updateNextBillableDay(order, endOfBillingPeriod);
            }

            // create the period and update the order-invoice relationship
            try {

                OrderProcessDAS das = new OrderProcessDAS();
                OrderProcessDTO orderProcess = new OrderProcessDTO();
                orderProcess.setPeriodStart(startOfBillingPeriod);
                orderProcess.setPeriodEnd(endOfBillingPeriod);
                orderProcess.setIsReview(newInvoice.getIsReview());
                orderProcess.setPurchaseOrder(order);
                InvoiceDAS invDas = new InvoiceDAS();
                orderProcess.setInvoice(invDas.find(invoice.getId()));
                BillingProcessDAS proDas = new BillingProcessDAS();
                orderProcess.setBillingProcess(process != null ? proDas.find(process.getId()) : null);
                orderProcess.setPeriodsIncluded(periods);
                orderProcess.setOrigin(origin);
                orderProcess = das.save(orderProcess);
                LOG.debug("created order process id " + orderProcess.getId() +
                        " for order " + order.getId());

            } catch (Exception e) {
                throw new SessionInternalError(e);
            }
        }

    }

    private void composeInvoice(Integer entityId, Integer userId,
            NewInvoiceDTO newInvoice)
            throws PluggableTaskException, TaskException, SessionInternalError {
        newInvoice.setEntityId(entityId);
        PluggableTaskManager taskManager =
                new PluggableTaskManager(entityId,
                Constants.PLUGGABLE_TASK_INVOICE_COMPOSITION);
        InvoiceCompositionTask task =
                (InvoiceCompositionTask) taskManager.getNextClass();
        while (task != null) {
            task.apply(newInvoice, userId);
            task = (InvoiceCompositionTask) taskManager.getNextClass();
        }

        String validationMessage = newInvoice.validate();
        if (validationMessage != null) {
            LOG.error(
                    "Composing invoice for entity " + entityId + " invalid new invoice object: " + validationMessage);
            throw new SessionInternalError(
                    "NewInvoiceDTO:" + validationMessage);
        }
    }

    private boolean addOrderToInvoice(Integer entityId, OrderDTO order,
            NewInvoiceDTO newInvoice, Date processDate, int maxPeriods)
            throws SessionInternalError, TaskException,
            PluggableTaskException {
        // require the calculation of the period dates
        PluggableTaskManager taskManager = new PluggableTaskManager(
                entityId, Constants.PLUGGABLE_TASK_ORDER_PERIODS);
        OrderPeriodTask optask = (OrderPeriodTask) taskManager.getNextClass();

        if (optask == null) {
            throw new SessionInternalError("There has to be " +
                    "one order period pluggable task configured");
        }
        Date start = optask.calculateStart(order);
        Date end = optask.calculateEnd(order, processDate, maxPeriods, start);
        List<PeriodOfTime> periods = optask.getPeriods();
        // there isn't anything billable from this order
        if (periods.size() == 0) {
            return false;
        }

        if (start != null && end != null && start.after(end)) {
            // how come it starts after it ends ???
            throw new SessionInternalError("Calculated for " +
                    "order " + order.getId() + " a period that" + " starts after it ends:" + start + " " +
                    end);
        }

        // add this order to the invoice being created
        newInvoice.addOrder(order, start, end, periods);


        // prepaid orders shouldn't have to be included
        // past time.
        if (order.getBillingTypeId().compareTo(
                Constants.ORDER_BILLING_PRE_PAID) == 0 &&
                start != null && // it has to be recursive too
                processDate.after(start)) {

            eLogger.warning(entityId, order.getBaseUserByUserId().getId(),
                    order.getId(), EventLogger.MODULE_BILLING_PROCESS,
                    EventLogger.BILLING_PROCESS_UNBILLED_PERIOD,
                    Constants.TABLE_PUCHASE_ORDER);

            LOG.warn("Order " + order.getId() + " is prepaid " +
                    "but has past time not billed.");
        }

        // initialize the currency of the new invoice 
        if (newInvoice.getCurrency() == null) {
            newInvoice.setCurrency(order.getCurrency());
        } else {
            // now we are not supporting orders with different
            // currencies in the same invoice. Later this could be done
            if (newInvoice.getCurrency().getId() != order.getCurrency().getId()) {
                throw new SessionInternalError("Orders with different " +
                        "currencies not supported in one invoice. " +
                        "Currency = " + newInvoice.getCurrency().getId() +
                        "order = " + order.getId());
            }
        }
        return true;
    }

    static void updateStatusFinished(OrderDTO order,
            Date startOfBillingPeriod,
            Date endOfBillingPeriod)
            throws SessionInternalError {

        // all one timers are done
        if (order.getOrderPeriod().getId() == Constants.ORDER_PERIOD_ONCE) {
            OrderBL orderBL = new OrderBL(order);
            orderBL.setStatus(null, Constants.ORDER_STATUS_FINISHED);
        } else { // recursive orders get more complicated
            // except those that are immortal :)
            if (order.getActiveUntil() == null) {
                return;
            }
            // see until when the incoming process will cover
            // compare if this is after the order exipres
            Logger log = Logger.getLogger(BillingProcessBL.class);
            log.debug("order " + order.getId() + "end of bp " +
                    endOfBillingPeriod + " active until " + order.getActiveUntil());
            if (endOfBillingPeriod.compareTo(Util.truncateDate(order.getActiveUntil())) >= 0) {
                OrderBL orderBL = new OrderBL(order);
                orderBL.setStatus(null, Constants.ORDER_STATUS_FINISHED);
            }
        }
    }

    static public Date getEndOfProcessPeriod(BillingProcessDTO process)
            throws SessionInternalError {
        GregorianCalendar cal = new GregorianCalendar();

        cal.setTime(process.getBillingDate());
        cal.add(MapPeriodToCalendar.map(process.getPeriodUnit().getId()),
                process.getPeriodValue());

        return cal.getTime();
    }

    static public void updateNextBillableDay(OrderDTO order,
            Date end) throws SessionInternalError {
        // if this order won't be process ever again, the 
        // it shouldn't have a next billable day        
        if (order.getStatusId().equals(Constants.ORDER_STATUS_FINISHED)) {
            order.setNextBillableDay(null);
        } else {
            order.setNextBillableDay(end);
        }
    }

    public BillingProcessDTOEx getDtoEx(Integer language) {
        BillingProcessDTOEx retValue = new BillingProcessDTOEx();

        retValue.setBillingDate(billingProcess.getBillingDate());
        retValue.setEntity(billingProcess.getEntity());
        retValue.setId(billingProcess.getId());
        retValue.setPeriodUnit(billingProcess.getPeriodUnit());
        retValue.setPeriodValue(billingProcess.getPeriodValue());
        retValue.setIsReview(billingProcess.getIsReview());


        // now add the runs and grand total
        BillingProcessRunDTOEx grandTotal =
                new BillingProcessRunDTOEx();
        int totalInvoices = 0;
        int runsCounter = 0;
        List<BillingProcessRunDTOEx> runs = new ArrayList<BillingProcessRunDTOEx>();
        // go throuhg every run
        for (Iterator it = billingProcess.getProcessRuns().iterator();
                it.hasNext();) {
            ProcessRunDTO run = (ProcessRunDTO) it.next();
            BillingProcessRunBL runBL = new BillingProcessRunBL(run);
            BillingProcessRunDTOEx runDto = runBL.getDTO(language);
            runs.add(runDto);
            runsCounter++;

            // add statistic for InProgress run proccess in DTO
            if (run.getPaymentFinished() == null) {
                addRuntimeStatistic(run.getBillingProcess().getId(), language, runDto);
            }

            LOG.debug("Run:" + run.getId() + " has " + run.getProcessRunTotals().size() +
                    " total records");
            // go over the totals, since there's one per currency
            for (Iterator it2 = runDto.getTotals().iterator(); it2.hasNext();) {
                // the total to process 
                BillingProcessRunTotalDTOEx totalDto =
                        (BillingProcessRunTotalDTOEx) it2.next();

                BillingProcessRunTotalDTOEx sum = getTotal(totalDto.getCurrency(), grandTotal.getTotals());

                BigDecimal totalTmp = totalDto.getTotalInvoiced().add(sum.getTotalInvoiced());
                sum.setTotalInvoiced(totalTmp);

                totalTmp = totalDto.getTotalPaid().add(sum.getTotalPaid());
                sum.setTotalPaid(totalTmp);

                // can't add up the not paid, because many runs will try to
                // get the same invoices paid, so the not paid field gets
                // duplicated amounts.
                totalTmp = sum.getTotalInvoiced().subtract(sum.getTotalPaid());
                sum.setTotalNotPaid(totalTmp);

                // make sure this total has the currency name initialized
                if (sum.getCurrencyName() == null) {
                    CurrencyBL currency = new CurrencyBL(sum.getCurrency().getId());
                    sum.setCurrencyName(currency.getEntity().getDescription(
                            language));
                }
                // add the payment method totals
                for (Enumeration en = totalDto.getPmTotals().keys();
                        en.hasMoreElements();) {
                    String method = (String) en.nextElement();
                    BigDecimal methodTotal = new BigDecimal(totalDto.getPmTotals().get(method).toString());

                    if (sum.getPmTotals().containsKey(method)) {
                        if (sum.getPmTotals().get(method) != null) {
                            methodTotal = methodTotal.add(new BigDecimal(((Float) sum.getPmTotals().
                                    get(method)).toString()));

                        }
                    }
                    sum.getPmTotals().put(method, new Float(methodTotal.floatValue()));
                }

                LOG.debug("Added total to run dto. PMs in total:" + sum.getPmTotals().size() + " now grandTotal totals:" + grandTotal.getTotals().size());
            }
            totalInvoices += runDto.getInvoicesGenerated();
        }

        grandTotal.setInvoicesGenerated(new Integer(totalInvoices));

        retValue.setRetries(new Integer(runsCounter));
        retValue.setRuns(runs);
        retValue.setGrandTotal(grandTotal);
        retValue.setBillingDateEnd(getEndOfProcessPeriod(billingProcess));
        retValue.setOrdersProcessed(new Integer(billingProcess.getOrderProcesses().size()));

        return retValue;
    }

    private void addRuntimeStatistic(Integer billingProcessId, Integer language,  BillingProcessRunDTOEx runDto) {
        for (Iterator iter = new BillingProcessDAS().getCountAndSum(billingProcessId); iter.hasNext();) {
            Object[] row = (Object[]) iter.next();

            BillingProcessRunTotalDTOEx totalRowDto =
                    new BillingProcessRunTotalDTOEx();
            totalRowDto.setProcessRun(runDto);
            totalRowDto.setCurrency(new CurrencyDAS().find((Integer) row[2]));
            totalRowDto.setCurrencyName(totalRowDto.getCurrency().getDescription(language));
            totalRowDto.setId(-1);
            totalRowDto.setTotalInvoiced((BigDecimal) row[1]);
            totalRowDto.setTotalNotPaid(BigDecimal.ZERO);
            totalRowDto.setTotalPaid(BigDecimal.ZERO);

            // now go over the totals by payment method
            Hashtable totals = new Hashtable();
            for (Iterator itt = new BillingProcessDAS().getSuccessfulProcessCurrencyMethodAndSum(billingProcessId); itt.hasNext();) {
                Object[] payedRow = (Object[]) itt.next();
                if (payedRow[0].equals(totalRowDto.getCurrency().getId())) {
                    PaymentMethodDTO paymentMethod = new PaymentMethodDAS().find((Integer) payedRow[1]);
                    BigDecimal payed = (BigDecimal) payedRow[2];
                    totals.put(paymentMethod.getDescription(language), payed);
                    totalRowDto.setTotalPaid(totalRowDto.getTotalPaid().add(payed));
                }
            }
            totalRowDto.setPmTotals(totals);
            for (Iterator itt = new BillingProcessDAS().getFailedProcessCurrencyAndSum(billingProcessId); itt.hasNext();) {
                Object[] unpayedRow = (Object[]) itt.next();
                if (unpayedRow[0].equals(totalRowDto.getCurrency().getId())) {
                    totalRowDto.setTotalNotPaid(totalRowDto.getTotalNotPaid().add((BigDecimal) unpayedRow[1]));
                }
            }

            runDto.setInvoicesGenerated(runDto.getInvoicesGenerated() + ((Long) row[0]).intValue());
            runDto.getTotals().add(totalRowDto);
        }
    }

    public CachedRowSet getList(Integer entityId)
            throws SQLException, Exception {
        prepareStatement(ProcessSQL.generalList);
        cachedResults.setInt(1, entityId.intValue());
        execute();
        conn.close();
        return cachedResults;

    }

    public int getLast(Integer entityId)
            throws SQLException, Exception {
        int retValue = -1;
        prepareStatement(ProcessSQL.lastId);
        cachedResults.setInt(1, entityId.intValue());
        execute();
        conn.close();

        if (cachedResults.next()) {
            retValue = cachedResults.getInt(1);
        }

        return retValue;
    }

    public Integer[] getToRetry(Integer entityId)
            throws SQLException, Exception {
        List list = new ArrayList();

        prepareStatement(ProcessSQL.findToRetry);
        cachedResults.setInt(1, entityId.intValue());
        execute();
        conn.close();

        while (cachedResults.next()) {
            list.add(new Integer(cachedResults.getInt(1)));
        }

        Integer retValue[] = new Integer[list.size()];
        list.toArray(retValue);

        return retValue;
    }

    /**
     * Tries to get paid the invoice of the parameter.
     * The processs Id and runId are only to update the run totals.
     * Only one of them is required. The runId should be passed if
     * this is a retry, otherwise the processId.
     * @param processId
     * @param runId
     * @param invoiceId
     * @throws SessionInternalError
     */
    public void generatePayment(Integer processId, Integer runId, Integer invoiceId) throws SessionInternalError {

        try {
            InvoiceBL invoiceBL = new InvoiceBL(invoiceId);
            InvoiceDTO newInvoice = invoiceBL.getEntity();
            IPaymentSessionBean paymentSess = (IPaymentSessionBean) Context.getBean(Context.Name.PAYMENT_SESSION);
            paymentSess.generatePayment(newInvoice);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public BillingProcessRunTotalDTOEx getTotal(CurrencyDTO currency, List totals) {
        BillingProcessRunTotalDTOEx retValue = null;
        for (int f = 0; f < totals.size(); f++) {
            BillingProcessRunTotalDTOEx total = (BillingProcessRunTotalDTOEx) totals.get(f);
            if (total.getCurrency().equals(currency)) {
                retValue = total;
                break;
            }
        }

        // it is looking for a total that doesn't exist
        if (retValue == null) {
            CurrencyDAS curDas = new CurrencyDAS();
            CurrencyDTO curDto = curDas.find(currency.getId());
            retValue = new BillingProcessRunTotalDTOEx(null, curDto, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            totals.add(retValue);
        }

        return retValue;
    }

    public BillingProcessDTOEx getReviewDTO(Integer entityId, Integer languageId) {
        billingProcess = billingProcessDas.findReview(entityId);
        if (billingProcess == null) {
            System.out.println("Don't found the billingProcess");
            return null;
        } else {
            System.out.println("found the billingProcess");
        }
        return getDtoEx(languageId);
    }

    public boolean isReviewPresent(Integer entityId) {
        return billingProcessDas.findReview(entityId) != null;
    }

    public void purgeReview(Integer entityId, boolean isReview) {
        BillingProcessDTO review = billingProcessDas.findReview(
                entityId);
        if (review == null) {
            // no review, nothing to delete then
            return;
        }

        // if we are here, a review exists
        ConfigurationBL configBL = new ConfigurationBL(entityId);
        if (configBL.getEntity().getGenerateReport().intValue() == 1 &&
                !new Integer(configBL.getEntity().getReviewStatus()).equals(
                Constants.REVIEW_STATUS_APPROVED) && !isReview) {
            eLogger.warning(entityId, null, configBL.getEntity().getId(),
                    EventLogger.MODULE_BILLING_PROCESS,
                    EventLogger.BILLING_REVIEW_NOT_APPROVED,
                    Constants.TABLE_BILLING_PROCESS_CONFIGURATION);
        }
        // delete the review
        LOG.debug("Removing review id = " + review.getId() + " from " +
                " entity " + entityId);
        // this is needed while the order process is JPA, but the billing process is Entity
        OrderProcessDAS processDas = new OrderProcessDAS();
        com.sapienter.jbilling.server.process.db.BillingProcessDTO processDto = new BillingProcessDAS().find(review.getId());
        for (OrderProcessDTO orderDto : processDto.getOrderProcesses()) {
            processDas.delete(orderDto);
        }
        processDto.getOrderProcesses().clear();
        // delete processRunUsers otherwise will be constraint violation
        for (ProcessRunDTO processRun : review.getProcessRuns()) {
            new ProcessRunUserDAS().removeProcessRunUsersForProcessRun(processRun.getId());
        }
        // otherwise this line would cascade de delete
        getHome().delete(review);
    }

    @SuppressWarnings("unchecked")
    private void processOrderToInvoiceEvents(NewInvoiceDTO newInvoice, Integer entityId) {
        List<OrderDTO> orders = newInvoice.getOrders();
        List<List<PeriodOfTime>> periods = newInvoice.getPeriods();
        for (int i = 0; i < orders.size(); i++) {
            OrderDTO order = orders.get(i);
            Integer userId = findUserId(order);
            for (PeriodOfTime period : periods.get(i)) {
                OrderToInvoiceEvent newEvent =
                        new OrderToInvoiceEvent(entityId, userId, order);
                newEvent.setStart(period.getStart());
                newEvent.setEnd(period.getEnd());
                EventManager.process(newEvent);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processOrderAddedOnInvoiceEvents(NewInvoiceDTO newInvoice, Integer entityId) {
        List<OrderDTO> orders = newInvoice.getOrders();
        List<List<PeriodOfTime>> periods = newInvoice.getPeriods();
        for (int i = 0; i < orders.size(); i++) {
            OrderDTO order = orders.get(i);
            Integer userId = findUserId(order);
            for (PeriodOfTime period : periods.get(i)) {
                LOG.info("Number of orders in map: " + newInvoice.getOrderTotalContributions().size());
                LOG.info("Map: " + newInvoice.getOrderTotalContributions());
                OrderAddedOnInvoiceEvent newEvent =
                        new OrderAddedOnInvoiceEvent(entityId, userId, order, 
                        newInvoice.getOrderTotalContributions().get(
                        order.getId()));
                newEvent.setStart(period.getStart());
                newEvent.setEnd(period.getEnd());
                EventManager.process(newEvent);
            }
        }
    }

    private Integer findUserId(OrderDTO order) {
        UserDTO user = order.getUser();

        // while this user has a parent and the flag is off, keep looking
        while(user.getCustomer().getParent() != null &&
                (user.getCustomer().getInvoiceChild() == null ||
                 user.getCustomer().getInvoiceChild() == 0)) {
            // go up one level
            LOG.debug("finding parent for invoicing. Now " + user.getUserId());
            user = user.getCustomer().getParent().getBaseUser();
        }

        return user.getUserId();
    }

    /**
     * Convert a given BillingProcessDTO into a BillingProcessWS web-service object.
     *
     * @param dto dto to convert
     * @return converted web-service object
     */
    public static BillingProcessWS getWS(BillingProcessDTO dto) {
        return dto != null ? new BillingProcessWS(dto) : null;
    }


    /**
     * Convert a given BillingProcessDTOEx into a BillingProcessWS web-service object.
     *
     * @param ex extended DTO to convert
     * @return converted web-service object
     */
    public static BillingProcessWS getWS(BillingProcessDTOEx ex) {
        return ex != null ? new BillingProcessWS(ex) : null;
    }
}
