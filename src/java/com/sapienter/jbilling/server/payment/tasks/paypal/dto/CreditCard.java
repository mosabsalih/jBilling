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
package com.sapienter.jbilling.server.payment.tasks.paypal.dto;

/**
 * Created by Roman Liberov
 */

public class CreditCard {
    private final String type;
    private final String account;
    private final String expirationDate;
    private final String cvv2;

    public CreditCard(String type, String account, String expirationDate, String cvv2) {
        this.type = type;
        this.account = account;
        this.expirationDate = expirationDate;
        this.cvv2 = cvv2;
    }

    public String getType() {
        return type;
    }

    public String getAccount() {
        return account;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getCvv2() {
        return cvv2;
    }
}
