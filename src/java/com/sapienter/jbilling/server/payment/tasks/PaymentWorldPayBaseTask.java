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

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.util.Constants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.ParameterParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class that contains all the common functionality needed to make a payment
 * to an RBS World Pay payment gateway.
 *
 * @author Brian Cowdery
 * @since 20-10-2009
 */
public abstract class PaymentWorldPayBaseTask extends PaymentTaskWithTimeout {
    private static final Logger LOG = Logger.getLogger(PaymentWorldPayBaseTask.class);

    /**
     * Parameters for RBS WorldPay payment gateway requests
     */
    public interface WorldPayParams {
        interface CreditCard {
            public static final String CARD_NUMBER      = "CardNumber";
            public static final String EXPIRATION_DATE  = "ExpirationDate"; // mm/yy or mm/yyyy
            public static final String CVV2             = "CVV2";           // optional CVV or CVC value
        }

        interface ReAuthorize {
            public static final String ORDER_ID = "OrderID";                // order id returned from a previously
        }                                                                   // successful transaction

        interface ForceParams {
            public static final String APPROVAL_CODE = "ApprovalCode";
        }

        interface SettleParams {
            public static final String ORDER_ID = "OrderID";                // order number of the transaction
        }

        /**
         * common parameters for ACH and credit card payment
         */
        interface General {
            public static final String SVC_TYPE       = "SvcType";          // @see SvcType
            public static final String FIRST_NAME     = "FirstName";
            public static final String LAST_NAME      = "LastName";
            public static final String STREET_ADDRESS = "StreetAddress";
            public static final String CITY           = "City";
            public static final String STATE          = "State";
            public static final String ZIP            = "Zip";
            public static final String COUNTRY        = "Country";
            public static final String AMOUNT         = "Amount";
        }
    }

    /**
     * RBS WorldPay gateway response parameters
     */
    public interface WorldPayResponse {
        public static final String TRANSACTION_STATUS = "TransactionStatus"; // @see TransactionStatus

        /*  transaction order number, which may be stored and used for subsequent payments
            through a re-authorization transaction. */
        public static final String ORDER_ID = "OrderId";
        
        /* approval codes returned by the Issuer if the authorization was approved */
        public static final String APPROVAL_CODE = "ApprovalCode";
        public static final String AVS_RESPONSE = "AVSResponse";            // Address Verification Service
        public static final String CVV2_RESPONSE = "CVV2Response";          // returned if CVV2 value was set

        public static final String ERROR_MSG = "ErrorMsg";
        public static final String ERROR_CODE = "ErrorCode";

    }

    /**
     * Represents the transaction type supported by the RBS WorldPay gateway.
     *
     * Please see <em>Appendix H: SVCTYPE</em> of the <em>API Specification - RBS WorldPay
     * Internet Processing Message Format</em> document.
     */
    public enum SvcType {
        AUTHORIZE       ("Authorize"),
        RE_AUTHORIZE    ("ReAuthorize"),
        SALE            ("Sale"),
        SETTLE          ("Settle"),
        FORCE           ("ForceSettle"),
        PARTIAL_SETTLE  ("PartialSettle"),
        REFUND_ORDER    ("CreditOrder"),
        REFUND_CREDIT   ("Credit");

        private String code;

