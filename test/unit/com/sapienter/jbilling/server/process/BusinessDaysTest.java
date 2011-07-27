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

package com.sapienter.jbilling.server.process;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * BusinessDaysTest
 *
 * @author Brian Cowdery
 * @since 29/04/11
 */
public class BusinessDaysTest extends TestCase {

    private static final Calendar calendar = GregorianCalendar.getInstance();

    // class under test
    BusinessDays businessDays = new BusinessDays();

    public BusinessDaysTest() {
    }

    public BusinessDaysTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendar.clear();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddBusinessDays() {
        // test add days over Saturday and Sunday
        calendar.set(2010, Calendar.APRIL, 29);                          // friday april 29th
        Date date = businessDays.addBusinessDays(calendar.getTime(), 3); // + 3 business days

        calendar.set(2010, Calendar.MAY, 4);
        assertEquals(date, calendar.getTime()); // should equal wednesday may 4th


        // test add days over Sunday
        calendar.set(2010, Calendar.MAY, 1);                        // sunday may 1st
        date = businessDays.addBusinessDays(calendar.getTime(), 3); // + 3 business days

        calendar.set(2010, Calendar.MAY, 5);
        assertEquals(date, calendar.getTime()); // should equal thursday may 5th
    }
}
