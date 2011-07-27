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


package com.sapienter.jbilling.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This is a Singleton call that provides the system properties from
 * the jbilling.properties file
 */
public class SystemProperties {
    private static final Logger LOG = Logger.getLogger(SystemProperties.class);

    private static final String JBILLING_HOME = "JBILLING_HOME";
    private static final String PROPERTIES_FILE = "jbilling.properties";
    private static final String RESOURCES_DIR = "resources";
    private static final String BASE_DIR_PROPERTY = "base_dir";

    private static SystemProperties INSTANCE;

    private String resourcesDir = null;
    private Properties prop = null;

    /*
        private singleton constructor
     */
    private SystemProperties() throws IOException {
        File properties = getPropertiesFile();
        FileInputStream stream = new FileInputStream(properties);

        prop = new Properties();
        prop.load(stream);

        stream.close();

        LOG.debug("System properties loaded from: " + properties.getPath());
        System.out.println("System properties loaded from: " + properties.getPath());

        resourcesDir = getJBillingResourcesDir();

        LOG.debug("Resolved jbilling resources directory to: " + resourcesDir);
        System.out.println("Resolved jbilling resources directory to: " + resourcesDir);
    }

    /**
     * Returns a singleton instance of SystemProperties
     *
     * @return instance
     * @throws IOException if properties could not be loaded
     */
    public static SystemProperties getSystemProperties()  throws IOException{
        if (INSTANCE == null)
            INSTANCE = new SystemProperties();
        return INSTANCE;
    }

    /**
     * Returns the jBilling home path where resources and configuration files
     * can be found.
     *
     * The environment variable JBILLING_HOME and system property JBILLING_HOME are examined
     * for this value, with precedence given to system properties set via command line arguments.
     *
     * If no jBilling home path is set, properties will be loaded from the classpath.
     *
     * @return jbilling home path
     */
    public static String getJBillingHome() {
        String jbillingHome = System.getProperty(JBILLING_HOME);

        if (jbillingHome == null) {
            jbillingHome = System.getenv(JBILLING_HOME);
        }

        return jbillingHome;
    }

    /**
     * Returns the path to the jBilling resources directory.
     *
     * The resources directory is always assumed to be located in JBILLING_HOME. If JBILLING_HOME is not
     * set, this method will return a relative path as the default location for the resources directory.
     *
     * @return path to the resources directory
     */
    public String getJBillingResourcesDir() {
        // try JBILLING_HOME
        String jbillingHome = getJBillingHome();
        if (jbillingHome != null) {
            return jbillingHome + File.separator + RESOURCES_DIR + File.separator;
        }

        try {
            // try root dir
            File resources = new File("." + File.separator + RESOURCES_DIR);
            if (resources.exists()) {
                return resources.getCanonicalPath() + File.separator;
            }

            // try one level down (tomcat root)
            resources = new File(".." + File.separator + RESOURCES_DIR);
            if (resources.exists()) {
                return resources.getCanonicalPath() + File.separator;
            }
        } catch (IOException e) {
            LOG.warn("IOException when attempting to resolve canonical path to jbilling resources/", e);
        }

        return "";
    }

    /**
     * Returns the path to the jbilling.properties file.
     *
     * @return properties file
     */
    public static File getPropertiesFile() {
        String jbillingHome = getJBillingHome();
        if (jbillingHome != null) {
            // properties file from filesystem
            return new File(jbillingHome + File.separator + PROPERTIES_FILE);

        } else {
            // properties file from classpath
            URL url = SystemProperties.class.getResource("/" + PROPERTIES_FILE);
            return new File(url.getFile());
        }
    }

    public String get(String key) throws Exception {
        // "base_dir" should always resolve to the JBILLING_HOME resources dir
        // this value is no longer part of jbilling.properties
        if (BASE_DIR_PROPERTY.equals(key)) {
            return resourcesDir;
        }

        // get value from jbilling.properties
        String value = prop.getProperty(key);

        if (value == null)
            throw new Exception("Missing system property: " + key);

        return value;
    }
    
    public String get(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }
}
