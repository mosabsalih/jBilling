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

import java.util.Date;
import java.util.HashSet;

import com.sapienter.jbilling.server.notification.MessageSection;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

/**
 * 
 * @author abimael
 * 
 */
public class NotificationMessageArchDAS extends
        AbstractDAS<NotificationMessageArchDTO> {
    private static int LINE_LENGTH = 500;

    public NotificationMessageArchDTO create(Integer id,
            MessageSection[] sections) {

        NotificationMessageArchLineDAS lineHome = new NotificationMessageArchLineDAS();
        NotificationMessageArchDTO nma = new NotificationMessageArchDTO();
        nma.setTypeId(id);
        nma.setCreateDatetime(new Date());

        for (int f = 0; f < sections.length; f++) {

            String content = sections[f].getContent();
            for (int index = 0; index < content.length(); index += LINE_LENGTH) {
                int end = (content.length() < index + LINE_LENGTH) ? content
                        .length() : index + LINE_LENGTH;
                NotificationMessageArchLineDTO line = lineHome.create(content
                        .substring(index, end), sections[f].getSection());
                line.setNotificationMessageArch(nma);
                nma.getNotificationMessageArchLines().add(line);
            }
        }

        return save(nma);
    }

}
