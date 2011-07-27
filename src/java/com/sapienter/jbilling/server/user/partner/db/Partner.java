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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sapienter.jbilling.server.process.db.PeriodUnitDTO;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

@Entity
@TableGenerator(
        name = "partner_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "partner",
        allocationSize = 10
)
@Table(name = "partner")
public class Partner implements java.io.Serializable {

    private Set<PartnerRange> ranges = new HashSet<PartnerRange>(0);

    private Integer relatedClerkUserId = null;

    private int id;

    private PeriodUnitDTO periodUnitDTO;
    private UserDTO baseUserByUserId;
    private UserDTO baseUserByRelatedClerk;
    private CurrencyDTO currencyDTO;
    private BigDecimal balance;
    private BigDecimal totalPayments;
    private BigDecimal totalRefunds;
    private BigDecimal totalPayouts;
    private BigDecimal percentageRate;
    private BigDecimal referralFee;
    private int oneTime;
    private int periodValue;
    private Date nextPayoutDate;
    private BigDecimal duePayout;
    private int automaticProcess;

    private Set<PartnerPayout> partnerPayouts = new HashSet<PartnerPayout>(0);

    private Set<CustomerDTO> customers = new HashSet<CustomerDTO>(0);
    private int versionNum;

    public Partner() {
    }

    public Partner(int id) {
        this.id = id;
    }

    public Partner(int id, PeriodUnitDTO periodUnitDTO, BigDecimal balance, BigDecimal totalPayments,
                   BigDecimal totalRefunds, BigDecimal totalPayouts, int oneTime, int periodValue,
                   Date nextPayoutDate, int automaticProcess) {
        this.id = id;
        this.periodUnitDTO = periodUnitDTO;
        this.balance = balance;
        this.totalPayments = totalPayments;
        this.totalRefunds = totalRefunds;
        this.totalPayouts = totalPayouts;
        this.oneTime = oneTime;
        this.periodValue = periodValue;
        this.nextPayoutDate = nextPayoutDate;
        this.automaticProcess = automaticProcess;
    }

    public Partner(int id, PeriodUnitDTO periodUnitDTO, UserDTO baseUserByUserId,
                   UserDTO baseUserByRelatedClerk, CurrencyDTO currencyDTO, BigDecimal balance,
                   BigDecimal totalPayments, BigDecimal totalRefunds, BigDecimal totalPayouts, BigDecimal percentageRate,
                   BigDecimal referralFee, int oneTime, int periodValue, Date nextPayoutDate,
                   BigDecimal duePayout, int automaticProcess, Set<PartnerPayout> partnerPayouts,
                   Set<CustomerDTO> customers) {
        this.id = id;
        this.periodUnitDTO = periodUnitDTO;
        this.baseUserByUserId = baseUserByUserId;
        this.baseUserByRelatedClerk = baseUserByRelatedClerk;
        this.currencyDTO = currencyDTO;
        this.balance = balance;
        this.totalPayments = totalPayments;
        this.totalRefunds = totalRefunds;
        this.totalPayouts = totalPayouts;
        this.percentageRate = percentageRate;
        this.referralFee = referralFee;
        this.oneTime = oneTime;
        this.periodValue = periodValue;
        this.nextPayoutDate = nextPayoutDate;
        this.duePayout = duePayout;
        this.automaticProcess = automaticProcess;
        this.partnerPayouts = partnerPayouts;
        this.customers = customers;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "partner_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_unit_id", nullable = false)
    public PeriodUnitDTO getPeriodUnit() {
        return this.periodUnitDTO;
    }

