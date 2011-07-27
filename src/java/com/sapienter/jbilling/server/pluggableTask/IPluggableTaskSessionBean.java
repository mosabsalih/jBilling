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

package com.sapienter.jbilling.server.pluggableTask;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDTO;

/**
 *
 * This is the session facade for the invoices in general. It is a statless
 * bean that provides services not directly linked to a particular operation
 *
 * @author emilc
 * 
 * Even when using JPA, container transactions are required. This is because
 * transactional demarcation is taked from the application server.
 **/
public interface IPluggableTaskSessionBean {

    public PluggableTaskDTO getDTO(Integer typeId, Integer entityId) 
            throws SessionInternalError;

    public PluggableTaskDTO[] getAllDTOs(Integer entityId) 
            throws SessionInternalError;

    public void createParameter(Integer executorId, Integer taskId, 
            PluggableTaskParameterDTO dto);

    public void update(Integer executorId, PluggableTaskDTO dto);
    
    public PluggableTaskDTO[] updateAll(Integer executorId, 
            PluggableTaskDTO dto[]);

    public void delete(Integer executorId, Integer id);

    public void deleteParameter(Integer executorId, Integer id);

    public void updateParameters(Integer executorId, PluggableTaskDTO dto)
            throws SessionInternalError;
}
