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

package com.sapienter.jbilling.server.process.task;

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.process.BusinessDays;
import com.sapienter.jbilling.server.process.db.AgeingEntityStepDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * BusinessDayAgeingTask
 *
 * @author Brian Cowdery
 * @since 29/04/11
 */
public class BusinessDayAgeingTask extends BasicAgeingTask {
    private static final Logger LOG = Logger.getLogger(BusinessDayAgeingTask.class);

    private static final String PARAM_HOLIDAY_FILE = "holiday_file";
    private static final String PARAM_DATE_FORMAT = "date_format";

    private BusinessDays businessDays;

    private BusinessDays getBusinessDaysHelper() {
        if (businessDays == null) {
            String dateFormat = getParameter(PARAM_DATE_FORMAT, "yyyy-MM-dd");
            String holidayFile = getParameter(PARAM_HOLIDAY_FILE, (String) null);

            if (holidayFile != null) {
                holidayFile = Util.getSysProp("base_dir") + File.separator + holidayFile;
            }

            businessDays = new BusinessDays(new File(holidayFile), new SimpleDateFormat(dateFormat));
        }

        return businessDays;
    }

    @Override
    public boolean isInvoiceOverdue(InvoiceDTO invoice, UserDTO user, Integer gracePeriod, Date today) {

        Date dueDate = getBusinessDaysHelper().addBusinessDays(invoice.getDueDate(), gracePeriod);

        // invoice due date + grace period as week days
        if (dueDate.before(today)) {
            LOG.debug("Invoice is overdue (due date " + invoice.getDueDate() + " + "
                      + gracePeriod + " days grace, is before today " + today + ")");
            return true;
        }

        LOG.debug("Invoice is NOT overdue (due date " + invoice.getDueDate() + " + "
                  + gracePeriod + " days grace is after today " + today + ")");
        return false;
    }

    @Override
    public boolean isAgeingRequired(UserDTO user, AgeingEntityStepDTO currentStep, Date today) {
        Date lastStatusChange = user.getLastStatusChange() != null
                                ? user.getLastStatusChange()
                                : user.getCreateDatetime();

        Date expiryDate = getBusinessDaysHelper().addBusinessDays(lastStatusChange, currentStep.getDays());

        // last status change + step days as week days
        if (expiryDate.equals(today) || expiryDate.before(today)) {
            LOG.debug("User status has expired (last change " + lastStatusChange + " + "
                      + currentStep.getDays() + " days is before today " + today + ")");
            return true;
        }

        LOG.debug("User does not need to be aged (last change " + lastStatusChange + " + "
                  + currentStep.getDays() + " days is after today " + today + ")");
        return false;
    }
}
