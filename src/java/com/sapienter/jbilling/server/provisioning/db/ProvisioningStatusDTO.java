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
package com.sapienter.jbilling.server.provisioning.db;

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
@DiscriminatorValue("order_line_provisioning_status")
public class ProvisioningStatusDTO extends AbstractGenericStatus 
        implements java.io.Serializable {

//    private Set<OrderLineDTO> orderLines = new HashSet<OrderLineDTO>(0);

    public ProvisioningStatusDTO() {
    }
    
    public ProvisioningStatusDTO(int statusValue) {
        this.statusValue = statusValue;
    }
/*
    public ProvisioningStatusDTO(int statusValue, Set<OrderLineDTO> orderLines) {
       this.statusValue = statusValue;
       this.orderLines = orderLines;
    }
*/
    @Transient
    protected String getTable() {
        return Constants.TABLE_ORDER_LINE_PROVISIONING_STATUS;
    }
/*
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="provisioningStatus")
    public Set<OrderLineDTO> getOrderLines() {
        return this.orderLines;
    }
    
    public void setOrderLines(Set<OrderLineDTO> orderLines) {
        this.orderLines = orderLines;
    }
*/
}
