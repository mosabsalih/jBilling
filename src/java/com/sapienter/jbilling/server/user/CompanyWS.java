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
package com.sapienter.jbilling.server.user;


import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.LanguageDAS;

public class CompanyWS implements java.io.Serializable {


    private int id;
    private Integer currencyId;
    private Integer languageId;
    private String description;

    private ContactWS contact;
    
    public CompanyWS() {
    }

    public CompanyWS(int i) {
        id = i;
    }

    public CompanyWS(CompanyDTO companyDto) {
        this.id = companyDto.getId();
        this.currencyId= companyDto.getCurrencyId();
        this.languageId = companyDto.getLanguageId();
        this.description = companyDto.getDescription();

        ContactDTO contact = new EntityBL(new Integer(this.id)).getContact();

        if (contact != null) {
            this.contact = new ContactWS(contact.getId(),
                                         contact.getAddress1(),
                                         contact.getAddress2(),
                                         contact.getCity(),
                                         contact.getStateProvince(),
                                         contact.getPostalCode(),
                                         contact.getCountryCode(),
                                         contact.getDeleted());
        }
    }
    
    public CompanyDTO getDTO(){
        CompanyDTO dto = new CompanyDAS().find(new Integer(this.id));
        dto.setCurrency(new CurrencyDAS().find(this.currencyId));
        dto.setLanguage(new LanguageDAS().find(this.languageId));
        dto.setDescription(this.description);
        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ContactWS getContact() {
        return contact;
    }

    public void setContact(ContactWS contact) {
        this.contact = contact;
    }

    public String toString() {
        return "CompanyWS [id=" + id + ", currencyId=" + currencyId
                + ", languageId=" + languageId + ", description=" + description
                + ", contact=" + contact + "]";
    }

}