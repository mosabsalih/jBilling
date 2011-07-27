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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@TableGenerator(
        name="pluggable_task_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="pluggable_task",
        allocationSize = 10
        )
@Table(name = "pluggable_task")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PluggableTaskDTO implements java.io.Serializable {
    private static final Logger LOG = Logger.getLogger(PluggableTaskDTO.class);
    //  this is in synch with the DB (pluggable task type)
    public static final Integer TYPE_EMAIL = new Integer(9);
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="pluggable_task_GEN")
    private Integer id;
   
    @Column(name = "entity_id")
    private Integer entityId;
    
    @Column(name = "processing_order")
    private Integer processingOrder;
    
    @Column(name = "notes")
    private String notes;
    
    @ManyToOne
    @JoinColumn(name="type_id")
    private PluggableTaskTypeDTO type;
    
    @OneToMany(mappedBy="task", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    @Fetch( FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Collection<PluggableTaskParameterDTO> parameters;
    
    @Version
    @Column(name="OPTLOCK")
    private Integer versionNum;
    
    public PluggableTaskDTO() {
        type = new PluggableTaskTypeDTO();
    }

    public PluggableTaskDTO(Integer entityId, PluggableTaskWS ws) {
        setEntityId(entityId);
        setId(ws.getId());
        setProcessingOrder(ws.getProcessingOrder());
        setNotes(ws.getNotes());
        setType(new PluggableTaskTypeDAS().find(ws.getTypeId()));
        versionNum = ws.getVersionNumber();
        parameters = new ArrayList<PluggableTaskParameterDTO>();
        // if this is an existing plug-in..
        Collection<PluggableTaskParameterDTO> params = null;
        if (getId() != null && getId() > 0) {
            params = new PluggableTaskBL(getId()).getDTO().getParameters();
        }
        for (String key: ws.getParameters().keySet()) {
            PluggableTaskParameterDTO parameter = new PluggableTaskParameterDTO();
            parameter.setName(key);
            parameter.setStrValue(ws.getParameters().get(key));
            parameter.setTask(this);
            parameters.add(parameter);
            if (params != null) {
                for (PluggableTaskParameterDTO dbParam: params) {
                    if (dbParam.getName().equals(parameter.getName())) {
                        parameter.setId(dbParam.getId());
                        parameter.setVersionNum(dbParam.getVersionNum());
                    }
                }
            }
        }
    }
 
    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
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

   public Collection<PluggableTaskParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<PluggableTaskParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public PluggableTaskTypeDTO getType() {
        return type;
    }

    public void setType(PluggableTaskTypeDTO type) {
        this.type = type;
    }

    protected int getVersionNum() { return versionNum; }

    public void populateParamValues() {
        if (parameters != null) {
            for (PluggableTaskParameterDTO parameter : parameters) {
                parameter.populateValue();
            }
        }
    }


	public void setNotes(String notes) {
		this.notes = notes;
	}


	public String getNotes() {
		return notes;
	}

    @Override
    public String toString() {
        return "PluggableTaskDTO [entityId=" + entityId + ", id=" + id
                + ", notes=" + notes + ", parameters=" + parameters
                + ", processingOrder=" + processingOrder + ", type=" + type
                + ", versionNum=" + versionNum + "]";
    }
}
