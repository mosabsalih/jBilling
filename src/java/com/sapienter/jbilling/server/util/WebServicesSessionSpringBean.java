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

/*
 * Created on Jan 27, 2005
 * One session bean to expose as a single web service, thus, one wsdl
 */
package com.sapienter.jbilling.server.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sapienter.jbilling.server.item.CurrencyBL;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDAS;
import com.sapienter.jbilling.server.order.OrderHelper;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.ContactTypeWS;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.LanguageDAS;
import com.sapienter.jbilling.server.util.db.LanguageDTO;
import com.sapienter.jbilling.server.util.db.PreferenceTypeDAS;
import com.sapienter.jbilling.server.util.db.PreferenceTypeDTO;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.rowset.CachedRowSet;

import com.sapienter.jbilling.client.authentication.CompanyUserDetails;
import com.sapienter.jbilling.common.InvalidArgumentException;
import com.sapienter.jbilling.common.JBCrypto;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.IInvoiceSessionBean;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.InvoiceWS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.item.IItemSessionBean;
import com.sapienter.jbilling.server.item.ItemBL;
import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeBL;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.mediation.IMediationSessionBean;
import com.sapienter.jbilling.server.mediation.MediationConfigurationBL;
import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
import com.sapienter.jbilling.server.mediation.MediationProcessWS;
import com.sapienter.jbilling.server.mediation.MediationRecordBL;
import com.sapienter.jbilling.server.mediation.MediationRecordLineWS;
import com.sapienter.jbilling.server.mediation.MediationRecordWS;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.mediation.RecordCountWS;
import com.sapienter.jbilling.server.mediation.db.MediationConfiguration;
import com.sapienter.jbilling.server.mediation.db.MediationProcess;
import com.sapienter.jbilling.server.mediation.db.MediationRecordDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordDTO;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;
import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDAS;
import com.sapienter.jbilling.server.mediation.db.MediationRecordStatusDTO;
import com.sapienter.jbilling.server.mediation.task.IMediationProcess;
import com.sapienter.jbilling.server.mediation.task.MediationResult;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.order.IOrderSessionBean;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.OrderLineBL;
import com.sapienter.jbilling.server.order.OrderLineWS;
import com.sapienter.jbilling.server.order.OrderProcessWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO;
import com.sapienter.jbilling.server.util.db.PreferenceDTO;
import grails.plugins.springsecurity.SpringSecurityService;
import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.PaymentWS;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDTO;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
import com.sapienter.jbilling.server.process.BillingProcessBL;
import com.sapienter.jbilling.server.process.BillingProcessConfigurationWS;
import com.sapienter.jbilling.server.process.BillingProcessDTOEx;
import com.sapienter.jbilling.server.process.BillingProcessWS;
import com.sapienter.jbilling.server.process.ConfigurationBL;
import com.sapienter.jbilling.server.process.IBillingProcessSessionBean;
import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDAS;
import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.provisioning.IProvisioningProcessSessionBean;
import com.sapienter.jbilling.server.rule.task.IRulesGenerator;
import com.sapienter.jbilling.server.user.AchBL;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.ContactWS;
import com.sapienter.jbilling.server.user.CreateResponseWS;
import com.sapienter.jbilling.server.user.CreditCardBL;
import com.sapienter.jbilling.server.user.IUserSessionBean;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.UserTransitionResponseWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.CustomerDAS;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.partner.PartnerBL;
import com.sapienter.jbilling.server.user.partner.PartnerWS;
import com.sapienter.jbilling.server.user.partner.db.Partner;
import com.sapienter.jbilling.server.util.api.WebServicesConstants;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;

import com.sapienter.jbilling.server.process.AgeingBL;
import com.sapienter.jbilling.server.process.AgeingDTOEx;
import com.sapienter.jbilling.server.process.AgeingWS;

import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDAS;
import com.sapienter.jbilling.server.user.contact.ContactFieldTypeWS;
import com.sapienter.jbilling.server.order.OrderPeriodWS;
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
import com.sapienter.jbilling.server.order.db.OrderPeriodDAS;

import com.sapienter.jbilling.server.user.CompanyWS;
import com.sapienter.jbilling.server.user.EntityBL; 

import javax.naming.NamingException;

@Transactional( propagation = Propagation.REQUIRED )
public class WebServicesSessionSpringBean implements IWebServicesSessionBean {
    private static final Logger LOG = Logger.getLogger(WebServicesSessionSpringBean.class);

    private SpringSecurityService springSecurityService;

    public SpringSecurityService getSpringSecurityService() {
        if (springSecurityService == null)
            this.springSecurityService = Context.getBean(Context.Name.SPRING_SECURITY_SERVICE);
        return springSecurityService;
    }

    public void setSpringSecurityService(SpringSecurityService springSecurityService) {
        this.springSecurityService = springSecurityService;
    }

    /*
     * Returns the user ID of the authenticated user account making the web service call.
     *
     * @return caller user ID
     */
    public Integer getCallerId() {
        CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
        return details.getUserId();
    }

    /**
     * Returns the company ID of the authenticated user account making the web service call.
     *
     * @return caller company ID
     */
    public Integer getCallerCompanyId() {
        CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
        return details.getCompanyId();
    }

    /**
     * Returns the language ID of the authenticated user account making the web service call.
     *
     * @return caller language ID
     */
    public Integer getCallerLanguageId() {
        CompanyUserDetails details = (CompanyUserDetails) getSpringSecurityService().getPrincipal();
        return details.getLanguageId();
    }


    // todo: reorganize methods and reformat code. should match the structure of the interface to make things readable.


    /*
        Invoices
     */

