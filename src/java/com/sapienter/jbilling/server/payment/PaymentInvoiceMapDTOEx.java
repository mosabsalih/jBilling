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

package com.sapienter.jbilling.server.payment;

import java.math.BigDecimal;
import java.util.Date;

import com.sapienter.jbilling.server.payment.db.PaymentInvoiceMapDTO;



public class PaymentInvoiceMapDTOEx extends PaymentInvoiceMapDTO {
    private Integer paymentId;
    private Integer invoiceId;
    private Integer currencyId;

    public PaymentInvoiceMapDTOEx(Integer id, BigDecimal amount, Date create) {
        super(id, amount, create);
    }
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }
    public Integer getCurrencyId() {
        return currencyId;
    }
    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }
    
    public String toString() {
        return "id = " + getId() +
                " paymentId=" + paymentId + 
                " invoiceId=" + invoiceId +
                " currencyId=" + currencyId +
                " amount=" + getAmount() +
                " date=" + getCreateDatetime();
    }
}
