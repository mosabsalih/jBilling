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
package com.sapienter.jbilling.server.item;

import com.sapienter.jbilling.server.item.db.ItemTypeDTO;

import javax.validation.constraints.Size;
import javax.validation.constraints.Min;

import java.io.Serializable;

/**
 * @author Brian Cowdery
 * @since 07-10-2009
 */
public class ItemTypeWS implements Serializable {

    private Integer id;
    
    @Size (min=1,max=100, message="validation.error.size,1,100")    
    private String description;
    
    @Min(value = 1, message="validation.error.min,1")
    private Integer orderLineTypeId;

    public ItemTypeWS() {
    }

    public ItemTypeWS(Integer id, String description, Integer orderLineTypeId) {
        this.id = id;
        this.description = description;
        this.orderLineTypeId = orderLineTypeId;
    }

    public ItemTypeWS(ItemTypeDTO item) {
        this.id = item.getId();
        this.description = item.getDescription();
        this.orderLineTypeId = item.getOrderLineTypeId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrderLineTypeId() {
        return orderLineTypeId;
    }

    public void setOrderLineTypeId(Integer orderLineTypeId) {
        this.orderLineTypeId = orderLineTypeId;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;
        
        ItemTypeWS that = (ItemTypeWS) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!id.equals(that.id)) return false;
        if (!orderLineTypeId.equals(that.orderLineTypeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + orderLineTypeId.hashCode();
        return result;
    }
}
