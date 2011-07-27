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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(
        name="contact_field_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="contact_field",
        allocationSize = 100
        )
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name="contact_field")
public class ContactFieldDTO  implements java.io.Serializable {


     private int id;
     private ContactFieldTypeDTO type;
     private ContactDTO contact;
     private String content;
     private int versionNum;

    public ContactFieldDTO() {
    }
    
    public ContactFieldDTO(ContactFieldDTO other) {
        setId(other.getId());
        setType(other.getType());
        setContact(other.getContact());
        setContent(other.getContent());
    }

    
    public ContactFieldDTO(int id, String content) {
        this.id = id;
        this.content = content;
    }
    public ContactFieldDTO(int id, ContactFieldTypeDTO contactFieldType, ContactDTO contact, String content) {
       this.id = id;
       this.type = contactFieldType;
       this.contact = contact;
       this.content = content;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="contact_field_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="type_id")
    public ContactFieldTypeDTO getType() {
        return this.type;
    }
    
    public void setType(ContactFieldTypeDTO contactFieldType) {
        this.type = contactFieldType;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contact_id")
    public ContactDTO getContact() {
        return this.contact;
    }
    
    public void setContact(ContactDTO contact) {
        this.contact = contact;
    }
    
    @Column(name="content", nullable=false, length=100)
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    @Transient
    public int getTypeId() {
        return this.getType().getId();
    }
    
}


