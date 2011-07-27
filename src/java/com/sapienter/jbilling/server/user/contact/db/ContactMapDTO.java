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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.util.db.JbillingTable;

@Entity
@TableGenerator(
        name="contact_map_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="contact_map",
        allocationSize = 100
        )
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name="contact_map"
    , uniqueConstraints = @UniqueConstraint(columnNames="contact_id") 
)
public class ContactMapDTO  implements java.io.Serializable {


     private int id;
     private JbillingTable jbillingTable;
     private ContactTypeDTO contactType;
     private ContactDTO contact;
     private int foreignId;
     private int versionNum;

    public ContactMapDTO() {
    }

    
    public ContactMapDTO(int id, JbillingTable jbillingTable, ContactTypeDTO contactType, int foreignId) {
        this.id = id;
        this.jbillingTable = jbillingTable;
        this.contactType = contactType;
        this.foreignId = foreignId;
    }
    public ContactMapDTO(int id, JbillingTable jbillingTable, ContactTypeDTO contactType, ContactDTO contact, int foreignId) {
       this.id = id;
       this.jbillingTable = jbillingTable;
       this.contactType = contactType;
       this.contact = contact;
       this.foreignId = foreignId;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="contact_map_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="table_id", nullable=false)
    public JbillingTable getJbillingTable() {
        return this.jbillingTable;
    }
    
    public void setJbillingTable(JbillingTable jbillingTable) {
        this.jbillingTable = jbillingTable;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", nullable=false)
    public ContactTypeDTO getContactType() {
        return this.contactType;
    }
    
    public void setContactType(ContactTypeDTO contactType) {
        this.contactType = contactType;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contact_id", unique=true)
    public ContactDTO getContact() {
        return this.contact;
    }
    
    public void setContact(ContactDTO contact) {
        this.contact = contact;
    }
    
    @Column(name="foreign_id", nullable=false)
    public int getForeignId() {
        return this.foreignId;
    }
    
    public void setForeignId(int foreignId) {
        this.foreignId = foreignId;
    }

    @Version
    @Column(name="OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }
}


