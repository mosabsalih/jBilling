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

package com.sapienter.jbilling.server.pluggableTask;

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import junit.framework.TestCase;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * BasicCompositionTaskTest
 *
 * @author Brian Cowdery
 * @since 13/09/11
 */
public class BasicCompositionTaskTest extends TestCase {

    private final TimeZone defaultTimeZone = TimeZone.getDefault();

    /**
     * BasicCompositionTask extended for testing. The locale is settable and this
     * will never attempt to look up the entity preference for appending the order
     * id to the invoice line.
     *
     * This class is needed so that the invoice line description composition can be
     * tested without the need for a live container.
     */
    private class TestBasicCompositionTask extends BasicCompositionTask {

        private Locale locale = Locale.getDefault();

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        @Override
        protected Locale getLocale(Integer userId) {
            return locale; // for testing, return whatever locale is set
        }

        @Override
        protected boolean appendOrderId(Integer entityId) {
            return false; // for testing, never append the order ID
        }
    }

    // class under test
    private TestBasicCompositionTask task = new TestBasicCompositionTask();


    public BasicCompositionTaskTest() {
    }

    public BasicCompositionTaskTest(String name) {
        super(name);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // reset timezone back to default
        TimeZone.setDefault(defaultTimeZone);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(defaultTimeZone));
    }

    public void testComposeDescription() {
        // period being processed
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.clear();

        calendar.set(2011, Calendar.SEPTEMBER, 1);
        Date start = calendar.getTime();

        calendar.set(2011, Calendar.OCTOBER, 1);
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);

        // verify description
        String description = task.composeDescription(getMockOrder(), period, "Line description");
        assertEquals("Line description Period from 09/01/2011 to 09/30/2011", description);
    }

    public void testComposeDescriptionTZ() {
        // try composing in a different time zone.
        TimeZone EDT = TimeZone.getTimeZone("EDT");
        TimeZone.setDefault(EDT);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(EDT));

        // period being processed
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.clear();

        calendar.set(2011, Calendar.SEPTEMBER, 1);
        Date start = calendar.getTime();

        calendar.set(2011, Calendar.OCTOBER, 1);
        Date end = calendar.getTime();

        PeriodOfTime period = new PeriodOfTime(start, end, 0, 0);

        // verify description, different timezone shouldn't have affected the dates
        String description = task.composeDescription(getMockOrder(), period, "Line description");
        assertEquals("Line description Period from 09/01/2011 to 09/30/2011", description);
    }

    private OrderDTO getMockOrder() {
        UserDTO user = new UserDTO(1);
        user.setCompany(new CompanyDTO(1));

        OrderDTO order = new OrderDTO();
        order.setBaseUserByUserId(user);
        order.setOrderPeriod(new OrderPeriodDTO(2)); // not a one time period

        return order;
    }
}
