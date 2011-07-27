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

import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDAS;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentInfoChequeDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import java.util.ArrayList;

public class PaymentDTOEx extends PaymentDTO {

    private Integer userId = null;
    private PaymentInfoChequeDTO cheque = null;
    private AchDTO ach = null;
    private CreditCardDTO creditCard = null;
    private String method = null;
    private List<Integer> invoiceIds = null;
    private List paymentMaps = null;
    private PaymentDTOEx payment = null; // for refunds
    private String resultStr = null;
    private Integer payoutId = null;

    // now we only support one of these
    private PaymentAuthorizationDTO authorization = null; // useful in refuds

    public PaymentDTOEx(PaymentDTO dto) {
        if (dto.getBaseUser() != null)
            userId = dto.getBaseUser().getId();

        setId(dto.getId());
        setCurrency(dto.getCurrency());
        setAmount(dto.getAmount());
        setBalance(dto.getBalance());
        setAttempt(dto.getAttempt());

        setCreditCard(dto.getCreditCard());
        setAch(dto.getAch());

        setDeleted(dto.getDeleted());
        setIsPreauth(dto.getIsPreauth());
        setIsRefund(dto.getIsRefund());

        setPaymentDate(dto.getPaymentDate());
        setCreateDatetime(dto.getCreateDatetime());
        setUpdateDatetime(dto.getUpdateDatetime());

        if (dto.getPaymentMethod() != null) {
            setPaymentMethod(dto.getPaymentMethod());
        }

        if (dto.getPaymentResult() != null) {
            setPaymentResult(dto.getPaymentResult());
        }
        setPaymentPeriod(dto.getPaymentPeriod());
        setPaymentNotes(dto.getPaymentNotes());

        invoiceIds = new ArrayList<Integer>();
        paymentMaps = new ArrayList();
    }

    public PaymentDTOEx(PaymentWS dto) {
        setId(dto.getId());
        setAmount(dto.getAmountAsDecimal());
        setAttempt(dto.getAttempt());
        setBalance(dto.getBalanceAsDecimal());
        setCreateDatetime(dto.getCreateDatetime());
        setCurrency(new CurrencyDTO(dto.getCurrencyId()));
        setDeleted(dto.getDeleted());
        setIsPreauth(dto.getIsPreauth());
        setIsRefund(dto.getIsRefund());
        setPaymentDate(dto.getPaymentDate());
        setUpdateDatetime(dto.getUpdateDatetime());
        setPaymentPeriod(dto.getPaymentPeriod());
        setPaymentNotes(dto.getPaymentNotes());

        if (dto.getMethodId() != null)
            setPaymentMethod(new PaymentMethodDTO(dto.getMethodId()));

        if (dto.getResultId() != null)
        setPaymentResult(new PaymentResultDAS().find(dto.getResultId()));

        userId = dto.getUserId();

        if (dto.getCheque() != null) {
            PaymentInfoChequeDTO chqDTO = new PaymentInfoChequeDTO();
            chqDTO.setBank(dto.getCheque().getBank());
            chqDTO.setDate(dto.getCheque().getDate());
            chqDTO.setId(dto.getCheque().getId() == null ? 0 : dto.getCheque().getId());
            chqDTO.setNumber(dto.getCheque().getNumber());
            cheque = chqDTO;
        } else {
            cheque = null;
        }

        if (dto.getCreditCard() != null) {
            creditCard = new CreditCardDTO(dto.getCreditCard());
        } else {
            creditCard = null;
        }

        method = dto.getMethod();

        if (dto.getAch() != null) {
            AchDTO achDTO = new AchDTO();
            achDTO.setAbaRouting(dto.getAch().getAbaRouting());
            achDTO.setAccountName(dto.getAch().getAccountName());
            achDTO.setAccountType(dto.getAch().getAccountType());
            achDTO.setBankAccount(dto.getAch().getBankAccount());
            achDTO.setBankName(dto.getAch().getBankName());
            achDTO.setGatewayKey(dto.getAch().getGatewayKey());
            //id may be null if the ACH is not saved yet
            if ( null != dto.getAch().getId()) {
                achDTO.setId(dto.getAch().getId());
            }
            this.ach = achDTO;
        } else {
            this.ach = null;
        }

        invoiceIds = new ArrayList<Integer>();
        paymentMaps = new ArrayList();

        if (dto.getInvoiceIds() != null) {
            for (int f = 0; f < dto.getInvoiceIds().length; f++) {
                invoiceIds.add(dto.getInvoiceIds()[f]);
            }
        }

        if (dto.getPaymentId() != null) {
            payment = new PaymentDTOEx();
            payment.setId(dto.getPaymentId());
        } else {
            payment = null;
        }

        authorization = new PaymentAuthorizationDAS().find(dto.getAuthorizationId());

    }
    /**
     *
     */
    public PaymentDTOEx() {
        super();
        invoiceIds = new ArrayList<Integer>();
        paymentMaps = new ArrayList();
    }

