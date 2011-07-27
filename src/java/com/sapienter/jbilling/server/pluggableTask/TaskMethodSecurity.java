/*
    jBilling - The Enterprise Open Source Billing System
    Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde

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
/*
package com.sapienter.jbilling.server.pluggableTask;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.PermissionConstants;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDTO;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.util.MethodBaseSecurityProxy;
import com.sapienter.jbilling.server.util.WSMethodSecurityProxy;

public class TaskMethodSecurity extends MethodBaseSecurityProxy {

    public void init(Class beanHome, Class beanRemote, Class beanLocalHome,
            Class beanLocal, Object securityMgr) throws InstantiationException {
        log = Logger.getLogger(WSMethodSecurityProxy.class);
        String methodName = null;
        try {
            Method methods[] = new Method[7];
            Method aMethod;
            int i = 0;

            // update
            Class params[] = new Class[2];
            params[0] = Integer.class;
            params[1] = PluggableTaskDTO.class;
            methodName = "update";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;
            
            // updateAll
            params = new Class[2];
            params[0] = Integer.class;
            params[1] = PluggableTaskDTO[].class;
            methodName = "updateAll";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;
            
            // create
            params = new Class[2];
            params[0] = Integer.class;
            params[1] = PluggableTaskDTO.class;
            methodName = "create";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;

            // createParameter
            params = new Class[3];
            params[0] = Integer.class;
            params[1] = Integer.class;
            params[2] = PluggableTaskParameterDTO.class;
            methodName = "createParameter";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;

            // delete
            params = new Class[2];
            params[0] = Integer.class;
            params[1] = Integer.class;
            methodName = "delete";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;

            // deleteParameter
            params = new Class[2];
            params[0] = Integer.class;
            params[1] = Integer.class;
            methodName = "deleteParameter";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;

            // updateParameters
            params = new Class[2];
            params[0] = Integer.class;
            params[1] = PluggableTaskDTO.class;
            methodName = "updateParameters";
            aMethod = beanRemote.getDeclaredMethod(methodName, params);
            methods[i++] = aMethod;

            // set the parent methods
            setMethods(methods);          

        } catch(NoSuchMethodException e) {
           String msg = "Failed to find method " + methodName;
           log.error(msg, e);
           throw new InstantiationException(msg);
        }
    }

    public void invoke(Method m, Object[] args, Object bean)
            throws SecurityException {
        if (!isMethodPresent(m)) {
            return;
        }
        // all methods for tasks need to have the executor id as the 
        // first parameter
        Integer userId = (Integer) args[0];
        // make sure this user has permisison first
        validatePermission(userId, PermissionConstants.P_TASK_MODIFY);
        if(m.getName().equals("update") ||
                m.getName().equals("updateParameters")) {
            PluggableTaskDTO dto = (PluggableTaskDTO) args[1];
            validate(userId, dto.getId());
        } else if(m.getName().equals("updateAll")) {
            PluggableTaskDTO dto[] = (PluggableTaskDTO[]) args[1];
            for (int f = 0; f < dto.length; f++) {
                validate(userId, dto[f].getId());
            }
        } else if(m.getName().equals("createParameter") || 
                m.getName().equals("delete")) {
            Integer taskId = (Integer) args[1];
            validate(userId, taskId);
        } else if(m.getName().equals("deleteParameter")) {
            Integer parameterId = (Integer) args[1];
            validateParameter(userId, parameterId);
        }
     }
*/
    
    /**
     * Validates that the given user can modify the task
     * @param userId
     * @param taskId
     */
/*
    private void validate(Integer userId, Integer taskId) {
        UserBL user = new UserBL(userId);
        PluggableTaskBL task = new PluggableTaskBL(taskId);
        if (user.getEntity().getEntity().getId() !=
                task.getDTO().getEntityId()) {
            throw new SecurityException("Unauthorize access to user " + 
                    userId);
        }
    }
    
    private void validateParameter(Integer userId, Integer parameterId) {
        PluggableTaskParameterDAS das = new PluggableTaskParameterDAS();
        validate(userId, das.find(parameterId).getTask().getId());
    }
}
*/
