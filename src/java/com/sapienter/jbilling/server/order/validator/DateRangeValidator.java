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

package com.sapienter.jbilling.server.order.validator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateRangeValidator
 *
 * @author Brian Cowdery
 * @since 26/01/11
 */
public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    private static final Logger LOG = Logger.getLogger(DateRangeValidator.class);

    // default java Date.toString() date format
    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");


    private String startDateFieldName;
    private String endDateFieldName;

    public void initialize(final DateRange dateRange) {
        startDateFieldName = dateRange.start();
        endDateFieldName = dateRange.end();
    }

    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        try {
            String startDateString = BeanUtils.getProperty(object, startDateFieldName);
            String endDateString = BeanUtils.getProperty(object, endDateFieldName);

            // only validate if both dates are present
            if (startDateString == null || startDateString.equals(""))
                return true;

            if (endDateString == null || endDateString.equals(""))
                return true;

            Date startDate = DEFAULT_DATE_FORMAT.parse(startDateString);
            Date endDate = DEFAULT_DATE_FORMAT.parse(endDateString);

            return startDate.before(endDate);

        } catch (IllegalAccessException e) {
            LOG.debug("Illegal access to the date range property fields.");
        } catch (NoSuchMethodException e) {
            LOG.debug("Date range property missing getter/setter methods.");
        } catch (InvocationTargetException e) {
            LOG.debug("Date property field cannot be accessed.");
        } catch (ParseException e) {
            LOG.debug("Date property values cannot be parsed.");
        }

        return false;
    }
}
