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

package com.sapienter.jbilling.server.item.tasks;

import com.sapienter.jbilling.server.rule.Result;
import java.math.BigDecimal;
import org.apache.log4j.Logger;

/**
 *
 * @author emilc
 */
public class PricingResult extends Result {

    private static final Logger LOG = Logger.getLogger(PricingResult.class);

    private final Integer itemId;
    private final Integer userId;
    private final Integer currencyId;
    private BigDecimal price;
    private BigDecimal quantity;
    private long pricingFieldsResultId;

    public PricingResult(Integer itemId, Integer userId, Integer currencyId) {
        this.itemId = itemId;
        this.userId = userId;
        this.currencyId = currencyId;
    }

    public PricingResult(Integer itemId, BigDecimal quantity, Integer userId, Integer currencyId) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.userId = userId;
        this.currencyId = currencyId;
    }    

    public Integer getCurrencyId() {
        return currencyId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        LOG.debug("Setting price. Result fields id " + pricingFieldsResultId + " item " + itemId + " price " + price );
        this.price = price;
    }

    public void setPrice(String price) {
        setPrice(new BigDecimal(price));
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public long getPricingFieldsResultId() {
        return pricingFieldsResultId;
    }

    public void setPricingFieldsResultId(long pricingFieldsResultId) {
        this.pricingFieldsResultId = pricingFieldsResultId;
    }

    public String toString() {
        return  "PricingResult:" +
                "itemId=" + itemId + " " +
                "userId=" + userId + " " +
                "currencyId=" + currencyId + " " +
                "price=" + price + " " +
                "pricing fields result id=" + pricingFieldsResultId + " " +
                super.toString();
    }

}
