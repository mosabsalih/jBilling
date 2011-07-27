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

import java.math.BigDecimal;
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

import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import java.util.HashSet;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(
        name = "process_run_total_GEN", 
        table = "jbilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "process_run_total", 
        allocationSize = 100)
@Table(name = "process_run_total")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProcessRunTotalDTO implements java.io.Serializable {

    private int id;
    private ProcessRunDTO processRun;
    private CurrencyDTO currencyDTO;
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalNotPaid;

    private Set<ProcessRunTotalPmDTO> totalsPaymentMethod = new HashSet<ProcessRunTotalPmDTO>(0);
    private int versionNum;
    
    public ProcessRunTotalDTO() {
    }

    public ProcessRunTotalDTO(int id, CurrencyDTO currencyDTO) {
        this.id = id;
        this.currencyDTO = currencyDTO;
    }

    public ProcessRunTotalDTO(int id, ProcessRunDTO processRun, CurrencyDTO currencyDTO,
                              BigDecimal totalInvoiced, BigDecimal totalPaid, BigDecimal totalNotPaid) {
        this.id = id;
        this.processRun = processRun;
        this.currencyDTO = currencyDTO;
        this.totalInvoiced = totalInvoiced;
        this.totalPaid = totalPaid;
        this.totalNotPaid = totalNotPaid;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "process_run_total_GEN")
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
    @JoinColumn(name = "currency_id", nullable = false)
    public CurrencyDTO getCurrency() {
        return this.currencyDTO;
    }

    public void setCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }

    @Column(name = "total_invoiced", precision = 17, scale = 17)
    public BigDecimal getTotalInvoiced() {
        return this.totalInvoiced;
    }

    public void setTotalInvoiced(BigDecimal totalInvoiced) {
        this.totalInvoiced = totalInvoiced;
    }

    @Column(name = "total_paid", precision = 17, scale = 17)
    public BigDecimal getTotalPaid() {
        return this.totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    @Column(name = "total_not_paid", precision = 17, scale = 17)
    public BigDecimal getTotalNotPaid() {
        return this.totalNotPaid;
    }

    public void setTotalNotPaid(BigDecimal totalNotPaid) {
        this.totalNotPaid = totalNotPaid;
    }

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="processRunTotal")
    public Set<ProcessRunTotalPmDTO> getTotalsPaymentMethod() {
        return totalsPaymentMethod;
    }
    
    public void setTotalsPaymentMethod(Set<ProcessRunTotalPmDTO> totalsPaymentMethod) {
        this.totalsPaymentMethod = totalsPaymentMethod;
    }

    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(" ProcessRunTotalDTO: id: ")
                .append(id)
                .append(" currency: ")
                .append(currencyDTO)
                .append(" totalInvoiced: ")
                .append(totalInvoiced)
                .append(" totalPaid: ")
                .append(totalPaid)
                .append(" totalNotPaid: ")
                .append(totalNotPaid)
                .append(" totalsPaymentMethod ");

        for (ProcessRunTotalPmDTO pm : totalsPaymentMethod) {
            ret.append(pm.toString());
        }

        return ret.toString();
    }
}
