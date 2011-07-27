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

package com.sapienter.jbilling.server.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.partner.db.Partner;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
import com.sapienter.jbilling.server.user.partner.db.PartnerRange;
import com.sapienter.jbilling.server.util.audit.db.EventLogDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

/**
 *
 * This is the session facade for the user. All interaction from the client
 * to the server is made through calls to the methods of this class. This 
 * class uses helper classes (Business Logic -> BL) for the real logic.
 *
 * This interface was created to stop Spring related 
 * ClassCastExceptions when getting the bean. 
 *
 * @author emilc
 */
public interface IUserSessionBean {

    /**
     * @return the new user id if everthing ok, or null if the username is already 
     * taken, any other problems go as an exception
     */
    public Integer create(UserDTOEx newUser, ContactDTOEx contact) 
            throws SessionInternalError;

    public UserDTO getUserDTO(String userName, Integer entityId) 
            throws SessionInternalError;

    public String getCustomerNotes(Integer userId) throws SessionInternalError;

    public Locale getLocale(Integer userId) throws SessionInternalError;
   
    public void setCustomerNotes(Integer userId, String notes)
            throws SessionInternalError;

    public void delete(Integer executorId, Integer userId) 
            throws SessionInternalError;

    public void delete(String userName, Integer entityId) 
            throws SessionInternalError;

    /**
     * @param userId The user that is doing this change, it could be
     * the same user or someone else in behalf.
     */
    public void update(Integer executorId, UserDTOEx dto) 
            throws SessionInternalError;

    public void updatePartner(Integer executorId, Partner dto) 
            throws SessionInternalError;

    public void updatePartnerRanges(Integer executorId, Integer partnerId, 
            PartnerRange[] ranges) throws SessionInternalError;

    public ContactDTOEx getPrimaryContactDTO(Integer userId)
            throws SessionInternalError;
    
    public void setPrimaryContact(ContactDTOEx dto, Integer userId)
            throws SessionInternalError;

     public ContactDTOEx getContactDTO(Integer userId, Integer contactTypeId)
             throws SessionInternalError;
 
    public ContactDTOEx getVoidContactDTO(Integer entityId)
            throws SessionInternalError;

    public void setContact(ContactDTOEx dto, Integer userId, Integer 
            contactTypeId) throws SessionInternalError;

    public boolean addContact(ContactDTOEx dto, String username,
            Integer entityId) throws SessionInternalError;

    public UserDTOEx getUserDTOEx(Integer userId) 
            throws SessionInternalError;
    
    public Boolean isParentCustomer(Integer userId) 
            throws SessionInternalError;
    
    public UserDTOEx getUserDTOEx(String userName, Integer entityId) 
            throws SessionInternalError;
    
    public Boolean hasSubAccounts(Integer userId)
            throws SessionInternalError;
    
    public CurrencyDTO getCurrency(Integer userId) throws SessionInternalError;

    public Integer createCreditCard(Integer userId, CreditCardDTO dto) 
            throws SessionInternalError;

    /**
     * This actually creates a credit card record for a user without it,
     * or updates an existing one.
     * Since now we are only supporting one cc per user, this will
     * just get the first cc and update it (it could have deleted 
     * all of them and create one, but it was too crapy).
     */
    public void updateCreditCard(Integer executorId, Integer userId,
            CreditCardDTO dto) throws SessionInternalError;

    public void updateCreditCard(String username, Integer entityId,
            CreditCardDTO dto) throws SessionInternalError;

    public void setAuthPaymentType(Integer userId, Integer newMethod, 
            Boolean use) throws SessionInternalError;
    
    public Integer getAuthPaymentType(Integer userId) 
            throws SessionInternalError;

    public void updateACH(Integer userId, Integer executorId, AchDTO ach)
            throws SessionInternalError;
    
    public AchDTO getACH(Integer userId) throws SessionInternalError;

    public void removeACH(Integer userId, Integer executorId)
            throws SessionInternalError;

    /**
     * Since now we are only supporting one cc per user, this will
     * just get the first cc .
     */
    public CreditCardDTO getCreditCard(Integer userId) 
            throws SessionInternalError;
    
    /**
     * @return The path or url of the css to use for the given entity
     */
    public String getEntityPreference(Integer entityId, Integer preferenceId) 
            throws SessionInternalError;
    
    /**
     * Get the entity's contact information
     * @param entityId
     * @return
     * @throws SessionInternalError
     */
    public ContactDTOEx getEntityContact(Integer entityId) 
            throws SessionInternalError;
    
    /**
     * 
     * @param entityId
     * @return
     * @throws SessionInternalError
     */
    public Integer getEntityPrimaryContactType(Integer entityId) 
            throws SessionInternalError;

    /**
     * This is really an entity level class, there is no user involved.
     * This means that the lookup of parameters will be based on the table
     * entity.
     * 
     * @param ids
     * An array of the parameter ids that will be looked up and returned in
     * the hashtable
     * @return
     * The paramteres in "id - value" pairs. The value is of type String
     */    
    public HashMap getEntityParameters(Integer entityId, Integer[] ids) 
            throws SessionInternalError;
    
    /**
     * @param entityId
     * @param params
     * @throws SessionInternalError
     */
    public void setEntityParameters(Integer entityId, HashMap params) 
            throws SessionInternalError;

    /**
     * This now only working with String parameters
     * 
     * @param entityId entity id
     * @param preferenceId preference Id
     * @param value String parameter value (optional)
     * @throws SessionInternalError
     */
    public void setEntityParameter(Integer entityId, Integer preferenceId, String value)
            throws SessionInternalError;

    /**
     * Marks as deleted all the credit cards associated with this user
     * and removes the relationship
     */
    public void deleteCreditCard(Integer executorId, Integer userId) 
            throws SessionInternalError;

    public void setUserStatus(Integer executorId, Integer userId, 
            Integer statusId) throws SessionInternalError; 
    
    public String getWelcomeMessage(Integer entityId, Integer languageId, 
            Integer statusId) throws SessionInternalError;

    /**
    * Describes the instance and its content for debugging purpose
    *
    * @return Debugging information about the instance and its content
    */
    public String toString();

    /**
     * This is the entry method for the payout batch. It goes over all the
     * partners with a next_payout_date <= today and calls the mthods to
     * process the payout
     * @param today
     */
    public void processPayouts(Date today) throws SessionInternalError;

    public void processPayout(Integer partnerId) throws SessionInternalError;

    public PartnerPayout calculatePayout(Integer partnerId, Date start, 
            Date end, Integer currencyId) throws SessionInternalError;

    public Partner getPartnerDTO(Integer partnerId) 
            throws SessionInternalError;

    public PartnerPayout getPartnerLastPayoutDTO(Integer partnerId) 
            throws SessionInternalError;

    public PartnerPayout getPartnerPayoutDTO(Integer payoutId) 
            throws SessionInternalError;

    public Date[] getPartnerPayoutDates(Integer partnerId) 
            throws SessionInternalError;
    
    public void notifyCreditCardExpiration(Date today) 
            throws SessionInternalError;

    public void setUserBlacklisted(Integer executorId, Integer userId, 
            Boolean isBlacklisted) throws SessionInternalError;

    /**
     * @throws NumberFormatException 
     * @throws NotificationNotFoundException 
     * @throws SessionInternalError 
     */
    public void sendLostPassword(String entityId, String username) 
        throws NumberFormatException, SessionInternalError,
        NotificationNotFoundException;
    
    public boolean isPasswordExpired(Integer userId);

    public List<EventLogDTO> getEventLog(Integer userId);
}
