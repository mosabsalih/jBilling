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
 * Created on May 11, 2005
 *
 */
package com.sapienter.jbilling.server.user;

import java.util.Comparator;

import com.sapienter.jbilling.server.user.partner.db.PartnerRange;

/**
 * @author Emil
 *
 */
public class PartnerRangeComparator implements Comparator {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1) {
        PartnerRange perA = (PartnerRange) arg0;
        PartnerRange perB = (PartnerRange) arg1;
        
        return new Integer(perA.getRangeFrom()).compareTo(perB.getRangeFrom());
    }

}
