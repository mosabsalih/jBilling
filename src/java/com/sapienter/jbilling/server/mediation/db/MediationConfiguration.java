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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;

@Entity
@TableGenerator(
        name = "mediation_cfg_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "mediation_cfg",
        allocationSize = 10
)
@Table(name = "mediation_cfg")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MediationConfiguration implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "mediation_cfg_GEN")
    private Integer id;

    @Column(name = "entity_id")
    private Integer entityId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pluggable_task_id")
    private PluggableTaskDTO pluggableTask;

    @Column(name = "name")
    private String name;

    @Column(name = "order_value")
    private Integer orderValue;

    @Column(name = "create_datetime")
    private Date createDatetime;

    @Version
    @Column(name = "OPTLOCK")
    private Integer versionNum;


    public MediationConfiguration() {
    }

    public MediationConfiguration(MediationConfigurationWS ws, PluggableTaskDTO pluggableTask) {
        this.id = ws.getId();
        this.entityId = ws.getEntityId();
        this.pluggableTask = pluggableTask;
        this.name = ws.getName();
        this.orderValue = ws.getOrderValue();
        this.createDatetime = ws.getCreateDatetime();
        this.versionNum = ws.getVersionNum();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }


    public PluggableTaskDTO getPluggableTask() {
        return pluggableTask;
    }

    public void setPluggableTask(PluggableTaskDTO pluggableTask) {
        this.pluggableTask = pluggableTask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Integer orderValue) {
        this.orderValue = orderValue;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    public String toString() {
        return "ID: " + id + " name: " + name + " order value: " + orderValue +
               " task: " + pluggableTask + " date: " + createDatetime +
               " entity id: " + entityId;
    }
}
