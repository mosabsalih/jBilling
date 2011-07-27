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

package com.sapienter.jbilling.server.mediation.task;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.mediation.Record;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * Stateless JDBC reader that does not change the state of the underlying database
 * being read or persist it's progress.
 *
 * This reader always operates using a LAST_ID mark method, where the "last ID read" is held
 * in memory to provide a starting point for the reading of each batch i.e., each batch of records
 * will be queried "WHERE ID > :last_read".
 *
 * Unlike the the {@link com.sapienter.jbilling.server.mediation.task.JDBCReader} this reader does not update or fetch the mediation "last ID read"
 * preference. Every subsequent execution of this reader starts at zero.
 *
 * @author Brian Cowdery
 * @since 27-09-2010
 */
public class StatelessJDBCReader extends AbstractJDBCReader {
    private static final Logger LOG = Logger.getLogger(StatelessJDBCReader.class);

    private Integer lastId = 0;

    @Override
    public Integer getLastId() {
        return lastId;
    }

    @Override
    public void setLastId(Integer lastId) {
        this.lastId = lastId;
    }

    @Override
    public MarkMethod getMarkMethod() {
        return MarkMethod.LAST_ID;
    }

    /**
     * Returns a SQL query that reads all records present regardless of previous reads.
     *
     * @return SQL query string
     */
    @Override
    protected String getSqlQueryString() {
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM ")
                .append(getTableName())
                .append(" WHERE ");
        
        // constrain query based on the last ID read
        if (getMarkMethod() == MarkMethod.LAST_ID) {
            if (getKeyColumns().size() > 1)
                throw new SessionInternalError("LAST_ID marking method only allows for one key column.");
            query.append(getKeyColumns().get(0)).append(" > ").append(getLastId()).append(" ");
        }

        // append optional user-defined where clause
        String where = getParameter(PARAM_WHERE_APPEND.getName(), (String) null);
        if (where != null)
            query.append(where).append(" ");

        // append optional user-defined order, or build one by using defined key columns
        String order = getParameter(PARAM_ORDER_BY.getName(), (String) null);
        query.append("ORDER BY ");
        
        if (order != null) {
            query.append(order);

        } else {
            for (Iterator<String> it = getKeyColumns().iterator(); it.hasNext();) {
                query.append(it.next());
                if (it.hasNext())
                    query.append(", ");
            }
        }

        LOG.debug("SQL query: '" + query + "'");        
        return query.toString();
    }

    /**
     * Records the "last read ID" so that the reader can start where it left off on
     * the next read.
     *
     * @param record record that was read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    @Override
    protected void recordRead(final Record record, final int[] keyColumnIndexes) {
        setLastId(record.getFields().get(keyColumnIndexes[0]).getIntValue());        
    }

    /**
     * Not implemented. Stateless JDBC reader does not record reads.
     * 
     * @param records list of records that were read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    @Override
    protected void batchRead(final List<Record> records, final int[] keyColumnIndexes) {
    }
}
