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
package com.sapienter.jbilling.server.process.db;

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

import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.UserStatusDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(name = "ageing_entity_step_GEN",
               table = "jbilling_seqs",
               pkColumnName = "name",
               valueColumnName = "next_id",
               pkColumnValue = "ageing_entity_step",
               allocationSize = 100)
@Table(name = "ageing_entity_step")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AgeingEntityStepDTO extends AbstractDescription implements Serializable {

    private int id;
    private CompanyDTO company;
    private UserStatusDTO userStatus;
    private int days;
    private int versionNum;

    public AgeingEntityStepDTO() {
    }

    public AgeingEntityStepDTO(int id, int days) {
        this.id = id;
        this.days = days;
    }

    public AgeingEntityStepDTO(int id, CompanyDTO entity,
            UserStatusDTO userStatus, int days) {
        this.id = id;
        this.company = entity;
        this.userStatus = userStatus;
        this.days = days;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_AGEING_ENTITY_STEP;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ageing_entity_step_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    public CompanyDTO getCompany() {
        return this.company;
    }

    public void setCompany(CompanyDTO entity) {
        this.company = entity;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    public UserStatusDTO getUserStatus() {
        return this.userStatus;
    }

    public void setUserStatus(UserStatusDTO userStatus) {
        this.userStatus = userStatus;
    }

    @Column(name = "days", nullable = false)
    public int getDays() {
        return this.days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getWelcomeMessage(Integer languageId) {
        return getDescription(languageId, "welcome_message");
    }

    public void setWelcomeMessage(Integer languageId, String message) {
        if (message == null) {
            message = "";
        }
        setDescription("welcome_message", languageId, message);
    }

    public String getFailedLoginMessage(Integer languageId) {
        return getDescription(languageId, "failed_login_message");
    }

    public void setFailedLoginMessage(Integer languageId, String message) {
        if (message == null) {
            message = "";
        }
        setDescription("failed_login_message", languageId, message);
    }


    @Version
    @Column(name = "OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }
}
