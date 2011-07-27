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
 * Created on Jul 26, 2004
 *
 */
package com.sapienter.jbilling.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.item.IItemSessionBean;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.order.IOrderSessionBean;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.IUserSessionBean;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
import java.math.BigDecimal;


/**
 * @author Emil
 */
public class UploadData {

    public static void main(String[] args) {
        
        // for each field that will be sent to the server we need an index
        int first_name = -1;
        int last_name = -1;
        int user_name = -1;
        int password = -1;
        int organization_name = -1;
        int street_addres1 = -1;
        int street_addres2 = -1;
        int phone_area_code = -1;
        int phone_phone_number = -1;
        int notes = -1;
        int country = -1;
        int status = -1;
        int email = -1;
        int city = -1;
        int state = -1;
        int postal_code = -1;
        int credit_card_number = -1;
        int expiry_month = -1;
        int expiry_year = -1;
        int name_on_card = -1;
        // these are for the orders
        int box = -1;
        int period = -1;
        int active_since = -1;
        int active_until = -1;
        int total = -1;
        // these are for entity-specific contact fields
        Hashtable entitySpecificFeilds;
        int specific[];
        int specificType[];
        
        String record = null;
        try {
            // see if all the properties are in place
            Properties prop = new Properties();
            FileInputStream gpFile = new FileInputStream("upload.properties");
            prop.load(gpFile);
            
            Integer entityId = Integer.valueOf(prop.getProperty("entity_id"));
            Integer languageId = Integer.valueOf((String) prop.getProperty(
                    "entity_language"));
            String entityName = prop.getProperty("entity_name");
            String fileName = prop.getProperty("file");
            Boolean processOrders = Boolean.valueOf(
                    prop.getProperty("load_orders"));
            // initialize the entity specific data
            entitySpecificFeilds = new Hashtable();
            int totalSpecificFields = Integer.valueOf(prop.getProperty(
                    "specific_fields", "0")).intValue();
            System.out.println("specific fields to load = " + totalSpecificFields);
            specific = new int[totalSpecificFields];
            specificType = new int[totalSpecificFields];
            for (int f = 0; f < totalSpecificFields; f++) {
                specific[f] = -1;
            }
            
            
            System.out.println("Processing file " + fileName + " for entity " + 
                    entityId);
    
            // open the file
            BufferedReader file = new BufferedReader(new FileReader(fileName));

            // TODO: use the standard API
/*            IUserSessionBean remoteSession = (IUserSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.USER_REMOTE_SESSION);
*/
            IUserSessionBean remoteSession = null;
            String header = file.readLine();
            String columns[] = header.split("\t");
            for (int f = 0; f < columns.length; f++) {
                // scan for the columns
                if (columns[f].equalsIgnoreCase("first_name")) {
                    first_name = f;
                } else if (columns[f].equalsIgnoreCase("last_name")) {
                    last_name = f;
                } else if (columns[f].equalsIgnoreCase("organization_name")) {
                    organization_name = f;
                } else if (columns[f].equalsIgnoreCase("street_addres1")) {
                    street_addres1 = f;
                } else if (columns[f].equalsIgnoreCase("street_addres2")) {
                    street_addres2 = f;
                } else if (columns[f].equalsIgnoreCase("phone_phone_number")) {
                    phone_phone_number = f;
                } else if (columns[f].equalsIgnoreCase("phone_area_code")) {
                    phone_area_code = f;
                } else if (columns[f].equalsIgnoreCase("notes")) {
                    notes = f;
                } else if (columns[f].equalsIgnoreCase("country")) {
                    country = f;
                } else if (columns[f].equalsIgnoreCase("status")) {
                    status = f;
                } else if (columns[f].equalsIgnoreCase("user_name")) {
                    user_name = f;
                } else if (columns[f].equalsIgnoreCase("password")) {
                    password = f;
                } else if (columns[f].equalsIgnoreCase("email")) {
                    // TODO : Warning!!, for some reason last time this was
                    // used it didn't load the emails properly
                    email = f;
                } else if (columns[f].equalsIgnoreCase("state")) {
                    state = f;
                } else if (columns[f].equalsIgnoreCase("city")) {
                    city = f;
                } else if (columns[f].equalsIgnoreCase("credit_card_number")) {
                    credit_card_number = f;
                } else if (columns[f].equalsIgnoreCase("expiry_month")) {
                    expiry_month = f;
                } else if (columns[f].equalsIgnoreCase("expiry_year")) {
                    expiry_year = f;
                } else if (columns[f].equalsIgnoreCase("name_on_card")) {
                    name_on_card = f;
                } else if (columns[f].equalsIgnoreCase("postal_code")) {
                    postal_code = f;
                }
                
                if (processOrders.booleanValue()) {
                    if (columns[f].equalsIgnoreCase("period")) {
                        period = f;
                    } else if (columns[f].equalsIgnoreCase("active_since")) {
                        active_since = f;
                    } else if (columns[f].equalsIgnoreCase("active_until")) {
                        active_until = f;
                    } else if (columns[f].equalsIgnoreCase("total")) {
                        total = f;
                    } else if (columns[f].equalsIgnoreCase("box")) {
                        box = f;
                    }
                }
                
                // go over the specific fields
                for (int spField = 1; spField <= totalSpecificFields; 
                        spField++) {
                    String spName = (String) prop.get("specific_title_" + 
                            spField);
                    if (columns[f].equalsIgnoreCase(spName)) {
                        specific[spField - 1] = f;
                        specificType[spField - 1] = Integer.valueOf(
                                (String) prop.get("specific_type_" + 
                                        spField)).intValue();
                        break;
                    }
                }
            }
            
            int totalRows = 0;
            record = readLine(file);
            while (record != null) {
                totalRows++;
                String fields[] = record.split("\t");
                
                // get the user object ready
                UserDTOEx user = new UserDTOEx();
                ContactDTOEx contact = new ContactDTOEx();
                CustomerDTO customer = new CustomerDTO();
                contact.setInclude(new Integer(1));
                
                user.setEntityId(entityId);
                user.getRoles().add(new RoleDTO(Constants.TYPE_CUSTOMER));
                
                if (first_name >= 0) {
                    contact.setFirstName(fields[first_name].trim());
                }
                if (last_name >= 0) {
                    contact.setLastName(fields[last_name].trim());
                }
                if (organization_name >= 0) {
                    contact.setOrganizationName(fields[organization_name].trim());
                }
                if (street_addres1 >= 0) {
                    contact.setAddress1(fields[street_addres1].trim());
                }
                if (street_addres2 >= 0) {
                    contact.setAddress2(fields[street_addres2].trim());
                }
                if (phone_phone_number >= 0) {
                    contact.setPhoneNumber(fields[phone_phone_number].trim());
                }
                if (phone_area_code >= 0) {
                    if (fields[phone_area_code].trim().length() > 0) {
                        contact.setPhoneAreaCode(Integer.valueOf(
                                fields[phone_area_code]));
                    }
                }
                if (country >= 0) {
                    contact.setCountryCode(fields[country].trim());
                }
                if (notes >= 0) {
                    customer.setNotes(fields[notes].trim());
                }
                if (email >= 0) {
                    contact.setEmail(fields[email].trim());
                }
                if (city >= 0) {
                    contact.setCity(fields[city].trim());
                }
                if (state >= 0) {
                    contact.setStateProvince(fields[state].trim());
                }
                if (postal_code >= 0) {
                    contact.setPostalCode(fields[postal_code].trim());
                }
                if (status >= 0) {
                    if (fields[status].charAt(0) == 'A' ||
                            fields[status].equals("FA")) {
                        user.setStatusId(UserDTOEx.STATUS_ACTIVE);
                    } else {
                        user.setStatusId(UserDTOEx.STATUS_DELETED);
                    }
                } else {
                    // default to active
                    user.setStatusId(UserDTOEx.STATUS_ACTIVE);
                }
                
                // define the username
                String username = new String();
                if (user_name >= 0) {
                    username = fields[user_name].trim();
                } else { // cook our own
                    if (contact.getFirstName() != null && 
                            contact.getFirstName().length() > 0) {
                        username += contact.getFirstName().charAt(0);
                    }
                    if (contact.getLastName() != null) {
                        if (contact.getLastName().length() >= 15) {
                            username += contact.getLastName().substring(
                                    0, 15 - username.length());
                        } else {
                            username += contact.getLastName();;
                        }
                    }
                    if (username.length() == 0) {
                        username += entityName;
                    }
                    // add a number for uniqueness
                    username += "_" + totalRows;
                }
                user.setUserName(username);
                
                // now the password
                if (password >= 0) {
                    user.setPassword(fields[password].trim());
                } else {
                    // default to the entity name
                    user.setPassword(entityName);
                }
                
                // the credit card
                CreditCardDTO cc = new CreditCardDTO();
                cc.setCcExpiry(new Date());
                if (credit_card_number >= 0 && 
                        fields[credit_card_number].trim().length() > 0) {
                    cc.setNumber(fields[credit_card_number].trim());
                    if (expiry_month >= 0) {
                        cc.getCcExpiry().setMonth(Integer.valueOf(fields[expiry_month].trim()).intValue());
                        cc.getCcExpiry().setDate(1);
                    }
                    if (expiry_year >= 0) {
                        cc.getCcExpiry().setYear(Integer.valueOf(fields[expiry_year].trim()).
                                intValue() - 1900);
                    }
                    if (name_on_card >= 0) {
                        cc.setName(fields[name_on_card].trim());
                    } 

                } else {
                    cc = null;
                }
                //System.out.println("CC = " + cc);
                
                user.setCustomer(customer);
                
                // the entity specific fields
                for (int spField = 0; spField < totalSpecificFields; spField++) {
                    if (specific[spField] >= 0) {
                        ContactFieldDTO fieldDto = new ContactFieldDTO();
                        fieldDto.setType(new ContactFieldTypeDTO(new Integer(specificType[spField])));
                        fieldDto.setContent(fields[specific[spField]].trim());
                        entitySpecificFeilds.put(
                                (specificType[spField] + ""),fieldDto);
                    }
                }
                contact.setFieldsTable(entitySpecificFeilds);
                
                Integer newUserId = remoteSession.create(user, contact);
                if (newUserId != null && notes >= 0) {
                    remoteSession.setCustomerNotes(newUserId, 
                            customer.getNotes().replaceAll("\n", "<br/>"));
                }
                
                if (cc != null) {
                    remoteSession.createCreditCard(newUserId, cc);
                    System.out.println("Credit card " + cc.getNumber() + 
                            " added to user " + newUserId);
                }
                
                
                if (newUserId == null) {
                    // then add the contact info
                    System.out.println("Exising user: " + user.getUserName());
                    remoteSession.addContact(contact, user.getUserName(), 
                            entityId);
                } else {
                    System.out.println("New user " + newUserId + " created");
                }
                
                if (processOrders.booleanValue() && newUserId != null) {
                    OrderDTO summary = new OrderDTO();
                    String ext = fields[period].trim();
                    System.out.print("[" + ext + "]");
                    Integer periodId = Integer.valueOf((String) prop.get(
                            "order_period_" + ext));
                    //summary.setPeriodId(periodId);
                    //summary.setUserId(newUserId);
                    if (active_since >= 0) {
                        summary.setActiveSince(Util.parseDate(
                                fields[active_since].trim()));
                    }
                    if (active_until >= 0) {
                        summary.setActiveUntil(Util.parseDate(
                                fields[active_until].trim()));
                    }
                    // this makes it prepaid (2 is pospaid)
                    //summary.setBillingTypeId(new Integer(1));
/*                    TODO: use the standard API
 *                  IOrderSessionBean remoteOrder = (IOrderSessionBean) 
                            RemoteContext.getBean(
                            RemoteContext.Name.ORDER_REMOTE_SESSION);
 */                   // add the item (quantity = 1)
                    IOrderSessionBean remoteOrder = null;
                    Integer itemId = Integer.valueOf((String) prop.getProperty(
                            "item_id"));
                    OrderDTO thisOrder = remoteOrder.addItem(itemId, Constants.BIGDECIMAL_ONE, summary, languageId,
                                                             newUserId, entityId);
                    
                    // to edit the total I need to get the line ..
                    OrderLineDTO thisLine = (OrderLineDTO) thisOrder.
                            getLine(itemId);
                    Float price = new Float(0);
                    if (fields[total] != null && fields[total].length() > 0) {
                        price = Float.valueOf(fields[total]);
                    }
                    thisLine.setPrice(new BigDecimal(price));
                    thisLine.setDescription(prop.getProperty("order_description"));
                    //System.out.println("desc = " + thisLine.getDescription());
                    thisOrder = remoteOrder.recalculate(thisOrder, entityId);
                    Integer newOrderId = remoteOrder.createUpdate(entityId, 
                            Integer.valueOf((String) prop.getProperty(
                                "creator_id")), thisOrder, languageId);
                    System.out.println("Order " + newOrderId + " created for" +
                            " user " + newUserId);
                    
                }
                
                record = readLine(file);
            }
            
            file.close();

            System.out.println("Total users uploaded: " + totalRows);
            
            /*
             *  now process the items if present
             */
            //  open the file
            fileName = prop.getProperty("fileTypes");
            if (fileName == null) {
                System.out.println("No items files specified");
                return; // no items to process
            }
            System.out.println("Now loading types " + fileName);
            file = new BufferedReader(new FileReader(fileName));
            // get the remote interfaces TODO use the standard API
/*            IItemSessionBean itemSession = (IItemSessionBean) 
                    RemoteContext.getBean(
                    RemoteContext.Name.ITEM_REMOTE_SESSION);
*/            
            IItemSessionBean itemSession = null;
            header = file.readLine();
            // the types file has only one field with the description
            Integer newType;
            Hashtable types = new Hashtable();
            totalRows = 0;
            record = readLine(file);
            while (record != null) {
                ItemTypeDTO type = new ItemTypeDTO();
                type.setDescription(record);
                type.setEntity(new CompanyDTO(entityId));
                newType = itemSession.createType(type);
                types.put(type.getDescription(), newType);
                totalRows++;
                record = readLine(file);
            }
            System.out.println("Created " + totalRows + " item categories");
            
            // proceed with the items
            fileName = prop.getProperty("fileItems");
            System.out.println("Now loading items" + fileName);
            file = new BufferedReader(new FileReader(fileName));
            
            header = file.readLine();
            Integer currencyId = Integer.valueOf(prop.getProperty(
                    "items.currency"));
            // this file has the fields fixed, no moving the columns around
            record = readLine(file);
            while (record != null) {
                String fields[] = record.split("\t");
                ItemDTO item = new ItemDTO();
                item.setEntity(new CompanyDTO(entityId));
                item.setDeleted(new Integer(0));
                item.setNumber(fields[0]);
                if (fields[1].trim().length() > 0) {
                    item.setPercentage(new BigDecimal(Float.valueOf(fields[1])));
                    item.setCurrencyId(currencyId);
                } else {
                    item.setPercentage(null);
                    item.setCurrencyId(null);
                }

                if (item.getPercentage() == null) {
                    item.setPrice(new BigDecimal(Float.valueOf(fields[3])));
                } else {
                    item.setPrice(null);
                }
                item.setDescription(fields[4]);
                Integer type[] = new Integer[1];
                type[0] = (Integer) types.get(fields[5]);
                if (type[0] == null) {
                    System.out.println("Missing item category " + fields[5]);
                    return;
                }
                item.setTypes(type);
                
                itemSession.create(item, languageId);
                totalRows++;
                record = readLine(file);
            }
            
            System.out.println("Created " + totalRows + " items");

        } catch (Exception e) {
            System.err.println("Exception on record " + record + " : " 
                    + e.getMessage());      
            e.printStackTrace();
        } 
    }
    
    static String readLine(BufferedReader file) 
            throws IOException {
        StringBuffer retValue = new StringBuffer();
        
        int aByte = file.read();
        boolean inString = false;
        while (aByte != -1) {
            if (aByte == '"') {
                inString = !inString;
            } else {
                if (!inString && aByte == '\n') {
                    break;
                } 
                retValue.append((char)aByte);
            }
            aByte = file.read();
        }
        
        //System.out.println("Read [" + retValue + "]");
        return retValue.length() > 0 ? retValue.toString() : null;
    }
}
