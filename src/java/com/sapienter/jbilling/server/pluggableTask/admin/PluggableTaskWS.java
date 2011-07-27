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

package com.sapienter.jbilling.server.pluggableTask.admin;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.sapienter.jbilling.server.security.WSSecured;


public class PluggableTaskWS implements java.io.Serializable, WSSecured {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer id;
    @NotNull(message="validation.error.notnull")
    @Min(value = 1, message = "validation.error.min,1")
    private Integer processingOrder;
    private String notes;
    @NotNull(message="validation.error.notnull")
    private Integer typeId;
    private Map<String, String> parameters = new HashMap<String, String>();
    private int versionNumber;
    
    public PluggableTaskWS() {
    }
    
    public PluggableTaskWS(PluggableTaskDTO dto) {
        setNotes(dto.getNotes());
        setId(dto.getId());
        setProcessingOrder(dto.getProcessingOrder());
        setTypeId(dto.getType().getId());
        for (PluggableTaskParameterDTO param:dto.getParameters()) {
            parameters.put(param.getName(), param.getValue());
        }
        versionNumber = dto.getVersionNum();
    }
    
	public void setNotes(String notes) {
		this.notes = notes;
	}


	public String getNotes() {
		return notes;
	}


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getProcessingOrder() {
        return processingOrder;
    }


    public void setProcessingOrder(Integer processingOrder) {
        this.processingOrder = processingOrder;
    }


    public Integer getTypeId() {
        return typeId;
    }


    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }


    public Map<String, String> getParameters() {
        return parameters;
    }


    public void setParameters(Hashtable<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public int getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    public String toString() {
        return "PluggableTaskWS [id=" + id + ", notes=" + notes
                + ", parameters=" + parameters + ", processingOrder="
                + processingOrder + ", typeId=" + typeId + ", versionNumber="
                + versionNumber + "]";
    }
    
    @Override
    public Integer getOwningEntityId() {
        if (getId() == null) {
            return null;
        }
        return new PluggableTaskBL(getId()).getDTO().getEntityId();
    }
    
    @Override
    public Integer getOwningUserId() {
        return null;
    }
}
