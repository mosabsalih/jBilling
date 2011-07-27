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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;


@Entity
@Table(name="order_billing_type")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class OrderBillingTypeDTO extends AbstractDescription implements java.io.Serializable {

    private int id;
    private Set<OrderDTO> orderDTOs = new HashSet<OrderDTO>(0);

    public OrderBillingTypeDTO() {
    }
    
    public OrderBillingTypeDTO(int id) {
        this.id = id;
    }

    public OrderBillingTypeDTO(int id, Set<OrderDTO> orderDTOs) {
       this.id = id;
       this.orderDTOs = orderDTOs;
    }

    @Transient
    public String getTable() {
        return Constants.TABLE_ORDER_BILLING_TYPE;
    }
    
    @Id 
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="orderBillingType")
    public Set<OrderDTO> getPurchaseOrders() {
        return this.orderDTOs;
    }
    
    public void setPurchaseOrders(Set<OrderDTO> orderDTOs) {
        this.orderDTOs = orderDTOs;
    }
    
    public String toString() {
        return " OrderBillingType=" + id;
    }
}


