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
package com.sapienter.jbilling.server.mediation.db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
/*
 * Only needed because Order is not JPA, but an entity. Once that table is
 * migrated to JPA, this class will not be necessary (but the table stays)
 */
@Entity
@IdClass(MapPK.class)
@Table(name = "mediation_order_map")
// no cache, this is only a temp class until the order is JPAed
public class MediationOrderMap implements Serializable {

    @Id
    private Integer mediationProcessId;
    
    @Id
    private Integer orderId;

    public Integer getMediationProcessId() {
        return mediationProcessId;
    }

    public void setMediationProcessId(Integer mediationProcessId) {
        this.mediationProcessId = mediationProcessId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

}
