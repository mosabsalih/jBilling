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

import com.sapienter.jbilling.common.SessionInternalError;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utilities for calculating dates and dealing with business days (aka, banking days).
 *
 * @author Vikas Bodani, Brian Cowdery
 * @since 29/04/11
 */
public class BusinessDays {
    private static final Logger LOG = Logger.getLogger(BusinessDays.class);

    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private List<Date> holidays = new ArrayList<Date>();
    private Calendar start = GregorianCalendar.getInstance();
    private Calendar end = GregorianCalendar.getInstance();

    public BusinessDays() {
    }

    public BusinessDays(List<Date> holidays) {
        this.holidays = holidays;
    }

    public BusinessDays(File holidayFile) {
        loadHolidayFile(holidayFile);
    }

    public BusinessDays(File holidayFile, DateFormat dateformat) {
        loadHolidayFile(holidayFile, dateformat);
    }

    /**
     * Loads an external file containing the dates of holidays. These days will be handled
     * as non-business days when calculating business dates.
     *
     * This method assumes a default date format of "yyyy-MM-dd".
     *
     * @param file files to load
     */
    public void loadHolidayFile(File file) {
        loadHolidayFile(file, DEFAULT_DATE_FORMAT);
    }

    /**
     * Loads an external file containing the dates of holidays. These days will be handled
     * as non-business days when calculating business dates.
     *
     * @param file files to load
     * @param dateFormat date format of dates
     */
    public void loadHolidayFile(File file, DateFormat dateFormat) {
        holidays.clear();

        if (file != null && file.exists()) {
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(file));

                String line = null;
                while ((line = in.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        try {
                            Date holiday = dateFormat.parse(line);
                            holidays.add(holiday);
                        } catch (ParseException e) {
                            LOG.warn("Invalid holiday date, or wrong date format - ignoring entry '" + line + "'");
                        }
                    }
                }

            } catch (IOException e) {
                LOG.warn("Holiday file " + file.getPath() + " could not be read.");

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    /* ignore */
                }
            }
        } else {
            LOG.warn("Holiday file not set or does not exist.");
        }
    }

    /**
     * Convert a non-business day range and calculate an appropriate end date that falls on
     * a business day. If the original date range contains a weekend day or holiday, then the
     * resulting end date will be incremented as to exclude it.
     *
     * @param startDate starting date
     * @param endDate ending date
     * @return new end date for a business-day date range
     */
    public Date calculateEndDate(Date startDate, Date endDate) {
        start.setTime(startDate);
        end.setTime(endDate);

        while (start.before(end)) {
            if (start.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || start.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                end.add(Calendar.DATE, 1);
            } else {
                for (Date holiday : holidays) {
                    if (isEqual(holiday, start.getTime())) {
                        end.add(Calendar.DATE, 1);
                    }
                }
            }
            start.add(Calendar.DATE, 1);
        }

        return end.getTime();
    }

    /**
     * Adds the given number of business days to the starting date.
     *
     * @param startDate initial start date to add days to
     * @param days business days to add
     * @return date with business days added
     */
    public Date addBusinessDays(Date startDate, int days) {
        start.setTime(startDate);

        int nonBusinessDays = 0;
        for (int i = 0; i < days; i++) {
            start.add(Calendar.DATE, 1);

            if (start.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || start.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                nonBusinessDays++;
            }

            for (Date holiday : holidays) {
                if (isEqual(holiday, start.getTime())) {
                    nonBusinessDays++;
                }
            }
        }

        start.add(Calendar.DATE,  nonBusinessDays);
        return start.getTime();
    }

    private boolean isEqual(Date startDate, Date endDate) {
        return new DateMidnight(startDate).equals(new DateMidnight(endDate));
    }
}
