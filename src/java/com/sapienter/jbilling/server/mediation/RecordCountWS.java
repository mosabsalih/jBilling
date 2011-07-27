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

import java.io.Serializable;

/**
 * Web-service compatible representation of the value map returned by
 * {@link IMediationSessionBean#getMediationRecordsByMediationProcess(Integer)}.
 *
 * This class is necessary as Apache CXF (JAXB) does not handle Maps and a custom JAXB binding
 * might not be supported by SOAP clients.
 *
 * @author Brian Cowdery
 * @since 25-10-2010
 */
public class RecordCountWS implements Serializable {

    private Integer statusId;
    private Long count;

    public RecordCountWS() {
    }

    public RecordCountWS(Integer statusId, Long count) {
        this.statusId = statusId;
        this.count = count;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RecordCountWS{"
               + "statusId=" + statusId
               + ", count=" + count
               + '}';
    }
}
