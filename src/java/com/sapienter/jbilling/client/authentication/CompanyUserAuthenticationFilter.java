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

package com.sapienter.jbilling.client.authentication;

import com.sapienter.jbilling.client.authentication.util.SecuritySession;
import com.sapienter.jbilling.client.authentication.util.UsernameHelper;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An extension of the base spring security {@link UsernamePasswordAuthenticationFilter} that appends
 * the user entered company ID to the username for authentication.
 *
 * Similar to the {@link UsernamePasswordAuthenticationFilter}, the web form parameter names can be
 * configured via spring bean properties. 
 *
 * Default configuration:
 *      passwordParameter = "j_password"
 *      usernameParameter = "j_username"
 *      clientIdParameter = "j_client_id"
 *
 *
 *
 * @author Brian Cowdery
 * @since 04-10-2010
 */
public class CompanyUserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger LOG = Logger.getLogger(CompanyUserAuthenticationFilter.class);

    public static final String FORM_CLIENT_ID_KEY = "j_client_id";

    private String clientIdParameter;
    private SecuritySession securitySession;

    public final String getClientIdParameter() {
        return clientIdParameter == null ? FORM_CLIENT_ID_KEY : clientIdParameter;
    }

    public void setClientIdParameter(String clientIdParameter) {
        this.clientIdParameter = clientIdParameter;
    }

    public SecuritySession getSecuritySession() {
        return securitySession;
    }

    public void setSecuritySession(SecuritySession securitySession) {
        this.securitySession = securitySession;
    }

    /**
     * Returns the form submitted user name as colon delimited string containing
     * the user name and client id of the user to authenticate, e.g., "bob:1"
     *
     * @param request HTTP servlet request
     * @return username string
     */
    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String username = request.getParameter(getUsernameParameter());
        String companyId = request.getParameter(getClientIdParameter());

        return UsernameHelper.buildUsernameToken(username, companyId);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            Authentication result) throws IOException, ServletException {

        if (securitySession != null) {
            securitySession.setAttributes(request, response, (CompanyUserDetails) result.getPrincipal());
        }

        super.successfulAuthentication(request, response, result);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        LOG.debug("User " + failed.getAuthentication().getPrincipal() + " authentication failed!");

        if (securitySession != null) {
            securitySession.clearAttributes(request, response);
        }

        super.unsuccessfulAuthentication(request, response, failed);
    }
}
