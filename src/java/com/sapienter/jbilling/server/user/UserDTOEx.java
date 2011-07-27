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

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.PermissionIdComparator;
import com.sapienter.jbilling.common.PermissionTypeIdComparator;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.permisson.db.PermissionDTO;
import com.sapienter.jbilling.server.user.permisson.db.PermissionTypeDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.LanguageDAS;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author emilc
 */
public final class UserDTOEx extends UserDTO {

    // constants
    
    // user status in synch with db table user_status
    public static final Integer STATUS_ACTIVE = new Integer(1); // this HAS to be the very first status
    public static final Integer STATUS_SUSPENDED = new Integer(5); 
    public static final Integer STATUS_DELETED = new Integer(8); // this HAS to be the last status
    
    // subscriber status in synch with db table subscriber_status
    public static final Integer SUBSCRIBER_ACTIVE = new Integer(1); 
    public static final Integer SUBSCRIBER_PENDING_UNSUBSCRIPTION = new Integer(2);
    public static final Integer SUBSCRIBER_UNSUBSCRIBED = new Integer(3);
    public static final Integer SUBSCRIBER_PENDING_EXPIRATION= new Integer(4);
    public static final Integer SUBSCRIBER_EXPIRED = new Integer(5);
    public static final Integer SUBSCRIBER_NONSUBSCRIBED = new Integer(6);
    public static final Integer SUBSCRIBER_DISCONTINUED = new Integer(7);

    private List<PermissionDTO> allPermissions = null;
    private List<PermissionDTO> permissionsTypeId = null; // same as before but sorted by type
    private List<Integer> roles = null;
    private Integer mainRoleId = null;
    private String mainRoleStr = null;
    private String languageStr = null;
    private Integer statusId = null;
    private String statusStr = null;
    private Integer subscriptionStatusId = null;
    private String subscriptionStatusStr = null;
    private CreditCardDTO creditCard = null; 
    private AchDTO ach = null;
    private Integer lastInvoiceId = null;
    private String currencySymbol = null;
    private String currencyName = null;
    private Locale locale = null;
    private List<String> blacklistMatches = null;
    private Boolean userIdBlacklisted = null;
    private BigDecimal balance = null; // calculated in real-time. Not a DB field


    /**
     * Constructor for UserDTOEx.
     * @param userId
     * @param entityId
     * @param userName
     * @param password
     * @param deleted
     */
    public UserDTOEx(Integer userId, Integer entityId, String userName,
            String password, Integer deleted, Integer language, Integer roleId,
            Integer currencyId, Date creation, Date modified, Date lLogin, 
            Integer failedAttempts) {
        // set the base dto fields
        setId((userId == null) ? 0 : userId);
        setUserName(userName);
        setPassword(password);
        setDeleted((deleted == null) ? 0 : deleted);
        setLanguage(new LanguageDAS().find(language));
        setCurrency(new CurrencyDAS().find(currencyId));
        setCreateDatetime(creation);
        setLastStatusChange(modified);
        setLastLogin(lLogin);
        setFailedAttempts((failedAttempts == null) ? 0 : failedAttempts);
        // the entity id
        setEntityId(entityId);
        // the permissions are defaulted to nothing
        allPermissions = new ArrayList();
        roles = new ArrayList<Integer>();
        if (roleId != null) {
            // we ask for at least one role for this user
            roles.add(roleId);
            mainRoleId = roleId;
        }
    }
    
    public UserDTOEx(UserWS dto, Integer entityId) {
        setId(dto.getUserId());
        setPassword(dto.getPassword());
        setDeleted(dto.getDeleted());
        setCreateDatetime(dto.getCreateDatetime());
        setLastStatusChange(dto.getLastStatusChange());
        setLastLogin(dto.getLastLogin());
        setUserName(dto.getUserName());
        setFailedAttempts(dto.getFailedAttempts());
        setCurrency(dto.getCurrencyId() == null ? null : new CurrencyDTO(dto.getCurrencyId()));
        creditCard = dto.getCreditCard() == null ? null : new CreditCardDTO(dto.getCreditCard());
        mainRoleStr = dto.getRole();
        mainRoleId = dto.getMainRoleId();
        languageStr = dto.getLanguage();
        setLanguage(dto.getLanguageId() == null ? null : 
                new LanguageDAS().find(dto.getLanguageId()));
        statusStr = dto.getStatus();
        statusId = dto.getStatusId();
        subscriptionStatusId = dto.getSubscriberStatusId();
        setEntityId(entityId);
        
        roles = new ArrayList<Integer>();
        roles.add(mainRoleId);
        
        if (mainRoleId.equals(Constants.TYPE_CUSTOMER)) {
            CustomerDTO customer = new CustomerDTO(dto);
            setCustomer(customer);
        }
    }
    
    public UserDTOEx() {
        super();
    }
    
    public UserDTOEx(UserDTO user) {
       super(user); 
    }

    public List<PermissionDTO> getAllPermissions() {
        return this.allPermissions;
    }
    // this expects the List to be sorted already
    public void setAllPermissions(List<PermissionDTO> permissions) {
        this.allPermissions = permissions;
    }
    
