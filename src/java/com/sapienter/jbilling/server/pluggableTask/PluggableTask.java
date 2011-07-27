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
 * Created on Apr 15, 2003
 *
 */
package com.sapienter.jbilling.server.pluggableTask;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.io.ResourceChangeScannerConfiguration;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ByteArrayResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDTO;


public abstract class PluggableTask {

    public static final SimpleDateFormat PARAMETER_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmm");

    protected Map<String, String> parameters = null;
    private Integer entityId = null;
    private PluggableTaskDTO task = null;
    protected Hashtable<Object, FactHandle> handlers = null;
    protected StatefulKnowledgeSession session = null;
    private static final Logger LOG = Logger.getLogger(PluggableTask.class);

    private static HashMap<Integer, KnowledgeAgent> knowledgeBasesCache = new HashMap<Integer, KnowledgeAgent>();
    private static AtomicBoolean isRulesChangeScanerStarted = new AtomicBoolean(false);


    public final List<ParameterDescription> descriptions = new ArrayList<ParameterDescription>();

    public List<ParameterDescription> getParameterDescriptions() {
        return descriptions;
    }

    protected Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getTaskId() {
        return task.getId();
    }

    public void initializeParamters(PluggableTaskDTO task)
            throws PluggableTaskException {
        Collection<PluggableTaskParameterDTO> DBparameters = task.getParameters();
        parameters = new HashMap<String, String>();
        entityId = task.getEntityId();
        this.task = task;
        if (DBparameters.size() <
                task.getType().getMinParameters().intValue()) {
            throw new PluggableTaskException("Type [" + task.getType().getClassName() + "] requires at least " +
                    task.getType().getMinParameters() + " parameters." +
                    DBparameters.size() + " found.");
        }

        if (DBparameters.isEmpty()) {
            return;
        }

        for (PluggableTaskParameterDTO parameter : DBparameters) {
            Object value = parameter.getIntValue();
            if (value == null) {
                value = parameter.getStrValue();
                if (value == null) {
                    value = parameter.getFloatValue();
                }
            }

            // change: all the parameters will be strings in jB3. TODO: drop the int_value, float_value columns
            parameters.put(parameter.getName(), value.toString());
        }
    }

