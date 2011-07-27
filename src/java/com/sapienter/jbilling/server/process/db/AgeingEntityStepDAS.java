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
package com.sapienter.jbilling.server.process.db;

import org.hibernate.Query;

import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.UserStatusDAS;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class AgeingEntityStepDAS extends AbstractDAS<AgeingEntityStepDTO> {

    private static final String findStepSQL = 
            "SELECT a " + 
            "  FROM AgeingEntityStepDTO a " + 
            " WHERE a.company.id = :entity " + 
            "   AND a.userStatus.id = :status ";

    public AgeingEntityStepDTO findStep(Integer entityId, Integer stepId) {
        Query query = getSession().createQuery(findStepSQL);
        query.setParameter("entity", entityId);
        query.setParameter("status", stepId);
        return (AgeingEntityStepDTO) query.uniqueResult();
    }

    public void create(Integer entityId, Integer statusId,
            String welcomeMessage, String failedLoginMessage,
            Integer languageId, int days) {

        AgeingEntityStepDTO ageing = new AgeingEntityStepDTO();
        ageing.setCompany(new CompanyDAS().find(entityId));
        ageing.setUserStatus(new UserStatusDAS().find(statusId));

        ageing.setWelcomeMessage(languageId, welcomeMessage);
        ageing.setFailedLoginMessage(languageId, failedLoginMessage);
        ageing.setDays(days);

        save(ageing);
    }
}
