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
package com.sapienter.jbilling.server.mediation;

public class FormatField {
    private String name;
    private String type;
    private Integer startPosition;
    private Integer length;
    private boolean isKey;
    private String durationFormat;
    
    public void isKeyTrue() {
        this.isKey = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FormatField() {
        isKey = false;
    }
    
    public boolean getIsKey() {
        return isKey;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    
    public String toString() {
        return "name: " + name + " type: " + type + " isKey: " + isKey + 
                " startPosition " + startPosition + " length " + length;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = Integer.valueOf(length);
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = Integer.valueOf(startPosition);
    }

    public String getDurationFormat() {
        return durationFormat;
    }

    public void setDurationFormat(String durationFormat) {
        this.durationFormat = durationFormat;
    }
}
