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

import java.util.Date;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.pluggableTask.BasicOrderPeriodTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.PreferenceBL;
import org.springframework.dao.EmptyResultDataAccessException;

public class ProRateOrderPeriodTask extends BasicOrderPeriodTask {

    protected Date viewLimit = null;

    //private static final Logger LOG = Logger
        //  .getLogger(ProRateOrderPeriodTask.class);

    public ProRateOrderPeriodTask() {
        viewLimit = null;
    }

    /**
     * This methods takes and order and calculates the end date that is going to
     * be covered cosidering the starting date and the dates of this process.
     * 
     * @param order
     * @param process
     * @param startOfBillingPeriod
     * @return
     * @throws SessionInternalError
     */
    public Date calculateEnd(OrderDTO order, Date processDate, int maxPeriods,
            Date periodStart) throws TaskException {

        // verify that the pro-rating preference is present
        PreferenceBL pref = new PreferenceBL();
        try {
            pref.set(order.getUser().getEntity().getId(),
                    Constants.PREFERENCE_USE_PRO_RATING);
        } catch (EmptyResultDataAccessException e1) {
            // the defaults are fine
        }
        if (pref.getInt() == 0) {
            throw new TaskException(
                    "This plug-in is only for companies with pro-rating enabled.");
        }

        return super.calculateEnd(order, processDate, maxPeriods,
                calculateCycleStarts(order, periodStart));

    }

    private Date calculateCycleStarts(OrderDTO order, Date periodStart) {
        Date retValue = null;
        if (order.getNextBillableDay() != null) {
            retValue = order.getNextBillableDay();
        } else if (order.getCycleStarts() != null) {
            retValue = order.getCycleStarts();
        } else {
            retValue = periodStart;
        }

        return Util.truncateDate(retValue);
    }

}
