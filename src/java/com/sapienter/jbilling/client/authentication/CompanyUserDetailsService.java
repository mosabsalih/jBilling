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

import com.sapienter.jbilling.client.authentication.util.UsernameHelper;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.permisson.db.PermissionDTO;
import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import grails.plugins.springsecurity.SpringSecurityService;
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of the GrailsUserDetailsService for use with the default DaoAuthenticationProvider. This
 * class fetches a user from the database and builds a list of granted authorities from the users assigned
 * permissions and roles.
 *
 * This must be used with the {@link CompanyUserAuthenticationFilter} to provide the company ID as part
 * of the username to load.
 *
 * @author Brian Cowdery
 * @since 04-10-2010
 */
public class CompanyUserDetailsService implements GrailsUserDetailsService {

    // empty list of roles for use if the given credentials don't resolve to a
    // usable UserDetails. Contains a single entry that does not grant any permissions.
    private static final List<GrantedAuthority> NO_AUTHORITIES;
    static {
        NO_AUTHORITIES = new ArrayList<GrantedAuthority>(1);
        NO_AUTHORITIES.add(new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE));
    }

    private SpringSecurityService springSecurityService;

    public SpringSecurityService getSpringSecurityService() {
        return springSecurityService;
    }

    public void setSpringSecurityService(SpringSecurityService springSecurityService) {
        this.springSecurityService = springSecurityService;
    }

    public UserDetails loadUserByUsername(String s, boolean loadRoles)
            throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(s);
    }

    /**
     * Loads the user account and all permissions/roles for the given user name. This method does not perform
     * authentication, only retrieves the {@link UserDetails} so that authentication can proceed.
     *
     * @param s username (principal) to retrieve
     * @return found user details
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException, DataAccessException {
        // get the user for the given name
        // CompanyUserAuthenticationFilter concatenates the user name with the entity id
        String[] tokens = s.split(UsernameHelper.VALUE_SEPARATOR);

        if (tokens.length < 2)
            throw new UsernameNotFoundException("Un-supported username '" + s + "', username must contain client id.");

        String username = tokens[0];
        Integer entityId = Integer.valueOf(tokens[1]);

        UserBL bl = new UserBL(username, entityId);
        UserDTO user = bl.getEntity();

        if (user == null)
            throw new UsernameNotFoundException("User '" + s + "' not found", username);

        // collect granted permissions and roles
        // this is a bad use of generics, the UserDetails signature should be <? extends GrantedAuthority>
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        for (PermissionDTO permission : bl.getPermissions()) {
            permission.initializeAuthority();
            authorities.add(permission);
        }
        
        for (RoleDTO role : user.getRoles()) {
            role.initializeAuthority();            
            authorities.add(role);
        }

        Integer mainRoleId = bl.getMainRole();

        // rebuild username token with company ID from retrieved user (just to be safe).
        String usernameToken = UsernameHelper.buildUsernameToken(user.getUserName(), user.getEntity().getId());
        
        // return user details for the retrieved account
        return new CompanyUserDetails(usernameToken, user.getPassword(), user.isEnabled(),
                                      !user.isAccountExpired(), !user.isPasswordExpired(), !user.isAccountLocked(),
                                      authorities.isEmpty() ? NO_AUTHORITIES : authorities,
                                      user, UserBL.getLocale(user), user.getId(), mainRoleId,
                                      user.getEntity().getId(), user.getCurrency().getId(), user.getLanguage().getId());
    }
}
