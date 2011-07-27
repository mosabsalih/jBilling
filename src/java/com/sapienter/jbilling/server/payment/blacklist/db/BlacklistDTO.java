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
package com.sapienter.jbilling.server.payment.blacklist.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;

@Entity
@TableGenerator(
        name="blacklist_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="blacklist",
        allocationSize = 100
        )
@Table(name = "blacklist")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BlacklistDTO implements Serializable {

    // constants
    
    // blacklist types
    public static final Integer TYPE_USER_ID = new Integer(1);
    public static final Integer TYPE_NAME = new Integer(2);
    public static final Integer TYPE_CC_NUMBER = new Integer(3);
    public static final Integer TYPE_ADDRESS = new Integer(4);
    public static final Integer TYPE_IP_ADDRESS = new Integer(5);
    public static final Integer TYPE_PHONE_NUMBER = new Integer(6);
    
    // blacklist sources
    public static final Integer SOURCE_CUSTOMER_SERVICE = new Integer(1);
    public static final Integer SOURCE_EXTERNAL_UPLOAD = new Integer(2);
    public static final Integer SOURCE_USER_STATUS_CHANGE = new Integer(3);
    public static final Integer SOURCE_BILLING_PROCESS = new Integer(4);

    private static final Logger LOG = Logger.getLogger(BlacklistDTO.class);

    // mapped columns
    
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="blacklist_GEN")
    private Integer id;

    @ManyToOne
    @JoinColumn(name="entity_id", nullable=false)
    private CompanyDTO company;

    @Column(name = "create_datetime", nullable=false, length=29)
    private Date createDate;

    @Column(name = "type", nullable=false)
    private Integer type;

    @Column(name = "source", nullable=false)
    private Integer source;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="credit_card_id")
    private CreditCardDTO creditCard;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="contact_id")
    private ContactDTO contact;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UserDTO user;

    @Version
    @Column(name="OPTLOCK")
    private Integer versionNum;

    public BlacklistDTO() {
    }

    public BlacklistDTO(Integer id, CompanyDTO company, Date createDate, 
            Integer type, Integer source, CreditCardDTO creditCard,
            ContactDTO contact, UserDTO user) {
        this.id = id;
        this.company = company;
        this.createDate = createDate;
        this.type = type;
        this.source = source;
        this.creditCard = creditCard;
        this.contact = contact;
        this.user = user;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getSource() {
        return source;
    }

    public void setCreditCard(CreditCardDTO creditCard) {
        this.creditCard = creditCard;
    }

    public CreditCardDTO getCreditCard() {
        return creditCard;
    }

    public void setContact(ContactDTO contact) {
        this.contact = contact;
    }

    public ContactDTO getContact() {
        return contact;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UserDTO getUser() {
        return user;
    } 

    protected int getVersionNum() { 
        return versionNum; 
    }
}
