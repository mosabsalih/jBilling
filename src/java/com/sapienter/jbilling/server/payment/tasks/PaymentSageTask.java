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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.util.Constants;

/**
 * A pluggable PaymentTask that uses Sage gateway for Bankcard and ACH
 * transactions.
 * 
 * The following parameters must be configured for this payment task to work: -
 * 
 * merchantid - merchant id provided by Sage merchantkey - merchant key provided
 * by Sage
 * 
 * timeout_sec - number of seconds for timeout (in inheritance from
 * PaymentTaskWithTimeout)
 * 
 * @author Krylenko
 */
public class PaymentSageTask extends PaymentTaskWithTimeout implements
        PaymentTask {
    // ------------------------ Constants -----------------------
    // names of plugin parameters
    private interface Params {
        public static final String MERCHANT_ID = "merchantid";

        public static final String MERCHANT_KEY = "merchantkey";
    }

    // Gateway urls
    private interface Urls {
        public static final String BANKCARD_POST_URL = "https://www.sagepayments.net/cgi-bin/eftBankcard.dll?transaction";

        public static final String VIRTUAL_CHECK_POST_URL = "https://www.sagepayments.net/cgi-bin/eftVirtualCheck.dll?transaction";
    }

    // Names of gateway parameters
    private interface SageParams {
        // common parameters for ACH and credit card payment
        interface General {
            public static final String MERCHANT_ID = "M_id";

            public static final String MERCHANT_KEY = "M_key";

            public static final String BILLING_ADDRESS = "C_address";

            public static final String BILLING_CITY = "C_city";

            public static final String BILLING_STATE = "C_state";

            public static final String BILLING_ZIP = "C_zip";

            public static final String EMAIL = "C_email";

            public static final String TRANSACTION_AMOUNT = "T_amt";

            public static final String TRANSACTION_CODE = "T_code";

            public static final String TRANSACTION_AUTH_CODE = "T_auth";
        }

        // parameters for credit card payment
        interface CreditCard {
            public static final String NAME = "C_name";

            public static final String CARD_NUMBER = "C_cardnumber";

            public static final String CARD_EXPIRATION_DATE = "C_exp";

            public static final String CARD_CVV = "C_cvv";
        }

        // parameters for ACH payment
        interface VirtualCheck {
            public static final String FIRST_NAME = "C_first_name";

            public static final String LAST_NAME = "C_last_name";

            public static final String BILLING_COUNTRY = "C_country";

            public static final String ROUTING_NUMBER = "C_rte";

            public static final String BANK_ACCOUNT_NUMBER = "C_acct";

            public static final String BANK_ACCOUNT_TYPE = "C_acct_type";

            public static final String CUSTOMER_TYPE = "C_customer_type";
        }
    }

    // Gateway constants
    private interface SageValues {
        interface BankAccountType {
            public static final String CHECKING = "DDA";

            public static final String SAVING = "SAV";
        }

        interface CustomerType {
            public static final String PERSONAL_MERCHANT_INITIATED = "PPD";
        }

        interface ApprovalIndicator {
            public static final String APPROVED = "A";

            public static final String FRONT_END_ERROR = "E";

            public static final String GATEWAY_ERROR = "X";
        }

        String[] ServerErrors = new String[] { "000000", "999999" };
    }

    // Type of transaction
    private enum Transaction {
        Payment("01"), AuthOnly("02"), Force("03"), Credit("06");

        private String code;

        private Transaction(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // Payment processor identificator
    private static final String PROCESSOR = "Sage";

    // Credit card expiration format
    private static final String DATE_FORMAT = "MMyy";

    // Value for checking bank account type
    private static final int CHECKING = 1;

    private static final Logger LOG = Logger.getLogger(PaymentSageTask.class);

    // ------------------------ Fields --------------------------
    private String merchantId;

    private String merchantKey;

    // ------------------------ Public methods ------------------
    /**
     * You could initialize plugin parameters here
     */
    @Override
    public void initializeParamters(PluggableTaskDTO task)
            throws PluggableTaskException {
        super.initializeParamters(task);
        merchantId = ensureGetParameter(Params.MERCHANT_ID);
        merchantKey = ensureGetParameter(Params.MERCHANT_KEY);
    }

    /**
     * Method for payment processisng
     * 
     * @param payment
     *            payment data
     * @return true if the next payment processor should be called by the
     *         business plugin manager. In other words, for result of success of
     *         failure, the return is false. If the communication with the
     *         payment processor fails (server down, timeout, etc), return
     *         false.
     */
    public boolean process(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("Payment processing for " + PROCESSOR + " gateway");
        Transaction transaction = Transaction.Payment;
        if (BigDecimal.ZERO.compareTo(payment.getAmount()) > 0) {
            transaction = Transaction.Credit;
            LOG.debug("Doing a credit transaction");
            // note: formatAmount() will make amount positive for sending to gateway
        }
        boolean result = doProcess(payment, transaction, null)
                .shouldCallOtherProcessors();
        LOG.debug("Processing result is " + payment.getPaymentResult().getId()
                + ", return value of process is " + result);
        return result;
    }

    /**
     * Do a credit card preauthorization of a fixed amount.
     * 
     * @param payment
     *            payment data
     * @return see prosess method description
     */
    public boolean preAuth(PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("PreAuth processing for " + PROCESSOR + " gateway");
        return doProcess(payment, Transaction.AuthOnly, null)
                .shouldCallOtherProcessors();
    }

    /**
     * Take a transaction done with 'preAuth' and confirm it
     * 
     * @param auth
     *            return value of preAuth.
     * @param payment
     *            payment data
     * @return see prosess method description
     */
    public boolean confirmPreAuth(PaymentAuthorizationDTO auth,
            PaymentDTOEx payment) throws PluggableTaskException {
        LOG.debug("ConfirmPreAuth processing for " + PROCESSOR + " gateway");
        if (!PROCESSOR.equals(auth.getProcessor())) {
            LOG.warn("The procesor of the pre-auth is not paypal, is "
                    + auth.getProcessor());
            // let the processor be called and failed, so the caller
            // can do something about it: probably call this one again but for
            // 'process'
        }
        CreditCardDTO card = payment.getCreditCard();
        if (card == null) {
            throw new PluggableTaskException(
                    "Credit card is required capturing" + " payment: "
                            + payment.getId());
        }
        if (!isApplicable(payment)) {
            LOG.error("This payment can not be captured" + payment);
            return true;
        }
        return doProcess(payment, Transaction.Force, auth)
                .shouldCallOtherProcessors();
    }

    /**
     * Method has been obsoleted
     */
    public void failure(Integer userId, Integer retry) {
        // ignore
    }

    // ------------------------ Private methods -----------------
    /**
     * Process transaction
     * 
     * @param payment
     *            payment data
     * @param transaction
     *            type of transaction
     * @param auth
     *            data for confirmPreAuth operation
     */
    private Result doProcess(PaymentDTOEx payment, Transaction transaction,
            PaymentAuthorizationDTO auth) throws PluggableTaskException {
        if (!isApplicable(payment)) {
            return NOT_APPLICABLE;
        }
        NVPList request = new NVPList();
        fillData(request, payment, transaction);
        if (Transaction.Force == transaction) {
            request.add(SageParams.General.TRANSACTION_AUTH_CODE, auth
                    .getTransactionId());
        }
        try {
            boolean isAch = isAch(payment);
            LOG.debug("Processing " + transaction + " for "
                    + (isAch ? "ACH" : "credit card"));
            SageAuthorization wrapper = new SageAuthorization(makeCall(request,
                    isAch), isAch);
            payment.setPaymentResult(new PaymentResultDAS().find(wrapper.getJBResultId()));
            storeProcessedAuthorization(payment, wrapper.getDTO());
            return new Result(wrapper.getDTO(), wrapper
                    .isCommunicationProblem());
        } catch (Exception e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    /**
     * Method check if plugin could handle this operation
     * 
     * @param payment
     *            payment data
     */
    private boolean isApplicable(PaymentDTOEx payment) {
        if (payment.getCreditCard() == null && payment.getAch() == null) {
            LOG.warn("Can't process without a credit card or ach");
            return false;
        }
        Integer refund = payment.getIsRefund();
        if (refund == null || refund == 0) {
            return true;
        } else {
            // todo: check if it true in Sage
            LOG.warn("Can't process refund");
            return false;
        }
    }

    /**
     * Check type of payment (ACH or credit card)
     * 
     * @param payment
     *            payment data
     */
    private boolean isAch(PaymentDTOEx payment) {
        // we use ACH payment by default
        return (payment.getAch() != null);
    }

    /**
     * Make request to agteway
     * 
     * @return response from gateway
     */
    private String makeCall(NVPList request, boolean isAch) throws IOException {
        LOG.debug("Request to " + PROCESSOR + " gateway sending...");
        // create a singular HttpClient object
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(getTimeoutSeconds() * 1000);
        PostMethod post = new PostMethod(getUrl(isAch));
        post.setRequestBody(request.toArray());
        // execute the method
        client.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();
        LOG.debug("Got response:" + responseBody);
        // clean up the connection resources
        post.releaseConnection();
        post.recycle();
        return responseBody;
    }

    /**
     * Fill request parameter with payment data
     */
    private void fillData(NVPList request, PaymentDTOEx payment,
            Transaction transaction) throws PluggableTaskException {
        boolean isAch = isAch(payment);
        request.add(SageParams.General.MERCHANT_ID, merchantId);
        request.add(SageParams.General.MERCHANT_KEY, merchantKey);
        try {
            ContactBL contact = new ContactBL();
            contact.set(payment.getUserId());
            ContactDTO contactEntity = contact.getEntity();
            request.add(SageParams.General.BILLING_ADDRESS, contactEntity
                    .getAddress1());
            request.add(SageParams.General.BILLING_CITY, contactEntity
                    .getCity());
            request.add(SageParams.General.BILLING_STATE, contactEntity
                    .getStateProvince());
            request.add(SageParams.General.BILLING_ZIP, contactEntity
                    .getPostalCode());
            if (isAch) {
                request.add(SageParams.VirtualCheck.FIRST_NAME, contactEntity
                        .getFirstName());
                request.add(SageParams.VirtualCheck.LAST_NAME, contactEntity
                        .getLastName());
                request.add(SageParams.VirtualCheck.BILLING_COUNTRY,
                        contactEntity.getCountryCode());
            } else {
                request.add(SageParams.CreditCard.NAME, contactEntity
                        .getLastName()
                        + " " + contactEntity.getFirstName());
            }
        } catch (Exception e) {
            throw new PluggableTaskException(
                    "Error loading Contact for user id " + payment.getUserId(),
                    e);
        }
        request.add(SageParams.General.TRANSACTION_AMOUNT, formatAmount(payment.getAmount()));
        request.add(SageParams.General.TRANSACTION_CODE, transaction.getCode());
        if (isAch) {
            AchDTO ach = payment.getAch();
            request.add(SageParams.VirtualCheck.ROUTING_NUMBER, ach
                    .getAbaRouting());
            request.add(SageParams.VirtualCheck.BANK_ACCOUNT_NUMBER, ach
                    .getBankAccount());
            request
                    .add(
                            SageParams.VirtualCheck.BANK_ACCOUNT_NUMBER,
                            ach.getAccountType() == CHECKING ? SageValues.BankAccountType.CHECKING
                                    : SageValues.BankAccountType.SAVING);
            request.add(SageParams.VirtualCheck.CUSTOMER_TYPE,
                    SageValues.CustomerType.PERSONAL_MERCHANT_INITIATED);
        } else {
            CreditCardDTO card = payment.getCreditCard();
            request.add(SageParams.CreditCard.CARD_NUMBER, card.getNumber());
            request.add(SageParams.CreditCard.CARD_EXPIRATION_DATE,
                    new SimpleDateFormat(DATE_FORMAT).format(card.getCcExpiry()));
            if (card.getSecurityCode() != null) {
                request.add(SageParams.CreditCard.CARD_CVV, String
                        .valueOf(payment.getCreditCard().getSecurityCode()));
            }
        }
    }

    /**
     * Format number to gateway format
     */
    private String formatAmount(BigDecimal amount) {
        amount = amount.abs().setScale(2, RoundingMode.HALF_EVEN); // gateway format, do not change!
        return amount.toPlainString();
    }

    /**
     * @return Gateway url
     */
    private String getUrl(boolean isAch) {
        return isAch ? Urls.VIRTUAL_CHECK_POST_URL : Urls.BANKCARD_POST_URL;
    }

    /**
     * Check if it server error
     * 
     * @param errorCode
     *            error code which was returned by gateway
     */
    private boolean isServerError(String errorCode) {
        for (String serverError : SageValues.ServerErrors) {
            if (serverError.equals(errorCode))
                return true;
        }
        return false;
    }

    // ------------------------ Private classes -----------------
    /**
     * Class for request parameters keeping
     */
    private static class NVPList extends LinkedList<NameValuePair> {
        public void add(String name, String value) {
            add(new NameValuePair(name, value));
        }

        public NameValuePair[] toArray() {
            return super.toArray(new NameValuePair[size()]);
        }
    }

    /**
     * Class for authorization response incaptulating
     */
    private class SageAuthorization {

        private final PaymentAuthorizationDTO paymentAuthDTO;

        public SageAuthorization(String gatewayResponse, boolean isAch) {
            LOG.debug("Payment authorization result of " + PROCESSOR
                    + " gateway parsing....");
            SageResponseParser responseParser = new SageResponseParser(
                    gatewayResponse, isAch);
            paymentAuthDTO = new PaymentAuthorizationDTO();
            paymentAuthDTO.setProcessor(PROCESSOR);
            paymentAuthDTO.setApprovalCode(responseParser
                    .getValue(responseParser.approvalCode));
            LOG
                    .debug("approvalCode [" + paymentAuthDTO.getApprovalCode()
                            + "]");
            paymentAuthDTO.setResponseMessage(responseParser
                    .getValue(responseParser.responseMessage));
            LOG.debug("responseMessage [" + paymentAuthDTO.getResponseMessage()
                    + "]");
            paymentAuthDTO.setCode1(responseParser
                    .getValue(responseParser.approvalIndicator));
            LOG.debug("approvalIndicator [" + paymentAuthDTO.getCode1() + "]");
            paymentAuthDTO.setCode2(responseParser
                    .getValue(responseParser.cvvIndicator));
            LOG.debug("cvvIndicator [" + paymentAuthDTO.getCode2() + "]");
            paymentAuthDTO.setAvs(responseParser
                    .getValue(responseParser.avsIndicator));
            LOG.debug("avsIndicator [" + paymentAuthDTO.getAvs() + "]");
            paymentAuthDTO.setCode3(responseParser
                    .getValue(responseParser.riskIndicator));
            LOG.debug("riskIndicator [" + paymentAuthDTO.getCode3() + "]");
            paymentAuthDTO.setTransactionId(responseParser
                    .getValue(responseParser.reference));
            LOG.debug("reference [" + paymentAuthDTO.getTransactionId() + "]");
        }

        public PaymentAuthorizationDTO getDTO() {
            return paymentAuthDTO;
        }

        public Integer getJBResultId() {
            if (isCommunicationProblem()) {
                return Constants.RESULT_UNAVAILABLE;
            }
            return SageValues.ApprovalIndicator.APPROVED
                    .equalsIgnoreCase(paymentAuthDTO.getCode1()) ? Constants.RESULT_OK
                    : Constants.RESULT_FAIL;
        }

        public boolean isCommunicationProblem() {
            return SageValues.ApprovalIndicator.GATEWAY_ERROR
                    .equalsIgnoreCase(paymentAuthDTO.getCode1())
                    && isServerError(paymentAuthDTO.getApprovalCode());
        }
    }

    /**
     * Class for gateway's response parsing
     */
    private class SageResponseParser {

        SageResponseEntry approvalIndicator;

        SageResponseEntry approvalCode;

        SageResponseEntry responseMessage;

        SageResponseEntry cvvIndicator;

        SageResponseEntry avsIndicator;

        SageResponseEntry riskIndicator;

        SageResponseEntry reference;

        private final String gatewayResponse;

        SageResponseParser(String gatewayResponse, boolean isAch) {
            this.gatewayResponse = gatewayResponse;
            approvalIndicator = new SageResponseEntry(2, 1);
            approvalCode = new SageResponseEntry(3, 6);
            responseMessage = new SageResponseEntry(9, 32);
            if (isAch) {
                riskIndicator = new SageResponseEntry(41, 2);
                reference = new SageResponseEntry(43, 10);
            } else {
                cvvIndicator = new SageResponseEntry(43, 1);
                avsIndicator = new SageResponseEntry(44, 1);
                riskIndicator = new SageResponseEntry(45, 2);
                reference = new SageResponseEntry(47, 10);
            }
        }

        String getValue(SageResponseEntry entry) {
            return (null == entry) ? null : entry.getValue();
        }

        /**
         * Class for gateway response entry incapsulating
         */
        class SageResponseEntry {
            private final int start;

            private final int length;

            private SageResponseEntry(int start, int length) {
                this.start = start;
                this.length = length;
            }

            String getValue() {
                return (start - 1 > 0 && start + length - 1 < gatewayResponse
                        .length()) ? gatewayResponse.substring(start - 1, start
                        + length - 1) : null;
            }
        }
    }

}
