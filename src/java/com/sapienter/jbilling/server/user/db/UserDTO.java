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
package com.sapienter.jbilling.server.user.db;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.notification.db.NotificationMessageArchDTO;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.partner.db.Partner;
import com.sapienter.jbilling.server.user.permisson.db.PermissionUserDTO;
import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import com.sapienter.jbilling.server.util.audit.db.EventLogDTO;
import com.sapienter.jbilling.server.util.csv.Exportable;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.LanguageDTO;
import org.hibernate.annotations.Cascade;

@Entity
@TableGenerator(
        name="base_user_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="base_user",
        allocationSize = 10
        )
// No cache, mutable and critical
@Table(name = "base_user")
public class UserDTO implements Serializable, Exportable {

    private int id;
    private String userName;
    private String password;
    private int deleted;
    boolean enabled = true;
    boolean accountExpired = false;
    boolean accountLocked = false;
    boolean passwordExpired = false;

    private Date createDatetime;
    private Date lastStatusChange;
    private Date lastLogin;
    private int failedAttempts;

    private Set<RoleDTO> roles = new HashSet<RoleDTO>(0);
    private Set<PermissionUserDTO> permissions = new HashSet<PermissionUserDTO>(0);

    private CurrencyDTO currencyDTO;
    private CompanyDTO company;
    private SubscriberStatusDTO subscriberStatus;
    private UserStatusDTO userStatus;
    private LanguageDTO language;
    private CustomerDTO customer;
    private ContactDTO contact;
    private Partner partnersForUserId;
    private int versionNum;

    private Set<PaymentDTO> payments = new HashSet<PaymentDTO>(0);
    private Set<AchDTO> achs = new HashSet<AchDTO>(0);
    private Set<Partner> partnersForRelatedClerk = new HashSet<Partner>(0);
    private Set<OrderDTO> purchaseOrdersForCreatedBy = new HashSet<OrderDTO>(0);
    private Set<OrderDTO> orders = new HashSet<OrderDTO>(0);
    private Set<CreditCardDTO> creditCards = new HashSet<CreditCardDTO>(0);
    private Set<NotificationMessageArchDTO> notificationMessageArchs = new HashSet<NotificationMessageArchDTO>(0);
    private Set<EventLogDTO> eventLogs = new HashSet<EventLogDTO>(0);
    private Set<InvoiceDTO> invoices = new HashSet<InvoiceDTO>(0);

    public UserDTO() {
    }

    public UserDTO(int id) {
        this.id = id;
    }

    public UserDTO(int id, short deleted, Date createDatetime, int failedAttempts) {
        this.id = id;
        this.deleted = deleted;
        this.createDatetime = createDatetime;
        this.failedAttempts = failedAttempts;
    }

    public UserDTO(int id, CurrencyDTO currencyDTO, CompanyDTO entity, SubscriberStatusDTO subscriberStatus,
            UserStatusDTO userStatus, LanguageDTO language, String password, short deleted, Date createDatetime,
            Date lastStatusChange, Date lastLogin, String userName, int failedAttempts, Set<PaymentDTO> payments,
            Set<AchDTO> achs, Set<PermissionUserDTO> permissionUsers,
            Set<Partner> partnersForRelatedClerk, CustomerDTO customer, Partner partnersForUserId,
            Set<OrderDTO> purchaseOrdersForCreatedBy, Set<OrderDTO> purchaseOrdersForUserId,
            Set<CreditCardDTO> creditCards, Set<NotificationMessageArchDTO> notificationMessageArchs, Set<RoleDTO> roles,
            Set<EventLogDTO> eventLogs, Set<InvoiceDTO> invoices) {
        this.id = id;
        this.currencyDTO = currencyDTO;
        this.company = entity;
        this.subscriberStatus = subscriberStatus;
        this.userStatus = userStatus;
        this.language = language;
        this.password = password;
        this.deleted = deleted;
        this.createDatetime = createDatetime;
        this.lastStatusChange = lastStatusChange;
        this.lastLogin = lastLogin;
        this.userName = userName;
        this.failedAttempts = failedAttempts;
        this.payments = payments;
        this.achs = achs;
        this.permissions = permissionUsers;
        this.partnersForRelatedClerk = partnersForRelatedClerk;
        this.customer = customer;
        this.partnersForUserId = partnersForUserId;
        this.purchaseOrdersForCreatedBy = purchaseOrdersForCreatedBy;
        this.orders = purchaseOrdersForUserId;
        this.creditCards = creditCards;
        this.notificationMessageArchs = notificationMessageArchs;
        this.roles = roles;
        this.eventLogs = eventLogs;
        this.invoices = invoices;
    }

