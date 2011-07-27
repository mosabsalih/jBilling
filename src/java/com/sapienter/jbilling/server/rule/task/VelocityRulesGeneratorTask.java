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
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;

/**
 * Generates rules using Velocity templates. 
 *
 * Parameters:
 *  - those of AbstractGeneratorTask
 *  - 'template_filename' - the velocity template to generate the
 *    rules. Relative path names are assumed to be from the 
 *    'jbilling/resource/rules' directory.
 */
public class VelocityRulesGeneratorTask extends AbstractGeneratorTask {

	public static final ParameterDescription PARAM_TEMPLATE_FILENAME = 
		new ParameterDescription("template_filename", true, ParameterDescription.Type.STR);

    { 
        descriptions.add(PARAM_TEMPLATE_FILENAME);
    }
    
    private static final Logger LOG = Logger.getLogger(VelocityRulesGeneratorTask.class);

    public VelocityRulesGeneratorTask() {
        super();
    }

    /**
     * Returns a string of the generated rules using data from the 
     * passed in object.
     */
    protected String generateRules(Object objects) throws TaskException {
        // get filename
        if (parameters.get(PARAM_TEMPLATE_FILENAME.getName()) == null) {
            throw new TaskException("No '" + PARAM_TEMPLATE_FILENAME.getName() + 
                    "' parameter specified.");
        }
        File templateFilename = new File(getAbsolutePath((String) 
                parameters.get(PARAM_TEMPLATE_FILENAME.getName())));

        // create a new engine (we need to set the file path)
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, 
                templateFilename.getParent());
        try {
            velocityEngine.init();
        } catch (Exception e) {
            LOG.error("Error initializing template engine.");
            throw new TaskException(e);
        }

        VelocityContext velocityContext = new VelocityContext();
        // add parameters using reflection
        addParameters(velocityContext, objects);

        // generate rules and return
        StringWriter result = new StringWriter();
        try {
            velocityEngine.mergeTemplate(templateFilename.getName(), 
                    velocityContext, result);
        } catch (Exception e) {
            LOG.error("Error generating rules.");
            throw new TaskException(e);
        }
        return result.toString();
    }

    /**
     * Adds parameters to the velocity context using reflection.
     * All 'get' methods that return Objects are added to the context,
     * with the chars after 'get' (first char lowercase) as the key.
     */
    private void addParameters(VelocityContext context, Object objects) 
            throws TaskException {
        Class<?> objectsClass = objects.getClass();
        Method methods[] = objectsClass.getMethods();
        for (Method method : methods) {
            // If method starts with 'get', returns an Object and
            // takes no parameters, execute it and save result for 
            // the velocity context.
            String methodName = method.getName();
            if (methodName.length() > 3 && methodName.startsWith("get") &&
                    !method.getReturnType().isPrimitive() &&
                    method.getParameterTypes().length == 0) {
                // use the chars after the 'get' as the key
                String key = methodName.substring(3, 4).toLowerCase();
                if (methodName.length() > 4) {
                    key += methodName.substring(4);
                }
                try {
                    context.put(key, method.invoke(objects));
                } catch (Exception e) {
                    LOG.error("Error invoking " + methodName + 
                            " method via reflection.");
                    throw new TaskException(e);
                }
            }
            // add the passed in Object itself to the context
            context.put("data", objects);
        }
    }

    /**
     * For unit testing.
     * @param parameters parameters to set
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
