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
package com.sapienter.jbilling.server.order.db;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractGenericStatus;


@Entity
@DiscriminatorValue("order_status")
public class OrderStatusDTO  extends AbstractGenericStatus implements java.io.Serializable {


     private Set<OrderDTO> orderDTOs = new HashSet<OrderDTO>(0);

    public OrderStatusDTO() {
    }

    
    public OrderStatusDTO(int statusValue) {
        this.statusValue = statusValue;
    }
    public OrderStatusDTO(int statusValue, Set<OrderDTO> orderDTOs) {
       this.statusValue = statusValue;
       this.orderDTOs = orderDTOs;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_ORDER_STATUS;
    }

@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="orderStatus")
    public Set<OrderDTO> getPurchaseOrders() {
        return this.orderDTOs;
    }
    
    public void setPurchaseOrders(Set<OrderDTO> orderDTOs) {
        this.orderDTOs = orderDTOs;
    }
}


