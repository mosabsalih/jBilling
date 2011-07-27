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

import com.sapienter.jbilling.server.mediation.Record;
import junit.framework.TestCase;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Cowdery
 * @since 28-09-2010
 */
public class JDBCReaderTest extends TestCase {

    // local in-memory database for testing
    private static final String DATABASE_NAME = "jdbc_reader_test";
    private static final String TABLE_NAME = "records";
    private static final String URL = "jdbc:hsqldb:mem:" + DATABASE_NAME;
    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private JdbcTemplate jdbcTemplate;

    // plug-in parameters
    private static Map<String, String> parameters;
    static {
        parameters = new HashMap<String, String>();
        parameters.put("database_name", DATABASE_NAME);
        parameters.put("table_name", TABLE_NAME);
        parameters.put("url", URL);
        parameters.put("username", USERNAME);
        parameters.put("password", PASSWORD);
        parameters.put("batch_size", "100");
    }

    // class under test
    private AbstractJDBCReader reader = new JDBCReader();

    public JDBCReaderTest() {
    }

    public JDBCReaderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        dataSource.setUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        jdbcTemplate = new JdbcTemplate(dataSource);
        createTestSchema();

        reader.setParameters(parameters);
        reader.validate(new ArrayList<String>());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
        Testing database setup
     */

    private void createTestSchema() {
        String sql =
                "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
                + " CREATE TABLE " + TABLE_NAME + "("
                + "    id INTEGER NOT NULL, "
                + "    content VARCHAR(255) NOT NULL, "
                + "    jbilling_timestamp TIMESTAMP NULL, "
                + "    PRIMARY KEY (id) "
                + " );";

        jdbcTemplate.execute(sql);
    }

    private void fillTestDatabase(int rows) {
        fillTestDatabase(rows, 0);
    }

    private void fillTestDatabase(int rows, int startingId) {
        for (int i = 0; i < rows; i++) {
            jdbcTemplate.update("INSERT INTO " + TABLE_NAME + " (id, content) VALUES (?, ?);",
                                new Object[] { startingId + i, "row number " + i});
        }
    }

    /*
        Tests
     */

    public void testReaderConfig() throws Exception {                
        assertEquals(DATABASE_NAME, reader.getDatabaseName());
        assertEquals(DRIVER, reader.getDriverClassName());
        assertEquals(URL, reader.getUrl());
        assertEquals(USERNAME, reader.getUsername());
        assertEquals(PASSWORD, reader.getPassword());

        // case corrected table/column names
        // table/column names are configured in lowercase, HSQLDB uses uppercase internally
        assertEquals("RECORDS", reader.getTableName());
        assertEquals("ID", reader.getKeyColumns().get(0));
        assertEquals("JBILLING_TIMESTAMP", reader.getTimestampColumnName());

        // the "jbilling_timestamp" column exits in our test db
        // record marking method set to TIMESTAMP
        assertEquals(AbstractJDBCReader.MarkMethod.TIMESTAMP, reader.getMarkMethod());

        // generated query string
        assertEquals("SELECT * FROM RECORDS WHERE JBILLING_TIMESTAMP IS NULL ORDER BY ID", reader.getSqlQueryString());
    }

    public void testBatchRead() throws Exception {
        fillTestDatabase(500); // 5 batches (100 per batch)

        StopWatch read = new StopWatch("read 500 rows from HSQLDB");
        read.start();

        int rowcount = 0;
        int iterations = 0;
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
            iterations++;
        }

        read.stop();
        System.out.println(read.shortSummary());        

        assertEquals("500 rows read", 500, rowcount);
        assertEquals("5 iterations to read", 5, iterations);
    }

    // NOTE: this test takes approximately 3.5 minutes, comment out to improve testing performance
    
    /**
     * Reader stress test. Reads a large volume of records to ensure that there are no memory
     * leaks adverse effects of reading in large volumes of data.
     *
     * @throws Exception jUnit thinks crazy things can happen
     */
    public void testLargeRead() throws Exception {
        fillTestDatabase(150000); // 1500 batches (100 per batch)

        StopWatch read = new StopWatch("read 150000 rows from HSQLDB");
        read.start();
        
        int rowcount = 0;
        int iterations = 0;
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
            iterations++;
        }

        read.stop();
        System.out.println(read.shortSummary());
        
        assertEquals("150000 rows read", 150000, rowcount);
        assertEquals("1500 iterations to read", 1500, iterations);
    }

    public void testMultipleReads() throws Exception {
        int rowcount = 0;

        // read 100 records
        fillTestDatabase(100, 0);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("100 rows read", 100, rowcount);

        // read again, no new records inserted
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("100 rows read, no new rows", 100, rowcount);

        // read another 100 records
        fillTestDatabase(100, 100);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("200 rows read", 200, rowcount);
    }

    public void testPartialBatchRead() throws Exception {
        int rowcount = 0;
        int iterations = 0;

        // read 133 records
        fillTestDatabase(133);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
            iterations++;
        }
        assertEquals("133 rows read", 133, rowcount);
        assertEquals("2 iterations to read", 2, iterations);

        // read again, no new records inserted
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("133 rows read, no new rows", 133, rowcount);
    }

    public void testTimestampMarking() throws Exception {
        int rowcount = 0;

        // read 100 records, verify timestamps are set
        fillTestDatabase(100, 0);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("100 rows read", 100, rowcount);
        assertAllTimestampsSet();

        // read another 100 records, verify timestamps are set
        fillTestDatabase(100, 100);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("200 rows, 100 new rows", 200, rowcount);
        assertAllTimestampsSet();

        // read a partial batch, verify timestamps are set
        fillTestDatabase(73, 200);
        for (List<Record> records : reader) {
            rowcount = rowcount + records.size();
        }
        assertEquals("273 rows, 73 new rows", 273, rowcount);
        assertAllTimestampsSet();
    }

    public void assertAllTimestampsSet() {
        int rows = jdbcTemplate.queryForInt("SELECT count(*) from " + TABLE_NAME + " WHERE jbilling_timestamp is null");
        assertEquals("all rows should be timestamped", 0, rows);
    }
}
