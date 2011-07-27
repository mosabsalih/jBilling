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
package com.sapienter.jbilling.server.notification.db;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.util.db.AbstractDAS;

/**
 * 
 * @author abimael
 * 
 */
public class NotificationMessageArchLineDAS extends
        AbstractDAS<NotificationMessageArchLineDTO> {

    public static final int CONTENT_MAX_LENGTH = 500;

    public NotificationMessageArchLineDTO create(String content, Integer section) {
        NotificationMessageArchLineDTO nmal = new NotificationMessageArchLineDTO();

        if (content.length() > CONTENT_MAX_LENGTH) {
            content = content.substring(0, CONTENT_MAX_LENGTH);
            Logger.getLogger(NotificationMessageArchLineDAS.class).warn(
                    "Trying to insert line too long. Truncating to "
                            + CONTENT_MAX_LENGTH);
        }

        nmal.setSection(section);
        nmal.setContent(content);

        return save(nmal);
    }

}