    public void setPeriodUnit(PeriodUnitDTO periodUnitDTO) {
        this.periodUnitDTO = periodUnitDTO;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public UserDTO getBaseUser() {
        return this.baseUserByUserId;
    }

    public void setBaseUser(UserDTO baseUserByUserId) {
        this.baseUserByUserId = baseUserByUserId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_clerk")
    public UserDTO getBaseUserByRelatedClerk() {
        return this.baseUserByRelatedClerk;
    }

    public void setBaseUserByRelatedClerk(UserDTO baseUserByRelatedClerk) {
        this.baseUserByRelatedClerk = baseUserByRelatedClerk;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_currency_id")
    public CurrencyDTO getFeeCurrency() {
        return this.currencyDTO;
    }

    public void setFeeCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }

    @Column(name = "balance", nullable = false, precision = 17, scale = 17)
    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Column(name = "total_payments", nullable = false, precision = 17, scale = 17)
    public BigDecimal getTotalPayments() {
        return this.totalPayments;
    }

    public void setTotalPayments(BigDecimal totalPayments) {
        this.totalPayments = totalPayments;
    }

    @Column(name = "total_refunds", nullable = false, precision = 17, scale = 17)
    public BigDecimal getTotalRefunds() {
        return this.totalRefunds;
    }

    public void setTotalRefunds(BigDecimal totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    @Column(name = "total_payouts", nullable = false, precision = 17, scale = 17)
    public BigDecimal getTotalPayouts() {
        return this.totalPayouts;
    }

    public void setTotalPayouts(BigDecimal totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    @Column(name = "percentage_rate", precision = 17, scale = 17)
    public BigDecimal getPercentageRate() {
        return this.percentageRate;
    }

    public void setPercentageRate(BigDecimal percentageRate) {
        this.percentageRate = percentageRate;
    }

    @Column(name = "referral_fee", precision = 17, scale = 17)
    public BigDecimal getReferralFee() {
        return this.referralFee;
    }

    public void setReferralFee(BigDecimal referralFee) {
        this.referralFee = referralFee;
    }

    @Column(name = "one_time", nullable = false)
    public int getOneTime() {
        return this.oneTime;
    }

    public void setOneTime(int oneTime) {
        this.oneTime = oneTime;
    }

    @Column(name = "period_value", nullable = false)
    public int getPeriodValue() {
        return this.periodValue;
    }

    public void setPeriodValue(int periodValue) {
        this.periodValue = periodValue;
    }

    @Column(name = "next_payout_date", nullable = false, length = 13)
    public Date getNextPayoutDate() {
        return this.nextPayoutDate;
    }

    public void setNextPayoutDate(Date nextPayoutDate) {
        this.nextPayoutDate = nextPayoutDate;
    }

    @Column(name = "due_payout", precision = 17, scale = 17)
    public BigDecimal getDuePayout() {
        return this.duePayout;
    }

    public void setDuePayout(BigDecimal duePayout) {
        this.duePayout = duePayout;
    }

    @Column(name = "automatic_process", nullable = false)
    public int getAutomaticProcess() {
        return this.automaticProcess;
    }

    public void setAutomaticProcess(int automaticProcess) {
        this.automaticProcess = automaticProcess;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "partner")
    public Set<PartnerPayout> getPartnerPayouts() {
        return this.partnerPayouts;
    }

    public void setPartnerPayouts(Set<PartnerPayout> partnerPayouts) {
        this.partnerPayouts = partnerPayouts;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "partner")
    public Set<CustomerDTO> getCustomers() {
        return this.customers;
    }

    public void setCustomers(Set<CustomerDTO> customers) {
        this.customers = customers;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "partner")
    @Fetch(FetchMode.SUBSELECT)
    public Set<PartnerRange> getRanges() {
        return this.ranges;
    }

    public void setRanges(Set<PartnerRange> ranges) {
        this.ranges = ranges;
    }

    @Version
    @Column(name = "OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    /* 
     * Inherited from DTOEx
     */
    @Transient
    public UserDTO getUser() {
        return getBaseUser();
    }

    /**
     * @return
     */
    @Transient
    public Integer getRelatedClerkUserId() {
        return relatedClerkUserId;
    }

    /**
     * @param relatedClerckUserId
     */
    public void setRelatedClerkUserId(Integer relatedClerckUserId) {
        this.relatedClerkUserId = relatedClerckUserId;
    }

    public void touch() {
        getBalance();
        getRanges().size();
        for (PartnerPayout payout : getPartnerPayouts()) {
            payout.touch();
        }
    }
}
