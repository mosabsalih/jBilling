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

/*
 * Created on Jan 22, 2005
 *
 */
package com.sapienter.jbilling.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
import com.sapienter.jbilling.server.util.Context;

/**
 * @author Emil
 *
 */
public class ExternalCallbackServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ExternalCallbackServlet.class);
    public void doPost(HttpServletRequest request, 
            HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            LOG.debug("callback received");
            
            if (request.getParameter("caller") == null ||
                    !request.getParameter("caller").equals("paypal")) {
                LOG.debug("caller not supported");
                return;
            }
            
            if (!verifyTransactionType(request.getParameter("txn_type"))) {
                LOG.debug("transaction is type " +request.getParameter("txn_type") + " ignoring");
                return;
            }
            
            // go over the parameters, making my string for the validation
            // call to paypal
            String validationStr = "cmd=_notify-validate";
            Enumeration parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String parameter = (String) parameters.nextElement();
                String value = request.getParameter(parameter);
                LOG.debug("parameter : " + parameter + 
                        " value : " + value);
                validationStr = validationStr + "&" + parameter + "=" + 
                    URLEncoder.encode(value);
            }
            
            LOG.debug("About to call paypal for validation.  Request" + validationStr);
            URL u = new URL("https://www.paypal.com/cgi-bin/webscr");
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            PrintWriter pw = new PrintWriter(uc.getOutputStream());
            pw.println(validationStr);
            pw.close();
    
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(uc.getInputStream()));
            String res = in.readLine();
            in.close();
    
            //check notification validation
            LOG.debug("Validation result is " + res);
            if(res.equals("VERIFIED")) {
            //if(res.equals("INVALID")) { // only for testing
                LOG.debug("ok");
                String invoiceNumber = request.getParameter("invoice");
                String paymentStatus = request.getParameter("payment_status");
                String paymentAmount = request.getParameter("mc_gross");
                String paymentCurrency = request.getParameter("mc_currency");
                String receiverEmail = request.getParameter("receiver_email");
                String userEmail = request.getParameter("payer_email");
                String userIdStr = request.getParameter("custom");
                
                if (paymentStatus == null || !paymentStatus.equalsIgnoreCase(
                        "completed")) {
                    LOG.debug("payment status is " + paymentStatus + " Rejecting");
                } else { 
                    try {
                        IPaymentSessionBean paymentSession = 
                                (IPaymentSessionBean) Context.getBean(
                                Context.Name.PAYMENT_SESSION);
                        Integer invoiceId = getInt(invoiceNumber);
                        BigDecimal amount = new BigDecimal(paymentAmount);
                        Integer userId = getInt(userIdStr);
                        Boolean result = paymentSession.processPaypalPayment(invoiceId, receiverEmail, amount,
                                                                             paymentCurrency, userId, userEmail);
                        
                        LOG.debug("Finished callback with result " + result);
                    } catch (Exception e) {
                        LOG.error("Exception processing a paypal callback ", e);
                    }
                   
                }
            }
            else if(res.equals("INVALID")) {
                LOG.debug("invalid");
            }
            else {
                LOG.debug("error");
            }
            LOG.debug("done callback");
        } catch (Exception e) {
            LOG.error("Error processing external callback", e);
        }
    }
    
    private Integer getInt(String str) {
        Integer retValue = null;
        if (str != null && str.length() > 0) {
            try {
                retValue = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid int field." + str + " - " + e.getMessage());
            }
        }
        return retValue;
    }
    
    private boolean verifyTransactionType(String type) {
        if (type == null || type.length() == 0) {
            return true;
        } else {
            if (type.equals("subscr_signup") ||
                    type.equals("subscr_cancel") ||
                    type.equals("subscr_failed") ||
                    type.equals("subscr_eot") ||
                    type.equals("subscr_modify")) {
                return false;
            }
            return true;
        }
    }
}
