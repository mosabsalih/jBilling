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
 * Created on Oct 11, 2004
 *
 */
package com.sapienter.jbilling.server.pluggableTask;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;

/**
 * @author Emil
 *
 */
public class CalculateDueDateDfFm extends CalculateDueDate {
    public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException {
        // make the normal calculations first
        super.apply(invoice, null);
        // then get into the Df Fm: last day of the month
        if (invoice.getDueDatePeriod().getDf_fm() != null && 
                invoice.getDueDatePeriod().getDf_fm().booleanValue()) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(invoice.getDueDate());
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, lastDay);
            invoice.setDueDate(cal.getTime());
        }
    }
        
}
