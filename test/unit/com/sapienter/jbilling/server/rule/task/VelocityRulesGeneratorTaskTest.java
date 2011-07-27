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

package com.sapienter.jbilling.server.rule.task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;

import junit.framework.TestCase;

import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.rule.task.test.Bundle;
import com.sapienter.jbilling.server.rule.task.test.Product;

/**
 * Unit tests for the VelocityRulesGeneratorTask plug-in.
 */
public class VelocityRulesGeneratorTaskTest extends TestCase {

    // class under test
    public static final VelocityRulesGeneratorTask task = new VelocityRulesGeneratorTask();
    public static File outputFile = null;

    // set plug-in parameters
    static {
        HashMap<String, String> parameters = new HashMap<String, String>();

        // rules digester config
        String config = System.getProperty("user.dir") + "/descriptors/rules/rules-generator-config.xml";
        parameters.put(AbstractGeneratorTask.PARAM_CONFIG_FILENAME.getName(), config);

        // output file
        try {
            outputFile = File.createTempFile("test", "pkg");
            outputFile.deleteOnExit();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        parameters.put(AbstractGeneratorTask.PARAM_OUTPUT_FILENAME.getName(), outputFile.getAbsolutePath());

        // rules velocity template
        String template = System.getProperty("user.dir") + "/descriptors/rules/rules-generator-template-unit-test.vm";
        parameters.put(VelocityRulesGeneratorTask.PARAM_TEMPLATE_FILENAME.getName(), template);
        task.setParameters(parameters);
    }

    public VelocityRulesGeneratorTaskTest() {
        super();
    }

    public VelocityRulesGeneratorTaskTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testXMLParsing() throws TaskException {
        String xml = 
            "<bundles> " +
              "<bundle> " +
                "<original-product> " +
                  "<name>Silver Package</name> " +
                "</original-product> " +
                "<replacement-product> " +
                  "<name>Medium speed connection</name> " +
                "</replacement-product> " +
                "<replacement-product> " +
                  "<name>Unlimited emails</name> " +
                "</replacement-product> " +
              "</bundle> " +
              "<bundle> " +
                "<original-product> " +
                  "<name>Gold Package</name> " +
                "</original-product> " +
                "<replacement-product> " +
                  "<name>High speed connection</name> " +
                "</replacement-product> " +
                "<replacement-product> " +
                  "<name>Unlimited emails</name> " +
                "</replacement-product> " +
              "</bundle> " +
            "</bundles>";

        // process XML
        task.unmarshal(xml);

        // get objects created
        Object data = task.getData();

        // test objects
        assertTrue("Data Object is an instance of List", data instanceof List);
        List<Bundle> bundles = (List<Bundle>) data;
        assertTrue("List contains two objects", bundles.size() == 2);

        // first bundle
        Bundle bundle1 = bundles.get(0);
        assertEquals("Bundle1 original product", "Silver Package", bundle1.getOriginalProduct().getName());
        // first bundle products
        List<Product> replacementProducts1 = bundle1.getReplacementProducts();
        assertEquals("Bundle1 first replacement product", "Medium speed connection", replacementProducts1.get(0).getName());
        assertEquals("Bundle1 second replacement product", "Unlimited emails", replacementProducts1.get(1).getName());

        // second bundle
        Bundle bundle2 = bundles.get(1);
        assertEquals("Bundle2 original product", "Gold Package", bundle2.getOriginalProduct().getName());

        // first bundle products
        List<Product> replacementProducts2 = bundle2.getReplacementProducts();
        assertEquals("Bundle2 first replacement product", "High speed connection", replacementProducts2.get(0).getName());
        assertEquals("Bundle2 second replacement product", "Unlimited emails", replacementProducts2.get(1).getName());
    }

    public void testRuleGeneration() throws Exception {
        // generate and compile rules
        task.process();

        // check the generated rules string
        String rules = task.getRules();

        String expected =
                "package InternalEventsRulesTask520\n" +
                "\n" +
                "import com.sapienter.jbilling.server.order.OrderLineBL\n" +
                "import com.sapienter.jbilling.server.order.event.OrderToInvoiceEvent\n" +
                "import com.sapienter.jbilling.server.order.db.OrderDTO\n" +
                "import com.sapienter.jbilling.server.order.db.OrderLineDTO\n" +
                "\n" +
                "rule 'Bundle 1'\n" +
                "when\n" +
                "        OrderToInvoiceEvent(userId == 1010)\n" +
                "        $order : OrderDTO(notes == \"Change me.\")\n" +
                "        $planLine : OrderLineDTO( itemId == 1) from $order.lines # Plan\n" +
                "then\n" +
                "        $order.setNotes(\"Modified by rules created by generateRules API method.\");\n" +
                "        $order.getLines().remove($planLine); # Plan is only for grouping\n" +
                "\n" +
                "        OrderLineBL.addItem($order, 1, false); # A product for this plan\n" +
                "        OrderLineBL.addItem($order, 1, false); # A product for this plan\n" +
                "        update($order);\n" +
                "end\n" +
                "rule 'Bundle 2'\n" +
                "when\n" +
                "        OrderToInvoiceEvent(userId == 1010)\n" +
                "        $order : OrderDTO(notes == \"Change me.\")\n" +
                "        $planLine : OrderLineDTO( itemId == 1) from $order.lines # Plan\n" +
                "then\n" +
                "        $order.setNotes(\"Modified by rules created by generateRules API method.\");\n" +
                "        $order.getLines().remove($planLine); # Plan is only for grouping\n" +
                "\n" +
                "        OrderLineBL.addItem($order, 1, false); # A product for this plan\n" +
                "        OrderLineBL.addItem($order, 1, false); # A product for this plan\n" +
                "        update($order);\n" +
                "end\n";

        assertEquals("Generated rules match expected rules.", expected, rules);
    }
}
