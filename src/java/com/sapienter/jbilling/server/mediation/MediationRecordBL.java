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

import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * MediationRecordBL
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class MediationRecordBL {

    /**
     * Convert a given MediationRecordDTO into a MediationRecordWS web-service object.
     *
     * @param dto dto to convert
     * @return converted web-service object
     */
    public static MediationRecordWS getWS(MediationRecordDTO dto) {
        return dto != null ? new MediationRecordWS(dto, getWS(dto.getLines())) : null;
    }

    /**
     * Converts a list of MediationRecordDTO objects into MediationRecordWS web-service objects.
     *
     * @see #getWS(MediationRecordDTO)
     *
     * @param objects objects to convert
     * @return a list of converted DTO objects, or an empty list if ws objects list was empty.
     */
    public static List<MediationRecordWS> getWS(List<MediationRecordDTO> objects) {
        List<MediationRecordWS> ws = new ArrayList<MediationRecordWS>(objects.size());
        for (MediationRecordDTO dto : objects)
            ws.add(getWS(dto));
        return ws;
    }

    /**
     * Convert a given MediationRecordLineDTO into a MediationRecordLineWS web-service object.
     *
     * @param dto dto to convert
     * @return converted web-service object
     */
    public static MediationRecordLineWS getWS(MediationRecordLineDTO dto) {
        return dto != null ? new MediationRecordLineWS(dto) : null;
    }

    /**
     * Converts a list of MediationRecordLineDTO objects into MediationRecordLineWS web-service objects.
     *
     * @see #getWS(MediationRecordLineDTO)
     *
     * @param objects objects to convert
     * @return a list of converted DTO objects, or an empty list if ws objects list was empty.
     */
    public static List<MediationRecordLineWS> getWS(Collection<MediationRecordLineDTO> objects) {
        List<MediationRecordLineWS> ws = new ArrayList<MediationRecordLineWS>(objects.size());
        for (MediationRecordLineDTO dto : objects)
            ws.add(getWS(dto));
        return ws;
    }


}
