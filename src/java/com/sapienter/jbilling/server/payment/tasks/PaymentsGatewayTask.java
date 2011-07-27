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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.util.Constants;

public class PaymentsGatewayTask extends PaymentTaskWithTimeout implements
        PaymentTask {

    // mandatory parameters
    public static final String PARAMETER_MERCHANT_ID = "merchant_id";
    public static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_HOST = "host"; // "www.paymentsgateway.net";
    private static final String PARAMETER_PORT = "port"; // 6050

    // optional parameters
    public static final String PARAMETER_AVS = "submit_avs";
    public static final String PARAMETER_TEST = "test";
    private static final String PARAMETER_TEST_HOST = "test_host"; // "www.paymentsgateway.net";
    private static final String PARAMETER_TEST_PORT = "test_port"; // 6050

    private static final int CONNECTION_TIME_OUT = 10000; // in millisec
    private static final int REPLY_TIME_OUT = 30000; // in millisec

    // Credit Card Types
    private static final int CC_TYPE_VISA = 2;
    private static final int CC_TYPE_MASTER = 3;
    private static final int CC_TYPE_AMEX = 4;
    private static final int CC_TYPE_DISC = 6;
    private static final int CC_TYPE_DINE = 7;
    // unsupported though
    private static final int CC_TYPE_JCB = 8;

    /*
     * jBilling defs. public static final Integer PAYMENT_METHOD_CHEQUE = new
     * Integer(1); public static final Integer PAYMENT_METHOD_VISA = new
     * Integer(2); public static final Integer PAYMENT_METHOD_MASTERCARD = new
     * Integer(3); public static final Integer PAYMENT_METHOD_AMEX = new
     * Integer(4); public static final Integer PAYMENT_METHOD_ACH = new
     * Integer(5); public static final Integer PAYMENT_METHOD_DISCOVERY = new
     * Integer(6); public static final Integer PAYMENT_METHOD_DINERS = new
     * Integer(7); public static final Integer PAYMENT_METHOD_PAYPAL = new
     * Integer(8);
     */

    // Payment Method

    private static final int PAYMENT_METHOD_CC = 1;
    private static final int PAYMENT_METHOD_ACH = 2;
    private static final int PAYMENT_METHOD_CHEQUE = 3;

    // CC Transaction Types
    private static final String CC_SALE = "10";
    private static final String CC_AUTH = "11";
    private static final String CC_CAPT = "12";
    private static final String CC_CRED = "13"; // CC Refunds

    // CC Transaction Types
    private static final String EFT_SALE = "20";
    private static final String EFT_AUTH = "21";
    private static final String EFT_CAPT = "22";
    private static final String EFT_CRED = "23"; // ACH Refund
    private static final String EFT_VERIFY = "26"; // EFT Verify Only - for use
                                                    // with ATMVerify (R)

    // Response Type
    private static final String RESPONSE_CODE_APPROVED = "A"; // Approved
    private static final String RESPONSE_CODE_DECLINED = "D"; // Declined
    private static final String RESPONSE_CODE_ERROR = "E"; // Exception

    private Logger log;
    private String payloadData = "";

    public PaymentsGatewayTask() {
        log = Logger.getLogger(PaymentsGatewayTask.class);
    }

    public boolean process(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {

        boolean retValue = false;

        try {

            int method = -1;
            boolean preAuth = false;
            if (paymentInfo.getIsPreauth() != null
                    && paymentInfo.getIsPreauth().intValue() == 1) {
                preAuth = true;
            }
            log.debug("Payment request Received ; Method : "
                    + paymentInfo.getMethod());

            if (paymentInfo.getCreditCard() != null) {
                method = PAYMENT_METHOD_CC;
            } else if (paymentInfo.getCheque() != null && paymentInfo.getAch() != null) {
                method = PAYMENT_METHOD_CHEQUE;
            } else if (paymentInfo.getAch() != null) {
                method = PAYMENT_METHOD_ACH;
            } else {
                // hmmm problem
                log.error("Can't process without a credit card, ach or cheque");
                throw new PluggableTaskException(
                        "Credit card/ACH/Cheque not present in payment");
            }

//          if (paymentInfo.getIsRefund() == 1
//                  && (paymentInfo.getPayment() == null || paymentInfo
//                          .getPayment().getAuthorization() == null)) {
//              log.error("Can't process refund without a payment with an"
//                      + " authorization record");
//              throw new PluggableTaskException("Refund without previous "
//                      + "authorization");
//          }

            // prepare common data for sending to Gateway
            validateParameters();
            String data = getChargeData(paymentInfo, method, preAuth);

            PaymentAuthorizationDTO response = processPGRequest(data);
            paymentInfo.setAuthorization(response);

            log.debug("Response code " + response.getCode1());
            if (RESPONSE_CODE_APPROVED.equals(response.getCode1())) {
                paymentInfo.setPaymentResult(new PaymentResultDAS()
                        .find(Constants.RESULT_OK));
                log.debug("result is ok");
            } else {
                paymentInfo.setPaymentResult(new PaymentResultDAS()
                        .find(Constants.RESULT_FAIL));
                log.debug("result is fail");
            }

            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            bl.create(response, paymentInfo.getId());

        } catch (PluggableTaskException e) {
            log.error("PluggableTaskException", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception", e);
            throw new PluggableTaskException(e);
        }

        log.debug("process returning " + retValue);

        return retValue;

    }

    private String getChargeData(PaymentDTOEx paymentInfo, int method,
            boolean preAuth) throws PluggableTaskException {

        String payloadData = new String("");
        try {

            payloadData += "pg_merchant_id="
                    + ensureGetParameter(PARAMETER_MERCHANT_ID) + "\n";
            payloadData += "pg_password="
                    + ensureGetParameter(PARAMETER_PASSWORD) + "\n";
            payloadData += "pg_total_amount="
                    + (paymentInfo.getAmount().abs()).toString() + "\n";
            payloadData += "pg_transaction_type="
                    + getTransType(paymentInfo, method, preAuth) + "\n";
            // common data
            // pg_consumer_id-assigned by merchant, returned with response
            // ecom_consumerorderid-assigned by merchant, returned with response
            // ecom_walletid-assigned by merchant, returned with response
            // pg_billto_postal_name_company-company name

            Integer userId = paymentInfo.getUserId();
            ContactBL contact = new ContactBL();
            contact.set(userId);
            ContactDTO entity = contact.getEntity();
            payloadData += "Ecom_BillTo_Postal_Name_First="
                    + entity.getFirstName() + "\n";
            payloadData += "Ecom_BillTo_Postal_Name_Last="
                    + entity.getLastName() + "\n";

            if ("true".equals(getOptionalParameter(PARAMETER_AVS, "false"))) {

                payloadData += "ecom_billto_postal_street_line1="
                        + entity.getAddress1() + "\n";
                payloadData += "ecom_billto_postal_street_line2="
                        + entity.getAddress2() + "\n";
                payloadData += "ecom_billto_postal_city=" + entity.getCity()
                        + "\n";
                payloadData += "ecom_billto_postal_stateprov="
                        + entity.getStateProvince() + "\n";
                payloadData += "ecom_billto_postal_postalcode="
                        + entity.getPostalCode() + "\n";
                payloadData += "ecom_billto_postal_countrycode="
                        + entity.getCountryCode() + "\n";
                payloadData += "ecom_billto_telecom_phone_number="
                        + entity.getPhoneNumber() + "\n";
                payloadData += "ecom_billto_online_email=" + entity.getEmail()
                        + "\n";
            }

            // pg_billto_ssn-customer?s social security number
            // pg_billto_dl_number-customer?s driver?s license number
            // pg_billto_dl_state-customer?s driver?s license state of issue
            // pg_billto_date_of_birth-customer?s date of birth (MM/DD/YYYY)
            // pg_entered_by-name or ID of the person entering the data; appears
            // in the Virtual Terminal transaction display window

            // payloadData+="pg_customer_ip_address="+InetAddress.getLocalHost().getHostAddress()+"\n";
            payloadData += "pg_customer_ip_address=1.1.11.1\n";
            payloadData += "pg_software_name=jBilling\n";
            payloadData += "pg_software_version=2.0.0\n";

            // pg_avs_method-specifies which AVS checks to perform on the
            // transaction (if any);
            // makes some optional fields required. See Appendix C for more
            // information on AVS.

            if (method == PAYMENT_METHOD_CC) {

                String ccType = getCCType(paymentInfo.getCreditCard().getType());
                payloadData += "ecom_payment_card_type=" + ccType + "\n";
                payloadData += "ecom_payment_card_name="
                        + paymentInfo.getCreditCard().getName() + "\n";
                payloadData += "ecom_payment_card_number="
                        + paymentInfo.getCreditCard().getNumber() + "\n";

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(paymentInfo.getCreditCard().getCcExpiry());

                payloadData += "ecom_payment_card_expdate_month="
                        + calendar.get(Calendar.MONTH) + "\n";
                payloadData += "ecom_payment_card_expdate_year="
                        + calendar.get(Calendar.YEAR) + "\n";
                payloadData += "ecom_payment_card_verification="
                        + paymentInfo.getCreditCard().getSecurityCode() + "\n";

            } else if (method == PAYMENT_METHOD_ACH) {

                /*
                 * "Ecom_Payment_Check_AccounT_Type=S",
                 * "Ecom_Payment_Check_Account= 14730",
                 * "Ecom_Payment_Check_TRN=021000021",
                 */

                String accType = "";
                payloadData += "Ecom_Payment_Check_TRN="
                        + paymentInfo.getAch().getAbaRouting() + "\n";
                payloadData += "Ecom_Payment_Check_Account="
                        + paymentInfo.getAch().getBankAccount() + "\n";

                if (paymentInfo.getAch().getAccountType() == 1) {
                    accType += "C";
                } else if (paymentInfo.getAch().getAccountType() == 2) {
                    accType += "S";
                } else {
                    log.error("unknown Account Type : "
                            + paymentInfo.getAch().getAccountType());
                    throw new PluggableTaskException("unknown Account Type");
                }

                payloadData += "Ecom_Payment_Check_AccounT_Type=" + accType
                        + "\n";

            } else if (method == PAYMENT_METHOD_CHEQUE) {
                payloadData += "Ecom_Payment_Check_TRN="
                        + paymentInfo.getAch().getAbaRouting() + "\n";
                payloadData += "Ecom_Payment_Check_Account="
                        + paymentInfo.getAch().getBankAccount() + "\n";
                payloadData += "Ecom_Payment_Check_Account_Type=C\n";
                payloadData += "ecom_payment_check_checkno=" + 
                        paymentInfo.getCheque().getNumber() + "\n";
                // payloadData += "PG_Entry_Class_Code=POS\n";
            }
        } catch (PluggableTaskException e) {
            log.error("PluggableTaskException", e);
            throw e;
        } catch (Exception e) {
            log.error("Exception", e);
            throw new PluggableTaskException(e);
        }
        payloadData += "endofdata\n";

        log.debug("charge data : " + payloadData);
        return payloadData;

    }

    /*
     * ecom_payment_card_type-credit card issuer from Table 5-Credit Card Issuer
     * Types below ecom_payment_card_name-cardholder name as it appears on the
     * card ecom_payment_card_number-card account number
     * ecom_payment_card_expdate_month-numeric month of expiration (Jan = 1)
     * ecom_payment_card_expdate_year-four-digit year of expiration
     * ecom_payment_card_verification-CVV2/verification number
     * pg_procurement_card-indicates procurement card transaction, requires
     * pg_sales_tax and pg_customer_acct_code fields
     * pg_customer_acct_code-accounting information for procurement card
     * transactions pg_cc_swipe_data-magstripe data from track one or two
     * pg_mail_or_phone_order-indicates mail order or phone order transaction
     * (as opposed to an Internet-based transaction)
     */

    private void validateParameters() throws PluggableTaskException {
        ensureGetParameter(PARAMETER_MERCHANT_ID);
        ensureGetParameter(PARAMETER_PASSWORD);
    }

    public void failure(Integer userId, Integer retry) {
    }

    /*
     * Credit Card 10 SALE Customer is charged 11 AUTH ONLY Authorization only,
     * CAPTURE transaction required 12 CAPTURE Completes AUTH ONLY transaction
     * 13 CREDIT Customer is credited 14 VOID Cancels non-settled transactions
     * 15 PRE-AUTH Customer charge approved from other source EFT 20 SALE
     * Customer is charged 21 AUTH ONLY Authorization only, CAPTURE transaction
     * required 22 CAPTURE Completes AUTH ONLY transaction 23 CREDIT Customer is
     * credited 24 VOID Cancels non-settled transactions 25 FORCE Customer
     * charged (no validation checks) 26 VERIFY ONLY Verification only, no
     * customer charge
     */

    public String getTransType(PaymentDTOEx paymentInfo, int method,
            boolean preAuth) throws PluggableTaskException {

        String transType = new String();

        if (paymentInfo.getIsRefund() == 1 || 
                paymentInfo.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            if (method == PAYMENT_METHOD_CC) {
                transType += CC_CRED;
            } else if (method == PAYMENT_METHOD_ACH) {
                transType += EFT_CRED;
            } else {
                log.error("Can't process refund for this method: " + method);
                throw new PluggableTaskException(
                        "Can't process refund for this method");
            }
        } else if (paymentInfo.getIsRefund() == 0) {

            switch (method) {
            case PAYMENT_METHOD_CC:
                if (preAuth) {
                    transType += CC_AUTH;
                } else {
                    transType += CC_SALE;
                }
                break;
            case PAYMENT_METHOD_ACH:
                if (preAuth) {
                    transType += EFT_AUTH;
                } else {
                    transType += EFT_SALE;
                }
                break;
            case PAYMENT_METHOD_CHEQUE:
                transType += EFT_VERIFY;
                break;
            default:
                log.error("Unknown payment method : " + method);
                throw new PluggableTaskException(
                        "Unknown payment method : Neither Credit Card, Cheque nor ACH ");
            }
        } else {
            log.error("Unknown transaction type : "
                    + paymentInfo.getIsRefund());
            throw new PluggableTaskException(
                    "Unknown transaction type : Neither Credit Card, Cheque nor ACH");
        }
        return transType;
    }

    public String getCCType(Integer type) {

        log.debug("credit card type: " + type);
        String ccType = "";
        switch (type) {

        case CC_TYPE_VISA:
            ccType += "VISA";
            break;

        case CC_TYPE_MASTER:
            ccType += "MAST";
            break;
        case CC_TYPE_AMEX:
            ccType += "AMER";
            break;

        case CC_TYPE_DISC:
            ccType += "DISC";
            break;

        case CC_TYPE_DINE:
            ccType += "DINE";
            break;

        case CC_TYPE_JCB:
            ccType += "JCB";
            break;

        default:
            log.error("Unknown credit card type: " + type);
            break;
        // throw new PluggableTaskException("Cannot find credit type");
        }
        return ccType;
    }

    private PaymentAuthorizationDTO processPGRequest(String data)
            throws PluggableTaskException {

        PaymentAuthorizationDTO dbRow = new PaymentAuthorizationDTO();

        String negRep = "";
        String autOut = "";
        
        try {
            BufferedReader br = callPG(data);
            String line = br.readLine();
            // log.debug("Response line: "+br);
            while (line != null) {
                // check for end of message
                if (line.equals("endofdata")) {
                    log.debug("ENDOFDATA");
                    break;
                }

                log.debug("Response line: " + line);
                // parse and display name/value pair
                int equalPos = line.indexOf('=');
                String name = line.substring(0, equalPos);
                String value = line.substring(equalPos + 1);
                log.debug(name + "=" + value);
                if (name.equals("pg_response_type")) {
                    dbRow.setCode1(value); // code if 1 it is ok
                }
                if (name.equals("pg_response_code")) {
                    dbRow.setCode2(value);
                }

                if (name.equals("pg_authorization_code")) {
                    dbRow.setApprovalCode(value);
                }
                if (name.equals("pg_response_description")) {
                    dbRow.setResponseMessage(value);
                }
                if (name.equals("pg_trace_number")) {
                    dbRow.setTransactionId(value);
                }
                // preAuth
                if (name.equals("pg_preauth_result")) {
                    dbRow.setCode3(value);
                }
                // Verify
                if(name.equals("pg_preauth_description")) {
                    autOut = value;
                }
                if(name.equals("pg_preauth_neg_report")) {
                    negRep = value;
                }

                // get next line
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            log.error("Error processing payment", e);
        }
        dbRow.setProcessor("PaymentsGateway");

        if (autOut != null && !"".equals(autOut.trim())) {
            dbRow.setResponseMessage(dbRow.getResponseMessage() + " - " + autOut);
        }
        
        if (negRep != null && !"".equals(negRep.trim()) && !negRep.equals(autOut)) {
            dbRow.setResponseMessage(dbRow.getResponseMessage() + " (" + negRep + ")");
        }
        
        return dbRow;
    }

    public boolean preAuth(PaymentDTOEx payment) throws PluggableTaskException {
        log = Logger.getLogger(PaymentsGatewayTask.class);

        log.error("Prcessing preAuth Reqquest");
        int method = 1; // CC
        boolean preAuth = true;
        if (payment.getCreditCard() != null) {
            method = PAYMENT_METHOD_CC;
        } else if (payment.getCheque() != null && payment.getAch() != null) {
            method = PAYMENT_METHOD_CHEQUE;
        } else if (payment.getAch() != null) {
            method = PAYMENT_METHOD_ACH;
        } else {
            // hmmm problem
            log.error("Can't process without a credit card or ach");
            throw new PluggableTaskException(
                    "Credit card/ACH not present in payment");
        }

        try {
            validateParameters();
            String data = getChargeData(payment, method, preAuth);
            PaymentAuthorizationDTO response = processPGRequest(data);

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

    /*
     * pay load for confirmPreAuth
     * 
     * Pg_merchant_id N8 M pg_password A20 M pg_transaction_type L M
     * pg_merchant_data_[1-9] A40 O pg_original_trace_number A36 M
     * pg_original_authorization_code A80 C (AC)
     */

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth,
            PaymentDTOEx paymentInfo) throws PluggableTaskException {

        log.error("Processing confirmPreAuth Request");
        boolean retValue = false;

        try {

            if (!RESPONSE_CODE_APPROVED.equals(auth.getCode1())) {
                log.error("Cannot process failed preAuth");
                return retValue;
            }
            payloadData += "pg_merchant_id="
                    + ensureGetParameter(PARAMETER_MERCHANT_ID) + "\n";
            payloadData += "pg_password="
                    + ensureGetParameter(PARAMETER_PASSWORD) + "\n";
            String transType = "";

            if (paymentInfo.getCreditCard() != null) {
                transType += CC_CAPT;

            } else if (paymentInfo.getAch() != null) {
                transType += EFT_CAPT;

            } else {
                // hmmm problem!!! this should not happen
                log.error("Can't process without a credit card or ach");
                throw new PluggableTaskException(
                        "Credit card/ACH not present in payment");
                // return false;
            }

            payloadData += "pg_transaction_type=" + transType + "\n";
            payloadData += "pg_original_trace_number="
                    + auth.getTransactionId() + "\n";
            payloadData += "pg_original_authorization_code="
                    + auth.getApprovalCode() + "\n";
            payloadData += "endofdata\n";

            BufferedReader br = callPG(payloadData);
            String line = br.readLine();

            while (line != null) {
                // check for end of message
                if (line.equals("endofdata")) {
                    log.debug("ENDOFDATA");
                    break;
                }

                log.debug("Response line: " + line);
                // parse and display name/value pair
                int equalPos = line.indexOf('=');
                String name = line.substring(0, equalPos);
                String value = line.substring(equalPos + 1);

                if (name.equals("pg_response_type")) {
                    if (RESPONSE_CODE_APPROVED.equals(value)) {
                        paymentInfo.setPaymentResult(new PaymentResultDAS()
                                .find(Constants.RESULT_OK));
                        log.debug("preAuth result is ok");
                        retValue = false;
                    } else {

                        paymentInfo.setPaymentResult(new PaymentResultDAS()
                                .find(Constants.RESULT_FAIL));
                        log.debug("preAuth result is failed");
                        retValue = true;
                    }

                    auth.setCode1(value);

                }
                if (name.equals("pg_response_code")) {
                    auth.setCode2(value);
                }

                if (name.equals("pg_authorization_code")) {
                    auth.setApprovalCode(value);
                }
                if (name.equals("pg_response_description")) {
                    auth.setResponseMessage(value);
                }
                if (name.equals("pg_trace_number")) {
                    auth.setTransactionId(value);
                }

                // get next line
                line = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            log.error("error trying to confirm pre-authorize", e);
            throw new PluggableTaskException(e);
        }

        return retValue;

    }

    private BufferedReader callPG(String data) throws PluggableTaskException {

        String host = null;
        int port;
        if ("true".equals(getOptionalParameter(PARAMETER_TEST, "false"))) {
            host = super.ensureGetParameter(PARAMETER_TEST_HOST);
            port = Integer.valueOf(
                    super.ensureGetParameter(PARAMETER_TEST_PORT)).intValue();
            log.debug("Running task in test mode!");
        } else {
            host = super.ensureGetParameter(PARAMETER_HOST);
            port = Integer.valueOf(super.ensureGetParameter(PARAMETER_PORT))
                    .intValue();
        }

        try {

            // set up secure connection w/JSSE
            java.security.Security
                    .addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            SocketFactory factory = (SocketFactory) SSLSocketFactory
                    .getDefault();

            SSLSocket s = (SSLSocket) factory.createSocket(host, port);
            log.debug("connected to :" + host + "on " + port);
            s.setEnabledCipherSuites(s.getSupportedCipherSuites());
            log.debug("cipher=" + s.getSession().getCipherSuite());

            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // write the content and be sure to flush
            log.debug("Writing data to PG " + data);
            dos.writeBytes(data);
            dos.flush();

            // read the response
            BufferedReader br = new BufferedReader(new InputStreamReader(s
                    .getInputStream()));

            return br;

        } catch (Exception e) {
            log.error("Error processing payment", e);
            return null;
        }
        // return null;
    }
}
