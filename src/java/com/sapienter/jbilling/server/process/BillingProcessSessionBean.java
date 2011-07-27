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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import org.hibernate.ScrollableResults;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.PaperInvoiceBatchBL;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.payment.event.EndProcessPaymentEvent;
import com.sapienter.jbilling.server.payment.event.ProcessPaymentEvent;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.db.*;
import com.sapienter.jbilling.server.process.event.NoNewInvoiceEvent;
import com.sapienter.jbilling.server.process.task.BasicBillingProcessFilterTask;
import com.sapienter.jbilling.server.process.task.IBillingProcessFilterTask;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.audit.EventLogger;

/**
 *
 * This is the session facade for the all the billing process and its 
 * related services. 
 */
@Transactional( propagation = Propagation.REQUIRED )
public class BillingProcessSessionBean implements IBillingProcessSessionBean {

    private static final Logger LOG = Logger.getLogger(BillingProcessSessionBean.class);

    private static final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Gets the invoices for the specified process id. The returned collection
     * is of extended dtos (InvoiceDTO).
     * @param processId
     * @return A collection of InvoiceDTO objects
     * @throws SessionInternalError
     */
    public Collection getGeneratedInvoices(Integer processId) {
        // find the billing_process home interface
        BillingProcessDAS processHome = new BillingProcessDAS();
        Collection<InvoiceDTO> invoices =  new InvoiceDAS().findByProcess(processHome.find(processId));
        
        for (InvoiceDTO invoice : invoices) {
            invoice.getOrderProcesses().iterator().next().getId(); // it is a touch
        }

        return invoices;

        
    }
    
