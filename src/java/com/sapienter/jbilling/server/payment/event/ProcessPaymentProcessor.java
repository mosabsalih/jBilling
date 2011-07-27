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

import javax.jms.Destination;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.payment.tasks.IAsyncPaymentParameters;
import com.sapienter.jbilling.server.system.event.AsynchronousEventProcessor;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;

/*
 * All this class has to do is to populate the 'message' field.
 * The actual posting on the queue is done by the parent/caller
 */
public class ProcessPaymentProcessor extends AsynchronousEventProcessor<IAsyncPaymentParameters> {

    private Integer entityId;
    
    public ProcessPaymentProcessor() {
        super();
    }

    @Override
    public void doProcess(Event event) {
        entityId = event.getEntityId();
        if (event instanceof ProcessPaymentEvent) {
            ProcessPaymentEvent pEvent;
            pEvent = (ProcessPaymentEvent) event;
            processPayment(pEvent);
        } else if (event instanceof EndProcessPaymentEvent) {
            EndProcessPaymentEvent endEvent = (EndProcessPaymentEvent) event;
            processEnd(endEvent);
        } else {
            throw new SessionInternalError("Can only process payment events");
        }
    }
    
    private void processPayment(ProcessPaymentEvent event) {
        // transform the event into map message fields
        try {
            message.setInt("invoiceId", event.getInvoiceId());
            message.setInt("processId", (event.getProcessId() == null) ? -1 : event.getProcessId());
            message.setInt("runId", (event.getRunId() == null) ? -1 : event.getRunId());
            message.setStringProperty("type", "payment");
            
            // add additional fields from the associated plug-in
            IAsyncPaymentParameters task = getPluggableTask(entityId, 
                    Constants.PLUGGABLE_TASK_ASYNC_PAYMENT_PARAMS);
            task.addParameters(message);
        } catch (Exception e) {
            throw new SessionInternalError("Error transforming message ", 
                    this.getClass(), e);
        }
    }
    
    private void processEnd(EndProcessPaymentEvent event) {
        try {
            message.setInt("runId", event.getRunId());
            message.setStringProperty("type", "ender");
        } catch (Exception e) {
            throw new SessionInternalError("Error transforming message ", 
                    this.getClass(), e);
        }
    }
    
    @Override
    protected int getEntityId() {
        return entityId.intValue();
    }
    
    @Override
    protected Destination getDestination() {
        return (Destination) Context.getBean(
                Context.Name.PROCESSORS_DESTINATION);
    }
    
}
