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
package com.sapienter.jbilling.server.util.db;


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name="international_description")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class InternationalDescriptionDTO  implements java.io.Serializable {


     private InternationalDescriptionId id;
     private String content;
     // OPTLOCK: it makes no sense in this entity. The optimistic locking id done at the
     // parent level (the entity that delegates the column to this one).
     // OR, the content is simple readonly

    public InternationalDescriptionDTO() {
    }

    public InternationalDescriptionDTO(InternationalDescriptionId id, String content) {
       this.id = id;
       this.content = content;
    }
   
     @EmbeddedId
    
    @AttributeOverrides( {
        @AttributeOverride(name="tableId", column=@Column(name="table_id", nullable=false) ), 
        @AttributeOverride(name="foreignId", column=@Column(name="foreign_id", nullable=false) ), 
        @AttributeOverride(name="psudoColumn", column=@Column(name="psudo_column", nullable=false, length=20) ), 
        @AttributeOverride(name="languageId", column=@Column(name="language_id", nullable=false) ) } )
    public InternationalDescriptionId getId() {
        return this.id;
    }
    
    public void setId(InternationalDescriptionId id) {
        this.id = id;
    }
    
    @Column(name="content", nullable=false, length=5000)
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}


