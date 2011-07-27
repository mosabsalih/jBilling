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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.provisioning.event.SubscriptionActiveEvent;
import com.sapienter.jbilling.server.provisioning.event.SubscriptionInactiveEvent;
import com.sapienter.jbilling.server.system.event.EventManager;


/**
 * @author othman
 *
 */
public class ProvisioningProcessBL {
    private static final Logger LOG = Logger.getLogger(ProvisioningProcessBL.class);

    /**
     * calls method activateOrder(OrderDTO) For each order where the condition OrderDTO.activeSince <= today <
     * OrderDTO.activeUntil is true
     */
    public void activateOrders() {
        OrderDAS orders = new OrderDAS();

        // get list of orders where OrderDTO.activeSince <= today <OrderDTO.activeUntil
        List<OrderDTO> toActivateOrders = orders.findToActivateOrders();

        if (toActivateOrders == null) {
            LOG.debug("toActivate orders list =null");

            return;
        }

        for (Iterator<OrderDTO> it = toActivateOrders.iterator(); it.hasNext(); ) {
            OrderDTO toActivateOrder = (OrderDTO) it.next();

            this.activateOrder(toActivateOrder);
        }
    }

    /**
     * calls method deActivateOrder(OrderDTO) For each order where the condition OrderDTO.activeSince > today ||
     * OrderDTO.activeUntil <= today is true
     */
    public void deActivateOrders() {
        OrderDAS orders = new OrderDAS();

        // get list of orders where OrderDTO.activeSince > today || OrderDTO.activeUntil <= today
        List<OrderDTO> toDeActivateOrders = orders.findToDeActiveOrders();

        if (toDeActivateOrders == null) {
            LOG.debug("toDeActivate orders list =null");

            return;
        }

        for (Iterator<OrderDTO> it = toDeActivateOrders.iterator(); it.hasNext(); ) {
            OrderDTO toDeActivateOrder = (OrderDTO) it.next();

            this.deActivateOrder(toDeActivateOrder);
        }
    }

    /**
     * each order line will be checked to see if any OrderLineDTO.provisioningStatus == PROVISIONING_STATUS_INACTIVE.
     *  If there are any matches, generate a SubscriptionActiveEvent on that order
     * @param order
     */
    private void activateOrder(OrderDTO order) {
        LOG.debug("active Order " + order.getId());

        boolean            doActivate = false;
        List<OrderLineDTO> orderLines = order.getLines();

        if (orderLines == null) {
            return;
        }

        for (Iterator<OrderLineDTO> it = orderLines.iterator(); it.hasNext(); ) {
            OrderLineDTO line = (OrderLineDTO) it.next();

            if ((line != null) && (line.getProvisioningStatusId() != null)
                    && line.getProvisioningStatusId().equals(Constants.PROVISIONING_STATUS_INACTIVE)) {
                LOG.debug(line + ": order line status is PROVISIONING_STATUS_INACTIVE-> Activate it!");
                doActivate = true;

                break;
            }
        }

        if (doActivate) {

            // generate SubscriptionActiveEvent on order
            Integer                 entityId = order.getUser().getCompany().getId();
            SubscriptionActiveEvent newEvent = new SubscriptionActiveEvent(entityId, order);

            EventManager.process(newEvent);
            LOG.debug("generated SubscriptionActiveEvent for order: " + order);
        }
    }

    /**
     * each order line will be checked to see if any
     * OrderLineDTO.provisioningStatus == PROVISIONING_STATUS_ACTIVE.
     * If there are any matches, generate a SubscriptionInactiveEvent on that order.
     * @param order
     */
    private void deActivateOrder(OrderDTO order) {
        LOG.debug("inactive Order " + order.getId());

        boolean            doInActivate = false;
        List<OrderLineDTO> orderLines   = order.getLines();

        if (orderLines == null) {
            return;
        }

        for (Iterator<OrderLineDTO> it = orderLines.iterator(); it.hasNext(); ) {
            OrderLineDTO line = (OrderLineDTO) it.next();

            if (line == null) {
                continue;
            }

            if ((line != null) && (line.getProvisioningStatusId() != null)
                    && line.getProvisioningStatusId().equals(Constants.PROVISIONING_STATUS_ACTIVE)) {
                LOG.debug(line + ": order line status is PROVISIONING_STATUS_ACTIVE-> DeActivate it!");
                doInActivate = true;

                break;
            }
        }

        if (doInActivate) {

            // generate SubscriptionInActiveEvent on order
            Integer                   entityId = order.getUser().getCompany().getId();
            SubscriptionInactiveEvent newEvent = new SubscriptionInactiveEvent(entityId, order);

            EventManager.process(newEvent);
            LOG.debug("generated SubscriptionInActiveEvent for order: " + order);
        }
    }
}
