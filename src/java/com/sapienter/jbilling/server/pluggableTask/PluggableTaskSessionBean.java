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

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDTO;
import com.sapienter.jbilling.server.util.Context;

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
@Transactional( propagation = Propagation.REQUIRED )
public class PluggableTaskSessionBean implements IPluggableTaskSessionBean {

    //private static final Logger LOG = Logger.getLogger(PluggableTaskSessionBean.class);

    public PluggableTaskDTO getDTO(Integer typeId, 
            Integer entityId) throws SessionInternalError {
        try {
            PluggableTaskBL bl = new PluggableTaskBL();
            bl.set(entityId, typeId);
            return bl.getDTO();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public PluggableTaskDTO[] getAllDTOs(Integer entityId) 
            throws SessionInternalError {
            
        PluggableTaskDAS das = (PluggableTaskDAS) Context.getBean(Context.Name.PLUGGABLE_TASK_DAS);
        Collection<PluggableTaskDTO> tasks = das.findAllByEntity(entityId);

        for (PluggableTaskDTO task : tasks) {
            task.populateParamValues();
        }

        PluggableTaskDTO[] retValue = 
            new PluggableTaskDTO[tasks.size()];
        retValue = (PluggableTaskDTO[]) tasks.toArray(retValue);
        
        return retValue;
    }

    public void createParameter(Integer executorId, Integer taskId, PluggableTaskParameterDTO dto) {
            
        PluggableTaskBL bl = new PluggableTaskBL();
        bl.createParameter(taskId, dto);
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void update(Integer executorId, PluggableTaskDTO dto) {
            
        PluggableTaskBL bl = new PluggableTaskBL();
        bl.update(executorId, dto);
        
    }
    
    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public PluggableTaskDTO[] updateAll(Integer executorId, PluggableTaskDTO dto[]) {

        PluggableTaskBL bl = new PluggableTaskBL();
        for (int f = 0; f < dto.length; f++) {
            bl.update(executorId, dto[f]);
            dto[f] = bl.getDTO(); // replace with the new version
        }
        
        return dto;
    }

    public void delete(Integer executorId, Integer id) {

        PluggableTaskBL bl = new PluggableTaskBL(id);
        bl.delete(executorId);
        
    }

    public void deleteParameter(Integer executorId, Integer id) {

        PluggableTaskBL bl = new PluggableTaskBL();
        bl.deleteParameter(executorId, id);
        
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void updateParameters(Integer executorId, PluggableTaskDTO dto) 
            throws SessionInternalError {

        PluggableTaskBL bl = new PluggableTaskBL();           
        bl.updateParameters(dto);
    }
}
