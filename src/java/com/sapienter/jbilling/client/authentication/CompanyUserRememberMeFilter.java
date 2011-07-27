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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Remember me authentication filter extended to set session attributes from the logged
 * in user on successful authentication. 
 *
 * @author Brian Cowdery
 * @since 25-11-2010
 */
public class CompanyUserRememberMeFilter extends RememberMeAuthenticationFilter {

    private SecuritySession securitySession;

    public SecuritySession getSecuritySession() {
        return securitySession;
    }

    public void setSecuritySession(SecuritySession securitySession) {
        this.securitySession = securitySession;
    }

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              Authentication result) {

        if (securitySession != null) {
            securitySession.setAttributes(request, response, (CompanyUserDetails) result.getPrincipal());
        }

        super.onSuccessfulAuthentication(request, response, result);
    }

    @Override
    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException failed) {

        if (securitySession != null) {
            securitySession.clearAttributes(request, response);
        }

        super.onUnsuccessfulAuthentication(request, response, failed);
    }
}
