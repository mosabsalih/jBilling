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

import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO;
import com.sapienter.jbilling.server.util.InternationalDescriptionWS;
import com.sapienter.jbilling.server.util.db.LanguageDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactTypeWS
 *
 * @author Brian Cowdery
 * @since 27/01/11
 */
public class ContactTypeWS {

    private Integer id;
    private Integer companyId;
    private Integer isPrimary;
    private List<InternationalDescriptionWS> descriptions = new ArrayList<InternationalDescriptionWS>();

    public ContactTypeWS() {
    }

    public ContactTypeWS(ContactTypeDTO contactType, List<LanguageDTO> languages) {
        this.id = contactType.getId();
        this.isPrimary = contactType.getIsPrimary();

        if (contactType.getEntity() != null)
            this.companyId = contactType.getEntity().getId();

        for (LanguageDTO language : languages) {
            descriptions.add(new InternationalDescriptionWS(contactType.getDescriptionDTO(language.getId())));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Integer primary) {
        isPrimary = primary;
    }

    public List<InternationalDescriptionWS> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<InternationalDescriptionWS> descriptions) {
        this.descriptions = descriptions;
    }

    public InternationalDescriptionWS getDescription(Integer languageId) {
        for (InternationalDescriptionWS description : descriptions)
            if (description.getLanguageId().equals(languageId))
                return description;
        return null;
    }

    @Override
    public String toString() {
        return "ContactTypeWS{"
               + "descriptions=" + descriptions
               + ", id=" + id
               + ", companyId=" + companyId
               + ", isPrimary=" + isPrimary
               + '}';
    }
}
