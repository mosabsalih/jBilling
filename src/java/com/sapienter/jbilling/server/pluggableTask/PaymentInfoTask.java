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

import com.sapienter.jbilling.server.payment.PaymentDTOEx;

/*
 * This task gathers the information necessary to process a payment.
 * Since each customer and entity can have different payment methods
 * this is better placed in a pluggable task.
 * The result of the process call is the payment dto with all the info
 * to later send the payment to the live processor. The methos of the
 * payment has to be also set
 */
public interface PaymentInfoTask {
    
    PaymentDTOEx getPaymentInfo(Integer userId) throws TaskException;
    
}
