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

package com.sapienter.jbilling.server.payment.blacklist;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDAS;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Util;

/** 
 * Processes blacklist CSV files.
 * It either adds to or replaces any existing CSV created blacklist entries.
 */
public class CsvProcessor {
    /**
     * ParseException thrown when unexpected or missing data encountered.
     */
    public static class ParseException extends RuntimeException {
        private int lineNum;
        private Column column;

        public ParseException(String message, int lineNum, Column column) {
            super(message);
            this.lineNum = lineNum;
            this.column = column;
        }

        public int getLineNum() {
            return lineNum;
        }

        public Column getColumn() {
            return column;
        }
    }

    // the columns and their order
    public enum Column { TYPE, FIRST_NAME, LAST_NAME, ADDRESS_1, ADDRESS_2, CITY,
            STATE_PROVINCE, POSTAL_CODE, COUNTRY_CODE, PHONE_COUNTRY_CODE,
            PHONE_AREA_CODE, PHONE_NUMBER, IP_ADDRESS, CC_NUMBER, USER_ID }

    private static final int NUM_OF_COLUMNS = 15;
    private static final char FIELD_SEPARATOR = ',';
    private static final String R_BUNDLE_KEY = "payment.blacklist.csv.";

    private static Logger LOG = Logger.getLogger(CsvProcessor.class);

    // some frequently used data classes
    private BlacklistDAS blacklistDAS = null;
    private UserDAS userDAS = null;
    private ContactDAS contactDAS = null;
    private CreditCardDAS creditCardDAS = null;
    private ContactFieldTypeDTO ipAddressFieldTypeDTO = null;
    private ResourceBundle rBundle = null;

    // current line and line number
    private String[] currentLine = null;
    private int lineNum = 0;

    public CsvProcessor() {
        blacklistDAS = new BlacklistDAS();
        userDAS = new UserDAS();
        contactDAS = new ContactDAS();
        creditCardDAS = new CreditCardDAS();
    }

    /**
     * Does the processing.
     * Reads the blacklist CSV file specified by filePath.
     * It will either add to or replace the existing uploaded 
     * blacklist for the given entity (company). Returns the number
     * of new blacklist entries created.
     */
    public int process(String filePath, boolean replace, Integer entityId) 
            throws ParseException {
        BufferedReader inFile = null;
        String inLine = null;
        lineNum = 0;
        int entriesAdded = 0;

        try {
            inFile = new BufferedReader(new FileReader(filePath));

            CompanyDTO company = new CompanyDAS().find(entityId);
            ipAddressFieldTypeDTO = new ContactFieldTypeDAS().find(
                BlacklistBL.getIpAddressCcfId(entityId));

            EntityBL entity = new EntityBL(entityId);
            Locale locale = entity.getLocale();
            rBundle = ResourceBundle.getBundle("entityNotifications", 
                    locale);

            // first, delete any existing entries, if required
            if (replace) {
                int number = blacklistDAS.deleteSource(entityId,
                        BlacklistDTO.SOURCE_EXTERNAL_UPLOAD);
                LOG.debug("Deleted " + number + " externally uploaded " +
                        "blacklist entries");
            }

            // loop through each line, determine its type and 
            // create an appropriate blacklist entry
            while ((inLine = inFile.readLine()) != null) {
                lineNum++;

                // skip blank lines
                if (inLine.equals("")) {
                    continue;
                }

                currentLine = Util.csvSplitLine(inLine, FIELD_SEPARATOR);

                // check for correct number of columns
                if (currentLine.length != NUM_OF_COLUMNS) {
                    throw new ParseException(getMessage("columns", lineNum, 
                            NUM_OF_COLUMNS, currentLine.length), lineNum, null);
                }

                Integer type = getInt(Column.TYPE);
                BlacklistDTO entry = new BlacklistDTO();

                if (type.equals(BlacklistDTO.TYPE_USER_ID)) {
                    createUserRecord(entry);
                } else if (type.equals(BlacklistDTO.TYPE_NAME)) {
                    createNameRecord(entry);
                } else if (type.equals(BlacklistDTO.TYPE_CC_NUMBER)) {
                    createCcRecord(entry);
                } else if (type.equals(BlacklistDTO.TYPE_ADDRESS)) {
                    createAddressRecord(entry);
                } else if (type.equals(BlacklistDTO.TYPE_IP_ADDRESS)) {
                    createIpAddressRecord(entry);
                } else if (type.equals(BlacklistDTO.TYPE_PHONE_NUMBER)) {
                    createPhoneRecord(entry);
                } else {
                    throw new ParseException(lineColMessage(Column.TYPE, 
                            "invalid_type", type), lineNum, Column.TYPE);
                }

                entry.setType(type);
                entry.setSource(BlacklistDTO.SOURCE_EXTERNAL_UPLOAD);
                entry.setCompany(company);
                entry.setCreateDate(new Date());
                blacklistDAS.save(entry);
                entriesAdded++;
            }
        } catch (ParseException pe) {
            throw pe;
        } catch (Exception e) {
            throw new SessionInternalError("Error while processing", 
                    CsvProcessor.class, e);
        } finally {
            try {
                if (inFile != null) {
                    inFile.close();
                }
            } catch (IOException ioe) {}
        }

        LOG.debug("Added " + entriesAdded + " blacklist entries");
        return entriesAdded;
    }


