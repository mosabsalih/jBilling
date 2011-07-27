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

package com.sapienter.jbilling.server.item;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemPriceDTOEx implements Serializable {

    // ItemPriceDTO
    private Integer id;
    private String price;
    private Integer currencyId;

    // ItemPriceDTOEx
    private String name = null;
    // this is useful for the form, exposing a Float is trouble
    private String priceForm = null;

    public ItemPriceDTOEx() {
    }

    public ItemPriceDTOEx(Integer id, BigDecimal price, Integer currencyId) {
        this.id = id;
        this.price = price != null ? price.toString() : null;
        this.currencyId = currencyId;
    }

    public ItemPriceDTOEx(ItemPriceDTOEx otherValue) {
        this.id = otherValue.id;
        this.price = otherValue.price;
        this.currencyId = otherValue.currencyId;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrice() {
        return this.price;
    }

    public BigDecimal getPriceAsDecimal() {
        return price != null ? new BigDecimal(price) : null;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPrice(BigDecimal price) {
        this.price = (price != null ? price.toString() : null);
    }

    public Integer getCurrencyId() {
        return this.currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriceForm() {
        return priceForm;
    }

    public void setPriceForm(String priceForm) {
        this.priceForm = priceForm;
    }

    public String itemPriceDtoToString() {
        StringBuffer str = new StringBuffer("{");
        str.append("id=" + getId() + " " + "price=" + getPrice() + " " + "currencyId=" + getCurrencyId());
        str.append('}');

        return (str.toString());
    }

    public boolean isIdentical(Object other) {
        if (other instanceof ItemPriceDTOEx) {
            ItemPriceDTOEx that = (ItemPriceDTOEx) other;
            boolean lEquals = true;
            if (this.price == null) {
                lEquals = lEquals && (that.price == null);
            } else {
                lEquals = lEquals && this.price.equals(that.price);
            }
            if (this.currencyId == null) {
                lEquals = lEquals && (that.currencyId == null);
            } else {
                lEquals = lEquals && this.currencyId.equals(that.currencyId);
            }

            return lEquals;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof ItemPriceDTOEx) {
            ItemPriceDTOEx that = (ItemPriceDTOEx) other;
            boolean lEquals = true;
            if (this.id == null) {
                lEquals = lEquals && (that.id == null);
            } else {
                lEquals = lEquals && this.id.equals(that.id);
            }

            lEquals = lEquals && isIdentical(that);

            return lEquals;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ((this.id != null) ? this.id.hashCode() : 0);
        result = 37 * result + ((this.price != null) ? this.price.hashCode() : 0);
        result = 37 * result + ((this.currencyId != null) ? this.currencyId.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "name = " + name + " priceForm = " + priceForm + itemPriceDtoToString();
    }
}
