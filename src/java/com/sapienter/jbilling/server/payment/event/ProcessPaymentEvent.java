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

import com.sapienter.jbilling.server.system.event.Event;

public class ProcessPaymentEvent implements Event {
    private final Integer invoiceId;
    private final Integer processId;
    private final Integer runId;
    private final Integer entityId;
    
    public ProcessPaymentEvent(Integer invoiceId,Integer processId,Integer runId,Integer entityId) {
        this.runId = runId;
        this.invoiceId = invoiceId;
        this.processId = processId;
        this.entityId= entityId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public String getName() {
        return "Process Payment";
    }

    public final Integer getInvoiceId() {
        return invoiceId;
    }

    public final Integer getProcessId() {
        return processId;
    }

    public final Integer getRunId() {
        return runId;
    }

}
