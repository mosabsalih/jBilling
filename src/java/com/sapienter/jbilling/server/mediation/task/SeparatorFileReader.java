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

package com.sapienter.jbilling.server.mediation.task;

import java.util.ArrayList;
import java.util.List;

import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import org.apache.commons.lang.StringUtils;

public class SeparatorFileReader extends AbstractFileReader {
    
    private String fieldSeparator;

    public SeparatorFileReader() {
    }
    
    public static final ParameterDescription PARAMETER_SEPARATOR = 
    	new ParameterDescription("separator", false, ParameterDescription.Type.STR);
    
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_SEPARATOR);
    }

    
    
    @Override
    public boolean validate(List<String> messages) {
        boolean retValue = super.validate(messages); 
        
        // optionals
        fieldSeparator = (StringUtils.isBlank(parameters.get(PARAMETER_SEPARATOR.getName()))
                          ? "," : parameters.get(PARAMETER_SEPARATOR.getName()));
       
        return retValue;
    }
    
    @Override
    protected String[] splitFields(String line) {
        return line.split(fieldSeparator, -1);
    }
}
