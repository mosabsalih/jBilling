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
import com.sapienter.jbilling.server.util.Context;

/**
 * CAI external provisioning plug-in. Contains logic for communicating
 * to the CAI system. Actual delivery of messages is left for 
 * implementations of IExternalCommunication. The configuration file
 * jbilling-provisioning.xml is used for selecting the cai 
 * IExternalCommunication class. 
 */
public class CAIProvisioningTask extends PluggableTask 
        implements IExternalProvisioning {
	
    public static final String PARAM_ID_DEFAULT = "cai";
    public static final ParameterDescription PARAMETER_ID = 
    	new ParameterDescription("id", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_REMOVE = 
    	new ParameterDescription("remove", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_USERNAME = 
    	new ParameterDescription("username", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PASSWORD = 
    	new ParameterDescription("password", true, ParameterDescription.Type.STR);

    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_ID);
        descriptions.add(PARAMETER_REMOVE);
        descriptions.add(PARAMETER_USERNAME);
        descriptions.add(PARAMETER_PASSWORD);
    }


    private static final Logger LOG = Logger.getLogger(
            CAIProvisioningTask.class);

    /**
     * Sends command to CAI system. Returns response.
     */
    public Map<String, Object> sendRequest(String id, String command) 
            throws TaskException {
        // construct final command string
        command = constructCommand(id, command);

        // send command and return results
        return parseResponse(sendCommand(command));
    }

    /**
     * Removes '-' from UUID and uses it as the command's TRANSID. 
     * Deletes any fields with values that match the 'remove' parameter.
     */
    private String constructCommand(String id, String command) {
        // remove '-' from UUID to create a transaction id
        StringBuilder transIdBuilder = new StringBuilder(id);
        int dashIndex = transIdBuilder.indexOf("-");
        while (dashIndex != -1) {
            transIdBuilder.delete(dashIndex, dashIndex + 1);
            dashIndex = transIdBuilder.indexOf("-", dashIndex);
        }
        id = transIdBuilder.toString();

        // add the transaction id to the command string
        StringBuilder commandBuilder = new StringBuilder(command);
        int insertIndex = commandBuilder.indexOf(":", 
                commandBuilder.indexOf(":") + 1);
        commandBuilder.insert(insertIndex, ":TRANSID," + id);

        // delete fields with values that match the remove parameter
        String removeValue = (String) parameters.get(PARAMETER_REMOVE.getName());
        if (removeValue != null) {
            int removeValueIndex = removeValueIndex(commandBuilder, removeValue);
            while (removeValueIndex != -1) {
                int fieldStartIndex = commandBuilder.lastIndexOf(":", 
                        removeValueIndex);
                commandBuilder.delete(fieldStartIndex, removeValueIndex + 1 +
                        removeValue.length());
                removeValueIndex = removeValueIndex(commandBuilder, removeValue);
            }
        }

        return commandBuilder.toString();
    }

    /**
     * Helper method for finding index of field values to be removed.
     */
    private int removeValueIndex(StringBuilder commandBuilder, String removeValue) {
        int removeValueIndex = commandBuilder.indexOf("," + removeValue + ":");
        if (removeValueIndex == -1) {
            removeValueIndex = commandBuilder.indexOf("," + removeValue + ";");
        }
        return removeValueIndex;
    }

    /**
     * Method to login to the CAI system and send command. 
     */
    private String sendCommand(String command) throws TaskException{
        IExternalCommunication cai = 
                (IExternalCommunication) Context.getBean(Context.Name.CAI);
//        IExternalCommunication cai = new TelnetCommunication();

        String username = (String) parameters.get(PARAMETER_USERNAME.getName());
        if (username == null) {
            throw new TaskException("No '" + PARAMETER_USERNAME.getName() + "' plug-in " +
                    "parameter found.");
        }
        String password = (String) parameters.get(PARAMETER_PASSWORD.getName());
        if (username == null) {
            throw new TaskException("No '" + PARAMETER_PASSWORD.getName() + "' plug-in " +
                    "parameter found.");
        }

        cai.connect(parameters);

        String response = cai.send("LOGIN:" + username + ":" + password + ";");
        if (!response.equals("RESP:0;")) {
            throw new TaskException("Couldn't login with username: '" + username + 
                    "' and password: '" + password + "'. Response: " + response);
        }

        LOG.debug("Sending command: " + command);
        response = cai.send(command);
        LOG.debug("Received response: " + response);

        String logout = cai.send("LOGOUT;");
        if (!logout.equals("RESP:0;")) {
            LOG.error("Logout error: " + logout);
        }

        cai.close();

        return response;
    }

    /**
     * Method to parse the response from the CAI system into a Map.
     * If 'RESP' is '0', 'result' is 'success', otherwise 'fail'. 
     */
    private Map<String, Object> parseResponse(String response) throws TaskException {
        // response in the form of: 
        // RESP:TRANSID,12345:0:NAME1,value1:NAME2,value2;

        Map<String, Object> results = new HashMap<String, Object>();

        if (!response.substring(0, 5).equals("RESP:")) {
            throw new TaskException("Expected 'RESP:' in response, but got: " +
                    response);
        }

        if (!response.substring(5,13).equals("TRANSID,")) {
            throw new TaskException("Expected 'TRANSID:' in response, but got: " +
                    response);
        }

        // get up to ':'. this will be the TRANSID value
        int transidIndex = response.indexOf(':', 13);
        String value = response.substring(13, transidIndex);
        results.put("TRANSID", value);

        // get up to next ':' or ';', this will be the RESP value
        int respIndex = fieldSplitIndex(response, transidIndex + 1);
        value = response.substring(transidIndex + 1, respIndex);
        results.put("RESP", value);

        // set result value
        if (value.equals("0")) {
            results.put("result", "success");
        } else {
            results.put("result", "fail");
        }

        // rest of the fields
        // get to next ':' or ';', then within that, split by ','
        int prevIndex = respIndex;
        int fieldSplitIndex = fieldSplitIndex(response, prevIndex + 1);
        while (fieldSplitIndex != -1) {
            String str = response.substring(prevIndex + 1, fieldSplitIndex);
            int commaIndex = str.indexOf(',');
            results.put(str.substring(0, commaIndex), 
                    str.substring(commaIndex + 1, str.length()));

            prevIndex = fieldSplitIndex;
            fieldSplitIndex = fieldSplitIndex(response, prevIndex + 1);
        }

        return results;
    }

    /**
     * Helper method for finding the index of field separators
     */
    private int fieldSplitIndex(String str, int fromIndex) {
        int index = str.indexOf(':', fromIndex);
        if (index == -1) {
            index = str.indexOf(';', fromIndex);
        }
        return index;
    }

    /**
     * Returns the id of the task. 
     */
    public String getId() {
        String id = (String) parameters.get(PARAMETER_ID.getName());
        if (id != null) {
            return id;
        }
        return PARAM_ID_DEFAULT;
    }

    /**
     * For allowing unit testing outside jBilling. 
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
