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

package com.sapienter.jbilling.server.pluggableTask;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderStatusDAS;
import com.sapienter.jbilling.server.process.ConfigurationBL;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import java.util.ArrayList;

public class BasicOrderPeriodTask
    extends PluggableTask
    implements OrderPeriodTask {
    
    protected Date viewLimit = null;
    private static final Logger LOG = Logger.getLogger(BasicOrderPeriodTask.class);
    private List<PeriodOfTime> periods = new ArrayList<PeriodOfTime>();

    
    public BasicOrderPeriodTask() {
        viewLimit = null;
    }

    /**
     * Calculates the date that the invoice about to be generated is
     * going to start cover from this order. This IS NOT the invoice
     * date, since an invoice is composed by (potentially) several orders and
     * other invoices
     * @param order
     * @return
     */
     public Date calculateStart(OrderDTO order) throws TaskException {
        Date retValue = null;

        if (order.getOrderPeriod().getId() == Constants.ORDER_PERIOD_ONCE) {
            // this should be irrelevant, and could be either the order date
            // or this process date ...
            return null;
        } 
        
        if (order.getNextBillableDay() == null) {
            // never been processed
            // If it is open started (with no start date), we assume that
            // it started when it was created
            retValue = order.getActiveSince() == null ? 
                            order.getCreateDate() :
                            order.getActiveSince();
            
        } else {
            // the process date means always which day has not been paid
            // for yet.
            retValue = order.getNextBillableDay();
        }
        
        if (retValue == null) {
            throw new TaskException("Missing some date fields on " +
                "order " + order.getId());
        }
        
        // it's important to truncate this date
        
        return Util.truncateDate(retValue);
    }
 
    /**
     * This methods takes and order and calculates the end date that is 
     * going to be covered cosidering the starting date and the dates
     * of this process. 
     * @param order
     * @param process
     * @param startOfBillingPeriod
     * @return
     * @throws SessionInternalError
     */
    public Date calculateEnd(OrderDTO order, Date processDate, int maxPeriods, Date startOfBillingPeriod) throws TaskException {

        if (order.getOrderPeriod().getId() ==  Constants.ORDER_PERIOD_ONCE) {
            periods.add(new PeriodOfTime(null, null, 0, 1));
            return null;
        }                    

        Date endOfPeriod = null;
        final Date firstBillableDate = calculateStart(order);

        GregorianCalendar cal = new GregorianCalendar();
        try {
            // calculate the how far we can see in the future
            // get the period of time from the process configuration
            if (viewLimit == null) {
                viewLimit = getViewLimit(order.getOrderPeriod(), processDate);
            }
            
            cal.setTime(startOfBillingPeriod);
        
            LOG.debug("Calculating ebp for order " + order.getId() + " sbp:" +
                    startOfBillingPeriod + " process date: " + processDate +
                    " viewLimit:" + viewLimit);
            
            if (!order.getStatusId().equals(Constants.ORDER_STATUS_ACTIVE)) {
                throw new TaskException("Only active orders should be " +
                        "generating invoice. This " + order.getStatusId());
            }

            if (order.getBillingTypeId().compareTo(
                    Constants.ORDER_BILLING_POST_PAID) == 0 ) {
                // this will move on time from the start of the billing period
                // to the closest multiple period that doesn't go beyond the 
                // visibility date

                while (cal.getTime().compareTo(viewLimit) < 0
                        && (order.getActiveUntil() == null || cal.getTime().compareTo(order.getActiveUntil()) < 0)
                        && periods.size() < maxPeriods) {

                    Date cycleStarts = cal.getTime();
                    cal.add(MapPeriodToCalendar.map(order.getOrderPeriod().getUnitId()), order.getOrderPeriod().getValue());
                    Date cycleEnds = cal.getTime();

                    if (cycleEnds.after(firstBillableDate)
                            && (cycleEnds.before(viewLimit) || (order.getActiveUntil() != null && order.getActiveUntil().before(viewLimit)))) {
                        // calculate the days for this cycle
                        PeriodOfTime cycle = new PeriodOfTime(cycleStarts, cycleEnds, 0, 0);

                        // not crete this period
                        PeriodOfTime pt = new PeriodOfTime((periods.size() == 0) ? firstBillableDate : endOfPeriod, cal.getTime(), cycle.getDaysInPeriod(), periods.size() + 1);
                        periods.add(pt);

                        endOfPeriod = cal.getTime();
                        LOG.debug("added period " + pt);
                    }

                    LOG.debug("post paid, now testing:" + cal.getTime() + "(eop) = " + endOfPeriod + " compare " + cal.getTime().compareTo(viewLimit));
                }
            } else if (order.getBillingTypeId().compareTo(
                    Constants.ORDER_BILLING_PRE_PAID) == 0) {
                /* here the end of the period will be after the start of the billing
                 * process. This means that is NOT taking ALL the periods that are
                 * visible to this process, just the first one after the start of the
                 * process
                 */
                 
                // bring the date until it goes over the view limit
                // (or it reaches the expiration).
                // This then takes all previous periods that should've been billed
                // by previous processes
                Date myStart = firstBillableDate;
                while (cal.getTime().compareTo(viewLimit) < 0 &&
                        (order.getActiveUntil() == null ||
                         cal.getTime().compareTo(order.getActiveUntil()) < 0) &&
                         periods.size() < maxPeriods) {
                    Date cycleStarts = cal.getTime();
                    cal.add(MapPeriodToCalendar.map(order.getOrderPeriod().getUnitId()),
                            order.getOrderPeriod().getValue().intValue());
                    Date cycleEnds = cal.getTime();
                    if (cal.getTime().after(firstBillableDate)) {
                        // calculate the days for this cycle
                        PeriodOfTime cycle = new PeriodOfTime(cycleStarts, cycleEnds, 0, 0);
                        periods.add(new PeriodOfTime(myStart, cal.getTime(),
                                cycle.getDaysInPeriod(), periods.size() + 1));
                        myStart = cal.getTime();
                    }
                    LOG.debug("pre paid, now testing:" + cal.getTime() +
                            "(eop) = " + endOfPeriod + " compare " + 
                            cal.getTime().compareTo(viewLimit));          
                }
                
                endOfPeriod = cal.getTime();
                        
            } else {
                throw new TaskException("Order billing type "
                        + order.getBillingTypeId() + " is not supported");
            }
        } catch (Exception e) {
            throw new TaskException(e);
        }

        LOG.debug("Calculated end of period as: " + endOfPeriod);
        endOfPeriod = verifyEndOfMonthDay(order, endOfPeriod);
        
        if (endOfPeriod == null) {
            throw new TaskException("Error calculating for order " + order.getId());

        } else if (order.getActiveUntil() != null && endOfPeriod.after(order.getActiveUntil())) {
            // make sure this date is not beyond the expiration date
            endOfPeriod = order.getActiveUntil();
        } 
        
        if (startOfBillingPeriod.compareTo(endOfPeriod) == 0) {
            // this order should not be in active status
            periods.clear();
            order.setOrderStatus(new OrderStatusDAS().find(Constants.ORDER_STATUS_FINISHED));
            new EventLogger().error(order.getBaseUserByUserId().getCompany().getId(), 
                    order.getBaseUserByUserId().getId(), order.getId(), 
                    EventLogger.MODULE_BILLING_PROCESS,
                    EventLogger.BILLING_PROCESS_WRONG_FLAG_ON,
                    Constants.TABLE_PUCHASE_ORDER);
            LOG.warn("Calculating the end period for" +
                " order " + order.getId() + " ends up being the same as the" +
                " start period. Shouldn't this order be excluded?");
        } 
        
        // make sure the last period actually reflects the last adjustments
        if (periods.size() > 0) {
            PeriodOfTime lastOne = periods.get(periods.size() - 1);
            periods.remove(lastOne);
            periods.add(new PeriodOfTime(lastOne.getStart(), endOfPeriod, lastOne.getDaysInCycle(),
                    periods.size() + 1));
        }
        LOG.debug("ebp:" + endOfPeriod);
        
        return endOfPeriod;
    }

    protected Date getViewLimit(OrderPeriodDTO orderPeriod, Date processDate) {
        Integer periodUnitId = orderPeriod.getPeriodUnit().getId();
        Integer periodValue = orderPeriod.getValue();

        LOG.debug("Calculating view limit, " + periodValue + " " + orderPeriod.getPeriodUnit().getDescription() + "(s) from " + processDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(processDate);
        cal.add(MapPeriodToCalendar.map(periodUnitId), periodValue);

        return cal.getTime();
    }

    /*
     * 
    // Last day of the month validation
    // If the current date is the last day of a month, the next date
    // might have to as well.
    */
    protected Date verifyEndOfMonthDay(OrderDTO order, Date date) throws TaskException {
        if (date == null || order == null) return null;
        
        GregorianCalendar current = new GregorianCalendar();
        // this makes only sense when the order is on monthly periods
        if (order.getOrderPeriod().getUnitId().equals(Constants.PERIOD_UNIT_MONTH)) {
            // the current next invoice date has to be the last day of that month, and not a 31
            current.setTime(calculateStart(order));
            // if the order has a cycle start, then take the day from it
            // this makes sense for the first invoice. For the rest it should already by it
            if (order.getCycleStarts() != null) {
                GregorianCalendar cycleStarts = new GregorianCalendar();
                cycleStarts.setTime(order.getCycleStarts());
                current.set(Calendar.DAY_OF_MONTH, cycleStarts.get(Calendar.DAY_OF_MONTH));
            }
            if (current.get(Calendar.DAY_OF_MONTH) == current.getActualMaximum(Calendar.DAY_OF_MONTH) &&
                    current.get(Calendar.DAY_OF_MONTH) < 31) {
                // set the end date propsed
                GregorianCalendar edp = new GregorianCalendar();
                edp.setTime(date);
                // the proposed end date should not be the end of the month
                if (edp.get(Calendar.DAY_OF_MONTH) != edp.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    // set the first invoicabe day
                    GregorianCalendar firstDate = new GregorianCalendar();
                    firstDate.setTime(order.getActiveSince() == null ? order.getCreateDate() : order.getActiveSince());
                    if (firstDate.get(Calendar.DAY_OF_MONTH) > edp.get(Calendar.DAY_OF_MONTH)) {
                        LOG.debug("Order " + order.getId() + ".Adjusting next invoice date " +
                                "because end of the month from " + 
                                edp.get(Calendar.DAY_OF_MONTH) + " to " + firstDate.get(Calendar.DAY_OF_MONTH));
                        edp.set(Calendar.DAY_OF_MONTH, firstDate.get(Calendar.DAY_OF_MONTH));
                        return edp.getTime();   
                    } else {
                        // the first date of invoice has to be grater than the day being proposed, otherwise
                        // there isn't anything to fix (the fix is to increas the edp by a few days)
                        return date;
                    }
                } else {
                    // if the proposed end date is the end of the month, it can't be corrected, since
                    // the correction means adding days.
                    return date;
                }
            } else { 
                // if the last next billing date is the 31, adding a month can't be problematic
                // if the last next billing date is not the last day of the month, it can't come from
                // a higher end date
                return date;
            }
        } else {
            return date;
        }
        
    }

    public List<PeriodOfTime> getPeriods() {
        return periods;
    }
    
    
}
