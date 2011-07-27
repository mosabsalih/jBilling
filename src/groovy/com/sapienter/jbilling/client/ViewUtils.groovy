
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

package com.sapienter.jbilling.client

import java.util.List;

import org.codehaus.groovy.grails.web.servlet.GrailsFlashScope 
import org.hibernate.StaleObjectStateException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource 
import com.sapienter.jbilling.common.SessionInternalError;

class ViewUtils {
	// thanks Groovy for adding the setters and getters for me
	ReloadableResourceBundleMessageSource messageSource;
	
	/**
	 * Will add to flas.errorMessages a list of string with each error message, if any.
	 * @param flash
	 * @param locale
	 * @param exception
	 * @return
	 * true if there are validation errors, otherwise false
	 */
	boolean resolveException(flash, Locale locale, Exception exception) {
		List<String> messages = new ArrayList<String>();
		if (exception instanceof SessionInternalError && exception.getErrorMessages()?.length > 0) {
			for (String message : exception.getErrorMessages()) {
				List<String> fields = message.split(",");
				String type = messageSource.getMessage("bean." + fields[0], null, locale);
				String property = messageSource.getMessage("bean." + fields[0] + "." + fields[1], null, locale);
				List restOfFields = null;
				if (fields.size() >= 4) {
					restOfFields = fields[3..fields.size()-1];
				}
				String errorMessage = messageSource.getMessage(fields[2], restOfFields as Object[] , locale);
				String finalMessage = messageSource.getMessage("validation.message", 
					[type, property, errorMessage] as Object[], locale);
				messages.add finalMessage;
			}
			flash.errorMessages = messages;
			return true;
		} else if (exception.getCause() instanceof StaleObjectStateException) {
            // this is two people trying to update the same data
            StaleObjectStateException ex = exception.getCause();
            flash.error = messageSource.getMessage("error.dobule_update", null, locale);
        } else {
            // generic error
            flash.error = messageSource.getMessage("error.exception", [exception.getCause().getMessage()] as Object[], locale);
        }
        
		return false;
	}
	
	
	
}
