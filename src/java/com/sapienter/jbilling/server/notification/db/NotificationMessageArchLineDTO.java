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
import javax.persistence.Version;

@Entity
@TableGenerator(
        name = "notification_message_arch_line_GEN", 
        table = "jbilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "notification_message_arch_line", 
        allocationSize = 100)
@Table(name = "notification_message_arch_line")
public class NotificationMessageArchLineDTO implements Serializable {

    private int id;
    private NotificationMessageArchDTO notificationMessageArch;
    private int section;
    private String content;
    private int versionNum;

    public NotificationMessageArchLineDTO() {
    }

    public NotificationMessageArchLineDTO(int id, int section, String content) {
        this.id = id;
        this.section = section;
        this.content = content;
    }

    public NotificationMessageArchLineDTO(int id,
            NotificationMessageArchDTO notificationMessageArch, int section,
            String content) {
        this.id = id;
        this.notificationMessageArch = notificationMessageArch;
        this.section = section;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "notification_message_arch_line_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_archive_id")
    public NotificationMessageArchDTO getNotificationMessageArch() {
        return this.notificationMessageArch;
    }

    public void setNotificationMessageArch(
            NotificationMessageArchDTO notificationMessageArch) {
        this.notificationMessageArch = notificationMessageArch;
    }

    @Column(name = "section", nullable = false)
    public int getSection() {
        return this.section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    @Column(name = "content", nullable = false, length = 1000)
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
}
