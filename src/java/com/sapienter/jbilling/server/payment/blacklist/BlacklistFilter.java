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
package com.sapienter.jbilling.server.payment.blacklist;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;

/**
 * Blacklist filter interface.
 */
public interface BlacklistFilter {
    /**
     * Checks if a payment is blacklisted
     */
    public Result checkPayment(PaymentDTOEx paymentInfo);

    /**
     * Checks if a user is blacklisted
     */
    public Result checkUser(Integer userId);

    /**
     * Returns the filter name to place on the authorization record for
     * blacklisted payments/users.
     */
    public String getName();

    /**
     * Used to return the result of blacklisted payments/users
     */
    static final class Result {
        private final boolean isBlacklisted;
        private final String message;

        public Result(boolean isBlacklisted, String message) {
            this.isBlacklisted = isBlacklisted;
            this.message = message;
        }

        public boolean isBlacklisted() {
            return isBlacklisted;
        }

        public String getMessage() {
            return message;
        }
    }
}
