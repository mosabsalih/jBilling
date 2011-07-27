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


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(name = "paper_invoice_batch_GEN", 
                table = "jbilling_seqs", 
                pkColumnName = "name", 
                valueColumnName = "next_id", 
                pkColumnValue = "paper_invoice_batch", 
                allocationSize = 100)
@Table(name="paper_invoice_batch")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PaperInvoiceBatchDTO implements java.io.Serializable {

    private int id;
    private int totalInvoices;
    private Date deliveryDate;
    private int isSelfManaged;
    private BillingProcessDTO billingProcesses = null;
    private Set<InvoiceDTO> invoices = new HashSet<InvoiceDTO>(0);
    private int versionNum;

    public PaperInvoiceBatchDTO() {
    }

    public PaperInvoiceBatchDTO(int id, int totalInvoices, int isSelfManaged) {
        this.id = id;
        this.totalInvoices = totalInvoices;
        this.isSelfManaged = isSelfManaged;
    }

    public PaperInvoiceBatchDTO(int id, int totalInvoices, Date deliveryDate, int isSelfManaged, BillingProcessDTO billingProcesses, Set<InvoiceDTO> invoices) {
        this.id = id;
        this.totalInvoices = totalInvoices;
        this.deliveryDate = deliveryDate;
        this.isSelfManaged = isSelfManaged;
        this.billingProcesses = billingProcesses;
        this.invoices = invoices;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "paper_invoice_batch_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "total_invoices", nullable = false)
    public int getTotalInvoices() {
        return this.totalInvoices;
    }

    public void setTotalInvoices(int totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    @Column(name = "delivery_date", length = 13)
    public Date getDeliveryDate() {
        return this.deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Column(name = "is_self_managed", nullable = false)
    public int getIsSelfManaged() {
        return this.isSelfManaged;
    }

    public void setIsSelfManaged(int isSelfManaged) {
        this.isSelfManaged = isSelfManaged;
    }
    
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="paperInvoiceBatch")
    public BillingProcessDTO getProcess() {
        return this.billingProcesses;
    }
    
    public void setProcess(BillingProcessDTO billingProcesses) {
        this.billingProcesses = billingProcesses;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "paperInvoiceBatch")
    public Set<InvoiceDTO> getInvoices() {
        return this.invoices;
    }

    public void setInvoices(Set<InvoiceDTO> invoices) {
        this.invoices = invoices;
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


