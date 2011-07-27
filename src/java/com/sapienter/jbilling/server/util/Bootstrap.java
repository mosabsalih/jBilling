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

package com.sapienter.jbilling.server.util;

import com.sapienter.jbilling.client.process.JobScheduler;
import com.sapienter.jbilling.client.process.Trigger;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.task.IScheduledTask;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

/**
 * Spring bean that bootstraps jBilling services on application start.
 *
 * @author Brian Cowdery
 * @since 22-09-2010
 */
public class Bootstrap {
    private static final Logger LOG = Logger.getLogger(Bootstrap.class);

    public void init() {
        scheduleBatchJobs();
        schedulePluggableTasks();
                                          
        LOG.debug("Starting the job scheduler");
        JobScheduler.getInstance().start();
    }

    public void destroy() {
        LOG.debug("Shutting down the job scheduler");
        JobScheduler.getInstance().shutdown();
    }

    /**
     * Schedule all core jBilling batch processes.
     */
    private void scheduleBatchJobs() {
        // todo: refactor "Trigger" into separate scheduled Job classes.
        Trigger.Initialize();
    }

    /**
     * Schedule all configured {@link IScheduledTask} plug-ins for each entity.
     */
    private void schedulePluggableTasks() {
        JobScheduler scheduler = JobScheduler.getInstance();
        try {
            for (CompanyDTO entity : new CompanyDAS().findEntities()) {
                PluggableTaskManager<IScheduledTask> manager =
                        new PluggableTaskManager<IScheduledTask>
                                (entity.getId(), com.sapienter.jbilling.server.util.Constants.PLUGGABLE_TASK_SCHEDULED);

                LOG.debug("Processing " + manager.getAllTasks().size() + " scheduled tasks for entity " + entity.getId());
                for (IScheduledTask task = manager.getNextClass(); task != null; task = manager.getNextClass()) {
                    try {
                        scheduler.getScheduler().scheduleJob(task.getJobDetail(), task.getTrigger());
                        LOG.debug("Scheduled: [" + task.getTaskName() + "]");
                    } catch (PluggableTaskException e) {
                        LOG.warn("Failed to schedule pluggable task [" + task.getTaskName() + "]");
                    } catch (SchedulerException e) {
                        LOG.warn("Failed to schedule pluggable task [" + task.getTaskName() + "]");
                    }                    
                }
            }
        } catch (PluggableTaskException e) {
            LOG.error("Exception occurred scheduling pluggable tasks.", e);
        }
    }
}
