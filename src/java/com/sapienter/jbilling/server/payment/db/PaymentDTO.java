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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
import com.sapienter.jbilling.server.util.csv.Exportable;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(
        name = "payment_GEN", 
        table = "jbilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "payment", 
        allocationSize = 100)
@Table(name = "payment")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PaymentDTO implements Serializable, Exportable {

    private int id;
    private UserDTO baseUser;
    private CurrencyDTO currencyDTO;
    private PaymentMethodDTO paymentMethod;
    private PaymentDTO payment;
    private CreditCardDTO creditCard;
    private PaymentResultDTO paymentResult;
    private AchDTO ach;
    private Integer attempt;
    private BigDecimal amount;
    private Date createDatetime;
    private Date paymentDate;
    private int deleted;
    private int isRefund;
    private PartnerPayout payoutId;
    private BigDecimal balance;
    private Date updateDatetime;
    private Integer isPreauth;
    private PaymentInfoChequeDTO paymentInfoCheque;
    
    private Set<PaymentInvoiceMapDTO> invoicesMap = new HashSet<PaymentInvoiceMapDTO>(0);
    private Set<PaymentAuthorizationDTO> paymentAuthorizations = new HashSet<PaymentAuthorizationDTO>(0);
    private Set<PaymentDTO> payments = new HashSet<PaymentDTO>(0);
    private Set<PartnerPayout> partnerPayouts = new HashSet<PartnerPayout>(0);

    private int versionNum;
    private Integer paymentPeriod;
    private String paymentNotes;

    public PaymentDTO() {
    }

    public PaymentDTO(int id) {
        this.id = id;
    }

    public PaymentDTO(PaymentDTOEx dto) {
        setId(dto.getId());
        setAmount(dto.getAmount());
        setAttempt(dto.getAttempt());
        setBalance(dto.getBalance());
        setCreateDatetime(dto.getCreateDatetime());
        setCurrency(new CurrencyDTO(dto.getCurrency().getId()));
        setDeleted(dto.getDeleted());
        setIsPreauth(dto.getIsPreauth());
        setIsRefund(dto.getIsRefund());
        setPaymentMethod(new PaymentMethodDTO(dto.getPaymentMethod().getId()));
        setPaymentDate(dto.getPaymentDate());
        setUpdateDatetime(dto.getUpdateDatetime());

        if (dto.getPaymentResult() != null)
            setPaymentResult(new PaymentResultDTO(dto.getPaymentResult().getId()));
    }

    public PaymentDTO(int id, CurrencyDTO currencyDTO,
            PaymentMethodDTO paymentMethod, BigDecimal amount, Date createDatetime,
            int deleted, int isRefund, Integer isPreauth) {
        this.id = id;
        this.currencyDTO = currencyDTO;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.createDatetime = createDatetime;
        this.deleted = deleted;
        this.isRefund = isRefund;
        this.isPreauth = isPreauth;
    }

    public PaymentDTO(int id, UserDTO baseUser, CurrencyDTO currencyDTO,
            PaymentMethodDTO paymentMethod, PaymentDTO payment,
            CreditCardDTO creditCard, PaymentResultDTO paymentResult, AchDTO ach,
            Integer attempt, BigDecimal amount, Date createDatetime,
            Date paymentDate, int deleted, int isRefund, PartnerPayout payoutId,
            BigDecimal balance, Date updateDatetime, int isPreauth,
            Set<PaymentAuthorizationDTO> paymentAuthorizations,
            Set<PaymentDTO> payments, Set<PartnerPayout> partnerPayouts,
            PaymentInfoChequeDTO paymentInfoCheque) {
        this.id = id;
        this.baseUser = baseUser;
        this.currencyDTO = currencyDTO;
        this.paymentMethod = paymentMethod;
        this.payment = payment;
        this.creditCard = creditCard;
        this.paymentResult = paymentResult;
        this.ach = ach;
        this.attempt = attempt;
        this.amount = amount;
        this.createDatetime = createDatetime;
        this.paymentDate = paymentDate;
        this.deleted = deleted;
        this.isRefund = isRefund;
        this.payoutId = payoutId;
        this.balance = balance;
        this.updateDatetime = updateDatetime;
        this.isPreauth = isPreauth;
        this.paymentAuthorizations = paymentAuthorizations;
        this.payments = payments;
        this.partnerPayouts = partnerPayouts;
        this.paymentInfoCheque = paymentInfoCheque;
    }

    public PaymentDTO(int id2, BigDecimal amount2, BigDecimal balance2,
            Date createDatetime2, Date updateDatetime2, Date paymentDate2,
            Integer attempt2, int deleted2, PaymentMethodDTO paymentMethod2,
            PaymentResultDTO paymentResult2, int isRefund2, Integer isPreauth2,
            CurrencyDTO currency, UserDTO baseUser2) {
        
        this.id = id2;
        this.amount = amount2;
        this.balance = balance2;
        this.createDatetime = createDatetime2;
        this.updateDatetime = updateDatetime2;
        this.paymentDate = paymentDate2;
        this.attempt = attempt2;
        this.deleted = deleted2;
        this.paymentMethod = paymentMethod2;
        this.paymentResult = paymentResult2;
        this .isRefund = isRefund2;
        this.isPreauth = isPreauth2;
        this.currencyDTO = currency;
        this.baseUser = baseUser2;
        
    }
    
    public PaymentDTO(PaymentDTO dto) {
        this.id = dto.id;
        this.baseUser = dto.baseUser;
        this.currencyDTO = dto.currencyDTO;
        this.paymentMethod = dto.paymentMethod;
        this.payment = dto.payment;
        this.creditCard = dto.creditCard;
        this.paymentResult = dto.paymentResult;
        this.ach = dto.ach;
        this.attempt = dto.attempt;
        this.amount = dto.amount;
        this.createDatetime = dto.createDatetime;
        this.paymentDate = dto.paymentDate;
        this.deleted = dto.deleted;
        this.isRefund = dto.isRefund;
        this.payoutId = dto.payoutId;
        this.balance = dto.balance;
        this.updateDatetime = dto.updateDatetime;
        this.isPreauth = dto.isPreauth;
        this.paymentAuthorizations = dto.paymentAuthorizations;
        this.payments = dto.payments;
        this.partnerPayouts = dto.partnerPayouts;
        this.paymentInfoCheque = dto.paymentInfoCheque;
        this.paymentNotes = dto.paymentNotes;
        this.paymentPeriod = dto.paymentPeriod;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "payment_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public UserDTO getBaseUser() {
        return this.baseUser;
    }

    public void setBaseUser(UserDTO baseUser) {
        this.baseUser = baseUser;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    public CurrencyDTO getCurrency() {
        return this.currencyDTO;
    }

    public void setCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", nullable = false)
    public PaymentMethodDTO getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodDTO paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    public PaymentDTO getPayment() {
        return this.payment;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id")
    public CreditCardDTO getCreditCard() {
        return this.creditCard;
    }

    public void setCreditCard(CreditCardDTO creditCard) {
        this.creditCard = creditCard;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    public PaymentResultDTO getPaymentResult() {
        return this.paymentResult;
    }

    public void setPaymentResult(PaymentResultDTO paymentResult) {
        this.paymentResult = paymentResult;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ach_id")
    public AchDTO getAch() {
        return this.ach;
    }

    public void setAch(AchDTO ach) {
        this.ach = ach;
    }

    @Column(name = "attempt")
    public Integer getAttempt() {
        return this.attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    /**
     * Returns the dollar value of the payment made.
     *
     * @return payment amount
     */
    @Column(name = "amount", nullable = false, precision = 17, scale = 17)
    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Column(name = "create_datetime", nullable = false, length = 29)
    public Date getCreateDatetime() {
        return this.createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    @Column(name = "payment_date", length = 13)
    public Date getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Column(name = "deleted", nullable = false)
    public int getDeleted() {
        return this.deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    @Column(name = "is_refund", nullable = false)
    public int getIsRefund() {
        return this.isRefund;
    }

    public void setIsRefund(int isRefund) {
        this.isRefund = isRefund;
    }

    @Column(name = "payout_id")
    public PartnerPayout setPayoutIncludedIn() {
        return this.payoutId;
    }

    public void setPayoutIncludedIn(PartnerPayout payoutId) {
        this.payoutId = payoutId;
    }

    /**
     * Returns the remaining balance left over from this payment. A payment amount can be
     * greater than the user's current owing balance, leaving a remainder.
     * 
     * @return remaining balance of this payment
     */
    @Column(name = "balance", precision = 17, scale = 17)
    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Column(name = "update_datetime", length = 29)
    public Date getUpdateDatetime() {
        return this.updateDatetime;
    }

    public void setUpdateDatetime(Date updateDatetime) {
        this.updateDatetime = updateDatetime;
    }

    @Column(name = "is_preauth", nullable = false)
    public Integer getIsPreauth() {
        return this.isPreauth;
    }

    public void setIsPreauth(Integer isPreauth) {
        this.isPreauth = isPreauth;
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "payment")
    public Set<PaymentAuthorizationDTO> getPaymentAuthorizations() {
        return this.paymentAuthorizations;
    }

    public void setPaymentAuthorizations(
            Set<PaymentAuthorizationDTO> paymentAuthorizations) {
        this.paymentAuthorizations = paymentAuthorizations;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "payment")
    public Set<PaymentDTO> getPayments() {
        return this.payments;
    }

    public void setPayments(Set<PaymentDTO> payments) {
        this.payments = payments;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "payment")
    public Set<PartnerPayout> getPartnerPayouts() {
        return this.partnerPayouts;
    }

    public void setPartnerPayouts(Set<PartnerPayout> partnerPayouts) {
        this.partnerPayouts = partnerPayouts;
    }

    @Transient
    public PaymentInfoChequeDTO getPaymentInfoCheque() {
        return new PaymentInfoChequeDAS().findByPayment(this);
    }

    public void setPaymentInfoCheque(PaymentInfoChequeDTO paymentInfoCheque) {
        
        PaymentInfoChequeDAS das = new PaymentInfoChequeDAS();
        PaymentInfoChequeDTO finded = das.findByPayment(this);
        
        if (finded != null) {
            das.delete(finded);
        }
        
        paymentInfoCheque.setPayment(this);
        das.save(paymentInfoCheque);
        
    }

    public void setInvoicesMap(Set<PaymentInvoiceMapDTO> invoicesMap) {
        this.invoicesMap = invoicesMap;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "payment")
    public Set<PaymentInvoiceMapDTO> getInvoicesMap() {
        return invoicesMap;
    }
    
    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    @Transient
    public Integer getMethodId() {
        return getPaymentMethod().getId();
    }

    @Transient
    public Integer getResultId() {
        return getPaymentResult().getId();
    }

    @Column(name = "payment_notes", nullable = true)
    public String getPaymentNotes(){
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes){
        this.paymentNotes = paymentNotes;
    }
    
    @Column(name = "payment_period", nullable = true)
    public Integer getPaymentPeriod(){
        return paymentPeriod;
    }

    public void setPaymentPeriod(Integer period){
        this.paymentPeriod = period;
    }

    @Transient
    public String[] getFieldNames() {
        return new String[] {
                "id",
                "userId",
                "userName",
                "linkedInvoices",
                "paymentMethod",
                "currency",
                "amount",
                "balance",
                "isRefund",
                "isPreauth",
                "createdDate",
                "paymentDate",
                "paymentNotes",

                // payment auth
                "paymentProcessor",
                "code1",
                "code2",
                "code3",
                "approvalCode",
                "avs",
                "transactionId",
                "md5",
                "cardCode",
                "responseMessage",

                // credit card
                "cardName",
                "cardNumber",
                "cardType",
                "cardExpiry",

                // ach
                "achAccountName",
                "achBankName",
                "achAccountType",

                // cheque
                "chequeBankName",
                "chequeNumber",
                "chequeDate",
        };
    }

    @Transient
    public Object[][] getFieldValues() {
        StringBuffer invoiceIds = new StringBuffer();
        for (Iterator<PaymentInvoiceMapDTO> it = invoicesMap.iterator(); it.hasNext();) {
            invoiceIds.append(it.next().getInvoiceEntity().getId());
            if (it.hasNext()) invoiceIds.append(", ");
        }

        PaymentAuthorizationDTO latestAuthorization = (!paymentAuthorizations.isEmpty()
                                                           ? paymentAuthorizations.iterator().next()
                                                           : null);

        return new Object[][] {
            {
                id,
                (baseUser != null ? baseUser.getId() : null),
                (baseUser != null ? baseUser.getUserName() : null),
                invoiceIds.toString(),
                (paymentMethod != null ? paymentMethod.getDescription() : null),
                (currencyDTO != null ? currencyDTO.getDescription() : null),
                amount,
                balance,
                isRefund,
                isPreauth,
                createDatetime,
                paymentDate,
                paymentNotes,

                (latestAuthorization != null ? latestAuthorization.getProcessor() : null),
                (latestAuthorization != null ? latestAuthorization.getCode1() : null),
                (latestAuthorization != null ? latestAuthorization.getCode2() : null),
                (latestAuthorization != null ? latestAuthorization.getCode3() : null),
                (latestAuthorization != null ? latestAuthorization.getApprovalCode() : null),
                (latestAuthorization != null ? latestAuthorization.getAvs() : null),
                (latestAuthorization != null ? latestAuthorization.getTransactionId() : null),
                (latestAuthorization != null ? latestAuthorization.getMD5() : null),
                (latestAuthorization != null ? latestAuthorization.getCardCode() : null),
                (latestAuthorization != null ? latestAuthorization.getResponseMessage() : null),

                (creditCard != null ? creditCard.getName() : null),
                (creditCard != null ? creditCard.getCcNumberPlain() : null),
                (creditCard != null ? creditCard.getCcType() : null),
                (creditCard != null ? creditCard.getExpiry() : null),

                (ach != null ? ach.getAccountName() : null),
                (ach != null ? ach.getBankName() : null),
                (ach != null ? ach.getAccountType() : null),

                (paymentInfoCheque != null ? paymentInfoCheque.getBank() : null),
                (paymentInfoCheque != null ? paymentInfoCheque.getNumber() : null),
                (paymentInfoCheque != null ? paymentInfoCheque.getDate() : null),
            }
        };
    }
}