    /**
     * @param entityId
     * @param languageId
     * @return
     * @throws SessionInternalError
     */
    public AgeingDTOEx[] getAgeingSteps(Integer entityId, 
            Integer executorLanguageId, Integer languageId) 
            throws SessionInternalError {
        try {
            AgeingBL ageing = new AgeingBL();
            return ageing.getSteps(entityId, executorLanguageId, languageId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    
    /**
     * @param entityId
     * @param languageId
     * @param steps
     * @throws SessionInternalError
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void setAgeingSteps(Integer entityId, Integer languageId, 
            AgeingDTOEx[] steps) 
            throws SessionInternalError {
        try {
            AgeingBL ageing = new AgeingBL();
            ageing.setSteps(entityId, languageId, steps);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void generateReview(Integer entityId, Date billingDate,
            Integer periodType, Integer periodValue)
            throws SessionInternalError {
        LOG.debug("Generating review entity " + entityId);
        IBillingProcessSessionBean local = (IBillingProcessSessionBean) 
                Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        local.processEntity(entityId, billingDate, periodType, 
                periodValue, true);
        // let know this entity that a new reivew is now pending approval
        try {
            String params[] = new String[1];
            params[0] = entityId.toString();
            NotificationBL.sendSapienterEmail(entityId, "process.new_review", 
                    null, params);
        } catch (Exception e) {
            LOG.warn("Exception sending email to entity", e);
        }
    }

    /**
     * Creates the billing process record. This has to be done in its own
     * transaction (thus, in its own method), so new invoices can link to
     * an existing process record in the db.
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public Integer createProcessRecord(Integer entityId, Date billingDate,
            Integer periodType, Integer periodValue, boolean isReview,
            Integer retries) 
            throws  SQLException {
        BillingProcessBL bpBL = new BillingProcessBL();
        BillingProcessDTO dto = new BillingProcessDTO();

        // process can't leave reviews behind, and a review has to 
        // delete the previous one too            
        bpBL.purgeReview(entityId, isReview);
        
        //I need to find the entity
        CompanyDAS comDas = new CompanyDAS();
        CompanyDTO company = comDas.find(entityId);
        //I need to find the PeriodUnit
        PeriodUnitDAS periodDas = new PeriodUnitDAS();
        PeriodUnitDTO period = periodDas.find(periodType);
        
        dto.setEntity(company);
        dto.setBillingDate(Util.truncateDate(billingDate));
        dto.setPeriodUnit(period);
        dto.setPeriodValue(periodValue);
        dto.setIsReview(isReview ? new Integer(1) : new Integer(0));
        dto.setRetriesToDo(retries);
        
        bpBL.findOrCreate(dto);
        return bpBL.getEntity().getId();
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public Integer createRetryRun(Integer processId) {
        BillingProcessBL process = new BillingProcessBL(processId);
        // create a new run record
        BillingProcessRunBL runBL = new BillingProcessRunBL();
        runBL.create(process.getEntity(), process.getEntity().getBillingDate());
        LOG.debug("created process run " + runBL.getEntity().getId());

        return runBL.getEntity().getId();
    }
    
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void processEntity(Integer entityId, Date billingDate, Integer periodType, Integer periodValue,
                              boolean isReview) throws SessionInternalError {
            
        if (entityId == null || billingDate == null) {
            throw new SessionInternalError("entityId and billingDate can't be null");
        }

        try {
            ConfigurationBL conf = new ConfigurationBL(entityId);

            IBillingProcessSessionBean local
                = (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
            
            Integer billingProcessId = local.createProcessRecord(
                    entityId, billingDate, periodType, periodValue, isReview,
                    conf.getEntity().getRetries());

            BillingProcessRunBL billingProcessRunBL = new BillingProcessRunBL();
            billingProcessRunBL.setProcess(billingProcessId);
            // TODO: all the customer's id in memory is not a good idea. 1M customers would be 4MB of memory
            List<Integer> successfullUsers = billingProcessRunBL.findSuccessfullUsers();
            
            // start processing users of this entity
            int totalInvoices = 0;
            
            boolean onlyRecurring;
            // find out parameters from the configuration
            onlyRecurring = conf.getEntity().getOnlyRecurring() == 1;
            LOG.debug("**** ENTITY " + entityId + " PROCESSING USERS");

            //Load the pluggable task for filtering the users
            PluggableTaskManager taskManager = new PluggableTaskManager(entityId,
                                                                        Constants.PLUGGABLE_TASK_BILL_PROCESS_FILTER);

            IBillingProcessFilterTask task = (IBillingProcessFilterTask) taskManager.getNextClass();

            // If one was not configured just use the basic task by default
            if (task == null) {
                task = new BasicBillingProcessFilterTask();
            }

            BillingProcessDAS bpDas = new BillingProcessDAS();

            int usersFailed = 0;
            ScrollableResults userCursor = task.findUsersToProcess(entityId, billingDate);
            if (userCursor!= null){
                int count = 0;
                while (userCursor.next()) {
                    Integer userId = (Integer) userCursor.get(0);
                    if(successfullUsers.contains(userId)) { // TODO: change this by a query to the DB
                        LOG.debug("User #" + userId + " was successfully processed during previous run. Skipping.");
                        continue;
                    }

                    Integer result[] = null;
                    try {
                        result = local.processUser(billingProcessId, userId,
                                isReview, onlyRecurring);
                    } catch(Throwable ex) {
                        LOG.error("Exception was caught when processing User #" + userId + ". Continue process skipping user    .", ex);
                        local.addProcessRunUser(billingProcessId, userId, ProcessRunUserDTO.STATUS_FAILED);
                    }
                    if (result != null) {
                        LOG.debug("User " + userId + " done invoice generation.");
                        if (!isReview) {
                            for (int f = 0; f < result.length; f++) {
                                local.emailAndPayment(entityId, result[f], 
                                        billingProcessId,  
                                        conf.getEntity().getAutoPayment().intValue() == 1);
                            }
                            LOG.debug("User " + userId + " done email & payment.");
                        }
                        totalInvoices += result.length;
                        local.addProcessRunUser(billingProcessId, userId, ProcessRunUserDTO.STATUS_SUCCEEDED);
                    } else {
                        LOG.debug("User " + userId + " NOT done");
                        local.addProcessRunUser(billingProcessId, userId, ProcessRunUserDTO.STATUS_FAILED);

                        ++usersFailed;
                    }
    
                    // make sure the memory doesn't get flooded
                    if ( ++count % Constants.HIBERNATE_BATCH_SIZE == 0) {
                        bpDas.reset();
                    }
                }
                userCursor.close(); // done with the cursor, needs manual closing
            }
            // restore the configuration in the session, the reset removed it
            conf.set(entityId);
            
            if (usersFailed == 0) { // only if all got well processed
                // if some of the invoices were paper invoices, a new file with all
                // of them has to be generated
                try {
                    BillingProcessBL process = new BillingProcessBL(billingProcessId);
                    PaperInvoiceBatchDTO batch = process.getEntity().getPaperInvoiceBatch(); 
                    if (totalInvoices > 0 && batch != null) {
                        PaperInvoiceBatchBL batchBl = new PaperInvoiceBatchBL(batch);
                        batchBl.compileInvoiceFilesForProcess(entityId);
                        
                        // send the file as an attachment 
                        batchBl.sendEmail();
                    }
                } catch (Exception e) {
                    LOG.error("Error generetaing batch file", e);
                }
                // now update the billing proces record 
            }

            if (usersFailed == 0) {
                Integer processRunId = local.updateProcessRunFinished(
                        billingProcessId, Constants.PROCESS_RUN_STATUS_SUCCESS);                

                if (!isReview) {
                    // the payment processing is happening in parallel
                    // this event marks the end of it
                    EndProcessPaymentEvent event = new EndProcessPaymentEvent(processRunId, entityId);
                    EventManager.process(event);
                    // and finally the next run date in the config
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(billingDate);
                    cal.add(MapPeriodToCalendar.map(periodType), periodValue.intValue());
                    conf.getEntity().setNextRunDate(cal.getTime());
                    LOG.debug("Updated run date to " + cal.getTime());
                }
            } else {
                local.updateProcessRunFinished(
                        billingProcessId, Constants.PROCESS_RUN_STATUS_FAILED);
                billingProcessRunBL.notifyProcessRunFailure(entityId, usersFailed);
                
                // TODO: check, if updating totals needed
                // TODO: in the case of errors during users processing
                BillingProcessRunBL runBL = new BillingProcessRunBL();
                runBL.setProcess(billingProcessId);
                // update the totals
                runBL.updateTotals(billingProcessId);
            }

            LOG.debug("**** ENTITY " + entityId + " DONE. Failed users = " + usersFailed);
            // TODO: review that this is not needed: EventManager.process(generatedEvent);
        } catch (Exception e) {
            // no need to specify a rollback, an error in any of the
            // updates would not require the rest to be rolled back.
            // Actually, it's better to keep as far as it went.
            LOG.error("Error processing entity " + entityId, e);
        } 
    }
    
    /**
     * This method process a payment synchronously. It is a wrapper to the payment processing  
     * so it runs in its own transaction
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void processPayment(Integer processId, Integer runId, Integer invoiceId) {
        try {
            BillingProcessBL bl = new BillingProcessBL();
            bl.generatePayment(processId, runId, invoiceId);
        } catch (Exception e) {
            LOG.error("Exception processing a payment ", e);
        }
    }

    /**
     * This method marks the end of payment processing. It is a wrapper
     * so it runs in its own transaction
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void endPayments(Integer runId) {
        BillingProcessRunBL run = new BillingProcessRunBL(runId);
        run.updatePaymentsFinished();
        // update the totals
        run.updateTotals(run.getEntity().getBillingProcess().getId());
        run.updatePaymentsStatistic(run.getEntity().getId());
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public boolean verifyIsRetry(Integer processId, int retryDays, Date today) {
        GregorianCalendar cal = new GregorianCalendar();
        // find the last run date
        BillingProcessBL process = new BillingProcessBL(processId);
        BillingProcessRunBL runBL = new BillingProcessRunBL();
        ProcessRunDTO lastRun = (ProcessRunDTO) 
                Collections.max(process.getEntity().getProcessRuns(),
                    runBL.new DateComparator());
        cal.setTime(Util.truncateDate(lastRun.getStarted()));
        LOG.debug("Retry evaluation lastrun = " + cal.getTime());
        cal.add(GregorianCalendar.DAY_OF_MONTH, retryDays);
        LOG.debug("Added days = " + cal.getTime() + " today = " + today);
        if (!cal.getTime().after(today)) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void doRetry(Integer processId, int retryDays, Date today) 
            throws SessionInternalError {
        try {
            IBillingProcessSessionBean process = (IBillingProcessSessionBean)
                    Context.getBean(Context.Name.BILLING_PROCESS_SESSION);

            if (process.verifyIsRetry(processId, retryDays, today)) {
                // it's time for a retry
                LOG.debug("Retring process " + processId);
                Integer runId = process.createRetryRun(processId); 
                Integer entityId = new BillingProcessDAS().find(processId).getEntity().getId();

                // get the invoices yet to be paid from this process
                InvoiceBL invoiceBL = new InvoiceBL();
                for (Iterator it = invoiceBL.getHome().findProccesableByProcess(
                        processId).iterator(); it.hasNext();) {
                    InvoiceDTO invoice = (InvoiceDTO) it.next();
                    LOG.debug("Retrying invoice " + invoice.getId());

                    // post the need of a payment process, it'll be done asynchronusly
                    ProcessPaymentEvent event = new ProcessPaymentEvent(invoice.getId(), 
                            null, runId, entityId);
                    EventManager.process(event);
                }

                // update the end date of this run
                BillingProcessRunBL runBl = new BillingProcessRunBL(runId);
                runBl.updateFinished(Constants.PROCESS_RUN_STATUS_SUCCESS);

                // the payment processing is happening in parallel
                // this event marks the end of it
                EndProcessPaymentEvent event = new EndProcessPaymentEvent(runId, entityId);
                EventManager.process(event);

                
                // update the process: one less retry to do
                BillingProcessBL bl = new BillingProcessBL(processId);
                int now = bl.getEntity().getRetriesToDo();
                now--;
                bl.getEntity().setRetriesToDo(new Integer(now));
            }
            
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void emailAndPayment(Integer entityId, Integer invoiceId, Integer processId, boolean processPayment) {
        try {
            InvoiceBL invoice = new InvoiceBL(invoiceId);
            Integer userId = invoice.getEntity().getBaseUser().getUserId();
 
            LOG.debug("email and payment for user " + userId + " invoice " + invoiceId);

            // last but not least, let this user know about his/her new
            // invoice.
            NotificationBL notif = new NotificationBL();
            
            try {
                MessageDTO[] invoiceMessage = notif.getInvoiceMessages(entityId,
                                                                       processId,
                                                                       invoice.getEntity().getBaseUser().getLanguageIdField(),
                                                                       invoice.getEntity());

                INotificationSessionBean notificationSess = (INotificationSessionBean)
                        Context.getBean(Context.Name.NOTIFICATION_SESSION);

                for (int msg = 0; msg < invoiceMessage.length; msg++) {
                    notificationSess.notify(userId, invoiceMessage[msg]);
                }
            } catch (NotificationNotFoundException e) {
                LOG.warn("Invoice message not defined for entity " + entityId + " Invoice email not sent");
            }
            
            if (processPayment) {
                // when the preference is set, 
                // only process payment if it doesn't have a negative balance 
                // that wasn't caused by a carried balance
                InvoiceDTO dto = invoice.getDTO();
                if (BigDecimal.ZERO.compareTo(dto.getBalance()) > 0
                        && BigDecimal.ZERO.compareTo(dto.getCarriedBalance()) <= 0) {

                    PreferenceBL preferenceBL = new PreferenceBL();
                    try {
                        preferenceBL.set(entityId, Constants.PREFERENCE_DELAY_NEGATIVE_PAYMENTS);
                    } catch (EmptyResultDataAccessException fe) { /* use default */ }

                    if (preferenceBL.getInt() == 1) {
                        processPayment = false;
                        LOG.warn("Delaying invoice payment with negative balance and no negative carried balance");
                    }
                }

                if (processPayment && BigDecimal.ZERO.compareTo(dto.getBalance()) != 0) {
                    ProcessPaymentEvent event = new ProcessPaymentEvent(invoiceId, processId, null, entityId);
                    EventManager.process(event);
                } else {
                    LOG.debug("Not processing a payment, balance of invoice is " + dto.getBalance());
                }
            }
        } catch (Exception e) {
            LOG.error("sending email and processing payment", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } 
    }
        

    /**
     * Process a user, generating the invoice/s,
     * @param userId
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public Integer[] processUser(Integer processId, Integer userId, boolean isReview, boolean onlyRecurring) {
        int invoiceGenerated = 0;
        Integer[] retValue = null;

        try {
            UserBL user = new UserBL(userId);
            
            if (!user.canInvoice()) {
                LOG.debug("Skipping non-customer / subaccount user " + userId);
                return new Integer[0];
            }

            BillingProcessBL processBL = new BillingProcessBL(processId);
            BillingProcessDTO process = processBL.getEntity();
            
            // payment and notification only needed if this user gets a 
            // new invoice.
            InvoiceDTO newInvoices[] = processBL.generateInvoice(process, user.getEntity(), isReview, onlyRecurring);
            if (newInvoices == null) {
                if (!isReview) {
                    NoNewInvoiceEvent event = new NoNewInvoiceEvent(
                            user.getEntityId(userId), userId, 
                            process.getBillingDate(), 
                            user.getEntity().getSubscriberStatus().getId());
                    EventManager.process(event);
                }
                return new Integer[0];
            }

            retValue = new Integer[newInvoices.length];
            for (int f = 0; f < newInvoices.length; f++) {
                retValue[f] = newInvoices[f].getId();
                invoiceGenerated++;
            }
            LOG.info("The user " + userId + " has been processed." + invoiceGenerated);

        } catch (Throwable e) {
            LOG.error("Exception caught when processing the user " + userId, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // rollback !
            return null; // the user was not processed
        }

        return retValue;
    }

    public BillingProcessDTOEx getDto(Integer processId, Integer languageId) {
        BillingProcessDTOEx retValue = null;
        
        BillingProcessBL process = new BillingProcessBL(processId);
        retValue = process.getDtoEx(languageId);
        if (retValue != null) retValue.toString(); // as a form of touch
           
        return retValue;            
    }

    public BillingProcessConfigurationDTO getConfigurationDto(Integer entityId) 
            throws SessionInternalError {
        BillingProcessConfigurationDTO retValue = null;
        
        try {
            ConfigurationBL config = new ConfigurationBL(entityId);
            retValue = config.getDTO();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }            
           
        return retValue;            
    }

    public Integer createUpdateConfiguration(Integer executorId,
            BillingProcessConfigurationDTO dto) 
            throws SessionInternalError {
        Integer retValue;
        
        try {
            LOG.debug("Updating configuration " + dto);
            ConfigurationBL config = new ConfigurationBL();
            retValue = config.createUpdate(executorId, dto);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }            
           
        return retValue;            
    }

    public Integer getLast(Integer entityId) 
            throws SessionInternalError {
        int retValue;
        
        try {
            BillingProcessBL process = new BillingProcessBL();
            retValue = process.getLast(entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }            
        
        return retValue > 0 ? new Integer(retValue) : null;
    }  

    public BillingProcessDTOEx getReviewDto(Integer entityId, Integer languageId) {
        BillingProcessDTOEx dto = null;
        BillingProcessBL process = new BillingProcessBL();
        dto = process.getReviewDTO(entityId, languageId);
        if (dto != null) dto.toString(); // as a touch
        
        return dto;           
    }    

    public BillingProcessConfigurationDTO setReviewApproval(
            Integer executorId, Integer entityId, 
            Boolean flag) throws SessionInternalError {
        try {
            LOG.debug("Setting review approval : " + flag);
            ConfigurationBL config = new ConfigurationBL(entityId);
            config.setReviewApproval(executorId, flag.booleanValue());
            return getConfigurationDto(entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public boolean trigger(Date pToday) throws SessionInternalError {

     if (!running.compareAndSet(false, true)) {
         LOG.warn("Failed to trigger billing process at " + pToday.getTime()
                     + ", another process is already running.");
         return false;
     }

        try {
            Date today = Util.truncateDate(pToday);
            EventLogger eLogger = EventLogger.getInstance();
            BillingProcessBL processBL = new BillingProcessBL();
            GregorianCalendar cal = new GregorianCalendar();  

            IBillingProcessSessionBean local
                = (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);

            // loop over all the entities
            EntityBL entityBL = new EntityBL();
            Integer entityArray[] = entityBL.getAllIDs();
            LOG.debug("Running trigger. Today = " + today + "[" + today.getTime() + "] entities = " + entityArray.length);

            for (int entityIndex = 0; entityIndex < entityArray.length; entityIndex++) {
                Integer entityId = entityArray[entityIndex];
                LOG.debug("New entity row index " + entityIndex + " of " + entityArray.length);
                LOG.debug("Processing (1) entity " + entityId + " total = " + entityArray.length);

                // now process this entity
                ConfigurationBL configEntity = new ConfigurationBL(entityId);
                BillingProcessConfigurationDTO config = configEntity.getDTO();
                if (!config.getNextRunDate().after(today)) {
                    // there should be a run today 
                    boolean doRun = true;
                    LOG.debug("A process has to be done for entity " + entityId);
                    // check that: the configuration requires a review
                    // AND, there is no partial run already there (failed)
                    if (config.getGenerateReport() == 1
                        && new BillingProcessDAS().isPresent(entityId, 0, config.getNextRunDate()) == null) {

                        // a review had to be done for the run to go ahead
                        boolean reviewPresent = processBL.isReviewPresent(entityId); 
                        if (!reviewPresent) {  // review wasn't generated
                            LOG.warn("Review is required but not present for " + "entity " + entityId);
                            eLogger.warning(entityId, null, config.getId(), 
                                            EventLogger.MODULE_BILLING_PROCESS,
                                            EventLogger.BILLING_REVIEW_NOT_GENERATED,
                                            Constants.TABLE_BILLING_PROCESS_CONFIGURATION);
                            
                            generateReview(entityId,
                                           config.getNextRunDate(),
                                           config.getPeriodUnit().getId(),
                                           config.getPeriodValue());

                            doRun = false;

                        } else if (new Integer(config.getReviewStatus()).equals(Constants.REVIEW_STATUS_GENERATED)) {
                            // the review has to be reviewd yet
                            GregorianCalendar now = new GregorianCalendar();
                            LOG.warn("Review is required but is not approved. Entity " + entityId
                                + " hour is " + now.get(GregorianCalendar.HOUR_OF_DAY));

                            eLogger.warning(entityId, null, config.getId(), 
                                            EventLogger.MODULE_BILLING_PROCESS,
                                            EventLogger.BILLING_REVIEW_NOT_APPROVED,
                                            Constants.TABLE_BILLING_PROCESS_CONFIGURATION);

                            try {
                                // only once per day please
                                if (now.get(GregorianCalendar.HOUR_OF_DAY) < 1) {
                                    String params[] = new String[1];
                                    params[0] = entityId.toString();
                                    NotificationBL.sendSapienterEmail(entityId, "process.review_waiting", null, params);
                                }
                            } catch (Exception e) {
                                LOG.warn("Exception sending an entity email", e);
                            }
                            doRun = false;

                        } else if (new Integer(config.getReviewStatus()).equals(Constants.REVIEW_STATUS_DISAPPROVED)) {
                            // is has been disapproved, let's regenerate
                            LOG.debug("The process should run, but the review has been disapproved");
                            generateReview(entityId,
                                           config.getNextRunDate(),
                                           config.getPeriodUnit().getId(),
                                           config.getPeriodValue());

                            doRun = false;
                        }
                    }
                    
                    // do the run
                    if (doRun) {
                        local.processEntity(entityId,
                                            config.getNextRunDate(), 
                                            config.getPeriodUnit().getId(),
                                            config.getPeriodValue(),
                                            false);
                    }

                } else {
                    // no run, may be then a review generation
                    LOG.debug("No run scheduled. Next run on " + config.getNextRunDate().getTime());
                    
                    /*
                     * Review generation
                     */
                    if (config.getGenerateReport() == 1) {
                        cal.setTime(config.getNextRunDate());
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -config.getDaysForReport().intValue());
                        if (!cal.getTime().after(today)) {
                            boolean reviewPresent = processBL.isReviewPresent(entityId);
                            if (reviewPresent && !Constants.REVIEW_STATUS_DISAPPROVED.equals(config.getReviewStatus())) {
                                // there's already a review there, and it's been
                                // either approved or not yet reviewed
                            } else {
                                LOG.debug("Review disapproved. Regeneratting.");
                                generateReview(entityId,
                                               config.getNextRunDate(),
                                               config.getPeriodUnit().getId(),
                                               config.getPeriodValue());
                            }
                        }
                    }
                } // else (no run)
                
                /*
                 * Retries, only if automatic payment is set
                 */
                if (config.getAutoPayment() == 1) {
                    // get the last process
                    Integer[] processToRetry = processBL.getToRetry(entityId);
                    for (Integer aProcessToRetry : processToRetry) {
                        local.doRetry(aProcessToRetry, config.getDaysForRetry(), today);
                    }
                }

            } // for all entities
        } catch (Exception e) {
            throw new SessionInternalError(e);
        } finally {
            running.set(false);
        }
        return true;
    }

    /**
     * @return the id of the invoice generated
     */
    public InvoiceDTO generateInvoice(Integer orderId, Integer invoiceId, Integer languageId)
            throws SessionInternalError {
        
        try {
            BillingProcessBL process = new BillingProcessBL();
            InvoiceDTO invoice = process.generateInvoice(orderId, invoiceId);
            invoice.touch();
            
            return invoice;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        } 
    }
    
    public void reviewUsersStatus(Integer entityId, Date today)
            throws SessionInternalError {
        try {
            AgeingBL age = new AgeingBL();
            age.reviewAll(entityId, today);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * Update status of BillingProcessRun in new transaction
     * for accessing updated entity from other thread
     * @param billingProcessId id of billing process for searching ProcessRun
     * @return id of updated ProcessRunDTO
     */
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public Integer updateProcessRunFinished(Integer billingProcessId, Integer processRunStatusId) {
        BillingProcessRunBL runBL = new BillingProcessRunBL();
        runBL.setProcess(billingProcessId);
        runBL.updateFinished(processRunStatusId);
        return runBL.getEntity().getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer addProcessRunUser(Integer billingProcessId, Integer userId, Integer status) {
        BillingProcessRunBL runBL = new BillingProcessRunBL();
        runBL.setProcess(billingProcessId);
        return runBL.addProcessRunUser(userId, status).getId();
    }

    /**
     * Returns true if the Billing Process is running.
     */
    public boolean isBillingRunning() {
    	return running.get();
    }
}
