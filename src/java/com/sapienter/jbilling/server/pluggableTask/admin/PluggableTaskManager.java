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

/*
 * Created on Apr 15, 2003
 *
 */
package com.sapienter.jbilling.server.pluggableTask.admin;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.util.Context;
import java.util.ArrayList;

public class PluggableTaskManager<T> {

    private static final Logger LOG = Logger.getLogger(PluggableTaskManager.class);

    private List<PluggableTaskDTO> classes = null;

    private Iterator it = null;

    private int lastProcessingOrder;

    public PluggableTaskManager(Integer entityId, Integer taskCategory)
            throws PluggableTaskException {

        try {
            lastProcessingOrder = 0;

            classes = new ArrayList<PluggableTaskDTO>();
            classes.addAll(((PluggableTaskDAS) Context.getBean(Context.Name.PLUGGABLE_TASK_DAS)).findByEntityCategory(
                    entityId, taskCategory));

            it = classes.iterator();
            LOG.debug("total classes = " + classes.size());

        } catch (Exception e) {
            throw new PluggableTaskException(e);
        }
    }

    public List<PluggableTaskDTO> getAllTasks() {
        return classes;
    }

    public T getNextClass() throws PluggableTaskException {
        if (it != null && it.hasNext()) {
            PluggableTaskDTO aRule = (PluggableTaskDTO) it.next();

            // check if the order by is in place
            int processingOrder = aRule.getProcessingOrder().intValue();
            // this is helpful also to identify bad data in the table
            if (processingOrder <= lastProcessingOrder) {
                // means that the results are not ordered !
                LOG.fatal("Results of processing tasks are not ordered");
                throw new PluggableTaskException("Processing tasks not ordered");
            }
            lastProcessingOrder = processingOrder;

            String className = aRule.getType().getClassName();
            String interfaceName = aRule.getType().getCategory().getInterfaceName();

            LOG.debug("Applying task " + className);

            return getInstance(className, interfaceName, aRule);
        }

        return null;

    }

    public T getInstance(String className, String interfaceName, PluggableTaskDTO aRule)
            throws PluggableTaskException {
        try {
            Class task = Class.forName(className);
            Class taskInterface = Class.forName(interfaceName);

            if (taskInterface.isAssignableFrom(task)) {
                T thisTask = (T) task.newInstance();
                ((PluggableTask) thisTask).initializeParamters(aRule);
                return thisTask;

            }
            throw new PluggableTaskException("The task " + className + " is not implementing "
                    + interfaceName);

        } catch (ClassNotFoundException e) {
            throw new PluggableTaskException("Can't find the classes for this" + " task. Class: "
                    + className + " Interface: " + interfaceName, e);
        } catch (Exception e) {
            throw new PluggableTaskException(e);
        }

    }
    
    public static Object getInstance(String className, String interfaceName) 
            throws PluggableTaskException {
        try {
            Class task = Class.forName(className);
            Class taskInterface = Class.forName(interfaceName);

            if (taskInterface.isAssignableFrom(task)) {
                Object thisTask = (Object) task.newInstance();
                return thisTask;

            }
            throw new PluggableTaskException("The task " + className
                    + " is not implementing " + interfaceName);

        } catch (ClassNotFoundException e) {
            throw new PluggableTaskException("Can't find the classes for this"
                    + " task. Class: " + className + " Interface: "
                    + interfaceName, e);
        } catch (Exception e) {
            throw new PluggableTaskException(e);
        }

    }
}
