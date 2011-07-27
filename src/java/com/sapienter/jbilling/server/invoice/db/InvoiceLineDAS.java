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

package com.sapienter.jbilling.server.invoice.db;

import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

import java.math.BigDecimal;

/**
 * 
 * @author abimael
 * 
 */
public class InvoiceLineDAS extends AbstractDAS<InvoiceLineDTO> {

    public InvoiceLineDTO create(String description, BigDecimal amount,
            BigDecimal quantity, BigDecimal price, Integer typeId, ItemDTO itemId,
            Integer sourceUserId, Integer isPercentage) {

        InvoiceLineDTO newEntity = new InvoiceLineDTO();
        newEntity.setDescription(description);
        newEntity.setAmount(amount);
        newEntity.setQuantity(quantity);
        newEntity.setPrice(price);
        newEntity.setInvoiceLineType(new InvoiceLineTypeDAS().find(typeId));
        newEntity.setItem(itemId);
        newEntity.setSourceUserId(sourceUserId);
        newEntity.setIsPercentage(isPercentage);
        newEntity.setDeleted(0);
        return save(newEntity);
    }

}
