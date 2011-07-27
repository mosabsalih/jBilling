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
package com.sapienter.jbilling.server.notification.db;

import java.io.Serializable;
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

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import com.sapienter.jbilling.server.util.db.NotificationCategoryDTO;

@Entity
@TableGenerator(
        name = "notification_message_type_GEN", 
        table = "jbilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "notification_message_type", 
        allocationSize = 100)
@Table(name = "notification_message_type")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class NotificationMessageTypeDTO extends AbstractDescription implements Serializable {

    private int id;
    private NotificationCategoryDTO category;
    private Set<NotificationMessageDTO> notificationMessages = new HashSet<NotificationMessageDTO>(
            0);
    private int versionNum;

    public NotificationMessageTypeDTO() {
    }

    public NotificationMessageTypeDTO(int id) {
        this.id = id;
    }

    public NotificationMessageTypeDTO(int id, NotificationCategoryDTO category, 
            Set<NotificationMessageDTO> notificationMessages) {
        this.id = id;
        this.category= category;
        this.notificationMessages = notificationMessages;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "notification_message_type_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    public NotificationCategoryDTO getCategory() {
		return category;
	}

	public void setCategory(NotificationCategoryDTO category) {
		this.category = category;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "notificationMessageType")
    public Set<NotificationMessageDTO> getNotificationMessages() {
        return this.notificationMessages;
    }

    public void setNotificationMessages(
            Set<NotificationMessageDTO> notificationMessages) {
        this.notificationMessages = notificationMessages;
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
    protected String getTable() {
        return Constants.TABLE_NOTIFICATION_MESSAGE_TYPE;
    }
    
}
