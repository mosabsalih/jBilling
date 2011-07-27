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

import com.sapienter.jbilling.server.order.db.OrderLineDTO;


@Deprecated /*  Replaced by SubscriptionResult to be used with version 2 of the rules tasks. */
public class Subscription {

    private final Integer itemId;
    private final Integer periodId;
    private final Date activeSince;
    private final Date activeUntil;
    private final BigDecimal quantity;

    public Subscription(OrderLineDTO line) {
        periodId = line.getPurchaseOrder().getOrderPeriod().getId();
        activeSince = line.getPurchaseOrder().getActiveSince();
        activeUntil = line.getPurchaseOrder().getActiveUntil();
        itemId = line.getItemId();
        quantity = line.getQuantity();
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
}
