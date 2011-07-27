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
package com.sapienter.jbilling.server.payment.event;

import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.process.IBillingProcessSessionBean;
import com.sapienter.jbilling.server.util.Context;

/*
 * The configuration needs to be done specifically for each installation/scenario
 * using the file jbilling-jms.xml
 */
public class ProcessPaymentMDB implements MessageListener {
    
    private final Logger LOG = Logger.getLogger(ProcessPaymentMDB.class);

    public void onMessage(Message message) {
        try {
            LOG.debug("Processing message. Processor " + message.getStringProperty("processor") + 
                    " entity " + message.getIntProperty("entityId") + " by " + this.hashCode());
            MapMessage myMessage = (MapMessage) message;
            
            // use a session bean to make sure the processing is done in one transaction
            IBillingProcessSessionBean process = (IBillingProcessSessionBean) 
                    Context.getBean(Context.Name.BILLING_PROCESS_SESSION);

            String type = message.getStringProperty("type"); 
            if (type.equals("payment")) {
                LOG.debug("Now processing asynch payment:" +
                        " processId: " + myMessage.getInt("processId") +
                        " runId:" + myMessage.getInt("runId") +
                        " invoiceId:" + myMessage.getInt("invoiceId"));
                Integer invoiceId = (myMessage.getInt("invoiceId") == -1) ? null : myMessage.getInt("invoiceId");
                if (invoiceId != null) {
                    // lock it
                    new InvoiceDAS().findForUpdate(invoiceId);
                }
                process.processPayment(
                        (myMessage.getInt("processId") == -1) ? null : myMessage.getInt("processId"),
                        (myMessage.getInt("runId") == -1) ? null : myMessage.getInt("runId"),
                        invoiceId);
                LOG.debug("Done");
            } else if (type.equals("ender")) {
                process.endPayments(myMessage.getInt("runId"));
            } else {
                LOG.error("Can not process message of type " + type);
            }
        } catch (Exception e) {
            LOG.error("Generating payment", e);
        }
    }

}
