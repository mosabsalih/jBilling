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

package com.sapienter.jbilling.server.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.entity.AchDTO;
import com.sapienter.jbilling.server.invoice.InvoiceWS;
import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
import com.sapienter.jbilling.server.mediation.MediationProcessWS;
import com.sapienter.jbilling.server.mediation.MediationRecordLineWS;
import com.sapienter.jbilling.server.mediation.MediationRecordWS;
import com.sapienter.jbilling.server.mediation.RecordCountWS;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.order.OrderLineWS;
import com.sapienter.jbilling.server.order.OrderProcessWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
import com.sapienter.jbilling.server.payment.PaymentWS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
import com.sapienter.jbilling.server.process.AgeingWS;
import com.sapienter.jbilling.server.process.BillingProcessConfigurationWS;
import com.sapienter.jbilling.server.process.BillingProcessWS;
import com.sapienter.jbilling.server.user.ContactTypeWS;
import com.sapienter.jbilling.server.user.ContactWS;
import com.sapienter.jbilling.server.user.CreateResponseWS;
import com.sapienter.jbilling.server.user.UserTransitionResponseWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
import com.sapienter.jbilling.server.user.partner.PartnerWS;
import com.sapienter.jbilling.server.user.contact.ContactFieldTypeWS;
import com.sapienter.jbilling.server.order.OrderPeriodWS;
import com.sapienter.jbilling.server.user.CompanyWS;

/**
 * Web service bean interface. 
 * {@see com.sapienter.jbilling.server.util.WebServicesSessionSpringBean} for documentation.
 */
@WebService
public interface IWebServicesSessionBean {

    public Integer getCallerId();
    public Integer getCallerCompanyId();
    public Integer getCallerLanguageId();


    /*
        Users
     */

    public UserWS getUserWS(Integer userId) throws SessionInternalError;
    public Integer createUser(UserWS newUser) throws SessionInternalError;
    public void updateUser(UserWS user) throws SessionInternalError;
    public void deleteUser(Integer userId) throws SessionInternalError;

    public ContactWS[] getUserContactsWS(Integer userId) throws SessionInternalError;
    public void updateUserContact(Integer userId, Integer typeId, ContactWS contact) throws SessionInternalError;

    public ContactTypeWS getContactTypeWS(Integer contactTypeId) throws SessionInternalError;
    public Integer createContactTypeWS(ContactTypeWS contactType) throws SessionInternalError;

    public void updateCreditCard(Integer userId, com.sapienter.jbilling.server.entity.CreditCardDTO creditCard) throws SessionInternalError;
    public void updateAch(Integer userId, AchDTO ach) throws SessionInternalError;

    public void setAuthPaymentType(Integer userId, Integer autoPaymentType, boolean use) throws SessionInternalError;
    public Integer getAuthPaymentType(Integer userId) throws SessionInternalError;

    public Integer[] getUsersByStatus(Integer statusId, boolean in) throws SessionInternalError;
    public Integer[] getUsersInStatus(Integer statusId) throws SessionInternalError;
    public Integer[] getUsersNotInStatus(Integer statusId) throws SessionInternalError;
    public Integer[] getUsersByCustomField(Integer typeId, String value) throws SessionInternalError;
    public Integer[] getUsersByCreditCard(String number) throws SessionInternalError;

    public Integer getUserId(String username) throws SessionInternalError;

    public void saveCustomContactFields(ContactFieldTypeWS[] fields) throws SessionInternalError;

    public void processPartnerPayouts(Date runDate);
    public PartnerWS getPartner(Integer partnerId) throws SessionInternalError;