        SvcType(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    /**
     * Represents transaction status codes returned by the RBS WorldPay gateway.
     *
     * Please see <em>Appendix K: Transaction Status</em> of the <em>API Specification - RBS WorldPay
     * Internet Processing Message Format</em> document.
     */
    public enum TransactionStatus {
        APPROVED        ("0"),
        NOT_APPROVED    ("1"),
        EXCEPTION       ("2");

        private String code;

        TransactionStatus(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    /**
     * Class for encapsulating authorization responses
     */
    public class WorldPayAuthorization {
        private final PaymentAuthorizationDTO paymentAuthDTO;

        public WorldPayAuthorization(String gatewayResponse) {
            LOG.debug("Payment authorization result of " + getProcessorName() + " gateway parsing....");

            WorldPayResponseParser responseParser = new WorldPayResponseParser(gatewayResponse);
            paymentAuthDTO = new PaymentAuthorizationDTO();
            paymentAuthDTO.setProcessor(getProcessorName());

            String approvalCode = responseParser.getValue(WorldPayResponse.APPROVAL_CODE);
            if (approvalCode != null) {
                paymentAuthDTO.setApprovalCode(approvalCode);
                LOG.debug("approvalCode [" + paymentAuthDTO.getApprovalCode() + "]");
            }

            String transactionStatus = responseParser.getValue(WorldPayResponse.TRANSACTION_STATUS);
            if (transactionStatus != null) {
                paymentAuthDTO.setCode2(transactionStatus);
                LOG.debug("transactionStatus [" + paymentAuthDTO.getCode2() + "]");
            }

            String orderID = responseParser.getValue(WorldPayResponse.ORDER_ID);
            if (orderID != null) {
                paymentAuthDTO.setTransactionId(orderID);
                paymentAuthDTO.setCode1(orderID);
                LOG.debug("transactionID/OrderID [" + paymentAuthDTO.getTransactionId() + "]");
            }

            String errorMsg = responseParser.getValue(WorldPayResponse.ERROR_MSG);
            if (errorMsg != null) {
                paymentAuthDTO.setResponseMessage(errorMsg);
                LOG.debug("errorMessage [" + paymentAuthDTO.getResponseMessage() + "]");
            }
        }

        public PaymentAuthorizationDTO getDTO() {
            return paymentAuthDTO;
        }

        public Integer getJBResultId() {
            Integer resultId = Constants.RESULT_UNAVAILABLE;

            if (TransactionStatus.APPROVED.getCode().equals(paymentAuthDTO.getCode2()))
                resultId = Constants.RESULT_OK;

            if (TransactionStatus.NOT_APPROVED.getCode().equals(paymentAuthDTO.getCode2()))
                resultId = Constants.RESULT_FAIL;

            if (TransactionStatus.EXCEPTION.getCode().equals(paymentAuthDTO.getCode2()))
                resultId = Constants.RESULT_UNAVAILABLE;

            return resultId;
        }

        public boolean isCommunicationProblem() {
            return TransactionStatus.EXCEPTION.getCode().equals(paymentAuthDTO.getCode2());
        }
    }

    /**
     * Class for gateway response parsing
     */
    private class WorldPayResponseParser {
        private final String gatewayResponse;
        private List<NameValuePair> responseEntries;

        WorldPayResponseParser(String gatewayResponse) {
            this.gatewayResponse = gatewayResponse;
            parseResponse();
        }

        public String getGatewayResponse() {
            return gatewayResponse;
        }

        public List<NameValuePair> getResponseEntries() {
            return responseEntries;
        }

        public String getValue(String responseParamName) {
            String val = null;
            for (NameValuePair pair : responseEntries) {
                if (pair.getName().equals(responseParamName)) {
                    val = pair.getValue();
                    break;
                }
            }
            return val;
        }

        @SuppressWarnings("unchecked")
        private void parseResponse() {
            ParameterParser parser = new ParameterParser();
            responseEntries = parser.parse(gatewayResponse, '&');

        }
    }

    /**
     * Name value pair list to hold request parameters, to be used in conjunction with the
     * {@link PaymentWorldPayBaseTask#buildRequest(PaymentDTOEx, SvcType)} method as a request
     * builder object.
     */
    public static class NVPList {
        List<NameValuePair> pairs = new LinkedList<NameValuePair>();

        public void add(String name, String value) {
            pairs.add(new NameValuePair(name, value));
        }

        public NameValuePair[] toArray() {
            return pairs.toArray(new NameValuePair[pairs.size()]);
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (Iterator<NameValuePair> it = pairs.iterator(); it.hasNext();) {
                NameValuePair pair = it.next();
                sb.append(pair.getName())
                  .append("=")
                  .append(pair.getValue());

                if (it.hasNext()) sb.append("&");
            }
            return sb.toString();
        }        
    }    

    public static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat("MM/yyyy");

    /* required */
    public static final String PARAMETER_STORE_ID      = "STOREID";    // store id assigned by RBS World Pay
    public static final String PARAMETER_MERCHANT_ID   = "MERCHANTID"; // merchant id assigned by RBS World Pay
    public static final String PARAMETER_TERMINAL_ID   = "TERMINALID"; // terminal id assigned by RBS World Pay

    /* optional */
    public static final String PARAMETER_WORLD_PAY_URL = "URL";
    public static final String DEFAULT_WORLD_PAY_URL = "https://tpdev.lynksystems.com/servlet/LynkePmtServlet";

    /*
     * optional Store SellerId associated with the StoreId. The SellerId is
     * mandatory if the security flag is turned on for the store.
     */
    public static final String PARAMETER_SELLER_ID = "SELLERID";

    /*
     * optional Password associated with the SellerId. The Password is
     * mandatory if the security flag is turned on for the store.
     */
    public static final String PARAMETER_PASSWORD = "PASSWORD";

    private String url;
    private String merchantId;
    private String storeId;
    private String terminalId;
    private String sellerId;
    private String password;

    public String getGatewayUrl() {
        if (url == null) url = getOptionalParameter(PARAMETER_WORLD_PAY_URL, DEFAULT_WORLD_PAY_URL);
        return url;
    }

    public String getMerchantId() throws PluggableTaskException {
        if (merchantId == null) merchantId = ensureGetParameter(PARAMETER_MERCHANT_ID);
        return merchantId;
    }

    public String getStoreId() throws PluggableTaskException {
        if (storeId == null) storeId = ensureGetParameter(PARAMETER_STORE_ID);
        return storeId;
    }

    public String getTerminalId() throws PluggableTaskException {
        if (terminalId == null) terminalId = ensureGetParameter(PARAMETER_TERMINAL_ID);
        return terminalId;
    }

    public String getSellerId() {
        if (sellerId == null) sellerId = getOptionalParameter(PARAMETER_SELLER_ID, "");
        return sellerId;
    }

    public String getPassword() {
        if (password == null) password = getOptionalParameter(PARAMETER_PASSWORD, "");
        return password;
    }

    /**
     * Utility method to format the given dollar float value to a two
     * digit number in compliance with the RBS WorldPay gateway API.
     *
     * @param amount dollar float value to format
     * @return formatted amount as a string
     */
    public static String formatDollarAmount(BigDecimal amount) {
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
    public static boolean isApplicable(PaymentDTOEx payment) {
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
    abstract String getProcessorName();

    /**
     * Constructs a request of NameValuePairs for submission to the configured RBS WorldPay gateway, and
     * returns the NVPList object.
     *
     * @param payment payment to build the request for
     * @param transaction transaction type
     * @return request parameter name value pair list
     * @throws PluggableTaskException if an unrecoverable exception occurs 
     */
    abstract NVPList buildRequest(PaymentDTOEx payment, SvcType transaction) throws PluggableTaskException;

    /**
     * Process a payment as per the given transaction SvcType. This method relies on the abstract
     * {@link #buildRequest(PaymentDTOEx, SvcType)} method to build the appropriate set of HTTP request
     * parameters for the required transaction/process.
     *
     * @param payment payment to process
     * @param transaction transaction type
     * @param auth payment pre-authorization, may be null.
     * @return payment result
     * @throws PluggableTaskException thrown if payment instrument is not a credit card, or if a refund is attempted with no authorization
     */
    protected Result doProcess(PaymentDTOEx payment, SvcType transaction, PaymentAuthorizationDTO auth)
            throws PluggableTaskException {

        if (!isApplicable(payment))
            return NOT_APPLICABLE;

        if (payment.getCreditCard() == null) {
            LOG.error("Can't process without a credit card");
            throw new PluggableTaskException("Credit card not present in payment");
        }

        if (payment.getAch() != null) {
            LOG.error("Can't process with a cheque");
            throw new PluggableTaskException("Can't process ACH charge");
        }

        NVPList request = buildRequest(payment, transaction);

        if (auth != null && !SvcType.RE_AUTHORIZE.equals(transaction)) {
            // add approvalCode & orderID parameters for this settlement transaction
            request.add(WorldPayParams.ForceParams.APPROVAL_CODE, auth.getApprovalCode());
            request.add(WorldPayParams.SettleParams.ORDER_ID, auth.getTransactionId());
        }

        if (payment.getIsRefund() == 1
                && (payment.getPayment() == null || payment.getPayment().getAuthorization() == null)) {
            LOG.error("Can't process refund without a payment with an authorization record");
            throw new PluggableTaskException("Refund without previous authorization");
        }

        try {
            LOG.debug("Processing " + transaction + " for credit card");
            WorldPayAuthorization wrapper = new WorldPayAuthorization(post(request));
            payment.setPaymentResult(new PaymentResultDAS().find(wrapper.getJBResultId()));

            // if transaction successful store it
            if (wrapper.getJBResultId().equals(Constants.RESULT_OK))
                storeProcessedAuthorization(payment, wrapper.getDTO());

            return new Result(wrapper.getDTO(), wrapper.isCommunicationProblem());

        } catch (Exception e) {
            LOG.error("Couldn't handle payment request due to error", e);
            payment.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return NOT_APPLICABLE;
        }
    }

    /**
     * Make request to the configured RBS WorldPay gateway. This method returns the response
     * body of an HTTP post request which can be parsed using the nested {@link WorldPayResponseParser}
     * class.
     *
     * @param request name value pair list of request parameters
     * @return response from gateway
     * @throws IOException thrown by {@link HttpClient#executeMethod(org.apache.commons.httpclient.HttpMethod)}
     */
    protected String post(NVPList request) throws IOException {
        LOG.debug("Making POST request to " + getProcessorName() + " gateway ...");

        HttpClient client = new HttpClient();
        client.setConnectionTimeout(getTimeoutSeconds() * 1000); // todo: remove deprecated connection timeout
        
        PostMethod post = new PostMethod(getGatewayUrl());             
        post.setRequestEntity(new StringRequestEntity(request.toString()));        
        LOG.debug("request body string: " + request.toString());

        // execute the method
        client.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();
        LOG.debug("Got response:" + responseBody);

        // clean up the connection resources
        post.releaseConnection();

        return responseBody;
    }
}
