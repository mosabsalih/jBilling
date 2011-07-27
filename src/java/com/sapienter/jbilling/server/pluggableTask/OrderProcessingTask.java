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

import com.sapienter.jbilling.server.order.db.OrderDTO;

/**
 * Defines the methods for those tasks that will be configured in the
 * order_processing_task table, allowing each entity to have its own
 * sets of processing rules for orders.
 * 
 * @author emilc
 *
 */


public interface OrderProcessingTask {
    public void doProcessing(OrderDTO order) throws TaskException;
}
