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

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import com.sapienter.jbilling.server.util.audit.EventLogger;
import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactMapDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactMapDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO;
import com.sapienter.jbilling.server.user.event.NewContactEvent;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.db.JbillingTableDAS;
import java.util.ArrayList;

public class ContactBL {
    private static final Logger LOG = Logger.getLogger(ContactBL.class);             

    // contact types in synch with the table contact_type
    static public final Integer ENTITY = new Integer(1);
    
    // private methods
    private ContactDAS contactDas = null;
    private ContactFieldDAS contactFieldDas = null;
    private ContactDTO contact = null;
    private Integer entityId = null;
    private JbillingTableDAS jbDAS = null;
    private EventLogger eLogger = null;
    
    public ContactBL(Integer contactId)
            throws NamingException {
        init();
        contact = contactDas.find(contactId);
    }
    
    public ContactBL() {
        init();
    }
    
    public void set(Integer userId) {
        contact = contactDas.findPrimaryContact(userId);
        //LOG.debug("Found " + contact + " for " + userId);
        setEntityFromUser(userId);
    }
    
    private void setEntityFromUser(Integer userId) {
        // id the entity
        UserBL user;
        try {
            user = new UserBL();
            entityId = user.getEntityId(userId);
        } catch (Exception e) {
            LOG.error("Finding the entity", e);
        } 

    }
 
    public void set(Integer userId, Integer contactTypeId) {
        contact = contactDas.findContact(userId, contactTypeId);
        setEntityFromUser(userId);
    }

    public void setEntity(Integer entityId) {
        this.entityId = entityId;
        contact = contactDas.findEntityContact(entityId);
    }

    public boolean setInvoice(Integer invoiceId) {
        boolean retValue = false;
        contact = contactDas.findInvoiceContact(invoiceId);
        InvoiceBL invoice = new InvoiceBL(invoiceId);
        if (contact == null) {
            set(invoice.getEntity().getBaseUser().getUserId());

        } else {
            entityId = invoice.getEntity().getBaseUser().getCompany().getId();
            retValue = true;
        }

        return retValue;
    }

    public Integer getPrimaryType(Integer entityId) {
        return new ContactTypeDAS().findPrimary(entityId).getId();
    }
    
    /**
     * Rather confusing considering the previous method, but necessary
     * to follow the convention
     * @return
     */
    public ContactDTO getEntity() {
        return contact;
    }
    
    
    public ContactDTOEx getVoidDTO(Integer myEntityId) {
        entityId = myEntityId;
        ContactDTOEx retValue = new ContactDTOEx();
        retValue.setFieldsTable(initializeFields());
        return retValue;
    }
    
    public ContactDTOEx getDTO() {

        ContactDTOEx retValue =  new ContactDTOEx(
            contact.getId(),
            contact.getOrganizationName(),
            contact.getAddress1(),
            contact.getAddress2(),
            contact.getCity(),
            contact.getStateProvince(),
            contact.getPostalCode(),
            contact.getCountryCode(),
            contact.getLastName(),
            contact.getFirstName(),
            contact.getInitial(),
            contact.getTitle(),
            contact.getPhoneCountryCode(),
            contact.getPhoneAreaCode(),
            contact.getPhoneNumber(),
            contact.getFaxCountryCode(),
            contact.getFaxAreaCode(),
            contact.getFaxNumber(),
            contact.getEmail(),
            contact.getCreateDate(),
            contact.getDeleted(),
            contact.getInclude());
        
        LOG.debug("ContactDTO: getting custom fields");
        try {
            retValue.setFieldsTable(initializeFields());
            for (ContactFieldDTO field: contact.getFields()) {
                // now find the field of this type
                ContactFieldDTO dto = (ContactFieldDTO) retValue
                        .getFieldsTable().get(String.valueOf(field.getType().getId()));
                if (field != null && dto != null) {
                    dto.setContent(field.getContent() == null ? "" : field.getContent());
                    dto.setId(field.getId());
                }
            }
        } catch (Exception e) {
            LOG.error("Error initializing fields", e);
        } 
        
        LOG.debug("Returning dto with " + retValue.getFieldsTable().size() + 
                " fields");
        
        return retValue;
    }
    
    public List<ContactDTOEx> getAll(Integer userId)  {
        List<ContactDTOEx> retValue = new ArrayList<ContactDTOEx>();
        UserBL user = new UserBL(userId);
        entityId = user.getEntityId(userId);
        for (ContactTypeDTO type: user.getEntity().getEntity().getContactTypes()) {
                contact = contactDas.findContact(userId, type.getId());
            if (contact != null) {
                ContactDTOEx dto = getDTO();
                dto.setType(type.getId());
                retValue.add(dto);
            }
        }
        return retValue;
    }
    
