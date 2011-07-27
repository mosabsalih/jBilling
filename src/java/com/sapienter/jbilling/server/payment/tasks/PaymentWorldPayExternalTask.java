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
package com.sapienter.jbilling.server.payment.tasks;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.payment.IExternalCreditCardStorage;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Brian Cowdery
 * @since 20-10-2009
 */
public class PaymentWorldPayExternalTask extends PaymentWorldPayBaseTask implements IExternalCreditCardStorage {
    private static final Logger LOG = Logger.getLogger(PaymentWorldPayExternalTask.class);

    @Override
    String getProcessorName() { return "WorldPay"; }    

    public boolean process(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("Payment processing for " + getProcessorName() + " gateway");

        if (payment.getPayoutId() != null) return true;

        /*  build a ReAuthorize request if the payment instrument has a gateway key to
            be used, otherwise create a new Sale transaction using the raw CC data.

            if the payment amount is negative or refund is set, do a Credit transaction.
         */
        prepareExternalPayment(payment);
        SvcType transaction = (BigDecimal.ZERO.compareTo(payment.getAmount()) > 0 || payment.getIsRefund() != 0
                               ? SvcType.REFUND_CREDIT
                               : (payment.getCreditCard().useGatewayKey()
                                  ? SvcType.RE_AUTHORIZE
                                  : SvcType.SALE));

        // process
        LOG.debug("creating " + transaction + " payment transaction");
        Result result = doProcess(payment, transaction, null);

        // update the stored external gateway key
        if (Constants.RESULT_OK.equals(payment.getResultId()))
            updateGatewayKey(payment);

        return result.shouldCallOtherProcessors();
    }

    public void failure(Integer userId, Integer retry) {
        // not supported
    }

