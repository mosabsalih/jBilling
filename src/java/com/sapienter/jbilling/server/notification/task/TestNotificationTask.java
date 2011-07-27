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
package com.sapienter.jbilling.server.notification.task;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.pluggableTask.NotificationTask;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.db.UserDTO;

public class TestNotificationTask extends PluggableTask implements NotificationTask {
    
	public static final ParameterDescription PARAMETER_FROM = 
		new ParameterDescription("from", false, ParameterDescription.Type.STR);
    public static final Logger LOG = Logger.getLogger(TestNotificationTask.class);
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_FROM);
    }

    

    public void deliver(UserDTO user, MessageDTO sections)
            throws TaskException {
        String directory = Util.getSysProp("base_dir");
        try {
            FileWriter writer = new FileWriter(directory + "/emails_sent.txt", true);
            
            // find the address
            ContactBL contact = new ContactBL();
            List<ContactDTOEx> emails = contact.getAll(user.getUserId());
            
            // find the from
            String from = (String) parameters.get(PARAMETER_FROM.getName());
            if (from == null || from.length() == 0) {
                from = Util.getSysProp("email_from");
            }
            
            String email = emails == null ? "No email" : emails.size() == 0 ? "No email" : emails.get(0).getEmail();
            writer.write("Date: " + Calendar.getInstance().getTime() + "\n");
            writer.write("To: " + email + "\n");
            writer.write("From: " + from + "\n");
            writer.write("Subject: " + sections.getContent()[0].getContent() + "\n");
            writer.write("Body: " + sections.getContent()[1].getContent() + "\n");
            writer.write("Attachement: " + sections.getAttachmentFile() + "\n");
            writer.write("        ----------------------        \n");
            
            writer.close();
            
            LOG.debug("Sent test notification to " + user.getUserId());
        } catch (Exception e) {
            LOG.error("Error sending test notification:" + e.getMessage(),e);
            throw new TaskException(e);
        }

    }

    public int getSections() {
        return 2;
    }
}
