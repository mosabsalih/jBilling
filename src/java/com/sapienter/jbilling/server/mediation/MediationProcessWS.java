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

package com.sapienter.jbilling.server.mediation;

import com.sapienter.jbilling.server.mediation.db.MediationOrderMap;
import com.sapienter.jbilling.server.mediation.db.MediationProcess;
import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MediationProcessWS
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class MediationProcessWS implements Serializable {

    private Integer id;
    private Integer configurationId;
    private Date startDatetime;
    private Date endDatetime;
    private Integer ordersAffected;   
    private List<Integer> orderIds;
    private List<Integer> recordIds;

    public MediationProcessWS() {
    }

    public MediationProcessWS(MediationProcess dto) {
        this.id = dto.getId();
        this.configurationId = dto.getConfiguration() != null ? dto.getConfiguration().getId() : null;
        this.startDatetime = dto.getStartDatetime();
        this.endDatetime = dto.getEndDatetime();
        this.ordersAffected = dto.getOrdersAffected();

        // order ID's
        this.orderIds = new ArrayList<Integer>(dto.getOrderMap().size());
        for (MediationOrderMap map : dto.getOrderMap())
            this.orderIds.add(map.getOrderId());

        // mediation record ID's
        this.recordIds = new ArrayList<Integer>(dto.getRecords().size());
        for (MediationRecordDTO record : dto.getRecords())
            this.recordIds.add(record.getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public Date getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Date startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Date getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Date endDatetime) {
        this.endDatetime = endDatetime;
    }

    public Integer getOrdersAffected() {
        return ordersAffected;
    }

    public void setOrdersAffected(Integer ordersAffected) {
        this.ordersAffected = ordersAffected;
    }

    public List<Integer> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<Integer> orderIds) {
        this.orderIds = orderIds;
    }

    public List<Integer> getRecordIds() {
        return recordIds;
    }

    public void setRecordIds(List<Integer> recordIds) {
        this.recordIds = recordIds;
    }

    @Override
    public String toString() {
        return "MediationProcessWS{"
               + "id=" + id
               + ", configurationId=" + configurationId
               + ", startDatetime=" + startDatetime
               + ", endDatetime=" + endDatetime
               + ", ordersAffected=" + ordersAffected
               + ", orderIds=" + orderIds
               + ", recordIds=" + recordIds
               + '}';
    }
}
