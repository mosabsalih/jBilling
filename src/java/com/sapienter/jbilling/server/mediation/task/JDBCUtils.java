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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working directly with databases through JDBC connections and hand written SQL queries.
 *
 * It is recommended that you instantiate a JDBC connection using the Spring
 * {@link org.springframework.jdbc.datasource.DataSourceUtils} class instead of creating one manually.
 *
 * These methods expect the given connection to be open and accessible. None of these methods
 * will close the given connection - you must remember to close the connection in your enclosing class!
 * 
 * @author Brian Cowdery
 * @since 27-09-2010
 */
public class JDBCUtils {

    /**
     * Returns the case-corrected table name for the given case-insensitive table name.
     * If the table does not exist, then this method will return null.
     *
     * @param connection connection to use for validating column name case
     * @param table table to validate
     * @return case-corrected table name
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static String correctTableName(Connection connection, String table) throws SQLException {
        if (table == null)
            return null;

        List<String> corrected = correctTableNames(connection, new String[] { table });
        return !corrected.isEmpty() ? corrected.get(0) : null;
    }

    /**
     * Returns a list of case-corrected table names for the given case insensitive array
     * of table names. If the table does not exist, then it will be omitted from
     * the returned list.
     *
     * @param connection connection to use for validating table name case
     * @param tables tables to validate
     * @return list of case-corrected tables names
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static List<String> correctTableNames(Connection connection, String[] tables) throws SQLException {
        List<String> dbTables = getAllTableNames(connection);
        List<String> corrected = new ArrayList<String>(tables.length);

        for (String table : tables)
            for (String dbTable : dbTables)
                if (table.equalsIgnoreCase(dbTable))
                    corrected.add(dbTable);

        return corrected;
    }

    /**
     * Returns a list of all table names in the database schema accessible by
     * the given connection.
     *
     * @param connection connection
     * @return list of table names
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static List<String> getAllTableNames(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<String>();
        
        ResultSet rs = connection.getMetaData().getTables(null, null, null, null);
        while (rs.next()) tables.add(rs.getString(3));
        rs.close();

        return tables;
    }

    /**
     * Returns the case-corrected column name for the given case-insensitive column name.
     * If the column does not exist on the given table, then this method will return null.
     *
     * @param connection connection to use for validating column name case
     * @param tableName table that contains the given columns
     * @param column column to validate
     * @return case-corrected column name
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static String correctColumnName(Connection connection, String tableName, String column) throws SQLException {
        if (column == null) 
            return null;

        List<String> corrected = correctColumnNames(connection, tableName, new String[] { column });
        return !corrected.isEmpty() ? corrected.get(0) : null;
    }

    /**
     * Returns a list of case-corrected column names for the given case insensitive array
     * of column names. If the column does not exist, then it will be omitted from
     * the returned list.
     *
     * @param connection connection to use for validating column name case
     * @param tableName table that contains the given columns
     * @param columns columns to validate
     * @return list of case-corrected column names
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static List<String> correctColumnNames(Connection connection, String tableName, String[] columns)
            throws SQLException {
        
        List<String> dbColumns = getAllColumnNames(connection, tableName);
        List<String> corrected = new ArrayList<String>(columns.length);

        for (String column : columns)
            for (String dbColumn : dbColumns)
                if (column.equalsIgnoreCase(dbColumn))
                    corrected.add(dbColumn);

        return corrected;
    }

    /**
     * Returns a list of all column names of the given table.
     *
     * @param connection connection
     * @param tableName table name
     * @return list of column names
     * @throws java.sql.SQLException if connection fails or meta-data could not be retrieved
     */
    public static List<String> getAllColumnNames(Connection connection, String tableName) throws SQLException {
        List<String> columns = new ArrayList<String>();

        ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, null);
        while (rs.next()) columns.add(rs.getString("COLUMN_NAME"));
        rs.close();

        return columns;
    }
}
