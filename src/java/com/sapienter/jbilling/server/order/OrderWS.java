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

package com.sapienter.jbilling.server.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import com.sapienter.jbilling.server.invoice.InvoiceWS;
import com.sapienter.jbilling.server.order.validator.DateRange;
import com.sapienter.jbilling.server.security.WSSecured;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Emil
 */

@DateRange(start = "activeSince", end = "activeUntil", message = "validation.activeUntil.before.activeSince")
public class OrderWS implements WSSecured, Serializable {

    private Integer id;
    private Integer statusId;
    private Integer isCurrent;
    @NotNull(message = "validation.error.null.user.id")
    private Integer userId = null;
    @NotNull(message = "validation.error.null.currency")
    private Integer currencyId = null;
    @NotNull(message = "validation.error.null.billing.type")
    private Integer billingTypeId;
    @NotNull(message = "validation.error.null.period")
    private Integer period = null;
    private Date createDate;
    private Integer createdBy;
    @NotNull(message = "validation.error.null.activeSince")
    private Date activeSince;
    private Date activeUntil;
    private Date cycleStarts;
    private Date nextBillableDay;
    private int deleted;
    private Integer notify;
    private Date lastNotified;
    private Integer notificationStep;
    private Integer dueDateUnitId;
    private Integer dueDateValue;
    private Integer dfFm;
    private Integer anticipatePeriods;
    private Integer ownInvoice;
    private String notes;
    private Integer notesInInvoice;
    @NotEmpty(message = "validation.error.empty.lines") @Valid
    private OrderLineWS orderLines[] = null;
    private String pricingFields = null;
    private InvoiceWS[] generatedInvoices= null;

    // balances
    private String total;

    // textual descriptions
    private String statusStr = null;
    private String timeUnitStr = null;
    private String periodStr = null;
    private String billingTypeStr = null;

    // optlock (not necessary)
    private Integer versionNum;

    public OrderWS() {
    }

    public OrderWS(Integer id, Integer billingTypeId, Integer notify, Date activeSince, Date activeUntil,
                   Date createDate, Date nextBillableDay, Integer createdBy, Integer statusId, Integer deleted,
                   Integer currencyId, Date lastNotified, Integer notifStep, Integer dueDateUnitId, Integer dueDateValue,
                   Integer anticipatePeriods, Integer dfFm, Integer isCurrent, String notes, Integer notesInInvoice,
                   Integer ownInvoice, Integer period, Integer userId, Integer version, Date cycleStarts) {
        setId(id);
        setBillingTypeId(billingTypeId);
        setNotify(notify);
        setActiveSince(activeSince);
        setActiveUntil(activeUntil);
        setAnticipatePeriods(anticipatePeriods);
        setCreateDate(createDate);
        setNextBillableDay(nextBillableDay);
        setCreatedBy(createdBy);
        setStatusId(statusId);
        setDeleted(deleted.shortValue());
        setCurrencyId(currencyId);
        setLastNotified(lastNotified);
        setNotificationStep(notifStep);
        setDueDateUnitId(dueDateUnitId);
        setDueDateValue(dueDateValue);
        setDfFm(dfFm);
        setIsCurrent(isCurrent);
        setNotes(notes);
        setNotesInInvoice(notesInInvoice);
        setOwnInvoice(ownInvoice);
        setPeriod(period);
        setUserId(userId);
        setVersionNum(version);
        setCycleStarts(cycleStarts);
    }

    public InvoiceWS[] getGeneratedInvoices() {
		return generatedInvoices;
	}