    public boolean isGranted(Integer permissionId) {
        PermissionDTO permission = new PermissionDTO(permissionId);
        if (Collections.binarySearch(allPermissions, permission,
                new PermissionIdComparator()) >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Verifies that a permision for the given type/foreign_id has been
     * granted. This is defenetly an expensive 
     * @param typeId
     * @param foreignId
     * @return
     */
    public boolean isGranted(Integer typeId, Integer foreignId) {
        if (permissionsTypeId == null) {
            permissionsTypeId = new ArrayList<PermissionDTO>();
            permissionsTypeId.addAll(allPermissions);
            Collections.sort(permissionsTypeId, new PermissionTypeIdComparator());
          /*
            Logger.getLogger(UserDTOEx.class).debug("Permissions now = " +
                    permissionsTypeId);
                    */
        }
        boolean retValue;
        PermissionDTO permission = new PermissionDTO(0, new PermissionTypeDTO(typeId, null), 
                foreignId, null, null);
        if (Collections.binarySearch(permissionsTypeId, permission,
                new PermissionTypeIdComparator()) >= 0) {
            retValue = true;
        } else {
            retValue = false;
        }
        
        /*
        Logger.getLogger(UserDTOEx.class).debug("permission for type = " + 
                typeId + " foreignId = " + foreignId + " result = " +
                retValue);
        */
        return retValue;
    }

    /**
     * Returns the entityId.
     * @return Integer
     */
    public Integer getEntityId() {
        return getCompany().getId();
    }

    /**
     * Sets the entityId.
     * @param entityId The entityId to set
     */
    public void setEntityId(Integer entityId) {
        setCompany(new CompanyDAS().find(entityId));
    }

    /**
     * @return
     */
    public Integer getMainRoleId() {
        return mainRoleId;
    }

    /**
     * @return
     */
    public String getMainRoleStr() {
        return mainRoleStr;
    }

    /**
     * @param integer
     */
    public void setMainRoleId(Integer integer) {
        mainRoleId = integer;
        if (roles == null) {
            roles = new ArrayList<Integer>();
        }
        if (!roles.contains(integer)) {
            roles.add(integer);
        }
    }

    /**
     * @param string
     */
    public void setMainRoleStr(String string) {
        mainRoleStr = string;
    }

    /**
     * @return
     */
    public String getLanguageStr() {
        return languageStr;
    }

    /**
     * @param string
     */
    public void setLanguageStr(String string) {
        languageStr = string;
    }

    /**
     * @return
     */
    public Integer getStatusId() {
        return statusId;
    }

    /**
     * @return
     */
    public String getStatusStr() {
        return statusStr;
    }

    /**
     * @param integer
     */
    public void setStatusId(Integer integer) {
        statusId = integer;
    }

    /**
     * @param string
     */
    public void setStatusStr(String string) {
        statusStr = string;
    }

    /**
     * @return
     */
    public CreditCardDTO getCreditCard() {
        return creditCard;
    }

    /**
     * @param cardDTO
     */
    public void setCreditCard(CreditCardDTO cardDTO) {
        creditCard = cardDTO;
    }

    /**
     * @return
     */
    public Integer getLastInvoiceId() {
        return lastInvoiceId;
    }

    /**
     * @param lastInvoiceId
     */
    public void setLastInvoiceId(Integer lastInvoiceId) {
        this.lastInvoiceId = lastInvoiceId;
    }

    /**
     * @return
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * @param currencySymbol
     */
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    /**
     * @return
     */
    public String getCurrencyName() {
        return currencyName;
    }

    /**
     * @param currencyName
     */
    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    /**
     * @return Returns the ach.
     */
    public AchDTO getAch() {
        return ach;
    }
    /**
     * @param ach The ach to set.
     */
    public void setAch(AchDTO ach) {
        this.ach = ach;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Integer getSubscriptionStatusId() {
        return subscriptionStatusId;
    }

    public void setSubscriptionStatusId(Integer subscriptionStatusId) {
        this.subscriptionStatusId = subscriptionStatusId;
    }

    public String getSubscriptionStatusStr() {
        return subscriptionStatusStr;
    }

    public void setSubscriptionStatusStr(String subscriptionStatusStr) {
        this.subscriptionStatusStr = subscriptionStatusStr;
    }
    
    public Integer getLanguageId() {
        if (getLanguage() != null) {
            return getLanguage().getId();
        }
        return null;
    }
    
    public void setUserId(Integer id) {
        setId(id);
    }
    
    public Integer getUserId() {
        return getId();
    }

    public List<String> getBlacklistMatches() {
        return blacklistMatches;
    }

    public void setBlacklistMatches(List<String> blacklistMatches) {
        this.blacklistMatches = blacklistMatches;
    }

    public Boolean getUserIdBlacklisted() {
        return userIdBlacklisted;
    }

    public void setUserIdBlacklisted(Boolean userIdBlacklisted) {
        this.userIdBlacklisted = userIdBlacklisted;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public BigDecimal getBalance() {
        return balance;
    }
}
