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

package com.sapienter.jbilling.client.authentication.util;

import com.sapienter.jbilling.client.authentication.CompanyUserDetails;
import grails.plugins.springsecurity.Secured;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Helper class to handle the export of user attributes as session attributes.
 *
 * @author Brian Cowdery
 * @since 25-11-2010
 */
public class SecuritySession {

    private static final Logger LOG = Logger.getLogger(SecuritySession.class);

    public static final String USER_ID = "user_id";
    public static final String USER_MAIN_ROLE_ID = "main_role_id";
    public static final String USER_LANGUAGE_ID = "language_id";
    public static final String USER_CURRENCY_ID = "currency_id";
    public static final String USER_COMPANY_ID = "company_id";
    public static final String USER_LOCALE = "locale";

    private LocaleResolver localeResolver;

    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public SecuritySession() {
    }

    /**
     * Sets common attributes of the logged in user as session attributes.
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param principal logged in user
     */
    public void setAttributes(HttpServletRequest request, HttpServletResponse response, CompanyUserDetails principal) {
        HttpSession session = request.getSession();

        session.setAttribute(USER_ID, principal.getUserId());
        session.setAttribute(USER_MAIN_ROLE_ID, principal.getMainRoleId());
        session.setAttribute(USER_LANGUAGE_ID, principal.getLanguageId());
        session.setAttribute(USER_CURRENCY_ID, principal.getCurrencyId());
        session.setAttribute(USER_COMPANY_ID, principal.getCompanyId());
        session.setAttribute(USER_LOCALE, principal.getLocale());

        // set locale for spring/grails
        if (localeResolver != null) {
            LOG.debug("Setting locale for Spring contexts: " + principal.getLocale());
            localeResolver.setLocale(request, response, principal.getLocale());

        } else {
            LOG.warn("Locale resolver not set or not available, cannot set user locale for Spring contexts!");
        }
    }

    /**
     * Clears the security session variables from the current HttpSession. This should be
     * done whenever an un-successful login attempt is made to ensure that no session attributes
     * leak over when switching users or performing complex authentication steps.
     *
     * @param request http servlet request
     * @param response http servlet response
     */
    public void clearAttributes(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        session.removeAttribute(USER_ID);
        session.removeAttribute(USER_MAIN_ROLE_ID);
        session.removeAttribute(USER_LANGUAGE_ID);
        session.removeAttribute(USER_CURRENCY_ID);
        session.removeAttribute(USER_COMPANY_ID);
        session.removeAttribute(USER_LOCALE);
    }
}
