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

package com.sapienter.jbilling.server.user.event;

import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 *
 * @author vikasb
 */
public class AchUpdateEvent implements Event {

    private final AchDTO ach;
    private final Integer entityId;

    public AchDTO getAch() {
        return ach;
    }

    public AchUpdateEvent(AchDTO ach, Integer entityId) {
        this.ach = ach;
        this.entityId = entityId;
    }

    public String getName() {
        return "Update ACH event";
    }

    public final Integer getEntityId() {
        return entityId;
    }
}
