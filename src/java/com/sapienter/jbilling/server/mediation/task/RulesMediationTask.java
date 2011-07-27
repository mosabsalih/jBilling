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
package com.sapienter.jbilling.server.mediation.task;


import org.apache.log4j.Logger;


import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.rule.RulesBaseTask;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import java.util.List;

public class RulesMediationTask extends RulesBaseTask implements
        IMediationProcess {

    protected Logger getLog() {
        return Logger.getLogger(RulesMediationTask.class);
    }
        
    public void process(List<Record> records, List<MediationResult> results, String configurationName)
            throws TaskException {
 
        // this plug-in gets called many times for the same instance
        rulesMemoryContext.clear();

        int index = -1; // to track the results list
        // if results are passed, there has to be one per record
        if (results != null && results.size() > 0) {
            if (records.size() != results.size()) {
                throw new TaskException("If results are passed, there have to be the same number as" +
                        " records");
            }
            index = 0;
        } else if (results == null) {
            throw new TaskException("The results array can not be null");
        }

        for (Record record: records) {
            // one result per record
            MediationResult result = null;
            if (index >= 0) {
                result = results.get(index++);
            } else {
                result = new MediationResult(configurationName, true);
            }
            result.setRecordKey(record.getKey());
            rulesMemoryContext.add(result);
            results.add(result); // for easy retrival later

            for (PricingField field: record.getFields()) {
                field.setResultId(result.getId());
                rulesMemoryContext.add(field);
            }
        }

        // add the company
        rulesMemoryContext.add(new CompanyDAS().find(getEntityId()));
        
        // then execute the rules
        executeRules();
    }
}
