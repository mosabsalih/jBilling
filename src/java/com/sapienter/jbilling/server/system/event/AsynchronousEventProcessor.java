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
package com.sapienter.jbilling.server.system.event;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.sapienter.jbilling.server.util.Context;

public abstract class AsynchronousEventProcessor<TType> extends EventProcessor<TType> {
    private static final Logger LOG = Logger.getLogger(AsynchronousEventProcessor.class); 

    protected MapMessage message;
    
    protected AsynchronousEventProcessor() {
    }
    
    public void process(final Event event) {
        JmsTemplate jmsTemplate = (JmsTemplate) Context.getBean(
                Context.Name.JMS_TEMPLATE);

        jmsTemplate.send(getDestination(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                message = session.createMapMessage();
                doProcess(event);
                message.setIntProperty("entityId", getEntityId());
                return message;
            }
        });
    }

    protected abstract void doProcess(Event event);
    protected abstract Destination getDestination();
    protected abstract int getEntityId();
}
