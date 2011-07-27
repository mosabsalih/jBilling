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

import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.db.AbstractDAS;
import com.sapienter.jbilling.server.util.db.LanguageDAS;
import com.sapienter.jbilling.server.util.db.LanguageDTO;

/**
 * 
 * @author abimael
 * 
 */
public class NotificationMessageDAS extends AbstractDAS<NotificationMessageDTO> {

    public NotificationMessageDTO findIt(Integer typeId, Integer entityId,
            Integer languageId) {

        /*
         * query="SELECT OBJECT(a) FROM notification_message a WHERE a.type.id =
         * ?1 AND a.entityId = ?2 AND a.languageId = ?3"
         * result-type-mapping="Local"
         */
        Criteria criteria = getSession().createCriteria(
                NotificationMessageDTO.class);
        criteria.createAlias("entity", "e").add(
                Restrictions.eq("e.id", entityId.intValue()));
        criteria.createAlias("notificationMessageType", "nmt").add(
                Restrictions.eq("nmt.id", typeId.intValue()));
        criteria.createAlias("language", "l").add(
                Restrictions.eq("l.id", languageId.intValue()));

        return (NotificationMessageDTO) criteria.uniqueResult();
    }

    public NotificationMessageDTO create(Integer typeId, Integer entityId,
            Integer languageId, Boolean useFlag) {

        // search company
        CompanyDTO company = new CompanyDAS().find(entityId);
        // search language
        LanguageDTO language = new LanguageDAS().find(languageId);

        NotificationMessageTypeDTO notif = new NotificationMessageTypeDAS().find(typeId);

        short flag = useFlag ? new Short("1") : new Short("0");
        NotificationMessageDTO nm = new NotificationMessageDTO();
        nm.setEntity(company);
        nm.setNotificationMessageType(notif);
        nm.setLanguage(language);
        nm.setUseFlag(flag);
        return save(nm);

    }

    public NotificationMessageDTO create(Integer typeId, Integer entityId,
            Integer languageId, Boolean useFlag,
            Set<NotificationMessageSectionDTO> notifs) {

        // search company
        CompanyDTO company = new CompanyDAS().find(entityId);
        // search language
        LanguageDTO language = new LanguageDAS().find(languageId);

        NotificationMessageTypeDTO notif = new NotificationMessageTypeDAS().find(typeId);

        short flag = useFlag ? new Short("1") : new Short("0");
        NotificationMessageDTO nm = new NotificationMessageDTO();
        nm.setEntity(company);
        nm.setNotificationMessageType(notif);
        nm.setLanguage(language);
        nm.setUseFlag(flag);
        nm.setNotificationMessageSections(notifs);
        return save(nm);

    }

}
