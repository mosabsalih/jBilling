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
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.mediation.Record;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/**
 * Standard non-volatile JDBC reader.
 *
 * This reader records it's progress and attempts to ensure that records are not read again
 * on subsequent executions.
 *
 * The reader attempts to detect the marking method (used to mark a record as "read") by
 * inspecting the database table to be read from. If the table contains the configured time stamp
 * column then the TIMESTAMP marking method will be used. The reader will fall back upon LAST_ID
 * marking if no time stamp column exists.
 *
 * LAST_ID marking will store the "last read ID" in as a mediation preference in the jBilling database. This
 * ID will be queried for each subsequent execution and the reader will start from the last read ID.
 *
 * TIMESTAMP marking updates a configured time stamp column in the source database. Every record that is
 * read is marked by setting the time stamp column to the current time. Records to be read must have a
 * null time stamp.
 *
 * @author Brian Cowdery
 * @since 27-09-2010
 */
public class JDBCReader extends AbstractJDBCReader {
    private static final Logger LOG = Logger.getLogger(JDBCReader.class);

    private String timestampUpdateSql = null;

    /**
     * Returns an SQL query to read records that have not previously been read.
     *
     * If MarkMethod.TIMESTAMP is used, then this will limit read records to those that have a
     * null time stamp column value.
     *
     * If MarkMethod.LAST_ID is used, then this will limit read records to those where the record
     * id is greater than the last read id (example: "WHERE id > 123").
     *
     * @return SQL query string
     */
    @Override
    protected String getSqlQueryString() {
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM ")
                .append(getTableName())
                .append(" WHERE ");

        // constrain query based on marking method
        if (getMarkMethod() == MarkMethod.LAST_ID) {
            if (getKeyColumns().size() > 1)
                throw new SessionInternalError("LAST_ID marking method only allows for one key column.");
            query.append(getKeyColumns().get(0)).append(" > ").append(getLastId()).append(" ");

        } else if (getMarkMethod() == MarkMethod.TIMESTAMP) {
            query.append(getTimestampColumnName()).append(" IS NULL ");

        } else {
            throw new SessionInternalError("Marking method not configured, 'id' or 'timestamp_column' not set.");
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
     * If MarkMethod.TIMESTAMP, this method will generate the SQL necessary to update the timestamps
     * of the read records, to be executed when the batch has been completely processed.
     *
     * If MarkMethod.LAST_ID, this method will increment the "last read ID" (see {@link #getLastId()}
     * field value with the id of the record that was read. This does not write the property out to the
     * database, as to prevent pre-maturely marking a record as read should a subsequent read throw an
     * exception that prevents the batch from being processed (see {@link #batchRead(java.util.List, int[])}.
     *
     * @param record record that was read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    @Override
    protected void recordRead(final Record record, final int[] keyColumnIndexes) {
        if (getMarkMethod() == MarkMethod.TIMESTAMP) {
            if (timestampUpdateSql == null) {
                timestampUpdateSql = buildTimestampUpdateSql(record, keyColumnIndexes);
                LOG.debug("Timestamp update SQL: '" + timestampUpdateSql + "'");
            }
        }

        if (getMarkMethod() == MarkMethod.LAST_ID) {
            setLastId(record.getFields().get(keyColumnIndexes[0]).getIntValue());
        }
    }

    /**
     * If MarkMethod.TIMESTAMP, this method will execute a batch update and set the
     * time stamp column of the database row for each record read.
     *
     * If MarkMethod.LAST_ID, this method will flush out the "last read ID" preference to the
     * jBilling database after the entire batch has been read.
     *
     * @param records list of records that were read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    @Override
    protected void batchRead(final List<Record> records, final int[] keyColumnIndexes) {
        if (getMarkMethod() == MarkMethod.TIMESTAMP) {
            if (timestampUpdateSql != null && !records.isEmpty())
                executeTimestampUpdateSql(records, keyColumnIndexes);
        }

        if (getMarkMethod() == MarkMethod.LAST_ID) {
            flushLastId();
        }
    }

    private String buildTimestampUpdateSql(Record record, int[] keyColumnIndexes) {
        // build update query to mark timestamps
        StringBuilder query = new StringBuilder()
                .append("UPDATE ")
                .append(getTableName())
                .append(" SET ")
                .append(getTimestampColumnName())
                .append(" = ? ")
                .append(" WHERE ");

        // add primary key constraint using key columns
        for (int i = 0; i < keyColumnIndexes.length; i++) {
            PricingField field = record.getFields().get(keyColumnIndexes[i]);
            query.append(field.getName()).append(" = ? ");

            if (i < keyColumnIndexes.length-1)
                query.append(" AND ");
        }

        return query.toString();
    }

    private void executeTimestampUpdateSql(final List<Record> records, final int[] keyColumnIndexes) {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // execute batch update for all read records
        getJdbcTemplate().batchUpdate(
                timestampUpdateSql,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setTimestamp(1, timestamp);

                        // query parameters for primary key SQL
                        int j = 2; // prepared statement parameter index
                        Record record = records.get(i);
                        for (int key : keyColumnIndexes) {
                            PricingField field = record.getFields().get(key);
                            switch (field.getType()) {
                                case STRING:
                                    ps.setString(j++, field.getStrValue());
                                    break;
                                case INTEGER:
                                    ps.setInt(j++, field.getIntValue());
                                    break;
                                case DECIMAL:
                                    ps.setBigDecimal(j++, field.getDecimalValue());
                                    break;
                                case DATE:
                                    ps.setTimestamp(j++, new Timestamp(field.getDateValue().getTime()));
                                    break;
                                case BOOLEAN:
                                    ps.setBoolean(j++, field.getBooleanValue());
                                    break;
                            }
                        }
                    }

                    public int getBatchSize() {
                        return records.size();
                    }
                });
    }
}
