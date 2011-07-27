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
package com.sapienter.jbilling.server.util;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Min;

import com.sapienter.jbilling.server.util.api.validation.UpdateValidationGroup;
import com.sapienter.jbilling.server.util.db.PreferenceDTO;
import com.sapienter.jbilling.server.util.db.PreferenceTypeDTO;

public class PreferenceWS implements Serializable {

    private Integer id;
    private PreferenceTypeWS preferenceType;
    private Integer tableId;
    private Integer foreignId;
    private String value;

    public PreferenceWS() {
    }

    public PreferenceWS(PreferenceTypeDTO preferenceType) {
        this.preferenceType = new PreferenceTypeWS(preferenceType);
    }

    public PreferenceWS(PreferenceDTO dto) {
        this.id = dto.getId();
        this.preferenceType = dto.getPreferenceType() != null ? new PreferenceTypeWS(dto.getPreferenceType()) : null;
        this.tableId = dto.getJbillingTable() != null ? dto.getJbillingTable().getId() : null;
        this.foreignId = dto.getForeignId();
        this.value = dto.getValue();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PreferenceTypeWS getPreferenceType() {
        return this.preferenceType;
    }

    public void setPreferenceType(PreferenceTypeWS preferenceType) {
        this.preferenceType = preferenceType;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }
    
    public Integer getForeignId() {
        return this.foreignId;
    }

    public void setForeignId(Integer foreignId) {
        this.foreignId = foreignId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PreferenceWS{"
               + "id=" + id
               + ", preferenceType=" + preferenceType
               + ", tableId=" + tableId
               + ", foreignId=" + foreignId
               + ", value='" + value + '\''
               + '}';
    }
}
