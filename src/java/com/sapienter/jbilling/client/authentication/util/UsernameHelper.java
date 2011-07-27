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

/**
 * Helper class to manage building username tokens for the {@link com.sapienter.jbilling.client.authentication.CompanyUserAuthenticationFilter} which
 * expects a username that also includes the client ID.
 *
 * @author Brian Cowdery
 * @since 24-11-2010
 */
public class UsernameHelper {

    public static final String VALUE_SEPARATOR = ";";

    
    public static String buildUsernameToken(String username, Integer companyId) {
        return buildUsernameToken(username, companyId.toString());
    }

    public static String buildUsernameToken(String username, String companyId) {
        StringBuilder token = new StringBuilder();
        token.append(username);
        token.append(VALUE_SEPARATOR);
        token.append(companyId);

        return token.toString();
    }
}