    /**
     * Create a Hashtable with the key beign the field type for the
     * entity
     * @return
     */
    private Hashtable<String, ContactFieldDTO> initializeFields() {
        // now go over the entity specific fields
        Hashtable<String, ContactFieldDTO> fields = new Hashtable<String, ContactFieldDTO>();
        EntityBL entity = new EntityBL(entityId);
        for (ContactFieldTypeDTO field: entity.getEntity().getContactFieldTypes()) {
            ContactFieldDTO fieldDto = new ContactFieldDTO();
            fieldDto.setType(field);
            fieldDto.setContent(new String()); // can't be null
            // the key HAS to be a String if we want struts to be able to
            // read the Hashtabe
            fields.put(String.valueOf(field.getId()), fieldDto);
        }
        
        return fields;
    }
    
    private void init() {
        contactDas = new ContactDAS();
        contactFieldDas = new ContactFieldDAS();
        jbDAS = (JbillingTableDAS) Context.getBean(Context.Name.JBILLING_TABLE_DAS);
        eLogger = EventLogger.getInstance();
    }
    
    public Integer createPrimaryForUser(ContactDTOEx dto, Integer userId, Integer entityId) 
            throws SessionInternalError {
        // find which type id is the primary for this entity
        try {
            Integer retValue;
            ContactTypeDTO type = new ContactTypeDAS().findPrimary(entityId);

            retValue =  createForUser(dto, userId, type.getId());
            // this is the primary contact, the only one with a user_id
            // denormilized for performance
            contact.setUserId(userId); 
            return retValue;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        } 
    }
    
    /**
     * Finds what is the next contact type and creates a new
     * contact with it
     * @param dto
     */
    public boolean append(ContactDTOEx dto, Integer userId) 
                throws SessionInternalError {
        UserBL user = new UserBL(userId);
        for (ContactTypeDTO type: user.getEntity().getEntity().getContactTypes()) {
            set(userId, type.getId());
            if (contact == null) {
                // this one is available
                createForUser(dto, userId, type.getId());
                return true;
            }
        }
        
        return false; // no type was avaiable
    }
    
    public Integer createForUser(ContactDTOEx dto, Integer userId, 
            Integer typeId) throws SessionInternalError {
        try {
            return create(dto, Constants.TABLE_BASE_USER, userId, typeId);
        } catch (Exception e) {
            LOG.debug("Error creating contact for " +
                    "user " + userId);
            throw new SessionInternalError(e);
        }
    }
    
    public Integer createForInvoice(ContactDTOEx dto, Integer invoiceId) {
        return create(dto, Constants.TABLE_INVOICE, invoiceId, new Integer(1));
    }
    
    /**
     * 
     * @param dto
     * @param table
     * @param foreignId
     * @param typeId Use 1 if it is not for a user (like and entity or invoice)
     * @return
     * @throws NamingException
     */
    public Integer create(ContactDTOEx dto, String table,  
            Integer foreignId, Integer typeId) {
        // first thing is to create the map to the user
        ContactMapDTO map = new ContactMapDTO();
        map.setJbillingTable(jbDAS.findByName(table));
        map.setContactType(new ContactTypeDAS().find(typeId));
        map.setForeignId(foreignId);
        map = new ContactMapDAS().save(map);
        
        // now the contact itself
        dto.setCreateDate(new Date());
        dto.setDeleted(0);
        dto.setVersionNum(0);
        dto.setId(0);
        
        contact = contactDas.save(new ContactDTO(dto)); // it won't take the Ex
        contact.setContactMap(map);
        map.setContact(contact);
        
        updateCreateFields(dto.getFieldsTable(), false);
        
        LOG.debug("created " + contact);

        // do an event if this is a user contact (invoices, companies, have
        // contacts too)
        if (table.equals(Constants.TABLE_BASE_USER)) {
            NewContactEvent event = new NewContactEvent(contact, entityId);
            EventManager.process(event);

            eLogger.auditBySystem(entityId,
                              contact.getUserId(),
                              Constants.TABLE_CONTACT,
                              contact.getId(),
                              EventLogger.MODULE_USER_MAINTENANCE,
                              EventLogger.ROW_CREATED, null, null, null);
        }

        return contact.getId();
    }
    
    public void updatePrimaryForUser(ContactDTOEx dto, Integer userId) {
        contact = contactDas.findPrimaryContact(userId);
        update(dto);
    }

    public void createUpdatePrimaryForUser(ContactDTOEx dto, Integer userId, Integer entityId) {
        contact = contactDas.findPrimaryContact(userId);

        if (contact == null) {
            createPrimaryForUser(dto, userId, entityId);
        } else {
            update(dto);
        }
    }
    
