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

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.process.IBillingProcessSessionBean;
import com.sapienter.jbilling.server.util.Context;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;

import java.util.Date;

/**
 * AgeingProcessTask
 *
 * @author Brian Cowdery
 * @since 29/04/11
 */
public class AgeingProcessTask extends AbstractBackwardSimpleScheduledTask {

    private static final Logger LOG = Logger.getLogger(AgeingProcessTask.class);

    private static final String PROPERTY_RUN_AGEING = "process.run_ageing";

    public String getTaskName() {
        return "ageing process: entity " + getEntityId() + ", " + getScheduleString();
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        super.execute(context); // _init(context);

        IBillingProcessSessionBean
                billing = (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);

        if (Util.getSysPropBooleanTrue(PROPERTY_RUN_AGEING)) {
            LOG.info("Starting ageing for entity " + getEntityId() + " at " + new Date());
            billing.reviewUsersStatus(getEntityId(), new Date());
            LOG.info("Ended ageing at " + new Date());
        }
    }

    /**
     * Returns the scheduled trigger for the ageing process. If the plug-in is missing
     * the {@link com.sapienter.jbilling.server.process.task.AbstractSimpleScheduledTask}
     * parameters use the the default jbilling.properties process schedule instead.
     *
     * @return billing trigger for scheduling
     * @throws com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException thrown if properties or plug-in parameters could not be parsed
     */
    @Override
    public SimpleTrigger getTrigger() throws PluggableTaskException {
        SimpleTrigger trigger = super.getTrigger();

        // trigger start time and frequency using jbilling.properties unless plug-in
        // parameters have been explicitly set to define the ageing schedule
        if (useProperties()) {
            LOG.debug("Scheduling ageing process from jbilling.properties ...");
            trigger= setTriggerFromProperties(trigger);
        } else {
            LOG.debug("Scheduling ageing process using plug-in parameters ...");
        }

        return trigger;
    }
}