    public InvoiceWS getInvoiceWS(Integer invoiceId)
            throws SessionInternalError {
        if (invoiceId == null) {
            return null;
        }
        InvoiceDTO invoice = new InvoiceDAS().find(invoiceId);

        if (invoice.getDeleted() == 1) {
            return null;
        }

        InvoiceWS wsDto= InvoiceBL.getWS(invoice);
        if ( null != invoice.getInvoiceStatus())
        {
        	wsDto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));
        }
        return wsDto;
    }

    public InvoiceWS[] getAllInvoicesForUser(Integer userId) {
        IInvoiceSessionBean invoiceBean = Context.getBean(Context.Name.INVOICE_SESSION);
        Set<InvoiceDTO> invoices = invoiceBean.getAllInvoices(userId);

        List<InvoiceWS> ids = new ArrayList<InvoiceWS>(invoices.size());
        for (InvoiceDTO invoice : invoices)
        {
        	InvoiceWS wsdto= InvoiceBL.getWS(invoice);
        	if ( null != invoice.getInvoiceStatus())
        		wsdto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));

        	ids.add(wsdto);
        }
        return ids.toArray(new InvoiceWS[ids.size()]);
    }

    public InvoiceWS[] getAllInvoices() {

        List<InvoiceDTO> invoices = new InvoiceDAS().findAll();

        List<InvoiceWS> ids = new ArrayList<InvoiceWS>(invoices.size());
        for (InvoiceDTO invoice : invoices)
        {
        	InvoiceWS wsdto= InvoiceBL.getWS(invoice);
        	if ( null != invoice.getInvoiceStatus())
        		wsdto.setStatusDescr(invoice.getInvoiceStatus().getDescription(getCallerLanguageId()));

        	ids.add(wsdto);
        }
        return ids.toArray(new InvoiceWS[ids.size()]);
    }

    public boolean notifyInvoiceByEmail(Integer invoiceId) {
    	INotificationSessionBean notificationSession =
	            (INotificationSessionBean) Context.getBean(
	            Context.Name.NOTIFICATION_SESSION);
	    return notificationSession.emailInvoice(invoiceId);
    }

    public Integer[] getAllInvoices(Integer userId) {
        IInvoiceSessionBean invoiceBean = Context.getBean(Context.Name.INVOICE_SESSION);
        Set<InvoiceDTO> invoices = invoiceBean.getAllInvoices(userId);

        List<Integer> ids = new ArrayList<Integer>(invoices.size());
        for (InvoiceDTO invoice : invoices)
            ids.add(invoice.getId());
        return ids.toArray(new Integer[ids.size()]);
    }

    public InvoiceWS getLatestInvoice(Integer userId)
            throws SessionInternalError {
        InvoiceWS retValue = null;
        try {
            if (userId == null) {
                return null;
            }
            InvoiceBL bl = new InvoiceBL();
            Integer invoiceId = bl.getLastByUser(userId);
            if (invoiceId != null) {
                retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
            }
            return retValue;
        } catch (Exception e) { // needed because the sql exception :(
            LOG.error("Exception in web service: getting latest invoice" +
                    " for user " + userId, e);
            throw new SessionInternalError("Error getting latest invoice");
        }
    }

    public Integer[] getLastInvoices(Integer userId, Integer number)
            throws SessionInternalError {
        if (userId == null || number == null) {
            return null;
        }

        InvoiceBL bl = new InvoiceBL();
        return bl.getManyWS(userId, number);
    }

    public Integer[] getInvoicesByDate(String since, String until)
            throws SessionInternalError {
        try {
            Date dSince = com.sapienter.jbilling.common.Util.parseDate(since);
            Date dUntil = com.sapienter.jbilling.common.Util.parseDate(until);
            if (since == null || until == null) {
                return null;
            }

            Integer entityId = getCallerCompanyId();

            InvoiceBL invoiceBl = new InvoiceBL();
            return invoiceBl.getInvoicesByCreateDateArray(entityId, dSince, dUntil);
        } catch (Exception e) { // needed for the SQLException :(
            LOG.error("Exception in web service: getting invoices by date" +
                    since + until, e);
            throw new SessionInternalError("Error getting last invoices");
        }
    }

    /**
     * Returns the invoices for the user within the given date range.
     */
    public Integer[] getUserInvoicesByDate(Integer userId, String since,
            String until) throws SessionInternalError {
        if (userId == null || since == null || until == null) {
            return null;
        }

        Date dSince = com.sapienter.jbilling.common.Util.parseDate(since);
        Date dUntil = com.sapienter.jbilling.common.Util.parseDate(until);

        InvoiceBL invoiceBl = new InvoiceBL();

        Integer[] results = invoiceBl.getUserInvoicesByDate(userId, dSince,
                dUntil);

        return results;
    }

    /**
     * Returns an array of IDs for all unpaid invoices under the given user ID.
     *
     * @param userId user IDs
     * @return array of un-paid invoice IDs
     */
    public Integer[] getUnpaidInvoices(Integer userId) {
        try {
            CachedRowSet rs = new InvoiceBL().getPayableInvoicesByUser(userId);

            Integer[] invoiceIds = new Integer[rs.size()];
            int i = 0;
            while (rs.next())
                invoiceIds[i++] = rs.getInt(1);

            rs.close();
            return invoiceIds;

        } catch (SQLException e) {
            throw new SessionInternalError("Exception occurred querying payable invoices.");
        } catch (Exception e) {
            throw new SessionInternalError("An un-handled exception occurred querying payable invoices.");
        }
    }

    /**
     * Generates and returns the paper invoice PDF for the given invoiceId.
     *
     * @param invoiceId invoice to generate PDF for
     * @return PDF invoice bytes
     * @throws SessionInternalError
     */
    public byte[] getPaperInvoicePDF(Integer invoiceId) throws SessionInternalError {
        IInvoiceSessionBean invoiceSession = (IInvoiceSessionBean) Context.getBean(Context.Name.INVOICE_SESSION);
        return invoiceSession.getPDFInvoice(invoiceId);
    }

    /**
     * Un-links a payment from an invoice, effectivley making the invoice "unpaid" by
     * removing the payment balance.
     *
     * If either invoiceId or paymentId parameters are null, no operation will be performed.
     *
     * @param invoiceId target Invoice
     * @param paymentId payment to be unlink
     */
    public void removePaymentLink(Integer invoiceId, Integer paymentId) {
		if (invoiceId == null || paymentId == null)
            return;

        boolean result= new PaymentBL(paymentId).unLinkFromInvoice(invoiceId);
        if (!result)
			throw new SessionInternalError("Unable to find the Invoice Id " + invoiceId + " linked to Payment Id " + paymentId);
	}

    /**
     * Applies an existing payment to an invoice.
     *
     * If either invoiceId or paymentId parameters are null, no operation will be performed.
     *
     * @param invoiceId target invoice
     * @param paymentId payment to apply
     */
    public void createPaymentLink(Integer invoiceId, Integer paymentId) {
        IPaymentSessionBean session = Context.getBean(Context.Name.PAYMENT_SESSION);
        session.applyPayment(paymentId, invoiceId);
    }

    /**
     * Deletes an invoice
     * @param invoiceId
     * The id of the invoice to delete
     */
    public void deleteInvoice(Integer invoiceId) {
        Integer executorId = getCallerId();
        InvoiceBL invoice = new InvoiceBL(invoiceId);
        invoice.delete(executorId);
    }

    /**
     * Deletes an Item
     * @param itemId
     * The id of the item to delete
     */
    public void deleteItem(Integer itemId) throws SessionInternalError {
    	ItemBL itemBl= new ItemBL(itemId);
    	itemBl.delete(getCallerId());
    	LOG.debug("Deleted Item, " + itemBl.getEntity().getDeleted());
    }

    /**
     * Deletes an Item Category
     * @param itemCategoryId
     * The id of the Item Category to delete
     */
    public void deleteItemCategory(Integer itemCategoryId) throws SessionInternalError {

    	ItemTypeBL bl = new ItemTypeBL(itemCategoryId);
		bl.delete(getCallerId());
    }

    /**
     * Generates invoices for orders not yet invoiced for this user.
     * Optionally only allow recurring orders to generate invoices.
     * Returns the ids of the invoices generated.
     */
    public Integer[] createInvoice(Integer userId, boolean onlyRecurring)
            throws SessionInternalError {
        UserDTO user = new UserDAS().find(userId);
        BillingProcessBL processBL = new BillingProcessBL();

        BillingProcessConfigurationDTO config =
                new BillingProcessConfigurationDAS().findByEntity(
                user.getCompany());

        // Create a mock billing process object, because the method
        // we are calling was meant to be called by the billing process.
        BillingProcessDTO billingProcess = new BillingProcessDTO();
        billingProcess.setId(0);
        billingProcess.setEntity(user.getCompany());
        billingProcess.setBillingDate(new Date());
        billingProcess.setPeriodUnit(config.getPeriodUnit());
        billingProcess.setPeriodValue(config.getPeriodValue());
        billingProcess.setIsReview(0);
        billingProcess.setRetriesToDo(0);

        InvoiceDTO[] newInvoices = processBL.generateInvoice(billingProcess,
                user, false, onlyRecurring);

        if (newInvoices != null) {
            Integer[] invoiceIds = new Integer[newInvoices.length];
            for (int i = 0; i < newInvoices.length; i++) {
                invoiceIds[i] = newInvoices[i].getId();
            }
            return invoiceIds;
        } else {
            return new Integer[]{};
        }
    }

    /**
     * Generates a new invoice for an order, or adds the order to an
     * existing invoice.
     *
     * @param orderId order id to generate an invoice for
     * @param invoiceId optional invoice id to add the order to. If null, a new invoice will be created.
     * @return id of generated invoice, null if no invoice generated.
     * @throws SessionInternalError if user id or order id is null.
     */
    public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId) throws SessionInternalError {
        if (orderId == null) throw new SessionInternalError("Order id cannot be null.");

        // validate order to be processed
        OrderDTO order = new OrderDAS().find(orderId);
        if (order == null || !Constants.ORDER_STATUS_ACTIVE.equals(order.getStatusId())) {
            LOG.debug("Order must exist and be active to generate an invoice.");
            return null;
        }

        // create new invoice, or add to an existing invoice
        InvoiceDTO invoice;
        if (invoiceId == null) {
            LOG.debug("Creating a new invoice for order " + order.getId());
            invoice = doCreateInvoice(order.getId());
            if ( null == invoice) { 
            	throw new SessionInternalError("Invoice could not be generated. The purchase order may not have any applicable periods to be invoiced.");
            }
        } else {
            LOG.debug("Adding order " + order.getId() + " to invoice " + invoiceId);
            IBillingProcessSessionBean process = (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
            invoice = process.generateInvoice(order.getId(), invoiceId, null);
        }

        return invoice == null ? null : invoice.getId();
    }

    /*
     * USERS
     */
    /**
     * Creates a new user. The user to be created has to be of the roles customer
     * or partner.
     * The username has to be unique, otherwise the creating won't go through. If
     * that is the case, the return value will be null.
     * @param newUser
     * The user object with all the information of the new user. If contact or
     * credit card information are present, they will be included in the creation
     * although they are not mandatory.
     * @return The id of the new user, or null if non was created
     */
    public Integer createUser(UserWS newUser)
            throws SessionInternalError {

//        validateUser(newUser);
        newUser.setUserId(0);

        Integer entityId = getCallerCompanyId();
        UserBL bl = new UserBL();

        if (bl.exists(newUser.getUserName(), entityId)) {
            throw new SessionInternalError("User already exists with username " + newUser.getUserName(),
                                            new String[] { "UserWS,userName,validation.error.user.already.exists" });
        }

        ContactBL cBl = new ContactBL();
        UserDTOEx dto = new UserDTOEx(newUser, entityId);
        Integer userId = bl.create(dto);
        if (newUser.getContact() != null) {
            newUser.getContact().setId(0);
            cBl.createPrimaryForUser(new ContactDTOEx(newUser.getContact()), userId, entityId);
        }

        if (newUser.getCreditCard() != null) {
            CreditCardDTO card = new CreditCardDTO(newUser.getCreditCard()); // new CreditCardDTO
            card.setId(0);
            card.getBaseUsers().add(bl.getEntity());

            CreditCardBL ccBL = new CreditCardBL();
            ccBL.create(card);

            UserDTO userD = new UserDAS().find(userId);
            userD.getCreditCards().add(ccBL.getEntity());
        }

        if (newUser.getAch() != null) {
            AchDTO ach = new AchDTO(newUser.getAch());
            ach.setId(0);
            ach.setBaseUser(bl.getEntity());
            AchBL abl = new AchBL();
            abl.create(ach);
        }
        return userId;
    }

    public void deleteUser(Integer userId) throws SessionInternalError {
        UserBL bl = new UserBL();
        Integer executorId = getCallerId();
        bl.set(userId);
        bl.delete(executorId);
    }

    /**
     * Fetches the ContactTypeWS for the given contact type ID. The returned WS object
     * contains a list of international descriptions for all available languages.
     *
     * @param contactTypeId contact type ID
     * @return contact type WS object
     * @throws SessionInternalError
     */
    public ContactTypeWS getContactTypeWS(Integer contactTypeId) throws SessionInternalError {
        ContactTypeDTO contactType = new ContactTypeDAS().find(contactTypeId);
        List<LanguageDTO> languages = new LanguageDAS().findAll();

        return new ContactTypeWS(contactType, languages);
    }

    /**
     * Creates a new contact type from the given WS object. This method also stores the international
     * description for each description/language in the WS object.
     *
     * @param contactType contact type WS
     * @return ID of created contact type
     * @throws SessionInternalError
     */
    public Integer createContactTypeWS(ContactTypeWS contactType) throws SessionInternalError {
        ContactTypeDTO dto = new ContactTypeDTO();
        dto.setEntity(new CompanyDTO(getCallerCompanyId()));
        dto.setIsPrimary(contactType.getPrimary());

        ContactTypeDAS contactTypeDas = new ContactTypeDAS();
        dto = contactTypeDas.save(dto);

        for (InternationalDescriptionWS description : contactType.getDescriptions()) {
            dto.setDescription(description.getContent(), description.getLanguageId());
        }

        // flush changes to the DB & clear cache
        contactTypeDas.flush();
        contactTypeDas.clear();

        return dto.getId();
    }

    public void updateUserContact(Integer userId, Integer typeId, ContactWS contact) throws SessionInternalError {
        // todo: support multiple WS method param validations through WSSecurityMethodMapper
        ContactTypeDTO type = new ContactTypeDAS().find(typeId);
        if (type == null || type.getEntity() == null || !getCallerCompanyId().equals(type.getEntity().getId()))
            throw new SessionInternalError("Invalid contact type.");

        // update the contact
        ContactBL cBl = new ContactBL();
        cBl.updateForUser(new ContactDTOEx(contact), userId, typeId);
    }

    /**
     * @param user
     */
    public void updateUser(UserWS user)
            throws SessionInternalError {

    	//TODO commenting validate user for create/edit customer grails impl. - vikasb
        //validateUser(user);

        UserBL bl = new UserBL(user.getUserId());

        // get the entity
        Integer entityId = getCallerCompanyId();
        Integer executorId = getCallerId();

        // convert to a DTO
        UserDTOEx dto = new UserDTOEx(user, entityId);

        // update the user info
        bl.update(executorId, dto);

        // now update the contact info
        if (user.getContact() != null) {
            ContactBL contactBl = new ContactBL();
            contactBl.setEntity(entityId);
            contactBl.createUpdatePrimaryForUser(new ContactDTOEx(user.getContact()), user.getUserId(), entityId);
        }

        // and the credit card
        if (user.getCreditCard() != null) {
            IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                    Context.Name.USER_SESSION);
            sess.updateCreditCard(executorId, user.getUserId(),
                    new CreditCardDTO(user.getCreditCard()));
        }

        //udpate customerdto here - notes, automaticPaymentMethod
        CustomerDTO cust= UserBL.getUserEntity(user.getUserId()).getCustomer();
    	if ( null != cust ) {
    		LOG.debug("This code should save=" + user.getNotes() + " and " + user.getAutomaticPaymentType());
    		cust.setNotes(user.getNotes());
    		cust.setAutoPaymentType(user.getAutomaticPaymentType());
    		new CustomerDAS().save(cust);
        }
    }

    /**
     * Retrieves a user with its contact and credit card information.
     * @param userId
     * The id of the user to be returned
     */
    public UserWS getUserWS(Integer userId) throws SessionInternalError {
        UserBL bl = new UserBL(userId);
        return bl.getUserWS();
    }

    /**
     * Retrieves all the contacts of a user
     * @param userId
     * The id of the user to be returned
     */
    public ContactWS[] getUserContactsWS(Integer userId)
            throws SessionInternalError {
        ContactWS[] dtos = null;
        ContactBL contact = new ContactBL();
        List result = contact.getAll(userId);
        dtos = new ContactWS[result.size()];
        for (int f = 0; f < result.size(); f++) {
            dtos[f] = new ContactWS((ContactDTOEx) result.get(f));
        }

        return dtos;
    }

    /**
     * Retrieves the user id for the given username
     */
    public Integer getUserId(String username)
            throws SessionInternalError {
        UserDAS das = new UserDAS();
        Integer retValue = das.findByUserName(username,
                getCallerCompanyId()).getId();
        return retValue;
    }

    /**
     * Retrieves an array of users in the required status
     */
    public Integer[] getUsersInStatus(Integer statusId) throws SessionInternalError {
        return getUsersByStatus(statusId, true);
    }

    /**
     * Retrieves an array of users in the required status
     */
    public Integer[] getUsersNotInStatus(Integer statusId) throws SessionInternalError {
        return getUsersByStatus(statusId, false);
    }

    /**
     * Retrieves an array of users in the required status
     */
    public Integer[] getUsersByCustomField(Integer typeId, String value)
            throws SessionInternalError {
        try {
            UserBL bl = new UserBL();
            Integer entityId = getCallerCompanyId();

            CachedRowSet users = bl.getByCustomField(entityId, typeId, value);
            LOG.debug("got collection. Now converting");
            Integer[] ret = new Integer[users.size()];
            int f = 0;
            while (users.next()) {
                ret[f] = users.getInt(1);
                f++;
            }
            users.close();
            return ret;
        } catch (Exception e) { // can't remove because of the SQL Exception :(
            LOG.error("WS - getUsersByCustomField", e);
            throw new SessionInternalError("Error getting users by custom field");
        }
    }

    public void saveCustomContactFields(ContactFieldTypeWS[] fields) throws SessionInternalError {
    	try {
    		ContactFieldTypeDAS das= new ContactFieldTypeDAS();
    		for (ContactFieldTypeWS ws: fields) { 
    			ContactFieldTypeDTO dto= ws.getId() == null ? new ContactFieldTypeDTO() : das.find(ws.getId());
    			dto.setDataType(ws.getDataType());
    			dto.setReadOnly(ws.getReadOnly());
    			dto.setPromptKey("placeholder_text");
    			dto.setEntity(new CompanyDTO(ws.getCompanyId()));
    			dto.setVersionNum(new Integer(0));
    			dto= das.save(dto);
    			
				for (InternationalDescriptionWS description : ws.getDescriptions()) {
				    dto.setDescription(description.getContent(), description.getLanguageId());
				}
    			das.flush();
    			das.clear();
    		}
    	}catch (Exception e) {
    		throw new SessionInternalError(e);
    	}
    }
    
    @Deprecated
    private Integer[] getByCCNumber(Integer entityId, String number) {
        List<Integer> usersIds = new CreditCardDAS().findByLastDigits(entityId, number);

        Integer[] ids = new Integer[usersIds.size()];
        return usersIds.toArray(ids);

    }

    /**
     * Retrieves an array of users in the required status
     */
    public Integer[] getUsersByCreditCard(String number) throws SessionInternalError {
        Integer entityId = getCallerCompanyId();

        Integer[] ret = getByCCNumber(entityId, number);
        return ret;
    }

    /**
     * Retrieves an array of users in the required status
     */
    public Integer[] getUsersByStatus(Integer statusId, boolean in) throws SessionInternalError {
        try {
            UserBL bl = new UserBL();
            CachedRowSet users = bl.getByStatus(getCallerCompanyId(), statusId, in);
            LOG.debug("got collection. Now converting");
            Integer[] ret = new Integer[users.size()];
            int f = 0;
            while (users.next()) {
                ret[f] = users.getInt(1);
                f++;
            }
            users.close();
            return ret;
        } catch (Exception e) { // can't remove because of SQLException :(
            throw new SessionInternalError(e);
        }
    }

    /**
     * Creates a user, then an order for it, an invoice out the order
     * and tries the invoice to be paid by an online payment
     * This is ... the mega call !!!
     */
    public CreateResponseWS create(UserWS user, OrderWS order)
            throws SessionInternalError {

        CreateResponseWS retValue = new CreateResponseWS();

        // the user first
        final Integer userId = createUser(user);
        retValue.setUserId(userId);

        if (userId == null) {
            return retValue;
        }

        // the order and (if needed) invoice
        order.setUserId(userId);

        Integer orderId = doCreateOrder(order, true).getId();
        InvoiceDTO invoice = doCreateInvoice(orderId);

        retValue.setOrderId(orderId);

        if (invoice != null) {
            retValue.setInvoiceId(invoice.getId());

            //the payment, if we have a credit card
            if (user.getCreditCard() != null) {
                PaymentDTOEx payment = doPayInvoice(invoice, new CreditCardDTO(user.getCreditCard()));
                PaymentAuthorizationDTOEx result = null;
                if (payment != null) {
                    result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
                    result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
                }
                retValue.setPaymentResult(result);
                retValue.setPaymentId(payment.getId());
            }
        } else {
            throw new SessionInternalError("Invoice expected for order: " + orderId);
        }

        return retValue;
    }

    public void processPartnerPayouts(Date runDate) {
        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        userSession.processPayouts(runDate);
    }

    public PartnerWS getPartner(Integer partnerId) throws SessionInternalError {
        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        Partner dto = userSession.getPartnerDTO(partnerId);

        return PartnerBL.getWS(dto);
    }

    /**
     * Pays given invoice, using the first credit card available for invoice'd
     * user.
     *
     * @return <code>null</code> if invoice has not positive balance, or if
     *         user does not have credit card
     * @return resulting authorization record. The payment itself can be found by
     * calling getLatestPayment
     */
    public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId) throws SessionInternalError {

        if (invoiceId == null) {
            throw new SessionInternalError("Can not pay null invoice");
        }

        final InvoiceDTO invoice = findInvoice(invoiceId);
        CreditCardDTO creditCard = getCreditCard(invoice.getBaseUser().getUserId());
        if (creditCard == null) {
            return null;
        }

        PaymentDTOEx payment = doPayInvoice(invoice, creditCard);

        PaymentAuthorizationDTOEx result = null;
        if (payment != null) {
            result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
            result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
        }

        return result;
    }

    /*
     * ORDERS
     */
    /**
     * @return the information of the payment aurhotization, or NULL if the
     * user does not have a credit card
     */
    public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order)
            throws SessionInternalError {

        PaymentAuthorizationDTOEx retValue = null;
        // start by creating the order. It'll do the checks as well
        Integer orderId = createOrder(order);

        Integer userId = order.getUserId();
        CreditCardDTO cc = getCreditCard(userId);
        UserBL user = new UserBL();
        Integer entityId = user.getEntityId(userId);
        if (cc != null) {
            CreditCardBL ccBl = new CreditCardBL();
            OrderDAS das = new OrderDAS();
            OrderDTO dbOrder = das.find(orderId);

            try {
                retValue = ccBl.validatePreAuthorization(entityId, userId, cc, dbOrder.getTotal(), dbOrder.getCurrencyId());
            } catch (PluggableTaskException e) {
                throw new SessionInternalError("doing validation", WebServicesSessionSpringBean.class, e);
            }
        }
        return retValue;
    }

    public Integer createOrder(OrderWS order)
            throws SessionInternalError {

        Integer orderId = doCreateOrder(order, true).getId();
        return orderId;
    }

    /**
     * Update the given order, or create it if it doesn't already exist.
     *
     * @param order order to update or create
     * @return order id
     * @throws SessionInternalError
     */
    public Integer createUpdateOrder(OrderWS order) throws SessionInternalError {
        IOrderSessionBean orderSession = Context.getBean(Context.Name.ORDER_SESSION);

        OrderDTO dto = new OrderBL().getDTO(order);
        return orderSession.createUpdate(getCallerCompanyId(), getCallerId(), dto, getCallerLanguageId());
    }

    public OrderWS rateOrder(OrderWS order)
            throws SessionInternalError {

        OrderWS ordr = doCreateOrder(order, false);
        return ordr;
    }

    public OrderWS[] rateOrders(OrderWS orders[])
            throws SessionInternalError {

        if (orders == null || orders.length == 0) {
            LOG.debug("Call to rateOrders without orders to rate");
            return null;
        }

        OrderWS retValue[] = new OrderWS[orders.length];
        for (int index = 0; index < orders.length; index++) {
            retValue[index] = doCreateOrder(orders[index],false);
        }
        return retValue;
    }

    public void updateItem(ItemDTOEx item) {
        UserBL bl = new UserBL(getCallerId());
        Integer executorId = bl.getEntity().getUserId();
        Integer languageId = bl.getEntity().getLanguageIdField();

        // do some transformation from WS to DTO :(
        ItemBL itemBL = new ItemBL();
        ItemDTO dto = itemBL.getDTO(item);

        IItemSessionBean itemSession = (IItemSessionBean) Context.getBean(
                Context.Name.ITEM_SESSION);
        itemSession.update(executorId, dto, languageId);
    }

    /**
     * Creates the given Order in jBilling, generates an Invoice for the same.
     * Returns the generated Invoice ID
     */
    public Integer createOrderAndInvoice(OrderWS order)
            throws SessionInternalError {

        Integer orderId = doCreateOrder(order, true).getId();
        InvoiceDTO invoice = doCreateInvoice(orderId);

        return invoice == null ? null : invoice.getId();
    }

    private void processLines(OrderDTO order, Integer languageId, Integer entityId, Integer userId, Integer currencyId,
                              String pricingFields) throws SessionInternalError {

        OrderHelper.synchronizeOrderLines(order);

        for (OrderLineDTO line : order.getLines()) {
            LOG.debug("Processing line " + line);

            if (line.getUseItem()) {
                List<PricingField> fields = pricingFields != null
                                            ? Arrays.asList(PricingField.getPricingFieldsValue(pricingFields))
                                            : null;

                ItemBL itemBl = new ItemBL(line.getItemId());
                itemBl.setPricingFields(fields);

                // get item with calculated price
                ItemDTO item = itemBl.getDTO(languageId, userId, entityId, currencyId, line.getQuantity(), order);
                LOG.debug("Populating line using item " + item);

                // set price or percentage from item
                if (item.getPrice() == null) {
                    line.setPrice(item.getPercentage());
                } else {
                    line.setPrice(item.getPrice());
                }

                // set description and line type
                line.setDescription(item.getDescription());
                line.setTypeId(item.getOrderLineTypeId());
            }
        }

        OrderHelper.desynchronizeOrderLines(order);
    }


    public void updateOrder(OrderWS order)
            throws SessionInternalError {
        validateOrder(order);
        try {
            // start by locking the order
            OrderBL oldOrder = new OrderBL();
            oldOrder.setForUpdate(order.getId());

            // do some transformation from WS to DTO :(
            OrderBL orderBL = new OrderBL();
            OrderDTO dto = orderBL.getDTO(order);

            // get the info from the caller
            UserBL bl = new UserBL(getCallerId());
            Integer executorId = bl.getEntity().getUserId();
            Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
            Integer languageId = bl.getEntity().getLanguageIdField();

            // see if the related items should provide info
            processLines(dto, languageId, entityId, order.getUserId(), order.getCurrencyId(), order.getPricingFields());

            // recalculate
            orderBL.set(dto);
            orderBL.recalculate(entityId);

            // update
            oldOrder.update(executorId, dto);

        } catch (Exception e) {
            LOG.error("WS - updateOrder", e);
            throw new SessionInternalError("Error updating order");
        }

    }

    public OrderWS getOrder(Integer orderId) throws SessionInternalError {
            // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        // now get the order. Avoid the proxy since this is for the client
        OrderDAS das = new OrderDAS();
        OrderDTO order = das.findNow(orderId);
        if (order == null) { // not found
            return null;
        }
        OrderBL bl = new OrderBL(order);
        if (order.getDeleted() == 1) {
            LOG.debug("Returning deleted order " + orderId);
        }
        return bl.getWS(languageId);
    }

    public Integer[] getOrderByPeriod(Integer userId, Integer periodId)
            throws SessionInternalError {
        if (userId == null || periodId == null) {
            return null;
        }
        // now get the order
        OrderBL bl = new OrderBL();
        return bl.getByUserAndPeriod(userId, periodId);
    }

    public OrderLineWS getOrderLine(Integer orderLineId) throws SessionInternalError {
        // now get the order
        OrderBL bl = new OrderBL();
        OrderLineWS retValue = null;

        retValue = bl.getOrderLineWS(orderLineId);

        return retValue;
    }

    public void updateOrderLine(OrderLineWS line) throws SessionInternalError {
            // now get the order
        OrderBL bl = new OrderBL();
        bl.updateOrderLine(line);
    }

    public OrderWS getLatestOrder(Integer userId) throws SessionInternalError {
        if (userId == null) {
            throw new SessionInternalError("User id can not be null");
        }
        OrderWS retValue = null;
        // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        // now get the order
        OrderBL bl = new OrderBL();
        Integer orderId = bl.getLatest(userId);
        if (orderId != null) {
            bl.set(orderId);
            retValue = bl.getWS(languageId);
        }
        return retValue;
    }

    public Integer[] getLastOrders(Integer userId, Integer number)
            throws SessionInternalError {
        if (userId == null || number == null) {
            return null;
        }
        UserBL userbl = new UserBL();

        OrderBL order = new OrderBL();
        return order.getListIds(userId, number, userbl.getEntityId(userId));
    }

    public void deleteOrder(Integer id) throws SessionInternalError {
        // now get the order
        OrderBL bl = new OrderBL();
        bl.setForUpdate(id);
        bl.delete(getCallerId());
    }

    /**
     * Returns the current order (order collecting current one-time charges) for the 
     * period of the given date and the given user. 
     * Returns null for users with no main subscription order.
     */
    public OrderWS getCurrentOrder(Integer userId, Date date) {
        OrderWS retValue = null;
        // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        // now get the current order
        OrderBL bl = new OrderBL();
        if (bl.getCurrentOrder(userId, date) != null) {
            retValue = bl.getWS(languageId);
        }

        return retValue;
    }

    /**
     * Updates the uesr's current one-time order for the given date.
     * Returns the updated current order. Throws an exception for
     * users with no main subscription order.
     */
    public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines,
            String pricing, Date date, String eventDescription) {
        try {
            UserBL userbl = new UserBL(userId);
            // check user has a main subscription order
            if (userbl.getEntity().getCustomer().getCurrentOrderId() == null) {
                throw new SessionInternalError("No main subscription order for userId: " + userId);
            }

            // get currency from the user
            Integer currencyId = userbl.getCurrencyId();

            // get language from the caller
            userbl.set(getCallerId());
            Integer languageId = userbl.getEntity().getLanguageIdField();

            // pricing fields
            List<Record> records = null;
            PricingField[] fieldsArray = PricingField.getPricingFieldsValue(
                    pricing);
            if (fieldsArray != null) {
                Record record = new Record();
                for (PricingField field : fieldsArray) {
                    record.addField(field, false); // don't care about isKey
                }
                records = new ArrayList<Record>(1);
                records.add(record);
            }

            List<OrderLineDTO> diffLines = null;
            OrderBL bl = new OrderBL();
            if (lines != null) {
                // get the current order
                bl.set(OrderBL.getOrCreateCurrentOrder(userId, date, currencyId, true));
                List<OrderLineDTO> oldLines = OrderLineBL.copy(bl.getDTO().getLines());

                // add the line to the current order
                for (OrderLineWS line : lines) {
                    bl.addItem(line.getItemId(), line.getQuantityAsDecimal(), languageId, userId, getCallerCompanyId(), currencyId, records);
                }

                // process lines to update prices and details from the source items
                processLines(bl.getEntity(), languageId, getCallerCompanyId(), userId, currencyId, pricing);
                diffLines = OrderLineBL.diffOrderLines(oldLines, bl.getDTO().getLines());

                // generate NewQuantityEvents
                bl.checkOrderLineQuantities(oldLines, bl.getDTO().getLines(), getCallerCompanyId(), bl.getDTO().getId(), true);

            } else if (records != null) {
                // Since there are no lines, run the mediation process
                // rules to create them.
                PluggableTaskManager<IMediationProcess> tm =
                        new PluggableTaskManager<IMediationProcess>(
                        getCallerCompanyId(),
                        Constants.PLUGGABLE_TASK_MEDIATION_PROCESS);
                IMediationProcess processTask = tm.getNextClass();

                MediationResult result = new MediationResult("WS", true);
                result.setUserId(userId);
                result.setEventDate(date);
                ArrayList results = new ArrayList(1);
                results.add(result);
                processTask.process(records, results, "WS");
                diffLines = result.getDiffLines();

                if (result.getCurrencyId() != null) {
                    currencyId = result.getCurrencyId();
                }

                // the mediation process might not have anything for you...
                if (result.getCurrentOrder() == null) {
                    LOG.debug("Call to updateOrder did not resolve to a current order lines = " +
                            Arrays.toString(lines) + " fields= " + Arrays.toString(fieldsArray));
                    return null;
                }
                bl.set(result.getCurrentOrder());
            } else {
                throw new SessionInternalError("Both the order lines and " +
                        "pricing fields were null. At least one of either " +
                        "must be provided.");
            }

            // save the event
            // assign to record DONE and BILLABLE status
            MediationRecordStatusDTO status = new MediationRecordStatusDAS().find(Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE);
            MediationRecordDTO record = new MediationRecordDTO(String.valueOf(date.getTime()),
                                                               new Date(),
                                                               null,
                                                               status);
            record = new MediationRecordDAS().save(record);

            IMediationSessionBean mediation = (IMediationSessionBean) Context.getBean(Context.Name.MEDIATION_SESSION);
            mediation.saveEventRecordLines(new ArrayList<OrderLineDTO>(diffLines), record, date,eventDescription);

            // return the updated order
            return bl.getWS(languageId);

        } catch (Exception e) {
            LOG.error("WS - getCurrentOrder", e);
            throw new SessionInternalError("Error updating current order");
        }
    }

    public OrderWS[] getUserSubscriptions(Integer userId) throws SessionInternalError {
    	if (userId == null) throw new SessionInternalError("User Id cannot be null.");

        List<OrderDTO> subscriptions= new OrderDAS().findByUserSubscriptions(userId);
        if (null == subscriptions)
        {
        	return new OrderWS[0];
        }
        OrderWS[] orderArr= new OrderWS[subscriptions.size()];
        OrderBL bl = null;
        for(OrderDTO dto: subscriptions) {
        	bl= new OrderBL(dto);
        	orderArr[subscriptions.indexOf(dto)]= bl.getWS(getCallerLanguageId());
        }

		return orderArr;
	}

    public boolean updateOrderPeriods(OrderPeriodWS[] orderPeriods) throws SessionInternalError {
        //IOrderSessionBean orderSession = Context.getBean(Context.Name.ORDER_SESSION);

		List<OrderPeriodDTO> periodDtos= new ArrayList<OrderPeriodDTO>(orderPeriods.length);
		OrderPeriodDAS periodDas= new OrderPeriodDAS();
		OrderPeriodDTO periodDto= null;
		for (OrderPeriodWS periodWS: orderPeriods) {
			if ( null != periodWS.getId()) {
				periodDto= periodDas.find(periodWS.getId());
			} 
			if ( null == periodDto ) {
				periodDto= new OrderPeriodDTO();
				periodDto.setCompany(new CompanyDAS().find(getCallerCompanyId()));
				//periodDto.setVersionNum(new Integer(0));
			}
			periodDto.setValue(periodWS.getValue());
			if (null != periodWS.getPeriodUnitId()) {
				periodDto.setUnitId(periodWS.getPeriodUnitId().intValue());
			}
			periodDto= periodDas.save(periodDto);
			if (periodWS.getDescriptions() != null && periodWS.getDescriptions().size() > 0 ) {
				periodDto.setDescription(((InternationalDescriptionWS)periodWS.getDescriptions().get(0)).getContent(), ((InternationalDescriptionWS)periodWS.getDescriptions().get(0)).getLanguageId());
			}
			LOG.debug("Converted to DTO: " + periodDto);
			periodDas.flush();
			periodDas.clear();
 			//periodDtos.add(periodDto);
			periodDto= null;
		}
        //orderSession.setPeriods(getCallerLanguageId(), periodDtos.toArray(new OrderPeriodDTO[periodDtos.size()]));
        return true;
    }

    public boolean deleteOrderPeriod(Integer periodId) throws SessionInternalError {
    	IOrderSessionBean orderSession = Context.getBean(Context.Name.ORDER_SESSION);
    	return orderSession.deletePeriod(periodId);
    }
    
    /*
     * PAYMENT
     */

    public Integer createPayment(PaymentWS payment) {
        return applyPayment(payment, null);
    }

    public void updatePayment(PaymentWS payment) {
        PaymentDTOEx dto = new PaymentDTOEx(payment);
        new PaymentBL(payment.getId()).update(getCallerId(), dto);
    }

    public void deletePayment(Integer paymentId) {
        new PaymentBL(paymentId).delete();
    }

    /**
     * Enters a payment and applies it to the given invoice. This method DOES NOT process
     * the payment but only creates it as 'Entered'. The entered payment will later be
     * processed by the billing process.
     *
     * Invoice ID is optional. If no invoice ID is given the payment will be applied to
     * the payment user's account according to the configured entity preferences.
     *
     * @param payment payment to apply
     * @param invoiceId invoice id
     * @return created payment id
     * @throws SessionInternalError
     */
    public Integer applyPayment(PaymentWS payment, Integer invoiceId)
            throws SessionInternalError {
        validatePayment(payment);
        payment.setIsRefund(0);

        if (payment.getMethodId() == null) {
            throw new SessionInternalError("Cannot apply a payment without a payment method.",
                                           new String[] { "PaymentWS,paymentMethodId,validation.error.apply.without.method" });
        }

        IPaymentSessionBean session = (IPaymentSessionBean) Context.getBean(Context.Name.PAYMENT_SESSION);
        return session.applyPayment(new PaymentDTOEx(payment), invoiceId);
    }

    /**
     * Processes a payment and applies it to the given invoice. This method will actively
     * processes the payment using the configured payment plug-in.
     *
     * Payment is optional when an invoice ID is provided. If no payment is given, the payment
     * will be processed using the invoiced user's configured "automatic payment" instrument.
     *
     * Invoice ID is optional. If no invoice ID is given the payment will be applied to the
     * payment user's account according to the configured entity preferences.
     *
     * @param payment payment to process
     * @param invoiceId invoice id
     * @return payment authorization from the payment processor
     */
    public PaymentAuthorizationDTOEx processPayment(PaymentWS payment, Integer invoiceId) {
        if (payment == null && invoiceId != null)
            return payInvoice(invoiceId);

        validatePayment(payment);
        Integer entityId = getCallerCompanyId();
        PaymentDTOEx dto = new PaymentDTOEx(payment);

        // payment without Credit Card or ACH, fetch the users primary payment instrument for use
        if (payment.getCreditCard() == null && payment.getAch() == null) {
            LOG.debug("processPayment() called without payment method, fetching users automatic payment instrument.");
            PaymentDTO instrument;
            try {
                instrument = PaymentBL.findPaymentInstrument(entityId, payment.getUserId());

            } catch (PluggableTaskException e) {
                throw new SessionInternalError("Exception occurred fetching payment info plug-in.",
                                               new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });

            } catch (TaskException e) {
                throw new SessionInternalError("Exception occurred with plug-in when fetching payment instrument.",
                                               new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });
            }

            if (instrument == null || (instrument.getCreditCard() == null && instrument.getAch() == null)) {
                throw new SessionInternalError("User " + payment.getUserId() + "does not have a default payment instrument.",
                                               new String[] { "PaymentWS,baseUserId,validation.error.no.payment.instrument" });
            }

            dto.setCreditCard(instrument.getCreditCard());
            dto.setAch(instrument.getAch());
        }

        // populate payment method based on the payment instrument
        if (dto.getCreditCard() != null) {    
            dto.setPaymentMethod(new PaymentMethodDTO(dto.getCreditCard().getCcType()));
        } else if (dto.getAch() != null) { 
            dto.setPaymentMethod(new PaymentMethodDTO(Constants.PAYMENT_METHOD_ACH));
        }

        // process payment
        IPaymentSessionBean session = (IPaymentSessionBean) Context.getBean(Context.Name.PAYMENT_SESSION);
        Integer result = session.processAndUpdateInvoice(dto, null, entityId);
        LOG.debug("paymentBean.processAndUpdateInvoice() Id=" + result);

        PaymentAuthorizationDTOEx auth = null;
        if (dto != null && dto.getAuthorization() != null) {
            LOG.debug("PaymentAuthorizationDTO Id =" + dto.getAuthorization().getId());
            auth = new PaymentAuthorizationDTOEx(dto.getAuthorization().getOldDTO());
            LOG.debug("PaymentAuthorizationDTOEx Id =" + auth.getId());
            auth.setResult(result.equals(Constants.RESULT_OK));

        } else {
            auth = new PaymentAuthorizationDTOEx();
            auth.setResult(result.equals(Constants.RESULT_FAIL));
        }
        return auth;
    }

    public PaymentWS getPayment(Integer paymentId)
            throws SessionInternalError {
        // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        PaymentBL bl = new PaymentBL(paymentId);
        return PaymentBL.getWS(bl.getDTOEx(languageId));
    }

    public PaymentWS getLatestPayment(Integer userId) throws SessionInternalError {
        PaymentWS retValue = null;
        // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        PaymentBL bl = new PaymentBL();
        Integer paymentId = bl.getLatest(userId);
        if (paymentId != null) {
            bl.set(paymentId);
            retValue = PaymentBL.getWS(bl.getDTOEx(languageId));
        }
        return retValue;
    }

    public Integer[] getLastPayments(Integer userId, Integer number) throws SessionInternalError {
        if (userId == null || number == null) {
            return null;
        }
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        PaymentBL payment = new PaymentBL();
        return payment.getManyWS(userId, number, languageId);
    }

    public PaymentWS getUserPaymentInstrument(Integer userId) throws SessionInternalError {
        PaymentDTO instrument;
        try {
            instrument = PaymentBL.findPaymentInstrument(getCallerCompanyId(), userId);
        } catch (PluggableTaskException e) {
            throw new SessionInternalError("Exception occurred fetching payment info plug-in.", e);
        } catch (TaskException e) {
            throw new SessionInternalError("Exception occurred with plug-in when fetching payment instrument.", e);
        }

        return instrument != null ? PaymentBL.getWS(new PaymentDTOEx(instrument)) : null;
    }

    public BigDecimal getTotalRevenueByUser (Integer userId) throws SessionInternalError {
    	return new PaymentDAS().findTotalRevenueByUser(userId);
    }

    /*
     * ITEM
     */
    public Integer createItem(ItemDTOEx item) throws SessionInternalError {
        ItemBL itemBL = new ItemBL();
        ItemDTO dto = itemBL.getDTO(item);
        
        // get the info from the caller
        UserBL bl = new UserBL(getCallerId());
        Integer languageId = bl.getEntity().getLanguageIdField();
        Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
        dto.setEntity(new CompanyDTO(entityId));

        // call the creation
        return itemBL.create(dto, languageId);
    }

    /**
     * Retrieves an array of items for the caller's entity.
     * @return an array of items from the caller's entity
     */
    public ItemDTOEx[] getAllItems() throws SessionInternalError {
        Integer entityId = getCallerCompanyId();
        ItemBL itemBL = new ItemBL();
        return itemBL.getAllItems(entityId);
    }

    /**
     * Implementation of the User Transitions List webservice. This accepts a
     * start and end date as arguments, and produces an array of data containing
     * the user transitions logged in the requested time range.
     * @param from Date indicating the lower limit for the extraction of transition
     * logs. It can be <code>null</code>, in such a case, the extraction will start
     * where the last extraction left off. If no extractions have been done so far and
     * this parameter is null, the function will extract from the oldest transition
     * logged.
     * @param to Date indicatin the upper limit for the extraction of transition logs.
     * It can be <code>null</code>, in which case the extraction will have no upper
     * limit.
     * @return UserTransitionResponseWS[] an array of objects containing the result
     * of the extraction, or <code>null</code> if there is no data thas satisfies
     * the extraction parameters.
     */
    public UserTransitionResponseWS[] getUserTransitions(Date from, Date to)
            throws SessionInternalError {

        UserTransitionResponseWS[] result = null;
        Integer last = null;
        // Obtain the current entity and language Ids

        UserBL user = new UserBL();
        Integer callerId = getCallerId();
        Integer entityId = getCallerCompanyId();
        EventLogger evLog = EventLogger.getInstance();

        if (from == null) {
            last = evLog.getLastTransitionEvent(entityId);
        }

        if (last != null) {
            result = user.getUserTransitionsById(entityId, last, to);
        } else {
            result = user.getUserTransitionsByDate(entityId, from, to);
        }

        if (result == null) {
            LOG.info("Data retrieved but resultset is null");
        } else {
            LOG.info("Data retrieved. Result size = " + result.length);
        }

        // Log the last value returned if there was any. This happens always,
        // unless the returned array is empty.
        if (result != null && result.length > 0) {
            LOG.info("Registering transition list event");
            evLog.audit(callerId, null, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                    EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                    result[0].getId().toString(), null);
        }
        return result;
    }

    /**
     * @return UserTransitionResponseWS[] an array of objects containing the result
     * of the extraction, or <code>null</code> if there is no data thas satisfies
     * the extraction parameters.
     */
    public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id)
            throws SessionInternalError {

        UserTransitionResponseWS[] result = null;
        // Obtain the current entity and language Ids

        UserBL user = new UserBL();
        Integer callerId = getCallerId();
        Integer entityId = getCallerCompanyId();
        EventLogger evLog = EventLogger.getInstance();

        result = user.getUserTransitionsById(entityId, id, null);

        if (result == null) {
            LOG.debug("Data retrieved but resultset is null");
        } else {
            LOG.debug("Data retrieved. Result size = " + result.length);
        }

        // Log the last value returned if there was any. This happens always,
        // unless the returned array is empty.
        if (result != null && result.length > 0) {
            LOG.debug("Registering transition list event");
            evLog.audit(callerId, null, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                    EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                    result[0].getId().toString(), null);
        }
        return result;
    }

    public ItemDTOEx getItem(Integer itemId, Integer userId, String pricing) {
        PricingField[] fields = PricingField.getPricingFieldsValue(pricing);

        ItemBL helper = new ItemBL(itemId);
        List<PricingField> f = new ArrayList<PricingField>();
        if (fields != null) f.addAll(Arrays.asList(fields));
        helper.setPricingFields(f);

        UserBL caller = new UserBL(getCallerId());
        Integer callerId = caller.getEntity().getUserId();
        Integer entityId = caller.getEntityId(callerId);
        Integer languageId = caller.getEntity().getLanguageIdField();

        // use the currency of the given user if provided, otherwise
        // default to the currency of the caller (admin user)
        Integer currencyId = (userId != null
                              ? new UserBL(userId).getCurrencyId()
                              : caller.getCurrencyId());

        ItemDTOEx retValue = helper.getWS(helper.getDTO(languageId, userId, entityId, currencyId));
        return retValue;
    }

    public Integer createItemCategory(ItemTypeWS itemType)
            throws SessionInternalError {

        UserBL bl = new UserBL(getCallerId());
        Integer entityId = bl.getEntityId(bl.getEntity().getUserId());

        ItemTypeDTO dto = new ItemTypeDTO();
        dto.setDescription(itemType.getDescription());
        dto.setOrderLineTypeId(itemType.getOrderLineTypeId());
        dto.setEntity(new CompanyDTO(entityId));

        ItemTypeBL itemTypeBL = new ItemTypeBL();
        itemTypeBL.create(dto);
        return itemTypeBL.getEntity().getId();
    }

    public void updateItemCategory(ItemTypeWS itemType) throws SessionInternalError {
        UserBL bl = new UserBL(getCallerId());
        Integer executorId = bl.getEntity().getUserId();

        ItemTypeBL itemTypeBL = new ItemTypeBL(itemType.getId());

        ItemTypeDTO dto = new ItemTypeDTO();
        dto.setDescription(itemType.getDescription());
        dto.setOrderLineTypeId(itemType.getOrderLineTypeId());

        itemTypeBL.update(executorId, dto);
    }

    private Integer zero2null(Integer var) {
        if (var != null && var.intValue() == 0) {
            return null;
        } else {
            return var;
        }
    }

    private Date zero2null(Date var) {
        if (var != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(var);
            if (cal.get(Calendar.YEAR) == 1) {
                return null;
            }
        }

        return var;

    }

    private void validateUser(UserWS newUser)
            throws SessionInternalError {
        // do the validation
        if (newUser == null) {
            throw new SessionInternalError("Null parameter");
        }
        // C# sends a 0 when it is null ...
        newUser.setCurrencyId(zero2null(newUser.getCurrencyId()));
        newUser.setPartnerId(zero2null(newUser.getPartnerId()));
        newUser.setParentId(zero2null(newUser.getParentId()));
        newUser.setMainRoleId(zero2null(newUser.getMainRoleId()));
        newUser.setLanguageId(zero2null(newUser.getLanguageId()));
        newUser.setStatusId(zero2null(newUser.getStatusId()));
        // clean up the cc number from spaces and '-'
        if (newUser.getCreditCard() != null &&
                newUser.getCreditCard().getNumber() != null) {
            newUser.getCreditCard().setNumber(CreditCardBL.cleanUpNumber(
                    newUser.getCreditCard().getNumber()));
        }

        // todo: additional hibernate validations
        // additional validation
        if (newUser.getMainRoleId().equals(Constants.TYPE_CUSTOMER) ||
                newUser.getMainRoleId().equals(Constants.TYPE_PARTNER)) {
        } else {
            throw new SessionInternalError("Valid user roles are customer (5) " +
                    "and partner (4)");
        }
        if (newUser.getCurrencyId() != null &&
                newUser.getCurrencyId().intValue() <= 0) {
            throw new SessionInternalError("Invalid currency code");
        }
        if (newUser.getStatusId().intValue() <= 0) {
            throw new SessionInternalError("Invalid status code");
        }
    }

    private void validateOrder(OrderWS order) throws SessionInternalError {
        if (order == null) {
            throw new SessionInternalError("Null parameter");
        }

        order.setUserId(zero2null(order.getUserId()));
        order.setPeriod(zero2null(order.getPeriod()));
        order.setBillingTypeId(zero2null(order.getBillingTypeId()));
        order.setStatusId(zero2null(order.getStatusId()));
        order.setCurrencyId(zero2null(order.getCurrencyId()));
        order.setNotificationStep(zero2null(order.getNotificationStep()));
        order.setDueDateUnitId(zero2null(order.getDueDateUnitId()));
        order.setDueDateValue(zero2null(order.getDueDateValue()));
        order.setDfFm(zero2null(order.getDfFm()));
        order.setAnticipatePeriods(zero2null(order.getAnticipatePeriods()));
        order.setActiveSince(zero2null(order.getActiveSince()));
        order.setActiveUntil(zero2null(order.getActiveUntil()));
        order.setNextBillableDay(zero2null(order.getNextBillableDay()));
        order.setLastNotified(null);

        // CXF seems to pass empty array as null
        if (order.getOrderLines() == null) {
            order.setOrderLines(new OrderLineWS[0]);
        }

        // todo: additional hibernate validations
        // the lines
        for (int f = 0; f < order.getOrderLines().length; f++) {
            OrderLineWS line = order.getOrderLines()[f];
            if (line.getUseItem() == null) {
            line.setUseItem(false);
            }
            line.setItemId(zero2null(line.getItemId()));
            String error = "";
            // if use the item, I need the item id
            if (line.getUseItem()) {
                if (line.getItemId() == null || line.getItemId().intValue() == 0) {
                    error += "OrderLineWS: if useItem == true the itemId is required - ";
                }
                if (line.getQuantityAsDecimal() == null || BigDecimal.ZERO.compareTo(line.getQuantityAsDecimal()) == 0) {
                    error += "OrderLineWS: if useItem == true the quantity is required - ";
                }
            } else {
                // I need the amount and description
                if (line.getAmount() == null) {
                    error += "OrderLineWS: if useItem == false the item amount " +
                            "is required - ";
                }
                if (line.getDescription() == null ||
                        line.getDescription().length() == 0) {
                    error += "OrderLineWS: if useItem == false the description " +
                            "is required - ";
                }
            }
            if (error.length() > 0) {
                throw new SessionInternalError(error);
            }
        }
    }

    private void validatePayment(PaymentWS payment)
            throws SessionInternalError {
        if (payment == null) {
            throw new SessionInternalError("Null parameter");
        }
        payment.setBaseUserId(payment.getBaseUserId());
        payment.setMethodId(payment.getMethodId());
        payment.setCurrencyId(payment.getCurrencyId());
        payment.setPaymentId(payment.getPaymentId());

        // todo: additional hibernate validations
            }

    private InvoiceDTO doCreateInvoice(Integer orderId) {
        try {
            BillingProcessBL process = new BillingProcessBL();
            InvoiceDTO invoice = process.generateInvoice(orderId, null);
            return invoice;
        } catch (Exception e) {
            LOG.error("WS - create invoice:", e);
            throw new SessionInternalError("Error while generating a new invoice");
        }
    }

    private PaymentDTOEx doPayInvoice(InvoiceDTO invoice, CreditCardDTO creditCard)
            throws SessionInternalError {

        if (invoice.getBalance() == null || BigDecimal.ZERO.compareTo(invoice.getBalance()) >= 0) {
            LOG.warn("Can not pay invoice: " + invoice.getId() + ", balance: " + invoice.getBalance());
            return null;
        }

        IPaymentSessionBean payment = (IPaymentSessionBean) Context.getBean(
                Context.Name.PAYMENT_SESSION);
        PaymentDTOEx paymentDto = new PaymentDTOEx();
        paymentDto.setIsRefund(0);
        paymentDto.setAmount(invoice.getBalance());
        paymentDto.setCreditCard(creditCard);
        paymentDto.setCurrency(new CurrencyDAS().find(invoice.getCurrency().getId()));
        paymentDto.setUserId(invoice.getBaseUser().getUserId());
        paymentDto.setPaymentMethod(new PaymentMethodDAS().find(
                com.sapienter.jbilling.common.Util.getPaymentMethod(
                creditCard.getNumber())));
        paymentDto.setPaymentDate(new Date());

        // make the call
        payment.processAndUpdateInvoice(paymentDto, invoice);

        return paymentDto;
    }

    /**
     * Conveniance method to find a credit card
     */
    private CreditCardDTO getCreditCard(Integer userId) {
        if (userId == null) {
            return null;
        }

        CreditCardDTO result = null;
        try {
            UserBL user = new UserBL(userId);
            Integer entityId = user.getEntityId(userId);
            if (user.hasCreditCard()) {
                // find it
                PaymentDTOEx paymentDto = PaymentBL.findPaymentInstrument(
                        entityId, userId);
                // it might have a credit card, but it might not be valid or
                // just not found by the plug-in
                if (paymentDto != null) {
                    result = paymentDto.getCreditCard();
                }
            }
        } catch (Exception e) { // forced by checked exceptions :(
            LOG.error("WS - finding a credit card", e);
            throw new SessionInternalError("Error finding a credit card for user: " + userId);
        }

        return result;
    }

    private OrderWS doCreateOrder(OrderWS order, boolean create)
            throws SessionInternalError {

        validateOrder(order);
        // get the info from the caller
        UserBL bl = new UserBL(getCallerId());
        Integer executorId = bl.getEntity().getUserId();
        Integer entityId = bl.getEntityId(bl.getEntity().getUserId());

        // we'll need the langauge later
        bl.set(order.getUserId());
        Integer languageId = bl.getEntity().getLanguageIdField();

        // convert to a DTO
        OrderBL orderBL = new OrderBL();
        OrderDTO dto = orderBL.getDTO(order);

        // process the lines and let the items provide the order line details
        LOG.debug("Processing order lines");
        processLines(dto, languageId, entityId, order.getUserId(), order.getCurrencyId(), order.getPricingFields());

        LOG.info("before cycle start");
        // set a default cycle starts if needed (obtained from the main
        // subscription order, if it exists)
        if (dto.getCycleStarts() == null && dto.getIsCurrent() == null) {
            Integer mainOrderId = orderBL.getMainOrderId(dto.getUser().getId());
            if (mainOrderId != null) {
                // only set a default if preference use current order is set
                PreferenceBL preferenceBL = new PreferenceBL();
                try {
                    preferenceBL.set(entityId, Constants.PREFERENCE_USE_CURRENT_ORDER);
                } catch (EmptyResultDataAccessException e) {
                    // default preference will be used
                    }
                if (preferenceBL.getInt() != 0) {
                    OrderDAS das = new OrderDAS();
                    OrderDTO mainOrder = das.findNow(mainOrderId);
                    LOG.debug("Copying cycle starts from main order");
                    dto.setCycleStarts(mainOrder.getCycleStarts());
                }
            }
        }

        orderBL.set(dto);
        orderBL.recalculate(entityId);

        if (create) {
            LOG.debug("creating order");

            dto.setId(null);
            dto.setVersionNum(null);
            for (OrderLineDTO line : dto.getLines()) {
                line.setId(0);
                line.setVersionNum(null);
            }

            Integer id = orderBL.create(entityId, executorId, dto);
            orderBL.set(id);
            return orderBL.getWS(languageId);
        }
        return getWSFromOrder(orderBL, languageId);
    }

    private OrderWS getWSFromOrder(OrderBL bl, Integer languageId) {
        OrderDTO order = bl.getDTO();
        OrderWS retValue = new OrderWS(order.getId(), order.getBillingTypeId(),
                order.getNotify(), order.getActiveSince(), order.getActiveUntil(),
                order.getCreateDate(), order.getNextBillableDay(),
                order.getCreatedBy(), order.getStatusId(), order.getDeleted(),
                order.getCurrencyId(), order.getLastNotified(),
                order.getNotificationStep(), order.getDueDateUnitId(),
                order.getDueDateValue(), order.getAnticipatePeriods(),
                order.getDfFm(), order.getIsCurrent(), order.getNotes(),
                order.getNotesInInvoice(), order.getOwnInvoice(),
                order.getOrderPeriod().getId(),
                order.getBaseUserByUserId().getId(),
                order.getVersionNum(), order.getCycleStarts());

        retValue.setPeriodStr(order.getOrderPeriod().getDescription(languageId));
        retValue.setBillingTypeStr(order.getOrderBillingType().getDescription(languageId));
        retValue.setTotal(order.getTotal());

        List<OrderLineWS> lines = new ArrayList<OrderLineWS>();
        for (Iterator<OrderLineDTO> it = order.getLines().iterator(); it.hasNext();) {
            OrderLineDTO line = (OrderLineDTO) it.next();
            LOG.info("copying line: " + line);
            if (line.getDeleted() == 0) {

                OrderLineWS lineWS = new OrderLineWS(line.getId(), line.getItem().getId(), line.getDescription(),
                        line.getAmount(), line.getQuantity(), line.getPrice(),
                        line.getCreateDatetime(), line.getDeleted(), line.getOrderLineType().getId(),
                        line.getEditable(), (line.getPurchaseOrder() != null?line.getPurchaseOrder().getId():null),
                        null, line.getVersionNum(),line.getProvisioningStatusId(),line.getProvisioningRequestId());

                lines.add(lineWS);
            }
        }
        retValue.setOrderLines(new OrderLineWS[lines.size()]);
        lines.toArray(retValue.getOrderLines());
        return retValue;
    }

    private InvoiceDTO findInvoice(Integer invoiceId) {
        final InvoiceDTO invoice;
        invoice = new InvoiceBL(invoiceId).getEntity();
        return invoice;
    }

    // TODO: This method is not secured or in a jUnit test
    public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId)
            throws SessionInternalError {
        InvoiceWS retValue = null;
        try {
            if (userId == null) {
                return null;
            }
            InvoiceBL bl = new InvoiceBL();
            Integer invoiceId = bl.getLastByUserAndItemType(userId, itemTypeId);
            if (invoiceId != null) {
                retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
            }
            return retValue;
        } catch (Exception e) { // forced by SQLException
            LOG.error("Exception in web service: getting latest invoice" +
                    " for user " + userId, e);
            throw new SessionInternalError("Error getting latest invoice");
        }
    }

    /**
     * Return 'number' most recent invoices that contain a line item with an
     * item of the given item type.
     */
    // TODO: This method is not secured or in a jUnit test
    public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number)
            throws SessionInternalError {
        if (userId == null || itemTypeId == null || number == null) {
            return null;
        }

        InvoiceBL bl = new InvoiceBL();
        return bl.getManyByItemTypeWS(userId, itemTypeId, number);
    }

    // TODO: This method is not secured or in a jUnit test
    public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId)
            throws SessionInternalError {
        if (userId == null) {
            throw new SessionInternalError("User id can not be null");
        }
        if (itemTypeId == null) {
            throw new SessionInternalError("itemTypeId can not be null");
        }
        OrderWS retValue = null;
        // get the info from the caller
        UserBL userbl = new UserBL(getCallerId());
        Integer languageId = userbl.getEntity().getLanguageIdField();

        // now get the order
        OrderBL bl = new OrderBL();
        Integer orderId = bl.getLatestByItemType(userId, itemTypeId);
        if (orderId != null) {
            bl.set(orderId);
            retValue = bl.getWS(languageId);
        }
        return retValue;
    }

    // TODO: This method is not secured or in a jUnit test
    public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number)
            throws SessionInternalError {
        if (userId == null || number == null) {
            return null;
        }
        OrderBL order = new OrderBL();
        return order.getListIdsByItemType(userId, itemTypeId, number);
    }

    public String isUserSubscribedTo(Integer userId, Integer itemId) {
        OrderDAS das = new OrderDAS();
        BigDecimal quantity = das.findIsUserSubscribedTo(userId, itemId);
        return quantity != null ? quantity.toString() : null;
    }

    public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId) {
        Integer[] result = null;
        OrderDAS das = new OrderDAS();
        result = das.findUserItemsByCategory(userId, categoryId);
        return result;
    }

    public ItemDTOEx[] getItemByCategory(Integer itemTypeId) {
        return new ItemBL().getAllItemsByType(itemTypeId);
    }

    public ItemTypeWS[] getAllItemCategories() {
        return new ItemTypeBL().getAllItemTypes();
    }

    public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId,
            String fields) {
        Integer[] itemIds = null;
        if (itemId != null) {
            itemIds = new Integer[] { itemId };
        }

        String[] fieldsArray = null;
        if (fields != null) {
            fieldsArray = new String[] { fields };
        }

        return doValidatePurchase(userId, itemIds, fieldsArray);
    }

    public ValidatePurchaseWS validateMultiPurchase(Integer userId,
            Integer[] itemIds, String[] fields) {

        return doValidatePurchase(userId, itemIds, fields);
    }

    private ValidatePurchaseWS doValidatePurchase(Integer userId, Integer[] itemIds, String[] fields) {

        if (userId == null || (itemIds == null && fields == null)) {
            return null;
        }

        List<List<PricingField>> fieldsList = null;
        if (fields != null) {
            fieldsList = new ArrayList<List<PricingField>>(fields.length);
            for (int i = 0; i < fields.length; i++) {
                fieldsList.add(new ArrayList(Arrays.asList(PricingField.getPricingFieldsValue(fields[i]))));
            }
        }

        List<Integer> itemIdsList = null;
        List<BigDecimal> prices = new ArrayList<BigDecimal>();
        List<ItemDTO> items = new ArrayList<ItemDTO>();

        if (itemIds != null) {
            itemIdsList = new ArrayList(Arrays.asList(itemIds));
        } else if (fields != null) {
            itemIdsList = new LinkedList<Integer>();

            for (List<PricingField> pricingFields : fieldsList) {
                try {
                    // Since there is no item, run the mediation process rules
                    // to create line/s. This will run pricing and
                    // item management rules as well

                    // fields need to be in records
                    Record record = new Record();
                    for (PricingField field : pricingFields) {
                        record.addField(field, false); // don't care about isKey
                    }
                    List<Record> records = new ArrayList<Record>(1);
                    records.add(record);

                    PluggableTaskManager<IMediationProcess> tm
                            = new PluggableTaskManager<IMediationProcess>(getCallerCompanyId(),
                                                                          Constants.PLUGGABLE_TASK_MEDIATION_PROCESS);

                    IMediationProcess processTask = tm.getNextClass();

                    MediationResult result = new MediationResult("WS", false);
                    result.setUserId(userId);
                    result.setEventDate(new Date());
                    ArrayList results = new ArrayList(1);
                    results.add(result);
                    processTask.process(records, results, "WS");

                    // from the lines, get the items and prices
                    for (OrderLineDTO line : result.getDiffLines()) {
                        items.add(new ItemBL(line.getItemId()).getEntity());
                        prices.add(line.getAmount());
                    }

                } catch (Exception e) {
                    // log stacktrace
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.close();
                    LOG.error("Validate Purchase error: " + e.getMessage() + "\n" + sw.toString());

                    ValidatePurchaseWS result = new ValidatePurchaseWS();
                    result.setSuccess(false);
                    result.setAuthorized(false);
                    result.setQuantity(BigDecimal.ZERO);
                    result.setMessage(new String[] { "Error: " + e.getMessage() } );

                    return result;
                }
            }
        } else {
            return null;
        }

        // find the prices first
        // this will do nothing if the mediation process was uses. In that case
        // the itemIdsList will be empty
        int itemNum = 0;
        for (Integer itemId : itemIdsList) {
            ItemBL item = new ItemBL(itemId);

            if (fieldsList != null && !fieldsList.isEmpty()) {
                int fieldsIndex = itemNum;
                // just get first set of fields if only one set
                // for many items
                if (fieldsIndex > fieldsList.size()) {
                    fieldsIndex = 0;
                }
                item.setPricingFields(fieldsList.get(fieldsIndex));
            }

            // todo: validate purchase should include the quantity purchased for validations
            prices.add(item.getPrice(userId, BigDecimal.ONE, getCallerCompanyId()));
            items.add(item.getEntity());
            itemNum++;
        }

        ValidatePurchaseWS ret = new UserBL(userId).validatePurchase(items, prices, fieldsList);
        return ret;
    }


    /**
     * Updates a users stored credit card.
     *
     * @param userId user to update
     * @param creditCard credit card details
     * @throws SessionInternalError
     */
    public void updateCreditCard(Integer userId, com.sapienter.jbilling.server.entity.CreditCardDTO creditCard)
            throws SessionInternalError {

        if (creditCard == null)
            return;

        if (creditCard.getName() == null || creditCard.getExpiry() == null)
            throw new SessionInternalError("Missing credit card name or expiry date");

        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        userSession.updateCreditCard(getCallerId(), userId, new CreditCardDTO(creditCard));
    }

    /**
     * Deletes a users stored credit card. Payments that were made using the deleted credit
     * card will not be affected.
     *
     * @param userId user to delete the credit card from
     */
    public void deleteCreditCard(Integer userId) {
        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        userSession.deleteCreditCard(getCallerId(), userId);
    }

    /**
     * Updates a users stored ACH details.
     *
     * @param userId user to update
     * @param ach ach details
     * @throws SessionInternalError
     */
    public void updateAch(Integer userId, com.sapienter.jbilling.server.entity.AchDTO ach)
            throws SessionInternalError {

        if (ach == null)
            return;

        if (ach.getAbaRouting() == null || ach.getBankAccount() == null)
            throw new SessionInternalError("Missing ACH routing number of bank account number.");

        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        userSession.updateACH(userId, getCallerId(), new AchDTO(ach));
    }

    /**
     * Deletes a users stored ACH details. Payments that were made using the deleted ACH
     * details will not be affected.
     *
     * @param userId user to delete the ACH details from.
     */
    public void deleteAch(Integer userId) {
        IUserSessionBean userSession = Context.getBean(Context.Name.USER_SESSION);
        userSession.removeACH(userId, getCallerId());
    }

    public Integer getAuthPaymentType(Integer userId)
            throws SessionInternalError {

        IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                Context.Name.USER_SESSION);
        return sess.getAuthPaymentType(userId);
    }

    public void setAuthPaymentType(Integer userId, Integer autoPaymentType, boolean use)
            throws SessionInternalError {

        IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                Context.Name.USER_SESSION);
        sess.setAuthPaymentType(userId, autoPaymentType, use);
    }

    public AgeingWS[] getAgeingConfiguration(Integer languageId) throws SessionInternalError {
	    try {
		    IBillingProcessSessionBean processSession = 
		    	(IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
		    AgeingDTOEx[] dtoArr= processSession.getAgeingSteps(getCallerCompanyId(), getCallerLanguageId(), languageId);
		    AgeingWS[] wsArr= new AgeingWS[dtoArr.length];
		    AgeingBL bl= new AgeingBL();
		    for (int i = 0; i < wsArr.length; i++) {
				wsArr[i]= bl.getWS(dtoArr[i]);
			}
		    return wsArr;
	    } catch (Exception e) {
	    	throw new SessionInternalError(e);
	    }
    }

    public void saveAgeingConfiguration(AgeingWS[] steps, Integer gracePeriod, Integer languageId) throws SessionInternalError { 
    	AgeingBL bl= new AgeingBL();
    	AgeingDTOEx[] dtoList= new AgeingDTOEx[steps.length];
	    for (int i = 0; i < steps.length; i++) {
	    	dtoList[i]= bl.getDTOEx(steps[i]);
		}
	    IBillingProcessSessionBean processSession = 
	    (IBillingProcessSessionBean) Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
	    processSession.setAgeingSteps (getCallerCompanyId(), languageId, bl.validate(dtoList));
	
	    // update the grace period in another call
	    IUserSessionBean userSession = (IUserSessionBean) Context.getBean(Context.Name.USER_SESSION);
	    userSession.setEntityParameter(getCallerCompanyId(),
                                       Constants.PREFERENCE_GRACE_PERIOD,
                                       (gracePeriod != null ? gracePeriod.toString() : null));
    }
    
    /*
        Billing process
     */

    public boolean isBillingRunning() {
    	IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        return processBean.isBillingRunning();
	}
    
    public void triggerBillingAsync(final Date runDate) {
    	Thread t =new Thread(new Runnable(){
	   		IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
		    public void run()
		    {
		    	 processBean.trigger(runDate);
		    }
	    });
	 
	    t.start();
    }
    
    public boolean triggerBilling(Date runDate) {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        return processBean.trigger(runDate);
    }

    public void triggerAgeing(Date runDate) {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        processBean.reviewUsersStatus(getCallerCompanyId(), runDate);
    }

    public BillingProcessConfigurationWS getBillingProcessConfiguration() throws SessionInternalError {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        BillingProcessConfigurationDTO configuration = processBean.getConfigurationDto(getCallerCompanyId());

        return ConfigurationBL.getWS(configuration);
    }

    public Integer createUpdateBillingProcessConfiguration(BillingProcessConfigurationWS ws)
            throws SessionInternalError {

    	//validation
    	if (!ConfigurationBL.validate(ws)) {
    		throw new SessionInternalError("Error: Invalid Next Run Date.");
    	}
        BillingProcessConfigurationDTO dto = ConfigurationBL.getDTO(ws);

        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        return processBean.createUpdateConfiguration(getCallerId(), dto);
    }

    public BillingProcessWS getBillingProcess(Integer processId) {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        BillingProcessDTOEx dto = processBean.getDto(processId, getCallerLanguageId());

        return BillingProcessBL.getWS(dto);
    }

    public Integer getLastBillingProcess() throws SessionInternalError {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        return processBean.getLast(getCallerCompanyId());
    }

    public List<OrderProcessWS> getOrderProcesses(Integer orderId) {
        OrderDTO order = new OrderBL(orderId).getDTO();

        if (order == null)
            return Collections.emptyList();

        List<OrderProcessWS> ws = new ArrayList<OrderProcessWS>(order.getOrderProcesses().size());
        for (OrderProcessDTO process : order.getOrderProcesses())
            ws.add(new OrderProcessWS(process));

        return ws;
    }

    public List<OrderProcessWS> getOrderProcessesByInvoice(Integer invoiceId) {
        InvoiceDTO invoice = new InvoiceBL(invoiceId).getDTO();

        if (invoice == null)
            return Collections.emptyList();

        List<OrderProcessWS> ws = new ArrayList<OrderProcessWS>(invoice.getOrderProcesses().size());
        for (OrderProcessDTO process : invoice.getOrderProcesses())
            ws.add(new OrderProcessWS(process));

        return ws;
    }

    public BillingProcessWS getReviewBillingProcess() {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        BillingProcessDTOEx dto = processBean.getReviewDto(getCallerCompanyId(), getCallerLanguageId());

        return BillingProcessBL.getWS(dto);
    }

    public BillingProcessConfigurationWS setReviewApproval(Boolean flag) throws SessionInternalError {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);
        BillingProcessConfigurationDTO dto = processBean.setReviewApproval(getCallerId(), getCallerCompanyId(), flag);

        return ConfigurationBL.getWS(dto);
    }

    public List<Integer> getBillingProcessGeneratedInvoices(Integer processId) {
        IBillingProcessSessionBean processBean = Context.getBean(Context.Name.BILLING_PROCESS_SESSION);

        // todo: IBillingProcessSessionBean#getGeneratedInvoices() should have a proper generic return type
        @SuppressWarnings("unchecked")
        Collection<InvoiceDTO> invoices  = processBean.getGeneratedInvoices(processId);

        List<Integer> ids = new ArrayList<Integer>(invoices.size());
        for (InvoiceDTO invoice : invoices)
            ids.add(invoice.getId());
        return ids;
    }


    /*
       Mediation process
     */

    public void triggerMediation() {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        mediationBean.trigger(getCallerCompanyId());
    }

    public boolean isMediationProcessing() throws SessionInternalError {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        return mediationBean.isProcessing(getCallerCompanyId());
    }

    public List<MediationProcessWS> getAllMediationProcesses() {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        List<MediationProcess> processes = mediationBean.getAll(getCallerCompanyId());

        // convert to web-service mediation process
        List<MediationProcessWS> ws = new ArrayList<MediationProcessWS>(processes.size());
        for (MediationProcess process : processes)
            ws.add(new MediationProcessWS(process));
        return ws;
    }

    public List<MediationRecordLineWS> getMediationEventsForOrder(Integer orderId) {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        List<MediationRecordLineDTO> events = mediationBean.getEventsForOrder(orderId);

        return MediationRecordBL.getWS(events);
    }

    public List<MediationRecordLineWS> getMediationEventsForInvoice(Integer invoiceId) {
        List<MediationRecordLineDTO> events = new MediationRecordLineDAS().findByInvoice(invoiceId);
        return MediationRecordBL.getWS(events);
    }

    public List<MediationRecordWS> getMediationRecordsByMediationProcess(Integer mediationProcessId) {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        List<MediationRecordDTO> records = mediationBean.getMediationRecordsByMediationProcess(mediationProcessId);

        return MediationRecordBL.getWS(records);
    }

    public List<RecordCountWS> getNumberOfMediationRecordsByStatuses() {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        Map<MediationRecordStatusDTO, Long> records = mediationBean.getNumberOfRecordsByStatuses(getCallerCompanyId());

        // convert to a simple object for web-services
        List<RecordCountWS> counts = new ArrayList<RecordCountWS>(records.size());
        for (Map.Entry<MediationRecordStatusDTO, Long> record : records.entrySet())
            counts.add(new RecordCountWS(record.getKey().getId(), record.getValue()));
        return counts;
    }

    public List<MediationConfigurationWS> getAllMediationConfigurations() {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);

        List<MediationConfiguration> configurations = mediationBean.getAllConfigurations(getCallerCompanyId());
        return MediationConfigurationBL.getWS(configurations);
    }

    public void createMediationConfiguration(MediationConfigurationWS cfg) {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);

        MediationConfiguration dto = MediationConfigurationBL.getDTO(cfg);
        mediationBean.createConfiguration(dto);
    }

    public List<Integer> updateAllMediationConfigurations(List<MediationConfigurationWS> configurations)
            throws SessionInternalError {

        // update all configurations
        List<MediationConfiguration> dtos = MediationConfigurationBL.getDTO(configurations);
        List<MediationConfiguration> updated;
        try {
            IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
            updated = mediationBean.updateAllConfiguration(getCallerId(), dtos);
        } catch (InvalidArgumentException e) {
            throw new SessionInternalError(e);
        }

        // return list of updated ids
        List<Integer> ids = new ArrayList<Integer>(updated.size());
        for (MediationConfiguration cfg : updated)
            ids.add(cfg.getId());
        return ids;
    }

    public void deleteMediationConfiguration(Integer cfgId) {
        IMediationSessionBean mediationBean = Context.getBean(Context.Name.MEDIATION_SESSION);
        mediationBean.delete(getCallerId(), cfgId);
    }


    /*
        Provisioning
     */

    public void triggerProvisioning() {
        IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
        provisioningBean.trigger();
    }

    public void updateOrderAndLineProvisioningStatus(Integer inOrderId, Integer inLineId, String result)
            throws SessionInternalError {
        IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
        provisioningBean.updateProvisioningStatus(inOrderId, inLineId, result);
    }

    public void updateLineProvisioningStatus(Integer orderLineId, Integer provisioningStatus) throws SessionInternalError {
        IProvisioningProcessSessionBean provisioningBean = Context.getBean(Context.Name.PROVISIONING_PROCESS_SESSION);
        provisioningBean.updateProvisioningStatus(orderLineId, provisioningStatus);
    }


    /*
        Utilities
     */

    public void generateRules(String rulesData) throws SessionInternalError {
        try {
            PluggableTaskManager<IRulesGenerator> tm =
                    new PluggableTaskManager<IRulesGenerator>(
                    getCallerCompanyId(),
                    Constants.PLUGGABLE_TASK_RULES_GENERATOR);
            IRulesGenerator rulesGenerator = tm.getNextClass();

            rulesGenerator.unmarshal(rulesData);
            rulesGenerator.process();

        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }


    /*
        Preferences
     */

    public void updatePreferences(PreferenceWS[] prefList) {
        PreferenceBL bl = new PreferenceBL();
        for (PreferenceWS pref: prefList) {
            bl.createUpdateForEntity(getCallerCompanyId(), pref.getPreferenceType().getId(), pref.getValue());
        }
    }

    public void updatePreference(PreferenceWS preference) {
        new PreferenceBL().createUpdateForEntity(getCallerCompanyId(),
                                                 preference.getPreferenceType().getId(),
                                                 preference.getValue());
    }

    public PreferenceWS getPreference(Integer preferenceTypeId) {
        PreferenceDTO preference = null;
        try {
            preference = new PreferenceBL(getCallerCompanyId(), preferenceTypeId).getEntity();
        } catch (DataAccessException e) {
            /* ignore */
        }

        if (preference != null) {
            // return preference if set
            return new PreferenceWS(preference);

        } else {
            // preference is not set, return empty
            PreferenceTypeDTO preferenceType = new PreferenceTypeDAS().find(preferenceTypeId);
            return preferenceType != null ? new PreferenceWS(preferenceType) : null;
        }
    }


    /*
        Currencies
     */

    public CurrencyWS[] getCurrencies() {
        CurrencyBL currencyBl = new CurrencyBL();

        CurrencyDTO[] currencies;
        try {
            currencies = currencyBl.getCurrencies(getCallerCompanyId(), getCallerLanguageId());
        } catch (SQLException e) {
            throw new SessionInternalError("Exception fetching currencies for entity " + getCallerCompanyId(), e);
        } catch (NamingException e) {
            throw new SessionInternalError("Exception fetching currencies for entity " + getCallerCompanyId(), e);
        }

        // Id of the default currency for this entity
        Integer entityDefault = currencyBl.getEntityCurrency(getCallerCompanyId());

        // convert to WS
        List<CurrencyWS> ws = new ArrayList<CurrencyWS>(currencies.length);
        for (CurrencyDTO currency : currencies) {
            ws.add(new CurrencyWS(currency, (currency.getId() == entityDefault)));
        }

        return ws.toArray(new CurrencyWS[ws.size()]);
    }

    public void updateCurrencies(CurrencyWS[] currencies) {
        for (CurrencyWS currency : currencies) {
            updateCurrency(currency);
        }
    }

    public void updateCurrency(CurrencyWS ws) {
        CurrencyDTO currency = new CurrencyDTO(ws);

        // update currency
        CurrencyBL currencyBl = new CurrencyBL(currency.getId());
        currencyBl.update(currency, getCallerCompanyId());

        // set as entity currency if flagged as default
        if (ws.isDefaultCurrency()) {
            currencyBl.setEntityCurrency(getCallerCompanyId(), currency.getId());
        }

        // update the description if its changed
        if ((ws.getDescription() != null && !ws.getDescription().equals(currency.getDescription()))) {
            currency.setDescription(ws.getDescription(), getCallerLanguageId());
        }
    }

    public Integer createCurrency(CurrencyWS ws) {
        CurrencyDTO currency = new CurrencyDTO(ws);

        // save new currency
        CurrencyBL currencyBl = new CurrencyBL(currency.getId());
        currencyBl.create(currency, getCallerCompanyId());
        currency = currencyBl.getEntity();

        // set as entity currency if flagged as default
        if (ws.isDefaultCurrency()) {
            currencyBl.setEntityCurrency(getCallerCompanyId(), currency.getId());
        }

        // set description
        if (ws.getDescription() != null) {
            currency.setDescription(ws.getDescription(), getCallerLanguageId());
        }

        return currency.getId();
    }

    public CompanyWS getCompany() {
        CompanyDTO company= new CompanyDAS().find(getCallerCompanyId());
        LOG.debug(company);
        return new CompanyWS(company);
    }
    
    public void updateCompany(CompanyWS companyWS) {
        new EntityBL().updateEntityAndContact(companyWS, getCallerCompanyId(), getCallerId());
    }

    /*
       Notifications
     */

    public void createUpdateNofications(Integer messageId, MessageDTO dto) {
        if (null == messageId) {
            new NotificationBL().createUpdate(getCallerCompanyId(), dto);
        } else {
            new NotificationBL(messageId).createUpdate(getCallerCompanyId(), dto);
        }
    }

    /*Secured via WSSecurityMethodMapper entry.*/
    public void saveCustomerNotes(Integer userId, String notes) {
    	CustomerDTO cust= UserBL.getUserEntity(userId).getCustomer();
    	if ( null != cust ) {
    		cust.setNotes(notes);
    	} else {
    		throw new SessionInternalError("Not a customer");
    	}
    }


    /*
     * Plug-ins
     */

    public PluggableTaskWS getPluginWS(Integer pluginId) {
        PluggableTaskDTO dto = new PluggableTaskBL(pluginId).getDTO();
        return new PluggableTaskWS(dto);
    }

    public Integer createPlugin(PluggableTaskWS plugin) {
        return new PluggableTaskBL().create(getCallerId(), new PluggableTaskDTO(getCallerCompanyId(), plugin));
    }

    public void updatePlugin(PluggableTaskWS plugin) {
        new PluggableTaskBL().update(getCallerId(), new PluggableTaskDTO(getCallerCompanyId(), plugin));
    }

    public void deletePlugin(Integer id) {
        new PluggableTaskBL(id).delete(getCallerId());
    }
}
