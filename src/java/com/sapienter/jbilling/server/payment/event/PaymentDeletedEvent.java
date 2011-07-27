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

import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 *
 * @author emilc
 */
public class PaymentDeletedEvent implements Event {

    private final PaymentDTO payment;
    private final Integer entityId;

    public PaymentDeletedEvent(Integer entityId, PaymentDTO payment) {
        this.payment = payment;
        this.entityId = entityId;
    }

    public final Integer getEntityId() {
        return entityId;
    }

    public final PaymentDTO getPayment() {
        return payment;
    }

    public String getName() {
        return "Payment Deleted";
    }

}
