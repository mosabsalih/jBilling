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

import java.util.Collection;

import org.hibernate.Query;


/**
 * 
 * @author abimael
 *
 */
public class InternationalDescriptionDAS extends AbstractDAS<InternationalDescriptionDTO> {

    private JbillingTableDAS jtDAS; // injected by Spring

    // should only be created from Spring
    protected InternationalDescriptionDAS() {
        super();
    }

    public void setJbDAS(JbillingTableDAS util) {
        this.jtDAS = util;
    }

    public InternationalDescriptionDTO findIt(String table,
            Integer foreignId, String column, Integer language) {

        if (foreignId == null || foreignId == 0) {
            return null;
        }
        
        InternationalDescriptionId idi =
                new InternationalDescriptionId(jtDAS.findByName(table).getId(),
                (foreignId == null) ? 0 : foreignId, column, (language == null) ? 0 : language);

        return find(idi); // this should cache ok
    }

    public InternationalDescriptionDTO create(String table, Integer foreignId, String column,
            Integer language, String message) {

        InternationalDescriptionId idi = new InternationalDescriptionId(
                jtDAS.findByName(table).getId(), foreignId, column, language);

        InternationalDescriptionDTO inter = new InternationalDescriptionDTO();
        inter.setId(idi);
        inter.setContent(message);

        return save(inter);

    }

    public Collection<InternationalDescriptionDTO> findByTable_Row(String table, Integer foreignId) {
        final String QUERY = "SELECT a " +
            "FROM InternationalDescriptionDTO a, JbillingTable b " +
            "WHERE a.id.tableId = b.id " +
            "AND b.name = :table " +
            "AND a.id.foreignId = :foreing ";

        Query query = getSession().createQuery(QUERY);
        query.setParameter("table", table);
        query.setParameter("foreing", foreignId);
        return query.list();
    }

    public static InternationalDescriptionDAS getInstance() {
        return new InternationalDescriptionDAS();
    }
}
