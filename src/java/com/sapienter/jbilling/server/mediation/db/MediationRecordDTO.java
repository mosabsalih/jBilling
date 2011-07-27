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
package com.sapienter.jbilling.server.mediation.db;

import com.sapienter.jbilling.server.mediation.MediationRecordWS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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

@TableGenerator(
        name = "mediation_record_GEN",
        table = "jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue = "mediation_record", 
        allocationSize = 100)
@Entity
@Table(name = "mediation_record")
// no cache : it is hardly ever re-read 
public class MediationRecordDTO implements Serializable {

    private Integer id;
    private String key;
    private Date started;
    private MediationProcess process;
    private MediationRecordStatusDTO recordStatus;
    private Collection<MediationRecordLineDTO> lines = new ArrayList<MediationRecordLineDTO>();
    private int optlock;

    protected MediationRecordDTO() {
    }

    public MediationRecordDTO(String key, Date started, MediationProcess process, MediationRecordStatusDTO recordStatus) {
        this.key = key;
        this.started = started;
        this.process = process;
        this.recordStatus = recordStatus;
    }

    public MediationRecordDTO(MediationRecordWS ws, MediationProcess process, MediationRecordStatusDTO recordStatus,
                              Collection<MediationRecordLineDTO> lines) {

        this.id = ws.getId();
        this.key = ws.getKey();
        this.started = ws.getStarted();
        this.process = process;
        this.recordStatus = recordStatus;
        this.lines = lines;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "mediation_record_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "id_key", nullable = false)
    public String getKey() {
        return key;
    }

    protected void setKey(String key) {
        this.key = key;
    }

    @Column(name = "start_datetime")
    public Date getStarted() {
        return started;
    }
    
    protected void setStarted(Date started) {
        this.started = started;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediation_process_id")
    public MediationProcess getProcess() {
        return process;
    }

    public void setProcess(MediationProcess process) {
        this.process = process;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    public MediationRecordStatusDTO getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(MediationRecordStatusDTO recordStatus) {
        this.recordStatus = recordStatus;
    }

    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY, mappedBy="record")
    public Collection<MediationRecordLineDTO> getLines() {
        return lines;
    }

    public void setLines(Collection<MediationRecordLineDTO> lines) {
        this.lines = lines;
    }

    @Version
    @Column(name = "OPTLOCK")
    public int getOptlock() {
        return optlock;
    }

    protected void setOptlock(int optlock) {
        this.optlock = optlock;
    }
}
