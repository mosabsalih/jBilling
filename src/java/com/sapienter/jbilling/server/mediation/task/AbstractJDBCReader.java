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

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.util.PreferenceBL;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription.Type.*;

/**
 * AbstractJDBCReader provides a generic base for all JDBC {@link com.sapienter.jbilling.server.mediation.task.IMediationReader} classes.
 *
 * @see JDBCUtils
 * 
 * @author Brian Cowdery
 * @since 27-09-2010
 */
public abstract class AbstractJDBCReader extends AbstractReader {
    private static final Logger LOG = Logger.getLogger(AbstractJDBCReader.class);

    private static final String MEDIATION_DIR = Util.getSysProp("base_dir") + "mediation/";

    // plug-in parameters
    protected static final ParameterDescription PARAM_DATABASE_NAME = new ParameterDescription("database_name", false, STR);
    protected static final ParameterDescription PARAM_TABLE_NAME = new ParameterDescription("table_name", false, STR);
    protected static final ParameterDescription PARAM_KEY_COLUMN_NAME = new ParameterDescription("key_column_name", false, STR);
    protected static final ParameterDescription PARAM_WHERE_APPEND = new ParameterDescription("where_append", false, STR);
    protected static final ParameterDescription PARAM_ORDER_BY = new ParameterDescription("order_by", false, STR);
    protected static final ParameterDescription PARAM_DRIVER = new ParameterDescription("driver", false, STR);
    protected static final ParameterDescription PARAM_URL = new ParameterDescription("url", false, STR);
    protected static final ParameterDescription PARAM_USERNAME = new ParameterDescription("username", false, STR);
    protected static final ParameterDescription PARAM_PASSWORD = new ParameterDescription("password", false, STR);
    protected static final ParameterDescription PARAM_TIMESTAMP_COLUMN_NAME = new ParameterDescription("timestamp_column_name", false, STR);
    protected static final ParameterDescription PARAM_LOWERCASE_COLUMN_NAME = new ParameterDescription("lc_column_names", false, STR);

    // parameter defaults
    protected static final String DATABASE_NAME_DEFAULT = "jbilling_cdr";
    protected static final String TABLE_NAME_DEFAULT = "cdr";
    protected static final String KEY_COLUMN_NAME_DEFAULT = "id";
    protected static final String DRIVER_DEFAULT = "org.hsqldb.jdbcDriver";
    protected static final String USERNAME_DEFAULT = "SA";
    protected static final String PASSWORD_DEFAULT = "";
    protected static final String TIMESTAMP_COLUMN_DEFAULT = "jbilling_timestamp";
    protected static final Boolean LOWERCASE_COLUMN_NAME_DEFAULT = true;

    // initializer for pluggable params
    {
    	descriptions.add(PARAM_DATABASE_NAME);
        descriptions.add(PARAM_TABLE_NAME);
        descriptions.add(PARAM_KEY_COLUMN_NAME);
        descriptions.add(PARAM_WHERE_APPEND);
        descriptions.add(PARAM_ORDER_BY);
        descriptions.add(PARAM_DRIVER);
        descriptions.add(PARAM_URL);
        descriptions.add(PARAM_USERNAME);
        descriptions.add(PARAM_PASSWORD);
        descriptions.add(PARAM_TIMESTAMP_COLUMN_NAME);
        descriptions.add(PARAM_LOWERCASE_COLUMN_NAME);
    }

    public enum MarkMethod { LAST_ID, TIMESTAMP }

    private JdbcTemplate jdbcTemplate;

    private String databaseName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    private String tableName;
    private List<String> keyColumns;
    private MarkMethod markMethod;
    private String timestampColumnName; // if MarkMethod.TIMESTAMP
    private Integer lastId;             // if MarkMethod.LAST_ID

    private boolean useLowercaseNames;

    public boolean validate(List<String> messages) {
        super.validate(messages);
        init();
        return true;
    }

