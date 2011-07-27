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

package com.sapienter.jbilling.server.pluggableTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;

/**
 * This simple task gets the days to add to the invoice date from the 
 * billing process configuration. It doesn't get into any other consideration,
 * like business days, etc ...
 * @author Emil
 */
public class CalculateDueDate
    extends PluggableTask
    implements InvoiceCompositionTask {

    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.InvoiceCompositionTask#apply(com.sapienter.betty.server.invoice.NewInvoiceDTO)
     */
    public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException {
        // set up
        Date generated = invoice.getBillingDate();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(generated);
        Logger.getLogger(CalculateDueDate.class).debug(
                "Calculating due date from " + cal.getTime());
        
        // get the days configures
        try {
            // add the period of time
            cal.add(MapPeriodToCalendar.map(invoice.getDueDatePeriod().getUnitId()), 
                    invoice.getDueDatePeriod().getValue().intValue());
            // set the due date
            invoice.setDueDate(cal.getTime());
        } catch (Exception e) {
            Logger.getLogger(CalculateDueDate.class).error("Exception:", e);
            throw new TaskException(e);
        }
       
    }
    
    public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
        throw new UnsupportedOperationException("Can't call this method");
    }


}
