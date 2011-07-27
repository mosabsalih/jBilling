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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.provisioning.task.mmsc.AddCustomerRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.DeleteCustomerRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.EfsBaseMSISDNRequest;
import com.sapienter.jbilling.server.provisioning.task.mmsc.IMMSCHandlerFacade;
import com.sapienter.jbilling.server.provisioning.task.mmsc.MMSCException_Exception;
import com.sapienter.jbilling.server.provisioning.task.mmsc.MmscFacadeHandlerResponse;
import com.sapienter.jbilling.server.provisioning.task.mmsc.ModifyCustomerRequest;
import com.sapienter.jbilling.server.util.Context;

/**
 * @author othman
 * 
 *         MMSC external provisioning plug-in. Contains logic for communicating
 *         to MMSC webservice. Actual delivery of messages is left for
 *         implementations of IMMSCCommunication. The configuration file
 *         jbilling-provisioning.xml is used for selecting the mmsc
 *         IMMSCCommunication class.
 */
public class MMSCProvisioningTask extends PluggableTask implements
        IExternalProvisioning {
	
	public static final String PARAM_ID_DEFAULT = "mmsc";

	public static final String PARAM_ID = "id";
    // MMSCProvisioningTask plugin parameters
    public static final ParameterDescription PARAM_LOGIN_PASSWORD= 
    	new ParameterDescription( "loginPassword" , true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription PARAM_LOGIN_USER=
    	new ParameterDescription( "loginUser" , true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription PARAM_PORTAL_ID=
    	new ParameterDescription( "portalId", true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription PARAM_APPLICATION_ID=
    	new ParameterDescription( "applicationId", true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription PARAM_BNET=
    	new ParameterDescription( "bnet", false, ParameterDescription.Type.STR);

    // other MMSC service interface parameters
    public static final ParameterDescription TRANSACTION_ID=
		new ParameterDescription( "transactionId", false, ParameterDescription.Type.STR);
    
    public static final ParameterDescription CHANNEL_ID=
		new ParameterDescription( "channeld", false, ParameterDescription.Type.STR);
    
    public static final ParameterDescription REFERENCE_ID=
		new ParameterDescription( "referenceId", false, ParameterDescription.Type.STR);

    public static final ParameterDescription TAG=
		new ParameterDescription( "tag", false, ParameterDescription.Type.STR);

    public static final ParameterDescription USER_ID=
		new ParameterDescription( "userId", false, ParameterDescription.Type.STR);
    
    public static final ParameterDescription MSISDN=
    	new ParameterDescription( "msisdn", true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription SUBSCRIPTION_TYPE=
		new ParameterDescription( "subscriptionType", true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription MMS_CAPABILITY=
		new ParameterDescription( "mmsCapability", true, ParameterDescription.Type.STR);
    
    public static final ParameterDescription METHOD_NAME=
		new ParameterDescription( "methodName", true, ParameterDescription.Type.STR);
    
    // MMSC service methods names. These names should match the methods names in
    // IMMSCCommunication class
    public static final String ADD_CUSTOMER = "addCustomer";
    public static final String MODIFY_CUSTOMER = "modifyCustomer";
    public static final String DELETE_CUSTOMER = "deleteCustomer";

    // MMSC Service-level error codes
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_MESSAGE = "statusMessage";
    public static final int STATUS_CODE_OK = 0;
    public static final int STATUS_CODE_SERVICE_ERROR = -1;
    public static final int STATUS_CODE_MSISDN_ERROR = 1;
    public static final int STATUS_CODE_SUBSCRIPTION_ERROR = 2;
    public static final int STATUS_CODE_BNET_ERROR = 3;

    private static final Logger LOG = Logger.getLogger(MMSCProvisioningTask.class);

    //initializer for pluggable params
    { 
    	descriptions.add(PARAM_LOGIN_PASSWORD);
    	descriptions.add(PARAM_LOGIN_USER);
    	descriptions.add(PARAM_PORTAL_ID);
    	descriptions.add(PARAM_APPLICATION_ID);
    	descriptions.add(PARAM_BNET);
    	descriptions.add(TRANSACTION_ID);
    	descriptions.add(CHANNEL_ID);
    	descriptions.add(REFERENCE_ID);
    	descriptions.add(TAG);
    	descriptions.add(USER_ID);
    	descriptions.add(MSISDN);
    	descriptions.add(SUBSCRIPTION_TYPE);
    	descriptions.add(MMS_CAPABILITY);
    	descriptions.add(METHOD_NAME);
    }
   
    /**
     * Sends command to MMSC system. Returns response.
     */
    
    public Map<String, Object> sendRequest(String id, String command)
            throws TaskException {
        
        // send command and return results
        return parseResponse(sendCommand(command, id));
    }
    
    
    /**
     * Method call MMSC webservice method.
     *
     * @param command
     * @param id
     * @return
     * @throws TaskException
     */
    private MmscFacadeHandlerResponse sendCommand(String command, String id)
            throws TaskException {
        IMMSCHandlerFacade mmsc = (IMMSCHandlerFacade) Context
                .getBean(Context.Name.MMSC);
        MmscFacadeHandlerResponse response = null;

        Map<String, String> params = getParameters(command, id);
        if (params == null || params.isEmpty())
            throw new TaskException("NULL or Empty Parameters List!");

        String methodName = params.get(METHOD_NAME.getName());
        if (methodName == null)
            throw new TaskException("Expected Method Name!");

        try {
            if (methodName.equals(ADD_CUSTOMER)) {
                String subscriptionType = (String) params.get(
                        SUBSCRIPTION_TYPE.getName());
                if (subscriptionType == null) {
                    throw new TaskException("parameter '" + SUBSCRIPTION_TYPE.getName()
                            + "' is Mandatory ");
                }
                AddCustomerRequest request = new AddCustomerRequest();
                populateRequest(request, params);
                request.setSubscriptionType(subscriptionType);
                response = mmsc.addCustomer(request);
            } else if (methodName.equals(MODIFY_CUSTOMER)) {
                String mmsCapability = (String) params.get(MMS_CAPABILITY.getName());
                if (mmsCapability == null) {
                    throw new TaskException("parameter '" + MMS_CAPABILITY.getName()
                            + "' is Mandatory ");
                }
                ModifyCustomerRequest request = new ModifyCustomerRequest();
                populateRequest(request, params);
                request.setMmsCapability(mmsCapability);
                response = mmsc.modifyCustomer(request);
            } else if (methodName.equals(DELETE_CUSTOMER)) {
                DeleteCustomerRequest request = new DeleteCustomerRequest();
                populateRequest(request, params);
                response = mmsc.deleteCustomer(request);
            } else {
                throw new TaskException("webservice method '" + methodName
                        + "' is Not Found! ");
            }
        } catch (MMSCException_Exception mmsce) {
            throw new TaskException(mmsce);
        }

        return response;
    }

    /**
     * Adds common parameters to request.
     */
    private void populateRequest(EfsBaseMSISDNRequest request, 
            Map<String, String> params) throws TaskException {
        request.setLoginUser((String) params.get(PARAM_LOGIN_USER.getName()));
        request.setLoginPassword((String) params.get(PARAM_LOGIN_PASSWORD.getName()));
        request.setPortalId((String) params.get(PARAM_PORTAL_ID.getName()));
        request.setApplicationId((String) params.get(PARAM_APPLICATION_ID.getName()));
        request.setTransactionId((String) params.get(TRANSACTION_ID.getName()));
        request.setChannelId((String) params.get(CHANNEL_ID.getName()));
        request.setReferenceId((String) params.get(REFERENCE_ID.getName()));
        request.setTag((String) params.get(TAG.getName()));
        request.setUserId((String) params.get(USER_ID.getName()));

        String msisdn = (String) params.get(MSISDN.getName());
        if (msisdn == null) {
            throw new TaskException("parameter '" + MSISDN + "' is Mandatory ");
        }
        request.setMSISDN(msisdn);
    }

    /**
     * method to parse MMSC command
     * @param command
     * @return
     * @throws TaskException
     */
    private Map<String, String> parseCommand(String command)
            throws TaskException {

        // sample command pattern to parse:
        // "addCustomer:msisdn,46701055555:subscriptionType,HK;"

        LOG.debug("parsing command string pattern: " + command);
        Map<String, String> params = new LinkedHashMap<String, String>();
        StringTokenizer st = new StringTokenizer(command, ":;");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String[] entry = token.split(",");
            if (entry.length > 2)
                throw new TaskException(
                        "Error parsing command: Expected two Tokens but found too many tokens: "
                                + token);
            else if (entry.length == 1) {
                // found method name
                params.put(METHOD_NAME.getName(), entry[0]);
            } else {
                params.put(entry[0], entry[1]);
            }
        }

        return params;

    }

    /**
     * returns Parametres Map extracted from MMSC command String
     * 
     * @param command
     * @param id
     * @return
     * @throws TaskException
     */
    private Map<String, String> getParameters(String command, String id)
            throws TaskException {

        Map<String, String> params = new LinkedHashMap<String, String>();

        // collect plugin parameters
        String username = (String) parameters.get(PARAM_LOGIN_USER.getName());
        if (username == null) {
            throw new TaskException("No '" + PARAM_LOGIN_USER + "' plug-in "
                    + "parameter found.");
        }
        params.put(PARAM_LOGIN_USER.getName(), username);

        String password = (String) parameters.get(PARAM_LOGIN_PASSWORD.getName());
        if (password == null) {
            throw new TaskException("No '" + PARAM_LOGIN_PASSWORD.getName()
                    + "' plug-in " + "parameter found.");
        }
        params.put(PARAM_LOGIN_PASSWORD.getName(), password);

        String portalId = (String) parameters.get(PARAM_PORTAL_ID.getName());
        if (portalId == null) {
            throw new TaskException("No '" + PARAM_PORTAL_ID.getName() + "' plug-in "
                    + "parameter found.");
        }
        params.put(PARAM_PORTAL_ID.getName(), portalId);

        String applicationId = (String) parameters.get(PARAM_APPLICATION_ID.getName());
        if (applicationId == null) {
            throw new TaskException("No '" + PARAM_APPLICATION_ID.getName()
                    + "' plug-in " + "parameter found.");
        }
        params.put(PARAM_APPLICATION_ID.getName(), applicationId);

        /*
        String bnet = (String) parameters.get(PARAM_BNET);
        if (bnet == null) {
            throw new TaskException("No '" + PARAM_BNET + "' plug-in "
                    + "parameter found.");
        }
        params.put(PARAM_BNET, bnet);
        */

        params.put(TRANSACTION_ID.getName(), id);

        Map<String, String> parsedCommand = parseCommand(command);

        // append parsed command key/value pairs
        params.putAll(parsedCommand);

        return params;

    }

    

    /**
     * Method to parse the response from the MMSC system into a Map. If
     * 'statusCode' is '0', 'result' is 'success', otherwise 'fail'.
     *
     * @param response
     * @return
     * @throws TaskException
     */
    private Map<String, Object> parseResponse(
            MmscFacadeHandlerResponse response) throws TaskException {

        Map<String, Object> results = new HashMap<String, Object>();

        String value = response.getTransactionId();
        if (value == null) {
            throw new TaskException("Expected '" + TRANSACTION_ID.getName()
                    + "' in response");
        }
        // set TRANSACTION_ID value
        results.put(TRANSACTION_ID.getName(), value);

        int statusCode = response.getStatusCode();
        // set STATUS_CODE value
        results.put(STATUS_CODE, "" + statusCode);

        // set result value
        if (statusCode == STATUS_CODE_OK) {
            results.put("result", "success");
        } else {
            results.put("result", "fail");
        }

        value = response.getStatusMessage();
        if (value == null) {
            throw new TaskException("Expected '" + STATUS_MESSAGE
                    + "' in response");
        }
        results.put(STATUS_MESSAGE, value);

        return results;
    }

    /**
     * Returns the id of the task.
     */
    public String getId() {
        String id = (String) parameters.get(PARAM_ID);
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
