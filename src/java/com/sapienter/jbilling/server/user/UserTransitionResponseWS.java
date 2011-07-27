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

package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.server.security.WSSecured;

import java.io.Serializable;
import java.util.Date;

public class UserTransitionResponseWS implements WSSecured, Serializable {
    private Integer id;
    private Integer userId;
    private Date transitionDate;
    private Integer fromStatusId;
    private Integer toStatusId;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Date getTransitionDate() {
        return transitionDate;
    }
    public void setTransitionDate(Date transitionDate) {
        this.transitionDate = transitionDate;
    }
    public Integer getFromStatusId() {
        return fromStatusId;
    }
    public void setFromStatusId(Integer fromStatusId) {
        this.fromStatusId = fromStatusId;
    }
    public Integer getToStatusId() {
        return toStatusId;
    }
    public void setToStatusId(Integer toStatusId) {
        this.toStatusId = toStatusId;
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
        return "id = " + getId() + " user_id = " + getUserId() +
                " from_status_id = " + getFromStatusId() + " to_status_id = " + getToStatusId() +
                " transition_date = " + getTransitionDate().toString();
    }

}
