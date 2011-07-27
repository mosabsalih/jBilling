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
package com.sapienter.jbilling.server.payment.db;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PaymentInvoiceId  implements java.io.Serializable {


     private Integer paymentId;
     private Integer invoiceId;
     private Double amount;
     private Date createDatetime;
     private int id;

    public PaymentInvoiceId() {
    }

    
    public PaymentInvoiceId(int id) {
        this.id = id;
    }
    public PaymentInvoiceId(Integer paymentId, Integer invoiceId, Double amount, Date createDatetime, int id) {
       this.paymentId = paymentId;
       this.invoiceId = invoiceId;
       this.amount = amount;
       this.createDatetime = createDatetime;
       this.id = id;
    }
   

    @Column(name="payment_id")
    public Integer getPaymentId() {
        return this.paymentId;
    }
    
    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    @Column(name="invoice_id")
    public Integer getInvoiceId() {
        return this.invoiceId;
    }
    
    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Column(name="amount", precision=17, scale=17)
    public Double getAmount() {
        return this.amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Column(name="create_datetime", length=29)
    public Date getCreateDatetime() {
        return this.createDatetime;
    }
    
    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
         if ( (other == null ) ) return false;
         if ( !(other instanceof PaymentInvoiceId) ) return false;
         PaymentInvoiceId castOther = ( PaymentInvoiceId ) other; 
         
         return ( (this.getPaymentId()==castOther.getPaymentId()) || ( this.getPaymentId()!=null && castOther.getPaymentId()!=null && this.getPaymentId().equals(castOther.getPaymentId()) ) )
 && ( (this.getInvoiceId()==castOther.getInvoiceId()) || ( this.getInvoiceId()!=null && castOther.getInvoiceId()!=null && this.getInvoiceId().equals(castOther.getInvoiceId()) ) )
 && ( (this.getAmount()==castOther.getAmount()) || ( this.getAmount()!=null && castOther.getAmount()!=null && this.getAmount().equals(castOther.getAmount()) ) )
 && ( (this.getCreateDatetime()==castOther.getCreateDatetime()) || ( this.getCreateDatetime()!=null && castOther.getCreateDatetime()!=null && this.getCreateDatetime().equals(castOther.getCreateDatetime()) ) )
 && (this.getId()==castOther.getId());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getPaymentId() == null ? 0 : this.getPaymentId().hashCode() );
         result = 37 * result + ( getInvoiceId() == null ? 0 : this.getInvoiceId().hashCode() );
         result = 37 * result + ( getAmount() == null ? 0 : this.getAmount().hashCode() );
         result = 37 * result + ( getCreateDatetime() == null ? 0 : this.getCreateDatetime().hashCode() );
         result = 37 * result + this.getId();
         return result;
   }   


}


