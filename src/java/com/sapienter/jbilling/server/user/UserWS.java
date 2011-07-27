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
 * Created on Dec 18, 2003
 *
 */
package com.sapienter.jbilling.server.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.entity.AchDTO;
import com.sapienter.jbilling.server.entity.CreditCardDTO;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.security.WSSecured;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.util.api.validation.CreateValidationGroup;
import com.sapienter.jbilling.server.util.api.validation.UpdateValidationGroup;

/** @author Emil */
public class UserWS implements WSSecured, Serializable {

    @Min(value = 1, message = "validation.error.min,1", groups = UpdateValidationGroup.class)
    @Max(value = 0, message = "validation.error.max,0", groups = CreateValidationGroup.class)
    private int id;
    private Integer currencyId;
    @Size(min = 5, max = 40, message = "validation.error.size,5,40", groups = CreateValidationGroup.class)
    private String password;
    private int deleted;
    private Date createDatetime;
    private Date lastStatusChange;
    private Date lastLogin;
    @NotNull(message="validation.error.notnull")
    @Size(min = 5, max = 50, message = "validation.error.size,5,50")
    private String userName;
    private int failedAttempts;
    private Integer languageId;

    @Valid
    private CreditCardDTO creditCard = null;
    @Valid
    private AchDTO ach = null;

    @NotNull(message = "validation.error.notnull")
    @Valid
    private ContactWS contact = null;
    private String role = null;
    private String language = null;
    private String status = null;
    private Integer mainRoleId = null;
    private Integer statusId = null;
    private Integer subscriberStatusId = null;
    private Integer customerId = null;
    private Integer partnerId = null;
    private Integer parentId = null;
    private Boolean isParent = null;
    private Boolean invoiceChild = null;
    private Boolean excludeAgeing = null;
    private Integer mainOrderId = null;
    private String[] blacklistMatches = null;
    private Boolean userIdBlacklisted = null;
    private Integer[] childIds = null;
    private String owingBalance = null;
    private Integer balanceType = null;
    private String dynamicBalance = null;
    @Digits(integer = 22, fraction = 10, message="validation.error.not.a.number")
    private String autoRecharge = null;
    @Digits(integer = 22, fraction = 10, message="validation.error.not.a.number")
    private String creditLimit = null;

    private String notes;
    private Integer automaticPaymentType;
    private String companyName;

    private Integer invoiceDeliveryMethodId;
    private Integer dueDateUnitId;
    private Integer dueDateValue;
    private Date nextInvoiceDate;
    
    public UserWS() {
    }

    public UserWS(UserDTOEx dto) {
        id = dto.getId();
        currencyId = dto.getCurrencyId();
        password = dto.getPassword();
        deleted = dto.getDeleted();
        createDatetime = dto.getCreateDatetime();
        lastStatusChange = dto.getLastStatusChange();
        lastLogin = dto.getLastLogin();
        userName = dto.getUserName();
        failedAttempts = dto.getFailedAttempts();
        languageId = dto.getLanguageId();
        creditCard = dto.getCreditCard() == null ? null : dto.getCreditCard().getOldDTO();
        ach = dto.getAch() == null ? null : dto.getAch().getOldDTO();
        role = dto.getMainRoleStr();
        mainRoleId = dto.getMainRoleId();
        language = dto.getLanguageStr();
        status = dto.getStatusStr();
        role = dto.getMainRoleStr();
        statusId = dto.getStatusId();
        subscriberStatusId = dto.getSubscriptionStatusId();

        if (dto.getCustomer() != null) {
            customerId = dto.getCustomer().getId();
            partnerId = (dto.getCustomer().getPartner() == null) ? null : dto.getCustomer().getPartner().getId();
            parentId = (dto.getCustomer().getParent() == null) ? null : dto.getCustomer().getParent().getBaseUser().getId();
            mainOrderId = dto.getCustomer().getCurrentOrderId();
            isParent = dto.getCustomer().getIsParent() != null && dto.getCustomer().getIsParent().equals(1);
            invoiceChild = dto.getCustomer().getInvoiceChild() != null && dto.getCustomer().getInvoiceChild().equals(1);
            excludeAgeing = dto.getCustomer().getExcludeAging() == 1;

            childIds = new Integer[dto.getCustomer().getChildren().size()];
            int index = 0;
            for (CustomerDTO customer : dto.getCustomer().getChildren()) {
                childIds[index] = customer.getBaseUser().getId();
                index++;
            }

            balanceType = dto.getCustomer().getBalanceType();

            setDynamicBalance(dto.getCustomer().getDynamicBalance());
            setCreditLimit(dto.getCustomer().getCreditLimit());
            setAutoRecharge(dto.getCustomer().getAutoRecharge());

            setNotes(dto.getCustomer().getNotes());
            setAutomaticPaymentType(dto.getCustomer().getAutoPaymentType());

            dueDateUnitId = dto.getCustomer().getDueDateUnitId();
            dueDateValue = dto.getCustomer().getDueDateValue();
        }

        blacklistMatches = dto.getBlacklistMatches() != null ? dto.getBlacklistMatches().toArray(new String[dto.getBlacklistMatches().size()]) : null;
        userIdBlacklisted = dto.getUserIdBlacklisted();

        if (null != dto.getCompany()) {
        	companyName= dto.getCompany().getDescription();
        }
        
        setOwingBalance(dto.getBalance());
        
        OrderDTO orderDto= (OrderDTO) new OrderDAS().findEarliestActiveOrder(dto.getId());
        if (null != orderDto) {
        	if ( null != orderDto.getNextBillableDay()) {
        		this.nextInvoiceDate= orderDto.getNextBillableDay();
        	} else if ( null != orderDto.getActiveSince()) {
        		this.nextInvoiceDate= orderDto.getActiveSince();
        	} else if ( null != orderDto.getCreateDate()) {
        		this.nextInvoiceDate= orderDto.getCreateDate();
        	}
        }        
    }

