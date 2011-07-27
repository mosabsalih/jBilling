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

import org.apache.log4j.Logger;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDAS;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.util.Constants;


/**
 * @author othman
 * 
 *         This is the session facade for the provisioning process and its
 *         related services.
 */
@Transactional( propagation = Propagation.REQUIRED )
public class ProvisioningProcessSessionBean 
        implements IProvisioningProcessSessionBean {
    private static final Logger LOG = Logger.getLogger(ProvisioningProcessSessionBean.class);

    public void trigger() throws SessionInternalError {
        LOG.debug("calling ProvisioningProcessSessionBean trigger() method");

        try {
            ProvisioningProcessBL processBL = new ProvisioningProcessBL();

            processBL.activateOrders();
            processBL.deActivateOrders();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updateProvisioningStatus(Integer in_order_id, Integer in_order_line_id, String result)
            throws EmptyResultDataAccessException {

        OrderDAS orderDb = new OrderDAS();
        OrderDTO order = orderDb.find(in_order_id);
        OrderBL order_bl = new OrderBL(order);

        OrderLineDAS lineDAS = new OrderLineDAS();
        OrderLineDTO order_line =  lineDAS.findForUpdate(in_order_line_id);//lineDb.findNow(in_order_line_id);

        if (order_line == null) {
            throw new EmptyResultDataAccessException("Didn't find order line: "
                    + in_order_line_id, 1);
        }
        LOG.debug("update order line : " + order_line.getId());

        if (result.equals("fail")) {
            order_bl.setProvisioningStatus(in_order_line_id,
                    Constants.PROVISIONING_STATUS_FAILED);
            LOG.debug("Provisioning status set to 'FAILED' for order line : "
                    + order_line.getId());
        } else if (result.equals("unavailable")) {
            order_bl.setProvisioningStatus(in_order_line_id,
                    Constants.PROVISIONING_STATUS_UNAVAILABLE);
            LOG.debug("Provisioning status set to 'UNAVAILABLE' for order line : "
                    + order_line.getId());
        } else if (result.equals("success")) {
            LOG.debug("order line Status before : "
                    + order_line.getProvisioningStatusId());
            if (order_line.getProvisioningStatusId().equals(
                    Constants.PROVISIONING_STATUS_PENDING_ACTIVE)) {
                order_bl.setProvisioningStatus(in_order_line_id,
                        Constants.PROVISIONING_STATUS_ACTIVE);
                LOG.debug("Provisioning status set to 'ACTIVE' for order line : "
                        + order_line.getId());
            } else if (order_line.getProvisioningStatusId().equals(
                    Constants.PROVISIONING_STATUS_PENDING_INACTIVE)) {
                order_bl.setProvisioningStatus(in_order_line_id,
                        Constants.PROVISIONING_STATUS_INACTIVE);
                LOG.debug("Provisioning status set to 'INACTIVE' for order line : "
                        + order_line.getId());
            } else {
                throw new SessionInternalError("Invalid or unexpected " + 
                        "provisioning status: " + 
                        order_line.getProvisioningStatusId());
            }
        } else {
            throw new SessionInternalError("Can not process message with " +
                    "result property value " + result);
        }
    }

    public void updateProvisioningStatus(Integer orderLineId, 
            Integer provisioningStatus) {
        OrderLineDTO orderLine = new OrderLineDAS().find(orderLineId);
        OrderBL orderBL = new OrderBL(orderLine.getPurchaseOrder());
        orderBL.setProvisioningStatus(orderLineId, provisioningStatus);
    }

    /**
     * Runs the external provisioning code in a transation.
     */
    public void externalProvisioning(Message message) {
        ExternalProvisioning provisioning = new ExternalProvisioning();
        provisioning.onMessage(message);
    }
}
