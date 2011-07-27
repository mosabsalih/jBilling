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

package com.sapienter.jbilling.server.pluggableTask;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.item.CurrencyBL;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.CreditCardBL;
import com.sapienter.jbilling.server.util.Constants;
import java.util.ArrayList;

public class PaymentAuthorizeNetTask extends PluggableTask
            implements PaymentTask {

    // pluggable task parameters names
    public static final ParameterDescription PARAMETER_LOGIN = 
        new ParameterDescription("login", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_TRANSACTION = 
        new ParameterDescription("transaction", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_TEST = 
        new ParameterDescription("test", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_AVS = 
        new ParameterDescription("submit_avs", false, ParameterDescription.Type.STR);
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_LOGIN); 
        descriptions.add(PARAMETER_TRANSACTION); 
        descriptions.add(PARAMETER_TEST); 
        descriptions.add(PARAMETER_AVS); 
    }
    
    //private static final String url = "https://certification.authorize.net/gateway/transact.dll";
    private static final String url = "https://secure.authorize.net/gateway/transact.dll";
    private static final int timeOut = 10000; // in millisec
    
    private static final Logger LOG = Logger.getLogger(PaymentAuthorizeNetTask.class);
    
    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#process(com.sapienter.betty.server.payment.PaymentDTOEx)
     */
    public boolean process(PaymentDTOEx paymentInfo) 
            throws PluggableTaskException{
        boolean retValue = false;
        boolean isTest = false;
        
        // authorize.net is not available for payouts to partners
        if (paymentInfo.getPayoutId() != null) {
            return true; // means that the next task should be processed
        }
        /*
         * Since this communicates via https, java needs a keystore with the client's key
         * in order to trust the authroize.net server.
         * The java properties javax.net.ssl.trustStore javax.net.ssl.trustStorePassword
         * have to be properly set (and the keystore present).
         * This can be done as a parameter to the JVM (-D), or right here:
            System.setProperty("javax.net.ssl.trustStore", "/dir1/keystore/client.keystore");
            System.setProperty("javax.net.ssl.trustStorePassword", "myPassword");
        */
        try {
            int method = -1; // 1 cc , 2 ach
            
            if (paymentInfo.getCreditCard() == null &&
                    paymentInfo.getAch() == null) {
                LOG.error("Can't process without a credit card or ach");
                throw new TaskException("Credit card/ACH not present in payment");
            }
            
            if (paymentInfo.getCreditCard() != null) {
                method = 1;
            }
            if (paymentInfo.getAch() != null) {
                method = 2;
            }
            
            if (paymentInfo.getCreditCard() != null &&
                    paymentInfo.getAch() != null) {
                LOG.warn("Both cc and ach are present");
                method = 2; // default to ach (cheaper)
            }
            
            
            
            if (paymentInfo.getIsRefund() == 1 &&
                    (paymentInfo.getPayment() == null ||
                        paymentInfo.getPayment().getAuthorization() ==null)) {
                LOG.error("Can't process refund without a payment with an" +
                        " authorization record");
                throw new TaskException("Refund without previous " +
                        "authorization");
            } 
            
            String expiry = null;
            if (method == 1) {
                expiry = CreditCardBL.get4digitExpiry(
                        paymentInfo.getCreditCard());
            }
            
            String login = (String) parameters.get(PARAMETER_LOGIN.getName());
            String transaction = (String) parameters.get(PARAMETER_TRANSACTION.getName());
            
            if (login == null || login.length() == 0 || transaction == null ||
                    transaction.length() == 0) {
                throw new TaskException("invalid parameters");
            }
            
            String testStr = (String) parameters.get(PARAMETER_TEST.getName());
            if (testStr != null) {
                isTest = true;
            }
            
            // find the currency code of this payment
            CurrencyBL currencyBL = new CurrencyBL(
                    paymentInfo.getCurrency().getId());
            String currencyCode = currencyBL.getEntity().getCode();
            
            LOG.debug("making call with " + login + " " + transaction + 
                    " " + expiry);
            
            NameValuePair[] data;
            if (method == 1) {
                if (paymentInfo.getIsRefund() == 0) {
                    data = getChargeData(login, transaction, isTest, 
                            paymentInfo.getAmount(), 
                            paymentInfo.getCreditCard().getNumber(), expiry,
                            currencyCode, true, paymentInfo.getId());
                } else {
                    data = getRefundData(login, transaction, isTest, 
                            paymentInfo.getAmount(), 
                            paymentInfo.getCreditCard().getNumber(), expiry,
                            paymentInfo.getPayment().getAuthorization().
                                getTransactionId());
                }
            } else {
                if (paymentInfo.getIsRefund() == 0) {
                    data = getACHChargeData(login, transaction, isTest, 
                            paymentInfo.getAmount(), 
                            paymentInfo.getAch().getAbaRouting(),
                            paymentInfo.getAch().getBankAccount(),
                            paymentInfo.getAch().getAccountType(),
                            paymentInfo.getAch().getBankName(),
                            paymentInfo.getAch().getAccountName(),
                            currencyCode);
                } else {
                    data = getACHRefundData(login, transaction, isTest, 
                            paymentInfo.getAmount(),
                            paymentInfo.getAch().getAbaRouting(),
                            paymentInfo.getAch().getBankAccount(),
                            paymentInfo.getAch().getAccountType(),
                            paymentInfo.getAch().getBankName(),
                            paymentInfo.getAch().getAccountName(),
                            paymentInfo.getPayment().getAuthorization().
                                getTransactionId());
                }               
            }
            
            // see if AVS info has to be included
            String doAvs = (String) parameters.get(PARAMETER_AVS.getName());
            if (doAvs != null && doAvs.equals("true")) {
                data = addAVSFields(paymentInfo.getUserId(), data);
                LOG.debug("returning after avs " + data);
            }
            
            AuthorizeNetResponseDTO response = makeCall(data);
            paymentInfo.setAuthorization(response.getPaymentAuthorizationDTO());
            
            // the result of this request goes in the dto
            if (Integer.valueOf(response.getPaymentAuthorizationDTO().
                    getCode1()).intValue() == 1) {
                paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
                LOG.debug("result is ok");
            } else {
                // there are actually two other codes, 2 is decalined, but
                // 3 is just 'error' may be for a 3 it should just return true
                // to try another processor. Now we only do that for exceptions
                paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_FAIL));
                LOG.debug("result is fail");
            }
            
            // now create the db row with the results of this authorization call
            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            // set the processor
            response.getPaymentAuthorizationDTO().setProcessor("Authorize.net");
            bl.create(response.getPaymentAuthorizationDTO(), paymentInfo.getId());
            
        } catch (HttpException e) {
            LOG.warn("Http exception when calling Authorize.net", e);
            paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));            
            retValue = true;
        } catch (IOException e) {
            LOG.warn("IO exception when calling Authorize.net", e);
            paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));            
            retValue = true;
        } catch (Exception e) {
            LOG.error("Exception", e);
            throw new PluggableTaskException(e);
        }
        
        // let's make this usefull for testing too
        if (isTest) {
            LOG.debug("Running Authorize.net task in test mode!");
            Random rand = new Random();
            paymentInfo.setPaymentResult(new PaymentResultDAS().find(new Integer(rand.nextInt(3) + 1)));
            retValue = false;
        }
        LOG.debug("returning "  + retValue);
        return retValue;
    }

    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#failure(com.sapienter.betty.interfaces.UserEntityLocal, int)
     */
    public void failure(Integer userId, Integer retry) {

    }

    /*
    public static void main(String[] args) {
        try {
            AuthorizeNetResponseDTO response = makeCall(getChargeData("alphat", 
                    "xxx", true, 
                    new Float(10.1), "4007000000027", "0505", "USD"));
            System.out.println("got dto:" + response);
            System.out.println("now the code is " + Integer.valueOf(response.getPaymentAuthorizationDTO().
            getCode1()).intValue());
        } catch (Exception e) {
            System.err.println("Got exception " + e);
        }
    }
    */
  
    public NameValuePair[] getChargeData(String login, 
            String transaction, boolean test, BigDecimal amount, String cc_number,
            String cc_expiry, String currencyCode, boolean isCharge,
            Integer paymentId) {

        NameValuePair[] data = {
            new NameValuePair("x_Version", "3.1"),
            new NameValuePair("x_Delim_Data", "TRUE"),
            new NameValuePair("x_relay_response", "False"),
            new NameValuePair("x_Login", login),
            new NameValuePair("x_Tran_Key", transaction),
            new NameValuePair("x_Amount", amount.toString()),
            new NameValuePair("x_Card_Num", cc_number),
            new NameValuePair("x_Exp_Date", cc_expiry),
            new NameValuePair("x_Type", isCharge ? "AUTH_CAPTURE" : "AUTH_ONLY"),
            new NameValuePair("x_Test_Request", test ? "TRUE" : "FALSE"),
            new NameValuePair("x_currency_code", currencyCode),
            new NameValuePair("x_invoice_num", paymentId.toString()),
            new NameValuePair("x_description", "Invoice number is payment ID")
        };
        
        return data;
    }


    // Since now a refund is done only when linked to a previous payment, 
    // I assume that the currency is not required
    private NameValuePair[] getRefundData(String login, 
            String transaction, boolean test, BigDecimal amount, String cc_number,
            String cc_expiry, String transactionId) {
    
        NameValuePair[] data = {
            new NameValuePair("x_Version", "3.1"),
            new NameValuePair("x_Delim_Data", "TRUE"),
            new NameValuePair("x_relay_response", "False"),
            new NameValuePair("x_Login", login),
            new NameValuePair("x_Tran_Key", transaction),
            new NameValuePair("x_Amount", amount.toString()),
            new NameValuePair("x_Card_Num", cc_number),
            new NameValuePair("x_Exp_Date", cc_expiry),
            new NameValuePair("x_Type", "CREDIT"),
            new NameValuePair("x_Test_Request", test ? "TRUE" : "FALSE"),
            new NameValuePair("x_Trans_ID", transactionId)
        };
            
        return data;
    }

    private NameValuePair[] getACHChargeData(String login, 
            String transaction, boolean test, BigDecimal amount,String aba,
            String account,
            Integer type, String bank, String name, String currencyCode) {

        NameValuePair[] data = {
            new NameValuePair("x_Version", "3.1"),
            new NameValuePair("x_Delim_Data", "TRUE"),
            new NameValuePair("x_relay_response", "False"),
            new NameValuePair("x_Login", login),
            new NameValuePair("x_Tran_Key", transaction),
            new NameValuePair("x_Amount", amount.toString()),
            new NameValuePair("x_bank_aba_code", aba),
            new NameValuePair("x_bank_acct_num", account),
            new NameValuePair("x_bank_acct_type", type.intValue() == 1 ? 
                    "CHECKING" : "SAVINGS"),
            new NameValuePair("x_bank_name", bank),
            new NameValuePair("x_bank_acct_name", name),
            new NameValuePair("x_Type", "AUTH_CAPTURE"),
            new NameValuePair("x_Test_Request", test ? "TRUE" : "FALSE"),
            new NameValuePair("x_currency_code", currencyCode)
        };
        
        return data;
    }

    private NameValuePair[] getACHRefundData(String login, 
            String transaction, boolean test, BigDecimal amount, String aba, 
            String account,
            Integer type, String bank, String name,String transactionId) {

        NameValuePair[] data = {
            new NameValuePair("x_Version", "3.1"),
            new NameValuePair("x_Delim_Data", "TRUE"),
            new NameValuePair("x_relay_response", "False"),
            new NameValuePair("x_Login", login),
            new NameValuePair("x_Tran_Key", transaction),
            new NameValuePair("x_Amount", amount.toString()),
            new NameValuePair("x_bank_aba_code", aba),
            new NameValuePair("x_bank_acct_num", account),
            new NameValuePair("x_bank_acct_type", type.intValue() == 1 ? 
                    "CHECKING" : "SAVINGS"),
            new NameValuePair("x_bank_name", bank),
            new NameValuePair("x_bank_acct_name", name),
            new NameValuePair("x_Type", "CREDIT"),
            new NameValuePair("x_Test_Request", test ? "TRUE" : "FALSE"),
            new NameValuePair("x_Trans_ID", transactionId)
        };
        
        return data;
    }

    private NameValuePair[] addAVSFields(Integer userId, NameValuePair[] fields) {
        try {
            List result = new ArrayList();
            for (int f = 0; f < fields.length; f++) {
                result.add(fields[f]);
            }
            ContactBL contact = new ContactBL();
            contact.set(userId);
            considerField(result, contact.getEntity().getFirstName(),
                    "x_first_name");
            considerField(result, contact.getEntity().getLastName(),
                    "x_last_name");
            considerField(result, contact.getEntity().getAddress1(),
                    "x_address");
            considerField(result, contact.getEntity().getCity(),
                    "x_city");
            considerField(result, contact.getEntity().getStateProvince(),
                    "x_state");
            considerField(result, contact.getEntity().getPostalCode(),
                    "x_zip");
            considerField(result, contact.getEntity().getCountryCode(),
                    "x_country");
            NameValuePair[] retValue = new NameValuePair[result.size()];
            retValue  = (NameValuePair[]) result.toArray(retValue);
            return retValue;
        } catch (Exception e) {
            LOG.warn("Exception when trying to add the AVS fields", e);
            return fields;
        }
    }
    
    private void considerField(List fields, String dbField, 
            String aNetField) {
        if (dbField != null && dbField.length() > 0) {
            NameValuePair field = new NameValuePair(aNetField, dbField);
            fields.add(field);
        }
    }
    
    public AuthorizeNetResponseDTO makeCall(NameValuePair[] data) 
            throws HttpException, IOException {
        Credentials creds = null;
//            creds = new UsernamePasswordCredentials(args[1], args[2]);

        //create a singular HttpClient object
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(timeOut);
        /*
        for (int f = 0; f < data.length; f++) {
            log.debug("Data=" + data[f].getName() + " " + data[f].getValue());    
        }
        */
        //set the default credentials
        if (creds != null) {
            client.getState().setCredentials(null, null, creds);
        }

        PostMethod post = new PostMethod(url);
        
        post.setRequestBody(data);

        //execute the method
        String responseBody = null;
        client.executeMethod(post);
        responseBody = post.getResponseBodyAsString();

        LOG.debug("Got response:" + responseBody);
        //write out the response body
        AuthorizeNetResponseDTO dto = new AuthorizeNetResponseDTO(
                responseBody);
        //clean up the connection resources
        post.releaseConnection();
        post.recycle();

        return dto;
    }
    
    /**
     * The argument 'payment' has to have
     *   - currency
     *   - amount
     *   - credit card
     *   - the id of the existing payment row
     * @return The information with the results of the pre-authorization, or null if the was
     * a problem, such as the processor being unavailable
     */
    public boolean preAuth(PaymentDTOEx payment) 
            throws PluggableTaskException {
        String login = (String) parameters.get(PARAMETER_LOGIN.getName());
        String transaction = (String) parameters.get(PARAMETER_TRANSACTION.getName());
        
        if (login == null || login.length() == 0 || transaction == null ||
                transaction.length() == 0) {
            throw new PluggableTaskException("invalid parameters");
        }
        
        try {
            CurrencyBL currencyBL = new CurrencyBL(payment.getCurrency().getId());
            String currencyCode = currencyBL.getEntity().getCode();

            NameValuePair data[] = getChargeData(login, transaction, false, 
                    payment.getAmount(), payment.getCreditCard().getNumber(), 
                    CreditCardBL.get4digitExpiry(payment.getCreditCard()),
                    currencyCode, false, new Integer(0));
            
            AuthorizeNetResponseDTO response = makeCall(data);
            
            // save this authorization into the DB
            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            PaymentAuthorizationDTO  authDto = new PaymentAuthorizationDTO(
                    response.getPaymentAuthorizationDTO());
            authDto.setProcessor("Authorize.net");
            bl.create(authDto, payment.getId());
            // since this is just an authorization, without a related payment
            // we leave it like this, no links to the payment table
            
            payment.setAuthorization(authDto);
            return false;
        } catch (Exception e) {
            LOG.info("error trying to pre-authorize", e);
            return true;
        } 
    }
    
    public boolean confirmPreAuth(PaymentAuthorizationDTO auth, 
            PaymentDTOEx paymentInfo) throws PluggableTaskException {
        // TODO Auto-generated method stub
        // Transaction type has to be PRIOR_AUTH_CAPTURE
        // the transactio id of the original authorization has to be included
        // along with the amount
        return true;
    }
    
}
