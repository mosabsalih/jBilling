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

import com.sapienter.jbilling.common.JBCrypto;
import com.sapienter.jbilling.common.PermissionConstants;
import com.sapienter.jbilling.common.PermissionIdComparator;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDAS;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.AgeingBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.AchDAS;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.CustomerDAS;
import com.sapienter.jbilling.server.user.db.SubscriberStatusDAS;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.db.UserStatusDAS;
import com.sapienter.jbilling.server.user.partner.PartnerBL;
import com.sapienter.jbilling.server.user.permisson.db.PermissionDTO;
import com.sapienter.jbilling.server.user.permisson.db.PermissionUserDTO;
import com.sapienter.jbilling.server.user.permisson.db.RoleDAS;
import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import com.sapienter.jbilling.server.user.tasks.IValidatePurchaseTask;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.DTOFactory;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.util.db.LanguageDAS;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import javax.sql.rowset.CachedRowSet;

import javax.naming.NamingException;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class UserBL extends ResultList implements UserSQL {

    private final static Logger LOG = Logger.getLogger(UserBL.class);

    private UserDTO user = null;
    private EventLogger eLogger = null;
    private Integer mainRole = null;
    private UserDAS das = null;

    public UserBL(Integer userId) {
        init();
        set(userId);
    }

    public UserBL() {
        init();
    }

    public UserBL(UserDTO entity) {
        user = entity;
        init();
    }

    public UserBL(String username, Integer entityId) {
        init();
        user = das.findByUserName(username, entityId);
    }

    public void set(Integer userId) {
        user = das.find(userId);
    }

    public void set(String userName, Integer entityId) {
        user = das.findByUserName(userName, entityId);
    }

    public void set(UserDTO user) {
        this.user = user;
    }

    public void setRoot(String userName) {
        user = das.findRoot(userName);
    }

    private void init() {
        eLogger = EventLogger.getInstance();
        das = new UserDAS();
    }

    /**
     * @param executorId This is the user that has ordered the update
     * @param dto This is the user that will be updated
     */
    public void update(Integer executorId, UserDTOEx dto)
            throws SessionInternalError {
        // password is the only one that might've not been set
        String changedPassword = dto.getPassword();
        if (changedPassword != null){
            //encrypt it based on the user role
            changedPassword = JBCrypto.getPasswordCrypto(getMainRole()).encrypt(changedPassword);
        }

        if (changedPassword != null &&
                !changedPassword.equals(user.getPassword())) {
            eLogger.audit(executorId, dto.getId(), Constants.TABLE_BASE_USER,
                    user.getUserId(), EventLogger.MODULE_USER_MAINTENANCE,
                    EventLogger.PASSWORD_CHANGE, null, user.getPassword(),
                    null);
            user.setPassword(changedPassword);
        }
        if (dto.getUserName() != null && !user.getUserName().equals(
                dto.getUserName())) {
            user.setUserName(dto.getUserName());
        }
        if (dto.getLanguageId() != null && !user.getLanguageIdField().equals(
                dto.getLanguageId())) {

                user.setLanguage(new LanguageDAS().find(dto.getLanguageId()));
        }
        if (dto.getEntityId() != null && user.getEntity().getId() !=
                dto.getEntityId()) {

            user.setCompany(new CompanyDAS().find(dto.getEntityId()));
        }
        if (dto.getStatusId() != null && user.getStatus().getId() !=
                dto.getStatusId()) {
            AgeingBL age = new AgeingBL();
            age.setUserStatus(executorId, user.getUserId(), dto.getStatusId(),
                    Calendar.getInstance().getTime());
        }
        updateSubscriptionStatus(dto.getSubscriptionStatusId());
        if (dto.getCurrencyId() != null && !user.getCurrencyId().equals(
                dto.getCurrencyId())) {
            user.setCurrency(new CurrencyDAS().find(dto.getCurrencyId()));
        }

        if (dto.getCustomer() != null) {
            if (dto.getCustomer().getInvoiceDeliveryMethod() != null) {
                user.getCustomer().setInvoiceDeliveryMethod(dto.getCustomer().getInvoiceDeliveryMethod());
            }

            user.getCustomer().setDueDateUnitId(dto.getCustomer().getDueDateUnitId());
            user.getCustomer().setDueDateValue(dto.getCustomer().getDueDateValue());
            user.getCustomer().setDfFm(dto.getCustomer().getDfFm());

            if (dto.getCustomer().getPartner() != null) {
                user.getCustomer().setPartner(dto.getCustomer().getPartner());
            } else {
                user.getCustomer().setPartner(null);
            }

            user.getCustomer().setExcludeAging(dto.getCustomer().getExcludeAging());
            user.getCustomer().setBalanceType(dto.getCustomer().getBalanceType());
            user.getCustomer().setCreditLimit(dto.getCustomer().getCreditLimit());
            user.getCustomer().setAutoRecharge(dto.getCustomer().getAutoRecharge());

            // set the sub-account fields
            user.getCustomer().setIsParent(dto.getCustomer().getIsParent());
            if (dto.getCustomer().getParent() != null) {
                // the API accepts the user ID of the parent instead of the customer ID
                user.getCustomer().setParent(new UserDAS().find(dto.getCustomer().getParent().getId()).getCustomer());

                // log invoice if child changes
                Integer oldInvoiceIfChild = user.getCustomer().getInvoiceChild();
                user.getCustomer().setInvoiceChild(dto.getCustomer().getInvoiceChild());

                eLogger.audit(executorId,
                              user.getId(),
                              Constants.TABLE_CUSTOMER,
                              user.getCustomer().getId(),
                              EventLogger.MODULE_USER_MAINTENANCE,
                              EventLogger.INVOICE_IF_CHILD_CHANGE,
                              (oldInvoiceIfChild != null ? oldInvoiceIfChild : 0),
                              null, null);
            }

            // update the main order
            if (dto.getCustomer().getCurrentOrderId() != null) {
                OrderBL order = new OrderBL(dto.getCustomer().getCurrentOrderId());
                order.setMainSubscription(executorId);
            }
        }

        eLogger.audit(executorId,
                      user.getId(),
                      Constants.TABLE_BASE_USER,
                      user.getId(),
                      EventLogger.MODULE_USER_MAINTENANCE,
                      EventLogger.ROW_UPDATED, null, null, null);

        updateRoles(dto.getRoles(), dto.getMainRoleId());
    }

    private void updateRoles(Set<RoleDTO> theseRoles, Integer main)
            throws SessionInternalError {

        if (theseRoles == null || theseRoles.isEmpty()) {
            if (main != null) {
                if (theseRoles == null) {
                    theseRoles = new HashSet<RoleDTO>();
                }
                theseRoles.add(new RoleDTO(main));
            } else {
                return; // nothing to do
            }
        }

        user.getRoles().clear();
        for (RoleDTO aRole: theseRoles) {
            // make sure the role is in the session
            RoleDTO dbRole = new RoleDAS().find(aRole.getId());
            //dbRole.getBaseUsers().add(user);
            user.getRoles().add(dbRole);
        }
    }

    public boolean exists(String userName, Integer entityId) {
        if (userName == null) {
            LOG.error("exists is being call with a null username");
            return true;
        }
        if (new UserDAS().findByUserName(userName, entityId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public Integer create(UserDTOEx dto) throws SessionInternalError {

        Integer newId;
        LOG.debug("Creating user " + dto);
        List<Integer> roles = new ArrayList<Integer>();
        if (dto.getRoles() == null || dto.getRoles().size() == 0) {
            if (dto.getMainRoleId() != null) {
                roles.add(dto.getMainRoleId());
            } else {
                LOG.warn("Creating user without any role...");
            }
        } else {
            for (RoleDTO role: dto.getRoles()) {
                roles.add(role.getId());
            }
        }

        Integer newUserRole = dto.getMainRoleId();
        JBCrypto passwordCrypter = JBCrypto.getPasswordCrypto(newUserRole);
        dto.setPassword(passwordCrypter.encrypt(dto.getPassword()));

        // may be this is a partner
        if (dto.getPartner() != null) {
            newId = create(dto.getEntityId(), dto.getUserName(), dto.getPassword(),
                    dto.getLanguageId(), roles, dto.getCurrencyId(),
                    dto.getStatusId(), dto.getSubscriptionStatusId());
            PartnerBL partner = new PartnerBL();
            partner.create(dto.getPartner());
            user.setPartner(partner.getEntity());
            partner.getEntity().setBaseUser(user);
        } else if (dto.getCustomer() != null) {
            // link the partner
            PartnerBL partner = null;
            if (dto.getCustomer().getPartner() != null) {
                partner = new PartnerBL(dto.getCustomer().
                        getPartner().getId());
                // see that this partner is valid
                if (partner.getEntity().getUser().getEntity().getId() != dto.getEntityId() ||
                        partner.getEntity().getUser().getDeleted() == 1) {
                    partner = null;
                }
            }
            newId = create(dto.getEntityId(), dto.getUserName(),
                    dto.getPassword(), dto.getLanguageId(),
                    roles, dto.getCurrencyId(),
                    dto.getStatusId(), dto.getSubscriptionStatusId());

            user.setCustomer(new CustomerDAS().create());
            user.getCustomer().setBaseUser(user);
            user.getCustomer().setReferralFeePaid(dto.getCustomer().getReferralFeePaid());

            if (partner != null) {
                user.getCustomer().setPartner(partner.getEntity());
            }

            // set the sub-account fields
            user.getCustomer().setIsParent(dto.getCustomer().getIsParent());
            if (dto.getCustomer().getParent() != null) {
                // the API accepts the user ID of the parent instead of the customer ID
                user.getCustomer().setParent(new UserDAS().find(dto.getCustomer().getParent().getId()).getCustomer());
                user.getCustomer().setInvoiceChild(dto.getCustomer().getInvoiceChild());
            }

            // set dynamic balance fields
            user.getCustomer().setBalanceType(dto.getCustomer().getBalanceType());
            user.getCustomer().setCreditLimit(dto.getCustomer().getCreditLimit());
            user.getCustomer().setDynamicBalance(dto.getCustomer().getDynamicBalance());
            user.getCustomer().setAutoRecharge(dto.getCustomer().getAutoRecharge());

            //additional customer fields
            user.getCustomer().setNotes(dto.getCustomer().getNotes());
            user.getCustomer().setAutoPaymentType(dto.getCustomer().getAutoPaymentType());

        } else { // all the rest
            newId = create(dto.getEntityId(), dto.getUserName(), dto.getPassword(),
                    dto.getLanguageId(), roles, dto.getCurrencyId(),
                    dto.getStatusId(), dto.getSubscriptionStatusId());
        }

        LOG.debug("created user id " + newId);

        return newId;
    }

    private Integer create(Integer entityId, String userName, String password,
            Integer languageId, List<Integer> roles, Integer currencyId,
            Integer statusId, Integer subscriberStatusId)
           throws SessionInternalError {
        // Default the language and currency to that one of the entity
        if (languageId == null) {
            EntityBL entity = new EntityBL(entityId);
            languageId = entity.getEntity().getLanguageId();
        }
        if (currencyId == null) {
            EntityBL entity = new EntityBL(entityId);
            currencyId = entity.getEntity().getCurrencyId();
        }

        // default the statuses
        if (statusId == null) {
            statusId = UserDTOEx.STATUS_ACTIVE;
        }
        if (subscriberStatusId == null) {
            subscriberStatusId = UserDTOEx.SUBSCRIBER_NONSUBSCRIBED;
        }

        UserDTO newUser = new UserDTO();
        newUser.setCompany(new CompanyDAS().find(entityId));
        newUser.setUserName(userName);
        newUser.setPassword(password);
        newUser.setLanguage(new LanguageDAS().find(languageId));
        newUser.setCurrency(new CurrencyDAS().find(currencyId));
        newUser.setUserStatus(new UserStatusDAS().find(statusId));
        newUser.setSubscriberStatus(new SubscriberStatusDAS().find(subscriberStatusId));
        newUser.setDeleted(new Integer(0));
        newUser.setCreateDatetime(Calendar.getInstance().getTime());
        newUser.setFailedAttempts(0);

        user = das.save(newUser);
        HashSet<RoleDTO> rolesDTO = new HashSet<RoleDTO>();
        for (Integer roleId: roles) {
            rolesDTO.add(new RoleDAS().find(roleId));
        }
        updateRoles(rolesDTO, null);

        eLogger.auditBySystem(entityId,
                              user.getId(),
                              Constants.TABLE_BASE_USER,
                              user.getId(),
                              EventLogger.MODULE_USER_MAINTENANCE,
                              EventLogger.ROW_CREATED, null, null, null);

        return user.getUserId();
    }

    @Deprecated
    public boolean validateUserNamePassword(UserDTOEx loggingUser,
           UserDTOEx db) {

        // the user status is not part of this check, as a customer that
        // can't login to the entity's service still has to be able to
        // as a customer to submit a payment or update her credit card
        if (db.getDeleted() == 0 &&
                loggingUser.getEntityId().equals(db.getEntityId())) {

            String dbPassword = db.getPassword();
            String notCryptedLoggingPassword = loggingUser.getPassword();

            //using service specific for DB-user, loging one may not have its role set
            JBCrypto passwordCryptoService = JBCrypto.getPasswordCrypto(db.getMainRoleId());
            String comparableLoggingPassword = passwordCryptoService.encrypt(notCryptedLoggingPassword);

            if (comparableLoggingPassword.equals(dbPassword)){
                user = getUserEntity(db.getUserId());
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to authenticate username/password for web services call.
     * The user must be an administrator and have permission 120 set.
     * Returns the user's UserDTO if successful, otherwise null.
     */
    @Deprecated
    public UserDTO webServicesAuthenticate(String username, String password) {
        // try to get root user for this username that has web
        // services permission
        user = das.findWebServicesRoot(username);
        if (user == null) {
            LOG.warn("Web services authentication: Username \"" + username +
                    "\" is either invalid, isn't an administrator or doesn't " +
                    "have web services permission granted (120).");
            return null;
        }

        // check password
        JBCrypto passwordCryptoService = JBCrypto.getPasswordCrypto(
                Constants.TYPE_ROOT);
        String encryptedPassword = passwordCryptoService.encrypt(
                password);

        if (encryptedPassword.equals(user.getPassword())) {
            return user;
        }
        LOG.warn("Web services authentication: Invlid password \"" + password +
                "\" for username \"" + username + "\"");
        return null;
    }

    public static UserDTO getUserEntity(Integer userId) {
        return new UserDAS().find(userId);
    }

     /**
      * sent the lost password to the user
      * @param entityId
      * @param userId
      * @param languageId
      * @throws SessionInternalError
      * @throws NotificationNotFoundException when no message row or message row is not activated for the specified entity
      */
     public void sendLostPassword(Integer entityId, Integer userId,
             Integer languageId) throws SessionInternalError,
             NotificationNotFoundException {
         NotificationBL notif = new NotificationBL();
         MessageDTO message = notif.getForgetPasswordEmailMessage(entityId, userId, languageId);
         INotificationSessionBean notificationSess =
                 (INotificationSessionBean) Context.getBean(
                 Context.Name.NOTIFICATION_SESSION);
         notificationSess.notify(userId, message);
     }

     public UserDTO getEntity() {
         return user;
     }

     public List<PermissionDTO> getPermissions() {
         List<PermissionDTO> ret = new ArrayList<PermissionDTO>();

         LOG.debug("Reading permisions for user " + user.getUserId());

         for (RoleDTO role: user.getRoles()) {
             // now get the permissions. They come sorted from the DB
             ret.addAll(role.getPermissions());
         }

         // now add / remove those privileges that were granted / revoked
         // to this particular user
         for(PermissionUserDTO permission : user.getPermissions()) {
             if (permission.isGranted()) {
                 // see that this guy has it
                 if (!ret.contains(permission.getPermission())) {
                     // not there, add it
                     //LOG.debug("adding " + thisPerm.getId());
                     ret.add(permission.getPermission());
                 }
             } else {
                 // make sure she doesn't
                 if (ret.contains(permission.getPermission())) {
                     //LOG.debug("removing " + thisPerm.getId());
                     ret.remove(permission.getPermission());
                 }
             }
         }

         // make sure the permissions are sorted
         Collections.sort(ret, new PermissionIdComparator());

         return ret;
     }

    /**
     * Sets the permissions for this user. Permissions that differ from the user's
     * role will be saved as user specific permissions that override the defaults.
     *
     * @param grantedPermissions a set of all permissions granted to this user
     */
    public void setPermissions(Set<PermissionDTO> grantedPermissions) {
        Set<PermissionDTO> rolePermissions = new HashSet<PermissionDTO>();
        for (RoleDTO role : user.getRoles()) {
            rolePermissions.addAll(role.getPermissions());
        }

        Set<PermissionUserDTO> userPermissions = new HashSet<PermissionUserDTO>();

        // add granted permissions
        for (PermissionDTO permission : grantedPermissions) {
            if (!rolePermissions.contains(permission)) {
                userPermissions.add(new PermissionUserDTO(user, permission, (short) 1));
            }
        }

        // add revoked permissions
        for (PermissionDTO permission : rolePermissions) {
            if (!grantedPermissions.contains(permission)) {
                userPermissions.add(new PermissionUserDTO(user, permission, (short) 0));
            }
        }

        LOG.debug("Saving " + userPermissions.size() + " overridden permissions for user " + user.getId());

        user.getPermissions().clear();
        user.getPermissions().addAll(userPermissions);

        this.user = das.save(user);
    }

    public UserWS getUserWS() throws SessionInternalError {
        UserDTOEx dto = DTOFactory.getUserDTOEx(user);
        UserWS retValue = new UserWS(dto);
        // the contact is not included in the Ex
        ContactBL bl= new ContactBL();

        bl.set(dto.getUserId());
        if (bl.getEntity() != null) { // this user has no contact ...
            retValue.setContact(new ContactWS(bl.getDTO()));
        }

        // some entities rather not know the credit card numbers
        if (retValue.getCreditCard() != null) {
            PreferenceBL pref = new PreferenceBL();
            try {
                pref.set(dto.getEntityId(), Constants.PREFERENCE_HIDE_CC_NUMBERS);
            } catch (EmptyResultDataAccessException e) {
                // the default is good for me
            }

            if (pref.getInt() == 1) {
                String ccNumber = retValue.getCreditCard().getNumber();
                if (ccNumber != null) {
                    retValue.getCreditCard().setNumber("************" +
                            ccNumber.substring(ccNumber.length()-4));
                }
            }
        }

        return retValue;
    }

    public Integer getMainRole() {
        if (mainRole == null) {
            List roleIds = new LinkedList();
            for (RoleDTO nextRoleObject : user.getRoles()){
                roleIds.add(nextRoleObject.getId());
            }
            mainRole = selectMainRole(roleIds);
        }
        return mainRole;
    }

    private static Integer selectMainRole(Collection allRoleIds){
        // the main role is the smallest of them, so they have to be ordered in the
        // db in ascending order (small = important);
        Integer result = null;
        for (Iterator roleIds = allRoleIds.iterator(); roleIds.hasNext();){
            Integer nextId = (Integer)roleIds.next();
            if (result == null || nextId.compareTo(result) < 0) {
                result = nextId;
            }
        }
        return result;
    }

    /**
     * Get the locale for this user.
     *
     * @return users locale
     */
    public Locale getLocale() {
        return getLocale(user);
    }

    /**
     * Get a locale for the given user based on their selected language and set country.
     *
     * This method assumes that the user is part of the current persistence context, and that
     * the LanguageDTO association can safely be lazy-loaded.
     *
     * @param user user
     * @return users locale
     */
    public static Locale getLocale(UserDTO user) {
        String languageCode = user.getLanguage().getCode();

        ContactDTO contact = new ContactDAS().findPrimaryContact(user.getId());

        String countryCode = null;
        if (contact != null)
            countryCode = contact.getCountryCode();

        return countryCode != null ? new Locale(languageCode, countryCode) : new Locale(languageCode);
    }

    public Integer getCurrencyId() {
        Integer retValue;

        if (user.getCurrencyId() == null) {
            retValue = user.getEntity().getCurrency().getId();
        } else {
            retValue = user.getCurrencyId();
        }

        return retValue;
    }

    /**
     * Will mark the user as deleted (deleted = 1), and do the same
     * with all her orders, etc ...
     * Not deleted for reporting reasong: invoices, payments
     */
    public void delete(Integer executorId) {
        user.setDeleted(1);
        user.setUserStatus(new UserStatusDAS().find(UserDTOEx.STATUS_DELETED));
        user.setLastStatusChange(Calendar.getInstance().getTime());

        // credit cards
        for (CreditCardDTO cc: user.getCreditCards()) {
            cc.setDeleted(1);
        }
        // orders
        for (OrderDTO order: user.getOrders()) {
            order.setDeleted(1);
        }
        // permisions
        user.getPermissions().clear();
        // roles
        user.getRoles().clear();

        if (executorId != null) {
            eLogger.audit(executorId, user.getId(), Constants.TABLE_BASE_USER,
                    user.getUserId(), EventLogger.MODULE_USER_MAINTENANCE,
                    EventLogger.ROW_DELETED, null, null, null);
        }
    }

    public UserDTO getDto() {
        return user;
    }

    /**
     *
     * @return true if the user has a credit card, or fals if it doeas not
     */
    public boolean hasCreditCard() {
        return !user.getCreditCards().isEmpty();
    }
    /**
     * Verifies that both user belong to the same entity.
     * @param rootUserName
     *  This has to be a root user
     * @param callerUserId
     * @return
     */
    public boolean validateUserBelongs(String rootUserName,
            Integer callerUserId)
            throws SessionInternalError {

        boolean retValue;
        user = das.find(callerUserId);
        set(rootUserName, user.getEntity().getId());
        if (user == null) {
            return false;
        }
        if (user.getDeleted() == 1) {
            throw new SessionInternalError("the caller is set as deleted");
        }
        if (!getMainRole().equals(Constants.TYPE_ROOT)) {
            throw new SessionInternalError("can't validate but root users");
        }
        retValue = true;

        return retValue;
    }

    public void updateAch(AchDTO ach, Integer executorId)
            throws NamingException, SessionInternalError {
        AchBL bl = new AchBL();
        if (ach.getBaseUser() == null) {
            ach.setBaseUser(user);
        }
        // let's see if this guy already has an ach record
        Set<AchDTO> rows = user.getAchs();
        if (rows.size() == 0) {
            bl.create(ach);
            rows.add(new AchDAS().find(bl.getEntity().getId()));
        } else { // its an update
                bl.set(((AchDTO)rows.toArray()[0]).getId());
                bl.update(executorId, ach);
        }
    }

    public static boolean validate(UserWS userWS) {
        return validate(new UserDTOEx(userWS, null));
    }

    /**
     * Validates the user info and the credit card if present
     * @param dto
     * @return
     */
    public static boolean validate(UserDTOEx dto) {
        boolean retValue = true;

        if (dto == null || dto.getUserName() == null ||
                dto.getPassword() == null || dto.getLanguageId() == null ||
                dto.getMainRoleId() == null || dto.getStatusId() == null) {
            retValue = false;
            Logger.getLogger(UserBL.class).debug("invalid " + dto);
        } else if (dto.getCreditCard() != null) {
            retValue = CreditCardBL.validate(dto.getCreditCard());
        }

        return retValue;
    }

    public UserWS[] convertEntitiesToWS(Collection dtos)
            throws SessionInternalError {
        try {
            UserWS[] ret = new UserWS[dtos.size()];
            int index = 0;
            for (Iterator it = dtos.iterator(); it.hasNext();) {
                user = (UserDTO) it.next();
                ret[index] = entity2WS();
                index++;
            }

            return ret;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public UserWS entity2WS()  {
        UserWS retValue = new UserWS();
        retValue.setCreateDatetime(user.getCreateDatetime());
        retValue.setCurrencyId(getCurrencyId());
        retValue.setDeleted(user.getDeleted());
        retValue.setLanguageId(user.getLanguageIdField());
        retValue.setLastLogin(user.getLastLogin());
        retValue.setLastStatusChange(user.getLastStatusChange());
        mainRole = null;
        retValue.setMainRoleId(getMainRole());
        if (user.getPartner() != null) {
            retValue.setPartnerId(user.getPartner().getId());
        }
        retValue.setPassword(user.getPassword());
        retValue.setStatusId(user.getStatus().getId());
        retValue.setUserId(user.getUserId());
        retValue.setUserName(user.getUserName());
        // now the contact
        ContactBL contact = new ContactBL();
        contact.set(retValue.getUserId());
        retValue.setContact(new ContactWS(contact.getDTO()));
        // the credit card
        Collection ccs = user.getCreditCards();
        if (ccs.size() > 0) {
            retValue.setCreditCard(((CreditCardDTO) ccs.toArray()[0]).getOldDTO());
        }
        return retValue;
    }

    public CachedRowSet findActiveWithOpenInvoices(Integer entityId)
            throws SQLException, NamingException {
        prepareStatement(UserSQL.findActiveWithOpenInvoices);
        cachedResults.setInt(1, entityId);

        execute();
        conn.close();
        return cachedResults;
    }


    public UserTransitionResponseWS[] getUserTransitionsById(Integer entityId,
            Integer last, Date to)  {

        try {
            UserTransitionResponseWS[] result = null;
            java.sql.Date toDate = null;
            String query = UserSQL.findUserTransitions;
            if (last.intValue() > 0) {
                query += UserSQL.findUserTransitionsByIdSuffix;
            }
            if (to != null) {
                query += UserSQL.findUserTransitionsUpperDateSuffix;
                toDate = new java.sql.Date(to.getTime());
            }

            int pos = 2;
            LOG.info("Getting transaction list by Id. query --> " + query);
            prepareStatement(query);
            cachedResults.setInt(1, entityId);

            if (last.intValue() > 0) {
                cachedResults.setInt(pos, last);
                pos++;
            }
            if (toDate != null) {
                cachedResults.setDate(pos, toDate);
            }

            execute();
            conn.close();

            if (cachedResults == null || !cachedResults.next()) {
                return null;
            }

            // Load the results into a linked list.
            List tempList = new LinkedList();
            UserTransitionResponseWS temp;
            do {
                temp = new UserTransitionResponseWS();
                temp.setId(cachedResults.getInt(1));
                temp.setToStatusId(Integer.parseInt(cachedResults.getString(2)));
                temp.setTransitionDate(new Date(cachedResults.getDate(3).getTime()));
                temp.setUserId(cachedResults.getInt(5));
                temp.setFromStatusId(cachedResults.getInt(4));
                tempList.add(temp);
            } while (cachedResults.next());

            // The list is now ready. Convert into an array and return.
            result = new UserTransitionResponseWS[tempList.size()];
            int count = 0;
            for (Iterator i = tempList.iterator(); i.hasNext();) {
                result[count] = (UserTransitionResponseWS) i.next();
                count++;
            }
            return result;
        } catch (SQLException e) {
            throw new SessionInternalError("Getting transitions", UserBL.class, e);
        }
    }

    public BigDecimal getBalance(Integer userId) {
        return new InvoiceDAS().findTotalBalanceByUser(userId).subtract(
                new PaymentDAS().findTotalBalanceByUser(userId));
    }

    public BigDecimal getTotalOwed(Integer userId) {
        return new InvoiceDAS().findTotalAmountOwed(userId);
    }

    public UserTransitionResponseWS[] getUserTransitionsByDate(Integer entityId,
            Date from, Date to) {
        try {

            UserTransitionResponseWS[] result = null;
            java.sql.Date toDate = null;
            String query = UserSQL.findUserTransitions;
            query += UserSQL.findUserTransitionsByDateSuffix;

            if (to != null) {
                query += UserSQL.findUserTransitionsUpperDateSuffix;
                toDate = new java.sql.Date(to.getTime());
            }
            LOG.info("Getting transaction list by date. query --> " + query);

            prepareStatement(query);
            cachedResults.setInt(1, entityId);
            cachedResults.setDate(2, new java.sql.Date(from.getTime()));
            if (toDate != null) {
                cachedResults.setDate(3, toDate);
            }
            execute();
            conn.close();

            if (cachedResults == null || !cachedResults.next()) {
                return null;
            }

            // Load the results into a linked list.
            List tempList = new LinkedList();
            UserTransitionResponseWS temp;
            do {
                temp = new UserTransitionResponseWS();
                temp.setId(cachedResults.getInt(1));
                temp.setToStatusId(Integer.parseInt(cachedResults.getString(2)));
                temp.setTransitionDate(new Date(cachedResults.getDate(3).getTime()));
                temp.setUserId(cachedResults.getInt(5));
                temp.setFromStatusId(cachedResults.getInt(4));
                tempList.add(temp);
            } while (cachedResults.next());

            // The list is now ready. Convert into an array and return.
            result = new UserTransitionResponseWS[tempList.size()];
            int count = 0;
            for (Iterator i = tempList.iterator(); i.hasNext();) {
                result[count] = (UserTransitionResponseWS) i.next();
                count++;
            }
            return result;
        } catch (SQLException e) {
            throw new SessionInternalError("Finding transitions", UserBL.class, e);
        }
    }

    public void updateSubscriptionStatus(Integer id) {
        if (id == null || user.getSubscriberStatus().getId() == id) {
            // no update ... it's already there
            return;
        }
        eLogger.auditBySystem(user.getEntity().getId(), user.getId(),
                Constants.TABLE_BASE_USER, user.getUserId(),
                EventLogger.MODULE_USER_MAINTENANCE,
                EventLogger.SUBSCRIPTION_STATUS_CHANGE,
                user.getSubscriberStatus().getId(), id.toString(), null);

        try {
            user.setSubscriberStatus(new SubscriberStatusDAS().find(id));
        } catch (Exception e) {
            throw new SessionInternalError("Can't update a user subscription status",
                    UserBL.class, e);
        }

        // make sure this is in synch with the ageing status of the user
        try {
            PreferenceBL link = new PreferenceBL();
            try {
                link.set(user.getEntity().getId(),
                        Constants.PREFERENCE_LINK_AGEING_TO_SUBSCRIPTION);
            } catch (EmptyResultDataAccessException e) {
                // i'll use the default
            }
            if (link.getInt() == 1) {
                AgeingBL ageing = new AgeingBL();
                if (id.equals(UserDTOEx.SUBSCRIBER_ACTIVE)) {
                    ageing.setUserStatus(null, user.getUserId(), UserDTOEx.STATUS_ACTIVE,
                            Calendar.getInstance().getTime());
                } else if (id.equals(UserDTOEx.SUBSCRIBER_EXPIRED) ||
                        id.equals(UserDTOEx.SUBSCRIBER_DISCONTINUED)) {
                    ageing.setUserStatus(null, user.getUserId(), UserDTOEx.STATUS_SUSPENDED,
                            Calendar.getInstance().getTime());
                }
            }
        } catch (Exception e) {
            throw new SessionInternalError("Can't update a user status",
                    UserBL.class, e);
        }

        LOG.debug("Subscription status updated to " + id);
    }

    // todo: should be moved into a scheduled task that expires passwords and sets a flag on the user
    @Deprecated
    public boolean isPasswordExpired() {
        boolean retValue = false;
        try {
            int expirationDays;
            PreferenceBL pref = new PreferenceBL();
            try {
                pref.set(user.getEntity().getId(), Constants.PREFERENCE_PASSWORD_EXPIRATION);
                expirationDays = pref.getInt();
            } catch (EmptyResultDataAccessException e) {
                expirationDays = pref.getInt();
            }

            // zero means that this is not enforced
            if (expirationDays == 0) {
                return false;
            }

            prepareStatement(UserSQL.lastPasswordChange);
            cachedResults.setInt(1, user.getUserId());
            execute();
            cachedResults.next();
            Date lastChange = cachedResults.getDate(1);
            // no changes? then take when the user signed-up
            if (lastChange == null) {
                lastChange = user.getCreateDatetime();
            }
            conn.close();

            long days = (Calendar.getInstance().getTimeInMillis() -
                    lastChange.getTime()) / (1000 * 60 * 60 * 24);
            if (days >= expirationDays) {
                retValue = true;
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    /**
     * Call this method when the user has provided the wrong password
     * @return
     * True if the account is now locked (maximum retries) or false if it is not locked.
     */
    public boolean failedLoginAttempt() {
        boolean retValue = false;
        int allowedRetries;
        PreferenceBL pref = new PreferenceBL();
        try {
            pref.set(user.getEntity().getId(), Constants.PREFERENCE_FAILED_LOGINS_LOCKOUT);
            allowedRetries = pref.getInt();
        } catch (EmptyResultDataAccessException e) {
            allowedRetries = pref.getInt();
        }

        // zero means not to enforce this rule
        if (allowedRetries > 0) {
            int total = user.getFailedAttempts();
            total ++;
            user.setFailedAttempts(total);

            if (total >= allowedRetries) {
                retValue = true;
                // lock out the user
                JBCrypto passwordCryptoService = JBCrypto.getPasswordCrypto(getMainRole());
                String newPassword = passwordCryptoService.encrypt(Util.getSysProp("lockout_password"));

                user.setPassword(newPassword);
                eLogger.auditBySystem(user.getEntity().getId(), user.getId(),
                        Constants.TABLE_BASE_USER, user.getUserId(),
                        EventLogger.MODULE_USER_MAINTENANCE,
                        EventLogger.ACCOUNT_LOCKED, new Integer(total),
                        null, null);
                LOG.debug("Locked account for user " + user.getUserId());
            }
        }

        return retValue;
    }

    public void successLoginAttempt() {
        user.setLastLogin(Calendar.getInstance().getTime());
        user.setFailedAttempts(0);
    }

    public boolean canInvoice() {
        // can't be deleted and has to be a customer
        if (user.getDeleted() == 1 ||
                !getMainRole().equals(Constants.TYPE_CUSTOMER)) {
            return false;
        }
        // child accounts only get invoiced if the exlicit flag is on
        if (user.getCustomer().getParent() != null &&
                (user.getCustomer().getInvoiceChild() == null ||
                user.getCustomer().getInvoiceChild().intValue() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the user has been invoiced for anything at the time given
     * as a parameter.
     * @return
     */
    public boolean isCurrentlySubscribed(Date forDate) {

        List<Integer> results = new InvoiceDAS().findIdsByUserAndPeriodDate(user.getUserId(), forDate);
        boolean retValue = !results.isEmpty();

        LOG.debug(" user " + user.getUserId() + " is subscribed result " + retValue);

        return retValue;
    }

    public CachedRowSet getByStatus(Integer entityId, Integer statusId, boolean in) {
        try {
            if (in) {
                prepareStatement(UserSQL.findInStatus);
            } else {
                prepareStatement(UserSQL.findNotInStatus);
            }
            cachedResults.setInt(1, statusId.intValue());
            cachedResults.setInt(2, entityId.intValue());
            execute();
            conn.close();
            return cachedResults;
        } catch (Exception e) {
            throw new SessionInternalError("Error getting user by status", UserBL.class, e);
        }
    }

    public CachedRowSet getByCustomField(Integer entityId, Integer typeId, String content) {
        try {
            prepareStatement(UserSQL.findByCustomField);
            cachedResults.setInt(1, typeId);
            cachedResults.setInt(2, entityId);
            cachedResults.setString(3, content);
            execute();
            conn.close();
            return cachedResults;
        } catch (Exception e) {
            throw new SessionInternalError("Error getting user by status", UserBL.class, e);
        }
    }

    /**
     * Returns a rowset of customer IDs with a matching custom contact field. This method
     * is essentially the same as {@link #getByCustomField(Integer, Integer, String)} except
     * that the underlying SQL query uses "like" instead of equals allowing wildcards to be used.
     *
     * @param entityId entity id
     * @param typeId custom contact field type id
     * @param content where custom contact field content is like
     * @return rowset of user IDs
     */
    public CachedRowSet getByCustomFieldLike(Integer entityId, Integer typeId, String content) {
        try {
            prepareStatement(UserSQL.findByCustomFieldLike);
            cachedResults.setInt(1, typeId);
            cachedResults.setInt(2, entityId);
            cachedResults.setString(3, content);
            execute();
            conn.close();
            return cachedResults;
        } catch (Exception e) {
            throw new SessionInternalError("Error getting user by status", UserBL.class, e);
        }
    }

    public CachedRowSet getByCCNumber(Integer entityId, String number) {
        try {

            prepareStatement(UserSQL.findByCreditCard);
            cachedResults.setString(1, number);
            cachedResults.setInt(2, entityId.intValue());
            execute();
            conn.close();

            return cachedResults;
        } catch (Exception e) {
            throw new SessionInternalError("Error getting user by cc", UserBL.class, e);
        }
    }

    public CreditCardDTO getCreditCard() {
        if (user.getCreditCards().isEmpty()) {
            return null;
        } else {
            return (CreditCardDTO) user.getCreditCards().toArray()[0];
        }
    }

    public Integer getByEmail(String email) {
        try {
            Integer retValue = null;
            prepareStatement(UserSQL.findByEmail);
            // this is being use for paypal subscriptions. It only has an email
            // so there is not way to limit by entity_id
            cachedResults.setString(1, email);
            execute();
            if (cachedResults.next()) {
                retValue = cachedResults.getInt(1);
            }
            cachedResults.close();
            conn.close();
            return retValue;
        } catch (Exception e) {
            throw new SessionInternalError("Error getting user by cc", UserBL.class, e);
        }
    }

    /**
     * Only needed due to the locking of entity beans.
     * Remove when using JPA
     * @param userId
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public Integer getEntityId(Integer userId) {
        if (userId == null) {
            userId = user.getUserId();
        }
        UserDTO user = das.find(userId);
        return user.getCompany().getId();

    }

    /**
     * Adds/removes blacklist entries directly related to this user.
     */
    public void setUserBlacklisted(Integer executorId, Boolean isBlacklisted) {
        BlacklistDAS blacklistDAS = new BlacklistDAS();
        List<BlacklistDTO> blacklist = blacklistDAS.findByUserType(
                user.getId(), BlacklistDTO.TYPE_USER_ID);

        if (isBlacklisted) {
            if (blacklist.isEmpty()) {
                // add a new blacklist entry
                LOG.debug("Adding blacklist record for user id: " +
                        user.getId());

                BlacklistDTO entry = new BlacklistDTO();
                entry.setCompany(user.getCompany());
                entry.setCreateDate(new Date());
                entry.setType(BlacklistDTO.TYPE_USER_ID);
                entry.setSource(BlacklistDTO.SOURCE_CUSTOMER_SERVICE);
                entry.setUser(user);
                entry = blacklistDAS.save(entry);

                eLogger.audit(executorId, user.getId(),
                        Constants.TABLE_BLACKLIST, entry.getId(),
                        EventLogger.MODULE_BLACKLIST,
                        EventLogger.BLACKLIST_USER_ID_ADDED, null, null, null);
            }
        } else {
            if (!blacklist.isEmpty()) {
                // remove any blacklist entries found
                LOG.debug("Removing blacklist records for user id: " +
                        user.getId());

                for (BlacklistDTO entry : blacklist) {
                    blacklistDAS.delete(entry);

                    eLogger.audit(executorId, user.getId(),
                            Constants.TABLE_BLACKLIST, entry.getId(),
                            EventLogger.MODULE_BLACKLIST,
                            EventLogger.BLACKLIST_USER_ID_REMOVED, null,
                            null, null);
                }
            }
        }
    }

    public ValidatePurchaseWS validatePurchase(List<ItemDTO> items,
            List<BigDecimal> amounts,
            List<List<PricingField>> pricingFields) {
        if (user.getCustomer() == null) {
            return null;
        }

        LOG.debug("validating purchase items:" + Arrays.toString(items.toArray()) +
                " amounts " + amounts + " customer " + user.getCustomer());

        ValidatePurchaseWS result = new ValidatePurchaseWS();

        // call plug-ins
        try {
            PluggableTaskManager<IValidatePurchaseTask> taskManager =
                    new PluggableTaskManager<IValidatePurchaseTask>(
                    user.getCompany().getId(),
                    Constants.PLUGGABLE_TASK_VALIDATE_PURCHASE);
            IValidatePurchaseTask myTask = taskManager.getNextClass();

            while(myTask != null) {
                myTask.validate(user.getCustomer(), items, amounts, result,
                        pricingFields);
                myTask = taskManager.getNextClass();
            }
        } catch (Exception e) {
            // log stacktrace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            LOG.error("Validate Purchase error: " + e.getMessage() + "\n" +
                    sw.toString());

            result.setSuccess(false);
            result.setAuthorized(false);
            result.setQuantity(BigDecimal.ZERO);
            result.setMessage(new String[] { "Error: " + e.getMessage() } );
        }

        return result;
    }

    public Integer getLanguage() {
        return user.getLanguageIdField();
    }
}
