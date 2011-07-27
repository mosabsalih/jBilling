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

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Report
 *
 * @author Brian Cowdery
 * @since 07/03/11
 */
@Entity
@Table(name = "report")
@TableGenerator(
    name = "report_GEN",
    table = "jbilling_seqs",
    pkColumnName = "name",
    valueColumnName = "next_id",
    pkColumnValue = "report",
    allocationSize = 10
)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ReportDTO extends AbstractDescription implements Serializable {

    public static final String BASE_PATH = Util.getSysProp("base_dir") + File.separator + "reports" + File.separator;

    private int id;
    private Set<CompanyDTO> entities = new HashSet<CompanyDTO>();
    private ReportTypeDTO type;
    private String name;
    private String fileName;
    private List<ReportParameterDTO<?>> parameters = new ArrayList<ReportParameterDTO<?>>();
    private Integer versionNum;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "entity_report_map",
           joinColumns = {
                   @JoinColumn(name = "report_id", updatable = false)
           },
           inverseJoinColumns = {
                   @JoinColumn(name = "entity_id", updatable = false)
           }
    )
    public Set<CompanyDTO> getEntities() {
        return entities;
    }

    public void setEntities(Set<CompanyDTO> entities) {
        this.entities = entities;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    public ReportTypeDTO getType() {
        return type;
    }

    public void setType(ReportTypeDTO type) {
        this.type = type;
    }

    @Column(name = "name", updatable = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "file_name", updatable = true, nullable = false)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the base path for this Jasper Report file on disk.
     *
     * @return base path for the Jasper Report file
     */
    @Transient
    public String getReportBaseDir() {
        return BASE_PATH + getType().getName() + File.separator;
    }

    /**
     * Returns the expected path for the Jasper Report file on disk.
     *
     * @return path to Jasper Report file.
     */
    @Transient
    public String getReportFilePath() {
        return getReportBaseDir() + getFileName();
    }

    /**
     * Returns a File object for the Jasper Report file.
     *
     * @return Jasper Report file.
     */
    @Transient
    public File getReportFile() {
        return fileName != null ? new File(getReportFilePath()) : null;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "report")
    @Fetch(FetchMode.SELECT)
    public List<ReportParameterDTO<?>> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportParameterDTO<?>> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns a ReportParameterDTO instance by name.
     *
     * @param name parameter name
     * @return found parameter in report parameters list
     */
    @Transient
    public ReportParameterDTO<?> getParameter(String name) {
        for (ReportParameterDTO<?> parameter : parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Converts the report parameters list to a map of objects. Parameter names
     * are used as keys and the set parameter values are used as map values.
     *
     * @return map of objects.
     */
    @Transient
    public Map<String, Object> getParameterMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (ReportParameterDTO<?> parameter : parameters) {
            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
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
        return Constants.TABLE_REPORT;
    }

    @Override
    public String toString() {
        return "Report{"
               + "id=" + id
               + ", type=" + (type != null ? type.getName() : null)
               + ", fileName='" + fileName + '\''
               + '}';
    }
}
