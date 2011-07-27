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
 * Created on Nov 22, 2004
 *
 */
package com.sapienter.jbilling.server.payment;

import java.util.Comparator;

import com.sapienter.jbilling.server.payment.db.PaymentDTO;

/**
 * @author Emil
 */
public class PaymentEntityComparator implements Comparator<PaymentDTO> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(PaymentDTO o1, PaymentDTO o2) {
        return new Integer(o1.getId()).compareTo(new Integer(o2.getId()));
        
    }

}
