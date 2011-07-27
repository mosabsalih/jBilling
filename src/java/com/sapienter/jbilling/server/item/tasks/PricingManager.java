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

import com.sapienter.jbilling.client.util.Constants;
import java.math.BigDecimal;
import org.apache.log4j.Logger;

/**
 *
 * @author emilc
 */
public class PricingManager {

    private static final Logger LOG = Logger.getLogger(PricingManager.class);
    private final Integer itemId;
    private final Integer userId;
    private final Integer currencyId;
    private BigDecimal price; // it is all about setting the value of this field ...

    public PricingManager(Integer itemId, Integer userId,
            Integer currencyId, BigDecimal price) {
        this.itemId = itemId;
        this.userId = userId;
        this.currencyId = currencyId;
        setPrice(price);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(double defaultPrice) {
        LOG.debug("Setting price of item " + itemId + " to " + defaultPrice);
        this.price = new BigDecimal(defaultPrice);
    }

    public void setPrice(BigDecimal defaultPrice) {
        this.price = defaultPrice;
    }

    public void setPrice(int price) {
        setPrice((double) price);
    }

    public void setByPercentage(double percentage) {
        this.price = price.add(price.divide(new BigDecimal(100), Constants.BIGDECIMAL_SCALE,
                Constants.BIGDECIMAL_ROUND).multiply(new BigDecimal(percentage)));
    }

    public void setByPercentage(int percentage) {
        setByPercentage((double) percentage);
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

    public String toString() {
        return "PricingManages=currencyId: " + currencyId + " itemId: " + itemId +
                " price " + price + " userId " + userId;
    }
}