    public void updateForUser(ContactDTOEx dto, Integer userId,
            Integer contactTypeId) throws SessionInternalError {
        contact = contactDas.findContact(userId, contactTypeId);
        if (contact != null) {
            if (entityId == null) {
                setEntityFromUser(userId);
            }
            update(dto);
        } else {
            try {
                createForUser(dto, userId, contactTypeId);
            } catch (Exception e1) {
                throw new SessionInternalError(e1);
            }
        } 
    }
    
    private void update(ContactDTOEx dto) {
        contact.setAddress1(dto.getAddress1());
        contact.setAddress2(dto.getAddress2());
        contact.setCity(dto.getCity());
        contact.setCountryCode(dto.getCountryCode());
        contact.setEmail(dto.getEmail());
        contact.setFaxAreaCode(dto.getFaxAreaCode());
        contact.setFaxCountryCode(dto.getFaxCountryCode());
        contact.setFaxNumber(dto.getFaxNumber());
        contact.setFirstName(dto.getFirstName());
        contact.setInitial(dto.getInitial());
        contact.setLastName(dto.getLastName());
        contact.setOrganizationName(dto.getOrganizationName());
        contact.setPhoneAreaCode(dto.getPhoneAreaCode());
        contact.setPhoneCountryCode(dto.getPhoneCountryCode());
        contact.setPhoneNumber(dto.getPhoneNumber());
        contact.setPostalCode(dto.getPostalCode());
        contact.setStateProvince(dto.getStateProvince());
        contact.setTitle(dto.getTitle());
        contact.setInclude(dto.getInclude());
        if (entityId == null) {
            setEntityFromUser(contact.getUserId());
        }
        
        NewContactEvent event = new NewContactEvent(contact, entityId);
        EventManager.process(event);

        eLogger.auditBySystem(entityId,
                              contact.getUserId(),
                              Constants.TABLE_CONTACT,
                              contact.getId(),
                              EventLogger.MODULE_USER_MAINTENANCE,
                              EventLogger.ROW_UPDATED, null, null, null);

        updateCreateFields(dto.getFieldsTable(), true);
    }
    
    private void updateCreateFields(Hashtable fields, boolean isUpdate) {
        if (fields == null) {
            // if the fields are not there, do nothing
            return;
        }
        // now the per-entity fields
        for (Iterator it = fields.keySet().iterator(); it.hasNext();) {
            String type = (String) it.next();
            ContactFieldDTO field = (ContactFieldDTO) fields.get(type);
            // we can't create or update custom fields with null value
            if (field.getContent() == null) {
                continue;
            }
            if (isUpdate) {
                if (field.getId() != 0) {
                    contactFieldDas.find(field.getId()).setContent(field.getContent());
                } else {
                    // it is un update, but don't know the field id
                    ContactFieldDTO aField = contactFieldDas.findByType(Integer.valueOf(type), contact.getId());
                    if (aField != null) {
                        aField.setContent(field.getContent());
                    } else {
                        // not there yet. It's ok
                        createContactField(Integer.valueOf(type), field.getContent());
                    }
                }
            } else {
                // create the new field
                createContactField(Integer.valueOf(type), field.getContent());
            }
        }

    }
    
    private void createContactField(Integer type, String content) {
        ContactFieldDTO newField = new ContactFieldDTO();
        newField.setType(new ContactFieldTypeDAS().find(type));
        newField.setContent(content);
        newField.setContact(contact);
        newField = new ContactFieldDAS().save(newField);
        contact.getFields().add(newField);
    }
    
    public void delete() {
        
        if (contact == null) return;
        
        LOG.debug("Deleting contact " + contact.getId());
        // delete the map first
        new ContactMapDAS().delete(contact.getContactMap());
        
        // now the fields
        for(ContactFieldDTO field: contact.getFields()) {
            new ContactFieldDAS().delete(field);
        }
        contact.getFields().clear();

        // for the logger
        Integer entityId = this.entityId;
        Integer userId = contact.getUserId();
        Integer contactId = contact.getId();

        // the contact goes last
        contactDas.delete(contact);
        contact = null;

        // log event
        eLogger.auditBySystem(entityId,
                              userId,
                              Constants.TABLE_CONTACT,
                              contactId,
                              EventLogger.MODULE_USER_MAINTENANCE,
                              EventLogger.ROW_DELETED, null, null, null);
    }
    
    /**
     * Sets this contact object to that on the parent, taking the children id
     * as a parameter. 
     * @param customerId
     */
    public void setFromChild(Integer userId) {
        UserBL customer = new UserBL(userId);
        set(customer.getEntity().getCustomer().getParent().getBaseUser().getUserId());
    }
}
