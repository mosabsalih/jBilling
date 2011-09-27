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

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/${appName}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }

    // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    log "warn"

    // repositories for dependency resolution
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.org/nexus/content/groups/public-jboss/"
    }

    // dependencies under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes
    dependencies {

        compile('org.springmodules:spring-modules-cache:0.8') {
            transitive = false
        }

        compile 'org.apache.activemq:activemq-all:5.3.2'
        compile('org.apache.activemq:activemq-pool:5.3.2') {
            excludes 'junit', 'commons-logging', 'log4j'
        }

        compile('org.apache.xmlrpc:xmlrpc-client:3.1') {
            excludes 'junit', 'xml-apis'
        }

        compile('org.apache.geronimo.javamail:geronimo-javamail_1.4_mail:1.5')
        compile('org.apache.geronimo.javamail:geronimo-javamail_1.4_provider:1.5')
        compile('org.apache.geronimo.specs:geronimo-javamail_1.4_spec:1.5')

        compile 'org.drools:drools-core:5.0.1'
        compile 'org.drools:drools-compiler:5.0.1'
        build 'org.drools:drools-decisiontables:5.0.1'
        build 'org.drools:drools-templates:5.0.1'
        build 'org.drools:drools-ant:5.0.1'

        compile 'quartz:quartz:1.4.5'

        compile 'joda-time:joda-time:1.6.2'

        compile('net.sf.opencsv:opencsv:2.1') {
            excludes 'junit'
        }

        compile 'javax.mail:mail:1.4.1'

        compile('commons-httpclient:commons-httpclient:3.0.1') {
            excludes 'junit'
        }
        compile 'commons-net:commons-net:2.0'
        compile 'commons-codec:commons-codec:1.5'

        compile 'org.hibernate:hibernate-validator:4.1.0.Final'
        compile 'javax.validation:validation-api:1.0.0.GA'

        compile 'org.codehaus.jackson:jackson-core-asl:1.7.4'
        compile 'org.codehaus.jackson:jackson-mapper-asl:1.7.4'

        compile 'org.apache.velocity:velocity:1.6.2'
        compile('org.apache.velocity:velocity-tools:2.0') {
            excludes 'struts-core', 'struts-taglib', 'struts-tiles'
        }

        compile('net.sf.jasperreports:jasperreports:4.0.0') {
            excludes 'jaxen', 'xalan', 'xml-apis'
        }
        compile 'net.sf.jasperreports:jasperreports-fonts:4.0.0'
        compile 'net.sourceforge.jexcelapi:jxl:2.6.10'

        compile('net.sf.barcode4j:barcode4j:2.0') {
            excludes 'xerces', 'xalan', 'xml-apis'
        }

        compile 'c3p0:c3p0:0.9.1.2'

        runtime 'postgresql:postgresql:8.4-702.jdbc4'
    }
}
