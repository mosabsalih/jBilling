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


import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

public class ProcessRunUserDAS extends AbstractDAS<ProcessRunUserDTO> {

    public ProcessRunUserDTO create(Integer processRunId, Integer userId, Integer status, Date created) {
        ProcessRunUserDTO dto = new ProcessRunUserDTO();
        dto.setStatus(status);
        dto.setCreated(created);
        dto.setUser(new UserDAS().find(userId));
        dto.setProcessRun(new ProcessRunDAS().find(processRunId));
        dto = save(dto);
        return dto;
    }

    public List<Integer> findSuccessfullUserIds(Integer processRunId) {
        return findUserIdsByStatus(processRunId, ProcessRunUserDTO.STATUS_SUCCEEDED);
    }

    public List<Integer> findFailedUserIds(Integer processRunId) {
        return findUserIdsByStatus(processRunId, ProcessRunUserDTO.STATUS_FAILED);
    }

    public Integer findSuccessfullUsersCount(Integer processRunId) {
        return findUsersCountByStatus(processRunId, ProcessRunUserDTO.STATUS_SUCCEEDED);
    }

    public Integer findFailedUsersCount(Integer processRunId) {
        return findUsersCountByStatus(processRunId, ProcessRunUserDTO.STATUS_FAILED);
    }

    private List<Integer> findUserIdsByStatus(Integer processRunId, Integer status) {
        Criteria criteria = getSession().createCriteria(ProcessRunUserDTO.class)
                .add(Restrictions.eq("status", status))
                .add(Restrictions.eq("processRun.id", processRunId))
                .setProjection(Projections.property("user.id"));

        return (List<Integer>) criteria.list();
    }

    public ProcessRunUserDTO getUser(Integer processRunId, Integer userId) {
        Criteria criteria = getSession().createCriteria(ProcessRunUserDTO.class)
                .add(Restrictions.eq("user.id", userId))
                .add(Restrictions.eq("processRun.id", processRunId));

        return (ProcessRunUserDTO) criteria.uniqueResult();
    }

    public void removeProcessRunUsersForProcessRun(Integer processRunId) {
        String hql = "DELETE FROM " + ProcessRunUserDTO.class.getSimpleName() +
                " WHERE processRun.id = :processRunId";
        Query query = getSession().createQuery(hql);
        query.setParameter("processRunId", processRunId);
        query.executeUpdate();
    }

    private Integer findUsersCountByStatus(Integer processRunId, Integer status) {
        Criteria criteria = getSession().createCriteria(ProcessRunUserDTO.class)
                .add(Restrictions.eq("status", status))
                .add(Restrictions.eq("processRun.id", processRunId))
                .setProjection(Projections.count("user.id"));

        return (Integer) criteria.uniqueResult();
    }

}
