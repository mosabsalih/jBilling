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

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

beans = {

    /*
        Database configuration
     */
    dataSource(ComboPooledDataSource) { bean ->
        bean.destroyMethod = 'close'

        // database connection properties from DataSource.groovy
        user = CH.config.dataSource.username
        password = CH.config.dataSource.password
        driverClass = CH.config.dataSource.driverClassName
        jdbcUrl = CH.config.dataSource.url

        // Connection pooling using c3p0
        acquireIncrement = 2
        initialPoolSize = 10
        minPoolSize = 10
        maxPoolSize = 50
        maxIdleTime = 300
        checkoutTimeout = 10000

        /*
           Periodically test the state of idle connections and validate connections on checkout. Handles
           potential timeouts by the database server. Increase the connection idle test period if you
           have intermittent database connection issues.
         */
        testConnectionOnCheckout = true
        idleConnectionTestPeriod = 30
        preferredTestQuery = "select id from jbilling_table where id = 1"

        /*
           Destroy un-returned connections after a period of time (in seconds) and throw an exception
           that shows who is still holding the un-returned connection. Useful for debugging connection
           leaks.
         */
        // unreturnedConnectionTimeout = 10
        // debugUnreturnedConnectionStackTraces = true
    }

    /*
        Custom data binding and property parsing rules
     */
    customPropertyEditorRegistrar(com.sapienter.jbilling.client.editor.CustomPropertyEditorRegistrar) {
        messageSource = ref('messageSource')
    }

    /*
        Spring security
     */
    // populates session attributes and locale from the authenticated user
    securitySession(com.sapienter.jbilling.client.authentication.util.SecuritySession) {
        localeResolver = ref('localeResolver')
    }

    // normal username / password authentication
    authenticationProcessingFilter(com.sapienter.jbilling.client.authentication.CompanyUserAuthenticationFilter) {
        authenticationManager = ref("authenticationManager")        
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('authenticationFailureHandler')
        rememberMeServices = ref('rememberMeServices')
        securitySession = ref('securitySession')
    }

    // remember me cookie authentication
    rememberMeAuthenticationFilter(com.sapienter.jbilling.client.authentication.CompanyUserRememberMeFilter) {
        authenticationManager = ref('authenticationManager')
        rememberMeServices = ref('rememberMeServices')
        securitySession = ref('securitySession')
    }

    /*
        Automatic authentication using a defined username and password that removes the need for the caller
        to authenticate themselves. This is used with web-service protocols that don't support authentication,
        but can also be used to create "pre-authenticated" URLS by updating the filter chain in 'Config.groovy'.
     */
    staticAuthenticationProcessingFilter(com.sapienter.jbilling.client.authentication.StaticAuthenticationFilter) {
        authenticationManager = ref("authenticationManager")
        authenticationDetailsSource = ref('authenticationDetailsSource')
        username = "admin;1"
        password = "123qwe"
    }

    userDetailsService(com.sapienter.jbilling.client.authentication.CompanyUserDetailsService) {
        springSecurityService = ref("springSecurityService")
    }

    passwordEncoder(com.sapienter.jbilling.client.authentication.JBillingPasswordEncoder)

    permissionVoter(com.sapienter.jbilling.client.authentication.PermissionVoter)

    webExpressionVoter(com.sapienter.jbilling.client.authentication.SafeWebExpressionVoter) {
        expressionHandler = ref("webExpressionHandler")
    }


    /*
        Remoting
     */
    // HTTP request handler for remote beans
    httpRequestAdapter org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter


    /*
        Others
     */
    // resolves exceptions into messages for the view
    viewUtils(com.sapienter.jbilling.client.ViewUtils) {
        messageSource = ref("messageSource")
    }
}
