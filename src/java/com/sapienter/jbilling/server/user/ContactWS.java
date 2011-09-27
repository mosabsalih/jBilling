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
 * Created on Jan 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.util.api.validation.EntitySignupValidationGroup;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

/** @author Emil */
public class ContactWS implements Serializable {

    private Integer id;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String organizationName;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String address1;
    private String address2;
    private String city;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String stateProvince;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String postalCode;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String countryCode;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String lastName;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String firstName;
    private String initial;
    private String title;
    private Integer phoneCountryCode;
    private Integer phoneAreaCode;
    @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
    private String phoneNumber;
    private Integer faxCountryCode;
    private Integer faxAreaCode;
    private String faxNumber;
    @NotEmpty(message = "validation.error.notnull")
    @Email(message = "validation.error.email")
    private String email;
    private Date createDate;
    private int deleted;
    private Boolean include;

    private Integer[] fieldIDs = null;
    private String[] fieldNames = null;
    private String[] fieldValues = null;
    private Integer type = null; // the contact type

    private Integer contactTypeId = null;
    private String contactTypeDescr = null;

    public ContactWS() {
        super();
    }

    public ContactWS(Integer id, String address1,
            String address2, String city, String stateProvince,
            String postalCode, String countryCode, int deleted) {
            this.id = id;
            this.address1 = address1;
            this.address2 = address2;
            this.city = city;
            this.stateProvince = stateProvince;
            this.postalCode = postalCode;
            this.countryCode = countryCode;
            this.deleted = deleted;
        }
    
    public ContactWS(Integer id, String organizationName, String address1,
                     String address2, String city, String stateProvince,
                     String postalCode, String countryCode, String lastName,
                     String firstName, String initial, String title,
                     Integer phoneCountryCode, Integer phoneAreaCode,
                     String phoneNumber, Integer faxCountryCode, Integer faxAreaCode,
                     String faxNumber, String email, Date createDate, Integer deleted,
                     Boolean include) {
        this.id = id;
        this.organizationName = organizationName;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.lastName = lastName;
        this.firstName = firstName;
        this.initial = initial;
        this.title = title;
        this.phoneCountryCode = phoneCountryCode;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
        this.faxCountryCode = faxCountryCode;
        this.faxAreaCode = faxAreaCode;
        this.faxNumber = faxNumber;
        this.email = email;
        this.createDate = createDate;
        this.deleted = deleted;
        this.include = include;
    }

    public ContactWS(ContactWS other) {
        setId(other.getId());
        setOrganizationName(other.getOrganizationName());
        setAddress1(other.getAddress1());
        setAddress2(other.getAddress2());
        setCity(other.getCity());
        setStateProvince(other.getStateProvince());
        setPostalCode(other.getPostalCode());
        setCountryCode(other.getCountryCode());
        setLastName(other.getLastName());
        setFirstName(other.getFirstName());
        setInitial(other.getInitial());
        setTitle(other.getTitle());
        setPhoneCountryCode(other.getPhoneCountryCode());
        setPhoneAreaCode(other.getPhoneAreaCode());
        setPhoneNumber(other.getPhoneNumber());
        setFaxCountryCode(other.getFaxCountryCode());
        setFaxAreaCode(other.getFaxAreaCode());
        setFaxNumber(other.getFaxNumber());
        setEmail(other.getEmail());
        setCreateDate(other.getCreateDate());
        setDeleted(other.getDeleted());
        setInclude(other.getInclude());
    }

