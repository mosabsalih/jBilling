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

package com.sapienter.jbilling.server.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentSet;

import org.springframework.jdbc.datasource.DataSourceUtils;
import javax.sql.rowset.CachedRowSet;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.db.NotificationMessageDAS;
import com.sapienter.jbilling.server.notification.db.NotificationMessageDTO;
import com.sapienter.jbilling.server.notification.db.NotificationMessageLineDAS;
import com.sapienter.jbilling.server.notification.db.NotificationMessageLineDTO;
import com.sapienter.jbilling.server.notification.db.NotificationMessageSectionDAS;
import com.sapienter.jbilling.server.notification.db.NotificationMessageSectionDTO;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.pluggableTask.NotificationTask;
import com.sapienter.jbilling.server.pluggableTask.PaperInvoiceNotificationTask;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.partner.PartnerBL;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.Util;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.dao.EmptyResultDataAccessException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.SortTool;
import org.apache.velocity.tools.generic.IteratorTool;



public class NotificationBL extends ResultList implements NotificationSQL {
    //
    private NotificationMessageDAS messageDas = null;
    private NotificationMessageDTO messageRow = null;
    private NotificationMessageSectionDAS messageSectionHome = null;
    private NotificationMessageLineDAS messageLineHome = null;
    private static final Logger LOG = Logger.getLogger(NotificationBL.class);

    public NotificationBL(Integer messageId)  {
        init();
        LOG.debug("Constructor ...");
        messageRow = messageDas.find(messageId);
    }

    public NotificationBL() {
        init();
    }

    private void init() {

        messageDas = new NotificationMessageDAS();
        messageSectionHome = new NotificationMessageSectionDAS();
        messageLineHome = new NotificationMessageLineDAS();
    }

    public NotificationMessageDTO getEntity() {
        return messageRow;
    }

    public void set(Integer type, Integer languageId, Integer entityId) {
        messageRow = messageDas.findIt(type, entityId, languageId);
        
    }

    public MessageDTO getDTO() throws SessionInternalError {
        MessageDTO retValue = new MessageDTO();

        retValue.setLanguageId(messageRow.getLanguage().getId());
        retValue.setTypeId(messageRow.getNotificationMessageType().getId());
        retValue.setUseFlag(new Boolean(messageRow.getUseFlag() == 1));

        setContent(retValue);

        return retValue;
    }

    public Integer createUpdate(Integer entityId, MessageDTO dto) {

        set(dto.getTypeId(), dto.getLanguageId(), entityId);
        // it's just so easy to delete cascade and recreate ...:D
        if (messageRow != null) {
            messageDas.delete(messageRow);
        }

        messageRow = messageDas.create(dto.getTypeId(), entityId, dto
                .getLanguageId(), dto.getUseFlag());

        // add the sections with the lines to the message entity
        for (int f = 0; f < dto.getContent().length; f++) {
            MessageSection section = dto.getContent()[f];

            // create the section bean
            NotificationMessageSectionDTO sectionBean = messageSectionHome
                    .create(section.getSection());
            int index = 0;
            while (index < section.getContent().length()) {
                String line;
                if (index + MessageDTO.LINE_MAX.intValue() <= section
                        .getContent().length()) {
                    line = section.getContent().substring(index,
                            index + MessageDTO.LINE_MAX.intValue());
                } else {
                    line = section.getContent().substring(index);
                }
                index += MessageDTO.LINE_MAX.intValue();

                NotificationMessageLineDTO nml = messageLineHome.create(line);
                nml.setNotificationMessageSection(sectionBean);
                sectionBean.getNotificationMessageLines().add(nml);

            }
            sectionBean.setNotificationMessage(messageRow);

            messageRow.getNotificationMessageSections().add(sectionBean);

        }

        PersistentSet msjs = (PersistentSet) messageRow
                .getNotificationMessageSections();
        NotificationMessageSectionDTO nnnn = ((NotificationMessageSectionDTO) msjs
                .toArray()[0]);
        PersistentSet nm = (PersistentSet) nnnn.getNotificationMessageLines();
        messageDas.save(messageRow);

        return messageRow.getId();
    }

    /*
     * Getters. These provide easy generation of messages by their type. So each
     * getter kows which type will generate, and gets as parameters the
     * information to generate that particular type of message.
     */

