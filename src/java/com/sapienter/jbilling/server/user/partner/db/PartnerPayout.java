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
package com.sapienter.jbilling.server.user.partner.db;


import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.sapienter.jbilling.server.payment.db.PaymentDTO;

@Entity
@TableGenerator(
        name="partner_payout_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="partner_payout",
        allocationSize=10
)
@Table(name="partner_payout")
public class PartnerPayout  implements java.io.Serializable {

    private int id;
    private PaymentDTO payment;
    private Partner partner;
    private Date startingDate;
    private Date endingDate;
    private BigDecimal paymentsAmount;
    private BigDecimal refundsAmount;
    private BigDecimal balanceLeft;
    private int versionNum;

    private Integer paymentId = null;
    private Integer partnerId = null;

    public PartnerPayout() {
    }

    public PartnerPayout(int id, Date startingDate, Date endingDate, BigDecimal paymentsAmount,
                         BigDecimal refundsAmount, BigDecimal balanceLeft) {
        this.id = id;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.paymentsAmount = paymentsAmount;
        this.refundsAmount = refundsAmount;
        this.balanceLeft = balanceLeft;
    }

    public PartnerPayout(int id, PaymentDTO payment, Partner partner, Date startingDate, Date endingDate,
                         BigDecimal paymentsAmount, BigDecimal refundsAmount, BigDecimal balanceLeft) {
        this.id = id;
        this.payment = payment;
        this.partner = partner;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.paymentsAmount = paymentsAmount;
        this.refundsAmount = refundsAmount;
        this.balanceLeft = balanceLeft;
    }

    @Id  @GeneratedValue(strategy=GenerationType.TABLE, generator="partner_payout_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="payment_id")
    public PaymentDTO getPayment() {
        return this.payment;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="partner_id")
    public Partner getPartner() {
        return this.partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    @Column(name="starting_date", nullable=false, length=13)
    public Date getStartingDate() {
        return this.startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    @Column(name="ending_date", nullable=false, length=13)
    public Date getEndingDate() {
        return this.endingDate;
    }

    public void setEndingDate(Date endingDate) {
        this.endingDate = endingDate;
    }

    @Column(name="payments_amount", nullable=false, precision=17, scale=17)
    public BigDecimal getPaymentsAmount() {
        return this.paymentsAmount;
    }

    public void setPaymentsAmount(BigDecimal paymentsAmount) {
        this.paymentsAmount = paymentsAmount;
    }

    @Column(name="refunds_amount", nullable=false, precision=17, scale=17)
    public BigDecimal getRefundsAmount() {
        return this.refundsAmount;
    }

    public void setRefundsAmount(BigDecimal refundsAmount) {
        this.refundsAmount = refundsAmount;
    }

    @Column(name="balance_left", nullable=false, precision=17, scale=17)
    public BigDecimal getBalanceLeft() {
        return this.balanceLeft;
    }

    public void setBalanceLeft(BigDecimal balanceLeft) {
        this.balanceLeft = balanceLeft;
    }

    @Version
    @Column(name="OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Transient
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    @Transient
    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    @Transient
    public BigDecimal getTotal() {
        return getBalanceLeft().add(getPayment().getAmount());
    }

    public void touch() {
        getStartingDate();
        if (getPayment() != null) {
            getPayment().getCurrency();
        }
    }
}


