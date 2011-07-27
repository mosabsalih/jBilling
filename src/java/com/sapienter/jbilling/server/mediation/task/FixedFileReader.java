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

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.mediation.FormatField;

import java.util.ArrayList;
import java.util.List;

public class FixedFileReader extends AbstractFileReader {

    private static final String BLANK = "";

    @Override
    protected String[] splitFields(String line) {

        List<String> fields = new ArrayList<String>();

        for (FormatField formatField : format.getFields()) {
            if (formatField.getStartPosition() == null
                || formatField.getLength() == null
                || formatField.getStartPosition() <= 0
                || formatField.getLength() <= 0) {

                throw new SessionInternalError("Position and length must be positive integers: '" + formatField + "'");
            }

            int start = formatField.getStartPosition() - 1;
            int end = start + formatField.getLength();

            // field end exceeds line length
            if (end > line.length())
                end = line.length();
                                    
            // parse field, or return a blank string if field start exceeds line length
            fields.add(start > line.length() ? BLANK : line.substring(start, end));
        }

        return fields.toArray(new String[fields.size()]);        
    }
}