	public void setGeneratedInvoices(InvoiceWS[] generatedInvoices) {
		this.generatedInvoices = generatedInvoices;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer current) {
        isCurrent = current;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getBillingTypeId() {
        return billingTypeId;
    }

    public void setBillingTypeId(Integer billingTypeId) {
        this.billingTypeId = billingTypeId;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getActiveSince() {
        return activeSince;
    }

    public void setActiveSince(Date activeSince) {
        this.activeSince = activeSince;
    }

    public Date getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(Date activeUntil) {
        this.activeUntil = activeUntil;
    }

    public Date getCycleStarts() {
        return cycleStarts;
    }

    public void setCycleStarts(Date cycleStarts) {
        this.cycleStarts = cycleStarts;
    }

    public Date getNextBillableDay() {
        return nextBillableDay;
    }

    public void setNextBillableDay(Date nextBillableDay) {
        this.nextBillableDay = nextBillableDay;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public Integer getNotify() {
        return notify;
    }

    public void setNotify(Integer notify) {
        this.notify = notify;
    }

    public Date getLastNotified() {
        return lastNotified;
    }

    public void setLastNotified(Date lastNotified) {
        this.lastNotified = lastNotified;
    }

    public Integer getNotificationStep() {
        return notificationStep;
    }

    public void setNotificationStep(Integer notificationStep) {
        this.notificationStep = notificationStep;
    }

    public Integer getDueDateUnitId() {
        return dueDateUnitId;
    }

    public void setDueDateUnitId(Integer dueDateUnitId) {
        this.dueDateUnitId = dueDateUnitId;
    }

    public Integer getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(Integer dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public Integer getDfFm() {
        return dfFm;
    }

    public void setDfFm(Integer dfFm) {
        this.dfFm = dfFm;
    }

    public Integer getAnticipatePeriods() {
        return anticipatePeriods;
    }

    public void setAnticipatePeriods(Integer anticipatePeriods) {
        this.anticipatePeriods = anticipatePeriods;
    }

    public Integer getOwnInvoice() {
        return ownInvoice;
    }

    public void setOwnInvoice(Integer ownInvoice) {
        this.ownInvoice = ownInvoice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getNotesInInvoice() {
        return notesInInvoice;
    }

    public void setNotesInInvoice(Integer notesInInvoice) {
        this.notesInInvoice = notesInInvoice;
    }

    public OrderLineWS[] getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(OrderLineWS[] orderLines) {
        this.orderLines = orderLines;
    }

    public String getPricingFields() {
        return pricingFields;
    }

    public void setPricingFields(String pricingFields) {
        this.pricingFields = pricingFields;
    }

    public String getTotal() {
        return total;
    }

    public BigDecimal getTotalAsDecimal() {
        return total != null ? new BigDecimal(total) : null;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setTotal(BigDecimal total) {
        this.total = (total != null ? total.toString() : null);
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getTimeUnitStr() {
        return timeUnitStr;
    }

    public void setTimeUnitStr(String timeUnitStr) {
        this.timeUnitStr = timeUnitStr;
    }

    public String getPeriodStr() {
        return periodStr;
    }

    public void setPeriodStr(String periodStr) {
        this.periodStr = periodStr;
    }

    public String getBillingTypeStr() {
        return billingTypeStr;
    }

    public void setBillingTypeStr(String billingTypeStr) {
        this.billingTypeStr = billingTypeStr;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    /**
     * Unsupported, web-service security enforced using {@link #getOwningUserId()}
     * @return null
     */
    public Integer getOwningEntityId() {
        return null;
    }

    public Integer getOwningUserId() {
        return getUserId();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("OrderWS");
        sb.append("{id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", currencyId=").append(currencyId);
        sb.append(", activeUntil=").append(activeUntil);
        sb.append(", activeSince=").append(activeSince);
        sb.append(", isCurrent=").append(isCurrent);
        sb.append(", statusStr='").append(statusStr).append('\'');
        sb.append(", periodStr='").append(periodStr).append('\'');
        sb.append(", periodId=").append(period);
        sb.append(", billingTypeStr='").append(billingTypeStr).append('\'');

        sb.append(", lines=");
        if (getOrderLines() != null) {
            sb.append(Arrays.toString(getOrderLines()));
        } else {
            sb.append("[]");
        }

        sb.append('}');
        return sb.toString();
    }
}
