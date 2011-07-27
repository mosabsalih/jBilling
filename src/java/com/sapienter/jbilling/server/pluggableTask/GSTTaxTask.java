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

package com.sapienter.jbilling.server.pluggableTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.util.Constants;

/**
 * Basic tasks that takes the quantity and multiplies it by the price to 
 * get the lines total. It also updates the order total with the addition
 * of all line totals
 * 
 * @author emilc
 *
 */
public class GSTTaxTask extends PluggableTask implements OrderProcessingTask {

    // pluggable task parameters names
    public static final ParameterDescription PARAMETER_RATE = 
        new ParameterDescription("rate", true, ParameterDescription.Type.STR);
    public static final ParameterDescription PARAMETER_DESCRIPTION = 
        new ParameterDescription("description", true, ParameterDescription.Type.STR);
    
    //initializer for pluggable params
    { 
    	descriptions.add(PARAMETER_RATE);
        descriptions.add(PARAMETER_DESCRIPTION);
    }

    public void doProcessing(OrderDTO order) throws TaskException {
        BigDecimal orderTotal = order.getTotal();
        BigDecimal taxRate = new BigDecimal(parameters.get(PARAMETER_RATE.getName()).toString());
        BigDecimal gstTax = orderTotal.divide(new BigDecimal("100"), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND).multiply(taxRate);
        
        OrderLineDTO taxLine = new OrderLineDTO();
        taxLine.setAmount(gstTax);
        taxLine.setDeleted(new Integer(0));
        taxLine.setDescription((String) parameters.get(PARAMETER_DESCRIPTION.getName()));
        taxLine.setTypeId(Constants.ORDER_LINE_TYPE_TAX);
        ItemDTO item = new ItemDTO();
        item.setId(0);
        taxLine.setItem(item);

        try {
            taxLine.setEditable(OrderBL.lookUpEditable(Constants.ORDER_LINE_TYPE_TAX));
        } catch (SessionInternalError e) {
            throw new TaskException("Error in GSTTaxTask. Bad order_line_type");
        }
        order.getLines().add(taxLine);
        order.setTotal(orderTotal.add(gstTax));
    }

}
