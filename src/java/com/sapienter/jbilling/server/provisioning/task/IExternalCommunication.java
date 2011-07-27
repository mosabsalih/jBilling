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

import com.sapienter.jbilling.server.pluggableTask.TaskException;

/**
 * Interface for communicating commands to external provisioning 
 * systems. Some possible implementations might be telnet, tcp/ip, 
 * X.25, or test dummy. 
 */
public interface IExternalCommunication {
    /**
     * Connects to external provisioning system. The 
     * ExternalProvisioning pluggable task can pass in its parameters.
     */
    public void connect(Map<String, String> parameters) throws TaskException;

    /**
     * Sends the command to the external provisioning system.
     */
    public String send(String command) throws TaskException;

    /**
     * Closes the connection to the external provisioning system.
     */
    public void close() throws TaskException;
}
