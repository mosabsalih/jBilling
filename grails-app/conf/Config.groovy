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

import org.apache.log4j.*

/*
    Load configuration files from the set "JBILLING_HOME" path (provided as either
    an environment variable or a command line system property). External configuration
    files will override default settings.
 */

def appHome = System.getProperty("JBILLING_HOME") ?: System.getenv("JBILLING_HOME")

if (appHome) {
    println "Loading configuration files from JBILLING_HOME = ${appHome}"
    grails.config.locations = [
            "file:${appHome}/${appName}-Config.groovy",
            "file:${appHome}/${appName}-DataSource.groovy"
    ]

} else {
    appHome = new File("../${appName}")
    if (appHome.listFiles({dir, file -> file ==~ /${appName}-.*\.groovy/} as FilenameFilter )) {
        println "Loading configuration files from ${appHome.canonicalPath}"
        grails.config.locations = [
                "file:${appHome.canonicalPath}/${appName}-Config.groovy",
                "file:${appHome.canonicalPath}/${appName}-DataSource.groovy"
        ]

        println "Setting JBILLING_HOME to ${appHome.canonicalPath}"
        System.setProperty("JBILLING_HOME", appHome.canonicalPath)

    } else {
        println "Loading configuration files from classpath"
    }
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'
// use the jQuery javascript library
grails.views.javascript.library="jquery"
// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}


/*
    Documentation
 */
grails.doc.authors="Emiliano Conde, Brian Cowdery, Emir Calabuch, Lucas Pickstone, Vikas Bodani, Crystal Bourque"
grails.doc.license="AGPL v3"
grails.doc.images=new File("src/docs/images")
grails.doc.api.org.springframework="http://static.springsource.org/spring/docs/3.0.x/javadoc-api/"
grails.doc.api.org.hibernate="http://docs.jboss.org/hibernate/stable/core/javadocs/"

//gdoc aliases
grails.doc.alias.userGuide="1. jBilling User Guide"
grails.doc.alias.integrationGuide="2. jBilling Integration Guide"

/*
    Spring Security
 */
// require authentication on all URL's
grails.plugins.springsecurity.rejectIfNoRule = false

// failure url
grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/login/authfail?login_error=1'

// remember me cookies
grails.plugins.springsecurity.rememberMe.cookieName = "jbilling_remember_me"
grails.plugins.springsecurity.rememberMe.key = "xANgU6Y7lJVhI"

// basic HTTP authentication filter for web-services
grails.plugins.springsecurity.useBasicAuth = true
grails.plugins.springsecurity.basic.realmName = "jBilling Web Services"

// authentication filter configuration
grails.plugins.springsecurity.filterChain.chainMap = [
        '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
]

// voter configuration
grails.plugins.springsecurity.voterNames = ['authenticatedVoter', 'roleVoter', 'permissionVoter', 'webExpressionVoter']