    public UserDTO(UserDTO another) {
        setId(another.getId());
        setCurrency(another.getCurrency());
        setCompany(another.getCompany());
        setSubscriberStatus(another.getSubscriberStatus());
        setUserStatus(another.getUserStatus());
        setLanguage(another.getLanguage());
        setPassword(another.getPassword());
        setDeleted(another.getDeleted());
        setCreateDatetime(another.getCreateDatetime());
        setLastStatusChange(another.getLastStatusChange());
        setLastLogin(another.getLastLogin());
        setUserName(another.getUserName());
        setFailedAttempts(another.getFailedAttempts());
        setCustomer(another.getCustomer());
        setPartner(another.getPartner());
        setPayments(another.getPayments());
        setAchs(another.getAchs());
        setPermissions(another.getPermissions());
        setPartnersForRelatedClerk(another.getPartnersForRelatedClerk());
        setPurchaseOrdersForCreatedBy(another.getPurchaseOrdersForCreatedBy());
        setOrders(another.getOrders());
        setCreditCards(another.getCreditCards());
        setNotificationMessageArchs(another.getNotificationMessageArchs());
        setRoles(another.getRoles());
        setEventLogs(another.getEventLogs());
        setInvoices(another.getInvoices());
    }


    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "base_user_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Transient
    public Integer getUserId() {
        return id;
    }

