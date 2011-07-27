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

package com.sapienter.jbilling.server.provisioning.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.provisioning.task.mmsc.AddCustomerRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.DeleteCustomerRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.GetCustomerRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.GetCustomerResponse;
import com.sapienter.jbilling.server.provisioning.task.mmsc.IMMSCHandlerFacade;
import com.sapienter.jbilling.server.provisioning.task.mmsc.MMSCException_Exception;
import com.sapienter.jbilling.server.provisioning.task.mmsc.MmscFacadeHandlerResponse;
import com.sapienter.jbilling.server.provisioning.task.mmsc.ModifyCustomerRequest;


/**
 * Dummy MMSC communication class for testing MMSCProvisioningTask.
 */
public class TestMMSCCommunication implements IMMSCHandlerFacade {
    private static final Logger LOG = Logger
            .getLogger(TestMMSCCommunication.class);


    public MmscFacadeHandlerResponse addCustomer(AddCustomerRequest request)
            throws MMSCException_Exception {
        LOG.debug("Calling Dummy method addCustomer");
        return getResponse(request.getTransactionId());
    }

    public MmscFacadeHandlerResponse modifyCustomer(
            ModifyCustomerRequest request) throws MMSCException_Exception {
        LOG.debug("Calling Dummy method modifyCustomer");
        return getResponse(request.getTransactionId());
    }

    public MmscFacadeHandlerResponse deleteCustomer(
            DeleteCustomerRequest request) throws MMSCException_Exception {
        LOG.debug("Calling Dummy method deleteCustomer");
        return getResponse(request.getTransactionId());
    }

    public GetCustomerResponse getCustomerInfo(GetCustomerRequest request)
            throws MMSCException_Exception {
        return null; // not implemented
    }

    private MmscFacadeHandlerResponse getResponse(String transactionId) {
        MmscFacadeHandlerResponse response = new MmscFacadeHandlerResponse();
        // wait for command rules task transaction to complete
        //pause(2000);

        response.setTransactionId(transactionId);
        response.setStatusCode(MMSCProvisioningTask.STATUS_CODE_OK);
        response.setStatusMessage("Operation Performed Successfully");

        return response;
    }

    private void pause(long t) {
        LOG.debug("TestMMSCCommunication: pausing for " + t + " ms...");

        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }

}
