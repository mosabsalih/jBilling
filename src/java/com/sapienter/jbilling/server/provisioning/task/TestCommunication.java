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

import java.util.Map;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.pluggableTask.TaskException;

/**
 * Dummy CAI communication class for testing CAIProvisioningTask.
 */
public class TestCommunication implements IExternalCommunication {
    private static final Logger LOG = Logger.getLogger(TestCommunication.class);

    public void connect(Map<String, String> parameters) throws TaskException {
        LOG.debug("Connect");
    }

    public String send(String command) throws TaskException {
        LOG.debug("Command: " + command);

        // return success (without a TRANSID) for login/logout commands
        if (command.startsWith("LOGIN:") || command.equals("LOGOUT;")) {
            return "RESP:0;";
        }

        // wait for command rules task transaction to complete
        //pause(2000);

        int transidIndexStart = command.indexOf(':', command.indexOf(':') + 1) 
                + 9;
        int transidIndexEnd = command.indexOf(":", transidIndexStart);
        String transid = command.substring(transidIndexStart, transidIndexEnd);

        // return success as well as the input fields
        String response = "RESP:TRANSID," + transid + ":0" + 
                command.substring(transidIndexEnd, command.length());
        LOG.debug("Response: " + response);
        return response;
    }

    public void close() throws TaskException {
        LOG.debug("Close");
    }

    private void pause(long t) {
        LOG.debug("TestCommunication: pausing for " + t + " ms...");

        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }
}
