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
package com.sapienter.jbilling.server.payment.event;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.util.Constants;

public abstract class AbstractPaymentEvent implements Event {
    private final PaymentDTOEx payment;
    private final Integer entityId;
    
    public static AbstractPaymentEvent forPaymentResult(Integer entityId, PaymentDTOEx payment){
        Integer result = payment.getPaymentResult().getId();
        AbstractPaymentEvent event = null;
        if (Constants.RESULT_UNAVAILABLE.equals(result)){
            event = new PaymentProcessorUnavailableEvent(entityId, payment);
        } else if (Constants.RESULT_OK.equals(result)){
            event = new PaymentSuccessfulEvent(entityId, payment);
        } else if (Constants.RESULT_FAIL.equals(result)){
            event = new PaymentFailedEvent(entityId, payment);
        } else if (Constants.RESULT_NULL.equals(result)){
           // some processors don't do anything (fake), only pass to the next
           // processor in the chain
            event = null;
        }
        return event;
    }
    
    public AbstractPaymentEvent(Integer entityId, PaymentDTOEx payment) {
        this.payment = payment;
        this.entityId = entityId;
    }
    
    public final Integer getEntityId() {
        return entityId;
    }
    
    public final PaymentDTOEx getPayment() {
        return payment;
    }

    public String toString() {
        return "Event " + getName() + " payment: " + payment + " entityId: " + entityId;
    }
    
    public String getPaymentProcessor(){
        PaymentAuthorizationDTO auth = payment.getAuthorization();
        return auth == null ? null : auth.getProcessor();
    }

}
