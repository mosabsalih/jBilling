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
package com.sapienter.jbilling.server.user.contact.db;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.sapienter.jbilling.server.user.db.UserDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@TableGenerator(
        name="contact_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="contact",
        allocationSize = 100
        )
@Table(name="contact", uniqueConstraints = @UniqueConstraint(columnNames="user_id"))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ContactDTO  implements java.io.Serializable {


     private Integer id;
     private String organizationName;
     private String address1;
     private String address2;
     private String city;
     private String stateProvince;
     private String postalCode;
     private String countryCode;
     private String lastName;
     private String firstName;
     private String initial;
     private String title;
     private Integer phoneCountryCode;
     private Integer phoneAreaCode;
     private String phoneNumber;
     private Integer faxCountryCode;
     private Integer faxAreaCode;
     private String faxNumber;
     private String email;
     private Date createDate;
     private int deleted;
     private Integer include;
     private Integer userId;
     private UserDTO baseUser;
     private ContactMapDTO contactMap = null;
     private Set<ContactFieldDTO> fields = new HashSet<ContactFieldDTO>(0);
     private int versionNum;

    public ContactDTO() {
    }

    
    public ContactDTO(Integer id, Date createDatetime, int deleted) {
        this.id = id;
        this.createDate = createDatetime;
        this.deleted = deleted;
    }
    public ContactDTO(Integer id, String organizationName, String streetAddres1, String streetAddres2, String city, String stateProvince, String postalCode, String countryCode, String lastName, String firstName, String personInitial, String personTitle, Integer phoneCountryCode, Integer phoneAreaCode, String phonePhoneNumber, Integer faxCountryCode, Integer faxAreaCode, String faxPhoneNumber, String email, Date createDatetime, int deleted, Integer notificationInclude, Integer userId, ContactMapDTO contactMap, Set<ContactFieldDTO> contactFields) {
       this.id = id;
       this.organizationName = organizationName;
       this.address1 = streetAddres1;
       this.address2 = streetAddres2;
       this.city = city;
       this.stateProvince = stateProvince;
       this.postalCode = postalCode;
       this.countryCode = countryCode;
       this.lastName = lastName;
       this.firstName = firstName;
       this.initial = personInitial;
       this.title = personTitle;
       this.phoneCountryCode = phoneCountryCode;
       this.phoneAreaCode = phoneAreaCode;
       this.phoneNumber = phonePhoneNumber;
       this.faxCountryCode = faxCountryCode;
       this.faxAreaCode = faxAreaCode;
       this.faxNumber = faxPhoneNumber;
       this.email = email;
       this.createDate = createDatetime;
       this.deleted = deleted;
       this.include = notificationInclude;
       this.userId = userId;
       this.contactMap = contactMap;
       this.fields = contactFields;
    }
    
    public ContactDTO(ContactDTO other) {
        setId(other.getId());
        setOrganizationName(other.getOrganizationName());
        setAddress1(other.getAddress1());
        setAddress2(other.getAddress2());
        setCity(other.getCity());
        setStateProvince(other.getStateProvince());
        setPostalCode(other.getPostalCode());
        setCountryCode(other.getCountryCode());
        setLastName(other.getLastName());
        setFirstName(other.getFirstName());
        setInitial(other.getInitial());
        setTitle(other.getTitle());
        setPhoneCountryCode(other.getPhoneCountryCode());
        setPhoneAreaCode(other.getPhoneAreaCode());
        setPhoneNumber(other.getPhoneNumber());
        setFaxCountryCode(other.getFaxCountryCode());
        setFaxAreaCode(other.getFaxAreaCode());
        setFaxNumber(other.getFaxNumber());
        setEmail(other.getEmail());
        setCreateDate(other.getCreateDate());
        setDeleted(other.getDeleted());
        setInclude(other.getInclude());
        setUserId(other.getUserId());
        setContactMap(other.getContactMap());
        setFields(other.getFields());
        setVersionNum(other.getVersionNum());
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="contact_GEN")
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    @Column(name="organization_name", length=200)
    public String getOrganizationName() {
        return this.organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    @Column(name="street_addres1", length=100)
    public String getAddress1() {
        return this.address1;
    }
    
    public void setAddress1(String streetAddres1) {
        this.address1 = streetAddres1;
    }
    
    @Column(name="street_addres2", length=100)
    public String getAddress2() {
        return this.address2;
    }
    
    public void setAddress2(String streetAddres2) {
        this.address2 = streetAddres2;
    }
    
    @Column(name="city", length=50)
    public String getCity() {
        return this.city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    @Column(name="state_province", length=30)
    public String getStateProvince() {
        return this.stateProvince;
    }
    
    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }
    
    @Column(name="postal_code", length=15)
    public String getPostalCode() {
        return this.postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    @Column(name="country_code", length=2)
    public String getCountryCode() {
        return this.countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    @Column(name="last_name", length=30)
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Column(name="first_name", length=30)
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Column(name="person_initial", length=5)
    public String getInitial() {
        return this.initial;
    }
    
    public void setInitial(String personInitial) {
        this.initial = personInitial;
    }
    
    @Column(name="person_title", length=40)
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String personTitle) {
        this.title = personTitle;
    }
    
    @Column(name="phone_country_code")
    public Integer getPhoneCountryCode() {
        return this.phoneCountryCode;
    }
    
    public void setPhoneCountryCode(Integer phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }
    
    @Column(name="phone_area_code")
    public Integer getPhoneAreaCode() {
        return this.phoneAreaCode;
    }
    
    public void setPhoneAreaCode(Integer phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }
    
    @Column(name="phone_phone_number", length=20)
    public String getPhoneNumber() {
        return this.phoneNumber;
    }
    
    public void setPhoneNumber(String phonePhoneNumber) {
        this.phoneNumber = phonePhoneNumber;
    }

    @Transient
    public String getCompletePhoneNumber() {
        StringBuilder phone = new StringBuilder();

        if (phoneCountryCode != null)
            phone.append(phoneCountryCode).append(" ");

        if (phoneAreaCode != null)
            phone.append(phoneAreaCode).append(" ");

        if (phoneNumber != null)
            phone.append(phoneNumber);

        return phone.toString();
    }
    
    @Column(name="fax_country_code")
    public Integer getFaxCountryCode() {
        return this.faxCountryCode;
    }
    
    public void setFaxCountryCode(Integer faxCountryCode) {
        this.faxCountryCode = faxCountryCode;
    }
    
    @Column(name="fax_area_code")
    public Integer getFaxAreaCode() {
        return this.faxAreaCode;
    }
    
    public void setFaxAreaCode(Integer faxAreaCode) {
        this.faxAreaCode = faxAreaCode;
    }
    
    @Column(name="fax_phone_number", length=20)
    public String getFaxNumber() {
        return this.faxNumber;
    }
    
    public void setFaxNumber(String faxPhoneNumber) {
        this.faxNumber = faxPhoneNumber;
    }

    @Transient
    public String getCompleteFaxNumber() {
        StringBuilder phone = new StringBuilder();

        if (faxCountryCode != null)
            phone.append(faxCountryCode).append(" ");

        if (faxAreaCode != null)
            phone.append(faxAreaCode).append(" ");

        if (faxNumber != null)
            phone.append(faxNumber);

        return phone.toString();
    }
    
    @Column(name="email", length=200)
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    @Column(name="create_datetime", nullable=false, length=29)
    public Date getCreateDate() {
        return this.createDate;
    }
    
    public void setCreateDate(Date createDatetime) {
        this.createDate = createDatetime;
    }
    
    @Column(name="deleted", nullable=false)
    public int getDeleted() {
        return this.deleted;
    }
    
    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
    
    @Column(name="notification_include")
    public Integer getInclude() {
        return this.include;
    }
    
    public void setInclude(Integer notificationInclude) {
        this.include = notificationInclude;
    }

    /**
     * Convenience back-reference to the user (if this is a primary contact). This association
     * is read-only and will not persist or update the user. Use {@link #setUserId(Integer)}
     * instead.
     *
     * @return base user
     */
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", unique = true, insertable = false, updatable = false)
    public UserDTO getBaseUser() {
        return baseUser;
    }

    public void setBaseUser(UserDTO baseUser) {
        this.baseUser = baseUser;
    }

    @Column(name = "user_id", unique = true)
    public Integer getUserId() {
        return this.userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="contact")
    public ContactMapDTO getContactMap() {
        return this.contactMap;
    }
    public void setContactMap(ContactMapDTO contactMap) {
        this.contactMap = contactMap;
    }
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="contact")
    public Set<ContactFieldDTO> getFields() {
        if (fields == null) {
            fields = new HashSet<ContactFieldDTO>(0);
        }
        return this.fields;
    }
    
    public void setFields(Set<ContactFieldDTO> contactFields) {
        this.fields = contactFields;
    }

    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    public String toString() {
        return " id " + getId() +
        " organizationName " + getOrganizationName() +
        " address1 " + getAddress1() +
        " address2 " + getAddress2() +
        " city " + getCity() +
        " stateProvince " + getStateProvince() +
        " postalCode " + getPostalCode() +
        " countryCode " + getCountryCode() +
        " lastName " + getLastName() +
        " firstName " + getFirstName() +
        " initial " + getInitial() +
        " title " + getTitle() +
        " phoneCountryCode " + getPhoneCountryCode() +
        " phoneAreaCode " + getPhoneAreaCode() +
        " phoneNumber " + getPhoneNumber() +
        " faxCountryCode " + getFaxCountryCode() +
        " faxAreaCode " + getFaxAreaCode() +
        " faxNumber " + getFaxNumber() +
        " email " + getEmail() +
        " createDate " + getCreateDate() +
        " deleted " + getDeleted() +
        " include " + getInclude() +
        " userId " + getUserId() +
        " contactMap = null " + getContactMap() +
        " versionNum " + getVersionNum();


    }
}


