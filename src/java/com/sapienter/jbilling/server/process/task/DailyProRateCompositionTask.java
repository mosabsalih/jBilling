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

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.pluggableTask.BasicCompositionTask;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.util.Constants;

/**
 * Calculates the pro-rated amount taking the smallest unit: days
 * @author emilc
 *
 */
public class DailyProRateCompositionTask extends BasicCompositionTask {
    private static final Logger LOG = Logger.getLogger(DailyProRateCompositionTask.class);
    
    public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
        if (period == null || fullPrice == null) {
            LOG.warn("Called with null parameters");
            return null;
        }
        
        // this is an amount from a one-time order, not a real period of time
        if (period.getDaysInCycle() == 0) {
            return fullPrice;
        }
        
        // if this is not a fraction of a period, don't bother making any calculations
        if (period.getDaysInCycle() == period.getDaysInPeriod()) {
            return fullPrice;
        }
        
        BigDecimal oneDay = fullPrice.divide(new BigDecimal(period.getDaysInCycle()), 
                Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
        BigDecimal proRatedAmount = oneDay.multiply(new BigDecimal(period.getDaysInPeriod()));
        return proRatedAmount;
    }

}
