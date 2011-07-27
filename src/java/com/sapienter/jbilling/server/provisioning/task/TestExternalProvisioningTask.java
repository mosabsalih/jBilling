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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;

/**
 * Test external provisioning pluggable task. See also 
 * TestExternalProvisioningMDB, ProvisioningTest, 
 * provisioning_commands.drl and jbilling-provisioning.xml. 
 */
public class TestExternalProvisioningTask extends PluggableTask 
        implements IExternalProvisioning {
	public static final ParameterDescription PARAM_ID = 
		new ParameterDescription("id", false, ParameterDescription.Type.STR);
    public static final String PARAM_ID_DEFAULT = "test";

    //initializer for pluggable params
    { 
    	descriptions.add(PARAM_ID);
    }



    private static final Logger LOG = Logger.getLogger(
            TestExternalProvisioningTask.class);

    public Map<String, Object> sendRequest(String id, String command) 
            throws TaskException {

        // wait for command rules task transaction to complete
        //pause(1000);

        LOG.debug("id: " + id);
        LOG.debug("command: " + command);

        Map<String, Object> response = new HashMap<String, Object>();

        if(command.startsWith("DELETE:THIS:MSISDN,54321")) {
            response.put("result", "fail");
        } else if(command.startsWith("DELETE:THAT:MSISDN,98765")) {
            throw new TaskException("Test Exception");
        } else {
            response.put("result", "success");
        }

        return response;
    }

    public String getId() {
        String id = (String) parameters.get(PARAM_ID.getName());
        if (id != null) {
            return id;
        }
        return PARAM_ID_DEFAULT;
    }

    private void pause(long t) {
        LOG.debug("TestExternalProvisioningTask: pausing for " + t + " ms...");

        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }
}
