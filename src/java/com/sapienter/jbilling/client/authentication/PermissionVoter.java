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

import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import org.codehaus.groovy.grails.plugins.springsecurity.WebExpressionConfigAttribute;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * A spring security {@link AccessDecisionVoter} implementation that handles individual user permission
 * access requests. User permission authorities are a uppercase string containing the permission type
 * and individual permission id (e.g., "WEB_SERVICES_120", "USER_CREATION_10").
 *
 * This voter abstains from un-supported access requests.
 *
 * @see com.sapienter.jbilling.server.user.UserBL#getPermissions() 
 * @see com.sapienter.jbilling.server.user.permisson.db.PermissionDTO#getAuthority()
 * 
 * @author Brian Cowdery
 * @since 04-10-2010
 */
public class PermissionVoter implements AccessDecisionVoter {

    protected static final String PERMISSION_ATTRIBUTE_REGEX = "[A-Z_]+\\d+";

    private String rolePrefix = RoleDTO.ROLE_AUTHORITY_PREFIX;

    /**
     * Supports access decisions for permission authorities, where the string does not
     * start with the "ROLE_" authority prefix.
     *
     * @param attribute authority to check
     * @return true if supported, false if not
     */
    public boolean supports(ConfigAttribute attribute) {
        String value = getAttributeValue(attribute);
        return value != null && !value.startsWith(getRolePrefix()) && value.matches(PERMISSION_ATTRIBUTE_REGEX);
    }

    public boolean supports(Class<?> clazz) {
        return true;
    }

    public int vote(Authentication authentication, Object o, Collection<ConfigAttribute> attributes) {
        int result = ACCESS_ABSTAIN; // abstain unless we find a supported attribute

        for (ConfigAttribute attribute : attributes) {
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                // grant access only if the user has an authority matching the request
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    if (getAttributeValue(attribute).equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        return result;
    }

    public String getAttributeValue(ConfigAttribute attribute) {
        if (attribute instanceof WebExpressionConfigAttribute) {
            return ((WebExpressionConfigAttribute) attribute).getAuthorizeExpression().getExpressionString();
        } else {
            return attribute.getAttribute();
        }
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }
}
