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

package com.sapienter.jbilling.server.mediation;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.mediation.db.MediationConfiguration;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.util.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * MediationBL
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class MediationConfigurationBL {

    private static PluggableTaskDAS getPluggableTaskDAS() {
        return Context.getBean(Context.Name.PLUGGABLE_TASK_DAS);
    }

    /**
     * Convert a given MediationConfiguration into a MediationConfigurationWS web-service object.
     *
     * @param dto dto to convert
     * @return converted web-service object
     */
    public static MediationConfigurationWS getWS(MediationConfiguration dto) {
        return dto != null ? new MediationConfigurationWS(dto) : null;
    }

    /**
     * Converts a list of MediationConfiguration objects into MediationConfigurationWS web-service objects.
     *
     * @see #getWS(MediationConfiguration)
     *
     * @param objects objects to convert
     * @return a list of converted DTO objects, or an empty list if ws objects list was empty.
     */
    public static List<MediationConfigurationWS> getWS(List<MediationConfiguration> objects) {
        List<MediationConfigurationWS> ws = new ArrayList<MediationConfigurationWS>(objects.size());
        for (MediationConfiguration dto : objects)
            ws.add(getWS(dto));
        return ws;
    }

    /**
     * Convert a given MediationConfigurationWS web-service object into a MediationConfiguration entity.
     *
     * The MediationConfigurationWS must have a pluggable task ID or an exception will be thrown.
     *
     * @param ws ws object to convert
     * @return converted DTO object
     * @throws SessionInternalError if required field is missing
     */
    public static MediationConfiguration getDTO(MediationConfigurationWS ws) {
        if (ws != null) {
            if (ws.getPluggableTaskId() == null)
                throw new SessionInternalError("MediationConfiguration must have a pluggable task id.");

            PluggableTaskDTO pluggableTask = getPluggableTaskDAS().find(ws.getPluggableTaskId());

            return new MediationConfiguration(ws, pluggableTask);
        }
        return null;
    }

    /**
     * Converts a list of MediationConfigurationWS web-service objects into MediationConfiguration objects.
     *
     * @see #getDTO(MediationConfigurationWS)
     *
     * @param objects web-service objects to convert
     * @return a list of converted WS objects, or an empty list if DTO objects list was empty.
     */
    public static List<MediationConfiguration> getDTO(List<MediationConfigurationWS> objects) {
        List<MediationConfiguration> dto = new ArrayList<MediationConfiguration>(objects.size());
        for (MediationConfigurationWS ws : objects)
            dto.add(getDTO(ws));
        return dto;
    }
}
