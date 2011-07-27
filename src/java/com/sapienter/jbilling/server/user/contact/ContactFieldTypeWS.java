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
package com.sapienter.jbilling.server.user.contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.InternationalDescriptionWS;

public class ContactFieldTypeWS  implements java.io.Serializable {


	private Integer id;
	private Integer companyId;
	private String promptKey;
	private String dataType;
	private Integer readOnly;
	private List<InternationalDescriptionWS> descriptions = new ArrayList<InternationalDescriptionWS>();

    public ContactFieldTypeWS() {
    	
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

	public String getPromptKey() {
		return promptKey;
	}

	public void setPromptKey(String promptKey) {
		this.promptKey = promptKey;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Integer readOnly) {
		this.readOnly = readOnly;
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
		return "ContactFieldTypeWS [id=" + id + ", companyId=" + companyId
				+ ", promptKey=" + promptKey + ", dataType=" + dataType
				+ ", readOnly=" + readOnly + ", descriptions=" + descriptions + "]";
	}
	
	public ContactFieldTypeDTO getDTO() { 
		
		ContactFieldTypeDTO dto= this.id == null ? new ContactFieldTypeDTO() : new ContactFieldTypeDAS().find(this.id);
		if ( null != dto ) { 
			dto.setDataType(this.dataType);
			dto.setEntity(new CompanyDTO(this.companyId));
			dto.setReadOnly(this.readOnly);
			dto.setVersionNum(0);
			//since Prompt key is not null
			dto.setPromptKey("placeholder_text");
			if (this.descriptions != null && this.descriptions.size() > 0 ) {
				dto.setDescription(((InternationalDescriptionWS)this.descriptions.get(0)).getContent(), ((InternationalDescriptionWS)this.descriptions.get(0)).getLanguageId());
			}
		}
		return dto;
	}
	
}


