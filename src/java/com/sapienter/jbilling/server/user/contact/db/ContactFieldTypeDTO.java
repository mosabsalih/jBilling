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
package com.sapienter.jbilling.server.user.contact.db;


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

import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;

@Entity
@TableGenerator(name = "contact_field_type_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "contact_field_type",
        allocationSize = 10)
@Table(name="contact_field_type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ContactFieldTypeDTO extends AbstractDescription implements java.io.Serializable {


	private int id;
	private CompanyDTO entity;
	private String promptKey;
	private String dataType;
	private Integer readOnly;
	private Set<ContactFieldDTO> contactFields = new HashSet<ContactFieldDTO>(0);
	private Integer versionNum;

    public ContactFieldTypeDTO() {
    }

    public ContactFieldTypeDTO(int id) {
        this.id = id;
    }

    
    public ContactFieldTypeDTO(int id, String promptKey, String dataType) {
        this.id = id;
        this.promptKey = promptKey;
        this.dataType = dataType;
    }
    
    public ContactFieldTypeDTO(Integer id, CompanyDTO entity, String promptKey, String dataType, Integer customerReadonly, Set<ContactFieldDTO> contactFields) {
       this.id = id;
       this.entity = entity;
       this.promptKey = promptKey;
       this.dataType = dataType;
       this.readOnly = customerReadonly;
       this.contactFields = contactFields;
    }
   
    @Id 
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "contact_field_type_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="entity_id")
    public CompanyDTO getEntity() {
        return this.entity;
    }
    
    public void setEntity(CompanyDTO entity) {
        this.entity = entity;
    }
    
    @Column(name="prompt_key", nullable=false, length=50)
    public String getPromptKey() {
        return this.promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    @Column(name="data_type", nullable=false, length=10)
    public String getDataType() {
        return this.dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @Column(name="customer_readonly")
    public Integer getReadOnly() {
        return this.readOnly;
    }
    
    public void setReadOnly(Integer customerReadonly) {
        this.readOnly = customerReadonly;
    }
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="type")
    public Set<ContactFieldDTO> getContactFields() {
        return this.contactFields;
    }
    
    public void setContactFields(Set<ContactFieldDTO> contactFields) {
        this.contactFields = contactFields;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_CONTACT_FIELD_TYPE;
    }

    @Version
    @Column(name = "OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }
    
    @Override
	public String toString() {
		return "ContactFieldTypeDTO [id=" + id + ", entity=" + entity
				+ ", promptKey=" + promptKey + ", dataType=" + dataType
				+ ", readOnly=" + readOnly + ", contactFields=" + contactFields
				+ "]";
	}

}


