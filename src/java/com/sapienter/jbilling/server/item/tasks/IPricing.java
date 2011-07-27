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

import java.util.List;

import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import java.math.BigDecimal;

public interface IPricing {

    /**
     * Get the price for the given item, user, and quantity being purchased. Pricing fields can be
     * provided to define specific pricing scenarios to be handled by the implementing class.
     *
     * @param itemId item id being purchased
     * @param quantity quantity being purchased 
     * @param userId user purchasing the item
     * @param currencyId currency of user
     * @param fields pricing fields
     * @param defaultPrice default price if no other price could be determined.
     * @param pricingOrder target order for this pricing request (may be null)
     * @return price
     * @throws TaskException checked exception if a problem occurs.
     */
    public BigDecimal getPrice(Integer itemId, BigDecimal quantity, Integer userId, Integer currencyId,
                               List<PricingField> fields, BigDecimal defaultPrice, OrderDTO pricingOrder)
            throws TaskException;
}
