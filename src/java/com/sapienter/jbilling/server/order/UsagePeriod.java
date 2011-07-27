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

package com.sapienter.jbilling.server.order;

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.process.PeriodOfTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * UsagePeriod represents a period of time for a customer's usage calculations. This
 * class holds the customer's main subscription order, the cycle start and end dates,
 * and a list of periods for the {@link UsageBL} usage period.
 *
 * @author Brian Cowdery
 * @since 03-09-2010
 */
public class UsagePeriod implements Serializable {

    // extracted from UsageBL fields to provide an object that could be cached

    private OrderDTO mainOrder;
    private Date cycleStartDate;
    private Date cycleEndDate;
    private List<PeriodOfTime> billingPeriods;

    public UsagePeriod() { }

    public OrderDTO getMainOrder() {
        return mainOrder;
    }

    public void setMainOrder(OrderDTO mainOrder) {
        this.mainOrder = mainOrder;
    }

    public Date getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(Date cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public Date getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(Date cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    public List<PeriodOfTime> getBillingPeriods() {
        return billingPeriods;
    }

    public void setBillingPeriods(List<PeriodOfTime> billingPeriods) {
        this.billingPeriods = billingPeriods;
    }

    @Override public String toString() {
        return "UsagePeriod{"
               + "mainOrder=" + (mainOrder != null ? mainOrder.getId() : null)
               + ", cycleStartDate=" + cycleStartDate
               + ", cycleEndDate=" + cycleEndDate
               + ", billingPeriods=" + (billingPeriods != null ? billingPeriods.size() : null)
               + '}';
    }
}