    public MessageDTO[] getInvoiceMessages(Integer entityId, Integer processId,
            Integer languageId, InvoiceDTO invoice)
            throws SessionInternalError, NotificationNotFoundException {
        MessageDTO retValue[] = null;
        Integer deliveryMethod;
        // now see what kind of invoice this customers wants
        if (invoice.getBaseUser().getCustomer() == null) {
            // this shouldn't be necessary. The only reason is here is
            // because the test data has invoices for root users. In
            // reality, all users that will get an invoice have to be
            // customers
            deliveryMethod = Constants.D_METHOD_EMAIL;
            LOG.warn("A user that is not a customer is getting an invoice."
                    + " User id = " + invoice.getBaseUser().getUserId());
        } else {
            deliveryMethod = invoice.getBaseUser().getCustomer()
                    .getInvoiceDeliveryMethod().getId();
        }

        int index = 0;
        if (deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
            retValue = new MessageDTO[2];
        } else {
            retValue = new MessageDTO[1];
        }
        if (deliveryMethod.equals(Constants.D_METHOD_EMAIL)
                || deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
            retValue[index] = getInvoiceEmailMessage(entityId, languageId,
                    invoice);
            index++;
        }

        if (deliveryMethod.equals(Constants.D_METHOD_PAPER)
                || deliveryMethod.equals(Constants.D_METHOD_EMAIL_AND_PAPER)) {
            retValue[index] = getInvoicePaperMessage(entityId, processId,
                    languageId, invoice);
            index++;
        }

        return retValue;
    }

    public MessageDTO getInvoicePaperMessage(Integer entityId,
            Integer processId, Integer languageId, InvoiceDTO invoice)
            throws SessionInternalError {
        MessageDTO retValue = new MessageDTO();

        retValue.setTypeId(MessageDTO.TYPE_INVOICE_PAPER);
        retValue.setDeliveryMethodId(Constants.D_METHOD_PAPER);

        // put the whole invoice as a parameter
        InvoiceBL invoiceBl = new InvoiceBL(invoice);
        InvoiceDTO invoiceDto = invoiceBl.getDTOEx(languageId, true);
        retValue.getParameters().put("invoiceDto", invoiceDto);
        // the process id is needed to maintain the batch record
        if (processId != null) {
            // single pdf invoices for the web-based app can ignore this
            retValue.getParameters().put("processId", processId);
        }
        try {
            setContent(retValue, MessageDTO.TYPE_INVOICE_PAPER, entityId,
                    languageId);
        } catch (NotificationNotFoundException e1) {
            // put blanks
            MessageSection sectionContent = new MessageSection(new Integer(1),
                    null);
            retValue.addSection(sectionContent);
            sectionContent = new MessageSection(new Integer(2), null);
            retValue.addSection(sectionContent);
        }

        return retValue;
    }

    public MessageDTO getPaymentMessage(Integer entityId, PaymentDTOEx dto,
            boolean result) throws SessionInternalError,
            NotificationNotFoundException {
        UserBL user = null;
        Integer languageId = null;
        MessageDTO message = initializeMessage(entityId, dto.getUserId());
        message.setTypeId(result ? MessageDTO.TYPE_PAYMENT : new Integer(
                MessageDTO.TYPE_PAYMENT.intValue() + 1));

        user = new UserBL(dto.getUserId());
        languageId = user.getEntity().getLanguageIdField();
        setContent(message, message.getTypeId(), entityId, languageId);

        // find the description for the payment method
        PaymentBL payment = new PaymentBL();
        message.addParameter("method", payment.getMethodDescription(dto
                .getPaymentMethod(), languageId));
        message.addParameter("total", Util.formatMoney(dto.getAmount(), dto
                .getUserId(), dto.getCurrency().getId(), true));

        message.addParameter("payment", payment.getEntity());
        // find an invoice in the list of invoices id
        if (dto.getInvoiceIds() != null && dto.getInvoiceIds().size() > 0) {
            Integer invoiceId = (Integer) dto.getInvoiceIds().get(0);
            InvoiceBL invoice = new InvoiceBL(invoiceId);
            message.addParameter("invoice_number", invoice.getEntity()
                    .getPublicNumber().toString());
            message.addParameter("invoice", invoice.getEntity());
        }
        message.addParameter("payment", dto);

        return message;
    }

    public MessageDTO getInvoiceRemainderMessage(Integer entityId,
            Integer userId, Integer days, Date dueDate, String number,
            BigDecimal total, Date date, Integer currencyId)
            throws SessionInternalError, NotificationNotFoundException {
        UserBL user = null;
        Integer languageId = null;
        MessageDTO message = initializeMessage(entityId, userId);
        message.setTypeId(MessageDTO.TYPE_INVOICE_REMINDER);

        user = new UserBL(userId);
        languageId = user.getEntity().getLanguageIdField();
        setContent(message, message.getTypeId(), entityId, languageId);

        message.addParameter("days", days.toString());
        message.addParameter("dueDate", Util.formatDate(dueDate, userId));
        message.addParameter("number", number);
        message.addParameter("total", Util.formatMoney(total, userId,
                currencyId, true));
        message.addParameter("date", Util.formatDate(date, userId));


        return message;
    }

