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
import java.util.Collection;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDAS;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;

public class PaymentAuthorizationBL {
    private PaymentAuthorizationDAS paymentAuthorizationDas = null;
    private PaymentAuthorizationDTO paymentAuthorization = null;
    private static final Logger LOG = Logger.getLogger(PaymentAuthorizationBL.class); 

    public PaymentAuthorizationBL(Integer paymentAuthorizationId) {
        init();
        set(paymentAuthorizationId);
    }
    
    public PaymentAuthorizationBL(PaymentAuthorizationDTO entity) {
        init();
        paymentAuthorization = entity;
    }

    public PaymentAuthorizationBL() {
        init();
    }

    private void init() {
        paymentAuthorizationDas = new PaymentAuthorizationDAS();

    }

    public PaymentAuthorizationDTO getEntity() {
        return paymentAuthorization;
    }
    
    public void set(Integer id) {
        paymentAuthorization = paymentAuthorizationDas.find(id);
    }
    
    public void create(PaymentAuthorizationDTO dto, Integer paymentId) {
        // create the record, there's no need for an event to be logged 
        // since the timestamp and the user are already in the paymentAuthorization row
        paymentAuthorization = paymentAuthorizationDas.create(
                dto.getProcessor(), dto.getCode1());
            
        paymentAuthorization.setApprovalCode(dto.getApprovalCode());
        paymentAuthorization.setAvs(dto.getAvs());
        paymentAuthorization.setCardCode(dto.getCardCode());
        paymentAuthorization.setCode2(dto.getCode2());
        paymentAuthorization.setCode3(dto.getCode3());
        paymentAuthorization.setMD5(dto.getMD5());
        paymentAuthorization.setTransactionId(dto.getTransactionId());
        paymentAuthorization.setResponseMessage(Util.truncateString(dto.getResponseMessage(),200));
        
        // all authorization have to be linked to a payment
        try {
            PaymentBL payment = new PaymentBL(paymentId);
            paymentAuthorization.setPayment(payment.getEntity());
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        // original dto would like to know the created id and the payment id
        dto.setId(paymentAuthorization.getId());
        dto.setPayment(new PaymentDTO(paymentId));
    }
    
    public PaymentAuthorizationDTO getDTO() {
        PaymentAuthorizationDTO dto = new PaymentAuthorizationDTO();
        dto.setApprovalCode(paymentAuthorization.getApprovalCode());
        dto.setAvs(paymentAuthorization.getAvs());
        dto.setCardCode(paymentAuthorization.getCardCode());
        dto.setCode1(paymentAuthorization.getCode1());
        dto.setCode2(paymentAuthorization.getCode2());
        dto.setCode3(paymentAuthorization.getCode3());
        dto.setMD5(paymentAuthorization.getMD5());
        dto.setId(paymentAuthorization.getId());
        dto.setProcessor(paymentAuthorization.getProcessor());        
        dto.setTransactionId(paymentAuthorization.getTransactionId());
        dto.setCreateDate(paymentAuthorization.getCreateDate());
        dto.setResponseMessage(paymentAuthorization.getResponseMessage());
        return dto;
    }
        
    public PaymentAuthorizationDTO getPreAuthorization(Integer userId) {
        PaymentAuthorizationDTO auth = null;
        try {
            PaymentDAS paymentHome = new PaymentDAS();

            Collection payments = paymentHome.findPreauth(userId);
            // at the time, use the very first one
            if (!payments.isEmpty()) {
                PaymentDTO payment = (PaymentDTO) payments.toArray()[0];
                Collection auths = payment.getPaymentAuthorizations();
                if (!auths.isEmpty()) {
                    paymentAuthorization = 
                            (PaymentAuthorizationDTO) auths.toArray()[0];
                    auth = getDTO();
                } else {
                    LOG.warn("Auth payment found, but without auth record?");
                }
            }
        } catch (Exception e) {
            LOG.warn("Exceptions finding a pre authorization", e);
        }
        LOG.debug("Looking for preauth for " + userId + " result " + auth);
        return auth;
    }

    public void markAsUsed(PaymentDTOEx user) {
        paymentAuthorization.getPayment().setBalance(BigDecimal.ZERO);
        // this authorization got used by a real payment. Link them
        try {
            PaymentBL payment = new PaymentBL(user.getId());
            paymentAuthorization.getPayment().setPayment(payment.getEntity());
        } catch (Exception e) {
            throw new SessionInternalError("linking authorization to user payment",
                    PaymentAuthorizationBL.class, e);
        } 
    }
}
