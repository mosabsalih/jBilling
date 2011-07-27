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

import java.math.BigDecimal;
import java.util.Hashtable;

import com.sapienter.jbilling.server.process.db.ProcessRunTotalDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

/**
 * @author Emil
 */
public class BillingProcessRunTotalDTOEx extends ProcessRunTotalDTO {

    private Hashtable pmTotals = null;
    private String currencyName = null;
    
    public BillingProcessRunTotalDTOEx() {
        super();
        pmTotals = new Hashtable();
    }

    public BillingProcessRunTotalDTOEx(Integer id, CurrencyDTO currency, BigDecimal totalInvoiced,
                                       BigDecimal totalPaid, BigDecimal totalNotPaid) {
        super((id == null ?  0 : id), null, currency, totalInvoiced, totalPaid, totalNotPaid);
        pmTotals = new Hashtable();
    }

    public Hashtable getPmTotals() {
        return pmTotals;
    }

    public void setPmTotals(Hashtable pmTotals) {
        this.pmTotals = pmTotals;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer(super.toString());
        ret.append(" currencyName: ")
                .append(currencyName)
                .append(" pmTotals ")
                .append(pmTotals);

        return ret.toString();
    }
}
