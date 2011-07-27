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

import java.math.BigDecimal;
import java.util.Date;

import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 * This event is triggered AFTER an order is added to an invoice.
 *
 */
public class OrderAddedOnInvoiceEvent implements Event {

    private Integer entityId;
    private Integer userId;
    private Integer orderId;
    private OrderDTO order;
    private Date start;
    private Date end;
    private BigDecimal totalInvoiced;

    public OrderAddedOnInvoiceEvent(Integer entityId, Integer userId, 
            OrderDTO order, BigDecimal totalInvoiced) {
        this.entityId = entityId;
        this.userId = userId;
        this.orderId = order.getId();
        this.order = order;
        this.totalInvoiced = totalInvoiced;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public String getName() {
        return "Order to Invoice for Entity " + entityId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    /**
     * Warning, the order returned is in the hibernate session.
     * Any changes will be reflected in the database.
     */
    public OrderDTO getOrder() {
        return order;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public String toString() {
        return getName();
    }
}
