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

package com.sapienter.jbilling.server.item.tasks;

import java.math.BigDecimal;
import java.util.Date;

import com.sapienter.jbilling.server.mediation.task.MediationResult;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.rule.Result;

/**
 * SubscriptionResult object used to check if a user holds a subscription to a specific item, or
 * the reverse - that the user does NOT hold a subscription.
 *
 * This class performs a lookup to check the status of a subscription when a new instance is
 * created. If the user does not currently hold an active subscription to  the item, the
 * {@link #isSubscribed()} flag will be set to false.
 * 
 * @author Brian Cowdery
 * @since 2009-12-15
 */
public class SubscriptionResult extends Result {

    private Integer userId;
    private Integer itemId;

    private Integer periodId;
    private Date activeSince;
    private Date activeUntil;
    private BigDecimal quantity;

    private boolean subscribed = false; // default to not subscribed

    public SubscriptionResult(OrderDTO order, Integer itemId) {
        this(order.getUserId(), itemId);
    }

    public SubscriptionResult(MediationResult result, Integer itemId) {
        this(result.getUserId(), itemId);
    }

    public SubscriptionResult(PricingResult result, Integer itemId) {
        this(result.getUserId(), itemId);
    }

    public SubscriptionResult(Integer userId, Integer itemId) {
        this.userId = userId;
        this.itemId = itemId;

        populateSubscription(this.userId, this.itemId);
    }

    /**
     * Populates the recurring order line details if an active subscription exists
     * for the given userId and itemId. This method will set {@link #isSubscribed()} to
     * true if a subscription exists.
     *
     * @param userId user id
     * @param itemId item id
     */
    private void populateSubscription(Integer userId, Integer itemId) {
        OrderLineDTO line = new OrderBL().getRecurringOrderLine(userId, itemId);

        if (line != null) {
            periodId = line.getPurchaseOrder().getOrderPeriod().getId();
            activeSince = line.getPurchaseOrder().getActiveSince();
            activeUntil = line.getPurchaseOrder().getActiveUntil();
            quantity = line.getQuantity();

            subscribed = true;
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public Date getActiveSince() {
        return activeSince;
    }

    public Date getActiveUntil() {
        return activeUntil;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getPeriodId() {
        return periodId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns true if the subscription is active for the set user and item,
     * false if no subscription exists or is not active (user is not subscribed). 
     *
     * @return true if user is subscribed to item.
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    public String toString() {
        return "SubscriptionResult: userId= " + userId + " itemId= " + itemId +
                " isSubscribed= "+ isSubscribed();
    }
}
