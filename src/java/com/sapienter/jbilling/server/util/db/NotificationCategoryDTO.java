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
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.notification.db.NotificationMessageTypeDTO;
import com.sapienter.jbilling.server.util.Constants;

@Entity
@Table(name="notification_category")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class NotificationCategoryDTO extends AbstractDescription implements java.io.Serializable {

    private int id;
    private Set<NotificationMessageTypeDTO> messageTypes = new HashSet<NotificationMessageTypeDTO>(0);

    public NotificationCategoryDTO() {
    }

    // for stubs
    public NotificationCategoryDTO(Integer id) {
        this.id = id;
    }
    
    public NotificationCategoryDTO(Integer id, Set<NotificationMessageTypeDTO> messageTypes) {
        this.id = id;
        this.messageTypes= messageTypes;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_NOTIFICATION_CATEGORY;
    }

    @Id
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "category")
	public Set<NotificationMessageTypeDTO> getMessageTypes() {
		return messageTypes;
	}

	public void setMessageTypes(Set<NotificationMessageTypeDTO> messageTypes) {
		this.messageTypes = messageTypes;
	}



}