    public ContactWS(ContactDTOEx other) {
        setId(other.getId());
        setOrganizationName(other.getOrganizationName());
        setAddress1(other.getAddress1());
        setAddress2(other.getAddress2());
        setCity(other.getCity());
        setStateProvince(other.getStateProvince());
        setPostalCode(other.getPostalCode());
        setCountryCode(other.getCountryCode());
        setLastName(other.getLastName());
        setFirstName(other.getFirstName());
        setInitial(other.getInitial());
        setTitle(other.getTitle());
        setPhoneCountryCode(other.getPhoneCountryCode());
        setPhoneAreaCode(other.getPhoneAreaCode());
        setPhoneNumber(other.getPhoneNumber());
        setFaxCountryCode(other.getFaxCountryCode());
        setFaxAreaCode(other.getFaxAreaCode());
        setFaxNumber(other.getFaxNumber());
        setEmail(other.getEmail());
        setCreateDate(other.getCreateDate());
        setDeleted(other.getDeleted());
        setInclude(other.getInclude() != null && other.getInclude().equals(1) );
        setType(other.getType());
        fieldIDs = new Integer[other.getFieldsTable().size()];
        fieldNames = new String[other.getFieldsTable().size()];
        fieldValues = new String[other.getFieldsTable().size()];
        int index = 0;

        for (Iterator it = other.getFieldsTable().keySet().iterator(); it.hasNext();) {
            fieldIDs[index] = new Integer((String) it.next());
            ContactFieldDTO fieldDto = (ContactFieldDTO) other.getFieldsTable().get(fieldIDs[index].toString());
            fieldNames[index] = fieldDto.getType().getPromptKey();
            fieldValues[index] = fieldDto.getContent();
            index++;
        }

        //set Contact Type Name
        if (null != other.getContactMap() && null != other.getContactMap().getContactType()) {
            setContactTypeId(other.getContactMap().getContactType().getId());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(Integer phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public Integer getPhoneAreaCode() {
        return phoneAreaCode;
    }

    public void setPhoneAreaCode(Integer phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getFaxCountryCode() {
        return faxCountryCode;
    }

    public void setFaxCountryCode(Integer faxCountryCode) {
        this.faxCountryCode = faxCountryCode;
    }

    public Integer getFaxAreaCode() {
        return faxAreaCode;
    }

    public void setFaxAreaCode(Integer faxAreaCode) {
        this.faxAreaCode = faxAreaCode;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public Boolean getInclude() {
        return include == null ? new Boolean(false) : include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }

    public Integer[] getFieldIDs() {
        return fieldIDs;
    }

    public void setFieldIDs(Integer[] fieldIDs) {
        this.fieldIDs = fieldIDs;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String[] getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String[] fieldValues) {
        this.fieldValues = fieldValues;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getContactTypeId() {
        return contactTypeId;
    }

    public void setContactTypeId(Integer contactTypeId) {
        this.contactTypeId = contactTypeId;
    }

    public String getContactTypeDescr() {
        return contactTypeDescr;
    }

    public void setContactTypeDescr(String contactTypeDescr) {
        this.contactTypeDescr = contactTypeDescr;
    }

    @Override
    public String toString() {
        return "ContactWS{"
               + "id=" + id
               + ", type=" + type
               + ", title='" + title + '\''
               + ", lastName='" + lastName + '\''
               + ", firstName='" + firstName + '\''
               + ", initial='" + initial + '\''
               + ", organization='" + organizationName + '\''
               + ", address1='" + address1 + '\''
               + ", address2='" + address2 + '\''
               + ", city='" + city + '\''
               + ", stateProvince='" + stateProvince + '\''
               + ", postalCode='" + postalCode + '\''
               + ", countryCode='" + countryCode + '\''
               + ", phone='" + (phoneCountryCode != null ? phoneCountryCode : "")
                             + (phoneAreaCode != null ? phoneAreaCode : "")
                             + (phoneNumber != null ?  phoneNumber : "") + '\''
               + ", fax='" + (faxCountryCode != null ? faxCountryCode : "")
                           + (faxAreaCode != null ? faxAreaCode : "")
                           + (faxNumber != null ? faxNumber : "") + '\''
               + ", email='" + email + '\''
               + ", type='" + type + '\''
               + ", include='" + include + '\''
               + '}';
    }
}