    /**
     * Creates a user blacklist entry.
     */
    private void createUserRecord(BlacklistDTO entry) throws ParseException {
        Integer userId = getInt(Column.USER_ID);
        if (userId == null) {
            throw new ParseException(lineColMessage(Column.USER_ID, 
                    "empty_user_id"), lineNum, Column.USER_ID);
        }

        // try to get user
        UserDTO user = userDAS.findNow(userId);
        if (user == null) {
            throw new ParseException(lineColMessage(Column.USER_ID, 
                    "invalid_user_id", userId), lineNum, Column.USER_ID);
        }

        entry.setUser(user);
    }

    /**
     * Creates a name blacklist entry.
     */
    private void createNameRecord(BlacklistDTO entry) throws ParseException {
        checkForEmptyRecord("NAME", Column.FIRST_NAME, Column.LAST_NAME);

        ContactDTO newContact = new ContactDTO();
        newContact.setCreateDate(new Date());
        newContact.setDeleted(0);
        newContact.setFirstName(getString(Column.FIRST_NAME));
        newContact.setLastName(getString(Column.LAST_NAME));

        entry.setContact(newContact);
    }

    /**
     * Creates an address blacklist entry.
     */
    private void createAddressRecord(BlacklistDTO entry) throws ParseException {
        checkForEmptyRecord("ADDRESS", Column.ADDRESS_1, Column.ADDRESS_2, Column.CITY,
                Column.STATE_PROVINCE, Column.POSTAL_CODE, Column.COUNTRY_CODE);

        ContactDTO newContact = new ContactDTO();
        newContact.setCreateDate(new Date());
        newContact.setDeleted(0);
        newContact.setAddress1(getString(Column.ADDRESS_1));
        newContact.setAddress2(getString(Column.ADDRESS_2));
        newContact.setCity(getString(Column.CITY));
        newContact.setStateProvince(getString(Column.STATE_PROVINCE));
        newContact.setPostalCode(getString(Column.POSTAL_CODE));
        newContact.setCountryCode(getString(Column.COUNTRY_CODE));

        entry.setContact(newContact);
    }

    /**
     * Creates a phone blacklist entry.
     */
    private void createPhoneRecord(BlacklistDTO entry) throws ParseException {
        checkForEmptyRecord("PHONE_NUMBER", Column.PHONE_COUNTRY_CODE, 
                Column.PHONE_AREA_CODE, Column.PHONE_NUMBER);

        ContactDTO newContact = new ContactDTO();
        newContact.setCreateDate(new Date());
        newContact.setDeleted(0);
        newContact.setPhoneCountryCode(getInt(Column.PHONE_COUNTRY_CODE));
        newContact.setPhoneAreaCode(getInt(Column.PHONE_AREA_CODE));
        newContact.setPhoneNumber(getString(Column.PHONE_NUMBER));

        entry.setContact(newContact);
    }

    /**
     * Creates a credit card number blacklist entry.
     */
    private void createCcRecord(BlacklistDTO entry) throws ParseException {
        checkForEmptyRecord("CC_NUMBER", Column.CC_NUMBER);

        CreditCardDTO creditCard = new CreditCardDTO();

        creditCard.setNumber(getString(Column.CC_NUMBER));
        creditCard.setDeleted(0);
        creditCard.setCcType(2); // not null
        creditCard.setCcExpiry(new Date()); // not null

        entry.setCreditCard(creditCard);
    }

    /**
     * Creates an ip address blacklist entry.
     */
    private void createIpAddressRecord(BlacklistDTO entry) throws ParseException {
        checkForEmptyRecord("IP_ADDRESS", Column.IP_ADDRESS);

        ContactDTO newContact = new ContactDTO();
        newContact.setCreateDate(new Date());
        newContact.setDeleted(0);
        ContactFieldDTO newField = new ContactFieldDTO();
        newField.setType(ipAddressFieldTypeDTO);
        newField.setContent(getString(Column.IP_ADDRESS));
        newField.setContact(newContact);

        Set<ContactFieldDTO> fields = new HashSet<ContactFieldDTO>(1);
        fields.add(newField);
        newContact.setFields(fields);

        entry.setContact(newContact);
    }

    /**
     * Returns the data in the specified column as an Integer.
     */
    private Integer getInt(Column column) throws ParseException {
        String field = getString(column);
        if (field == null) {
            return null;
        }

        Integer integer = null;
        try {
            integer = new Integer(field);
        } catch (NumberFormatException nfe) {
            throw new ParseException(lineColMessage(column, "get_int", field), 
                    lineNum, column);
        }

        return integer;
    }

    /**
     * If the specified column contains an empty string,
     * returns null, otherwise returns the string.
     */
    private String getString(Column column) {
        if (currentLine[column.ordinal()].equals("")) {
            return null;
        } else {
            return currentLine[column.ordinal()];
        }
    }

    /**
     * Checks that all the given Columns aren't empty.
     */
    private void checkForEmptyRecord(String type, Column... columns) 
            throws ParseException {
        for (Column column : columns) {
            if (!currentLine[column.ordinal()].equals("")) {
                return;
            }
        }
        throw new ParseException(getMessage("empty_record", lineNum, type),
                lineNum, null);
    }

    /**
     * Returns current location concatenated with the resource bundle message -
     * useful for error messages.
     */
    private String lineColMessage(Column column, String messageKey, 
            Object... messageParams) {
        return getMessage("location", lineNum, column) + 
                getMessage(messageKey, messageParams);
    }

    /**
     * Gets the international error message for the given key. 
     * Also inserts the parameters into the string.
     */
    private String getMessage(String key, Object... params) {
        String message = rBundle.getString(R_BUNDLE_KEY + key);
        for (Object param : params) {
            message = message.replaceFirst("\\|X\\|", param.toString());
        }
        return message;
    }
}
