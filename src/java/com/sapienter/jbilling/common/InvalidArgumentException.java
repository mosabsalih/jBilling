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
package com.sapienter.jbilling.common;

import org.apache.log4j.Logger;

public class InvalidArgumentException extends RuntimeException {
    private final Integer code;
    private final Exception e;
    private static final Logger LOG = Logger.getLogger(InvalidArgumentException.class);
    
    public InvalidArgumentException(String message, Integer code, Exception e) {
        super(message);
        this.code = code;
        this.e = e;
        LOG.debug(message + ((e == null) ? "" : " - " + e.getMessage()));
    }

    public InvalidArgumentException(String message, Integer code) {
        this(message, code, null);
    }

    public InvalidArgumentException(InvalidArgumentException e) {
        this(e.getMessage(), e.getCode(), e.getException());
    }
    
    public Integer getCode() {
        return code;
    }
    public Exception getException() {
        return e;
    }
}
