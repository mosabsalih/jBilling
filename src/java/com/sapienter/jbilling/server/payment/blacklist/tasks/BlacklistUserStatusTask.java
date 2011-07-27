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

package com.sapienter.jbilling.server.payment.blacklist.tasks;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.blacklist.BlacklistBL;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.process.event.NewUserStatusEvent;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.system.event.task.IInternalEventsTask;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDAS;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;

/**
 * Blacklists users and all their data when their status moves to 
 * suspended or higher. 
 */
public class BlacklistUserStatusTask extends PluggableTask 
        implements IInternalEventsTask {
    private static final Logger LOG = Logger.getLogger(BlacklistUserStatusTask.class);

    private static final Class<Event> events[] = new Class[] { 
            NewUserStatusEvent.class };

    public Class<Event>[] getSubscribedEvents() {
        return events;
    }

    public void process(Event event) {
        NewUserStatusEvent myEvent = (NewUserStatusEvent) event;
        // only process suspended or higher events
        if (myEvent.getNewStatusId() < UserDTOEx.STATUS_SUSPENDED) {
            return;
        }

        // If user was already suspended or higher, then only blacklist user 
        // & their info if their user id isn't already blacklisted.
        if (myEvent.getOldStatusId() >= UserDTOEx.STATUS_SUSPENDED &&
                BlacklistBL.isUserIdBlacklisted(myEvent.getUserId())) {
            LOG.warn("User id is blacklisted for an already suspended or " +
                    "higher user, returning");
            return;
        }

        UserDTO user = new UserDAS().find(myEvent.getUserId());
        BlacklistBL blacklistBL = new BlacklistBL();

        LOG.debug("Adding blacklist records for user id: " + user.getId());

        // blacklist user id
        blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_USER_ID,
                BlacklistDTO.SOURCE_USER_STATUS_CHANGE, null, null, user);

        // user's contact
        ContactDTO contact = new ContactDAS().findPrimaryContact(myEvent.getUserId());

        if (contact == null) {
            LOG.warn("User " + myEvent.getUserId() + " does not have contact information to blacklist.");
            return;
        }

        // contact to be added to blacklist
        ContactDTO newContact = null;

        // blacklist name
        if (contact.getFirstName() != null || contact.getLastName() != null) {
            newContact = new ContactDTO();
            newContact.setCreateDate(new Date());
            newContact.setDeleted(0);
            newContact.setFirstName(contact.getFirstName());
            newContact.setLastName(contact.getLastName());
            blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_NAME,
                    BlacklistDTO.SOURCE_USER_STATUS_CHANGE, null, newContact, 
                    null);
        }

        // blacklist address
        if (contact.getAddress1() != null || contact.getAddress2() != null ||
                contact.getCity() != null || contact.getStateProvince() != null ||
                contact.getPostalCode() != null || contact.getCountryCode() != null) {
            newContact = new ContactDTO();
            newContact.setCreateDate(new Date());
            newContact.setDeleted(0);
            newContact.setAddress1(contact.getAddress1());
            newContact.setAddress2(contact.getAddress2());
            newContact.setCity(contact.getCity());
            newContact.setStateProvince(contact.getStateProvince());
            newContact.setPostalCode(contact.getPostalCode());
            newContact.setCountryCode(contact.getCountryCode());
            blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_ADDRESS,
                    BlacklistDTO.SOURCE_USER_STATUS_CHANGE, null, newContact, null);
        }

        // blacklist phone number
        if (contact.getPhoneCountryCode() != null || 
                contact.getPhoneAreaCode() != null || 
                contact.getPhoneNumber() != null) {
            newContact = new ContactDTO();
            newContact.setCreateDate(new Date());
            newContact.setDeleted(0);
            newContact.setPhoneCountryCode(contact.getPhoneCountryCode());
            newContact.setPhoneAreaCode(contact.getPhoneAreaCode());
            newContact.setPhoneNumber(contact.getPhoneNumber());
            blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_PHONE_NUMBER,
                    BlacklistDTO.SOURCE_USER_STATUS_CHANGE, null, newContact, null);
        }

        // blacklist cc numbers
        Collection<CreditCardDTO> creditCards = user.getCreditCards();
        for (CreditCardDTO cc : creditCards) {
            if (cc.getNumber() != null) {
                CreditCardDTO creditCard = new CreditCardDTO();
                creditCard.setNumber(cc.getNumber());
                creditCard.setDeleted(0);
                creditCard.setCcType(cc.getCcType()); // not null
                creditCard.setCcExpiry(cc.getCcExpiry()); // not null
                blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_CC_NUMBER,
                        BlacklistDTO.SOURCE_USER_STATUS_CHANGE, creditCard, 
                        null, null);
            }
        }

        // blacklist ip address
        Integer ipAddressCcf = 
                BlacklistBL.getIpAddressCcfId(user.getCompany().getId());
        String ipAddress = null;

        if (ipAddressCcf == null) {
            // blacklist preference or payment filter plug-in 
            // not configured properly
            LOG.warn("Null ipAddressCcf - skipping adding IpAddress contact info");
            return;
        }

        // find the ip address custom contact field
        Set<ContactFieldDTO> contactFields = contact.getFields();
        for (ContactFieldDTO contactField : contactFields) {
            if (contactField.getType().getId() == ipAddressCcf) {
                ipAddress = contactField.getContent();
                break;
            }
        }

        // blacklist the ip address if it was found
        if (ipAddress != null) {
            newContact = new ContactDTO();
            newContact.setCreateDate(new Date());
            newContact.setDeleted(0);
            ContactFieldDTO newField = new ContactFieldDTO();
            newField.setType(new ContactFieldTypeDAS().find(ipAddressCcf));
            newField.setContent(ipAddress);
            newField.setContact(newContact);

            Set<ContactFieldDTO> fields = new HashSet<ContactFieldDTO>(1);
            fields.add(newField);
            newContact.setFields(fields);
            blacklistBL.create(user.getCompany(), BlacklistDTO.TYPE_IP_ADDRESS,
                    BlacklistDTO.SOURCE_USER_STATUS_CHANGE, null, newContact, null);
        }
    }
}
