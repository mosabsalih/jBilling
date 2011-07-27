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

import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.system.event.Event;

import java.math.BigDecimal;

/**
 * This event is triggered when an order line's quantity is updated in an order.
 * In most cases rhe event can be used as 'order line updated' event.
 * 
 * @author Lucas Pickstone
 * 
 */
public class NewQuantityEvent implements Event {

    private final Integer entityId;
    private final BigDecimal oldQuantity;
    private final BigDecimal newQuantity;
    private final Integer orderId;
    // original (old) order line
    //   - line deleted : old line
    //   - line added : new line
    //   - line changed quantity : old line
    private final OrderLineDTO orderLine;
    // the new line:
    //   - line deleted : null
    //   - line added : null
    //   - line changed quantity : new line
    private final OrderLineDTO newOrderLine;
    
    public NewQuantityEvent(Integer entityId, BigDecimal oldQuantity,
            BigDecimal newQuantity, Integer orderId, OrderLineDTO orderLine,
            OrderLineDTO newOrderLine) {
        this.entityId = entityId;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.orderId = orderId;
        // make copies, so any futher changes do not affect the event
        this.orderLine = orderLine == null ? null : new OrderLineDTO(orderLine);
        this.newOrderLine = newOrderLine == null ? null : new OrderLineDTO(newOrderLine);
    }

    public Integer getEntityId() {
        return entityId;
    }
    
    public BigDecimal getOldQuantity() {
        return oldQuantity;
    }

    public BigDecimal getNewQuantity() {
        return newQuantity;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public OrderLineDTO getOrderLine() {
        return orderLine;
    }

    public OrderLineDTO getNewOrderLine() {
        return newOrderLine;
    }

    public String getName() {
        return "New Quantity Event - entity " + entityId;
    }
    
    public String toString() {
        return getName() + " - entity " + entityId + " new order line " + newOrderLine +
                " new Quantity " + newQuantity + " old Quantity " + oldQuantity +
                " order line " + orderLine;
    }

}
