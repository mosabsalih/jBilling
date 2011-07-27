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

import java.util.Date;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.system.event.Event;

public class NewActiveUntilEvent implements Event {
    private static final Logger LOG = Logger.getLogger(NewActiveUntilEvent.class); 
    private Integer entityId;
    private Integer userId;
    private Integer orderId;
    private Date newActiveUntil; 
    private Date oldActiveUntil;
    private Integer orderType;
    private Integer statusId;
    
    public NewActiveUntilEvent(Integer orderId, 
            Date newActiveUntil, Date oldActiveUntil) {
        try {
            OrderBL order = new OrderBL(orderId);
            
            this.entityId = order.getEntity().getUser().getEntity().getId();
            this.userId = order.getEntity().getUser().getUserId();
            this.orderType = order.getEntity().getOrderPeriod().getId();
            this.statusId = order.getEntity().getStatusId();
        } catch (Exception e) {
            LOG.error("Handling order in event", e);
        } 
        this.orderId = orderId;
        this.newActiveUntil = newActiveUntil;
        this.oldActiveUntil = oldActiveUntil;
    }
    
    public Integer getEntityId() {
        return entityId;
    }

    public String getName() {
        return "New active until";
    }

    public String toString() {
        return getName();
    }
    public Date getNewActiveUntil() {
        return newActiveUntil;
    }
    public Date getOldActiveUntil() {
        return oldActiveUntil;
    }
    public Integer getOrderId() {
        return orderId;
    }
    public Integer getUserId() {
        return userId;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public Integer getStatusId() {
        return statusId;
    }
}
