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


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.notification.db.NotificationMessageDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;

@Entity
@Table(name="language")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class LanguageDTO  implements java.io.Serializable {


     private int id;
     private String code;
     private String description;
     private Set<NotificationMessageDTO> notificationMessages = new HashSet<NotificationMessageDTO>(0);
     private Set<CompanyDTO> entities = new HashSet<CompanyDTO>(0);
     private Set<UserDTO> baseUsers = new HashSet<UserDTO>(0);

    public LanguageDTO() {
    }

    public LanguageDTO(int id) {
        this.id = id;
    }
    
    public LanguageDTO(int id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }
    public LanguageDTO(int id, String code, String description, Set<NotificationMessageDTO> notificationMessages, Set<CompanyDTO> entities, Set<UserDTO> baseUsers) {
       this.id = id;
       this.code = code;
       this.description = description;
       this.notificationMessages = notificationMessages;
       this.entities = entities;
       this.baseUsers = baseUsers;
    }
   
     @Id 
    
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Column(name="code", nullable=false, length=2)
    public String getCode() {
        return this.code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    @Column(name="description", nullable=false, length=50)
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="language")
    public Set<NotificationMessageDTO> getNotificationMessages() {
        return this.notificationMessages;
    }
    
    public void setNotificationMessages(Set<NotificationMessageDTO> notificationMessages) {
        this.notificationMessages = notificationMessages;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="language")
    public Set<CompanyDTO> getEntities() {
        return this.entities;
    }
    
    public void setEntities(Set<CompanyDTO> entities) {
        this.entities = entities;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="language")
    public Set<UserDTO> getBaseUsers() {
        return this.baseUsers;
    }
    
    public void setBaseUsers(Set<UserDTO> baseUsers) {
        this.baseUsers = baseUsers;
    }
}


