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
package com.sapienter.jbilling.server.pluggableTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.MessageSection;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.db.UserDTO;

/* 
 * This will send an email to the main contant of the provided user
 * It will expect two sections to compose the email message:
 * 1 - The subject
 * 2 - The body 
 * 
 * If the html parameter is true, then a third section will be expected
 * 3 - HTML body
 */
public class BasicEmailNotificationTask extends PluggableTask
        implements NotificationTask {

    // pluggable task parameters names
    public static final ParameterDescription PARAMETER_SMTP_SERVER = 
        new ParameterDescription("smtp_server", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PORT = 
    	new ParameterDescription("port", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_USERNAME = 
    	new ParameterDescription("username", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_PASSWORD = 
    	new ParameterDescription("password", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_FROM = 
        new ParameterDescription("from", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_FROM_NAME = 
    	new ParameterDescription("from_name", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_REPLYTO = 
    	new ParameterDescription("reply_to", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_BCCTO = 
    	new ParameterDescription("bcc_to", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_HTML = 
    	new ParameterDescription("html", false, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_TLS = 
    	new ParameterDescription("tls", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_SSL_AUTH = 
    	new ParameterDescription("ssl_auth", true, ParameterDescription.Type.STR);
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_BCCTO);
    	descriptions.add(PARAMETER_FROM);
    	descriptions.add(PARAMETER_FROM_NAME);
    	descriptions.add(PARAMETER_HTML);
    	descriptions.add(PARAMETER_PASSWORD);
    	descriptions.add(PARAMETER_PORT);
    	descriptions.add(PARAMETER_REPLYTO);
    	descriptions.add(PARAMETER_SMTP_SERVER);
    	descriptions.add(PARAMETER_SSL_AUTH);
    	descriptions.add(PARAMETER_TLS);
    	descriptions.add(PARAMETER_USERNAME);
    }

    private static final Logger LOG = Logger.getLogger(BasicEmailNotificationTask.class);

    // local variables
    private JavaMailSenderImpl sender = new JavaMailSenderImpl();
    private String server;
    private int port;
    private String username;
    private String password;
    private String replyTo;
    private boolean doHTML;
    private boolean tls;
    private boolean sslAuth;
    
    private void init() {
                // set some parameters
        server = (String) parameters.get(PARAMETER_SMTP_SERVER.getName());
        if (server == null || server.length() == 0) {
            server = Util.getSysProp("smtp_server");
        }

        port = Integer.parseInt(Util.getSysProp("smtp_port"));
        String strPort = String.valueOf(parameters.get(PARAMETER_PORT.getName()));
        if (strPort != null && strPort.trim().length() > 0) {
            try {
                port = Integer.valueOf(strPort).intValue();
            } catch (NumberFormatException e) {
                LOG.error("The port is not a number", e);
            }
        }
        username = (String) parameters.get(PARAMETER_USERNAME.getName());
        if (username == null || username.length() == 0) {
            username = Util.getSysProp("smtp_username");
        }
        password = (String) parameters.get(PARAMETER_PASSWORD.getName());
        if (password == null || password.length() == 0) {
            password = Util.getSysProp("smtp_password");
        }
        replyTo = (String) parameters.get(PARAMETER_REPLYTO.getName());

        doHTML = Boolean.parseBoolean((String) parameters.get(PARAMETER_HTML.getName()));

        tls = Boolean.parseBoolean((String) parameters.get(PARAMETER_TLS.getName()));

        sslAuth = Boolean.parseBoolean((String) parameters.get(PARAMETER_SSL_AUTH.getName()));
    }

    public void deliver(UserDTO user, MessageDTO message)
            throws TaskException {

        // do not process paper invoices. So far, all the rest are emails
        // This if is necessary because an entity can have some customers
        // with paper invoices and others with emal invoices.
        if (message.getTypeId().compareTo(
                MessageDTO.TYPE_INVOICE_PAPER) == 0) {
            return;
        }

        // verify that we've got the right number of sections
        MessageSection[] sections = message.getContent();
        if (sections.length < getSections()) {
            throw new TaskException("This task takes " + getSections() + " sections." +
                    sections.length + " found.");
        }


        // create the session & message
        init();
        sender.setHost(server);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setPort(port);
        if (username != null && username.length() > 0) {
            sender.getJavaMailProperties().setProperty("mail.smtp.auth", "true");
        }
        if (tls) {
            sender.getJavaMailProperties().setProperty("mail.smtp.starttls.enable", "true");
        }
        if (sslAuth) {
            // required for SMTP servers that use SSL authentication, 
            // e.g., Gmail's SMTP servers
            sender.getJavaMailProperties().setProperty(
                    "mail.smtp.socketFactory.class", 
                    "javax.net.ssl.SSLSocketFactory");
        }

        MimeMessage mimeMsg = sender.createMimeMessage();
        MimeMessageHelper msg = null; 

        // set the message's fields
        // the to address/es
        try {
            msg = new MimeMessageHelper(mimeMsg, doHTML || message.getAttachmentFile() != null);
            ContactBL contact = new ContactBL();
            List contacts = contact.getAll(user.getUserId());
            List addresses = new ArrayList<InternetAddress>();
            boolean atLeastOne = false;
            for (int f = 0; f < contacts.size(); f++) {
                ContactDTOEx record = (ContactDTOEx) contacts.get(f);
                String address = record.getEmail();
                if (record.getInclude() != null &&
                        record.getInclude().intValue() == 1 &&
                        address != null && address.trim().length() > 0) {
                    addresses.add(new InternetAddress(address, false));
                    atLeastOne = true;
                }
            }
            if (!atLeastOne) {
                // not a huge deal, but no way I can send anything
                LOG.info("User without email address " +
                        user.getUserId());
                return;
            } else {
                msg.setTo((InternetAddress[])addresses.toArray(new InternetAddress[addresses.size()]));
            }
        } catch (Exception e) {
            LOG.debug("Exception setting addresses ", e);
            throw new TaskException("Setting addresses");
        }

        // the from address
        String from = (String) parameters.get(PARAMETER_FROM.getName());
        if (from == null || from.length() == 0) {
            from = Util.getSysProp("email_from");
        }

        String fromName = (String) parameters.get(PARAMETER_FROM_NAME.getName());
        try {
            if (fromName == null || fromName.length() == 0) {
                msg.setFrom(new InternetAddress(from));
            } else {
                msg.setFrom(new InternetAddress(from, fromName));
            }
        } catch (Exception e1) {
            throw new TaskException("Invalid from address:" + from +
                    "." + e1.getMessage());
        }
        // the reply to 
        if (replyTo != null && replyTo.length() > 0) {
            try {
                msg.setReplyTo(replyTo);
            } catch (Exception e5) {
                LOG.error("Exception when setting the replyTo address: " +
                        replyTo, e5);
            }
        }
        // the bcc if specified
        String bcc = (String) parameters.get(PARAMETER_BCCTO.getName());
        if (bcc != null && bcc.length() > 0) {
            try {
                msg.setBcc(new InternetAddress(bcc, false));
            } catch (AddressException e5) {
                LOG.warn("The bcc address " + bcc + " is not valid. " +
                        "Sending without bcc", e5);
            } catch (MessagingException e5) {
                throw new TaskException("Exception setting bcc " +
                        e5.getMessage());
            }
        }

        // the subject and body
        try {
            msg.setSubject(sections[0].getContent());
            if (doHTML) {
                // both are sent as alternatives
                msg.setText(sections[1].getContent(), sections[2].getContent());
            } else {
                // only plain text
                msg.setText(sections[1].getContent());
            }
            if (message.getAttachmentFile() != null) {
                File file = (File) new File(message.getAttachmentFile());

                msg.addAttachment(file.getName(), new FileSystemResource(file));
                LOG.debug("added attachment " + file.getName());
            }
        } catch (MessagingException e2) {
            throw new TaskException("Exception setting up the subject and/or" +
                    " body." + e2.getMessage());
        }
        // the date
        try {
            msg.setSentDate(Calendar.getInstance().getTime());
        } catch (MessagingException e3) {
            throw new TaskException("Exception setting up the date" +
                    "." + e3.getMessage());
        }

        // send the message
        try {
            String allEmails = "";
            for (Address address : msg.getMimeMessage().getRecipients(Message.RecipientType.TO)) {
                allEmails = allEmails + " " + address.toString();
            }
            LOG.debug(
                    "Sending email to " + allEmails + " bcc " + bcc + " server=" + server +
                    " port=" + port + " username=" + username + " password=" +
                    password);
            sender.send(mimeMsg);
            //if there was an attachment, remove the file
            if (message.getAttachmentFile() != null) {
                File file = new File(message.getAttachmentFile());
                if (!file.delete()) {
                    LOG.debug("Could not delete attachment file " +
                            file.getName());
                }
            }
        } catch (Throwable e4) { // need to catch a messaging exception plus spring's runtimes
            LOG.warn("Error sending email", e4);
            // send an emial to the entity to let it know about the failure
            try {
                String params[] = new String[6]; // five parameters for this message;
                params[0] = (e4.getMessage() == null ? "No detailed exception message" : e4.getMessage());
                params[1] = "";
                for (Address address : msg.getMimeMessage().getAllRecipients()) {
                    params[1] = params[1] + " " + address.toString();
                }
                params[2] = server;
                params[3] = port + " ";
                params[4] = username;
                params[5] = password;

                NotificationBL.sendSapienterEmail(user.getEntity().getId(),
                        "notification.email.error", null, params);

            } catch (Exception e5) {
                LOG.warn("Exception sending error message to entity", e5);
            }
            throw new TaskException("Exception sending the message" +
                    "." + e4.getMessage());
        }
    }

    public int getSections() {
        init();
        return doHTML ? 3 : 2;
    }
}
