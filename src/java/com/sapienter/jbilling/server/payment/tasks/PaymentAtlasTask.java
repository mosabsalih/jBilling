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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.CreditCardBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.util.Constants;

public class PaymentAtlasTask extends PaymentTaskWithTimeout {

    public static final ParameterDescription PARAMETER_MERCHANT_ACCOUNT_CODE = 
    	new ParameterDescription("merchant_account_code", true, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_TEST = 
    	new ParameterDescription("test", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_AVS = 
    	new ParameterDescription("submit_avs", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_PASSWORD = 
    	new ParameterDescription("password", true, ParameterDescription.Type.STR);

    private static final String URL = "https://atlasportal.net:8443/gates/xmlrpc";

    private static final String TEST_URL = "https://atlasbilling.net:8443/gates/xmlrpc";

    private static final int CONNECTION_TIME_OUT = 10000; // in millisec

    private static final int REPLY_TIME_OUT = 30000; // in millisec

    private Logger log = Logger.getLogger(PaymentAtlasTask.class);

    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_AVS);
        descriptions.add(PARAMETER_MERCHANT_ACCOUNT_CODE);
        descriptions.add(PARAMETER_PASSWORD);
        descriptions.add(PARAMETER_TEST);
    }

    
    public boolean process(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        boolean retValue = false;

        if (paymentInfo.getPayoutId() != null) {
            return true;
        }
        try {
            if (paymentInfo.getCreditCard() == null) {
                log.error("Can't process without a credit card");
                throw new TaskException("Credit card not present in payment");
            }
            if (paymentInfo.getAch() != null) {
                log.error("Can't process with a cheque");
                throw new TaskException("Can't process ACH charge");
            }

            if (paymentInfo.getIsRefund() == 1
                    && (paymentInfo.getPayment() == null || paymentInfo
                            .getPayment().getAuthorization() == null)) {
                log
                        .error("Can't process refund without a payment with an authorization record");
                throw new TaskException("Refund without previous authorization");
            }
            validateParameters();

            Map<String, Object> data;
            if (paymentInfo.getIsRefund() == 0) {
                data = getChargeData(paymentInfo);
            } else {
                data = getRefundData(paymentInfo);
            }

            if ("true".equals(getOptionalParameter(PARAMETER_AVS.getName(), "false"))) {
                addAVSFields(paymentInfo.getUserId(), data);
                log.debug("returning after avs " + data);
            }

            PaymentAuthorizationDTO response = makeCall(data, true);
            paymentInfo.setAuthorization(response);

            if ("1".equals(response.getCode1())) {
                paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
                log.debug("result is ok");
            } else {
                paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_FAIL));
                log.debug("result is fail");
            }

            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            bl.create(response, paymentInfo.getId());

        } catch (MalformedURLException e) {
            log.error("MalformedURLException exception when calling Atlas", e);
            paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            retValue = true;
        } catch (XmlRpcException e) {
            log.error("XmlRpcException exception when calling Atlas", e);
            paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            retValue = false;
        } catch (PluggableTaskException e) {
            log.error("PluggableTaskException", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception", e);
            throw new PluggableTaskException(e);
        }
        log.debug("returning " + retValue);
        return retValue;
    }

    private void validateParameters() throws PluggableTaskException {
        ensureGetParameter(PARAMETER_MERCHANT_ACCOUNT_CODE.getName());
        ensureGetParameter(PARAMETER_PASSWORD.getName());
    }

    public void failure(Integer userId, Integer retry) {
    }

    private Map<String, Object> getData(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("merchantAccountCode",
                ensureGetParameter(PARAMETER_MERCHANT_ACCOUNT_CODE.getName()));
        if (paymentInfo.getUserId() != null)
            data.put("customerAccountCode", String.valueOf(paymentInfo
                    .getUserId()));
        data.put("accountNumber", paymentInfo.getCreditCard().getNumber());
        data.put("name", paymentInfo.getCreditCard().getName());
        data.put("amount", paymentInfo.getAmount().multiply(new BigDecimal("100")).intValue());
        data.put("taxAmount", 0);
        String securityCode = paymentInfo.getCreditCard().getSecurityCode();
        if (securityCode != null)
            data.put("cvv2", securityCode);
        data.put("expirationDate", CreditCardBL.get4digitExpiry(paymentInfo
                .getCreditCard()));
        data.put("transactionDate", paymentInfo.getPaymentDate());
        data.put("transactionCode", paymentInfo.getId() + "");
        return data;
    }

    private Map<String, Object> getChargeData(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        Map<String, Object> data = getData(paymentInfo);
        data.put("creditIndicator", Boolean.FALSE);
        data.put("type", "sale-request");
        return data;
    }

