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

/*
 * Created on Oct 12, 2003
 *
 */
package com.sapienter.jbilling.server.process;

import java.util.Date;
import java.util.List;

import com.sapienter.jbilling.server.process.db.ProcessRunDTO;
import java.util.ArrayList;

/**
 * @author Emil
 */
public class BillingProcessRunDTOEx extends ProcessRunDTO {

    List<BillingProcessRunTotalDTOEx> totals = null;
    String statusStr;
    Integer usersSucceeded;
    Integer usersFailed;
    /**
     * 
     */
    public BillingProcessRunDTOEx() {
        super();
        totals = new ArrayList<BillingProcessRunTotalDTOEx>();
        setInvoicesGenerated(0);
    }

    /**
     * @param id
     * @param tryNumber
     * @param started
     * @param finished
     * @param invoiceGenerated
     * @param totalInvoiced
     * @param totalPaid
     * @param totalNotPaid
     */
    public BillingProcessRunDTOEx(Integer id, Date runDate, Date started,
            Date finished, Date paymentFinished, Integer invoiceGenerated) {
        setId(id);
        setRunDate(runDate);
        setStarted(started);
        setFinished(finished);
        setPaymentFinished(paymentFinished);
        setInvoicesGenerated(invoiceGenerated);
        
        totals = new ArrayList<BillingProcessRunTotalDTOEx>();
    }

    public List<BillingProcessRunTotalDTOEx> getTotals() {
        return totals;
    }

    public void setTotals(List<BillingProcessRunTotalDTOEx> totals) {
        this.totals = totals;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public Integer getUsersSucceeded() {
        return usersSucceeded;
    }

    public void setUsersSucceeded(Integer usersSucceeded) {
        this.usersSucceeded = usersSucceeded;
    }

    public Integer getUsersFailed() {
        return usersFailed;
    }

    public void setUsersFailed(Integer usersFailed) {
        this.usersFailed = usersFailed;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer(super.toString());
        ret.append(" totals: ");
        for (BillingProcessRunTotalDTOEx x : totals) {
            ret.append(x.toString());
        }

        return ret.toString();
    }
}