    public MessageDTO getForgetPasswordEmailMessage(Integer entityId,
            Integer userId, Integer languageId) throws SessionInternalError,
            NotificationNotFoundException {
        MessageDTO message = initializeMessage(entityId, userId);

        message.setTypeId(MessageDTO.TYPE_FORGETPASSWORD_EMAIL);

        setContent(message, MessageDTO.TYPE_FORGETPASSWORD_EMAIL, entityId,
                languageId);

        return message;
    }

    public MessageDTO getInvoiceEmailMessage(Integer entityId,
            Integer languageId, InvoiceDTO invoice)
            throws SessionInternalError, NotificationNotFoundException {
        MessageDTO message = initializeMessage(entityId, invoice.getBaseUser()
                .getUserId());

        message.setTypeId(MessageDTO.TYPE_INVOICE_EMAIL);

        setContent(message, MessageDTO.TYPE_INVOICE_EMAIL, entityId,
                languageId);

        message.addParameter("total", Util.formatMoney(invoice.getTotal(),
                invoice.getBaseUser().getUserId(), invoice.getCurrency().getId(), true));
        message.addParameter("id", invoice.getId() + "");
        message.addParameter("number", invoice.getPublicNumber());
        // format the date depending of the customers locale

        message.addParameter("due_date", Util.formatDate(invoice.getDueDate(),
                invoice.getBaseUser().getUserId()));
        String notes = invoice.getCustomerNotes();
        
        message.addParameter("notes", notes);
        message.addParameter("invoice", invoice);

        // if the entity has the preference of pdf attachment, do it
        try {
            PreferenceBL pref = new PreferenceBL();

            try {
                pref.set(entityId, Constants.PREFERENCE_PDF_ATTACHMENT);
            } catch (EmptyResultDataAccessException e1) {
                // no problem, I'll get the defaults
            }
            if (pref.getInt() == 1) {
                message.setAttachmentFile(generatePaperInvoiceAsFile(invoice));
                LOG.debug("Setted attachement " + message.getAttachmentFile());
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return message;
    }

    public MessageDTO getAgeingMessage(Integer entityId, Integer languageId,
            Integer statusId, Integer userId) throws SessionInternalError,
            NotificationNotFoundException {
        MessageDTO message = initializeMessage(entityId, userId);
        message.setTypeId(new Integer(MessageDTO.TYPE_AGEING.intValue()
                + statusId.intValue() - 1));

        try {
            setContent(message, message.getTypeId(), entityId, languageId);
            UserBL user = new UserBL(userId);
            InvoiceBL invoice = new InvoiceBL();
            Integer invoiceId = invoice.getLastByUser(userId);
            if (invoiceId != null) {
                invoice.set(invoiceId);

                message.addParameter("total", Util.decimal2string(invoice.getEntity().getBalance(), user.getLocale()));
                message.addParameter("invoice", invoice.getEntity());
            } else {
                LOG.warn("user " + userId + " has no invoice but an ageing "
                        + "message is being sent");
            }
        } catch (SQLException e1) {
            throw new SessionInternalError(e1);
        }

        return message;
    }

    public MessageDTO getOrderNotification(Integer entityId, Integer step,
            Integer languageId, Date activeSince, Date activeUntil,
            Integer userId, BigDecimal total, Integer currencyId)
            throws  SessionInternalError,
            NotificationNotFoundException {
        MessageDTO retValue = initializeMessage(entityId, userId);
        retValue.setTypeId(new Integer(MessageDTO.TYPE_ORDER_NOTIF.intValue()
                + step.intValue() - 1));
        try {
            setContent(retValue, retValue.getTypeId(), entityId, languageId);
            Locale locale;
            try {
                UserBL user = new UserBL(userId);
                locale = user.getLocale();
            } catch (Exception e) {
                throw new SessionInternalError(e);
            }
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "entityNotifications", locale);
            SimpleDateFormat formatter = new SimpleDateFormat(bundle
                    .getString("format.date"));

            retValue
                    .addParameter("period_start", formatter.format(activeSince));
            retValue.addParameter("period_end", formatter.format(activeUntil));
            retValue.addParameter("total", Util.formatMoney(total, userId,
                    currencyId, true));
        } catch (ClassCastException e) {
            throw new SessionInternalError(e);
        } 

        return retValue;
    }

    public MessageDTO getPayoutMessage(Integer entityId, Integer languageId, BigDecimal total, Date startDate,
                                       Date endDate, boolean clerk, Integer partnerId)
            throws SessionInternalError, NotificationNotFoundException {

        MessageDTO message = new MessageDTO();
        if (!clerk) {
            message.setTypeId(MessageDTO.TYPE_PAYOUT);
        } else {
            message.setTypeId(MessageDTO.TYPE_CLERK_PAYOUT);
        }

        try {
            EntityBL en = new EntityBL(entityId);

            setContent(message, message.getTypeId(), entityId, languageId);
            message.addParameter("total", Util.decimal2string(total, en.getLocale()));

            message.addParameter("company", new CompanyDAS().find(entityId)
                    .getDescription());
            PartnerBL partner = new PartnerBL(partnerId);

            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            message.addParameter("period_end", Util.formatDate(cal.getTime(),
                    partner.getEntity().getUser().getUserId()));
            cal.setTime(startDate);
            message.addParameter("period_start", Util.formatDate(cal.getTime(),
                    partner.getEntity().getUser().getUserId()));
            message.addParameter("partner_id", partnerId.toString());
        } catch (ClassCastException e) {
            throw new SessionInternalError(e);
        }
        
        return message;
    }

    public MessageDTO getCreditCardMessage(Integer entityId,
            Integer languageId, Integer userId, CreditCardDTO creditCard)
            throws SessionInternalError, 
            NotificationNotFoundException {
        MessageDTO message = initializeMessage(entityId, userId);
        message.setTypeId(MessageDTO.TYPE_CREDIT_CARD);

        setContent(message, message.getTypeId(), entityId, languageId);
        SimpleDateFormat format = new SimpleDateFormat("MM/yy");
        message.addParameter("expiry_date", format.format(creditCard
                .getCcExpiry()));

        return message;
    }

    private void setContent(MessageDTO newMessage, Integer type,
            Integer entity, Integer language) throws SessionInternalError,
            NotificationNotFoundException {
        set(type, language, entity);
        if (messageRow != null) {
            if (messageRow.getUseFlag() == 0) {
                // if (messageRow.getUseFlag().intValue() == 0) {
                throw new NotificationNotFoundException("Notification " + "flaged for not use");
            }
            setContent(newMessage);
        } else {
            String message = "Looking for notification message type " + type + " for entity " + 
                    entity + " language " + language + " but could not find it. This entity has " +
                    "to specify " + "this notification message.";
            LOG.warn(message);
            throw new NotificationNotFoundException(message);
        }

    }

    private void setContent(MessageDTO newMessage) throws SessionInternalError {

        // go through the sections
        Collection sections = messageRow.getNotificationMessageSections();
        for (Iterator it = sections.iterator(); it.hasNext();) {
            NotificationMessageSectionDTO section = (NotificationMessageSectionDTO) it
                    .next();
            // then through the lines of this section
            StringBuffer completeLine = new StringBuffer();
            Collection lines = section.getNotificationMessageLines();
            int checkOrder = 0; // there's nothing to assume that the lines
            // will be retrived in order, but the have to!
            List vLines = new ArrayList<NotificationMessageSectionDTO>(lines);
            Collections.sort(vLines, new NotificationLineEntityComparator());
            for (Iterator it2 = vLines.iterator(); it2.hasNext();) {
                NotificationMessageLineDTO line = (NotificationMessageLineDTO) it2
                        .next();
                if (line.getId() <= checkOrder) {
                    // if (line.getId().intValue() <= checkOrder) {
                    LOG.error("Lines have to be retreived in order. "
                            + "See class java.util.TreeSet for solution or "
                            + "Collections.sort()");
                    throw new SessionInternalError("Lines have to be "
                            + "retreived in order.");
                } else {
                    checkOrder = line.getId();
                    // checkOrder = line.getId().intValue();
                }
                completeLine.append(line.getContent());
            }
            // add the content of this section to the message
            MessageSection sectionContent = new MessageSection(section
                    .getSection(), completeLine.toString());
            newMessage.addSection(sectionContent);
        }
    }

    static public String parseParameters(String content, HashMap parameters) {
        
        // get the engine from Spring
        VelocityEngine velocity = (VelocityEngine) Context.getBean(Context.Name.VELOCITY);
        VelocityContext velocityContext = new VelocityContext(parameters);
        StringWriter result = new StringWriter();
        
        try {
            velocity.evaluate(velocityContext, result, "Error template as string?", content);
        } catch (Exception e) {
            throw new SessionInternalError("Rendering email", NotificationBL.class, e);
        }

        return result.toString();

    }

    /**
     * A rather expensive call for what it achieves. It looks suitable for caching, but then
     * it is rarely called (only from the GUI)... and then the orm cache helps too.
     * @param entityId
     * @return
     */
    public int getSections(Integer entityId) {
        int higherSection = 0;
        try {
            PluggableTaskManager taskManager =
                    new PluggableTaskManager(
                    entityId,
                    Constants.PLUGGABLE_TASK_NOTIFICATION);
            NotificationTask task =
                    (NotificationTask) taskManager.getNextClass();

            while (task != null) {
                if (task.getSections() > higherSection) {
                    higherSection = task.getSections();
                }

                task = (NotificationTask) taskManager.getNextClass();
            }
        } catch (Exception e) {
            throw new SessionInternalError("Finding number of sections for notificaitons", 
                    NotificationBL.class, e);
        }
        return higherSection;
    }

    public CachedRowSet getTypeList(Integer languageId) throws SQLException,
            Exception {

        prepareStatement(NotificationSQL.listTypes);
        cachedResults.setInt(1, languageId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }

    public String getEmails(String separator, Integer entityId)
            throws SQLException {
        StringBuffer retValue = new StringBuffer();
        conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();
        PreparedStatement stmt = conn
                .prepareStatement(NotificationSQL.allEmails);
        stmt.setInt(1, entityId.intValue());
        ResultSet res = stmt.executeQuery();
        boolean first = true;

        while (res.next()) {
            if (first) {
                first = false;
            } else {
                retValue.append(separator);
            }
            retValue.append(res.getString(1));
        }

        res.close();
        stmt.close();
        conn.close();

        return retValue.toString();
    }

    public static byte[] generatePaperInvoiceAsStream(String design, 
            boolean useSqlQuery, InvoiceDTO invoice, ContactDTOEx from, 
            ContactDTOEx to, String message1, String message2, Integer entityId,
            String username, String password) throws FileNotFoundException,
            SessionInternalError {
        JasperPrint report = generatePaperInvoice(design, useSqlQuery, invoice,
                from, to, message1, message2, entityId, username, password);
        try {
            return JasperExportManager.exportReportToPdf(report);
        } catch (JRException e) {
            LOG.error("Exception generating paper invoice", e);
            return null;
        }
    }

    public static String generatePaperInvoiceAsFile(String design, 
            boolean useSqlQuery, InvoiceDTO invoice, ContactDTOEx from, 
            ContactDTOEx to, String message1, String message2, Integer entityId,
            String username, String password) throws FileNotFoundException,
            SessionInternalError {

        JasperPrint report = generatePaperInvoice(design, useSqlQuery, invoice, from, to, message1, message2, entityId,
                                                  username, password);

        String fileName = null;
        try {
            fileName = com.sapienter.jbilling.common.Util
                    .getSysProp("base_dir")
                       + "invoices/"
                       + entityId
                       + "-"
                       + invoice.getId()
                       + "-invoice.pdf";

            JasperExportManager.exportReportToPdfFile(report, fileName);
        } catch (JRException e) {
            LOG.error("Exception generating paper invoice", e);
        }
        return fileName;
    }

    private static JasperPrint generatePaperInvoice(String design, 
            boolean useSqlQuery, InvoiceDTO invoice, ContactDTOEx from, 
            ContactDTOEx to, String message1, String message2, Integer entityId,
            String username, String password) throws FileNotFoundException,
            SessionInternalError {
        try {
            // This is needed for JasperRerpots to work, for some twisted XWindows issue
            System.setProperty("java.awt.headless", "true");
            String designFile = com.sapienter.jbilling.common.Util.getSysProp("base_dir")
                                + "designs/" + design + ".jasper";

            File compiledDesign = new File(designFile);
            LOG.debug("Generating paper invoice with design file : " + designFile);
            FileInputStream stream = new FileInputStream(compiledDesign);
            Locale locale = (new UserBL(invoice.getUserId())).getLocale();

            // add all the invoice data
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("invoiceNumber", invoice.getPublicNumber());
            parameters.put("invoiceId", invoice.getId());
            parameters.put("entityName", printable(from.getOrganizationName()));
            parameters.put("entityAddress", printable(from.getAddress1()));
            parameters.put("entityAddress2", printable(from.getAddress2()));
            parameters.put("entityPostalCode", printable(from.getPostalCode()));
            parameters.put("entityCity", printable(from.getCity()));
            parameters.put("entityProvince", printable(from.getStateProvince()));
            parameters.put("customerOrganization", printable(to.getOrganizationName()));
            parameters.put("customerName", printable(to.getFirstName(), to.getLastName()));
            parameters.put("customerAddress", printable(to.getAddress1()));
            parameters.put("customerAddress2", printable(to.getAddress2()));
            parameters.put("customerPostalCode", printable(to.getPostalCode()));
            parameters.put("customerCity", printable(to.getCity()));
            parameters.put("customerProvince", printable(to.getStateProvince()));
            parameters.put("customerUsername", username);
            parameters.put("customerPassword", password);
            parameters.put("customerId", invoice.getUserId().toString());
            parameters.put("invoiceDate", Util.formatDate(invoice.getCreateDatetime(), invoice.getUserId()));
            parameters.put("invoiceDueDate", Util.formatDate(invoice.getDueDate(), invoice.getUserId()));

            // customer message
            LOG.debug("m1 = " + message1 + " m2 = " + message2);
            parameters.put("customerMessage1", printable(message1));
            parameters.put("customerMessage2", printable(message2));

            // invoice notes stripped of html line breaks
            String notes = invoice.getCustomerNotes();
            if (notes != null) {
                notes = notes.replaceAll("<br/>", "\r\n");
            }
            parameters.put("notes", notes);

            // now some info about payments
            try {
                InvoiceBL invoiceBL = new InvoiceBL(invoice.getId());
                try {
                    parameters.put("paid", Util.formatMoney(invoiceBL
                            .getTotalPaid(), invoice.getUserId(), invoice
                            .getCurrency().getId(), false));
                    // find the previous invoice and its payment for extra info
                    invoiceBL.setPrevious();
                    parameters.put("prevInvoiceTotal", Util.formatMoney(
                            invoiceBL.getEntity().getTotal(), invoice
                                    .getUserId(), invoice.getCurrency().getId(),
                            false));
                    parameters.put("prevInvoicePaid", Util.formatMoney(invoiceBL.getTotalPaid(), invoice
                                    .getUserId(), invoice.getCurrency().getId(),
                            false));
                } catch (EmptyResultDataAccessException e1) {
                    parameters.put("prevInvoiceTotal", "0");
                    parameters.put("prevInvoicePaid", "0");
                }

            } catch (Exception e) {
                LOG.error("Exception generating paper invoice", e);
                return null;
            }

            // add all the custom contact fields
            // the from
            for (Iterator it = from.getFieldsTable().values().iterator(); it
                    .hasNext();) {
                ContactFieldDTO field = (ContactFieldDTO) it.next();
                String fieldName = field.getType().getPromptKey();
                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                parameters.put("from_custom_" + fieldName, field.getContent());
            }
            for (Iterator it = to.getFieldsTable().values().iterator(); it
                    .hasNext();) {
                ContactFieldDTO field = (ContactFieldDTO) it.next();
                String fieldName = field.getType().getPromptKey();
                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                parameters.put("to_custom_" + fieldName, field.getContent());
            }

            // the logo is a file
            File logo = new File(com.sapienter.jbilling.common.Util
                    .getSysProp("base_dir")
                    + "logos/entity-" + entityId + ".jpg");
            parameters.put("entityLogo", logo);

            // the invoice lines go as the data source for the report
            // we need to extract the taxes from them, put the taxes as
            // an independent parameter, and add the taxes rates as more
            // parameters
            BigDecimal taxTotal = new BigDecimal(0);
            int taxItemIndex = 0;
            // I need a copy, so to not affect the real invoice
            List<InvoiceLineDTO> lines = new ArrayList<InvoiceLineDTO>(invoice.getInvoiceLines());
           // Collections.copy(lines, invoice.getInvoiceLines());

            List<InvoiceLineDTO> linesRemoved = new ArrayList<InvoiceLineDTO>();
            for (InvoiceLineDTO line: lines) {
                // log.debug("Processing line " + line);
                // process the tax, if this line is one
                if (line.getInvoiceLineType() != null && // for headers/footers
                        line.getInvoiceLineType().getId() == 
                                Constants.INVOICE_LINE_TYPE_TAX) {
                    // update the total tax variable
                    taxTotal = taxTotal.add(line.getAmount());
                    // add the tax amount as an array parameter
                    parameters.put("taxItem_" + taxItemIndex, Util.decimal2string(line.getPrice(), locale));
                    taxItemIndex++;
                    // taxes are not displayed as invoice lines
                    linesRemoved.add(line); // can't do lines.remove(): ConcurrentModificationException
                } else if (line.getIsPercentage() != null && line.getIsPercentage().intValue() == 1) {
                    // if the line is a percentage, remove the price
                    line.setPrice(null);
                }
            }
            lines.removeAll(linesRemoved); // removed them once out of the loop. Otherwise it will throw
            // remove the last line, that is the total footer
            lines.remove(lines.size() - 1);
            // now add the tax
            parameters.put("tax", Util.formatMoney(taxTotal, invoice.getUserId(), invoice
                    .getCurrency().getId(), false));
            parameters.put("totalWithTax", Util.formatMoney(invoice.getTotal(),
                    invoice.getUserId(), invoice.getCurrency().getId(), false));
            parameters.put("totalWithoutTax", Util.formatMoney(invoice.getTotal().subtract(taxTotal),
                    invoice.getUserId(), invoice.getCurrency().getId(), false));
            parameters.put("balance", Util.formatMoney(invoice.getBalance(),
                    invoice.getUserId(), invoice.getCurrency().getId(), false));
            parameters.put("carriedBalance", Util.formatMoney(invoice.getCarriedBalance(),
                    invoice.getUserId(), invoice.getCurrency().getId(), false));

            LOG.debug("Parameter tax = " + parameters.get("tax")
                    + " totalWithTax = " + parameters.get("totalWithTax")
                    + " totalWithoutTax = " + parameters.get("totalWithoutTax")
                    + " balance = " + parameters.get("balance"));

            // set report locale
            parameters.put(JRParameter.REPORT_LOCALE, locale);

            // set the subreport directory
            String subreportDir = com.sapienter.jbilling.common.Util
                .getSysProp("base_dir") + "designs/";
            parameters.put("SUBREPORT_DIR", subreportDir);

            // at last, generate the report
            JasperPrint report = null;
            if (useSqlQuery) {
                DataSource dataSource = (DataSource) Context.getBean(Context.Name.DATA_SOURCE);
                Connection connection = DataSourceUtils.getConnection(dataSource);

                report = JasperFillManager.fillReport(stream, parameters, connection);

                DataSourceUtils.releaseConnection(connection, dataSource);
            } else {
                JRBeanCollectionDataSource data = 
                        new JRBeanCollectionDataSource(lines);
                report = JasperFillManager.fillReport(stream, parameters, data);
            }

            stream.close();

            return report;
        } catch (Exception e) {
            LOG.error("Exception generating paper invoice", e);
            return null;
        }
    }

    private static String printable(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    /**
     * Safely concatenates 2 strings together with a blank space (" "). Null strings
     * are handled safely, and no extra concatenated character will be added if one
     * string is null.
     *
     * @param str
     * @param str2
     * @return concatenated, printable string
     */
    private static String printable(String str, String str2) {
        StringBuilder builder = new StringBuilder();
        
        if (str != null) builder.append(str).append(" ");
        if (str2 != null) builder.append(str2);
        
        return builder.toString();
    }

    public static void sendSapienterEmail(Integer entityId, String messageKey,
            String attachmentFileName, String[] params)
            throws MessagingException, IOException {
        String address = null;

        ContactBL contactBL = new ContactBL();
        contactBL.setEntity(entityId);

        address = contactBL.getEntity().getEmail();
        if (address == null) {
            // can't send something to the ether
            LOG.warn("Trying to send email to entity " + entityId
                    + " but no address was found");
            return;
        }
        sendSapienterEmail(address, entityId, messageKey, attachmentFileName,
                params);
    }

    /**
     * This method is intended to be used to send an email from the system to
     * the entity. This is different than from the entity to a customer, which
     * should use a notification pluggable task. The file
     * entityNotifications.properties has to have key + "_subject" and key +
     * "_body" Note: For any truble, the best documentation is the source code
     * of the MailTag of Jakarta taglibs
     */
    public static void sendSapienterEmail(String address, Integer entityId,
            String messageKey, String attachmentFileName, String[] params)
            throws MessagingException, IOException {
        Properties prop = new Properties();

        LOG.debug("seding sapienter email " + messageKey + " to " + address
                + " of entity " + entityId);
        // tell the server that is has to authenticate to the maileer
        // (yikes, this was painfull to find out)
        prop.setProperty("mail.smtp.auth", "true");

        // create the session & message
        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(com.sapienter.jbilling.common.Util
                .getSysProp("email_from"), com.sapienter.jbilling.common.Util
                .getSysProp("email_from_name")));
        // the to address
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
                address, false));
        // the subject and body are international
        EntityBL entity = new EntityBL(entityId);
        Locale locale = entity.getLocale();

        ResourceBundle rBundle = ResourceBundle.getBundle(
                "entityNotifications", locale);
        String subject = rBundle.getString(messageKey + "_subject");
        String message = rBundle.getString(messageKey + "_body");

        // if there are parameters, replace them
        if (params != null) {
            for (int f = 0; f < params.length; f++) {
                message = message.replaceFirst("\\|X\\|", params[f]);
            }
        }

        msg.setSubject(subject);

        if (attachmentFileName == null) {
            msg.setText(message);
        } else {
            // it is a 'multi part' email
            MimeMultipart mp = new MimeMultipart();

            // the text message is one part
            MimeBodyPart text = new MimeBodyPart();
            text.setDisposition(Part.INLINE);
            text.setContent(message, "text/plain");
            mp.addBodyPart(text);

            // the attachement is another.
            MimeBodyPart file_part = new MimeBodyPart();
            File file = (File) new File(attachmentFileName);
            FileDataSource fds = new FileDataSource(file);
            DataHandler dh = new DataHandler(fds);
            file_part.setFileName(file.getName());
            file_part.setDisposition(Part.ATTACHMENT);
            file_part.setDescription("Attached file: " + file.getName());
            file_part.setDataHandler(dh);
            mp.addBodyPart(file_part);

            msg.setContent(mp);
        }

        // the date
        msg.setSentDate(Calendar.getInstance().getTime());

        Transport transport = session.getTransport("smtp");
        transport.connect(com.sapienter.jbilling.common.Util
                .getSysProp("smtp_server"), Integer
                .parseInt(com.sapienter.jbilling.common.Util
                        .getSysProp("smtp_port")),
                com.sapienter.jbilling.common.Util.getSysProp("smtp_username"),
                com.sapienter.jbilling.common.Util.getSysProp("smtp_password"));
        InternetAddress addresses[] = new InternetAddress[1];
        addresses[0] = new InternetAddress(address);
        transport.sendMessage(msg, addresses);
    }

