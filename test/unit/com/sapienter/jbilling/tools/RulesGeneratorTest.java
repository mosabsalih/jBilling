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

package com.sapienter.jbilling.tools;

import junit.framework.TestCase;

import java.io.*;

/**
 * @author Alexander Aksenov
 * @since 14.03.2010
 */
public class RulesGeneratorTest extends TestCase {

    private File templateFile;
    private File dataFile;
    private File outputFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateFile = File.createTempFile("template", ".vm");
        dataFile = File.createTempFile("data", ".csv");
        outputFile = File.createTempFile("output", ".drl");
        templateFile.deleteOnExit();
        dataFile.deleteOnExit();
        outputFile.deleteOnExit();
    }

    public void testRulesGeneration() throws IOException {
        BufferedWriter templateWriter = new BufferedWriter(new FileWriter(templateFile));
        String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator == null) lineSeparator = "\n";
        templateWriter.write("when" + lineSeparator +
                "PricingField( name == \"prefix\", intValue_== $field_1)" + lineSeparator +
                "then" + lineSeparator +
                "setPrice($field_2);" + lineSeparator +
                lineSeparator);
        templateWriter.close();

        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile));
        dataWriter.write("613999,0.89\n" +
                "613989,0.99\n" +
                "613979,1.09");
        dataWriter.close();

        RulesGenerator.generateRules(
                templateFile,
                dataFile,
                ',',
                outputFile
        );

        String result = readFileToString(outputFile.getAbsolutePath());
        assertTrue("Output file should contains rule with 613999 prefix",
                result.contains("when" + lineSeparator +
                        "PricingField( name == \"prefix\", intValue_== 613999)" + lineSeparator +
                        "then" + lineSeparator +
                        "setPrice(0.89);" + lineSeparator +
                        lineSeparator));
        assertTrue("Output file should contains rule with 613979 prefix",
                result.contains("when" + lineSeparator +
                        "PricingField( name == \"prefix\", intValue_== 613979)" + lineSeparator +
                        "then" + lineSeparator +
                        "setPrice(1.09);" + lineSeparator +
                        lineSeparator));
    }

    public void testCounters() throws IOException {
        BufferedWriter templateWriter = new BufferedWriter(new FileWriter(templateFile));
        String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator == null) lineSeparator = "\n";
        templateWriter.write("Row $row_number from $total_rows rows, columns count $total_columns" + lineSeparator);
        templateWriter.close();

        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile));
        dataWriter.write("613999,0.89\n" +
                "613989,0.99, 555\n" +
                "613979,1.09");
        dataWriter.close();

        RulesGenerator.generateRules(
                templateFile,
                dataFile,
                ',',
                outputFile
        );

        String result = readFileToString(outputFile.getAbsolutePath());
        assertTrue("Total rows should be 3", result.contains("from 3 rows"));
        assertTrue("Row number 0 should be presented", result.contains("Row 0 from"));
        assertTrue("Row number 1 should be presented", result.contains("Row 1 from"));
        assertTrue("Row number 2 should be presented", result.contains("Row 2 from"));
        assertTrue("Columns count 2 should be presented", result.contains("columns count 2"));
        assertTrue("Columns count 3 should be presented", result.contains("columns count 3"));

    }

    private String readFileToString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }

}
