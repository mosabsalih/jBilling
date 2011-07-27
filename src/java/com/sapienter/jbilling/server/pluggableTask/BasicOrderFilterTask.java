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
 * Created on Apr 28, 2003
 *
 */
package com.sapienter.jbilling.server.pluggableTask;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.process.BillingProcessBL;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
import com.sapienter.jbilling.server.util.audit.EventLogger;

/**
 * Verifies if the order should be included in a porcess considering its
 * active range dates. It takes the billing period type in consideration
 * as well. This probably would've been easier to do if EJB/QL had support
 * for Date types
 */
public class BasicOrderFilterTask 
        extends PluggableTask implements OrderFilterTask {

    private static final Logger LOG =  Logger.getLogger(BasicOrderFilterTask.class);
    protected Date billingUntil = null;
    
    public BasicOrderFilterTask() {
        billingUntil = null;
    }
    
    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.OrderFilterTask#isApplicable(com.sapienter.betty.interfaces.OrderEntityLocal)
     */
    public boolean isApplicable(OrderDTO order, 
            BillingProcessDTO process) throws TaskException {

        boolean retValue = true;
        
        GregorianCalendar cal = new GregorianCalendar();

        LOG.debug("running isApplicable for order " + order.getId() + 
                " billingUntil = " + billingUntil);
        // some set up to simplify the code
        Date activeUntil = null;
        if (order.getActiveUntil() != null) {
            activeUntil = Util.truncateDate(order.getActiveUntil());
        }
        Date activeSince = null;
        if (order.getActiveSince() != null) {
            activeSince = Util.truncateDate(order.getActiveSince());
        } else {
            // in fact, an open starting point doesn't make sense (an order reaching
            // inifinitly backwards). So we default to the creation date
            activeSince = Util.truncateDate(order.getCreateDate());
        }
        
        try {
            // calculate how far in time this process applies
            if (billingUntil == null) {
                // this could have been set by a class extending this one
                // If not, we use this as a default
                billingUntil = BillingProcessBL.getEndOfProcessPeriod(process);
            }

            EventLogger eLog = EventLogger.getInstance();
            
            if (order.getBillingTypeId().compareTo(
                    Constants.ORDER_BILLING_POST_PAID) == 0) {
                
                // check if it is too early        
                if(activeSince.
                        after(billingUntil)) {
                    // didn't start yet
                    eLog.info(process.getEntity().getId(), 
                            order.getBaseUserByUserId().getId(), order.getId(), 
                            EventLogger.MODULE_BILLING_PROCESS,
                            EventLogger.BILLING_PROCESS_NOT_ACTIVE_YET,
                            Constants.TABLE_PUCHASE_ORDER);
                    retValue = false;
                // One time only orders don't need to check for periods                     
                } else if (!order.getPeriodId().equals(
                        Constants.ORDER_PERIOD_ONCE)) {
                    // check that there's at least one period since this order
                    // started, otherwise it's too early to bill
                    cal.setTime(activeSince);
                    cal.add(MapPeriodToCalendar.map(order.getOrderPeriod().getUnitId()),
                             order.getOrderPeriod().getValue().intValue());
                    Date firstBillingDate = thisOrActiveUntil(cal.getTime(), activeUntil);
                    if (!firstBillingDate.before(billingUntil)) {
                        eLog.info(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_ONE_PERIOD_NEEDED,
                                Constants.TABLE_PUCHASE_ORDER);
                        
                        retValue = false; // gotta wait for the first bill
                    }
                }
                
                // there must be at least one period after the last paid day
                if (retValue && order.getNextBillableDay() != null) {
                    cal.setTime(order.getNextBillableDay());
                    cal.add(MapPeriodToCalendar.map(order.getOrderPeriod().getUnitId()),
                             order.getOrderPeriod().getValue().intValue());
                    Date endOfNextPeriod = thisOrActiveUntil(cal.getTime(), activeUntil);                
                    if (endOfNextPeriod.after(billingUntil)) {
                        eLog.info(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_RECENTLY_BILLED,
                                Constants.TABLE_PUCHASE_ORDER);
                        
                        retValue = false;
                        
                        // may be it's actually billed to the end of its life span
                        if (activeUntil != null && //may be it's immortal ;)
                                order.getNextBillableDay().compareTo(activeUntil) >= 0) {
                            // this situation shouldn't have happened
                            LOG.warn("Order " + order.getId() + " should've been" +
                                " flagged out in the previous process");
                            eLog.warning(process.getEntity().getId(), 
                                    order.getBaseUserByUserId().getId(), 
                                    order.getId(), 
                                    EventLogger.MODULE_BILLING_PROCESS,
                                    EventLogger.BILLING_PROCESS_WRONG_FLAG_ON,
                                    Constants.TABLE_PUCHASE_ORDER);
                            OrderBL orderBL = new OrderBL(order);
                            orderBL.setStatus(null, Constants.ORDER_STATUS_FINISHED);    
                            order.setNextBillableDay(null);         
                        }
                    }
                }
                
                // post paid orders can't be too late to process 
                      
            } else if (order.getBillingTypeId().compareTo(
                    Constants.ORDER_BILLING_PRE_PAID) == 0) {
            
                //  if it has a billable day
                if (order.getNextBillableDay() != null) {
                    // now check if there's any more time to bill as far as this
                    // process goes
                    LOG.debug("order " + order.getId() + " nbd = " + 
                            order.getNextBillableDay() + " bu = " + billingUntil);
                    if (order.getNextBillableDay().compareTo(billingUntil) >= 0) {
                        retValue = false;
                        eLog.info(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_RECENTLY_BILLED,
                                Constants.TABLE_PUCHASE_ORDER);
                        
                    }
                    
                    // check if it is all billed already
                    if (activeUntil != null && order.getNextBillableDay().
                                compareTo(activeUntil) >= 0) { 
                        retValue = false;
                        LOG.warn("Order " + order.getId() + " was set to be" +
                                " processed but the next billable date is " +
                                "after the active until");
                        eLog.warning(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_EXPIRED,
                                Constants.TABLE_PUCHASE_ORDER);
                        OrderBL orderBL = new OrderBL(order);
                        orderBL.setStatus(null, Constants.ORDER_STATUS_FINISHED);
                        order.setNextBillableDay(null);                                   
                    }
                } else if (activeUntil != null && process.getBillingDate().
                        after(activeUntil)) {
                    retValue = false;
                    OrderBL orderBL = new OrderBL(order);
                    orderBL.setStatus(null, Constants.ORDER_STATUS_FINISHED);
                    eLog.warning(process.getEntity().getId(), 
                            order.getBaseUserByUserId().getId(), order.getId(), 
                            EventLogger.MODULE_BILLING_PROCESS,
                            EventLogger.BILLING_PROCESS_WRONG_FLAG_ON,
                            Constants.TABLE_PUCHASE_ORDER);
                    LOG.warn("Found expired order " + order.getId() + 
                            " without nbp but with to_process=1");
                }
            
                
                // see if it is too early
                if (retValue && activeSince != null) {
                    
                    if (!activeSince.before(billingUntil)) {
                        // This process is not including the time this order
                        // starts
                        retValue = false;
                        eLog.info(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_NOT_ACTIVE_YET,
                                Constants.TABLE_PUCHASE_ORDER);
                        
                    }
                }
                
                // finally if it is too late (would mean a warning)
                if (retValue && activeUntil != null) {
                    if (process.getBillingDate().after(activeUntil)) {
                        // how come this order has some period yet to be billed, but
                        // the active is already history ? It should've been billed
                        // in a previous process
                        eLog.warning(process.getEntity().getId(), 
                                order.getBaseUserByUserId().getId(), 
                                order.getId(), 
                                EventLogger.MODULE_BILLING_PROCESS,
                                EventLogger.BILLING_PROCESS_EXPIRED,
                                Constants.TABLE_PUCHASE_ORDER);
            
                        LOG.warn("Order with time yet to be billed not included!");
                    }
                }
                        
            } else {
                throw new TaskException("Billing type of this order " +
                    "is not supported:" + order.getBillingTypeId());
            }
        } catch (NumberFormatException e) {
            LOG.fatal("Exception converting types", e);
            throw new TaskException("Exception with type conversions: " +
                    e.getMessage());
        } catch (SessionInternalError e) {
            LOG.fatal("Internal exception ", e);
            throw new TaskException(e);
        } 
        
        LOG.debug("Order " + order.getId() + " filter:" + retValue); 
        
        return retValue;

    }

    private Date thisOrActiveUntil(Date thisDate, Date activeUntil) {
        if (activeUntil == null) return thisDate;
        return activeUntil.before(thisDate) ? activeUntil : thisDate;
    }
}