    /**
     * Initializes the reader with plug-in parameters, validating the database table and column names
     * and ensuring that the reader is in a ready state.
     */
    private void init() {
        // data source
        this.databaseName = getParameter(PARAM_DATABASE_NAME.getName(), DATABASE_NAME_DEFAULT);
        this.url = getParameter(PARAM_URL.getName(), "jdbc:hsqldb:" + MEDIATION_DIR + this.databaseName + ";shutdown=true");
        this.username = getParameter(PARAM_USERNAME.getName(), USERNAME_DEFAULT);
        this.password = getParameter(PARAM_PASSWORD.getName(), PASSWORD_DEFAULT);
        this.driverClassName = getParameter(PARAM_DRIVER.getName(), DRIVER_DEFAULT);

        // briefly create a connection to determine case-corrected table and column names
        // then terminate and discard connection as soon as it's not needed.
        DataSource dataSource = getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        
        try {
            this.tableName = JDBCUtils.correctTableName(connection, getParameter(PARAM_TABLE_NAME.getName(), TABLE_NAME_DEFAULT));
            LOG.debug("Table name: '" + getTableName() + "'");

            String[] keyColumns = getParameter(PARAM_KEY_COLUMN_NAME.getName(), KEY_COLUMN_NAME_DEFAULT).split(",");
            this.keyColumns = JDBCUtils.correctColumnNames(connection, this.tableName, keyColumns);
            LOG.debug("Key column names: " + getKeyColumns());

            String timestampColumnName = getParameter(PARAM_TIMESTAMP_COLUMN_NAME.getName(), TIMESTAMP_COLUMN_DEFAULT);
            this.timestampColumnName = JDBCUtils.correctColumnName(connection, this.tableName, timestampColumnName);
            LOG.debug("Timestamp marker column name: '" + getTimestampColumnName() + "'");

        } catch (SQLException e) {
            throw new SessionInternalError("Could not validate table or column names against the database.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        // determine marking method for this reader
        this.markMethod = getTimestampColumnName() != null ? MarkMethod.TIMESTAMP : MarkMethod.LAST_ID;
        LOG.debug("Using marking method " + getMarkMethod());

        // force lowercase PricingField names ?
        this.useLowercaseNames = getParameter(PARAM_LOWERCASE_COLUMN_NAME.getName(), LOWERCASE_COLUMN_NAME_DEFAULT);

        // build a Spring JdbcTemplate
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcTemplate.setMaxRows(getBatchSize());
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUrl(getUrl());
        dataSource.setUsername(getUsername());
        dataSource.setPassword(getPassword());

        return dataSource;
    }

    public String getTableName() {
        return tableName;
    }    

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public MarkMethod getMarkMethod() {
        return markMethod;
    }

    public void setMarkMethod(MarkMethod markMethod) {
        this.markMethod = markMethod;
    }

    public String getTimestampColumnName() {
        return timestampColumnName;
    }

    public Integer getLastId() {
        if (lastId == null) readLastId();
        return lastId;
    }

    public void setLastId(Integer lastId) {
        this.lastId = lastId;
    }

    /**
     * Reads the "last read ID" preference for this entity and sets the lastId field value. 
     */
    protected Integer readLastId() {
        PreferenceBL preference = new PreferenceBL();
        try {
            preference.set(getEntityId(), Constants.PREFERENCE_MEDIATION_JDBC_READER_LAST_ID);
        } catch (EmptyResultDataAccessException fe) {
            /* use default */
        }

        lastId = preference.getInt();
        LOG.debug("Fetched 'last read ID' preference: " + lastId);
        return lastId;
    }

    /**
     * Updates the mediation "last read ID" preference with the current lastId field value.
     */
    protected void flushLastId() {
        LOG.debug("Updating 'last read ID' preference to: " + getLastId());
        PreferenceBL preferenceBL = new PreferenceBL();
        preferenceBL.createUpdateForEntity(getEntityId(),
                                           Constants.PREFERENCE_MEDIATION_JDBC_READER_LAST_ID,
                                           getLastId());
    }

    /**
     * Returns true if read {@link com.sapienter.jbilling.server.item.PricingField} names should be
     * in lower case. If false, column names can be used as-is with no case-shifting.
     * 
     * @return true if PricingField names should be in lowercase
     */
    public boolean useLowercaseNames() {
        return useLowercaseNames;
    }

    /**
     * Returns a JDBC record iterator that reads batches of records from the database.
     *
     * @return record iterator
     */
    public Iterator<List<Record>> iterator() {
        try {
            return new Reader(getJdbcTemplate());
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * Internal Record iterator class. 
     */
    public class Reader implements Iterator<List<Record>> {
        private JdbcTemplate jdbcTemplate;
        private PricingField.Type[] columnTypes;
        private String[] columnNames;
        private int[] keyColumnIndexes;
        private List<Record> records;

        protected Reader(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        public boolean hasNext() {
            records = getNextBatch();
            return !records.isEmpty();
        }

        public List<Record> next() {
            if (records.isEmpty())
                throw new NoSuchElementException();

            // return a defensive copy
            return new ArrayList<Record>(records);
        }

        private List<Record> getNextBatch() {            
            // fetch records
            String query = getSqlQueryString();
            SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

            // fill metadata info in first time
            if (columnNames == null)
                parseMetaData(rs);

            List<Record> records = new ArrayList<Record>(getBatchSize());
            while (rs.next()) {
                Record record = new Record();

                for (int i = 0; i < columnTypes.length; i++) {
                    String name = columnNames[i];
                    boolean index = isKeyIndex(i);

                    switch (columnTypes[i]) {
                        case STRING:
                            record.addField(new PricingField(name, rs.getString(i + 1)), index);
                            break;

                        case INTEGER:
                            record.addField(new PricingField(name, rs.getInt(i + 1)), index);
                            break;

                        case DECIMAL:
                            record.addField(new PricingField(name, rs.getBigDecimal(i + 1)), index);
                            break;

                        case DATE:
                            record.addField(new PricingField(name, rs.getTimestamp(i + 1)), index);
                            break;

                        case BOOLEAN:
                            record.addField(new PricingField(name, rs.getBoolean(i + 1)), index);
                            break;
                    }
                }

                recordRead(record, keyColumnIndexes);
                records.add(record);
            }

            batchRead(records, keyColumnIndexes);
            return records;
        }

        /**
         * Sets the column info (names, types, key) from the database meta-data.
         *
         * @param records database rows to read
         */
        private void parseMetaData(SqlRowSet records) {
            SqlRowSetMetaData metaData = records.getMetaData();
            columnTypes = new PricingField.Type[metaData.getColumnCount()];
            columnNames = new String[metaData.getColumnCount()];
            List<Integer> keyColumns = new LinkedList<Integer>();

            for (int i = 0; i < columnTypes.length; i++) {
                // set column types of the result set
                switch (metaData.getColumnType(i + 1)) {
                    case Types.CHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.VARCHAR:
                        columnTypes[i] = PricingField.Type.STRING;
                        break;

                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        columnTypes[i] = PricingField.Type.INTEGER;
                        break;

                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.NUMERIC:
                    case Types.REAL:
                        columnTypes[i] = PricingField.Type.DECIMAL;
                        break;

                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        columnTypes[i] = PricingField.Type.DATE;
                        break;

                    case Types.BIT:
                    case Types.BOOLEAN:
                        columnTypes[i] = PricingField.Type.BOOLEAN;
                        break;

                    default:
                        throw new SessionInternalError("Unsupported java.sql.type " + metaData.getColumnTypeName(i + 1)
                                                       + " for column '" + metaData.getColumnName(i + 1) + "'.");
                }

                // set column names
                if (useLowercaseNames()) {
                    columnNames[i] = metaData.getColumnName(i + 1).toLowerCase();
                } else {
                    columnNames[i] = metaData.getColumnName(i + 1);
                }

                // check if primary key
                for (String name : getKeyColumns()) {
                    if (columnNames[i].equalsIgnoreCase(name)) {
                        keyColumns.add(i);
                    }
                }
            }

            if (keyColumns.isEmpty()) {
                throw new SessionInternalError("No primary key column(s) found in result set.");
            } else {
                keyColumnIndexes = new int[keyColumns.size()];
                int i = 0;
                for (Integer index : keyColumns) {
                    keyColumnIndexes[i] = index;
                    i++;
                }
            }
        }

        /**
         * Returns if the column at this index is a key column.
         * 
         * @return true if the value at the given index represents a key column
         */
        private boolean isKeyIndex(int index) {
            for (int i : keyColumnIndexes)
                if (i == index)
                    return true;
            return false;
        }

        /**
         * {@link java.util.Iterator#remove()} is not supported by this implementation.
         */
        public void remove() {
            throw new UnsupportedOperationException("remove() operation not supported.");
        }
    }


    /*
        Implementation hooks
     */

    /**
     * Returns an SQL query to read records from the database. This query may optionally use
     * the reader {@link com.sapienter.jbilling.server.mediation.task.AbstractJDBCReader.MarkMethod} ({@link #getMarkMethod()} to limit records to those that
     * have not been previously read.
     *
     * @return SQL query string
     */
    protected abstract String getSqlQueryString();

    /**
     * Called after each record is read.
     *
     * Can be used to perform an action after each record is read from the database, this
     * is commonly used to mark that a record has been read before hitting the database for the
     * next read operation.
     *
     * @param record record that was read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    protected abstract void recordRead(final Record record, final int[] keyColumnIndexes);

    /**
     * Called after each complete batch of records is read.
     *
     * Can be used to perform an action after each batch of records is read from the database,
     * commonly used to persist a list of changes marking records as read in the database.
     *
     * @param records list of records that were read
     * @param keyColumnIndexes index of record PricingFields that represent key columns.
     */
    protected abstract void batchRead(final List<Record> records, final int[] keyColumnIndexes);

}
