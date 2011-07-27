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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.process.db.PeriodUnitDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;

@Entity
@TableGenerator(
        name = "order_period_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "order_period",
        allocationSize = 100
)
@Table(name = "order_period")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OrderPeriodDTO extends AbstractDescription implements java.io.Serializable {

    private int id;
    private CompanyDTO company;
    private PeriodUnitDTO periodUnitDTO;
    private Integer value;
    private Set<OrderDTO> orderDTOs = new HashSet<OrderDTO>(0);
    private Integer versionNum;

    public OrderPeriodDTO() {
    }

    public OrderPeriodDTO(int id) {
        this.id = id;
    }

    public OrderPeriodDTO(int id, CompanyDTO entity, PeriodUnitDTO periodUnitDTO, Integer value, Set<OrderDTO> orderDTOs) {
       this.id = id;
       this.company = entity;
       this.periodUnitDTO = periodUnitDTO;
       this.value = value;
       this.orderDTOs = orderDTOs;
    }
    
    @Transient
    protected String getTable() {
        return Constants.TABLE_ORDER_PERIOD;
    }
   
    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_period_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    public CompanyDTO getCompany() {
        return this.company;
    }

    public void setCompany(CompanyDTO entity) {
        this.company = entity;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    public PeriodUnitDTO getPeriodUnit() {
        return this.periodUnitDTO;
    }

    public void setPeriodUnit(PeriodUnitDTO periodUnitDTO) {
        this.periodUnitDTO = periodUnitDTO;
    }

    @Column(name = "value")
    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "orderPeriod")
    public Set<OrderDTO> getPurchaseOrders() {
        return this.orderDTOs;
    }

    public void setPurchaseOrders(Set<OrderDTO> orderDTOs) {
        this.orderDTOs = orderDTOs;
    }

    @Version
    @Column(name = "OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }

    protected void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        return "OrderPeriodDTO:[" +
               " id=" + id +
               " company=" + company +
               " periodUnitDTO=" + periodUnitDTO +
               " value=" + value +
               " versionNum=" + versionNum + "]";
    }

    // convenient methods for migration from entity beans
    @Transient
    public Integer getUnitId() {
        if (getPeriodUnit() != null)
            return getPeriodUnit().getId();

        return null;
    }

    public void setUnitId(int id) {
        PeriodUnitDTO period = new PeriodUnitDTO(id);
        setPeriodUnit(period);
    }

    public void touch() {
        getUnitId();
    }
}


