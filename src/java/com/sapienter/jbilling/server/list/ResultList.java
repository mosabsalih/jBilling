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

package com.sapienter.jbilling.server.list;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sapienter.jbilling.server.util.Context;
import com.sun.rowset.CachedRowSetImpl;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

/**
 * Makes a JDBC query using the sql string got as a parameter.
 * It has no clue about the sql to be executed, it simply gets
 * the connection from the pool, makes the query and returns the
 * ResultSet.
 * 
 * In this first cut, it uses a CachedRowSet with all the rows in
 * it to send to the client. No paging from the application server
 * is considered. This is ment for list that are small enough to
 * fit in memory in the web server tier.
 * 
 * @author Emil Created on 27-Feb-2003
 */
public class ResultList {

    protected CachedRowSet cachedResults;
    protected Connection conn = null;

    protected ResultList() {
    }

    protected void prepareStatement(String SQL) throws SQLException {

        // the default is TYPE_SCROLL_INSENSITIVE and CONCUR_READ_ONLY
        // which is good
        cachedResults = new CachedRowSetImpl();

        cachedResults.setCommand(SQL);
        cachedResults.setFetchDirection(ResultSet.FETCH_UNKNOWN);
    // the BL list class will set the parameters of the query
    }

    protected void execute() throws SQLException {
        // the connection will be closed by the RowSet as soon as it
        // finished executing the command
        conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();

        cachedResults.execute(conn);
    }
}
