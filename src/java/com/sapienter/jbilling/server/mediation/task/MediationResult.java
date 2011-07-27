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
package com.sapienter.jbilling.server.mediation.task;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.rule.Result;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author emilc
 */
public class MediationResult extends Result {

    private static final Logger LOG = Logger.getLogger(MediationResult.class);

    public static final int STEP_1_START = 1000;
    public static final int STEP_2_AFTER_USER = 2000;
    public static final int STEP_3_CURRENT_ORDER = 3000;
    public static final int STEP_4_RESOLVE_ITEM = 4000;
    public static final int STEP_5_PRICING = 5000;
    public static final int STEP_6_ITEM_MANAGEMENT = 6000;
    public static final int STEP_7_DIFF = 7000;

    // the lines that where 'created' by the mediation process
    private List<OrderLineDTO> lines = null;

    // the difference lines of the current orders, comparing the original lines
    private List<OrderLineDTO> diffLines = null;
    // the original lines in the current order before the new lines were applied
    private List<OrderLineDTO> oldLines = null;
    private String recordKey = null;
    private OrderDTO currentOrder = null;
    private Integer userId = null;
    private Integer currencyId = null;
    private final String configurationName;
    private Date eventDate = null;
    private String description = null;
    private boolean persist = false; // whether changes are allowed to the DB
    // custom errors go here
    private List<String> errors = new ArrayList<String>(0);

    // the mediation step
    private int step = STEP_1_START;

    public MediationResult(String configurationName, boolean persist) {
        this.configurationName = configurationName;
        lines = new ArrayList<OrderLineDTO>();
        this.persist = persist;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public List<OrderLineDTO> getLines() {
        return lines;
    }

    public String getDescription() {
        return description;
    }

    public OrderDTO getCurrentOrder() {
        return currentOrder;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addLine(Integer itemId, Integer quantity) {
        addLine(itemId, new BigDecimal(quantity));
    }

    public void addLine(Integer itemId, Double quantity) {
        addLine(itemId, new BigDecimal(quantity).setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
    }

    public void addLine(Integer itemId, BigDecimal quantity) {
        OrderLineDTO line = new OrderLineDTO();
        line.setItemId(itemId);
        line.setQuantity(quantity);
        line.setDefaults();
        lines.add(line);
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public void setEventDate(Date date) {
        eventDate = date;
    }

    public void setEventDate(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        try {
            eventDate = dateFormat.parse(date);
        } catch (ParseException e) {
            eventDate = null;
            LOG.warn("Exception parsing a string date to set the event date", e);
        }
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setCurrentOrder(OrderDTO currentOrder) {
        this.currentOrder = currentOrder;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<OrderLineDTO> getDiffLines() {
        return diffLines;
    }

    public void setDiffLines(List<OrderLineDTO> diffLines) {
        this.diffLines = diffLines;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public boolean getPersist() {
        return persist;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public List<OrderLineDTO> getOldLines() {
        return oldLines;
    }

    public void setOldLines(List<OrderLineDTO> oldLines) {
        this.oldLines = oldLines;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        LOG.debug("Now from step " + this.step + " to step " + step + " id " + getId() + " record " + getRecordKey());
        this.step = step;
    }
}

