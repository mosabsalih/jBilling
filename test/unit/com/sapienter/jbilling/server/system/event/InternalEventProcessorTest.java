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
package com.sapienter.jbilling.server.system.event;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.process.event.NewUserStatusEvent;
import com.sapienter.jbilling.server.system.event.task.IInternalEventsTask;
import junit.framework.TestCase;

/**
 * @author Brian Cowdery
 * @since 08-04-2010
 */
@SuppressWarnings("unchecked")
public class InternalEventProcessorTest extends TestCase {

    // class under test
    public static final InternalEventProcessor processor = new InternalEventProcessor();

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsProcessable() throws Exception {
        TestInternalEventTask task = new TestInternalEventTask();
        task.setSubscribedEvents(new Class[]{ TestEvent.class });

        assertTrue("subscribed to TestEvent, processing TestEvent",
                   processor.isProcessable(task, new TestEvent()));
    }

    public void testIsProcessableNegativeCase() {
        TestInternalEventTask task = new TestInternalEventTask();
        task.setSubscribedEvents(new Class[]{ TestEvent.class });

        assertFalse("subscribed to NewContactEvent, processing TestEvent",
                    processor.isProcessable(task, new NewUserStatusEvent(1, 2, 3, 4)));
    }

    public void testIsProcessableCatchAll() throws Exception {
        TestInternalEventTask task = new TestInternalEventTask();
        task.setSubscribedEvents(new Class[]{ CatchAllEvent.class });
        
        assertTrue("subscribed to CatchAllEvents (any and all events accepted), processing TestEvent",
                   processor.isProcessable(task, new TestEvent()));
    }

    public void testIsProcessableEmptySubscribedEvents() throws Exception {
        TestInternalEventTask task = new TestInternalEventTask();
        task.setSubscribedEvents(new Class[]{});

        assertFalse("not subscribed to any event, processing TestEvent",
                    processor.isProcessable(task, new TestEvent()));
    }

    public void testIsProcessableNullSubscribedEvents() throws Exception {
        TestInternalEventTask task = new TestInternalEventTask();
        task.setSubscribedEvents(null);

        assertFalse("subscribed event list is null, processing TestEvent",
                    processor.isProcessable(task, new TestEvent()));
    }

    /**
     * Test event class
     */
    public static class TestEvent implements Event {
        public String getName() { return "test event"; }
        public Integer getEntityId() { return null; }
    }

    /**
     * Test internal event plug-in
     */
    public static class TestInternalEventTask implements IInternalEventsTask {
        private Class<Event>[] events = new Class[]{};
        
        public Class<Event>[] getSubscribedEvents() { return events; }
        public void setSubscribedEvents(Class<Event>[] events) { this.events = events; }

        public void process(Event event) throws PluggableTaskException { /* noop */ }
    }
}
