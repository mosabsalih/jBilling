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

package com.sapienter.jbilling.server.process;

import com.sapienter.jbilling.server.process.db.AgeingEntityStepDTO;


/**
 * @author Emil
 */
public class AgeingDTOEx extends AgeingEntityStepDTO {
    private Integer statusId = null;
    private String statusStr = null;
    private String welcomeMessage = null;
    private String failedLoginMessage = null;
    private Boolean inUse = null;
    private Integer canLogin = null;
    
    /**
     * @return
     */
    public String getFailedLoginMessage() {
        return failedLoginMessage;
    }

    /**
     * @param failedLoginMessage
     */
    public void setFailedLoginMessage(String failedLoginMessage) {
        this.failedLoginMessage = failedLoginMessage;
    }

    /**
     * @return
     */
    public Integer getStatusId() {
        return statusId;
    }

    /**
     * @param statusId
     */
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    /**
     * @return
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * @param welcomeMessage
     */
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    /**
     * @return
     */
    public Boolean getInUse() {
        return inUse;
    }

    /**
     * @param inUse
     */
    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * @return
     */
    public String getStatusStr() {
        return statusStr;
    }

    /**
     * @param statusStr
     */
    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    /**
     * @return
     */
    public Integer getCanLogin() {
        return canLogin;
    }

    /**
     * @param canLogin
     */
    public void setCanLogin(Integer canLogin) {
        this.canLogin = canLogin;
    }

	public String toString() {
		return "AgeingDTOEx [statusId=" + statusId + ", statusStr=" + statusStr
				+ ", welcomeMessage=" + welcomeMessage
				+ ", failedLoginMessage=" + failedLoginMessage + ", inUse="
				+ inUse + "]";
	}

}
