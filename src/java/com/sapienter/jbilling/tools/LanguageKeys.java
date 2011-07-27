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

/*
 * Created on Sep 24, 2004
 *
 * Shows which keys are missing for a language in the ApplicationProperties
 */
package com.sapienter.jbilling.tools;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Emil
 *
 */
public class LanguageKeys {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: LanguageKeys language_code");
        }
        
        String language = args[0];
        
        try {
            // open the default properties page
            Properties globalProperties = new Properties();
            FileInputStream propFile = new FileInputStream(
                    "ApplicationResources.properties");
            globalProperties.load(propFile);

            // and the one for the specifed language
            Properties languageProperties = new Properties();
            propFile = new FileInputStream(
                    "ApplicationResources_" + language + ".properties");
            languageProperties.load(propFile);
            
            // no go through all the keys
            for (Iterator it = globalProperties.keySet().iterator(); 
                    it.hasNext(); ) {
                String key = (String) it.next();
                if (!languageProperties.containsKey(key)) {
                    System.out.println(key + "=" + 
                            globalProperties.getProperty(key));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
