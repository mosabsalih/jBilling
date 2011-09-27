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
 * Created on Sep 16, 2004
 *
 */
package com.sapienter.jbilling.server.user;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;

/**
 * @author Emil
 */
public class ContactDTOEx extends ContactDTO implements Serializable  {
    
    private Hashtable<String, ContactFieldDTO> fieldsTable = null; // the entity specific fields
    private Integer type = null; // the contact type

    /**
     * 
     */
    public ContactDTOEx() {
        super();
    }

    /**
     * @param id
     * @param organizationName
     * @param address1
     * @param address2
     * @param city
     * @param stateProvince
     * @param postalCode
     * @param countryCode
     * @param lastName
     * @param firstName
     * @param initial
     * @param title
     * @param phoneCountryCode
     * @param phoneAreaCode
     * @param phoneNumber
     * @param faxCountryCode
     * @param faxAreaCode
     * @param faxNumber
     * @param email
     * @param createDate
     * @param deleted
     */
    public ContactDTOEx(Integer id, String organizationName, String address1,
            String address2, String city, String stateProvince,
            String postalCode, String countryCode, String lastName,
            String firstName, String initial, String title,
            Integer phoneCountryCode, Integer phoneAreaCode,
            String phoneNumber, Integer faxCountryCode, Integer faxAreaCode,
            String faxNumber, String email, Date createDate, Integer deleted,
            Integer notify) {
        super(id,organizationName, address1, address2, city, stateProvince, postalCode, countryCode, 
                lastName, firstName, initial, title, phoneCountryCode, phoneAreaCode, 
                phoneNumber, faxCountryCode, faxAreaCode, faxNumber, email, createDate, deleted, 
                notify, null, null, null);
    }

    /**
     * @param otherValue
     */
    public ContactDTOEx(ContactDTO otherValue) {
        super(otherValue);
    }
    
    public ContactDTOEx(ContactWS ws) {
        setId(ws.getId());
        setOrganizationName(ws.getOrganizationName());
        setAddress1(ws.getAddress1());
        setAddress2(ws.getAddress2());
        setCity(ws.getCity());
        setStateProvince(ws.getStateProvince());
        setPostalCode(ws.getPostalCode());
        setCountryCode(ws.getCountryCode());
        setLastName(ws.getLastName());
        setFirstName(ws.getFirstName());
        setInitial(ws.getInitial());
        setTitle(ws.getTitle());
        setPhoneCountryCode(ws.getPhoneCountryCode());
        setPhoneAreaCode(ws.getPhoneAreaCode());
        setPhoneNumber(ws.getPhoneNumber());
        setFaxCountryCode(ws.getFaxCountryCode());
        setFaxAreaCode(ws.getFaxAreaCode());
        setFaxNumber(ws.getFaxNumber());
        setEmail(ws.getEmail());
        setCreateDate(ws.getCreateDate());
        setDeleted(ws.getDeleted());
        setInclude( ( ws.getInclude() ? 1 : 0 ) );

        // contacts from ws are always included in notifications
        //setInclude(new Integer(1));

        // now add the custom fields
        if (ws.getFieldIDs() == null || ws.getFieldIDs().length == 0) {
            return;
        }

        fieldsTable = new Hashtable<String, ContactFieldDTO>();
        for (int f = 0; f < ws.getFieldIDs().length; f++) {
            fieldsTable.put(String.valueOf(ws.getFieldIDs()[f]),
                            new ContactFieldDTO(0, new ContactFieldTypeDTO(ws.getFieldIDs()[f]), null, ws.getFieldValues()[f]));
        }
    }

    public Hashtable<String, ContactFieldDTO> getFieldsTable() {
        return fieldsTable;
    }
    public void setFieldsTable(Hashtable<String, ContactFieldDTO> fields) {
        this.fieldsTable = fields;
    }
    public Integer getType() {
        return type;
}
    public void setType(Integer type) {
        this.type = type;
    }
}
