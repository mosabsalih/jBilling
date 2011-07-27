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

import java.math.BigDecimal;
import java.util.List;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.server.item.ItemBL;
import com.sapienter.jbilling.server.item.ItemDecimalsException;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import java.util.ArrayList;

public class BasicItemManager extends PluggableTask implements IItemPurchaseManager {

    protected ItemDTO item = null;
    private OrderLineDTO latestLine = null;
    
    public void addItem(Integer itemID, Integer quantity, Integer language,
            Integer userId, Integer entityId, Integer currencyId,
            OrderDTO newOrder, List<Record> records) throws TaskException {
        
        addItem(itemID, new BigDecimal(quantity), language, userId, entityId, currencyId, newOrder, records);
    }
    
    public void addItem(Integer itemID, BigDecimal quantity, Integer language,
            Integer userId, Integer entityId, Integer currencyId,
            OrderDTO newOrder, List<Record> records) throws TaskException {

        // Validate decimal quantity with the item
        if (quantity.remainder(Constants.BIGDECIMAL_ONE).compareTo(BigDecimal.ZERO) > 0) {        
            try {
                ItemBL bl = new ItemBL();
                bl.set(itemID);
                if( bl.getEntity().getHasDecimals().intValue() == 0 ) {
                    latestLine = null;
                    throw new ItemDecimalsException( "Item does not allow Decimals" );
                }
            } catch( Exception e ) {
                throw new TaskException(e);
            }
        }
        
        // check if the item is already in the order
        OrderLineDTO line = (OrderLineDTO) newOrder.getLine(itemID);

        OrderLineDTO myLine = new OrderLineDTO();
        myLine.setItem(new ItemDTO(itemID));
        myLine.setQuantity(quantity);
        populateOrderLine(language, userId, entityId, currencyId, myLine, records);
        myLine.setDefaults();

        if (line == null) { // not yet there
            newOrder.getLines().add(myLine);
            myLine.setPurchaseOrder(newOrder);
            latestLine = myLine;
        } else {
            // the item is there, I just have to update the quantity
            BigDecimal dec = line.getQuantity().add(quantity);
            line.setQuantity(dec);
            
            // and also the total amount for this order line
            dec = line.getAmount().add(myLine.getAmount());
            line.setAmount(dec);
            latestLine = line;
        }
    }
    
    /**
     * line can not be null, nor line.getItemId. All the rest will be populated
     * @param language
     * @param userId
     * @param entityId
     * @param currencyId
     * @param line
     */
    public void populateOrderLine(Integer language, Integer userId, Integer entityId, Integer currencyId,
                                  OrderLineDTO line, List<Record> records) {

        ItemBL itemBL = new ItemBL(line.getItemId());
        if (records != null) {
            List<PricingField> fields = new ArrayList<PricingField>();
            for (Record record : records) {
                fields.addAll(record.getFields());
            }
            itemBL.setPricingFields(fields);
        }

        // get ItemDTO with price populated for the quantity being purchased
        item = itemBL.getDTO(language, userId, entityId, currencyId, line.getQuantity());

        Boolean editable = OrderBL.lookUpEditable(item.getOrderLineTypeId());

        if (line.getDescription() == null) {
            line.setDescription(item.getDescription());
        }
        if (line.getQuantity() == null) {
            line.setQuantity(Constants.BIGDECIMAL_ONE);
        }
        if (line.getPrice() == null) {
            line.setPrice((item.getPercentage() == null) ? item.getPrice() :
                item.getPercentage());
        }
        if (line.getAmount() == null) {
            BigDecimal additionAmount = null;
            // normal price, multiply by quantity
            if (item.getPercentage() == null) {
                additionAmount = line.getPrice();
                additionAmount = additionAmount.multiply(line.getQuantity());
            } else {
                // percentage ignores the quantity
                additionAmount = item.getPercentage();
            }
            line.setAmount(additionAmount.setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
        }
        line.setCreateDatetime(null);
        line.setDeleted(0);
        line.setTypeId(item.getOrderLineTypeId());
        line.setEditable(editable);
        line.setItem(item);
    }

    public OrderLineDTO getLatestLine() {
        return latestLine;
    }
}
