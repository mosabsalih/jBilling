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

package com.sapienter.jbilling.server.provisioning;

import javax.jms.Message;

import com.sapienter.jbilling.common.SessionInternalError;

/**
 * @author othman
 * 
 *         This is the session facade for the provisioning process and its
 *         related services.
 */
public interface IProvisioningProcessSessionBean {
    public void trigger() throws SessionInternalError;

    public void updateProvisioningStatus(Integer in_order_id,
            Integer in_order_line_id, String result);

    public void updateProvisioningStatus(Integer orderLineId, 
            Integer provisioningStatus);

    /**
     * Runs the external provisioning code in a transation.
     */
    public void externalProvisioning(Message message);
}
