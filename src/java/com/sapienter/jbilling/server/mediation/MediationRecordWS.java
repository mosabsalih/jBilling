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

package com.sapienter.jbilling.server.mediation;

import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MediationRecordWS
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class MediationRecordWS implements Serializable {

    private Integer id;
    private String key;
    private Date started;
    private Integer processId;
    private Integer recordStatusId;
    private List<MediationRecordLineWS> lines = new ArrayList<MediationRecordLineWS>();

    public MediationRecordWS() {
    }

    public MediationRecordWS(MediationRecordDTO dto, List<MediationRecordLineWS> lines) {
        this.id = dto.getId();
        this.key = dto.getKey();
        this.started = dto.getStarted();
        this.processId = dto.getProcess() != null ? dto.getProcess().getId() : null;
        this.recordStatusId = dto.getRecordStatus() != null ? dto.getRecordStatus().getId() : null;
        this.lines = lines;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public Integer getRecordStatusId() {
        return recordStatusId;
    }

    public void setRecordStatusId(Integer recordStatusId) {
        this.recordStatusId = recordStatusId;
    }

    public List<MediationRecordLineWS> getLines() {
        return lines;
    }

    public void setLines(List<MediationRecordLineWS> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "MediationRecordWS{"
               + "id=" + id
               + ", key='" + key + '\''
               + ", started=" + started
               + ", processId=" + processId
               + ", recordStatusId=" + recordStatusId
               + ", lines=" + (lines != null ? lines.size() : null)
               + '}';
    }
}
