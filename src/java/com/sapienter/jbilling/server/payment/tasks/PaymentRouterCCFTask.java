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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;

/**
 * Routes payments to other processor plug-ins baed on a custom 
 * contact field of the customer. The id of CCF must be set using a 
 * parameter. To configure the routing, the CCF value is set as a 
 * parameter name and the id of the payment processor as the 
 * parameter's value.
 */
public class PaymentRouterCCFTask extends AbstractPaymentRouterTask {
	public static final ParameterDescription PARAM_CUSTOM_FIELD_PAYMENT_PROCESSOR = 
		new ParameterDescription("custom_field_id", true, ParameterDescription.Type.STR);
    private static final Logger LOG = Logger.getLogger(PaymentRouterCCFTask.class);

    //initializer for pluggable params
    { 
    	descriptions.add(PARAM_CUSTOM_FIELD_PAYMENT_PROCESSOR);
    }
    
    
    @Override
    protected PaymentTask selectDelegate(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        Integer userId = paymentInfo.getUserId();
        String processorName = getProcessorName(userId);
        if (processorName == null) {
            return null;
        }
        Integer selectedTaskId;
        try {
            // it is a task parameter the id of the processor
            selectedTaskId = intValueOf(parameters.get(processorName));
        } catch (NumberFormatException e) {
            throw new PluggableTaskException("Invalid payment task id :"
                    + processorName + " for userId: " + userId);
        }
        if (selectedTaskId == null) {
            LOG.warn("Could not find processor for " + parameters.get(processorName));
            return null;
        }

        LOG.debug("Delegating to task id " + selectedTaskId);
        PaymentTask selectedTask = instantiateTask(selectedTaskId);

        return selectedTask;
    }

    @Override
    public Map<String, String> getAsyncParameters(InvoiceDTO invoice) 
            throws PluggableTaskException {
        String processorName = getProcessorName(invoice.getUserId());
        Map<String, String> parameters = new HashMap<String, String>(1);
        parameters.put("processor", processorName);
        return parameters;
    }

    private String getProcessorName(Integer userId) throws PluggableTaskException {
        ContactBL contactLoader;
        String processorName = null;
        contactLoader = new ContactBL();
        contactLoader.set(userId);

        ContactDTOEx contact = contactLoader.getDTO();
        ContactFieldDTO paymentProcessorField = (ContactFieldDTO) contact.getFieldsTable().get(
                parameters.get(PARAM_CUSTOM_FIELD_PAYMENT_PROCESSOR.getName()));
        if (paymentProcessorField == null){
            LOG.warn("Can't find CCF with type " + 
                    parameters.get(PARAM_CUSTOM_FIELD_PAYMENT_PROCESSOR.getName()) +
                    " contact = " + contact);
            processorName = null;
        } else {
            processorName = paymentProcessorField.getContent();
        }
        return processorName;
    }
}
