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

import com.sapienter.jbilling.server.user.db.UserDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;

@Entity
@TableGenerator(
        name = "process_run_user_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "process_run_user",
        allocationSize = 100)
@Table(name = "process_run_user")
// No cache
public class ProcessRunUserDTO implements java.io.Serializable {

    public static final Integer STATUS_FAILED = 0;
    public static final Integer STATUS_SUCCEEDED = 1;

    private int id;
    private ProcessRunDTO processRun;
    private UserDTO user;
    private Integer status;
    private Date created;

    private int versionNum;

    public ProcessRunUserDTO() {
    }

    public ProcessRunUserDTO(int id, ProcessRunDTO processRun, UserDTO user, Integer status, Date created) {
        this.id = id;
        this.processRun = processRun;
        this.user = user;
        this.status = status;
        this.created = created;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "process_run_user_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_run_id")
    public ProcessRunDTO getProcessRun() {
        return this.processRun;
    }

    public void setProcessRun(ProcessRunDTO processRun) {
        this.processRun = processRun;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserDTO getUser() {
        return this.user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Column(name = "status", nullable = false)
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "created", nullable = false, length = 29)
    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Version
    @Column(name = "OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(" ProcessRunUserDTO: id: ")
                .append(id)
                .append(" user: ")
                .append(user)
                .append(" created: ")
                .append(created)
                .append(" status: ")
                .append(status);

        return ret.toString();
    }
}