    @Column(name = "user_name", length = 50)
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "password", length = 40)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns 1 if this user is deleted, 0 if they are active.
     * @return is user deleted
     */
    @Column(name = "deleted", nullable = false)
    public int getDeleted() {
        return this.deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    /**
     * Returns true if this user is enabled and not deleted.
     *
     * todo: enabled flag is transient, field currently only exists for Spring Security integration
     *
     * @return true if user enabled
     */
    @Transient
    public boolean isEnabled() {
        return enabled && deleted == 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns true if this user's account is expired.
     *
     * todo: expired flag is transient, field currently only exists for Spring Security integration
     *
     * @return true if user expired
     */
    @Transient
    public boolean isAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    /**
     * Returns true if this user's account has been locked, either by a system administrator
     * or by too many failed log-in attempts.
     *
     * todo: locked flag is transient, field currently only exists for Spring Security integration
     *
     * @return true if user is locked
     */
    @Transient
    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     * Returns true if the users password has expired.
     *
     * todo: expired flag is transient, field currently only exists for Spring Security integration
     *
     * @return true if password has expired
     */
    @Transient
    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    @Column(name = "create_datetime", nullable = false, length = 29)
    public Date getCreateDatetime() {
        return this.createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    @Column(name = "last_status_change", length = 29)
    public Date getLastStatusChange() {
        return this.lastStatusChange;
    }

    public void setLastStatusChange(Date lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    @Column(name = "last_login", length = 29)
    public Date getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Column(name = "failed_attempts", nullable = false)
    public int getFailedAttempts() {
        return this.failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_role_map",
               joinColumns = {@JoinColumn(name = "user_id", updatable = false)},
               inverseJoinColumns = {@JoinColumn(name = "role_id", updatable = false)})
    public Set<RoleDTO> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<PermissionUserDTO> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<PermissionUserDTO> permissionUsers) {
        this.permissions = permissionUsers;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    public CurrencyDTO getCurrency() {
        return this.currencyDTO;
    }

    public void setCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }

    @Transient
    public Integer getCurrencyId() {
        if (getCurrency() == null) {
            return null;
        }
        return getCurrency().getId();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    public CompanyDTO getCompany() {
        return this.company;
    }

    public void setCompany(CompanyDTO entity) {
        this.company = entity;
    }

    @Transient
    public CompanyDTO getEntity() {
        return getCompany();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_status")
    public SubscriberStatusDTO getSubscriberStatus() {
        return this.subscriberStatus;
    }

    public void setSubscriberStatus(SubscriberStatusDTO subscriberStatus) {
        this.subscriberStatus = subscriberStatus;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    public UserStatusDTO getUserStatus() {
        return this.userStatus;
    }

    public void setUserStatus(UserStatusDTO userStatus) {
        this.userStatus = userStatus;
    }

    @Transient
    public UserStatusDTO getStatus() {
        return getUserStatus();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    public LanguageDTO getLanguage() {
        return this.language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }

    @Transient
    public Integer getLanguageIdField() {
        if (getLanguage() == null) {
            return getEntity().getLanguageId();
        }

        return getLanguage().getId();
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<PaymentDTO> getPayments() {
        return this.payments;
    }

    public void setPayments(Set<PaymentDTO> payments) {
        this.payments = payments;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<AchDTO> getAchs() {
        return this.achs;
    }

    public void setAchs(Set<AchDTO> achs) {
        this.achs = achs;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUserByRelatedClerk")
    public Set<Partner> getPartnersForRelatedClerk() {
        return this.partnersForRelatedClerk;
    }

    public void setPartnersForRelatedClerk(Set<Partner> partnersForRelatedClerk) {
        this.partnersForRelatedClerk = partnersForRelatedClerk;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public CustomerDTO getCustomer() {
        return this.customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    /**
     * Convenience mapping for the users primary contact. This association is read-only and
     * will not persist or update the users stored contact. use {@link ContactBL} instead.
     *
     * @return users primary contact
     */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "baseUser")
    public ContactDTO getContact() {
        return contact;
    }

    public void setContact(ContactDTO contact) {
        this.contact = contact;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Partner getPartner() {
        return this.partnersForUserId;
    }

    public void setPartner(Partner partnersForUserId) {
        this.partnersForUserId = partnersForUserId;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUserByCreatedBy")
    public Set<OrderDTO> getPurchaseOrdersForCreatedBy() {
        return this.purchaseOrdersForCreatedBy;
    }

    public void setPurchaseOrdersForCreatedBy(Set<OrderDTO> purchaseOrdersForCreatedBy) {
        this.purchaseOrdersForCreatedBy = purchaseOrdersForCreatedBy;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUserByUserId")
    public Set<OrderDTO> getOrders() {
        return this.orders;
    }

    public void setOrders(Set<OrderDTO> purchaseOrdersForUserId) {
        this.orders = purchaseOrdersForUserId;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_credit_card_map",
               joinColumns = {@JoinColumn(name = "user_id", updatable = false)},
               inverseJoinColumns = {@JoinColumn(name = "credit_card_id", updatable = false)}
    )
    public Set<CreditCardDTO> getCreditCards() {
        return this.creditCards;
    }

    public void setCreditCards(Set<CreditCardDTO> creditCards) {
        this.creditCards = creditCards;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<NotificationMessageArchDTO> getNotificationMessageArchs() {
        return this.notificationMessageArchs;
    }

    public void setNotificationMessageArchs(Set<NotificationMessageArchDTO> notificationMessageArchs) {
        this.notificationMessageArchs = notificationMessageArchs;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<EventLogDTO> getEventLogs() {
        return this.eventLogs;
    }

    public void setEventLogs(Set<EventLogDTO> eventLogs) {
        this.eventLogs = eventLogs;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "baseUser")
    public Set<InvoiceDTO> getInvoices() {
        return this.invoices;
    }

    public void setInvoices(Set<InvoiceDTO> invoices) {
        this.invoices = invoices;
    }

    @Version
    @Column(name = "OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        /*  Avoid lazy loaded fields to prevent a LazyInitializationException
            when printing users outside of the initial transaction. */
        return "UserDTO{"
               + "id=" + id
               + ", userName='" + userName + '\''
               + '}';
    }

    public void touch() {
        // touch
        if (getCustomer() != null) {
            getCustomer().getTotalSubAccounts();
            if (getCustomer().getParent() != null) {
                getCustomer().getParent().getBaseUser().getId();
            }
        }

        if (getPartner() != null) {
            getPartner().touch();
        }
    }

    @Transient
    public String[] getFieldNames() {
        return new String[] {
                "id",
                "userName",
                "password",
                "status",
                "subscriberStatus",
                "deleted",
                "accountExpired",
                "accountLocked",
                "passwordExpired",
                "lastLogin",
                "lastStatusChange",
                "createdDateTime",
                "language",
                "currency",

                // customer
                "invoiceDeliveryMethod",
                "autoPaymentType",
                "notes",
                "parentUserId",
                "isParent",
                "invoiceIfChild",
                "excludeAging",
                "balanceType",
                "dynamicBalance",
                "creditLimit",
                "autoRecharge",

                // contact
                "organizationName",
                "title",
                "firstName",
                "lastName",
                "initial",
                "address1",
                "address2",
                "city",
                "stateProvince",
                "postalCode",
                "countryCode",
                "phoneNumber",
                "faxNumber",
                "email"
        };
    }

    @Transient
    public Object[][] getFieldValues() {
        ContactBL contactBL = new ContactBL();
        contactBL.set(id);

        ContactDTO contact = contactBL.getEntity();

        return new Object[][] {
            {
                id,
                userName,
                password,
                (userStatus != null ? userStatus.getDescription() : null),
                (subscriberStatus != null ? subscriberStatus.getDescription() : null),
                deleted,
                accountExpired,
                accountLocked,
                passwordExpired,
                lastLogin,
                lastStatusChange,
                createDatetime,
                (language != null ? language.getDescription() : null),
                (currencyDTO != null ? currencyDTO.getDescription() : null),

                // customer
                (customer != null && customer.getInvoiceDeliveryMethod() != null
                 ? customer.getInvoiceDeliveryMethod().getId()
                 : null),

                (customer != null ? customer.getAutoPaymentType() : null),
                (customer != null ? customer.getNotes() : null),

                (customer != null && customer.getParent() != null
                 ? customer.getParent().getBaseUser().getId()
                 : null),

                (customer != null ? customer.getIsParent() : null),
                (customer != null ? customer.getInvoiceChild() : null),
                (customer != null ? customer.getExcludeAging() : null),
                (customer != null ? customer.getBalanceType() : null),
                (customer != null ? customer.getDynamicBalance() : null),
                (customer != null ? customer.getCreditLimit() : null),
                (customer != null ? customer.getAutoRecharge() : null),

                // contact
                (contact != null ? contact.getOrganizationName() : null),
                (contact != null ? contact.getTitle() : null),
                (contact != null ? contact.getFirstName() : null),
                (contact != null ? contact.getLastName() : null),
                (contact != null ? contact.getInitial() : null),
                (contact != null ? contact.getCity() : null),
                (contact != null ? contact.getStateProvince() : null),
                (contact != null ? contact.getPostalCode() : null),
                (contact != null ? contact.getCountryCode() : null),
                (contact != null ? contact.getCompletePhoneNumber() : null),
                (contact != null ? contact.getCompleteFaxNumber() : null),
                (contact != null ? contact.getEmail() : null),
            }
        };
    }
}
