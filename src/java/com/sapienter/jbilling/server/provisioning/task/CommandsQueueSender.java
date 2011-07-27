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

package com.sapienter.jbilling.server.provisioning.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.audit.EventLogger;

/**
 * @author othman
 * 
 */
public class CommandsQueueSender {
    private static final Logger LOG = Logger
            .getLogger(CommandsQueueSender.class);
    private EventLogger eLogger = null;
    private Integer entityId;
    private OrderDTO order;

    public CommandsQueueSender(OrderDTO order) {
        this.order = order;
        this.setEntityId(order.getUser().getCompany().getId());
        eLogger = EventLogger.getInstance();
    }

    /**
     * @param commandQueue
     */
    private void postCommand(LinkedList<StringPair> commandQueue,
            String eventType, MapMessage message) throws JMSException {
        String command = null;

        // sets command id
        UUID uid = UUID.randomUUID();

        message.setStringProperty("id", uid.toString());
        LOG.debug("set message property id=" + uid.toString());
        message.setIntProperty("entityId", getEntityId());
        LOG.debug("set message property entityId=" + getEntityId());
        message.setIntProperty("order_id", order.getId());
        LOG.debug("set message property order_id=" + order.getId());
        Integer order_line_id = null;
        // populate Message properties with command queue pairs
        for (Iterator<StringPair> it = commandQueue.iterator(); it.hasNext();) {
            StringPair param = (StringPair) it.next();

            if (param.getName().equals("command")) {
                command = param.getValue();
                LOG.debug("command: " + command);
            }

            // lookup order line associated with command
            if (param.getName().equals("order_line_id")) {
                

                try {
                    order_line_id = Integer.valueOf(param.getValue());
                } catch (NumberFormatException e) {
                }

                LOG.debug("order line id associated with command: [" + command
                        + "," + order_line_id + "]");

                if (order_line_id != null) { // found order line id associated with command
                    OrderBL order_bl = new OrderBL(order);
                    OrderLineDTO line=order_bl.getOrderLine(order_line_id);
                    LOG.debug("old order line ProvisioningRequestId: "
                            + line.getProvisioningRequestId());
                    // update order line's provisioningRequestId
                    line.setProvisioningRequestId(uid.toString());
                    LOG.debug(" updated order line ProvisioningRequestId: "
                            + line.getProvisioningRequestId());
                    // update order line's provisioningStatus
                    if (eventType.equals(ProvisioningCommandsRulesTask.ACTIVATED_EVENT_TYPE)) {
                        order_bl.setProvisioningStatus(order_line_id,
                                Constants.PROVISIONING_STATUS_PENDING_ACTIVE);

                    } else if (eventType.equals(ProvisioningCommandsRulesTask.DEACTIVATED_EVENT_TYPE)) {
                        order_bl.setProvisioningStatus(order_line_id,
                                Constants.PROVISIONING_STATUS_PENDING_INACTIVE);

                    }
                }
            }

            message.setStringProperty(param.getName(), param.getValue());
            LOG.debug("set Message property : (" + param.getName() + ","
                    + param.getValue() + ")");
        }

        Integer userId = order.getUser().getId();

        LOG.debug("adding event log messages");

        // add a log for message id
        eLogger.auditBySystem(entityId, userId, Constants.TABLE_ORDER_LINE, order_line_id, EventLogger.MODULE_PROVISIONING,
                EventLogger.PROVISIONING_UUID, null, uid.toString(), null);
        // add a log for command value
        eLogger.auditBySystem(entityId, userId, Constants.TABLE_ORDER_LINE, order_line_id, EventLogger.MODULE_PROVISIONING,
                EventLogger.PROVISIONING_COMMAND, null, command, null);

        LOG.debug("Sending message for command '" + command + "'");
    }

    /**
     * @param commands
     */
    public void postCommandsQueue(LinkedList<LinkedList<StringPair>> commands,
            final String eventType) throws JMSException {
        LOG.debug("calling postCommandsQueue()");

        if (commands == null) {
            LOG.debug("Found NULL commands queue Object");

            return;
        }

        if (commands.isEmpty()) {
            LOG.debug("Found empty commands queue. No commands to post. Returning. ");

            return;
        }

        JmsTemplate jmsTemplate = (JmsTemplate) Context.getBean(
                Context.Name.JMS_TEMPLATE);

        for (Iterator<LinkedList<StringPair>> it = commands.iterator(); it
                .hasNext();) {
            final LinkedList<StringPair> commandQueue = (LinkedList<StringPair>)
                    it.next();

            Destination destination = (Destination) Context.getBean(
                    Context.Name.PROVISIONING_COMMANDS_DESTINATION);

            jmsTemplate.send(destination, new MessageCreator() {
                public Message createMessage(Session session) 
                        throws JMSException {
                    MapMessage message = session.createMapMessage();
                    postCommand(commandQueue, eventType, message);
                    return message;
                }
            });
        }
    }

    /**
     * @return the entityId
     */
    public Integer getEntityId() {
        return entityId;
    }

    /**
     * @param entityId
     *            the entityId to set
     */
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
}
