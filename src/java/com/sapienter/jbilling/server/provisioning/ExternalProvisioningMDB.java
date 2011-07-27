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

package com.sapienter.jbilling.server.provisioning;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.util.Context;

/**
 * Receives messages from the provisioning commands rules task. Calls
 * the external provisioning logic through the provisioning session 
 * bean so it runs in a transaction. Configured in jbilling-jms.xml.
 */
public class ExternalProvisioningMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(
            ExternalProvisioningMDB.class);

    public void onMessage(Message message) {
        try {
            LOG.debug("Received a message");

            // use a session bean to make sure the processing is done in 
            // a transaction
            IProvisioningProcessSessionBean provisioning = 
                    (IProvisioningProcessSessionBean) Context.getBean(
                    Context.Name.PROVISIONING_PROCESS_SESSION);

            provisioning.externalProvisioning(message);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
}
