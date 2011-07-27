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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

public class SessionInternalError extends RuntimeException {

	private String errorMessages[] = null;
	
    public SessionInternalError() {
    }

    public SessionInternalError(String s) {
        super(s);
    }
    
    public SessionInternalError(String s, Class className, Exception e) {
        super(s);
        Logger log = Logger.getLogger(className);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();

        log.fatal(s + e.getMessage() + "\n" + sw.toString());
        
    }

    public SessionInternalError(Exception e) {
        super(e.getMessage());

        if (e instanceof SessionInternalError) {
            setErrorMessages(((SessionInternalError) e).getErrorMessages());
        }

        Logger log = Logger.getLogger("com.sapienter.jbilling");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        log.fatal("Internal error: " + e.getMessage() + "\n" + sw.toString());
    }

    public SessionInternalError(String message, Throwable e) {
        super(message + " Cause: " + e.getMessage(), e);
    }

    public SessionInternalError(String message, Throwable e, String[] errors) {
        super(message, e);
        setErrorMessages(errors);
    }

    public SessionInternalError(String message, String[] errors) {
        super(message);
        setErrorMessages(errors);
    }

	public void setErrorMessages(String errors[]) {
		this.errorMessages = errors;
	}

	public String[] getErrorMessages() {
		return errorMessages;
	}
}
