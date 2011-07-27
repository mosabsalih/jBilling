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
package com.sapienter.jbilling.server.rule;

import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;
import org.mvel2.optimizers.OptimizerFactory;

/**
 *
 * @author emilc
 */
public abstract class RulesBaseTask extends PluggableTask {
    
    public static final ParameterDescription PARAM_FILE = 
        new ParameterDescription("file", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAM_URL = 
        new ParameterDescription("url", false, ParameterDescription.Type.STR);    
    public static final ParameterDescription PARAM_DIR = 
        new ParameterDescription("dir", false, ParameterDescription.Type.STR);    

    //initializer for pluggable params
    { 
        descriptions.add(PARAM_FILE);
        descriptions.add(PARAM_URL);
        descriptions.add(PARAM_DIR);
    }
  
    
    protected Logger LOG = getLog(); // to be set by the real plug-in

    protected List<Object> rulesMemoryContext = new ArrayList<Object>();

    protected void executeRules() throws TaskException {
        // show what's in first
        for (Object o: rulesMemoryContext) {
            LOG.debug("in memory context=" + o);
        }

        // JBRULES-2253: NoClassDefFoundError with the ASM optimizer when optimizing MVEL consequences.
        // Use the reflective optimizer as a workaround - this may reduce rules performance.
        // @see http://mvel.codehaus.org/Optimizers
        OptimizerFactory.setDefaultOptimizer("reflective");
        LOG.debug("Using MVEL thread accessor optimizer: " + OptimizerFactory.getThreadAccessorOptimizer());
        LOG.debug("Using MVEL accessor compiler: " + OptimizerFactory.getDefaultAccessorCompiler());

        KnowledgeBase knowledgeBase;
        StatelessKnowledgeSession statelessSession;
        try {
            knowledgeBase = readKnowledgeBase();
            statelessSession = knowledgeBase.newStatelessKnowledgeSession();
        } catch (Exception e) {
            throw new TaskException(e);
        }

        // add the log object for the rules to use
        statelessSession.setGlobal("LOG", LOG);
        statelessSession.execute(rulesMemoryContext);
    }

    protected abstract Logger getLog();
}
