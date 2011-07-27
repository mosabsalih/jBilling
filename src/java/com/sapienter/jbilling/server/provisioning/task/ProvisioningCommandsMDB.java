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

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import org.springframework.dao.EmptyResultDataAccessException;

import com.sapienter.jbilling.server.provisioning.IProvisioningProcessSessionBean;
import com.sapienter.jbilling.server.util.Context;

/*
 * The configuration needs to be done specifically for each installation/scenario
 * using the file jbilling-jms.xml
 */
public class ProvisioningCommandsMDB implements MessageListener {
    private final Logger LOG = Logger.getLogger(ProvisioningCommandsMDB.class);

    public void onMessage(Message message) {
        try {
            LOG.debug("Provisioning command MDB " + " command=" + message.getStringProperty("in_command") + "- entity="
                      + message.getIntProperty("in_entityId") + " - Processing message by  " + this.hashCode());

            MapMessage myMessage            = (MapMessage) message;
            String     in_order_line_id_str = myMessage.getStringProperty("in_order_line_id");
            String     in_order_id_str      = myMessage.getStringProperty("in_order_id");
            Integer    in_order_id          = null;
            Integer    in_order_line_id     = null;

            try {
                in_order_line_id = Integer.parseInt(in_order_line_id_str.trim());
            } catch (Exception e) {}

            LOG.debug("Message property in_order_line_id value : " + in_order_line_id);

            try {
                in_order_id = Integer.parseInt(in_order_id_str.trim());
            } catch (Exception e) {}

            LOG.debug("Message property in_order_id value : " + in_order_id);

            String result = myMessage.getStringProperty("out_result");

            LOG.debug("Message property result value : " + result);

            IProvisioningProcessSessionBean remoteProvisioning = 
                    (IProvisioningProcessSessionBean) Context.getBean(
                    Context.Name.PROVISIONING_PROCESS_SESSION);

            // try updating the order line's provisioning status
            boolean keepTrying = true;
            for (int tries = 1; keepTrying; tries++) {
                try {
                    remoteProvisioning.updateProvisioningStatus(in_order_id, 
                            in_order_line_id, result);
                    keepTrying = false;
                } catch (EmptyResultDataAccessException erdae) {
                    // order line not there yet
                    LOG.debug("Didn't find order line: " + in_order_line_id);
                    if (tries == 1) {
                        pause(100);
                    } else if (tries == 2) {
                        pause(1000);
                    } else if (tries == 3) {
                        pause(10000);
                    } else {
                        throw erdae;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("processing provisioning command", e);
        }
    }

    private void pause(long t) {
        LOG.debug("pausing for " + t + " ms...");
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }
}
