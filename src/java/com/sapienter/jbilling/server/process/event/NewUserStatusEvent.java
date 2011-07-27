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

package com.sapienter.jbilling.server.process.event;

import com.sapienter.jbilling.server.system.event.Event;

/**
 * This event is triggered when a user's status is changed.
 */
public class NewUserStatusEvent implements Event {
    private Integer entityId;
    private Integer userId;
    private Integer oldStatusId;
    private Integer newStatusId;

    public NewUserStatusEvent(Integer entityId, Integer userId, 
            Integer oldStatusId, Integer newStatusId) {
        this.entityId = entityId;
        this.userId = userId;
        this.oldStatusId = oldStatusId;
        this.newStatusId = newStatusId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public Integer getUserId() {
        return userId;
    }

    /**
     * Returns the status id (status_value) of the users original state before the status
     * was changed and the event fired.
     *
     * @see com.sapienter.jbilling.server.util.db.AbstractGenericStatus#getId()
     *
     * @return users original status id
     */
    public Integer getOldStatusId() {
        return oldStatusId;
    }

    /**
     * Returns the new status id (status_value) of the users newly assigned status.
     *
     * @see com.sapienter.jbilling.server.util.db.AbstractGenericStatus#getId()
     *
     * @return users new status id
     */
    public Integer getNewStatusId() {
        return newStatusId;
    }

    public String getName() {
        return "New User Status Event";
    }
    
    public String toString() {
        return getName() + " - entity " + entityId;
    }
}