    public boolean preAuth(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("Pre-authorization processing for " + getProcessorName() + " gateway");
        prepareExternalPayment(payment);
        return doProcess(payment, SvcType.AUTHORIZE, null).shouldCallOtherProcessors();
    }

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth, PaymentDTOEx payment)
            throws PluggableTaskException {

        LOG.debug("Confirming pre-authorization for " + getProcessorName() + " gateway");

        if (!getProcessorName().equals(auth.getProcessor())) {
            /*  let the processor be called and fail, so the caller can do something
                about it: probably re-call this payment task as a new "process()" run */
            LOG.warn("The processor of the pre-auth is not " + getProcessorName() + ", is " + auth.getProcessor());
        }

        CreditCardDTO card = payment.getCreditCard();
        if (card == null) {
            throw new PluggableTaskException("Credit card is required, capturing payment: " + payment.getId());
        }

        if (!isApplicable(payment)) {
            LOG.error("This payment can not be captured" + payment);
            return true;
        }

        // process
        prepareExternalPayment(payment);
        Result result = doProcess(payment, SvcType.SETTLE, auth);

        // update the stored external gateway key
        if (Constants.RESULT_OK.equals(payment.getResultId()))
            updateGatewayKey(payment);

        return result.shouldCallOtherProcessors();
    }

    /**
     * Prepares a given payment to be processed using an external storage gateway key instead of
     * the raw credit card number. If the associated credit card has been obscured it will be
     * replaced with the users stored credit card from the database, which contains all the relevant
     * external storage data.
     *
     * New or un-obscured credit cards will be left as is.
     *
     * @param payment payment to prepare for processing from external storage
     */
    public void prepareExternalPayment(PaymentDTOEx payment) {
        if (payment.getCreditCard().useGatewayKey()) {
            LOG.debug("credit card is obscured, retrieving from database to use external store.");
            payment.setCreditCard(new UserBL(payment.getUserId()).getCreditCard());
        } else {
            LOG.debug("new credit card or previously un-obscured, using as is.");
        }
    }

    /**
     * Updates the gateway key of the credit card associated with this payment. RBS WorldPay
     * implements the gateway key a per-transaction ORDER_ID that is returned as part of the
     * payment response.
     *
     * @param payment successful payment containing the credit card to update.
     *  */
    public void updateGatewayKey(PaymentDTOEx payment) {
        PaymentAuthorizationDTO auth = payment.getAuthorization();

        // update the gateway key with the returned RBS WorldPay ORDER_ID
        CreditCardDTO card = payment.getCreditCard();
        card.setGatewayKey(auth.getTransactionId());

        // obscure new credit card numbers
        if (!Constants.PAYMENT_METHOD_GATEWAY_KEY.equals(card.getCcType()))
            card.obscureNumber();
    }    

    /**
     * {@inheritDoc}
     *
     * Creates a payment of zero dollars and returns the RBC WorldPay ORDER_ID as the gateway
     * key to be stored for future transactions.
     */
    public String storeCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        UserDTO user;
        if (contact != null) {
            UserBL bl = new UserBL(contact.getUserId());
            user = bl.getEntity();
            creditCard = bl.getCreditCard(); 
        } else if (creditCard != null && !creditCard.getBaseUsers().isEmpty()) {
            user = creditCard.getBaseUsers().iterator().next();
        } else {
            LOG.error("Could not determine user id for external credit card storage");
            return null;
        }

        // new contact that has not had a credit card created yet
        if (creditCard == null) {
            LOG.warn("No credit card to store externally.");
            return null;
        }

        /*  Note, don't use PaymentBL.findPaymentInstrument() as the given creditCard is still being
            processed at the time that this event is being handled, and will not be found.

            PaymentBL()#create() will cause a stack overflow as it will attempt to update the credit card,
            emitting another NewCreditCardEvent which is then handled by this method and repeated.
         */
        PaymentDTO paymentInfo = new PaymentDTO();
        paymentInfo.setBaseUser(user);
        paymentInfo.setCurrency(user.getCurrency());
        paymentInfo.setAmount(BigDecimal.ZERO);
        paymentInfo.setCreditCard(creditCard);
        paymentInfo.setPaymentMethod(new PaymentMethodDAS().find(Util.getPaymentMethod(creditCard.getNumber())));
        paymentInfo.setIsRefund(0);
        paymentInfo.setIsPreauth(0);
        paymentInfo.setDeleted(0);
        paymentInfo.setAttempt(1);
        paymentInfo.setPaymentDate(new Date());
        paymentInfo.setCreateDatetime(new Date());

        PaymentDTOEx payment = new PaymentDTOEx(new PaymentDAS().save(paymentInfo));

        try {
            doProcess(payment, SvcType.SALE, null);
        } catch (PluggableTaskException e) {
            LOG.error("Could not process external storage payment", e);
            return null;
        }        

        // if result is OK, return authorization transaction id as the gateway key
        return Constants.RESULT_OK.equals(payment.getResultId())
               ? payment.getAuthorization().getTransactionId()
               : null;

    }

    /**
     * 
     */
    public String deleteCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        //noop
        return null;
    }
    
    @Override // implements abstract method
    public NVPList buildRequest(PaymentDTOEx payment, SvcType transaction) throws PluggableTaskException {
        NVPList request = new NVPList();

        request.add(PARAMETER_MERCHANT_ID, getMerchantId());
        request.add(PARAMETER_STORE_ID, getStoreId());
        request.add(PARAMETER_TERMINAL_ID, getTerminalId());
        request.add(PARAMETER_SELLER_ID, getSellerId());
        request.add(PARAMETER_PASSWORD, getPassword());

        request.add(WorldPayParams.General.AMOUNT, formatDollarAmount(payment.getAmount()));
        request.add(WorldPayParams.General.SVC_TYPE, transaction.getCode());

        CreditCardDTO card = payment.getCreditCard();

        /*  Sale transactions do not support the use of the ORDER_ID gateway key. After an initial sale
            transaction RBS WorldPay will have a record of our transactions for reference - so all
            other transaction types are safe for use with the stored gateway key.
         */
        if (SvcType.SALE.equals(transaction)
                && Constants.PAYMENT_METHOD_GATEWAY_KEY.equals(Util.getPaymentMethod(card.getNumber()))) {
            throw new PluggableTaskException("Cannot process a SALE transaction with an obscured credit card!");
        }

        if (card.useGatewayKey()) {
            request.add(WorldPayParams.ReAuthorize.ORDER_ID, card.getGatewayKey());

        } else {
            ContactBL contact = new ContactBL();
            contact.set(payment.getUserId());

            request.add(WorldPayParams.General.STREET_ADDRESS, contact.getEntity().getAddress1());
            request.add(WorldPayParams.General.CITY, contact.getEntity().getCity());
            request.add(WorldPayParams.General.STATE, contact.getEntity().getStateProvince());
            request.add(WorldPayParams.General.ZIP, contact.getEntity().getPostalCode());

            request.add(WorldPayParams.General.FIRST_NAME, contact.getEntity().getFirstName());
            request.add(WorldPayParams.General.LAST_NAME, contact.getEntity().getLastName());
            request.add(WorldPayParams.General.COUNTRY, contact.getEntity().getCountryCode());


            request.add(WorldPayParams.CreditCard.CARD_NUMBER, card.getNumber());
            request.add(WorldPayParams.CreditCard.EXPIRATION_DATE, EXPIRATION_DATE_FORMAT.format(card.getCcExpiry()));

            if (card.getSecurityCode() != null) {
                request.add(WorldPayParams.CreditCard.CVV2, String.valueOf(payment.getCreditCard().getSecurityCode()));
            }
        }

        return request;
    }
}
