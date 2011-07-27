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

import com.sapienter.jbilling.server.payment.IExternalCreditCardStorage;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;

/**
 * A fake IExternalCreditCardStorage task for use with the SaveCreditCardExternallyTask. This plugin
 * will return a crafted "gateway key" for storage that will be used to replace the users existing
 * credit card details.
 *
 * The gateway key represents a vendor specific unique identifier for use when processing subsequent
 * payments for the same user - elmiminating the need to send the credit card in its entirety.
 */
public class PaymentFakeExternalStorage extends PaymentFakeTask implements IExternalCreditCardStorage {

    private static final String PARAM_RETURN_NULL = "return_null"; // return null if true
    private static final String PARAM_RETURN_VALUE = "return_value"; // explicit return value for testing
    
    private static final String DELETE_RETURN_NULL = "delete_return_null"; // to return null, set param value as true
    private static final String DELETE_RETURN_VALUE = "delete_return_value"; // explicit return value for testing

    public static final String DEFAULT_RETURN_VALUE = "stored externaly"; // note: typo maybe used in existing tests, leave as-is
    public static final String DEFAULT_DELETE_VALUE = "deleted externally";

    /**
     * Always returns "stored externaly" as a gateway key. Explicit keys can be set using the
     * "return_value" parameter for testing.
     *
     * @param contact contact to process
     * @param creditCard credit card to process
     * @param ach ach to process
     * @return resulting unique gateway key for the credit card/contact
     */
    public String storeCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        if (getParameter(PARAM_RETURN_NULL, false))
            return null;
        
        return getParameter(PARAM_RETURN_VALUE, DEFAULT_RETURN_VALUE);
    }

    /**
     * Always returns "deleted externally" as a gateway key. Explicit keys can be set using the
     * "delete_return_value" parameter for testing.
     *
     * @param contact contact to process
     * @param creditCard credit card to process
     * @param ach ach to process
     * @return resulting unique gateway key for the credit card/contact
     */
    public String deleteCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        if (getParameter(PARAM_RETURN_NULL, false))
            return null;
        
        return getParameter(PARAM_RETURN_VALUE, DEFAULT_DELETE_VALUE);
    }
}
