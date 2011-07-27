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
package com.sapienter.jbilling.server.util.db;

import org.hibernate.Query;

public class PreferenceDAS extends AbstractDAS<PreferenceDTO> {
    private static final String findByType_Row =
        "SELECT a " + 
        "  FROM PreferenceDTO a " + 
        " WHERE a.preferenceType.id = :typeId " +
        "   AND a.foreignId = :foreignId " +
        "   AND a.jbillingTable.name = :tableName ";

    public PreferenceDTO findByType_Row(Integer typeId,Integer foreignId,String tableName) {
        Query query = getSession().createQuery(findByType_Row);
        query.setParameter("typeId", typeId);
        query.setParameter("foreignId", foreignId);
        query.setParameter("tableName", tableName);
        query.setCacheable(true);
        return (PreferenceDTO) query.uniqueResult();
    }

}
