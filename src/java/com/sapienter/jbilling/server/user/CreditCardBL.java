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

package com.sapienter.jbilling.server.user;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.payment.event.AbstractPaymentEvent;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.event.NewCreditCardEvent;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;

public class CreditCardBL extends ResultList
        implements CreditCardSQL {

    private CreditCardDAS creditCardDas = null;
    private CreditCardDTO creditCard = null;
    private static final Logger LOG = Logger.getLogger(CreditCardBL.class);
    private EventLogger eLogger = null;

    public CreditCardBL(Integer creditCardId) {
        init();
        set(creditCardId);
    }

    public CreditCardBL() {
        init();
    }

    public CreditCardBL(CreditCardDTO row) {
        init();
        creditCard = row;
    }

    private void init() {
        eLogger = EventLogger.getInstance();
        creditCardDas = new CreditCardDAS();
    }

    public CreditCardDTO getEntity() {
        return creditCard;
    }

    public void set(Integer id) {
        creditCard = creditCardDas.find(id);
    }

    public void set(CreditCardDTO pEntity) {
        creditCard = pEntity;
    }

    /**
     * Creates a new persistent CreditCardDTO and emits a NewCreditCardEvent.
     *
     * @param dto credit card to persist
     * @return id of persisted credit card
     */
    public Integer create(CreditCardDTO dto) {
        dto.setId(0);
        dto.setVersionNum(null);
        dto.setDeleted(0);

        // Only save un-obscured credit cards. If a card is obscured, we assume that it is an
        // existing card stored against an external payment gateway - fetch from the db instead
        if (!dto.useGatewayKey() || !dto.isNumberObsucred()) {
            creditCard = creditCardDas.save(dto);
            UserDTO user = getUser(creditCard);
            EventManager.process(new NewCreditCardEvent(creditCard, (user == null ? null : user.getEntity().getId())));
            LOG.debug("Saved new credit card " + creditCard.getId());

        } else {
            UserDTO user = getUser(dto);
            creditCard = new UserBL(user.getId()).getCreditCard();
            LOG.debug("Credit card obscured, using the stored credit card.");
        }

        return (creditCard == null ? null : creditCard.getId());
    }

    /**
     * Get the associated user for this credit card.
     *
     * @return associated user, null if not found
     */
    public UserDTO getUser() {
        return getUser(creditCard);
    }

    /**
     * Get the associated user for the given credit card. If the credit card is
     * a user credit card, the base user will be returned. If the credit card is being
     * used for a one-time payment, the user of the payment will be returned.
     *
     * @param dto credit card
     * @return associated user, null if not found
     */
    public UserDTO getUser(CreditCardDTO dto) {
        if (dto != null) {
            if (!dto.getBaseUsers().isEmpty()) {
                // credit card saved for a user
                return dto.getBaseUsers().iterator().next();

            } else if (!dto.getPayments().isEmpty()) {
                // credit card saved for a payment (cc not linked to a user)
                PaymentDTO payment = dto.getPayments().iterator().next();
                return payment.getBaseUser();
            }
        }
        return null;
    }

    public void update(Integer executorId, CreditCardDTO dto, Integer userId)
            throws SessionInternalError {
        if (executorId != null) {
            eLogger.audit(executorId, userId, Constants.TABLE_CREDIT_CARD,
                    creditCard.getId(),
                    EventLogger.MODULE_CREDIT_CARD_MAINTENANCE,
                    EventLogger.ROW_UPDATED, null,
                    null, creditCard.getCcExpiry());
        }
        creditCard.setCcExpiry(dto.getCcExpiry());
        creditCard.setName(dto.getName());

        // the number can be null, because calls from the API would do this
        // to leave the number unchanged. Ignore masked numbers and leave number as-is.
        if (dto.getNumber() != null && !dto.getNumber().contains("*")) {
            creditCard.setNumber(dto.getNumber());
        }

        creditCard.setDeleted(new Integer(0));

        // remove any pre-authorization. Otherwise the next payment won't be
        // done with this new credit card
        if (userId != null) {
            PaymentBL paymentBl = new PaymentBL();
            for (PaymentDTO auth : (Collection<PaymentDTO>) paymentBl.getHome().findPreauth(userId)) {
                LOG.debug("New credit card for user with pre-auths." + dto);
                paymentBl.set(auth);
                paymentBl.delete();
            }

        }

        UserDTO userD = new UserDAS().find(userId);
        dto.getBaseUsers().add(userD);
        creditCard.setBaseUsers(dto.getBaseUsers());
        userD.getCreditCards().add(creditCard);        

        NewCreditCardEvent event = new NewCreditCardEvent(creditCard, userD.getCompany().getId());     
        EventManager.process(event);

        new UserDAS().save(userD);
        new CreditCardDAS().save(creditCard);

    }

    public void delete(Integer executorId) {
        // now delete this creditCard record
        eLogger.audit(executorId, null, Constants.TABLE_CREDIT_CARD,
                creditCard.getId(),
                EventLogger.MODULE_CREDIT_CARD_MAINTENANCE,
                EventLogger.ROW_DELETED, null, null, null);

        creditCard.setDeleted(new Integer(1));

        Iterator<UserDTO> itera = creditCard.getBaseUsers().iterator();

        while (itera.hasNext()) {
            UserDTO uus = itera.next();

            uus.getCreditCards().remove(creditCard);
            itera.remove();

            itera = creditCard.getBaseUsers().iterator();

            new UserDAS().save(uus);
            new CreditCardDAS().save(creditCard);

        }

    }

    public void notifyExipration(Date today)
            throws SQLException,
            SessionInternalError {
        LOG.debug("Sending credit card expiration notifications. Today " + today);
        prepareStatement(CreditCardSQL.expiring);
        cachedResults.setDate(1, new java.sql.Date(today.getTime()));

        execute();
        while (cachedResults.next()) {
            Integer userId = new Integer(cachedResults.getInt(1));
            Integer ccId = new Integer(cachedResults.getInt(2));

            set(ccId);
            NotificationBL notif = new NotificationBL();
            UserBL user = new UserBL(userId);
            try {
                MessageDTO message = notif.getCreditCardMessage(user.getEntity().
                        getEntity().getId(), user.getEntity().getLanguageIdField(),
                        userId, getDTO());

                INotificationSessionBean notificationSess = 
                        (INotificationSessionBean) Context.getBean(
                        Context.Name.NOTIFICATION_SESSION);
                notificationSess.notify(userId, message);
            } catch (NotificationNotFoundException e) {
                LOG.warn("credit card message not set to user " + userId +
                        " because the entity lacks notification text");
            }
        }
        conn.close();

    }

    /**
     * Returns true if it makes sense to send this cc to the processor.
     * Otherwise false (like when the card is now expired). 
     */
    public boolean validate() {
        boolean retValue = true;

        if (creditCard.getCcExpiry().before(Calendar.getInstance().getTime())) {
            retValue = false;
        } else {
            if (Util.getPaymentMethod(creditCard.getNumber()) == null) {
                retValue = false;
            }
        }

        return retValue;
    }

    static public boolean validate(CreditCardDTO dto) {
        boolean retValue = true;

        if (dto.getCcExpiry() == null || dto.getName() == null ||
                dto.getNumber() == null) {
            retValue = false;
            Logger.getLogger(CreditCardBL.class).debug("invalid " + dto);
        }

        return retValue;
    }

    public CreditCardDTO getDTO() {
        CreditCardDTO dto = new CreditCardDTO();

        dto.setId(creditCard.getId());
        dto.setDeleted(creditCard.getDeleted());
        dto.setCcExpiry(creditCard.getCcExpiry());
        dto.setName(creditCard.getName());
        dto.setNumber(creditCard.getNumber());
        dto.setCcType(creditCard.getCcType());
        dto.setSecurityCode(creditCard.getSecurityCode());
        dto.setGatewayKey(creditCard.getGatewayKey());

        return dto;
    }

    public Integer getPaymentMethod() {
        return Util.getPaymentMethod(creditCard.getNumber());
    }

    /**
     * removes spaces and '-' from the number.
     * @param number
     * @return
     */
    public static String cleanUpNumber(String number) {
        return number.replaceAll("[-\\ ]", "").trim();
    }

    /**
     * Only used from the API, thus the usage of PaymentAuthorizationDTOEx
     * @param entityId
     * @param userId
     * @param cc
     * @param amount
     * @param currencyId
     * @return
     * @throws com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException
     */
    public PaymentAuthorizationDTOEx validatePreAuthorization(Integer entityId, Integer userId, CreditCardDTO cc,
                                                              BigDecimal amount, Integer currencyId) throws PluggableTaskException {

        // create a new payment record
        PaymentDTOEx paymentDto = new PaymentDTOEx();
        paymentDto.setAmount(amount);
        paymentDto.setCurrency(new CurrencyDAS().find(currencyId));
        paymentDto.setCreditCard(cc);
        paymentDto.setUserId(userId);
        paymentDto.setIsPreauth(1);

        // filler fields, required
        paymentDto.setIsRefund(0);
        paymentDto.setPaymentMethod(new PaymentMethodDAS().find(Util.getPaymentMethod(cc.getNumber())));
        paymentDto.setAttempt(1);
        paymentDto.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_ENTERED)); // to be updated later
        paymentDto.setPaymentDate(Calendar.getInstance().getTime());
        paymentDto.setBalance(amount);

        PaymentBL payment = new PaymentBL();
        payment.create(paymentDto); // this updates the id

        // use the payment processor configured 
        PluggableTaskManager taskManager = new PluggableTaskManager(entityId, Constants.PLUGGABLE_TASK_PAYMENT);
        PaymentTask task = (PaymentTask) taskManager.getNextClass();

        boolean processNext = true;
        while (task != null && processNext) {
            processNext = task.preAuth(paymentDto);
            // get the next task
            task = (PaymentTask) taskManager.getNextClass();

            // at the time, a pre-auth acts just like a normal payment for events
            AbstractPaymentEvent event = AbstractPaymentEvent.forPaymentResult(entityId, paymentDto);
            if (event != null) {
                EventManager.process(event);
            }
        }

        // update the result
        payment.getEntity().setPaymentResult(paymentDto.getPaymentResult());

        //create the return value
        PaymentAuthorizationDTOEx retValue = new PaymentAuthorizationDTOEx(paymentDto.getAuthorization().getOldDTO());
        if (paymentDto.getPaymentResult().getId() != Constants.RESULT_OK) {
            // if it was not successfull, it should not have balance
            payment.getEntity().setBalance(BigDecimal.ZERO);
            retValue.setResult(false);
        } else {
            retValue.setResult(true);
        }

        return retValue;
    }

    public static String get4digitExpiry(CreditCardDTO cc) {
        String expiry = null;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(cc.getCcExpiry());
        expiry = String.valueOf(
                cal.get(GregorianCalendar.MONTH) + 1) + String.valueOf(
                cal.get(GregorianCalendar.YEAR)).substring(2);
        if (expiry.length() == 3) {
            expiry = "0" + expiry;
        }

        return expiry;

    }

    /**
     * Deletes existing cc records and adds a new one.
     * @param executorId
     * Id of the user executing this method.
     * @param userId
     * Id of user who is updating cc.
     * @param cc
     * New cc data.
     */
    public void updateForUser(Integer executorId, Integer userId,
            CreditCardDTO cc) {

        UserDTO user = UserBL.getUserEntity(userId);

        Iterator iter = user.getCreditCards().iterator();
        // delete existing cc records
        while (iter.hasNext()) {
            set(((CreditCardDTO) iter.next()).getId());
            delete(executorId);
            iter.remove();
        }
        // add the new one
        create(cc);
//        creditCard.setUserId(userId);

        user.getCreditCards().add(getEntity());
        getEntity().getBaseUsers().add(user);

        new UserDAS().save(user);
        new CreditCardDAS().save(getEntity());

    }
}
