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
package com.sapienter.jbilling.server.order.event;

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 * This event is triggered when an new active until is entered in an order that
 * is before (smaller) than the next invoice date. This is, the order is being
 * cancelled for a date that has been already invoiced.
 * 
 * @author emilc
 * 
 */
public class PeriodCancelledEvent implements Event {

    private final Integer entityId;
    private final Integer executorId;
    private final OrderDTO order;
    
    public PeriodCancelledEvent(OrderDTO order, Integer entityId, Integer executorId) {
        this.entityId = entityId;
        this.order = order;
        this.executorId = executorId;
    }
    public Integer getEntityId() {
        return entityId;
    }
    
    public Integer getExecutorId() {
        return executorId;
    }
    
    public OrderDTO getOrder() {
        return order;
    }

    public String getName() {
        return "Perdiod Cancelled Event - entity " + entityId;
    }
    
    public String toString() {
        return getName() + " - entity " + entityId;
    }

}
