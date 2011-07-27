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
 * Created on Feb 4, 2005
 *
 */
package com.sapienter.jbilling.server.user;

import java.io.Serializable;

import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;

/**
 * @author Emil
 */
public class CreateResponseWS implements Serializable {
    private Integer userId = null;
    private Integer orderId = null;
    private Integer invoiceId = null;
    private Integer paymentId = null;
    private PaymentAuthorizationDTOEx paymentResult = null;
    
    public Integer getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }
    public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public Integer getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
    public PaymentAuthorizationDTOEx getPaymentResult() {
        return paymentResult;
    }
    public void setPaymentResult(PaymentAuthorizationDTOEx paymentResult) {
        this.paymentResult = paymentResult;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String toString() {
        return "user id = " + userId + " invoice id = " + invoiceId +
                " order id = " + orderId + " paymentId = " + paymentId +
                " payment result = " + paymentResult;
    }
}
