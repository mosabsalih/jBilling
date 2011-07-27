
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

import java.util.List;

import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import com.sapienter.jbilling.server.user.db.UserStatusDTO;
import com.sapienter.jbilling.server.user.db.SubscriberStatusDTO;
import com.sapienter.jbilling.server.process.db.PeriodUnitDTO;

class SelectionTagLib {
	
	def accountType = { attrs, bodyy -> 
		println System.currentTimeMillis();
		String checking, savings;
		savings=checking= ""
		println "taglib: accountType=" + attrs.value + " " + attrs.name
		
		if (attrs.value == 1 ) {
			checking= "checked=checked" 
		} else if (attrs.value == 2 ){
			savings="checked=checked"
		}
		
		out << "Checking<input type='radio' name=\'" + attrs.name + "\' value=\'1\' " + checking + ">"
		out << "Savings<input type='radio' name=\'"  + attrs.name + "\' value=\'2\' "  + savings + ">"
		
	}
	
	def selectRoles = { attrs, body ->
		
		Integer langId= attrs.languageId?.toInteger();
		String name= attrs.name;		
		String value = attrs.value?.toString()
		
		List list= new ArrayList();
		String[] sarr= null;
		for (RoleDTO role: RoleDTO.list()) {
			String title= role.getTitle(langId);
			sarr=new String[2]
			sarr[0]= role.getId()
			sarr[1]= title
			list.add(sarr)
		}
		out << render(template:"/selectTag", model:[name:name, list:list, value:value])
	}

	def userStatus = { attrs, body ->
		
		Integer langId= attrs.languageId?.toInteger();
		String name= attrs.name;
		String value = attrs.value?.toString()

		List list= new ArrayList();
		String[] sarr= null;
		for (UserStatusDTO status: UserStatusDTO.list()) {
			String title= status.getDescription(langId);			
			sarr=new String[2]
			sarr[0]= status.getId()
			sarr[1]= title
			list.add(sarr)
		}
		out << render(template:"/selectTag", model:[name:name, list:list, value:value])
		
	}
	
	def subscriberStatus = { attrs, body ->
		
		Integer langId= attrs.languageId?.toInteger();
		String name= attrs.name;
		String value = attrs.value?.toString()
        String cssClass = attrs.cssClass?.toString()

		log.info "Value of tagName=" + name + " is " + value
		
		List list= new ArrayList();
		String[] sarr= null;
		for (SubscriberStatusDTO status: SubscriberStatusDTO.list()) {
			String title= status.getDescription(langId);
			sarr=new String[2]
			sarr[0]= status.getId()
			sarr[1]= title
			list.add(sarr)
		}
		
		out << render(template:"/selectTag", model:[name:name, list:list, value:value, cssClass: cssClass])
	}

	def periodUnit = { attrs, body ->
		
		Integer langId= attrs.languageId?.toInteger();
		String name= attrs.name;
		String value = attrs.value?.toString()

		log.info "Value of tagName=" + name + " is " + value
		
		List list= new ArrayList();
		String[] sarr= null;
		for (PeriodUnitDTO period: PeriodUnitDTO.list()) {
			String title= period.getDescription(langId);
			sarr=new String[2]
			sarr[0]= period.getId()
			sarr[1]= title
			list.add(sarr)
		}
		
		out << render(template:"/selectTag", model:[name:name, list:list, value:value])
	}
	
}