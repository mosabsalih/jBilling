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



package com.sapienter.jbilling.server.provisioning.event;

//~--- non-JDK imports --------------------------------------------------------

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 * This event occurs when an order's activeSince date becomes <= current date (or null)
 *
 * @author othman El Moulat
 *
 */
public class SubscriptionActiveEvent implements Event {
    private final Integer  entityId;
    private final OrderDTO order;

    /**
     *    @param entityId
     *    @param order
     */
    public SubscriptionActiveEvent(Integer entityId, OrderDTO order) {
        this.entityId = entityId;
        this.order    = order;
    }

    public Integer getEntityId() {
        return entityId;
    }

    /**
     *     @return the order
     */
    public OrderDTO getOrder() {
        return order;
    }

    public String getName() {
        return "Subscription Active Event - entity " + entityId;
    }

    public String toString() {
        return getName() + " - entity " + entityId;
    }
}
