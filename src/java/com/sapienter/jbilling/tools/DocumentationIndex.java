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
 * Created on Nov 15, 2004
 *
 */
package com.sapienter.jbilling.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;

/**
 * @author Emil
 *
 */
public class DocumentationIndex {

    static private BufferedReader reader = null;
    
    public static void main(String[] args) {
        try {
            // find my properties
            Properties globalProperties = new Properties();
            FileInputStream gpFile = new FileInputStream("indexing.properties");
            globalProperties.load(gpFile);
            
            // read the directory
            String dirName = globalProperties.getProperty("directory");
            File dir = new File(dirName);
            String filesNames[] = dir.list();
            
            List entries = new ArrayList();
            for (int f = 0; f < filesNames.length; f++) {
                File thisFile = new File(dirName + "/" + filesNames[f]);
                // skip directories
                if (!thisFile.isDirectory() && !filesNames[f].equals(
                        "index.html")) {
                    entries.add(filesNames[f]);
                }
            }
            // sort them by name
            Collections.sort(entries);
            
            // create the result file
            FileOutputStream result = new FileOutputStream(new File(dirName + 
                    "/index.html"));
            result.write("<html><body>".getBytes());
            
            for (Iterator it = entries.iterator(); it.hasNext();) {
                String entry = (String) it.next();
                System.out.println("Adding entry" + entry);

                if (entry.endsWith(".htm")) {
                    // it is an html page, process it
                    String htmlentry = entry.replaceAll(" ", "%20");
                    String link = "<a href=" + htmlentry+ ">";
                    
                    reader = new BufferedReader( new FileReader(
                            dir + "/" + entry) ); 
                    // find the title
                    String title = getText("title");
                    link += title + "</a><br/>\n";
                    
                    result.write(link.getBytes());
                }

            }
            result.write("</body></html>".getBytes());
            result.close();
            
            System.out.println("Done.");
            
        } catch (FileNotFoundException e) {
            System.err.println("Could not open file. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static String getText(String tagName) throws IOException {
        StringBuffer retValue = new StringBuffer();
        String line = reader.readLine();
        while (line != null) {
            if (line.indexOf("<" + tagName + ">") >= 0) {
                return line.substring(line.indexOf(">") + 1, line.lastIndexOf('<'));
            }
            
            line = reader.readLine();
        }
        
        return null;
    }

}
