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

package com.sapienter.jbilling.server.user.partner;

import com.sapienter.jbilling.server.user.partner.db.PartnerRange;

import java.io.Serializable;

/**
 * PartnerRangeWS
 *
 * @author Brian Cowdery
 * @since 25-10-2010
 */
public class PartnerRangeWS implements Serializable {

    private Integer id;
    private Integer partnerId;
    private Double percentageRate;
    private Double referralFee;
    private Integer rangeFrom;
    private Integer rangeTo;

    public PartnerRangeWS() {
    }

    public PartnerRangeWS(PartnerRange dto) {
        this.id = dto.getId();
        this.partnerId = dto.getPartner() != null ? dto.getPartner().getId() : null;
        this.percentageRate = dto.getPercentageRate();
        this.referralFee = dto.getReferralFee();
        this.rangeFrom = dto.getRangeFrom();
        this.rangeTo = dto.getRangeTo();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Double getPercentageRate() {
        return percentageRate;
    }

    public void setPercentageRate(Double percentageRate) {
        this.percentageRate = percentageRate;
    }

    public Double getReferralFee() {
        return referralFee;
    }

    public void setReferralFee(Double referralFee) {
        this.referralFee = referralFee;
    }

    public Integer getRangeFrom() {
        return rangeFrom;
    }

    public void setRangeFrom(Integer rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public Integer getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(Integer rangeTo) {
        this.rangeTo = rangeTo;
    }

    @Override
    public String toString() {
        return "PartnerRangeWS{"
               + "id=" + id
               + ", partnerId=" + partnerId
               + ", percentageRate=" + percentageRate
               + ", referralFee=" + referralFee
               + ", rangeFrom=" + rangeFrom
               + ", rangeTo=" + rangeTo
               + '}';
    }
}