    /**
     * @param id
     * @param amount
     * @param createDateTime
     * @param attempt
     * @param deleted
     * @param methodId
     */
//    public PaymentDTOEx(Integer id, BigDecimal amount, Date createDateTime,
//            Date updateDateTime,
//            Date paymentDate, Integer attempt, Integer deleted,
//            Integer methodId, Integer resultId, Integer isRefund,
//            Integer isPreauth, Integer currencyId, BigDecimal balance) {
//        super(id, amount, balance, createDateTime, updateDateTime,
//                paymentDate, attempt, deleted, methodId, resultId, isRefund,
//                isPreauth, currencyId, null, null);
//        invoiceIds = new ArrayList<Integer>();
//        paymentMaps = new ArrayList();
//    }

    /**
     * @param otherValue
     */
//    public PaymentDTOEx(PaymentDTO otherValue) {
//        super(otherValue);
//        invoiceIds = new ArrayList<Integer>();
//        paymentMaps = new ArrayList();
//    }

    public boolean validate() {
        boolean retValue = true;

        // check some mandatory fields
        if (getPaymentMethod() == null || getPaymentResult() == null) {
            retValue = false;
        }

        return retValue;
    }

    public String toString() {

        StringBuffer maps = new StringBuffer();
        if (paymentMaps != null) {
            for (int f = 0; f < paymentMaps.size(); f++) {
                maps.append(paymentMaps.get(f).toString());
                maps.append(" - ");
            }
        }

        // had to repeat this code :( To exclude the number
        StringBuffer cc = new StringBuffer("{");
        if (creditCard != null) {
            cc.append("id=" + creditCard.getId() + " " + "expiry="
                    + creditCard.getCcExpiry() + " " + "name="
                    + creditCard.getName() + " " + "type="
                    + creditCard.getCcType() + " " + "deleted="
                    + creditCard.getDeleted() + " " + "securityCode="
                    + creditCard.getSecurityCode());
        }
        cc.append('}');


        return super.toString() + " credit card:" + cc.toString() +
            " cheque:" + cheque + " payment maps:" + maps.toString();
    }
    /**
     * @return
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param integer
     */
    public void setUserId(Integer integer) {
        userId = integer;
    }


    /**
     * @return
     */
    public PaymentInfoChequeDTO getCheque() {
        return cheque;
    }

    /**
     * @param chequeDTO
     */
    public void setCheque(PaymentInfoChequeDTO chequeDTO) {
        cheque = chequeDTO;
    }

    /**
     * @return
     */
    public CreditCardDTO getCreditCard() {
        return creditCard;
    }

    /**
     * @param cardDTO
     */
    public void setCreditCard(CreditCardDTO cardDTO) {
        creditCard = cardDTO;
    }

    /**
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param string
     */
    public void setMethod(String string) {
        method = string;
    }


    /**
     * @return
     */
    public List<Integer> getInvoiceIds() {
        return invoiceIds;
    }

    /**
     * @param vector
     */
    public void setInvoiceIds(List vector) {
        invoiceIds = vector;
    }

    /**
     * @return
     */
    public PaymentDTOEx getPayment() {
        return payment;
    }

    /**
     * @param ex
     */
    public void setPayment(PaymentDTOEx ex) {
        payment = ex;
    }

    /**
     * @return
     */
    public PaymentAuthorizationDTO getAuthorization() {
        Logger.getLogger(PaymentDTOEx.class).debug("Returning " +
                authorization + " for payemnt " + getId());
        return authorization;
    }

    /**
     * @param authorizationDTO
     */
    public void setAuthorization(PaymentAuthorizationDTO authorizationDTO) {
        authorization = authorizationDTO;
    }

    /**
     * @return
     */
    public String getResultStr() {
        return resultStr;
    }

    /**
     * @param resultStr
     */
    public void setResultStr(String resultStr) {
        this.resultStr = resultStr;
    }

    /**
     * @return
     */
    public Integer getPayoutId() {
        return payoutId;
    }

    /**
     * @param payoutId
     */
    public void setPayoutId(Integer payoutId) {
        this.payoutId = payoutId;
    }

    /**
     * @return Returns the ach.
     */
    public AchDTO getAch() {
        return ach;
    }
    /**
     * @param ach The ach to set.
     */
    public void setAch(AchDTO ach) {
        this.ach = ach;
    }
    public List getPaymentMaps() {
        Logger.getLogger(PaymentDTOEx.class).debug("Returning " +
                paymentMaps.size() + " elements in the map");
        return paymentMaps;
    }

    public void addPaymentMap(PaymentInvoiceMapDTOEx map) {
        Logger.getLogger(PaymentDTOEx.class).debug("Adding map to the vector ");
        paymentMaps.add(map);
    }
}
