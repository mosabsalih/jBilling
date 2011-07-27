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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.paypal.sdk.exceptions.PayPalException;
import com.sapienter.jbilling.common.CommonConstants;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.payment.IExternalCreditCardStorage;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.payment.tasks.paypal.PaypalApi;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.CreditCard;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.Payer;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.Payment;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.PaypalResult;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;

import com.sapienter.jbilling.server.payment.tasks.paypal.dto.*;
import com.sapienter.jbilling.server.payment.db.*;

/**
 * Created by Roman Liberov, 03/02/2010
 */
public class PaymentPaypalExternalTask extends PaymentTaskWithTimeout implements IExternalCreditCardStorage {

    private static final Logger LOG = Logger.getLogger(PaymentPaypalExternalTask.class);

    /* Plugin parameters */
    public static final ParameterDescription PARAMETER_PAYPAL_USER_ID =
    	new ParameterDescription("PaypalUserId", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PAYPAL_PASSWORD =
    	new ParameterDescription("PaypalPassword", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PAYPAL_SIGNATURE =
    	new ParameterDescription("PaypalSignature", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PAYPAL_ENVIRONMENT =
    	new ParameterDescription("PaypalEnvironment", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PAYPAL_SUBJECT =
    	new ParameterDescription("PaypalSubject", false, ParameterDescription.Type.STR);

    public String getUserId() throws PluggableTaskException {
        return ensureGetParameter(PARAMETER_PAYPAL_USER_ID.getName());
    }

    public String getPassword() throws PluggableTaskException {
        return ensureGetParameter(PARAMETER_PAYPAL_PASSWORD.getName());
    }

    public String getSignature() throws PluggableTaskException {
        return ensureGetParameter(PARAMETER_PAYPAL_SIGNATURE.getName());
    }

    public String getEnvironment() throws PluggableTaskException {
        return getOptionalParameter(PARAMETER_PAYPAL_ENVIRONMENT.getName(), "Live");
    }

    public String getSubject() {
        return getOptionalParameter(PARAMETER_PAYPAL_SUBJECT.getName(), "");
    }

    //initializer for pluggable params
    {
    	descriptions.add(PARAMETER_PAYPAL_USER_ID);
        descriptions.add(PARAMETER_PAYPAL_PASSWORD);
        descriptions.add(PARAMETER_PAYPAL_SIGNATURE);
        descriptions.add(PARAMETER_PAYPAL_ENVIRONMENT);
        descriptions.add(PARAMETER_PAYPAL_SUBJECT);
    }

    private PaypalApi getApi() throws PluggableTaskException, PayPalException {
        return new PaypalApi(getUserId(), getPassword(),  getSignature(),
                    getEnvironment(), getSubject(), getTimeoutSeconds() * 1000);
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
     * Updates the gateway key of the credit card associated with this payment. PayPal
     * returns a TRANSACTIONID which can be used to start new transaction without specifying
     * payer info.
     *
     * @param payment successful payment containing the credit card to update.
     *  */
    public void updateGatewayKey(PaymentDTOEx payment) {
        PaymentAuthorizationDTO auth = payment.getAuthorization();

        // update the gateway key with the returned PayPal TRANSACTIONID
        CreditCardDTO card = payment.getCreditCard();
        card.setGatewayKey(auth.getTransactionId());

        // obscure new credit card numbers
        if (!com.sapienter.jbilling.common.Constants.PAYMENT_METHOD_GATEWAY_KEY.equals(card.getCcType()))
            card.obscureNumber();
    }

    /**
     * Utility method to format the given dollar float value to a two
     * digit number in compliance with the PayPal gateway API.
     *
     * @param amount dollar float value to format
     * @return formatted amount as a string
     */
    private static String formatDollarAmount(BigDecimal amount) {
        amount = amount.abs().setScale(2, RoundingMode.HALF_EVEN); // gateway format, do not change!
        return amount.toPlainString();
    }

    /**
     * Utility method to check if a given {@link PaymentDTOEx} payment can be processed
     * by this task.
     *
     * @param payment payment to check
     * @return true if payment can be processed with this task, false if not
     */
    private static boolean isApplicable(PaymentDTOEx payment) {
        if (payment.getCreditCard() == null && payment.getAch() == null) {
            LOG.warn("Can't process without a credit card or ach");
            return false;
        }
        return true;
    }

    /**
     * Returns the name of this payment processor.
     * @return payment processor name
     */
    private String getProcessorName() {
        return "PayPal";
    }

    private static boolean isRefund(PaymentDTOEx payment) {
        return BigDecimal.ZERO.compareTo(payment.getAmount()) > 0 || payment.getIsRefund() != 0;
    }

    private static boolean isCreditCardStored(PaymentDTOEx payment) {
        return payment.getCreditCard().useGatewayKey();
    }

    private PaymentAuthorizationDTO buildPaymentAuthorization(PaypalResult paypalResult) {
        LOG.debug("Payment authorization result of " + getProcessorName() + " gateway parsing....");

        PaymentAuthorizationDTO paymentAuthDTO = new PaymentAuthorizationDTO();
        paymentAuthDTO.setProcessor(getProcessorName());

        String txID = paypalResult.getTransactionId();
        if (txID != null) {
            paymentAuthDTO.setTransactionId(txID);
            paymentAuthDTO.setCode1(txID);
            LOG.debug("transactionId/code1 [" + txID + "]");
        }

        String errorMsg = paypalResult.getErrorCode();
        if (errorMsg != null) {
            paymentAuthDTO.setResponseMessage(errorMsg);
            LOG.debug("errorMessage [" + errorMsg + "]");
        }

        String avs = paypalResult.getAvs();
        if(avs != null) {
            paymentAuthDTO.setAvs(avs);
            LOG.debug("avs [" + avs + "]");
        }

        return paymentAuthDTO;
    }

    private static String convertCreditCardType(int ccType) {
        switch(ccType) {
            case 2: return CreditCardType.VISA.toString();
            case 3: return CreditCardType.MASTER_CARD.toString();
            case 4: return CreditCardType.AMEX.toString();
            case 6: return CreditCardType.DISCOVER.toString();
        }

        return "";
    }

    private static String convertCreditCardExpiration(Date ccExpiry) {
        return new SimpleDateFormat("MMyyyy").format(ccExpiry);
    }

    private static CreditCard convertCreditCard(PaymentDTOEx payment) {
        return new CreditCard(
                            convertCreditCardType(payment.getCreditCard().getCcType()),
                            payment.getCreditCard().getCcNumberPlain(),
                            convertCreditCardExpiration(payment.getCreditCard().getExpiry()),
                            payment.getCreditCard().getSecurityCode());
    }

    private static Payer convertPayer(PaymentDTOEx payment) {
        ContactBL contactBL = new ContactBL();
        contactBL.set(payment.getUserId());

        ContactDTO contact = contactBL.getEntity();

        Payer payer = new Payer();
        payer.setEmail(contact.getEmail());
        payer.setFirstName(contact.getFirstName());
        payer.setLastName(contact.getLastName());
        payer.setStreet(contact.getAddress1());
        payer.setCity(contact.getCity());
        payer.setState(contact.getStateProvince());
        payer.setCountryCode(contact.getCountryCode());
        payer.setZip(contact.getPostalCode());

        return payer;
    }

    private void storePaypalResult(PaypalResult result, PaymentDTOEx payment,
                                   PaymentAuthorizationDTO paymentAuthorization, boolean updateKey) {
        if(result.isSucceseeded()) {
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
            new PaymentAuthorizationBL().create(paymentAuthorization, payment.getId());
            payment.setAuthorization(paymentAuthorization);
            if(updateKey) {
                updateGatewayKey(payment);
            }
        } else {
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_FAIL));
        }
    }

    private Result doRefund(PaymentDTOEx payment) throws PluggableTaskException {
        try {
            PaypalApi api = getApi();

            PaypalResult result = api.refundTransaction(
                    payment.getAuthorization().getTransactionId(),
                    formatDollarAmount(payment.getAmount()),
                    RefundType.FULL);

            PaymentAuthorizationDTO paymentAuthorization = buildPaymentAuthorization(result);
            storePaypalResult(result, payment, paymentAuthorization, false);

            return new Result(paymentAuthorization, false);

        } catch (PayPalException e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    private Result doPaymentWithStoredCreditCard(PaymentDTOEx payment, PaymentAction paymentAction) throws PluggableTaskException {
        try {
            PaypalResult result = getApi().doReferenceTransaction(
                    payment.getAuthorization().getTransactionId(),
                    paymentAction,
                    new Payment(formatDollarAmount(payment.getAmount()), "USD"));

            PaymentAuthorizationDTO paymentAuthorization = buildPaymentAuthorization(result);
            storePaypalResult(result, payment, paymentAuthorization, true);

            return new Result(paymentAuthorization, false);

        } catch (PayPalException e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    private Result doPaymentWithoutStoredCreditCard(PaymentDTOEx payment, PaymentAction paymentAction,
                                                    boolean updateKey) throws PluggableTaskException {
        try {
            PaypalResult result = getApi().doDirectPayment(
                    paymentAction,
                    convertPayer(payment),
                    convertCreditCard(payment),
                    new Payment(formatDollarAmount(payment.getAmount()), "USD"));

            PaymentAuthorizationDTO paymentAuthorization = buildPaymentAuthorization(result);
            storePaypalResult(result, payment, paymentAuthorization, updateKey);

            return new Result(paymentAuthorization, false);

        } catch (PayPalException e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    private Result doCapture(PaymentDTOEx payment, PaymentAuthorizationDTO auth) throws PluggableTaskException {
        try {
            PaypalResult result = getApi().doCapture(
                    auth.getTransactionId(),
                    new Payment(formatDollarAmount(payment.getAmount()), "USD"),
                    CompleteType.COMPLETE);

            PaymentAuthorizationDTO paymentAuthorization = buildPaymentAuthorization(result);
            storePaypalResult(result, payment, paymentAuthorization, true);

            return new Result(paymentAuthorization, false);

        } catch (PayPalException e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    private boolean doProcess(PaymentDTOEx payment, PaymentAction paymentAction, boolean updateKey)
            throws PluggableTaskException {

        if(isRefund(payment)) {
            return doRefund(payment).shouldCallOtherProcessors();
        }

        if(isCreditCardStored(payment)) {
            return doPaymentWithStoredCreditCard(payment, paymentAction)
                    .shouldCallOtherProcessors();
        }

        return doPaymentWithoutStoredCreditCard(payment, paymentAction, updateKey)
                .shouldCallOtherProcessors();
    }

    private void doVoid(PaymentDTOEx payment) throws PluggableTaskException {
        try {
            getApi().doVoid(payment.getAuthorization().getTransactionId());
        } catch (PayPalException e) {
            LOG.error("Couldn't void payment authorization due to error", e);
            throw new PluggableTaskException(e);
        }
    }

    public boolean process(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("Payment processing for " + getProcessorName() + " gateway");

        if (payment.getPayoutId() != null) {
            return true;
        }

        if(!isApplicable(payment)) {
            return NOT_APPLICABLE.shouldCallOtherProcessors();
        }

        prepareExternalPayment(payment);
        return doProcess(payment, PaymentAction.SALE, true /* updateKey */);
    }

    public void failure(Integer userId, Integer retry) {
        // do nothing
    }

    public boolean preAuth(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("Pre-authorization processing for " + getProcessorName() + " gateway");
        prepareExternalPayment(payment);
        return doProcess(payment, PaymentAction.AUTHORIZATION, true /* updateKey */);
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
            LOG.error("This payment can not be captured: " + payment);
            return NOT_APPLICABLE.shouldCallOtherProcessors();
        }

        // process
        prepareExternalPayment(payment);
        return doCapture(payment, auth).shouldCallOtherProcessors();
    }

    public String storeCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        LOG.debug("Storing creadit card info within " + getProcessorName() + " gateway");
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
        PaymentDTO payment = new PaymentDTO();
        payment.setBaseUser(user);
        payment.setCurrency(user.getCurrency());
        payment.setAmount(CommonConstants.BIGDECIMAL_ONE_CENT);
        payment.setCreditCard(creditCard);
        payment.setPaymentMethod(new PaymentMethodDAS().find(Util.getPaymentMethod(creditCard.getNumber())));
        payment.setIsRefund(0);
        payment.setIsPreauth(0);
        payment.setDeleted(0);
        payment.setAttempt(1);
        payment.setPaymentDate(new Date());
        payment.setCreateDatetime(new Date());

        PaymentDTOEx paymentEx = new PaymentDTOEx(new PaymentDAS().save(payment));
        try {
            doProcess(paymentEx, PaymentAction.SALE, false /* updateKey */);
            doVoid(paymentEx);

            PaymentAuthorizationDTO auth = paymentEx.getAuthorization();
            return auth.getTransactionId();
        } catch (PluggableTaskException e) {
            LOG.error("Could not process external storage payment", e);
            return null;
        }
    }

    /**
     *
     */
    public String deleteCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        //noop
        return null;
    }
}
