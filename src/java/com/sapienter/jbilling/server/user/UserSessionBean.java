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

import com.sapienter.jbilling.common.JNDILookup;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.process.AgeingBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.partner.PartnerBL;
import com.sapienter.jbilling.server.user.partner.db.Partner;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
import com.sapienter.jbilling.server.user.partner.db.PartnerRange;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.DTOFactory;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.audit.db.EventLogDAS;
import com.sapienter.jbilling.server.util.audit.db.EventLogDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.LanguageDAS;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 *
 * This is the session facade for the user. All interaction from the client
 * to the server is made through calls to the methods of this class. This
 * class uses helper classes (Business Logic -> BL) for the real logic.
 *
 * Had to implement IUserSessionBean to stop Spring related
 * ClassCastExceptions when getting the bean.
 *
 * @author emilc
 */
@Transactional( propagation = Propagation.REQUIRED )
public class UserSessionBean implements IUserSessionBean, ApplicationContextAware, PartnerSQL {

    private static final Logger LOG = Logger.getLogger(UserSessionBean.class);

    public void setApplicationContext(ApplicationContext ctx) {
        Context.setApplicationContext(ctx);
    }


    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    /**
     * @return the new user id if everthing ok, or null if the username is already
     * taken, any other problems go as an exception
     */
    public Integer create(UserDTOEx newUser, ContactDTOEx contact)
            throws SessionInternalError {
        try {
            UserBL bl = new UserBL();
            if (!bl.exists(newUser.getUserName(), newUser.getEntityId())) {

                ContactBL cBl = new ContactBL();

                Integer userId = bl.create(newUser);
                if (userId != null) {
                    // children inherit the contact of the parent user
                    if (newUser.getCustomer() != null &&
                        newUser.getCustomer().getParent() != null) {
                        cBl.setFromChild(userId);
                        contact = cBl.getDTO();
                        LOG.debug("Using parent's contact " + contact.getId());
                    }
                    cBl.createPrimaryForUser(contact, userId, newUser.getEntityId());
                } else {
                    // means that the partner doens't exist
                    userId = new Integer(-1);
                }
                return userId;
            }

            return null;

        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public UserDTO getUserDTO(String userName, Integer entityId)
            throws SessionInternalError {
        UserDTO dto = null;
        try {
            UserBL user = new UserBL(userName, entityId);
            dto = user.getDto();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return dto;
    }

    public String getCustomerNotes(Integer userId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            return user.getEntity().getCustomer().getNotes();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Locale getLocale(Integer userId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            return user.getLocale();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void setCustomerNotes(Integer userId, String notes)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            user.getEntity().getCustomer().setNotes(notes);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void delete(Integer executorId, Integer userId)
            throws SessionInternalError {
        if (userId == null) {
            throw new SessionInternalError("userId can't be null");
        }
        try {
            UserBL bl = new UserBL(userId);
            bl.delete(executorId);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void delete(String userName, Integer entityId)
            throws SessionInternalError {
        if (userName == null) {
            throw new SessionInternalError("userId can't be null");
        }
        try {
            UserBL user = new UserBL(userName, entityId);
            user.delete(null);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }


    /**
     * @param executorId The user that is doing this change, it could be
     * the same user or someone else in behalf.
     */
    public void update(Integer executorId, UserDTOEx dto)
            throws SessionInternalError {
        try {
            UserBL bl = new UserBL(dto.getUserId());
            bl.update(executorId, dto);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updatePartner(Integer executorId, Partner dto)
            throws SessionInternalError {
        try {
            PartnerBL bl = new PartnerBL(dto.getId());
            bl.update(executorId, dto);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updatePartnerRanges(Integer executorId, Integer partnerId,
                                    PartnerRange[] ranges)
            throws SessionInternalError {
        try {
            PartnerBL bl = new PartnerBL(partnerId);
            bl.setRanges(executorId, ranges);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public ContactDTOEx getPrimaryContactDTO(Integer userId)
            throws SessionInternalError {
        try {
            ContactBL bl = new ContactBL();
            bl.set(userId);
            return bl.getDTO();
        } catch (Exception e) {
            LOG.error("Exception retreiving the customer contact", e);
            throw new SessionInternalError("Customer primary contact");
        }
    }

    public void setPrimaryContact(ContactDTOEx dto, Integer userId)
            throws SessionInternalError {
        try {
            ContactBL cbl = new ContactBL();

            cbl.updatePrimaryForUser(dto, userId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public ContactDTOEx getContactDTO(Integer userId, Integer contactTypeId)
            throws SessionInternalError {
        ContactBL bl = new ContactBL();
        bl.set(userId, contactTypeId);
        if (bl.getEntity() != null) {
            return bl.getDTO();
        } else {
            return getVoidContactDTO(new UserDAS().find(userId).getCompany().getId());
        }
    }

    public ContactDTOEx getVoidContactDTO(Integer entityId)
            throws SessionInternalError {
        try {
            ContactBL bl = new ContactBL();
            return bl.getVoidDTO(entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void setContact(ContactDTOEx dto, Integer userId, Integer
            contactTypeId)
            throws SessionInternalError {
        try {
            ContactBL cbl = new ContactBL();

            cbl.updateForUser(dto, userId, contactTypeId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public boolean addContact(ContactDTOEx dto, String username,
                              Integer entityId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(username, entityId);
            ContactBL cbl = new ContactBL();

            return cbl.append(dto, user.getEntity().getUserId());
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public UserDTOEx getUserDTOEx(Integer userId)
            throws SessionInternalError {
        UserDTOEx dto = null;

        try {
            dto = DTOFactory.getUserDTOEx(userId);
            dto.touch();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return dto;
    }

    public Boolean isParentCustomer(Integer userId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            Integer isParent = user.getEntity().getCustomer().getIsParent();
            if (isParent == null || isParent.intValue() == 0) {
                return new Boolean(false);
            } else {
                return new Boolean(true);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    // Check if there is any Active Children under this client
    public Boolean hasSubAccounts(Integer userId)
            throws SessionInternalError {
        try {
            boolean hasSubAccounts = false;
            UserBL user = new UserBL(userId);
            Iterator childs = user.getEntity().getCustomer().getChildren().iterator();
            while( !hasSubAccounts && childs.hasNext() ){
                CustomerDTO child = (CustomerDTO)childs.next();
                if( child.getBaseUser().getDeleted() == 0 ){
                    hasSubAccounts = true;
                }
            }
            return hasSubAccounts;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public UserDTOEx getUserDTOEx(String userName, Integer entityId)
            throws SessionInternalError{
        UserDTOEx dto = null;

        try {
            UserBL bl = new UserBL();
            bl.set(userName, entityId);
            dto = DTOFactory.getUserDTOEx(bl.getEntity());
            dto.touch();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return dto;
    }

    public CurrencyDTO getCurrency(Integer userId)
            throws SessionInternalError{
        return new UserDAS().find(userId).getCurrency();
    }

    public Integer createCreditCard(Integer userId,
                                    CreditCardDTO dto) throws SessionInternalError {
        try {
            // add the base user to the given CreditCardDTO
            UserDTO user = new UserDAS().find(userId);
            dto.getBaseUsers().add(user);

            // create the cc record
            CreditCardBL ccBL = new CreditCardBL();
            ccBL.create(dto);

            user.getCreditCards().add(ccBL.getEntity());
            new UserDAS().save(user);

            return ccBL.getEntity().getId();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * This actually creates a credit card record for a user without it,
     * or updates an existing one.
     * Since now we are only supporting one cc per user, this will
     * just get the first cc and update it (it could have deleted
     * all of them and create one, but it was too crapy).
     */
    public void updateCreditCard(Integer executorId, Integer userId,
                                 CreditCardDTO dto) throws SessionInternalError {
        try {
            // find this user and get the first cc
            UserBL userBL = new UserBL(userId);
            updateCreditCard(userBL.getEntity(), dto, executorId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updateCreditCard(String username, Integer entityId,
                                 CreditCardDTO dto) throws SessionInternalError {
        try {
            UserBL userBL = new UserBL(username, entityId);
            updateCreditCard(userBL.getEntity(), dto, null);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    private void updateCreditCard(UserDTO user,
                                  CreditCardDTO dto, Integer executorId)
            throws SessionInternalError {
        // find this user and get the first cc

        try {
            UserBL userBL = new UserBL();
            userBL.set(user);

            if (dto != null) {
                if (dto.getId() == 0) {
                    // create a new credit card
                    createCreditCard(user.getUserId(), dto);

                } else {
                    // update existing credit card
                    Integer primaryCreditCardId = userBL.getEntity().getCreditCards().iterator().next().getId();
                    new CreditCardBL(primaryCreditCardId).update(executorId, dto, user.getId());
                }

            } else {
                // credit card is set to null, delete existing customer card
                deleteCreditCard(executorId, user.getUserId());
            }

        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

    }

    public void setAuthPaymentType(Integer userId, Integer newMethod,
                                   Boolean use)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            if (user.getEntity().getCustomer() == null) {
                LOG.warn("Trying to update the automatic payment type of a " +
                         "non customer");
                return;
            }
            Integer method = user.getEntity().getCustomer().
                    getAutoPaymentType();
            // it wants to use this one now
            if (use.booleanValue()) {
                user.getEntity().getCustomer().setAutoPaymentType(newMethod);
            }
            // it has this method, and doesn't want to use it any more
            if (method != null && method.equals(newMethod) &&
                !use.booleanValue()) {
                user.getEntity().getCustomer().setAutoPaymentType(null);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Integer getAuthPaymentType(Integer userId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            Integer method;
            if (user.getEntity().getCustomer() != null) {
                method = user.getEntity().getCustomer().getAutoPaymentType();
            } else {
                // this will be necessary as long as non-customers can have
                // a credit card
                method = new Integer(0);
            }
            return method;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updateACH(Integer userId, Integer executorId, AchDTO ach)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            user.updateAch(ach, executorId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public AchDTO getACH(Integer userId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            Set<AchDTO> ach = user.getEntity().getAchs();
            if (ach.size() > 0) {
                AchBL bl = new AchBL(((AchDTO)ach.toArray()[0]).getId());
                return bl.getDTO();
            }
            return null;

        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void removeACH(Integer userId, Integer executorId)
            throws SessionInternalError {
        try {
            UserBL user = new UserBL(userId);
            Set<AchDTO> ach = user.getEntity().getAchs();
            if (ach.size() > 0) {
                AchDTO _achDTO= (AchDTO)ach.toArray()[0];
                AchBL bl = new AchBL((_achDTO).getId());
                bl.delete(executorId);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * Since now we are only supporting one cc per user, this will
     * just get the first cc .
     */
    public CreditCardDTO getCreditCard(Integer userId)
            throws SessionInternalError {
        CreditCardDTO retValue;
        try {
            // find this user and get the first cc
            UserBL userBL = new UserBL(userId);
            if (!userBL.getEntity().getCreditCards().isEmpty()) {
                CreditCardBL ccBL = new CreditCardBL(((CreditCardDTO)
                                                              userBL.getEntity().getCreditCards().toArray()[0]).getId());
                retValue = ccBL.getDTO();
            } else { // return a blank one
                retValue = null;
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    /**
     * @return The path or url of the css to use for the given entity
     */
    public String getEntityPreference(Integer entityId, Integer preferenceId)
            throws SessionInternalError {
        try {
            String result = null;
            PreferenceBL preference = new PreferenceBL();
            try {
                preference.set(entityId, preferenceId);
                result = preference.getValueAsString();
            } catch (EmptyResultDataAccessException e) {
                // it is missing, so it will pick up the default
            }

            if (result == null  || result.trim().length() == 0) {
                result = preference.getDefaultValue(preferenceId);
                LOG.debug("Using default");
            }

            if (result == null) {
                LOG.warn("Preference " + preferenceId + " does not have a " +
                         " default.");
            }

            LOG.debug("result for " + preferenceId + " =" + result);
            return result;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * Get the entity's contact information
     * @param entityId
     * @return
     * @throws SessionInternalError
     */
    public ContactDTOEx getEntityContact(Integer entityId)
            throws SessionInternalError {
        try {
            ContactBL bl = new ContactBL();
            bl.setEntity(entityId);
            return bl.getDTO();
        } catch (Exception e) {
            LOG.error("Exception retreiving the entity contact", e);
            throw new SessionInternalError("Customer primary contact");
        }
    }

    /**
     *
     * @param entityId
     * @return
     * @throws SessionInternalError
     */
    public Integer getEntityPrimaryContactType(Integer entityId)
            throws SessionInternalError {
        try {
            ContactBL contact = new ContactBL();
            return contact.getPrimaryType(entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

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
            throws SessionInternalError {
        HashMap retValue = new HashMap();

        try {
            PreferenceBL preference = new PreferenceBL();
            for (int f = 0; f < ids.length; f++) {
                try {
                    preference.set(entityId, ids[f]);
                    retValue.put(ids[f], preference.getValueAsString());
                } catch (EmptyResultDataAccessException e1) {
                    // use a default
                    retValue.put(ids[f], preference.getDefaultValue(ids[f]));
                }
            }
            return retValue;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * @param entityId
     * @param params
     * @throws SessionInternalError
     */
    public void setEntityParameters(Integer entityId, HashMap params)
            throws SessionInternalError {
        try {
            PreferenceBL preference = new PreferenceBL();
            for (Iterator it = params.keySet().iterator(); it.hasNext();) {
                Integer preferenceId = (Integer) it.next();

                Object value = params.get(preferenceId);
                if (value != null) {
                    if (value instanceof Integer) {
                        preference.createUpdateForEntity(entityId, preferenceId, (Integer) value);

                    } else if (value instanceof String) {
                        preference.createUpdateForEntity(entityId, preferenceId, (String) value);

                    } else if (value instanceof Float) {
                        preference.createUpdateForEntity(entityId, preferenceId, new BigDecimal(value.toString()));

                    } else if (value instanceof BigDecimal) {
                        preference.createUpdateForEntity(entityId, preferenceId, (BigDecimal) value);
                    }

                } else {
                    preference.createUpdateForEntity(entityId, preferenceId, (String) null);
                }
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * This now only working with String parameters
     *
     * @param entityId entity id
     * @param preferenceId preference Id
     * @param value String parameter value (optional)
     * @throws SessionInternalError
     */
    public void setEntityParameter(Integer entityId, Integer preferenceId, String value) throws SessionInternalError {
        try {
            LOG.debug("updating preference " + preferenceId + " for entity " + entityId);
            PreferenceBL preference = new PreferenceBL();
            preference.createUpdateForEntity(entityId, preferenceId, value);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * Marks as deleted all the credit cards associated with this user
     * and removes the relationship
     */
    public void deleteCreditCard(Integer executorId, Integer userId)
            throws SessionInternalError {
        try {
            // find this user and get the first cc
            UserBL userBL = new UserBL(userId);
            Iterator it = userBL.getEntity().getCreditCards().iterator();
            while (it.hasNext()) {
                CreditCardBL bl = new CreditCardBL(((CreditCardDTO)
                                                            it.next()).getId());
                bl.delete(executorId);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void setUserStatus(Integer executorId, Integer userId,
                              Integer statusId)
            throws SessionInternalError {
        try {
            AgeingBL age = new AgeingBL();
            age.setUserStatus(executorId, userId, statusId,
                              Calendar.getInstance().getTime());
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public String getWelcomeMessage(Integer entityId, Integer languageId,
                                    Integer statusId)
            throws SessionInternalError {
        String retValue;
        try {
            AgeingBL age = new AgeingBL();
            LOG.debug("Getting welcome message for " + entityId +
                      " language " + languageId + " status " + statusId);
            retValue = age.getWelcome(entityId, languageId, statusId);
            //log.debug("welcome = " + retValue);
            if (retValue == null) {
                LOG.warn("No message found. Looking for active status");
                retValue = age.getWelcome(entityId, languageId,
                                          UserDTOEx.STATUS_ACTIVE);
                if (retValue == null) {
                    LOG.warn("Using welcome default");
                    retValue = "Welcome!";
                }
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    /**
     * Describes the instance and its content for debugging purpose
     *
     * @return Debugging information about the instance and its content
     */
    public String toString() {
        return "UserSessionBean [ " + " ]";
    }

    /**
     * This is the entry method for the payout batch. It goes over all the
     * partners with a next_payout_date <= today and calls the mthods to
     * process the payout
     * @param today
     */
    public void processPayouts(Date today)
            throws SessionInternalError {
        try {
            JNDILookup jndi = JNDILookup.getFactory();
            Connection conn = jndi.lookUpDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(duePayout);
            stmt.setDate(1, new java.sql.Date(today.getTime()));
            ResultSet result = stmt.executeQuery();
            // since esql doesn't support dates, a direct call is necessary
            while (result.next()) {
                processPayout(new Integer(result.getInt(1)));
            }
            result.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void processPayout(Integer partnerId)
            throws SessionInternalError {
        try {
            LOG.debug("Processing partner " + partnerId);
            PartnerBL partnerBL = new PartnerBL();
            partnerBL.processPayout(partnerId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public PartnerPayout calculatePayout(Integer partnerId, Date start,
                                         Date end, Integer currencyId)
            throws SessionInternalError {
        try {
            PartnerBL partnerBL = new PartnerBL(partnerId);
            if (currencyId == null) {
                // we default to this partners currency
                currencyId = partnerBL.getEntity().getUser().getEntity().
                        getCurrencyId();
            }
            return partnerBL.calculatePayout(start, end, currencyId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Partner getPartnerDTO(Integer partnerId)
            throws SessionInternalError {
        PartnerBL partnerBL = new PartnerBL(partnerId);
        Partner retValue = partnerBL.getDTO();
        retValue.touch();
        return retValue;
    }

    public PartnerPayout getPartnerLastPayoutDTO(Integer partnerId)
            throws SessionInternalError {
        try {
            PartnerBL partnerBL = new PartnerBL();
            return partnerBL.getLastPayoutDTO(partnerId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public PartnerPayout getPartnerPayoutDTO(Integer payoutId)
            throws SessionInternalError {
        try {
            PartnerBL partnerBL = new PartnerBL();
            partnerBL.setPayout(payoutId);
            return partnerBL.getPayoutDTO();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Date[] getPartnerPayoutDates(Integer partnerId)
            throws SessionInternalError {
        try {
            PartnerBL partnerBL = new PartnerBL(partnerId);
            return partnerBL.calculatePayoutDates();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void notifyCreditCardExpiration(Date today)
            throws SessionInternalError {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                CreditCardBL bl = new CreditCardBL();
                bl.notifyExipration(today);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void setUserBlacklisted(Integer executorId, Integer userId,
                                   Boolean isBlacklisted) throws SessionInternalError {
        try {
            UserBL bl = new UserBL(userId);
            bl.setUserBlacklisted(executorId, isBlacklisted);
        } catch(Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * @throws NumberFormatException
     * @throws NotificationNotFoundException
     * @throws SessionInternalError
     */
    @Deprecated
    public void sendLostPassword(String entityId, String username)
            throws NumberFormatException, SessionInternalError,
                   NotificationNotFoundException {
        UserBL user = new UserBL(username, Integer.valueOf(entityId));

        user.sendLostPassword(Integer.valueOf(entityId), user.getEntity().getUserId(),  user.getEntity().getLanguageIdField());
    }

    @Deprecated
    public boolean isPasswordExpired(Integer userId) {
        UserBL user;
        user = new UserBL(userId);
        return user.isPasswordExpired();
    }

    public List<EventLogDTO> getEventLog(Integer userId) {
        List<EventLogDTO> events = new EventLogDAS().getEventsByAffectedUser(
                userId);
        for (EventLogDTO event : events) {
            event.touch();
        }
        return events;
    }
}
