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

package com.sapienter.jbilling.server.process;

import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;
import com.sapienter.jbilling.server.util.api.validation.UpdateValidationGroup;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * BillingProcessConfigurationWS
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class BillingProcessConfigurationWS implements Serializable {

    private int id;
    private Integer periodUnitId;
    private Integer entityId;
    @NotNull(message = "validation.error.is.required")
    private Date nextRunDate;
    private Integer generateReport;
    private Integer retries;
    private Integer daysForRetry;
    private Integer daysForReport;
    private int reviewStatus;
    @Min(value = 1, message = "validation.error.min,1")
    private int periodValue;
    private int dueDateUnitId;
    private int dueDateValue;
    private Integer dfFm;
    private Integer onlyRecurring;
    private Integer invoiceDateProcess;
    private Integer autoPayment;
    @Min(value = 1, message = "validation.error.min,1")
    private int maximumPeriods;
    private int autoPaymentApplication;
    
    public BillingProcessConfigurationWS() {
    }

    public BillingProcessConfigurationWS(BillingProcessConfigurationDTO dto) {
        this.id = dto.getId();
        this.periodUnitId = dto.getPeriodUnit() != null ? dto.getPeriodUnit().getId() : null ;
        this.entityId = dto.getEntity() != null ? dto.getEntity().getId() : null;
        this.nextRunDate = dto.getNextRunDate();
        this.generateReport = dto.getGenerateReport();
        this.retries = dto.getRetries();
        this.daysForRetry = dto.getDaysForRetry();
        this.daysForReport = dto.getDaysForReport();
        this.reviewStatus = dto.getReviewStatus();
        this.periodValue = dto.getPeriodValue();
        this.dueDateUnitId = dto.getDueDateUnitId();
        this.dueDateValue = dto.getDueDateValue();
        this.dfFm = dto.getDfFm();
        this.onlyRecurring = dto.getOnlyRecurring();
        this.invoiceDateProcess = dto.getInvoiceDateProcess();
        this.autoPayment = dto.getAutoPayment();
        this.maximumPeriods = dto.getMaximumPeriods();
        this.autoPaymentApplication = dto.getAutoPaymentApplication();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getPeriodUnitId() {
        return periodUnitId;
    }

    public void setPeriodUnitId(Integer periodUnitId) {
        this.periodUnitId = periodUnitId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Date getNextRunDate() {
        return nextRunDate;
    }

    public void setNextRunDate(Date nextRunDate) {
        this.nextRunDate = nextRunDate;
    }

    public Integer getGenerateReport() {
        return generateReport;
    }

    public void setGenerateReport(Integer generateReport) {
        this.generateReport = generateReport;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getDaysForRetry() {
        return daysForRetry;
    }

    public void setDaysForRetry(Integer daysForRetry) {
        this.daysForRetry = daysForRetry;
    }

    public Integer getDaysForReport() {
        return daysForReport;
    }

    public void setDaysForReport(Integer daysForReport) {
        this.daysForReport = daysForReport;
    }

    public int getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(int reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public int getPeriodValue() {
        return periodValue;
    }

    public void setPeriodValue(int periodValue) {
        this.periodValue = periodValue;
    }

    public int getDueDateUnitId() {
        return dueDateUnitId;
    }

    public void setDueDateUnitId(int dueDateUnitId) {
        this.dueDateUnitId = dueDateUnitId;
    }

    public int getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(int dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public Integer getDfFm() {
        return dfFm;
    }

    public void setDfFm(Integer dfFm) {
        this.dfFm = dfFm;
    }

    public Integer getOnlyRecurring() {
        return onlyRecurring;
    }

    public void setOnlyRecurring(Integer onlyRecurring) {
        this.onlyRecurring = onlyRecurring;
    }

    public Integer getInvoiceDateProcess() {
        return invoiceDateProcess;
    }

    public void setInvoiceDateProcess(Integer invoiceDateProcess) {
        this.invoiceDateProcess = invoiceDateProcess;
    }

    public Integer getAutoPayment() {
        return autoPayment;
    }

    public void setAutoPayment(Integer autoPayment) {
        this.autoPayment = autoPayment;
    }

    public int getMaximumPeriods() {
        return maximumPeriods;
    }

    public void setMaximumPeriods(int maximumPeriods) {
        this.maximumPeriods = maximumPeriods;
    }

    public int getAutoPaymentApplication() {
        return autoPaymentApplication;
    }

    public void setAutoPaymentApplication(int autoPaymentApplication) {
        this.autoPaymentApplication = autoPaymentApplication;
    }

    @Override
    public String toString() {
        return "BillingProcessConfigurationWS{"
               + "id=" + id
               + ", entityId=" + entityId
               + ", nextRunDate=" + nextRunDate
               + ", generateReport=" + generateReport
               + ", retries=" + retries
               + ", daysForRetry=" + daysForRetry
               + ", daysForReport=" + daysForReport
               + ", reviewStatus=" + reviewStatus
               + ", periodValue=" + periodValue
               + ", dueDateUnitId=" + dueDateUnitId
               + ", dueDateValue=" + dueDateValue
               + ", dfFm=" + dfFm
               + ", onlyRecurring=" + onlyRecurring
               + ", invoiceDateProcess=" + invoiceDateProcess
               + ", autoPayment=" + autoPayment
               + ", maximumPeriods=" + maximumPeriods
               + ", autoPaymentApplication=" + autoPaymentApplication
               + ", periodUnitId=" + periodUnitId
               + '}';
    }
}
