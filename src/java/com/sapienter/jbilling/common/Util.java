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

package com.sapienter.jbilling.common;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Client miscelaneous utility functions
 */
public class Util {
    private static final Logger LOG = Logger.getLogger(Util.class);
    /**
     * Creates a date object with the given parameters only if they belong to a
     * valid day, so February 30th would be returning null.
     * @param year
     * @param month
     * @param day
     * @return null if the parameters are invalid, otherwise the date object
     */
    static public Date getDate(Integer year, Integer month, 
            Integer day) {
        Date retValue = null;
        
        try {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setLenient(false);
            cal.clear();
            cal.set(year.intValue(), month.intValue()-1, day.intValue());
        
            retValue = cal.getTime();
        } catch (Exception e) {
            
        }
        return retValue;
    }
    
    /**
     * Converts a string in the format yyyy-mm-dd to a Date.
     * If the string can't be converted, it returns null
     * @param str
     * @return
     */
    static public Date parseDate(String str) {
        if (str == null || str.length() < 8 || str.length() > 10) {
            return null;
        }
        
        if (str.charAt(4) != '-' || str.lastIndexOf('-') < 6 || 
                str.lastIndexOf('-') > 7) {
            return null;
        }
        
        try {
            int year = getYear(str);
            int month = getMonth(str);
            int day = getDay(str);
        
            return getDate(new Integer(year), new Integer(month),
                    new Integer(day));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Recives date in sql format yyyy-mm-dd and extracts the day
     * @param day
     * @return
     */
    static public int getDay(String str) 
            throws SessionInternalError {
        // from the last '-' to the end
        try {
            return Integer.valueOf(str.substring(str.lastIndexOf('-') + 1)).
                    intValue();
        } catch (NumberFormatException e) {
            throw new SessionInternalError("Cant get the day from " + str);
        }
    }

    static public int getMonth(String str) 
            throws SessionInternalError {
        // from the first '-' to the second '-'
        try {
            return Integer.valueOf(str.substring(str.indexOf('-') + 1, 
                    str.lastIndexOf('-'))).intValue();
        } catch (NumberFormatException e) {
            throw new SessionInternalError("Cant get the month from " + str);
        }
                    
    }

    static public int getYear(String str) 
            throws SessionInternalError {
        // from the begining to the first '-'
        try {
            return Integer.valueOf(str.substring(0, str.indexOf('-'))).intValue();
        } catch (NumberFormatException e) {
            throw new SessionInternalError("Cant get the year from " + str);
        }
    }
    
    /**
     * Compares to dates, contemplating the posibility of null values.
     * If both are null, they are consider equal.
     * @param date1
     * @param date2
     * @return true if equal, otherwise false. 
     */
   
    static public boolean equal(Date date1, Date date2) {
        boolean retValue;
        if (date1 == null && date2 == null) {
            retValue = true;
        } else if ((date1 == null && date2 != null) ||
                (date1 != null && date2 == null)) {
            retValue = false;
        } else {
            retValue = (date1.compareTo(date2) == 0);
        }
        
        return retValue;
    }

    static public Date truncateDate(Date arg) {
        if (arg == null) return null;
        GregorianCalendar cal = new GregorianCalendar();
        
        cal.setTime(arg);
        cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
        cal.set(GregorianCalendar.MINUTE, 0);
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        
        return cal.getTime();
    }

    /**
     * Takes a date and returns it as String with the format 'yyyy-mm-dd'
     * @param date
     * @return
     */
    static public String parseDate(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        
        cal.setTime(date);
        return cal.get(GregorianCalendar.YEAR) + "-" +
                (cal.get(GregorianCalendar.MONTH) + 1) + "-" +
                cal.get(GregorianCalendar.DATE); 
    }

    /**
     * Returns the payment method for the given credit card. If this credit card
     * has been obscured (by the {@link com.sapienter.jbilling.server.payment.tasks.SaveCreditCardExternallyTask} plug-in)
     * then the payment type cannot detected and this method will return PAYMENT_METHOD_GATEWAY_KEY.
     *
     * @param creditCardNumber credit card number to parse
     * @return payment method
     */
    static public Integer getPaymentMethod(String creditCardNumber) {
        Integer type = null;

        switch (creditCardNumber.charAt(0)) {
        case '4':
            type = Constants.PAYMENT_METHOD_VISA;
            break;
        case '5':
            type = Constants.PAYMENT_METHOD_MASTERCARD;
            break;
        case '3':
            // both diners and american express start with a 3
            if (creditCardNumber.charAt(1) == '7') {
                type = Constants.PAYMENT_METHOD_AMEX;
            } else if (creditCardNumber.charAt(1) == '8') {
                type = Constants.PAYMENT_METHOD_DINERS;
            }
            break;
        case '6':
            type = Constants.PAYMENT_METHOD_DISCOVERY;
            break;
        }

        /*
            This isn't 100% accurate as obscured credit card numbers may not always mean that a gateway key
            is present. We should be checking CreditCardDTO to ensure that gatewayKey is not null when an
            obscured credit card number is encountered.
         */
        if (creditCardNumber.contains("*"))
            type = Constants.PAYMENT_METHOD_GATEWAY_KEY;

        return type;
    }

    static public String truncateString(String str, int length) {
        if (str == null) return null;
        String retValue;
        if (str.length() <= length) {
            retValue = str;
        } else {
            retValue = str.substring(0, length);
        }
        
        return retValue;
    }

    public static String getSysProp(String key) { 
        try {
            return SystemProperties.getSystemProperties().get(key);
        } catch (Exception e) {
            LOG.error("Cannot read property '" + key + "' from " + SystemProperties.getPropertiesFile().getPath(), e);
            return null;
        }
    }

    /**
     * Gets a boolean system property. It returns true by default, and on any error.
     *
     * @param key boolean system property
     * @return boolean property value
     */
    public static boolean getSysPropBooleanTrue(String key) {
        try {
            return Boolean.parseBoolean(SystemProperties.getSystemProperties().get(key, "true"));
        } catch (Exception e) {
            LOG.error("Cannot read property '" + key + "' from " + SystemProperties.getPropertiesFile().getPath());
        }

        return true; // default if not found
    }
    
    /**
     * Credit Card Validate
     * Reference: http://www.ling.nwu.edu/~sburke/pub/luhn_lib.pl
     */
    public static boolean luhnCheck(String cardNumber) throws SessionInternalError {
        //just in case the card number is formated and may contain spaces
        cardNumber=getDigitsOnly(cardNumber);
        //mod 10 validation
        if (isLuhnNum(cardNumber)) {
            int no_digit = cardNumber.length();
            int oddoeven = no_digit & 1;
            
            int sum = 0;
            int digit = 0;
            int addend = 0;
            boolean timesTwo = false;
            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                digit = Integer.parseInt(cardNumber.substring(i, i + 1));
                if (timesTwo) {
                    addend = digit * 2;
                    if (addend > 9) {
                        addend -= 9;
                    }
                } else {
                    addend = digit;
                }
                sum += addend;
                timesTwo = !timesTwo;
            }
            if (sum == 0) return false;
            if (sum % 10 == 0) return true;
        };
        return false;
    }
    
    private static String getDigitsOnly(String s) {
        StringBuffer digitsOnly = new StringBuffer();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (Character.isDigit(c)) {
                digitsOnly.append(c);
            }
        }
        return digitsOnly.toString();
    }
    
    private static boolean isLuhnNum(String argvalue) {
        if (argvalue.length() == 0) {
            return false;
        }
        for (int n = 0; n < argvalue.length(); n++) {
            char c = argvalue.charAt(n);
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

}
