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

package com.sapienter.jbilling.server.payment.tasks;

import java.util.Map;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;

/**
 * Abstract class for payment routers. Payment routers must implement
 * the selectDelegate method which returns a PaymentTask to process 
 * the payment. The subclass can optionally override the 
 * getAsyncParameters method if the RouterAsyncParameters plug-in is
 * to be used with it.
 */
public abstract class AbstractPaymentRouterTask extends PluggableTask 
        implements PaymentTask {
    private static final Logger LOG = Logger.getLogger(AbstractPaymentRouterTask.class);

    /**
     * Determines what processor is to process the payment. Takes the
     * payment info and returns a processor. 
     */
    protected abstract PaymentTask selectDelegate(PaymentDTOEx paymentInfo)
            throws PluggableTaskException;

    /**
     * Method called by RouterAsyncParameters to add any parameters for
     * concurrent asychronous payment processing.
     */
    public Map<String, String> getAsyncParameters(InvoiceDTO invoice) 
            throws PluggableTaskException {
        return null;
    }

    public void failure(Integer userId, Integer retry) {
        // ignore, failure is already forced by broken delegate
    }

    public boolean process(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        LOG.debug("Routing for " + paymentInfo);
        PaymentTask delegate = selectDelegate(paymentInfo);
        if (delegate == null) {
            // give them a chance
            LOG.error("ATTENTION! Could not find a process to delegate for " +
                    "user : " + paymentInfo.getUserId());
            return false;
        }

        delegate.process(paymentInfo);

        LOG.debug("done");
        // they already used their chance
        return false;
    }

    public boolean preAuth(PaymentDTOEx paymentInfo) 
            throws PluggableTaskException {
        PaymentTask delegate = selectDelegate(paymentInfo);
        delegate.preAuth(paymentInfo);

        // they already used their chance
        return false;
    }

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth, 
            PaymentDTOEx paymentInfo) throws PluggableTaskException {
        PaymentTask delegate = selectDelegate(paymentInfo);
        if (delegate == null){
            LOG.error("ATTENTION! Delegate is recently changed for user : " + 
                    paymentInfo.getUserId() + " with not captured transaction: " +
                    auth.getTransactionId());
            return false;
        }
        delegate.confirmPreAuth(auth, paymentInfo);
        // they already used their chance
        return false;
    }

    protected PaymentTask instantiateTask(Integer taskId)
            throws PluggableTaskException {
        PluggableTaskBL<PaymentTask> taskLoader = 
                new PluggableTaskBL<PaymentTask>(taskId);
        return taskLoader.instantiateTask();
    }

    protected Integer intValueOf(Object object) {
        if (object instanceof Number) {
            return Integer.valueOf(((Number) object).intValue());
        }
        if (object instanceof String) {
            String parseMe = (String) object;
            return Integer.parseInt(parseMe);
        }
        return null;
    }
}
