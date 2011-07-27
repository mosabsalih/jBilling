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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;

/**
 *
 * This is the session facade for the all the billing process and its 
 * related services. 
 */
public interface IBillingProcessSessionBean {

    /**
     * Gets the invoices for the specified process id. The returned collection
     * is of extended dtos (InvoiceDTO).
     * @param processId
     * @return A collection of InvoiceDTO objects
     * @throws SessionInternalError
     */
    public Collection getGeneratedInvoices(Integer processId);
    
    /**
     * @param entityId
     * @param languageId
     * @return
     * @throws SessionInternalError
     */
    public AgeingDTOEx[] getAgeingSteps(Integer entityId, 
            Integer executorLanguageId, Integer languageId) 
            throws SessionInternalError;
    
    /**
     * @param entityId
     * @param languageId
     * @param steps
     * @throws SessionInternalError
     */
    public void setAgeingSteps(Integer entityId, Integer languageId, 
            AgeingDTOEx[] steps) throws SessionInternalError;

    public void generateReview(Integer entityId, Date billingDate,
            Integer periodType, Integer periodValue)
            throws SessionInternalError;

    /**
     * Creates the billing process record. This has to be done in its own
     * transaction (thus, in its own method), so new invoices can link to
     * an existing process record in the db.
     */
    public Integer createProcessRecord(Integer entityId, Date billingDate,
            Integer periodType, Integer periodValue, boolean isReview,
            Integer retries) throws  SQLException;

    public Integer createRetryRun(Integer processId);
    
    public void processEntity(Integer entityId, Date billingDate,
            Integer periodType, Integer periodValue, boolean isReview)
            throws SessionInternalError;

    /**
     * This method process a payment synchronously. It is a wrapper to the payment processing  
     * so it runs in its own transaction
     */
    public void processPayment(Integer processId, Integer runId, 
            Integer invoiceId);

    /**
     * This method marks the end of payment processing. It is a wrapper
     * so it runs in its own transaction
     */
    public void endPayments(Integer runId);

    public boolean verifyIsRetry(Integer processId, int retryDays, Date today);

    public void doRetry(Integer processId, int retryDays, Date today) 
            throws SessionInternalError;
    
    public void emailAndPayment(Integer entityId, Integer invoiceId,
            Integer processId, boolean processPayment);        

    /**
     * Process a user, generating the invoice/s,
     * @param userId
     */
    public Integer[] processUser(Integer processId, Integer userId,
            boolean isReview, boolean onlyRecurring);

    public BillingProcessDTOEx getDto(Integer processId, Integer languageId);

    public BillingProcessConfigurationDTO getConfigurationDto(Integer entityId) 
            throws SessionInternalError;

    public Integer createUpdateConfiguration(Integer executorId,
            BillingProcessConfigurationDTO dto) throws SessionInternalError;

    public Integer getLast(Integer entityId) throws SessionInternalError;

    public BillingProcessDTOEx getReviewDto(Integer entityId, 
            Integer languageId);

    public BillingProcessConfigurationDTO setReviewApproval(Integer executorId,
            Integer entityId, Boolean flag) throws SessionInternalError;

    public boolean trigger(Date pToday) throws SessionInternalError;

    /**
     * @return the id of the invoice generated
     */
    public InvoiceDTO generateInvoice(Integer orderId, Integer invoiceId,
            Integer languageId) throws SessionInternalError;
    
    public void reviewUsersStatus(Integer entityId, Date today) throws SessionInternalError;

    /**
     * Update status of BillingProcessRun in new transaction
     * for accessing from other thread
     * @param billingProcessId id of billing process for searching ProcessRun
     * @param processRunStatusId id of finished process run status (success or failure)
     * @return id of updated ProcessRunDTO
     */
    public Integer updateProcessRunFinished(Integer billingProcessId, Integer processRunStatusId);

    /**
     * Adds ProcessRunUser in new transaction
     * for accessing from other thread
     * @param billingProcessId id of billing process for searching ProcessRun
     * @param userId ID of user
     * @param status Status of billing process for specified user: 0 - failed, 1 - succeeded
     * @return id of inserted ProcessRunUserDTO
     */
    public Integer addProcessRunUser(Integer billingProcessId, Integer userId, Integer status);

    /**
     * Returns true if the Billing Process is currently running.
     * @return
     */
    public boolean isBillingRunning() ;
}
