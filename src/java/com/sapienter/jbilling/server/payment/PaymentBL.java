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

package com.sapienter.jbilling.server.payment;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.InvoiceIdComparator;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentInfoChequeDAS;
import com.sapienter.jbilling.server.payment.db.PaymentInfoChequeDTO;
import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDAS;
import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.payment.db.PaymentResultDTO;
import com.sapienter.jbilling.server.payment.event.AbstractPaymentEvent;
import com.sapienter.jbilling.server.payment.event.PaymentDeletedEvent;
import com.sapienter.jbilling.server.pluggableTask.PaymentInfoTask;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.AchBL;
import com.sapienter.jbilling.server.user.CreditCardBL;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import javax.sql.rowset.CachedRowSet;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PaymentBL extends ResultList implements PaymentSQL {

    private static final Logger LOG = Logger.getLogger(PaymentBL.class);

    private PaymentDAS paymentDas = null;
    private PaymentInfoChequeDAS chequeDas = null;
    private CreditCardDAS ccDas = null;
    private PaymentMethodDAS methodDas = null;
    private PaymentInvoiceMapDAS mapDas = null;
    private PaymentDTO payment = null;
    private EventLogger eLogger = null;

    public PaymentBL(Integer paymentId) {
        init();
        set(paymentId);
    }

    public PaymentBL() {
        init();
    }

    public PaymentBL(PaymentDTO payment) {
        init();
        this.payment = payment;
    }

    public void set(PaymentDTO payment) {
        this.payment = payment;
    }

    private void init() {
        try {
            eLogger = EventLogger.getInstance();
            paymentDas = new PaymentDAS();

            chequeDas = new PaymentInfoChequeDAS();

            ccDas = new CreditCardDAS();

            methodDas = new PaymentMethodDAS();

            mapDas = new PaymentInvoiceMapDAS();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public PaymentDTO getEntity() {
        return payment;
    }

    public PaymentDAS getHome() {
        return paymentDas;
    }

    public String getMethodDescription(PaymentMethodDTO method, Integer languageId) {
        // load directly from the DB, otherwise proxies get in the way
        return new PaymentMethodDAS().find(method.getId()).getDescription(languageId);
    }

    public void set(Integer id) {
        payment = paymentDas.find(id);
    }

    public void create(PaymentDTOEx dto) {
        // create the record
        payment = paymentDas.create(dto.getAmount(), dto.getPaymentMethod(),
                dto.getUserId(), dto.getAttempt(), dto.getPaymentResult(), dto.getCurrency());

        payment.setPaymentDate(dto.getPaymentDate());
        payment.setBalance(dto.getBalance());
        // now verify if an info record should be created as well
        if (dto.getCheque() != null) {
            // create the db record
            PaymentInfoChequeDTO cheque = chequeDas.create();
            cheque.setBank(dto.getCheque().getBank());
            cheque.setNumber(dto.getCheque().getNumber());
            cheque.setDate(dto.getCheque().getDate());

            // update the relationship dto-info
            payment.setPaymentInfoCheque(cheque);
        }

        if (dto.getCreditCard() != null) {
            dto.getCreditCard().getPayments().add(payment); // back reference to payment
            CreditCardBL cc = new CreditCardBL();
            cc.create(dto.getCreditCard());

            payment.setCreditCard(cc.getEntity());
        }

        if (dto.getAch() != null) {
            dto.getAch().getPayments().add(payment); // back reference to payment
            AchBL achBl = new AchBL();
            achBl.create(dto.getAch());
            payment.setAch(achBl.getEntity());
        }

        // may be this is a refund
        if (dto.getIsRefund() == 1) {
            payment.setIsRefund(new Integer(1));
            // now all refunds have balance = 0
            payment.setBalance(BigDecimal.ZERO);
            if (dto.getPayment() != null) {
                // this refund is link to a payment
                PaymentBL linkedPayment = new PaymentBL(dto.getPayment().getId());
                payment.setPayment(linkedPayment.getEntity());
            }
        }

        // preauth payments
        if (dto.getIsPreauth() != null && dto.getIsPreauth().intValue() == 1) {
            payment.setIsPreauth(1);
        }

// the payment period length this payment was expected to last
        if (dto.getPaymentPeriod() != null){
            payment.setPaymentPeriod(dto.getPaymentPeriod());

        }
        // the notes related to this payment
        if (dto.getPaymentNotes() != null){
            payment.setPaymentNotes(dto.getPaymentNotes());
        }

        dto.setId(payment.getId());
        dto.setCurrency(payment.getCurrency());
        paymentDas.save(payment);
        // add a log row for convenience
        UserDAS user = new UserDAS();
        eLogger.auditBySystem(user.find(dto.getUserId()).getCompany().getId(),
                dto.getUserId(), Constants.TABLE_PAYMENT, dto.getId(),
                EventLogger.MODULE_PAYMENT_MAINTENANCE,
                EventLogger.ROW_CREATED, null, null, null);


    }

    void createMap(InvoiceDTO invoice, BigDecimal amount) {
        BigDecimal realAmount;
        if (new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_FAIL) || new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_UNAVAILABLE)) {
            realAmount = BigDecimal.ZERO;
        } else {
            realAmount = amount;
        }
        mapDas.create(invoice, payment, realAmount);
    }

    /**
     * Updates a payment record, including related cheque or credit card
     * records. Only valid for entered payments not linked to an invoice.
     *
     * @param dto
     *            The DTO with all the information of the new payment record.
     */
    public void update(Integer executorId, PaymentDTOEx dto)
            throws SessionInternalError {
        // the payment should've been already set when constructing this
        // object
        if (payment == null) {
            throw new EmptyResultDataAccessException("Payment to update not set", 1);
        }

        // we better log this, so this change can be traced
        eLogger.audit(executorId, payment.getBaseUser().getId(),
                Constants.TABLE_PAYMENT, payment.getId(),
                EventLogger.MODULE_PAYMENT_MAINTENANCE,
                EventLogger.ROW_UPDATED, null, payment.getAmount().toString(),
                null);

        // start with the payment's own fields
        payment.setUpdateDatetime(Calendar.getInstance().getTime());
        payment.setAmount(dto.getAmount());
        // since the payment can't be linked to an invoice, the balance
        // has to be equal to the total of the payment
        payment.setBalance(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());

        // now the records related to the method
        if (dto.getCheque() != null) {
            PaymentInfoChequeDTO cheque = payment.getPaymentInfoCheque();
            cheque.setBank(dto.getCheque().getBank());
            cheque.setNumber(dto.getCheque().getNumber());
            cheque.setDate(dto.getCheque().getDate());
        } else if (dto.getCreditCard() != null) {
            CreditCardBL cc = new CreditCardBL(payment.getCreditCard());
            cc.update(executorId, dto.getCreditCard(), null);
        } else if (dto.getAch() != null) {
            AchBL achBl = new AchBL(payment.getAch());
            achBl.update(executorId, dto.getAch());
        }

        // the payment period length this payment was expected to last
        if (dto.getPaymentPeriod() != null){
            payment.setPaymentPeriod(dto.getPaymentPeriod());

        }
        // the notes related to this payment
        if (dto.getPaymentNotes() != null){
            payment.setPaymentNotes(dto.getPaymentNotes());
        }
    }

    /**
     * Goes through the payment pluggable tasks, and calls them with the payment
     * information to get the payment processed. If a call fails because of the
     * availability of the processor, it will try with the next task. Otherwise
     * it will return the result of the process (approved or declined).
     *
     * @return the constant of the result allowing for the caller to attempt it
     *         again with different payment information (like another cc number)
     */
    public Integer processPayment(Integer entityId, PaymentDTOEx info)
            throws SessionInternalError {
        Integer retValue = null;
        try {
            PluggableTaskManager taskManager = new PluggableTaskManager(
                    entityId, Constants.PLUGGABLE_TASK_PAYMENT);
            PaymentTask task = (PaymentTask) taskManager.getNextClass();

            if (task == null) {
                // at least there has to be one task configurated !
                LOG.warn("No payment pluggable" + "tasks configurated for entity " + entityId);
                return null;
            }

            create(info);
            boolean processorUnavailable = true;
            while (task != null && processorUnavailable) {
                // see if this user has pre-auths
                PaymentAuthorizationBL authBL = new PaymentAuthorizationBL();
                PaymentAuthorizationDTO auth = authBL.getPreAuthorization(info.getUserId());
                if (auth != null) {
                    processorUnavailable = task.confirmPreAuth(auth, info);
                    if (!processorUnavailable) {
                        if (new Integer(info.getPaymentResult().getId()).equals(Constants.RESULT_FAIL)) {
                            processorUnavailable = task.process(info);
                        }
                        // in any case, don't use this preAuth again
                        authBL.markAsUsed(info);
                    }
                } else {
                    // get this payment processed
                    processorUnavailable = task.process(info);
                }

                // allow the pluggable task to do something if the payment
                // failed (like notification, suspension, etc ... )
                if (!processorUnavailable && new Integer(info.getPaymentResult().getId()).equals(Constants.RESULT_FAIL)) {
                    task.failure(info.getUserId(), info.getAttempt());
                }
                // trigger an event
                AbstractPaymentEvent event = AbstractPaymentEvent.forPaymentResult(entityId, info);

                if (event != null) {
                    EventManager.process(event);
                }

                // get the next task
                LOG.debug("Getting next task, processorUnavailable : " + processorUnavailable);
                task = (PaymentTask) taskManager.getNextClass();
            }

            // if after all the tasks, the processor in unavailable,
            // return that
            if (processorUnavailable) {
                retValue = Constants.RESULT_UNAVAILABLE;
            } else {
                retValue = info.getPaymentResult().getId();
            }

            // the balance of the payment depends on the result
            if (retValue.equals(Constants.RESULT_OK) || retValue.equals(Constants.RESULT_ENTERED)) {
                payment.setBalance(payment.getAmount());
            } else {
                payment.setBalance(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            LOG.fatal("Problems handling payment task.", e);
            throw new SessionInternalError("Problems handling payment task.");
        }

        // add a notification to the user if the payment was good or bad
        if (retValue.equals(Constants.RESULT_OK) || retValue.equals(Constants.RESULT_FAIL)) {
            sendNotification(info, entityId);
        }

        // obscure credit cards used for one-time payments
        if (payment.getCreditCard() != null && payment.getCreditCard().getBaseUsers().isEmpty()) {
            payment.getCreditCard().obscureNumber();
        }

        return retValue;
    }

    public PaymentDTO getDTO() {
        return new PaymentDTO(payment.getId(), payment.getAmount(), payment.getBalance(), payment.getCreateDatetime(), payment.getUpdateDatetime(), payment.getPaymentDate(), payment.getAttempt(), payment.getDeleted(),
                payment.getPaymentMethod(), payment.getPaymentResult(), payment.getIsRefund(), payment.getIsPreauth(), payment.getCurrency(), payment.getBaseUser());
    }

    public PaymentDTOEx getDTOEx(Integer language) {
        PaymentDTOEx dto = new PaymentDTOEx(getDTO());
        dto.setUserId(payment.getBaseUser().getUserId());
        // now add all the invoices that were paid by this payment
        Iterator it = payment.getInvoicesMap().iterator();
        while (it.hasNext()) {
            PaymentInvoiceMapDTO map = (PaymentInvoiceMapDTO) it.next();
            dto.getInvoiceIds().add(map.getInvoiceEntity().getId());

            dto.addPaymentMap(getMapDTO(map.getId()));
        }

        // cheque info if applies
        PaymentInfoChequeDTO chequeDto = null;
        if (payment.getPaymentInfoCheque() != null) {
            chequeDto = new PaymentInfoChequeDTO();
            chequeDto.setBank(payment.getPaymentInfoCheque().getBank());
            chequeDto.setDate(payment.getPaymentInfoCheque().getDate());
            chequeDto.setId(payment.getPaymentInfoCheque().getId());
            chequeDto.setNumber(payment.getPaymentInfoCheque().getNumber());
        }
        dto.setCheque(chequeDto);

        // credit card info if applies
        CreditCardDTO ccDto = null;
        if (payment.getCreditCard() != null) {
            ccDto = new CreditCardDTO();
            ccDto.setNumber(payment.getCreditCard().getNumber());
            ccDto.setCcExpiry(payment.getCreditCard().getCcExpiry());
            ccDto.setName(payment.getCreditCard().getName());
            ccDto.setCcType(payment.getCreditCard().getCcType());
            ccDto.setGatewayKey(payment.getCreditCard().getGatewayKey());
        }
        dto.setCreditCard(ccDto);

        // ach if applies
        if (payment.getAch() != null) {
            AchBL achBl = new AchBL(payment.getAch());
            dto.setAch(achBl.getDTO());
        } else {
            dto.setAch(null);
        }

        // payment method (international)
        PaymentMethodDTO method = payment.getPaymentMethod();
        dto.setMethod(method.getDescription(language));

        // refund fields if applicable
        dto.setIsRefund(payment.getIsRefund());
        if (payment.getPayment() != null && payment.getId() != payment.getPayment().getId()) {
            PaymentBL linkedPayment = new PaymentBL(payment.getPayment().getId());
            dto.setPayment(linkedPayment.getDTOEx(language));
        }

        // the first authorization if any
        if (!payment.getPaymentAuthorizations().isEmpty()) {
            PaymentAuthorizationBL authBL = new PaymentAuthorizationBL(
                    (PaymentAuthorizationDTO) payment.getPaymentAuthorizations().iterator().next());
            dto.setAuthorization(authBL.getDTO());
        }

        // the result in string mode (international)
        if (payment.getPaymentResult() != null) {
            PaymentResultDTO result = payment.getPaymentResult();
            dto.setResultStr(result.getDescription(language));
        }

        // to which payout this payment has been included
        if (payment.getPartnerPayouts().size() > 0) {
            dto.setPayoutId(((PartnerPayout) payment.getPartnerPayouts().toArray()[0]).getId());
        }

        // the payment period length this payment was expected to last
        if (payment.getPaymentPeriod() != null){
            dto.setPaymentPeriod(payment.getPaymentPeriod());

        }
        // the notes related to this payment
        if (payment.getPaymentNotes() != null){
            dto.setPaymentNotes(payment.getPaymentNotes());
        }

        return dto;
    }

    public static PaymentWS getWS(PaymentDTOEx dto) {
        PaymentWS ws = new PaymentWS();
        ws.setId(dto.getId());
        ws.setAmount(dto.getAmount());
        ws.setAttempt(dto.getAttempt());
        ws.setBalance(dto.getBalance());
        ws.setCreateDatetime(dto.getCreateDatetime());
        ws.setDeleted(dto.getDeleted());
        ws.setIsPreauth(dto.getIsPreauth());
        ws.setIsRefund(dto.getIsRefund());
        ws.setPaymentDate(dto.getPaymentDate());
        ws.setUpdateDatetime(dto.getUpdateDatetime());
        ws.setPaymentNotes(dto.getPaymentNotes());
        ws.setPaymentPeriod(dto.getPaymentPeriod());

        if (dto.getCurrency() != null)
            ws.setCurrencyId(dto.getCurrency().getId());

        if (dto.getPaymentMethod() != null)
            ws.setMethodId(dto.getPaymentMethod().getId());

        if (dto.getPaymentResult() != null)
            ws.setResultId(dto.getPaymentResult().getId());

        if (dto.getCreditCard() != null) {
            com.sapienter.jbilling.server.entity.CreditCardDTO ccDTO = new com.sapienter.jbilling.server.entity.CreditCardDTO();
            ccDTO.setDeleted(dto.getCreditCard().getDeleted());
            ccDTO.setExpiry(dto.getCreditCard().getCcExpiry());
            ccDTO.setId(dto.getCreditCard().getId());
            ccDTO.setName(dto.getCreditCard().getName());
            ccDTO.setNumber(dto.getCreditCard().getCcNumberPlain());
            ccDTO.setSecurityCode(dto.getCreditCard().getSecurityCode());
            ccDTO.setType(dto.getCreditCard().getCcType());
            ws.setCreditCard(ccDTO);
        } else {
            ws.setCreditCard(null);
        }

        ws.setUserId(dto.getUserId());

        if (dto.getCheque() != null) {
            com.sapienter.jbilling.server.entity.PaymentInfoChequeDTO chqDTO = new com.sapienter.jbilling.server.entity.PaymentInfoChequeDTO();
            chqDTO.setBank(dto.getCheque().getBank());
            chqDTO.setDate(dto.getCheque().getDate());
            chqDTO.setId(dto.getCheque().getId());
            chqDTO.setNumber(dto.getCheque().getNumber());
            ws.setCheque(chqDTO);
        } else {
            ws.setCheque(null);
        }

        ws.setMethod(dto.getMethod());

        if (dto.getAch() != null) {
            com.sapienter.jbilling.server.entity.AchDTO achDTO = new com.sapienter.jbilling.server.entity.AchDTO();
            achDTO.setAbaRouting(dto.getAch().getAbaRouting());
            achDTO.setAccountName(dto.getAch().getAccountName());
            achDTO.setAccountType(dto.getAch().getAccountType());
            achDTO.setBankAccount(dto.getAch().getBankAccount());
            achDTO.setBankName(dto.getAch().getBankName());
            achDTO.setGatewayKey(dto.getAch().getGatewayKey());
            achDTO.setId(dto.getAch().getId());
            ws.setAch(achDTO);
        } else {
            ws.setAch(null);
        }

        if (dto.getAuthorization() != null) {
            com.sapienter.jbilling.server.entity.PaymentAuthorizationDTO authDTO = new com.sapienter.jbilling.server.entity.PaymentAuthorizationDTO();
            authDTO.setAVS(dto.getAuthorization().getAvs());
            authDTO.setApprovalCode(dto.getAuthorization().getApprovalCode());
            authDTO.setCardCode(dto.getAuthorization().getCardCode());
            authDTO.setCode1(dto.getAuthorization().getCode1());
            authDTO.setCode2(dto.getAuthorization().getCode2());
            authDTO.setCode3(dto.getAuthorization().getCode3());
            authDTO.setCreateDate(dto.getAuthorization().getCreateDate());
            authDTO.setId(dto.getAuthorization().getId());
            authDTO.setMD5(dto.getAuthorization().getMD5());
            authDTO.setProcessor(dto.getAuthorization().getProcessor());
            authDTO.setResponseMessage(dto.getAuthorization().getResponseMessage());
            authDTO.setTransactionId(dto.getAuthorization().getTransactionId());

            ws.setAuthorization(authDTO);
        } else {
            ws.setAuthorization(null);
        }

        Integer invoiceIds[] = new Integer[dto.getInvoiceIds().size()];

        for (int f = 0; f < dto.getInvoiceIds().size(); f++) {
            invoiceIds[f] = (Integer) dto.getInvoiceIds().get(f);
        }
        ws.setInvoiceIds(invoiceIds);

        if (dto.getPayment() != null) {
            ws.setPaymentId(dto.getPayment().getId());
        } else {
            ws.setPaymentId(null);
        }
        return ws;
    }

    public CachedRowSet getList(Integer entityID, Integer languageId,
            Integer userRole, Integer userId, boolean isRefund)
            throws SQLException, Exception {

        // the first variable specifies if this is a normal payment or
        // a refund list
        if (userRole.equals(Constants.TYPE_ROOT) || userRole.equals(Constants.TYPE_CLERK)) {
            prepareStatement(PaymentSQL.rootClerkList);
            cachedResults.setInt(1, isRefund ? 1 : 0);
            cachedResults.setInt(2, entityID.intValue());
            cachedResults.setInt(3, languageId.intValue());
        } else if (userRole.equals(Constants.TYPE_PARTNER)) {
            prepareStatement(PaymentSQL.partnerList);
            cachedResults.setInt(1, isRefund ? 1 : 0);
            cachedResults.setInt(2, entityID.intValue());
            cachedResults.setInt(3, userId.intValue());
            cachedResults.setInt(4, languageId.intValue());
        } else if (userRole.equals(Constants.TYPE_CUSTOMER)) {
            prepareStatement(PaymentSQL.customerList);
            cachedResults.setInt(1, isRefund ? 1 : 0);
            cachedResults.setInt(2, userId.intValue());
            cachedResults.setInt(3, languageId.intValue());
        } else {
            throw new Exception("The payments list for the type " + userRole + " is not supported");
        }

        execute();
        conn.close();
        return cachedResults;
    }

    /**
     * Does the actual work of deleteing the payment
     *
     * @throws SessionInternalError
     */
    public void delete() throws SessionInternalError {

        try {
            LOG.debug("Deleting payment " + payment.getId());

            Integer entityId = payment.getBaseUser().getEntity().getId();
            EventManager.process(new PaymentDeletedEvent(entityId, payment));

            payment.setUpdateDatetime(Calendar.getInstance().getTime());
            payment.setDeleted(new Integer(1));

            eLogger.auditBySystem(entityId, payment.getBaseUser().getId(),
                    Constants.TABLE_PAYMENT, payment.getId(),
                    EventLogger.MODULE_PAYMENT_MAINTENANCE,
                    EventLogger.ROW_DELETED, null, null, null);

        } catch (Exception e) {
            LOG.warn("Problem deleteing payment.", e);
            throw new SessionInternalError("Problem deleteing payment.");
        }
    }

    /*
     * This is the list of payment that are refundable. It shows when entering a
     * refund.
     */
    public CachedRowSet getRefundableList(Integer languageId, Integer userId)
            throws SQLException, Exception {
        prepareStatement(PaymentSQL.refundableList);
        cachedResults.setInt(1, 0); // is not a refund
        cachedResults.setInt(2, userId.intValue());
        cachedResults.setInt(3, languageId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }

    public boolean isMethodAccepted(Integer entityId, Integer paymentMethodId) {

        boolean retValue = false;

        PaymentMethodDTO method = methodDas.find(paymentMethodId);

        for (Iterator it = method.getEntities().iterator(); it.hasNext();) {
            if (((CompanyDTO) it.next()).getId() == entityId) {
                retValue = true;
                break;
            }
        }
        return retValue;
    }

    public static PaymentDTOEx findPaymentInstrument(Integer entityId,
            Integer userId) throws PluggableTaskException,
            SessionInternalError, TaskException {

        PluggableTaskManager taskManager = new PluggableTaskManager(entityId,
                Constants.PLUGGABLE_TASK_PAYMENT_INFO);
        PaymentInfoTask task = (PaymentInfoTask) taskManager.getNextClass();

        if (task == null) {
            // at least there has to be one task configurated !
            Logger.getLogger(PaymentBL.class).fatal(
                    "No payment info pluggable" + "tasks configurated for entity " + entityId);
            throw new SessionInternalError("No payment info pluggable" + "tasks configurated for entity " + entityId);
        }

        // get this payment information. Now we only expect one pl.tsk
        // to get the info, I don't see how more could help
        return task.getPaymentInfo(userId);

    }

    public static boolean validate(PaymentWS dto) {
        boolean retValue = true;

        if (dto.getAmount() == null || dto.getMethodId() == null || dto.getIsRefund() == 0 || dto.getResultId() == null || dto.getUserId() == null || (dto.getCheque() == null && dto.getCreditCard() == null)) {
            retValue = false;
        } else if (dto.getCreditCard() != null) {
            PaymentDTOEx ex = new PaymentDTOEx(dto);
            retValue = CreditCardBL.validate(ex.getCreditCard());
        } else if (dto.getCheque() != null) {
            PaymentDTOEx ex = new PaymentDTOEx(dto);
            retValue = validate(ex.getCheque());
        }

        return retValue;
    }

    public static boolean validate(PaymentInfoChequeDTO dto) {
        boolean retValue = true;

        if (dto.getDate() == null || dto.getNumber() == null) {
            retValue = false;
        }

        return retValue;
    }

    public Integer getLatest(Integer userId) throws SessionInternalError {
        Integer retValue = null;
        try {
            prepareStatement(PaymentSQL.getLatest);
            cachedResults.setInt(1, userId.intValue());
            execute();
            if (cachedResults.next()) {
                int value = cachedResults.getInt(1);
                if (!cachedResults.wasNull()) {
                    retValue = new Integer(value);
                }
            }
            cachedResults.close();
            conn.close();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    public Integer[] getManyWS(Integer userId, Integer number,
            Integer languageId) {
        List<Integer> result = new PaymentDAS().findIdsByUserLatestFirst(
                userId, number);
        return result.toArray(new Integer[result.size()]);

    }

    private List<PaymentDTO> getPaymentsWithBalance(Integer userId) {
        // this will usually return 0 or 1 records, rearly a few more
        List<PaymentDTO> paymentsList = null;
        Collection payments = paymentDas.findWithBalance(userId);

        if (payments != null) {
            paymentsList = new ArrayList<PaymentDTO>(payments); // needed for the
            // sort
            Collections.sort(paymentsList, new PaymentEntityComparator());
            Collections.reverse(paymentsList);
        } else {
            paymentsList = new ArrayList<PaymentDTO>(); // empty
        }

        return paymentsList;
    }

    /**
     * Given an invoice, the system will look for any payment with a balance
     * and get the invoice paid with this payment.
     */
    public void automaticPaymentApplication(InvoiceDTO invoice)
            throws SQLException {
        List payments = getPaymentsWithBalance(invoice.getBaseUser().getUserId());

        for (int f = 0; f < payments.size() && invoice.getBalance().compareTo(BigDecimal.ZERO) > 0; f++) {
            payment = (PaymentDTO) payments.get(f);
            if (new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_FAIL) || new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_UNAVAILABLE)) {
                continue;
            }
            applyPaymentToInvoice(invoice);
        }
    }

    /**
     * Give an payment (already set in this object), it will look for any
     * invoices with a balance and get them paid, starting wiht the oldest.
     */
    public void automaticPaymentApplication() throws SQLException {
        if (BigDecimal.ZERO.compareTo(payment.getBalance()) >= 0) {
            return; // negative payment, skip
        }

        Collection<InvoiceDTO> invoiceCollection = new InvoiceDAS().findWithBalanceByUser(payment.getBaseUser());

        // sort from oldest to newest
        List<InvoiceDTO> invoices = new ArrayList<InvoiceDTO>(invoiceCollection);
        Collections.sort(invoices, new InvoiceIdComparator());

        for (InvoiceDTO invoice : invoices) {
            // negative balances don't need paying
            if (BigDecimal.ZERO.compareTo(invoice.getBalance()) > 0) {
                continue;
            }

            applyPaymentToInvoice(invoice);
            if (BigDecimal.ZERO.compareTo(payment.getBalance()) >= 0) {
                break; // no payment balance remaining
            }
        }
    }

    private void applyPaymentToInvoice(InvoiceDTO invoice) throws SQLException {
        // this is not actually getting de Ex, so it is faster
        PaymentDTOEx dto = new PaymentDTOEx(getDTO());

        // not pretty, but the methods are there
        IPaymentSessionBean psb = (IPaymentSessionBean) Context.getBean(
            Context.Name.PAYMENT_SESSION);
        // make the link between the payment and the invoice
        BigDecimal paidAmount = psb.applyPayment(dto, invoice, true);
        createMap(invoice, paidAmount);

        // notify the customer
        dto.setUserId(invoice.getBaseUser().getUserId()); // needed for the
        // notification
        // the notification only understands ok or not, if the payment is
        // entered
        // it has to show as ok
        dto.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
        sendNotification(dto, payment.getBaseUser().getEntity().getId());
    }

    /**
     * sends an notification with a payment
     */
    public void sendNotification(PaymentDTOEx info, Integer entityId) {
        try {
            NotificationBL notif = new NotificationBL();
            MessageDTO message = notif.getPaymentMessage(entityId, info,
                    new Integer(info.getPaymentResult().getId()).equals(Constants.RESULT_OK));

            INotificationSessionBean notificationSess =
                    (INotificationSessionBean) Context.getBean(
                    Context.Name.NOTIFICATION_SESSION);
            notificationSess.notify(info.getUserId(), message);
        } catch (NotificationNotFoundException e1) {
            // won't send anyting because the entity didn't specify the
            // notification
            LOG.warn("Can not notify a customer about a payment " +
                    "beacuse the entity lacks the notification. " +
                    "entity = " + entityId);
        }
    }

    /*
     * The payment doesn't have to be set. It adjusts the balances of both the
     * payment and the invoice and deletes the map row.
     */
    public void removeInvoiceLink(Integer mapId) {
        try {
            // find the map
            PaymentInvoiceMapDTO map = mapDas.find(mapId);
            // start returning the money to the payment's balance
            BigDecimal amount = map.getAmount();
            payment = map.getPayment();
            amount = amount.add(payment.getBalance());
            payment.setBalance(amount);

            // the balace of the invoice also increases
            InvoiceDTO invoice = map.getInvoiceEntity();
            amount = map.getAmount().add(invoice.getBalance());
            invoice.setBalance(amount);

            // this invoice probably has to be paid now
            if (Constants.BIGDECIMAL_ONE_CENT.compareTo(invoice.getBalance()) <= 0) {
                invoice.setToProcess(1);
            }

            // log that this was deleted, otherwise there will be no trace
            eLogger.info(invoice.getBaseUser().getEntity().getId(),
                    payment.getBaseUser().getId(), mapId,
                    EventLogger.MODULE_PAYMENT_MAINTENANCE,
                    EventLogger.ROW_DELETED,
                    Constants.TABLE_PAYMENT_INVOICE_MAP);

            // get rid of the map all together
            mapDas.delete(map);



        } catch (EntityNotFoundException enfe) {
            LOG.error("Exception removing payment-invoice link: EntityNotFoundException", enfe);
        } catch (Exception e) {
            LOG.error("Exception removing payment-invoice link", e);
            throw new SessionInternalError(e);
        }
    }

    /**
     * This method removes the link between this payment and the
     * <i>invoiceId</i> of the Invoice
     * @param invoiceId Invoice Id to be unlinked from this payment
     */
    public boolean unLinkFromInvoice(Integer invoiceId) {

    	InvoiceDTO invoice= new InvoiceDAS().find(invoiceId);
		Iterator<PaymentInvoiceMapDTO> it = invoice.getPaymentMap().iterator();
		boolean bSucceeded= false;
        while (it.hasNext()) {
            PaymentInvoiceMapDTO map = it.next();
            if (this.payment.getId() == map.getPayment().getId()) {
	            this.removeInvoiceLink(map.getId());
	            invoice.getPaymentMap().remove(map);
	            bSucceeded=true;
	            break;
            }
        }
        return bSucceeded;
    }

    public PaymentInvoiceMapDTOEx getMapDTO(Integer mapId) {
        // find the map
        PaymentInvoiceMapDTO map = mapDas.find(mapId);
        PaymentInvoiceMapDTOEx dto = new PaymentInvoiceMapDTOEx(map.getId(), map.getAmount(), map.getCreateDatetime());
        dto.setPaymentId(map.getPayment().getId());
        dto.setInvoiceId(map.getInvoiceEntity().getId());
        dto.setCurrencyId(map.getPayment().getCurrency().getId());
        return dto;
    }
}
