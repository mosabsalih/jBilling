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
 * Created on Dec 10, 2004
 *
 */
package com.sapienter.jbilling.server.pluggableTask;

import java.util.GregorianCalendar;


import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.process.BillingProcessBL;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.PreferenceBL;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * @author Emil
 *
 */
public class OrderFilterAnticipatedTask extends BasicOrderFilterTask {
    
    private static final Logger LOG = Logger.getLogger(OrderFilterAnticipatedTask.class);
    
    public boolean isApplicable(OrderDTO order, 
            BillingProcessDTO process) throws TaskException {
        // by default, keep it in null 
        billingUntil = null;
        try {
            PreferenceBL preference = new PreferenceBL();
            try {
                preference.set(process.getEntity().getId(), 
                        Constants.PREFERENCE_USE_ORDER_ANTICIPATION);
            } catch (EmptyResultDataAccessException e) {
                // I like the default
            }
            if (preference.getInt() == 0 ) {
                LOG.warn("OrderAnticipated task is called, but this " +
                        "entity has the preference off");
            } else if (order.getAnticipatePeriods() != null && 
                    order.getAnticipatePeriods().intValue() > 0) {
                LOG.debug("Using anticipated order. Org billingUntil = " +
                        billingUntil + " ant periods " + 
                        order.getAnticipatePeriods());
                // calculate an extended end of billing process
                billingUntil = BillingProcessBL.getEndOfProcessPeriod(process);
                
                // move it forward by the number of ant months
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(billingUntil);
                cal.add(GregorianCalendar.MONTH,
                        order.getAnticipatePeriods().intValue());
                billingUntil = cal.getTime();
            }
        } catch (Exception e) {
            LOG.error("Exception:", e);
            throw new TaskException(e);
        }
        
        return super.isApplicable(order, process);
    }
}
