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
package com.sapienter.jbilling.server.process.task;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * Scheduled tasks that will be added to the Quartz Scheduler instance at application start up. All
 * Scheduled tasks must return a valid JobDetail and Trigger item for scheduling, but you may use
 * covariant return types in implementing classes to use a specific type of Trigger.
 *
 * This task is implemented in 2 separate abstract base classes to make adding new IScheduledTask
 * plug-ins easier. These are the {@link AbstractCronTask} and the {@link AbstractSimpleScheduledTask}
 * which provide (respectively) Quartz CronTrigger and StandardTrigger configuration. 
 *
 * @link http://www.quartz-scheduler.org/docs/
 *
 * @author Brian Cowdery
 * @since 02-02-2010
 */
public interface IScheduledTask extends Job {
    public String getTaskName();
    public JobDetail getJobDetail() throws PluggableTaskException;
    public Trigger getTrigger() throws PluggableTaskException;
}