	public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public ContactWS getContact() {
        return contact;
    }

    public void setContact(ContactWS contact) {
        this.contact = contact;
    }

    public CreditCardDTO getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardDTO creditCard) {
        this.creditCard = creditCard;
    }

    public AchDTO getAch() {
        return ach;
    }

    public void setAch(AchDTO ach) {
        this.ach = ach;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String type) {
        this.role = type;
    }

    public Integer getMainRoleId() {
        return mainRoleId;
    }

    public void setMainRoleId(Integer mainRoleId) {
        this.mainRoleId = mainRoleId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getSubscriberStatusId() {
        return subscriberStatusId;
    }

    public void setSubscriberStatusId(Integer subscriberStatusId) {
        this.subscriberStatusId = subscriberStatusId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public Boolean getInvoiceChild() {
        return invoiceChild;
    }

    public void setInvoiceChild(Boolean invoiceChild) {
        this.invoiceChild = invoiceChild;
    }

    public Boolean getExcludeAgeing() {
        return excludeAgeing;
    }

    public void setExcludeAgeing(Boolean excludeAgeing) {
        this.excludeAgeing = excludeAgeing;
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public int getUserId() {
        return id;
    }

    public void setUserId(int id) {
        this.id = id;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(Date lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getMainOrderId() {
        return mainOrderId;
    }

    public void setMainOrderId(Integer mainOrderId) {
        this.mainOrderId = mainOrderId;
    }

    public String[] getBlacklistMatches() {
        return blacklistMatches;
    }

    public void setBlacklistMatches(String[] blacklistMatches) {
        this.blacklistMatches = blacklistMatches;
    }

    public Boolean getUserIdBlacklisted() {
        return userIdBlacklisted;
    }

    public void setUserIdBlacklisted(Boolean userIdBlacklisted) {
        this.userIdBlacklisted = userIdBlacklisted;
    }

    public Integer[] getChildIds() {
        return childIds;
    }

    public void setChildIds(Integer[] childIds) {
        this.childIds = childIds;
    }

    public String getOwingBalance() {
        return owingBalance;
    }

    public BigDecimal getOwingBalanceAsDecimal() {
        return owingBalance == null ? null : new BigDecimal(owingBalance);
    }

    public void setOwingBalance(String owingBalance) {
        this.owingBalance = owingBalance;
    }

    public void setOwingBalance(BigDecimal owingBalance) {
        this.owingBalance = (owingBalance != null ? owingBalance.toString() : null);
    }

    public Integer getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(Integer balanceType) {
        this.balanceType = balanceType;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getCreditLimitAsDecimal() {
         return creditLimit == null ? null : new BigDecimal(creditLimit);
    }

    public void setCreditLimitAsDecimal(BigDecimal creditLimit) {
        setCreditLimit(creditLimit);
    }

    public void setCreditLimit(String creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = (creditLimit != null ? creditLimit.toString() : null);
    }

    public String getDynamicBalance() {
        return dynamicBalance;
    }

    public BigDecimal getDynamicBalanceAsDecimal() {
        return dynamicBalance == null ? null : new BigDecimal(dynamicBalance);
    }

    public void setDynamicBalance(String dynamicBalance) {
        this.dynamicBalance = dynamicBalance;
    }

    public void setDynamicBalance(BigDecimal dynamicBalance) {
        this.dynamicBalance = (dynamicBalance != null ? dynamicBalance.toString() : null);
    }

    public String getAutoRecharge() {
        return autoRecharge;
    }

    public BigDecimal getAutoRechargeAsDecimal() {
        return autoRecharge != null ? new BigDecimal(autoRecharge) : null;
    }

    public void setAutoRechargeAsDecimal(BigDecimal autoRecharge) {
        setAutoRecharge(autoRecharge);
    }

    public void setAutoRecharge(String autoRecharge) {
        this.autoRecharge = autoRecharge;
    }

    public void setAutoRecharge(BigDecimal autoRecharge) {
        this.autoRecharge = (autoRecharge != null ? autoRecharge.toString() : null);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getAutomaticPaymentType() {
        return automaticPaymentType;
    }

    public void setAutomaticPaymentType(Integer automaticPaymentType) {
        this.automaticPaymentType = automaticPaymentType;
    }

    public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

    public Integer getInvoiceDeliveryMethodId() {
        return invoiceDeliveryMethodId;
    }

    public void setInvoiceDeliveryMethodId(Integer invoiceDeliveryMethodId) {
        this.invoiceDeliveryMethodId = invoiceDeliveryMethodId;
    }

    public Integer getDueDateUnitId() {
        return dueDateUnitId;
    }

    public void setDueDateUnitId(Integer dueDateUnitId) {
        this.dueDateUnitId = dueDateUnitId;
    }

    public Integer getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(Integer dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public Date getNextInvoiceDate() {
		return nextInvoiceDate;
	}

	public void setNextInvoiceDate(Date nextInvoiceDate) {
		this.nextInvoiceDate = nextInvoiceDate;
	}

	/**
     * Unsupported, web-service security enforced using {@link #getOwningUserId()}
     *
     * @return null
     */
    public Integer getOwningEntityId() {
        return null;
    }

    public Integer getOwningUserId() {
        return getUserId();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserWS [ach=");
        builder.append(ach);
        builder.append(", autoRecharge=");
        builder.append(autoRecharge);
        builder.append(", automaticPaymentType=");
        builder.append(automaticPaymentType);
        builder.append(", balanceType=");
        builder.append(balanceType);
        builder.append(", blacklistMatches=");
        builder.append(Arrays.toString(blacklistMatches));
        builder.append(", childIds=");
        builder.append(Arrays.toString(childIds));
        builder.append(", companyName=");
        builder.append(companyName);
        builder.append(", contact=");
        builder.append(contact);
        builder.append(", createDatetime=");
        builder.append(createDatetime);
        builder.append(", creditCard=");
        builder.append(creditCard);
        builder.append(", creditLimit=");
        builder.append(creditLimit);
        builder.append(", currencyId=");
        builder.append(currencyId);
        builder.append(", customerId=");
        builder.append(customerId);
        builder.append(", deleted=");
        builder.append(deleted);
        builder.append(", dueDateUnitId=");
        builder.append(dueDateUnitId);
        builder.append(", dueDateValue=");
        builder.append(dueDateValue);
        builder.append(", dynamicBalance=");
        builder.append(dynamicBalance);
        builder.append(", excludeAgeing=");
        builder.append(excludeAgeing);
        builder.append(", failedAttempts=");
        builder.append(failedAttempts);
        builder.append(", id=");
        builder.append(id);
        builder.append(", invoiceChild=");
        builder.append(invoiceChild);
        builder.append(", invoiceDeliveryMethodId=");
        builder.append(invoiceDeliveryMethodId);
        builder.append(", isParent=");
        builder.append(isParent);
        builder.append(", language=");
        builder.append(language);
        builder.append(", languageId=");
        builder.append(languageId);
        builder.append(", lastLogin=");
        builder.append(lastLogin);
        builder.append(", lastStatusChange=");
        builder.append(lastStatusChange);
        builder.append(", mainOrderId=");
        builder.append(mainOrderId);
        builder.append(", mainRoleId=");
        builder.append(mainRoleId);
        builder.append(", nextInvoiceDate=");
        builder.append(nextInvoiceDate);
        builder.append(", notes=");
        builder.append(notes);
        builder.append(", owingBalance=");
        builder.append(owingBalance);
        builder.append(", parentId=");
        builder.append(parentId);
        builder.append(", partnerId=");
        builder.append(partnerId);
        builder.append(", password=");
        builder.append(password);
        builder.append(", role=");
        builder.append(role);
        builder.append(", status=");
        builder.append(status);
        builder.append(", statusId=");
        builder.append(statusId);
        builder.append(", subscriberStatusId=");
        builder.append(subscriberStatusId);
        builder.append(", userIdBlacklisted=");
        builder.append(userIdBlacklisted);
        builder.append(", userName=");
        builder.append(userName);
        builder.append("]");
        return builder.toString();
    }
   

}
