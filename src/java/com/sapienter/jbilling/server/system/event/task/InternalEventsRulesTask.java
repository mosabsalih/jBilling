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

package com.sapienter.jbilling.server.system.event.task;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.rule.RulesBaseTask;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.util.Context;

/**
 * InternalEventsRulesTask is a rules-based plug-in that can respond 
 * to interal events. The events it subscribes to is configured in 
 * Spring using the file jbilling-internal-events-rules-tasks.xml. 
 * It inserts into the rules memory context the received event object,
 * plus the publically accessible objects the event contains.
 */
public class InternalEventsRulesTask extends RulesBaseTask
        implements IInternalEventsTask {

    private static final Class<Event>[] DEFAULT_SUBSCRIBED_EVENTS = 
            new Class[] { };

    @Override
    protected Logger getLog() {
        return Logger.getLogger(InternalEventsRulesTask.class);
    }

    /**
     * Returns the subscribed events from the Spring configuration.
     */
    public Class<Event>[] getSubscribedEvents() {
        // get the configuration
        Map<String, List<String>> config = (Map<String, List<String>>) 
                Context.getBean(Context.Name.INTERNAL_EVENTS_RULES_TASK_CONFIG);
        List<String> classNames = config.get(getTaskId().toString());

        // not configured yet?
        if (classNames == null) {
            LOG.info("No configuration found for InternalEventsRulesTask " +
                    "with task id: " + getTaskId());
            return DEFAULT_SUBSCRIBED_EVENTS;
        }

        Class<Event>[] events = new Class[classNames.size()];
        int i = 0;
        for (String className : classNames) {
            try {
                events[i] = (Class<Event>) Class.forName(className);
                i++;
            } catch (Exception e) {
                throw new SessionInternalError("Exception getting event " +
                        "Class object: " + className + " configured for task: " + 
                        getTaskId() + " ", InternalEventsRulesTask.class, e);
            }
        }

        return events;
    }

    /**
     * Processes the event by placing the event object and the objects
     * it contains into the rules memory context. Executes the rules. 
     */
    public void process(Event event) throws PluggableTaskException {
        // add event
        rulesMemoryContext.add(event);

        // Extract fields from concrete event type using reflection
        // and add to rules memory context.
        try {
            Class<?> eventClass = event.getClass();
            Method methods[] = eventClass.getMethods();
            for (Method method : methods) {
                // If method starts with 'get', returns an Object and
                // takes no parameters, execute it and save result for 
                // rules memory context.
                if (method.getName().startsWith("get") &&
                        !method.getReturnType().isPrimitive() &&
                        method.getParameterTypes().length == 0) {
                    rulesMemoryContext.add(method.invoke(event));
                }
            }
            executeRules();
        } catch (Exception e) {
            throw new PluggableTaskException("Error extracting event fields.", e);
        }
   }
}
