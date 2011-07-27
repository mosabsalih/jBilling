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

/**
 *
 * @author Brian Cowdery
 * @since 01-12-2009
 */
package com.sapienter.jbilling.server.process;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class PeriodOfTimeTest extends TestCase {

    private static final TimeZone tz = TimeZone.getTimeZone("America/Edmonton"); // Alberta, Canada observes DST
    private static final Calendar calendar = GregorianCalendar.getInstance(tz);

    public PeriodOfTimeTest() {
        super();
    }

    public PeriodOfTimeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendar.clear(); // just to be safe...
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDaysInPeriodDST() throws Exception {
        calendar.clear();
        calendar.set(2009, 2, 1);  // Start Date before DST switchover (March 8th, 2009)
        Date start = calendar.getTime();
        
        calendar.clear();
        calendar.set(2009, 2, 10); // End Date after DST switchover
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(9, period.getDaysInPeriod());
    }

    public void testDaysInPeriod31Days() throws Exception {
        calendar.clear();
        calendar.set(2009, 0, 1);  // Start January 01
        Date start = calendar.getTime();

        calendar.clear();
        calendar.set(2009, 0, 31); // End January 31 
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(30, period.getDaysInPeriod());
    }

    /**
     * Note that based on the current implementation of {@link PeriodOfTime} this should
     * be no different than the above 31 day test case. However this provides a regression
     * test should the implementation change in the future (eg, migration to JodaTime).
     */
    public void testDaysInPeriod30Days() {
        calendar.clear();
        calendar.set(2009, 3, 1);  // Start April 01
        Date start = calendar.getTime();

        calendar.clear();
        calendar.set(2009, 3, 30); // End April 30 
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(29, period.getDaysInPeriod());
    }

    public void testDaysInPeriodMidMonthDays() {
        calendar.clear();
        calendar.set(2009, 0, 5);  // Start January 05
        Date start = calendar.getTime();

        calendar.clear();
        calendar.set(2009, 0, 14); // End January 14
        Date end = calendar.getTime();

        // January 5, 6, 7, 8, 9, 10, 11, 12, 13, = 9 days inclusive
        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(9, period.getDaysInPeriod());
    }

    public void testDaysInPeriodSpanMonths() {
        calendar.clear();
        calendar.set(2009, 0, 1);  // Start January 01
        Date start = calendar.getTime();

        calendar.clear();
        calendar.set(2009, 1, 14); // End February 14
        Date end = calendar.getTime();

        // 31 days + 13 days = 44 days
        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(44, period.getDaysInPeriod());
    }

    /**
     * Demonstrates the effect of {@code calendar.add(Calendar.MONTH, 1)}. The expected
     * result is a resulting days-in-period matching the length of the month, however this
     * fails to consider that #getDaysInPeriod counts start and end days <b>inclusively</b>
     *
     * eg.
     *     Calendar calendar = GregorianCalendar.getInstance();
     *     calendar.set(2009, 1, 1);
     *     Date start = calendar.getTime(); // January 01
     *
     *     calendar.add(Calendar.MONTH, 1);
     *     Date end = calendar.gettime(); // February 01
     *
     *     * January 01 to Feburary 01 is 32 days!
     */
    public void testDaysInPeriodAddMonths() {
        calendar.clear();
        calendar.set(2009, 0, 1);        // Start January 01
        Date start = calendar.getTime();

        calendar.add(Calendar.MONTH, 1); // End February 01
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(31, period.getDaysInPeriod());
    }

    public void testDaysInPeriodEndBeforeStart() throws Exception {
        calendar.clear();
        calendar.set(2009, 1, 1); // Start February 01
        Date start = calendar.getTime();

        calendar.clear();
        calendar.set(2009, 0, 1); // End January 01
        Date end = calendar.getTime();

        // Start date occurs before end date, default to 0 days (not a negative value!)
        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);
        assertEquals(0, period.getDaysInPeriod());
    }
}
 