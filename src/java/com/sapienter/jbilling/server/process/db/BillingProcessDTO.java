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
import java.util.Date;
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

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;

@Entity
@TableGenerator(name = "billing_process_GEN", 
                table = "jbilling_seqs", 
                pkColumnName = "name", 
                valueColumnName = "next_id", 
                pkColumnValue = "billing_process", 
                allocationSize = 10)
@Table(name = "billing_process")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BillingProcessDTO implements Serializable {

    private int id;
    private PeriodUnitDTO periodUnitDTO;
    private PaperInvoiceBatchDTO paperInvoiceBatch;
    private CompanyDTO entity;
    private Date billingDate;
    private int periodValue;
    private int isReview;
    private int retriesToDo;
    private Set<OrderProcessDTO> orderProcesses = new HashSet<OrderProcessDTO>(
            0);
    private Set<InvoiceDTO> invoices = new HashSet<InvoiceDTO>(0);
    private Set<ProcessRunDTO> processRuns = new HashSet<ProcessRunDTO>(0);
    private int versionNum;
    private static final Logger LOG = Logger.getLogger(BillingProcessDTO.class);

    public BillingProcessDTO() {
    }

    public BillingProcessDTO(int id, PeriodUnitDTO periodUnitDTO,
            CompanyDTO entity, Date billingDate, int periodValue, int isReview,
            int retriesToDo) {
        this.id = id;
        this.periodUnitDTO = periodUnitDTO;
        this.entity = entity;
        this.billingDate = billingDate;
        this.periodValue = periodValue;
        this.isReview = isReview;
        this.retriesToDo = retriesToDo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "billing_process_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_unit_id", nullable = false)
    public PeriodUnitDTO getPeriodUnit() {
        return this.periodUnitDTO;
    }

    public void setPeriodUnit(PeriodUnitDTO periodUnitDTO) {
        this.periodUnitDTO = periodUnitDTO;
    }


    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="paper_invoice_batch_id")
    public PaperInvoiceBatchDTO getPaperInvoiceBatch() {
        return this.paperInvoiceBatch;
    }
    
    public void setPaperInvoiceBatch(PaperInvoiceBatchDTO paperInvoiceBatch) {
        this.paperInvoiceBatch = paperInvoiceBatch;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    public CompanyDTO getEntity() {
        return this.entity;
    }

    public void setEntity(CompanyDTO entity) {
        this.entity = entity;
    }

    @Column(name = "billing_date", nullable = false, length = 13)
    public Date getBillingDate() {
        return this.billingDate;
    }

    public void setBillingDate(Date billingDate) {
        this.billingDate = billingDate;
    }

    @Column(name = "period_value", nullable = false)
    public int getPeriodValue() {
        return this.periodValue;
    }

    public void setPeriodValue(int periodValue) {
        this.periodValue = periodValue;
    }

    @Column(name = "is_review", nullable = false)
    public int getIsReview() {
        return this.isReview;
    }

    public void setIsReview(int isReview) {
        this.isReview = isReview;
    }

    @Column(name = "retries_to_do", nullable = false)
    public int getRetriesToDo() {
        return this.retriesToDo;
    }

    public void setRetriesToDo(int retriesToDo) {
        this.retriesToDo = retriesToDo;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "billingProcess")
    public Set<OrderProcessDTO> getOrderProcesses() {
        return this.orderProcesses;
    }

    public void setOrderProcesses(Set<OrderProcessDTO> orderProcesses) {
        this.orderProcesses = orderProcesses;
    }

    // this is useful for the cascade, but any call to it will be very expensive and even
    // inacurate. Use InvoiceDAS.findByProcess instead
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "billingProcess")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<InvoiceDTO> getInvoices() {
        return this.invoices;
    }

    public void setInvoices(Set<InvoiceDTO> invoices) {
        this.invoices = invoices;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "billingProcess")
    @OrderBy( clause = "id")
    public Set<ProcessRunDTO> getProcessRuns() {
        return this.processRuns;
    }

    public void setProcessRuns(Set<ProcessRunDTO> processRuns) {
        this.processRuns = processRuns;
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
        StringBuffer ret = new StringBuffer("BillingProcessDTO: id: " + id + " periodUint " + periodUnitDTO + " paperInvoiceBatch "
                + paperInvoiceBatch  + " entity " + entity + " billingDate " + billingDate
                + " periodValue " + periodValue + " isReview " + isReview + " retriesToDo " + retriesToDo +
                " orderProcesses (count) " + orderProcesses.size() +
                " invoices (count) " + invoices.size() + " processRuns ");
        for (ProcessRunDTO run: processRuns) {
            ret.append(run.toString());
        }
        return ret.toString();
    }
}
