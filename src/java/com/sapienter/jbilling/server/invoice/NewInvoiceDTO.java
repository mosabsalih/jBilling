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

/*
 * Created on 20-Apr-2003
 *
 */
package com.sapienter.jbilling.server.invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.order.TimePeriod;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import java.util.ArrayList;

public class NewInvoiceDTO extends InvoiceDTO {

    private List<OrderDTO> orders = null;
    private Set<InvoiceDTO> invoices = null;
    private List<InvoiceLineDTO> resultLines = null;
    private List<List<PeriodOfTime>> periods = new ArrayList<List<PeriodOfTime>>();
    private Map<Integer, BigDecimal> orderTotalContributions = null;
    private Integer entityId = null;
    private Date billingDate = null;
    private TimePeriod dueDatePeriod = null;
    boolean dateIsRecurring;
    private static final Logger LOG = Logger.getLogger(NewInvoiceDTO.class);

    public NewInvoiceDTO() {
        orders = new ArrayList<OrderDTO>();
        invoices = new HashSet<InvoiceDTO>();
        resultLines = new ArrayList<InvoiceLineDTO>();
        orderTotalContributions = new HashMap<Integer, BigDecimal>();
        LOG.debug("New invoice object with date = " + billingDate);
    }

    public void setDate(Date newDate) {
        billingDate = newDate;
    }

    /**
     * Use the earliest day, with priority to recurring orders
     * Used only for the parameter invoice date = begining of period invoiced
     * @param newDate
     * @param isRecurring
     */
    public void setDate(Date newDate, boolean isRecurring) {
        if (billingDate == null) {
            billingDate = newDate;
            dateIsRecurring = isRecurring;
        } else if (dateIsRecurring) {
            if (newDate.before(billingDate) && isRecurring) {
                billingDate = newDate;
            }
        } else {
            if (!isRecurring && billingDate.before(newDate)) {
            } else {
                billingDate = newDate;
                dateIsRecurring = isRecurring;
            }
        }
    }

    public void addOrder(OrderDTO order, Date start, Date end, List<PeriodOfTime> periods) throws SessionInternalError {
        Logger.getLogger(NewInvoiceDTO.class).debug("Adding order " +
                order.getId() + " to new invoice");
        orders.add(order);
        if (start != null && end != null && start.after(end)) {
            // how come it starts after it ends ???
            throw new SessionInternalError("Adding " +
                    "order " + order.getId() + " with a period that" + " starts after it ends:" + start + " " +
                    end);
        }
        this.periods.add(periods);
    }

    public void addInvoice(InvoiceDTO line) {
        invoices.add(line);
    }

    public List getOrders() {
        return orders;
    }

    public Set<InvoiceDTO> getInvoices() {
        return invoices;
    }

    public List getResultLines() {
        return resultLines;
    }

    public void addResultLine(InvoiceLineDTO line) {
        resultLines.add(line);
    }

    /**
     * 
     * @return If this object holds any order lines or invoice lines,
     * therefore if it makes sense to apply invoice composition tasks to it.
     */
    public boolean isEmpty() {
        return orders.isEmpty() && invoices.isEmpty();
    }

    /**
     * @return If after the invoice composition tasks lines have
     * been inserted in the resultLines vector.
     */
    public boolean areLinesGeneratedEmpty() {
        return resultLines.isEmpty();
    }

    public String validate() {
        String message = null;

        if (getDueDate() == null) {
            // due date is mandaroty
            message = "Due date is null";
        } else if (getDueDate().before(getBillingDate())) {
            // the due date has to be after the invoice's billing date
            message = "Due date has to be past the billing date";
        }

        return message;
    }

    /**
     * @return
     */
    public Date getBillingDate() {
        return billingDate;
    }

    /**
     * @param date
     */
    public void setBillingDate(Date date) {
        billingDate = date;
    }

    public void calculateTotal() {
        Iterator lines = resultLines.iterator();
        BigDecimal total = new BigDecimal(0);
        while (lines.hasNext()) {
            InvoiceLineDTO line = (InvoiceLineDTO) lines.next();
            total = total.add(line.getAmount());
        }

        setTotal(total);
    }

    /**
     * @return
     */
    public List<List<PeriodOfTime>> getPeriods() {
        return periods;
    }

    /**
     * @return Returns the entityId.
     */
    public Integer getEntityId() {
        return entityId;
    }

    /**
     * @param entityId The entityId to set.
     */
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public TimePeriod getDueDatePeriod() {
        return dueDatePeriod;
    }

    public void setDueDatePeriod(TimePeriod dueDatePeriod) {
        this.dueDatePeriod = dueDatePeriod;
    }

    public Map<Integer, BigDecimal> getOrderTotalContributions() {
        return orderTotalContributions;
    }
}
