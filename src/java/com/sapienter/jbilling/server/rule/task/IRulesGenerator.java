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

import com.sapienter.jbilling.server.pluggableTask.TaskException;

/**
 * Interface for Rules Generator task, which handles calls made to the
 * generateRules API method. First, the unmarshal method is called to
 * parse and validate the input string. The process method is then 
 * called to generate, compile and save the rules. 
 */
public interface IRulesGenerator {

    /**
     * Parses and validates the input data.
     */
    public void unmarshal(String objects) throws TaskException;

    /**
     * Generates, compiles and saves the rules.
     */
    public void process() throws TaskException;
}
