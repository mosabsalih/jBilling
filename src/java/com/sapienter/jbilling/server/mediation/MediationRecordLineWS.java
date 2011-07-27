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

import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * MediationRecordLineWS
 *
 * @author Brian Cowdery
 * @since 21-10-2010
 */
public class MediationRecordLineWS implements Serializable {

    private int id;
    private Integer orderLineId;
    private Date eventDate;
    private String amount; // use strings instead of BigDecimal for WS compatibility
    private String quantity;
    private String description;

    public MediationRecordLineWS() {
    }

    public MediationRecordLineWS(MediationRecordLineDTO dto) {
        this.id = dto.getId();
        this.orderLineId = dto.getOrderLine() != null ? dto.getOrderLine().getId() : null;
        this.eventDate = dto.getEventDate();
        setAmount(dto.getAmount());
        setQuantity(dto.getQuantity());
        this.description = dto.getDescription();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(Integer orderLineId) {
        this.orderLineId = orderLineId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public BigDecimal getAmount() {
        return amount != null ? new BigDecimal(amount) : null;
    }

    public void setAmount(BigDecimal amount) {
        if (amount != null)
            this.amount = amount.toString();
    }

    public BigDecimal getQuantity() {
        return quantity != null ? new BigDecimal(quantity) : null;
    }

    public void setQuantity(BigDecimal quantity) {
        if (quantity != null)
            this.quantity = quantity.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MediationRecordLineWS{"
               + "id=" + id
               + ", orderLineId=" + orderLineId
               + ", eventDate=" + eventDate
               + ", amount=" + amount
               + ", quantity=" + quantity
               + ", description='" + description + '\''
               + '}';
    }
}
