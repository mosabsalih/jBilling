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
package com.sapienter.jbilling.server.payment.tasks;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;

/**
 * A dummy task that really doesn't add parameters. Useful for those simple cases
 * where concurrent payment processing is not needed.
 *  
 * @author Emiliano Conde
 *
 */
public class NoAsyncParameters extends PluggableTask implements IAsyncPaymentParameters {

    public void addParameters(MapMessage message) throws TaskException {
        try {
            message.setStringProperty("processor", "all");
        } catch (JMSException e) {
            throw new TaskException(e);
        }
    }

}
