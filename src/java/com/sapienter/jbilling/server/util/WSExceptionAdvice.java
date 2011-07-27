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

package com.sapienter.jbilling.server.util;

import org.apache.log4j.Logger;

import org.springframework.aop.ThrowsAdvice;

import com.sapienter.jbilling.common.SessionInternalError;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Re-throws any exceptions from the API as SessionInternalErrors to
 * prevent server exception classes being required on the client. 
 * Useful for remoting protocols such as Hessian which propagate the 
 * exception stack trace from the server to the client. 
 */
public class WSExceptionAdvice implements ThrowsAdvice {

    private static final Logger LOG = Logger.getLogger(WSExceptionAdvice.class);

    public void afterThrowing(Method method, Object[] args, Object target, Exception throwable) {
    	// Avoid catching automatic validation exceptions
    	if (throwable instanceof SessionInternalError) {
    		String messages[] = ((SessionInternalError)throwable).getErrorMessages();
    		if (messages != null && messages.length > 0) {
    			LOG.debug("Validation errors:" + Arrays.toString(messages));
    			return;
    		}
    	}
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.close();

        LOG.debug(throwable.getMessage() + "\n" + sw.toString());

        String message = "Error calling jBilling API. Method: " + method.getName();

        throw new SessionInternalError(message, throwable);
    }
}