    public UserTransitionResponseWS[] getUserTransitions(Date from, Date to) throws SessionInternalError;
    public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id) throws SessionInternalError;

    public CreateResponseWS create(UserWS user, OrderWS order) throws SessionInternalError;


    /*
        Items
     */

    public ItemDTOEx getItem(Integer itemId, Integer userId, String pricing);
    public ItemDTOEx[] getAllItems() throws SessionInternalError;
    public Integer createItem(ItemDTOEx item) throws SessionInternalError;
    public void updateItem(ItemDTOEx item);
    public void deleteItem(Integer itemId);

    public ItemDTOEx[] getItemByCategory(Integer itemTypeId);
    public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId);

    public ItemTypeWS[] getAllItemCategories();
    public Integer createItemCategory(ItemTypeWS itemType) throws SessionInternalError;
    public void updateItemCategory(ItemTypeWS itemType) throws SessionInternalError;
    public void deleteItemCategory(Integer itemCategoryId);
    
    public String isUserSubscribedTo(Integer userId, Integer itemId);

    public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId) throws SessionInternalError;
    public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number) throws SessionInternalError;

    public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId) throws SessionInternalError;
    public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number) throws SessionInternalError;

    public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId, String fields);
    public ValidatePurchaseWS validateMultiPurchase(Integer userId, Integer[] itemId, String[] fields);


    /*
        Orders
     */

    public OrderWS getOrder(Integer orderId) throws SessionInternalError;
    public Integer createOrder(OrderWS order) throws SessionInternalError;
    public void updateOrder(OrderWS order) throws SessionInternalError;
    public Integer createUpdateOrder(OrderWS order) throws SessionInternalError;
    public void deleteOrder(Integer id) throws SessionInternalError;

    public Integer createOrderAndInvoice(OrderWS order) throws SessionInternalError;

    public OrderWS getCurrentOrder(Integer userId, Date date) throws SessionInternalError;
    public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines, String pricing, Date date, String eventDescription) throws SessionInternalError;

    public OrderWS[] getUserSubscriptions(Integer userId) throws SessionInternalError;
    
    public OrderLineWS getOrderLine(Integer orderLineId) throws SessionInternalError;
    public void updateOrderLine(OrderLineWS line) throws SessionInternalError;

    public Integer[] getOrderByPeriod(Integer userId, Integer periodId) throws SessionInternalError;
    public OrderWS getLatestOrder(Integer userId) throws SessionInternalError;
    public Integer[] getLastOrders(Integer userId, Integer number) throws SessionInternalError;

    public OrderWS rateOrder(OrderWS order) throws SessionInternalError;
    public OrderWS[] rateOrders(OrderWS orders[]) throws SessionInternalError;

    public boolean updateOrderPeriods(OrderPeriodWS[] orderPeriods) throws SessionInternalError;
    public boolean deleteOrderPeriod(Integer periodId) throws SessionInternalError;
    
    public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order) throws SessionInternalError;


    /*
        Invoices
     */

    public InvoiceWS getInvoiceWS(Integer invoiceId) throws SessionInternalError;
    public Integer[] createInvoice(Integer userId, boolean onlyRecurring) throws SessionInternalError;
    public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId) throws SessionInternalError;
    public void deleteInvoice(Integer invoiceId);

    public InvoiceWS[] getAllInvoicesForUser(Integer userId);
    public Integer[] getAllInvoices(Integer userId);
    public InvoiceWS getLatestInvoice(Integer userId) throws SessionInternalError;
    public Integer[] getLastInvoices(Integer userId, Integer number) throws SessionInternalError;

    public Integer[] getInvoicesByDate(String since, String until) throws SessionInternalError;
    public Integer[] getUserInvoicesByDate(Integer userId, String since, String until) throws SessionInternalError;
    public Integer[] getUnpaidInvoices(Integer userId) throws SessionInternalError;

    public byte[] getPaperInvoicePDF(Integer invoiceId) throws SessionInternalError;
    public boolean notifyInvoiceByEmail(Integer invoiceId);


    /*
        Payments
     */

    public PaymentWS getPayment(Integer paymentId) throws SessionInternalError;
    public PaymentWS getLatestPayment(Integer userId) throws SessionInternalError;
    public Integer[] getLastPayments(Integer userId, Integer number) throws SessionInternalError;
    public BigDecimal getTotalRevenueByUser (Integer userId) throws SessionInternalError;

    public PaymentWS getUserPaymentInstrument(Integer userId) throws SessionInternalError;

    public Integer createPayment(PaymentWS payment);
    public void updatePayment(PaymentWS payment);
    public void deletePayment(Integer paymentId);

    public void removePaymentLink(Integer invoiceId, Integer paymentId) throws SessionInternalError;
    public void createPaymentLink(Integer invoiceId, Integer paymentId);

    public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId) throws SessionInternalError;
    public Integer applyPayment(PaymentWS payment, Integer invoiceId) throws SessionInternalError;
    public PaymentAuthorizationDTOEx processPayment(PaymentWS payment, Integer invoiceId);

    
    /*
        Billing process
     */

    public boolean isBillingRunning();
    public void triggerBillingAsync(final Date runDate);
    public boolean triggerBilling(Date runDate);
    public void triggerAgeing(Date runDate);

    public BillingProcessConfigurationWS getBillingProcessConfiguration() throws SessionInternalError;
    public Integer createUpdateBillingProcessConfiguration(BillingProcessConfigurationWS ws) throws SessionInternalError;

    public BillingProcessWS getBillingProcess(Integer processId);
    public Integer getLastBillingProcess() throws SessionInternalError;
    
    public List<OrderProcessWS> getOrderProcesses(Integer orderId);
    public List<OrderProcessWS> getOrderProcessesByInvoice(Integer invoiceId);

    public BillingProcessWS getReviewBillingProcess();
    public BillingProcessConfigurationWS setReviewApproval(Boolean flag) throws SessionInternalError;

    public List<Integer> getBillingProcessGeneratedInvoices(Integer processId);

    public AgeingWS[] getAgeingConfiguration(Integer languageId) throws SessionInternalError ;
    public void saveAgeingConfiguration(AgeingWS[] steps, Integer gracePeriod, Integer languageId) throws SessionInternalError;


    /*
        Mediation process
     */

    public void triggerMediation();
    public boolean isMediationProcessing();

    public List<MediationProcessWS> getAllMediationProcesses();
    public List<MediationRecordLineWS> getMediationEventsForOrder(Integer orderId);
    public List<MediationRecordLineWS> getMediationEventsForInvoice(Integer invoiceId);
    public List<MediationRecordWS> getMediationRecordsByMediationProcess(Integer mediationProcessId);
    public List<RecordCountWS> getNumberOfMediationRecordsByStatuses();

    public List<MediationConfigurationWS> getAllMediationConfigurations();
    public void createMediationConfiguration(MediationConfigurationWS cfg);
    public List<Integer> updateAllMediationConfigurations(List<MediationConfigurationWS> configurations) throws SessionInternalError;
    public void deleteMediationConfiguration(Integer cfgId);


    /*
        Provisioning process
     */

    public void triggerProvisioning();

    public void updateOrderAndLineProvisioningStatus(Integer inOrderId, Integer inLineId, String result);
    public void updateLineProvisioningStatus(Integer orderLineId, Integer provisioningStatus);


    /*
        Utilities
     */

    public void generateRules(String rulesData) throws SessionInternalError;


    /*
        Preferences
     */

    public void updatePreferences(PreferenceWS[] prefList);
    public void updatePreference(PreferenceWS preference);
    public PreferenceWS getPreference(Integer preferenceTypeId);


    /*
        Currencies
     */

    public CurrencyWS[] getCurrencies();
    public void updateCurrencies(CurrencyWS[] currencies);
    public void updateCurrency(CurrencyWS currency);
    public Integer createCurrency(CurrencyWS currency);

    public CompanyWS getCompany();
    public void updateCompany(CompanyWS companyWS);
    
    /*
        Notifications
    */

    public void createUpdateNofications(Integer messageId, MessageDTO dto);
    public void saveCustomerNotes(Integer userId, String notes);


    /*
        Plug-ins
     */

    public PluggableTaskWS getPluginWS(Integer pluginId);
    public Integer createPlugin(PluggableTaskWS plugin);
    public void updatePlugin(PluggableTaskWS plugin);
    public void deletePlugin(Integer plugin);
}
