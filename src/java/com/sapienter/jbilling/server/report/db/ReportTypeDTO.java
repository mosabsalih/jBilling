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

package com.sapienter.jbilling.server.report.db;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * ReportType
 *
 * @author Brian Cowdery
 * @since 07/03/11
 */
@Entity
@Table(name = "report_type")
@TableGenerator(
    name = "report_type_GEN",
    table = "jbilling_seqs",
    pkColumnName = "name",
    valueColumnName = "next_id",
    pkColumnValue = "report_type",
    allocationSize = 10
)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ReportTypeDTO extends AbstractDescription implements Serializable {

    private int id;
    private String name;
    private Set<ReportDTO> reports = new HashSet<ReportDTO>();
    private Integer versionNum;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_type_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "name", updatable = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.replaceAll(" ", "_");
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "type")
    public Set<ReportDTO> getReports() {
        return reports;
    }

    public void setReports(Set<ReportDTO> reports) {
        this.reports = reports;
    }

    @Version
    @Column(name = "OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_REPORT_TYPE;
    }

    @Override
    public String toString() {
        return "ReportType{"
               + "id=" + id
               + ", name='" + name + '\''
               + '}';
    }
}
