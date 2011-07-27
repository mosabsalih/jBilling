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
 * Interface for External Provisioning pluggable tasks.
 */
public interface IExternalProvisioning {
    /**
     * Receives a UUID and command string. Returns results in a map.
     */
    public Map<String, Object> sendRequest(String id, String command) 
            throws TaskException;

    /**
     * Returns the id of the task (used for command mapping).
     */
    public String getId();
}
