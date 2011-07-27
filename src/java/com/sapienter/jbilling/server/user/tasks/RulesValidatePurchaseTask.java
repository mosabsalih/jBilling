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

package com.sapienter.jbilling.server.user.tasks;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;

import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.item.tasks.Subscription;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import java.util.ArrayList;

/**
 * Pluggable task allows running rules for validatePurchase API method.
 */
public class RulesValidatePurchaseTask extends PluggableTask 
        implements IValidatePurchaseTask {

    public ValidatePurchaseWS validate(CustomerDTO customer, 
            List<ItemDTO> items, List<BigDecimal> amounts, 
            ValidatePurchaseWS result, List<List<PricingField>> fields) 
            throws TaskException {

        if (!result.getAuthorized()) {
            return result;
        }

        BigDecimal amount = BigDecimal.ZERO;
        for (BigDecimal a : amounts) {
            amount = amount.add(a);
        }

        Integer userId = customer.getBaseUser().getId();

        KnowledgeBase knowledgeBase;
        try {
            knowledgeBase = readKnowledgeBase();
        } catch (Exception e) {
            throw new TaskException(e);
        }
        StatelessKnowledgeSession mySession = knowledgeBase.newStatelessKnowledgeSession();
        List<Object> rulesMemoryContext = new ArrayList<Object>();

        // add any pricing fields
        if (fields != null && !fields.isEmpty()) {
            for (List<PricingField> pricingFields : fields) {
                rulesMemoryContext.addAll(pricingFields);
            }
        }

        // add the data
        rulesMemoryContext.add(customer);
        rulesMemoryContext.add(customer.getBaseUser());
        rulesMemoryContext.add(result);
        for (ItemDTO item : items) {
            rulesMemoryContext.add(item);
        }

        // add user contact info
        ContactBL contact = new ContactBL();
        contact.set(userId);
        ContactDTOEx contactDTO = contact.getDTO();
        rulesMemoryContext.add(contactDTO);
        for (ContactFieldDTO field : (Collection<ContactFieldDTO>) 
                 contactDTO.getFieldsTable().values()) {
            rulesMemoryContext.add(field);    
        }

        // add the subscriptions
        OrderBL order = new OrderBL();
        for (OrderDTO myOrder : order.getActiveRecurringByUser(userId)) {
            for (OrderLineDTO myLine : myOrder.getLines()) {
                rulesMemoryContext.add(new Subscription(myLine));
            }
        }

        // add the current order
        OrderDTO currentOrder = order.getCurrentOrder(userId, new Date());
        if (currentOrder != null) {
            rulesMemoryContext.add(currentOrder);
            for (OrderLineDTO line : currentOrder.getLines()) {
                rulesMemoryContext.add(line);
            }
        }

        // add the helper
        ValidatePurchase helper = new ValidatePurchase(amount);
        mySession.setGlobal("validatePurchase", helper);

        // execute the rules
        mySession.execute(rulesMemoryContext);

        // add any messages
        List<String> messages = helper.getMessages();
        if (messages.size() > 0) {
            String[] originalArray = result.getMessage();
            if (originalArray == null) {
                result.setMessage(messages.toArray(new String[0]));
            } else {
                String[] newArray = Arrays.copyOf(originalArray, 
                        originalArray.length + messages.size());
                int i = originalArray.length;
                for (String s : messages) {
                    newArray[i] = s;
                    i++;
                }
                result.setMessage(newArray);
            }
        }

        return result;
    }

    public static class ValidatePurchase {
        private BigDecimal amount;
        private List<String> messages;

        public ValidatePurchase(BigDecimal amount) {
            this.amount = amount;
            messages = new LinkedList<String>();
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void addMessage(String message) {
            messages.add(message);
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