    /**
     * Creates a message object with a set of standard parameters
     * 
     * @param entityId
     * @param userId
     * @return The message object with many useful parameters
     */
    private MessageDTO initializeMessage(Integer entityId, Integer userId)
            throws SessionInternalError {
        MessageDTO retValue = new MessageDTO();
        try {
            UserBL user = new UserBL(userId);
            ContactBL contact = new ContactBL();

            // this user's info
            contact.set(userId);
            if (contact.getEntity() != null) {
                retValue.addParameter("contact", contact.getEntity());

                retValue.addParameter("first_name", contact.getEntity().getFirstName());
                retValue.addParameter("last_name", contact.getEntity().getLastName());
                retValue.addParameter("address1", contact.getEntity().getAddress1());
                retValue.addParameter("address2", contact.getEntity().getAddress2());
                retValue.addParameter("city", contact.getEntity().getCity());
                retValue.addParameter("organization_name", contact.getEntity().getOrganizationName());
                retValue.addParameter("postal_code", contact.getEntity().getPostalCode());
                retValue.addParameter("state_province", contact.getEntity().getStateProvince());
            }

            if (user.getEntity() != null) {
                retValue.addParameter("user", user.getEntity());

                retValue.addParameter("username", user.getEntity().getUserName());
                retValue.addParameter("password", user.getEntity().getPassword());
                retValue.addParameter("user_id", user.getEntity().getUserId().toString());
            }

            if (user.getCreditCard() != null) {
                retValue.addParameter("credit_card", user.getCreditCard());
            }

            // the entity info
            contact.setEntity(entityId);
            if (contact.getEntity() != null) {
                retValue.addParameter("company_contact", contact.getEntity());

                retValue.addParameter("company_id", entityId.toString());
                retValue.addParameter("company_name", contact.getEntity().getOrganizationName());
            }

            //velocity tools
            retValue.addParameter("tools-date", new DateTool());
            retValue.addParameter("tools-math", new MathTool());
            retValue.addParameter("tools-number", new NumberTool());
            retValue.addParameter("tools-render", new RenderTool());
            retValue.addParameter("tools-escape", new EscapeTool());
            retValue.addParameter("tools-resource", new ResourceTool());
            retValue.addParameter("tools-alternator", new AlternatorTool());
            retValue.addParameter("tools-valueParser", new ValueParser());
            retValue.addParameter("tools-list", new ListTool());
            retValue.addParameter("tools-sort", new SortTool());
            retValue.addParameter("tools-iterator", new IteratorTool());

            //Adding a CCF Field to Email Template
            List<ContactDTOEx> listDto= contact.getAll(userId);
            if (null != listDto && listDto.size() > 0 ) {
                ContactDTOEx contactDTOEx= listDto.get(0);
                Map<String, ContactFieldDTO> fieldsMap= contactDTOEx.getFieldsTable();
                for (Iterator<?> it= fieldsMap.values().iterator();it.hasNext(); ) {
                     ContactFieldDTO contactFieldDTO= (ContactFieldDTO) it.next();
                     if ( null != contactFieldDTO && null != contactFieldDTO.getContent()
                        && null != contactFieldDTO.getType())
                     {
                        retValue.addParameter(contactFieldDTO.getType().getPromptKey(), contactFieldDTO.getContent());
                     }
                }
            }
            
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
        return retValue;
    }

    public String generatePaperInvoiceAsFile(InvoiceDTO invoice)
            throws SessionInternalError {

        try {
            Integer entityId = invoice.getBaseUser().getEntity().getId();

            // the language doesn't matter when getting a paper invoice
            MessageDTO paperMsg = getInvoicePaperMessage(entityId, null,
                    invoice.getBaseUser().getLanguageIdField(), invoice);
            PaperInvoiceNotificationTask task = new PaperInvoiceNotificationTask();
            PluggableTaskBL taskBL = new PluggableTaskBL();
            taskBL.set(entityId, Constants.PLUGGABLE_TASK_T_PAPER_INVOICE);
            task.initializeParamters(taskBL.getDTO());

            String filename = task.getPDFFile(invoice.getBaseUser(), paperMsg);

            return filename;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
}