    /**
     * Returns the plug-in parameter value as a String if it exists, or
     * returns the given default value if it doesn't
     *
     * @param key plug-in parameter name
     * @param defaultValue default value if parameter not defined
     * @return parameter value, or default if not defined
     */
    protected String getParameter(String key, String defaultValue) {
        String value = parameters.get(key);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * Returns the plug-in parameter value as an Integer if it exists, or
     * returns the given default value if it doesn't
     *
     * @param key plug-in parameter name
     * @param defaultValue default value if parameter not defined
     * @return parameter value, or default if not defined
     */
    protected Integer getParameter(String key, Integer defaultValue) throws PluggableTaskException {
        String value = parameters.get(key);

        try {
            return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            throw new PluggableTaskException(key + " could not be parsed as an integer!", e);
        }
    }

    /**
     * Returns the plug-in parameter value as a boolean value if it exists, or
     * returns the given default value if it doesn't.
     *
     * "true" and "True" equals Boolean.TRUE, all other values equate to false.
     *
     * @param key plug-in parameter name
     * @param defaultValue default value if parameter not defined
     * @return parameter value, or default if not defined
     */
    protected Boolean getParameter(String key, Boolean defaultValue) {
        String value = parameters.get(key);
        return StringUtils.isNotBlank(value) ? (value).equalsIgnoreCase("true") : defaultValue;
    }

    /**
     * Returns the plug-in parameter value as a Date value if it exists, or
     * returns the given default value if it doesn't.
     *
     * Parameter date strings must be in the format "yyyyMMdd-HHmm"
     *
     * @param key plug-in parameter name
     * @param defaultValue default value if parameter not defined
     * @return parameter value, or default if not defined
     * @throws PluggableTaskException thrown if parameter could not be parsed as a date
     */
    protected Date getParameter(String key, Date defaultValue) throws PluggableTaskException {
        String value = parameters.get(key);

        try {
            return StringUtils.isNotBlank(value) ? PARAMETER_DATE_FORMAT.parse(value) : defaultValue;
        } catch (ParseException e) {
            throw new PluggableTaskException(key + " could not be parsed as a date!", e);
        }
    }

    protected KnowledgeBase readKnowledgeBase() {
        if (knowledgeBasesCache.containsKey(task.getId())) {
            return knowledgeBasesCache.get(task.getId()).getKnowledgeBase();
        }

        // Creating agent with default KnowledgeAgentConfiguration for scanning files and directories
        KnowledgeAgent kAgent = KnowledgeAgentFactory.newKnowledgeAgent("Knowledge agent for task#" + task.getId());

        // Adding resources for observing by KnowledgeAgent and creating KnowledgeBase.
        // Current version of api (5.0.1) does not implement adding resources from KnowledgeBase,
        // that was mentioned in api documentation (may be bug in source code).
        // So, we use other aproach for configuring KnowledgeAgent
        // Now agent interface allowes defining resources and directories for observing
        // only through ChangeSet from Resource (usually xml config file)
        // We create needed configuration dynamically as string
        // from task parameters information
        kAgent.applyChangeSet(new ByteArrayResource(createChangeSetStringFromTaskParameters().getBytes()));

        // Cache agent for further usage without recreation
        knowledgeBasesCache.put(task.getId(), kAgent);
        // Start scanning services for automatical updates of cached agents
        startRulesScannerIfNeeded();

        return kAgent.getKnowledgeBase();
    }

    public static void invalidateRuleCache(Integer taskId) {
        knowledgeBasesCache.remove(taskId);
    }

    protected void executeStatefulRules(StatefulKnowledgeSession session, List context) {
        handlers = new Hashtable<Object, FactHandle>();
        for (Object o : context) {
            if (o != null) {
                LOG.debug("inserting object " + o);
                handlers.put(o, session.insert(o));
            } else {
                LOG.warn("Attempted to insert a NULL object into the working memeory");
            }

        }

        session.fireAllRules();
        session.dispose();
        handlers.clear();
        handlers = null;
    }

    protected void removeObject(Object o) {
        FactHandle h = handlers.get(o);
        if (h != null) {
            LOG.debug("removing object " + o + " hash " + o.hashCode());
            session.retract(h);
            handlers.remove(o);
        }
    }

    /*
    public void updateObject(Object oldO, Object newO)
            throws TaskException {
        removeObject(oldO);
        LOG.debug("inserting object " + newO + "hash " + newO.hashCode());
        handlers.put(newO, session.insert(newO));
//        session.fireAllRules(); // could it lead to infinite recurring loop?
    }
    */

    /**
     * Creating ChangeSet configuration from task parameters
     * for obserivng KnowledgeBase
     *
     * @return xml-configuration string
     */
    private String createChangeSetStringFromTaskParameters() {
        // todo: may be some problems (messages in console) now with xml validation during parsing
        // but it's recomended in api-documentation schemas
        StringBuilder str = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<change-set xmlns='http://drools.org/drools-5.0/change-set' " +
                " xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' " +
                " xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >");
        str.append("<add>");

        String defaultDir = Util.getSysProp("base_dir") + "rules";
        String prefix;
        for (String key : parameters.keySet()) {
            String value = (String) parameters.get(key);
            LOG.debug("processing parameter " + key + " value " + value);

            if (StringUtils.isNotEmpty(value)) {
                if (key.equals("file")) {
                    String[] files = com.sapienter.jbilling.server.util.Util
                            .csvSplitLine(value, ' ');
                    for (String file : files) {
                        prefix = "";
                        if (!(new File(file)).isAbsolute()) {
                            // prepend the default directory if file path is relative
                            prefix = defaultDir + File.separator;
                        }
                        LOG.debug("adding parameter " + file);
                        appendResource(str, "file:" + prefix + file, "PKG");
                    }

                } else if (key.equals("dir")) {
                    String[] dirs = com.sapienter.jbilling.server.util.Util
                            .csvSplitLine(value, ' ');
                    for (String dir : dirs) {
                        prefix = "";
                        if (!new File(dir).isAbsolute()) {
                            // prepend the default directory if directory path is relative
                            prefix = defaultDir + File.separator;
                        }
                        LOG.debug("adding parameter " + dir);
                        appendResource(str, "file:" + prefix + dir, "PKG");
                    }

                } else if (key.equals("url")) {
                    String[] urls = com.sapienter.jbilling.server.util.Util
                            .csvSplitLine(value, ' ');
                    for (String url : urls) {
                        LOG.debug("adding parameter " + url);
                        appendResource(str, url, "PKG");
                    }

                } else {
                    //for other types of resources
                    LOG.warn("Resource for parameter " + key + "->" + value + " not supported");
                }
            }
        }
        if (parameters.isEmpty()) {
            appendResource(str, "file:" + defaultDir, "PKG");
            LOG.debug("No task parameters, using directory default:" + defaultDir);
        }

        str.append("</add>");
        str.append("</change-set>");
        return str.toString();
    }

    private void appendResource(StringBuilder builder, String source, String type) {
        builder.append("<resource source='");
        builder.append(source);
        builder.append("' type='");
        builder.append(type);
        builder.append("' />");
    }

    private void startRulesScannerIfNeeded() {
        if (!isRulesChangeScanerStarted.getAndSet(true)) {
            // Set the interval on the ResourceChangeScannerService if it presented in configuration. Default value - 60s.
            if (Util.getSysProp("rules_scanner_interval") != null) {
                ResourceChangeScannerConfiguration sconf = ResourceFactory.getResourceChangeScannerService().newResourceChangeScannerConfiguration();
                // set the disk scanning interval to 30s, default is 60s
                sconf.setProperty("drools.resource.scanner.interval", Util.getSysProp("rules_scanner_interval"));
                ResourceFactory.getResourceChangeScannerService().configure(sconf);
            }
            // Starting services for scanning rules updates
            ResourceFactory.getResourceChangeNotifierService().start();
            ResourceFactory.getResourceChangeScannerService().start();
        }
    }

    public boolean validate() {
        if (getParameterDescriptions() != null) {
            // validate that those required are present
            for (ParameterDescription param: getParameterDescriptions()) {
                if (param.isRequired()) {
                    if(parameters == null || !parameters.containsKey(param.getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * The getter and setter methods for the class field parameters
     * is provided only for the sole purpose of injecting a pluggable
     * task via spring configuration for tests that run without the
     * jbilling running.
     * @return
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
