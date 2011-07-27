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
package com.sapienter.jbilling.server.user.event;

import com.sapienter.jbilling.server.system.event.Event;

import java.math.BigDecimal;

/**
 * Event representing dynamic balance changes of a customer.
 *
 * @author Brian Cowdery
 * @since  10-14-2009
 */
public class DynamicBalanceChangeEvent implements Event {
        
    private Integer entityId;
    private Integer userId;
    private BigDecimal newBalance;
    private BigDecimal oldBalance;

    public DynamicBalanceChangeEvent(Integer entityId, Integer userId, BigDecimal newBalance, BigDecimal oldBalance) {
        this.entityId = entityId;
        this.userId = userId;
        this.newBalance = newBalance;
        this.oldBalance = oldBalance;
    }

    public String getName() {
        return "Dynamic Balance Change Event";
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public BigDecimal getOldBalance() {
        return oldBalance;
    }

    @Override
    public String toString() {
        return "DynamicBalanceChangeEvent{" +
                "entityId=" + entityId +
                ", userId=" + userId +
                ", newBalance=" + newBalance +
                ", oldBalance=" + oldBalance +
                '}';
    }
}
