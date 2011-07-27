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
 * Created on Jan 15, 2005
 *
 */
package com.sapienter.jbilling.server.user;

/**
 * @author Emil
 *
 */
public interface CreditCardSQL {
    static final String expiring =
        "select bu.id, cc.id " +
        " from base_user bu, credit_card cc, user_credit_card_map uccm " +
        "where bu.deleted = 0 " +
        "  and bu.status_id < " + UserDTOEx.STATUS_SUSPENDED +
        "  and cc.deleted = 0 " +
        "  and bu.id = uccm.user_id " +
        "  and cc.id = uccm.credit_card_id " +
        "  and cc.cc_expiry <= ?";
}
