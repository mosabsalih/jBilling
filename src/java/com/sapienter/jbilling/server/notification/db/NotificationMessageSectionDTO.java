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
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@TableGenerator(
        name = "notification_message_section_GEN", 
        table = "jbilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "notification_message_section", 
        allocationSize = 100)
@Table(name = "notification_message_section")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class NotificationMessageSectionDTO implements Serializable {

    private int id;
    private NotificationMessageDTO notificationMessage;
    private Integer section;
    private Set<NotificationMessageLineDTO> notificationMessageLines = new HashSet<NotificationMessageLineDTO>(
            0);
    private int versionNum;

    public NotificationMessageSectionDTO() {
    }

    public NotificationMessageSectionDTO(int id) {
        this.id = id;
    }

    public NotificationMessageSectionDTO(int id,
            NotificationMessageDTO notificationMessage, Integer section,
            Set<NotificationMessageLineDTO> notificationMessageLines) {
        this.id = id;
        this.notificationMessage = notificationMessage;
        this.section = section;
        this.notificationMessageLines = notificationMessageLines;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "notification_message_section_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    public NotificationMessageDTO getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(
            NotificationMessageDTO notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    @Column(name = "section")
    public Integer getSection() {
        return this.section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "notificationMessageSection")
    @Fetch( FetchMode.JOIN  )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<NotificationMessageLineDTO> getNotificationMessageLines() {
        return this.notificationMessageLines;
    }

    public void setNotificationMessageLines(
            Set<NotificationMessageLineDTO> notificationMessageLines) {
        this.notificationMessageLines = notificationMessageLines;
    }
    
    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

}
