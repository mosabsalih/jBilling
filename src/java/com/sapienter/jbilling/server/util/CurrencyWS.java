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

package com.sapienter.jbilling.server.util;

import com.sapienter.jbilling.server.util.db.CurrencyDTO;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CurrencyWS
 *
 * @author Brian Cowdery
 * @since 07/04/11
 */
public class CurrencyWS implements Serializable {

    private Integer id;
    private String description;
    private String symbol;
    private String code;
    private String countryCode;
    private Boolean inUse;
    private String rate;
    private String sysRate;

    private boolean defaultCurrency;

    public CurrencyWS() {
    }

    public CurrencyWS(CurrencyDTO dto, boolean defaultCurrency) {
        this.id = dto.getId();
        this.description = dto.getDescription();
        this.symbol = dto.getSymbol();
        this.code = dto.getCode();
        this.countryCode = dto.getCountryCode();
        this.inUse = dto.getInUse();

        setRate(dto.getRate());
        setSysRate(dto.getSysRate());

        this.defaultCurrency = defaultCurrency;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    public String getRate() {
        return rate;
    }

    public BigDecimal getRateAsDecimal() {
        return rate != null ? new BigDecimal(rate) : null;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = (rate != null ? rate.toString() : null);
    }

    public void setRateAsDecimal(BigDecimal rate) {
        setRate(rate);
    }

    public String getSysRate() {
        return sysRate;
    }

    public BigDecimal getSysRateAsDecimal() {
        return sysRate != null ? new BigDecimal(sysRate) : null;
    }

    public void setSysRate(String sysRate) {
        this.sysRate = sysRate;
    }

    public void setSysRate(BigDecimal systemRate) {
        this.sysRate = (systemRate != null ? systemRate.toString() : null);
    }

    public void setSysRateAsDecimal(BigDecimal systemRate) {
        setSysRate(systemRate);
    }

    public boolean isDefaultCurrency() {
        return defaultCurrency;
    }

    public boolean getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(boolean defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public String toString() {
        return "CurrencyWS{"
               + "id=" + id
               + ", symbol='" + symbol + '\''
               + ", code='" + code + '\''
               + ", countryCode='" + countryCode + '\''
               + ", inUse=" + inUse
               + ", rate='" + rate + '\''
               + ", systemRate='" + sysRate + '\''
               + ", isDefaultCurrency=" + defaultCurrency
               + '}';
    }
}
