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
package com.sapienter.jbilling.server.system.event;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;

/**
 * Implementation of this interface take an event, open it to extracts its data.
 * Then calls a specific pluggable task using this data as parameters
 * Usually, there is a one-to-one relationship between:
 *          event - processor - pluggable task
 * Yet, a processor can take care of many events, and deal with one pluggable task.
 * It can also deal with more than one pluggable task, but I don't see a reason 
 * for this.
 * @author ece
 */
public abstract class EventProcessor<TaskType> {
    public abstract void process(Event event);

    protected TaskType getPluggableTask(Integer entityId, Integer taskCategoryId) {
        try {
            PluggableTaskManager taskManager =
                new PluggableTaskManager(entityId,
                taskCategoryId);
            return  (TaskType) taskManager.getNextClass();
        } catch (PluggableTaskException e) {
            throw new SessionInternalError(e);
        }
    }

    public String toString() {
        return this.getClass().getName();
    }
}
