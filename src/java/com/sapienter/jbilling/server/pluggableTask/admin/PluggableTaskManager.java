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
import org.springframework.util.ClassUtils;

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

    /**
     * Get a plug-in instance initialized using the parameters from the given PluggableTaskDTO entity. Used to
     * load and initialize a plug-in from the database.
     *
     * @param className class name to get instance of
     * @param interfaceName plug-in interface
     * @param pluggableTask pluggable task entity to initialize plug-in parameters from
     * @return instance of the plug-in class initialized with parameters
     * @throws PluggableTaskException throw if an unhandled exception occurs
     */
    @SuppressWarnings("unchecked")
    public T getInstance(String className, String interfaceName, PluggableTaskDTO pluggableTask)
            throws PluggableTaskException {

        try {
            T instance = (T) getInstance(className, interfaceName);
            ((PluggableTask) instance).initializeParamters(pluggableTask);
            return instance;

        } catch (Exception e) {
            throw new PluggableTaskException("Unhandled exception initializing plug-in instance", e);
        }
    }

    /**
     * Get a plug-in instance for the given class name, ensuring that the resulting instance matches
     * the desired plug-in interface.
     *
     * @param className class name to get instance of
     * @param interfaceName plug-in interface
     * @return instance of the plug-in class
     * @throws PluggableTaskException thrown if plug-in class or interface could not be found, or if plug-in
     *                                does not implement the interface.
     */
    public static Object getInstance(String className, String interfaceName) throws PluggableTaskException {
        try {
            Class task = getClass(className);
            Class iface =  getClass(interfaceName);

            if (task == null) {
                throw new PluggableTaskException("Could not load plug-in class '" + className + "', class not found.");
            }

            if (iface == null) {
                throw new PluggableTaskException("Could not load interface '" + interfaceName + "', class not found.");
            }

            if (!iface.isAssignableFrom(task)) {
                throw new PluggableTaskException("Plug-in '" + className + "' does not implement '" + interfaceName + "'");
            }

            LOG.debug("Creating a new instance of " + className);
            return task.newInstance();

        } catch (Exception e) {
            throw new PluggableTaskException("Unhandled exception fetching plug-in instance", e);
        }
    }

    /**
     * Attempts to fetch the class by name. This method goes through all the common class loaders
     * allow the loading of plug-ins from 3rd party libraries and to ensure portability across containers.
     *
     * @param className class to load
     * @return class if name found and loadable, null if class could not be found in any class loader
     */
    private static Class getClass(String className) {
        // attempt to load from the thread class loader
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            return loader.loadClass(className);

        } catch (ClassNotFoundException e) {
            LOG.debug("Cannot load class from the current thread context class loader.");
        }

        // last ditch attempt to load from whatever class loader was used to execute this code
        try {
            return Class.forName(className);

        } catch (ClassNotFoundException e) {
            LOG.fatal("Cannot load class from the caller class loader.", e);
        }

        return null;
    }

}
