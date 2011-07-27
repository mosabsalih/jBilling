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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
import javax.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@TableGenerator(
        name = "mediation_process_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "mediation_process",
        allocationSize = 10
)
@Table(name = "mediation_process")
// no cache. This table is not read repeatedly
public class MediationProcess implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "mediation_process_GEN")
    private Integer id;

    @Column(name = "start_datetime")
    private Date startDatetime;

    @Column(name = "end_datetime")
    private Date endDatetime;

    @Column(name = "orders_affected")
    private Integer ordersAffected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private MediationConfiguration configuration;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediation_process_id")
    public Collection<MediationOrderMap> orderMap = new ArrayList<MediationOrderMap>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "process")
    public Collection<MediationRecordDTO> records = new ArrayList<MediationRecordDTO>(0);

    @Version
    @Column(name = "OPTLOCK")
    private Integer versionNum;

    public Date getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Date endDatetime) {
        this.endDatetime = endDatetime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdersAffected() {
        return ordersAffected;
    }

    public void setOrdersAffected(Integer ordersAffected) {
        this.ordersAffected = ordersAffected;
    }

    public Date getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Date startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public Collection<MediationOrderMap> getOrderMap() {
        return orderMap;
    }

    public void setOrderMap(Collection<MediationOrderMap> orderMap) {
        this.orderMap = orderMap;
    }

    public MediationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MediationConfiguration configuration) {
        this.configuration = configuration;
    }

    public Collection<MediationRecordDTO> getRecords() {
        return records;
    }

    public void setRecords(Collection<MediationRecordDTO> records) {
        this.records = records;
    }

    public String toString() {
        return "MediationProcess= " +
               " orders affected = " + getOrdersAffected();
    }
}
