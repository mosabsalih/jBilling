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

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security authorization filter that uses a bean defined username and password for authentication
 * instead of client provided credentials.
 *
 * This is mostly used to authenticate web-service beans using protocols that lack the ability to
 * authenticate themselves, but it may also be used statically define the authentication of a particular
 * URL removing the need to pass credentials.
 *
 * It's a good idea to use this authentication filter with an additional IP address filter for security.
 *
 * Example configuration:
 *
 * resources.groovy
 * <code>
 *     staticAuthenticationProcessingFilter(com.sapienter.jbilling.client.authentication.StaticAuthenticationFilter) {
 *         authenticationManager = ref("authenticationManager")
 *         authenticationDetailsSource = ref('authenticationDetailsSource')
 *         username = "admin;1"
 *         password = "123qwe"
 *     }
 * </code>
 *
 * Config.groovy
 * <code>
 *     grails.plugins.springsecurity.filterChain.chainMap = [
 *         '/httpinvoker/**': 'securityContextPersistenceFilter,staticAuthenticationProcessingFilter,securityContextHolderAwareRequestFilter,basicExceptionTranslationFilter,filterInvocationInterceptor',
 *         '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
 *     ]
 * </code>
 *
 * @author Brian Cowdery
 * @since 20-10-2010
 */
public class StaticAuthenticationFilter extends GenericFilterBean {

    // dependency injection from spring
    private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private AuthenticationEntryPoint authenticationEntryPoint = new HttpAuthenticationEntryPoint();
    private AuthenticationManager authenticationManager;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private String username;
    private String password;

    public AuthenticationDetailsSource getAuthenticationDetailsSource() {
        return authenticationDetailsSource;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return authenticationEntryPoint;
    }

    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public RememberMeServices getRememberMeServices() {
        return rememberMeServices;
    }

    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Perform authentication using the configured username and password.
     *
     * This filter does not allow anonymous authentication. If a user is already logged in anonymously
     * (if the filter chain puts anonymous authentication before this filter), the token will be removed
     * and a real authentication attempt will be made.
     *
     * @param req servlet request
     * @param res servlet response
     * @param chain filter chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        String username = getUsername() != null ? getUsername().trim() : "";
        String password = getPassword() != null ? getPassword() : "";

        if (isAuthenticationRequired(username)) {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            authRequest.setDetails(getAuthenticationDetailsSource().buildDetails(request));

            Authentication authResult;
            try {
                // run authentication using the configured auth manager
                authResult = getAuthenticationManager().authenticate(authRequest);

            } catch (AuthenticationException failed) {
                // authentication failed
                SecurityContextHolder.getContext().setAuthentication(null);
                getRememberMeServices().loginFail(request, response);
                getAuthenticationEntryPoint().commence(request, response, failed);

                return;
            }

            // authentication successful
            SecurityContextHolder.getContext().setAuthentication(authResult);
            getRememberMeServices().loginSuccess(request, response, authResult);
        }

        chain.doFilter(request, response);
    }

    /**
     * Returns true if the username is not already logged in, and that the existing
     * authorization (if present) is accepted by this filter.
     *
     * This method also returns true if the the user is currently logged in anonymously.
     *
     * @param username username to check
     * @return true if authentication required, false if already logged in or authentication type cannot be handled
     */
    private boolean isAuthenticationRequired(String username) {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        // re-authenticate if username doesn't match SecurityContextHolder and user isn't authenticated
        if (existingAuth == null || !existingAuth.isAuthenticated())
            return true;

        // limit username comparison to providers which use usernames (e.g, UsernamePasswordAuthenticationToken)
        if (existingAuth instanceof UsernamePasswordAuthenticationToken && !existingAuth.getName().equals(username))
            return true;

        // replace existing anonymous authentication
        if (existingAuth instanceof AnonymousAuthenticationToken)
            return true;

        return false;
    }
}