    private Map<String, Object> getRefundData(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        Map<String, Object> data = getData(paymentInfo);
        data.put("itemCode", paymentInfo.getPayment().getAuthorization()
                .getTransactionId());
        data.put("creditIndicator", Boolean.TRUE);
        data.put("type", "credit-request");
        return data;
    }

    private void addAVSFields(Integer userId, Map<String, Object> data) {
        try {
            ContactBL contact = new ContactBL();
            contact.set(userId);
            ContactDTO entity = contact.getEntity();
            data.put("city", entity.getCity());
            data.put("email", entity.getEmail());
            data.put("customerAccountCode", userId.toString());
            data.put("phone", entity.getPhoneNumber());
            data.put("state", entity.getStateProvince());
            data.put("street", entity.getAddress1() + " "
                    + entity.getAddress2());
            data.put("zipCode", entity.getPostalCode());
            data.put("isOrganization", Boolean.FALSE);
        } catch (Exception e) {
            log.error("Exception when trying to add the AVS fields", e);
        }
    }

    private PaymentAuthorizationDTO makeCall(Map<String, Object> data,
            boolean isCharge) throws XmlRpcException, MalformedURLException,
            PluggableTaskException {

        URL callURL = null;
        if ("true".equals(getOptionalParameter(PARAMETER_TEST.getName(), "false"))) {
            callURL = new URL(TEST_URL);
            log.debug("Running Atlas task in test mode!");
        } else {
            callURL = new URL(URL);
        }
        String merchantAccountCode = ensureGetParameter(PARAMETER_MERCHANT_ACCOUNT_CODE.getName());

        int merchantCode = Integer.parseInt(merchantAccountCode);
        merchantCode = merchantCode - (merchantCode % 1000);

        XmlRpcClient paymentProcessor = new XmlRpcClient();
        paymentProcessor.setTransportFactory(new XmlRpcCommonsTransportFactory(
                paymentProcessor));

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(callURL);
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(CONNECTION_TIME_OUT);
        config.setReplyTimeout(REPLY_TIME_OUT);

        paymentProcessor.setConfig(config);

        List<Map<String, Object>> transactionRequestList = new ArrayList<Map<String, Object>>(
                1);
        transactionRequestList.add(data);

        Map<String, Object> configParam = new HashMap<String, Object>();
        if (isCharge) {
            configParam.put("waitConfirmation", "ignore");
        } else {
            configParam.put("waitConfirmation", "terminate");
        }

        Object[] params = new Object[] { String.valueOf(merchantCode),
                ensureGetParameter(PARAMETER_PASSWORD.getName()), transactionRequestList,
                configParam };

        Object[] resresponse = (Object[]) paymentProcessor.execute(
                "XMLRPCGates.processRetail", params);

        Map<String, Object> transactionResponseMap = (Map<String, Object>) resresponse[0];

        log.debug("Got response:" + transactionResponseMap);

        boolean isCredit = "credit-response".equals(transactionResponseMap
                .get("type"));

        PaymentAuthorizationDTO dbRow = new PaymentAuthorizationDTO();
        if (!isCredit
                && "A01".equals(transactionResponseMap.get("responseCode"))) {
            dbRow.setCode1("1"); // code if 1 it is ok
        } else if (isCredit
                && "A02".equals(transactionResponseMap.get("responseCode"))) {
            dbRow.setCode1("1");
        } else if ('A' != ((String) transactionResponseMap.get("responseCode"))
                .charAt(0)) {
            dbRow.setCode1("2");
        } else {
            dbRow.setCode1("0");
        }
        dbRow.setCode3((String) transactionResponseMap.get("responseCode"));
        dbRow.setResponseMessage((String) transactionResponseMap
                .get("responseMessage"));
        dbRow.setApprovalCode((String) transactionResponseMap
                .get("processorCode"));
        dbRow.setAvs((String) transactionResponseMap.get("avsResultCode"));
        dbRow.setTransactionId((String) transactionResponseMap
                .get("referenceNumber"));
        dbRow.setProcessor("Intrannuity");
        return dbRow;
    }

    public boolean preAuth(PaymentDTOEx payment) throws PluggableTaskException {
        try {
            validateParameters();
            Map<String, Object> data = getChargeData(payment);
            PaymentAuthorizationDTO response = makeCall(data, false);

            PaymentAuthorizationDTO authDtoEx = new PaymentAuthorizationDTO(
                    response);
            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            bl.create(authDtoEx, payment.getId());

            payment.setAuthorization(authDtoEx);
            return false;
        } catch (Exception e) {
            log.error("error trying to pre-authorize", e);
            return true;
        }
    }

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth,
            PaymentDTOEx paymentInfo) throws PluggableTaskException {
        return true;
    }

}
