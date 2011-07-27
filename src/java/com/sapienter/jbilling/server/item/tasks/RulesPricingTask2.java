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
package com.sapienter.jbilling.server.item.tasks;

import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.rule.RulesBaseTask;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.util.DTOFactory;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author emilc
 */
public class RulesPricingTask2 extends RulesBaseTask implements IPricing {

    protected Logger getLog() {
        return Logger.getLogger(RulesPricingTask2.class);
    }
    
    public BigDecimal getPrice(Integer itemId, BigDecimal quantity, Integer userId, Integer currencyId,
            List<PricingField> fields, BigDecimal defaultPrice, OrderDTO pricingOrder)
            throws TaskException {

        // the result goes in the memory context
        PricingResult result = new PricingResult(itemId, quantity, userId, currencyId);
        rulesMemoryContext.add(result);

        if (fields != null && !fields.isEmpty()) {
            // bind the pricing fields to this result
            result.setPricingFieldsResultId(result.getId());
            for (PricingField field : fields) {
                field.setResultId(result.getId());
            }
            rulesMemoryContext.addAll(fields);
        }

        try {
            if (userId != null) {
                UserDTOEx user = DTOFactory.getUserDTOEx(userId);
                rulesMemoryContext.add(user);
                ContactBL contact = new ContactBL();
                contact.set(userId);
                ContactDTOEx contactDTO = contact.getDTO();
                rulesMemoryContext.add(contactDTO);
                for (ContactFieldDTO field: (Collection<ContactFieldDTO>) contactDTO.getFieldsTable().values()) {
                    rulesMemoryContext.add(field);
                }
            }
        } catch (Exception e) {
            throw new TaskException(e);
        }

        executeRules();

        // the rules might not have any price for this. Use the default then.
        if (result.getPrice() == null) {
            result.setPrice(defaultPrice); // set the default
        }
        return result.getPrice();
    }
}
