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

package com.sapienter.jbilling.server.order.task;

import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.tasks.BasicItemManager;
import com.sapienter.jbilling.server.item.tasks.IItemPurchaseManager;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.rule.RulesBaseTask;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.util.DTOFactory;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This plug-in does item management rules that are compatible with the
 * mediation process.
 * This means that it is not called by the mediation process.
 * It does call the basic item manager, which in turn runs pricing rules.
 * @author emilc
 */
public class RulesItemManager2 extends RulesBaseTask implements IItemPurchaseManager {

    protected Logger getLog() {
        return Logger.getLogger(RulesItemManager2.class);
    }

    public void addItem(Integer itemID, BigDecimal quantity, Integer language,
            Integer userId, Integer entityId, Integer currencyId,
            OrderDTO order, List<Record> records) throws TaskException {
        // start by calling the standard plug-in
        BasicItemManager manager = new BasicItemManager();
        manager.addItem(itemID, quantity, language, userId, entityId, currencyId, order, records);

        processRules(order, userId);
    }

    protected void processRules(OrderDTO order, Integer userId) throws TaskException {

        rulesMemoryContext.add(order);

        // add OrderDTO to rules memory context
        order.setCurrency(new CurrencyDAS().find(order.getCurrency().getId()));
        if (order.getCreateDate() == null) {
            order.setCreateDate(new Date());
        }

        // needed for calls to 'rateOrder'
        if (order.getPricingFields() != null) {
            for(PricingField field: order.getPricingFields()) {
                rulesMemoryContext.add(field);
            }
        }

        try {
            UserDTOEx user = DTOFactory.getUserDTOEx(userId);
            rulesMemoryContext.add(user);
            ContactBL contact = new ContactBL();
            contact.set(userId);
            ContactDTOEx contactDTO = contact.getDTO();
            rulesMemoryContext.add(contactDTO);
            for (ContactFieldDTO field : (Collection<ContactFieldDTO>) contactDTO.getFieldsTable().values()) {
                rulesMemoryContext.add(field);
            }
        } catch (Exception e) {
            throw new TaskException(e);
        }

        executeRules();
    }
}
