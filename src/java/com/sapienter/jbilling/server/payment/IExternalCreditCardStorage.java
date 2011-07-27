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
package com.sapienter.jbilling.server.payment;

import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;

public interface IExternalCreditCardStorage {

    /**
     * Store the given credit card using the payment gateways storage mechanism.
     *
     * This method should return null for storage failures, so that the
     * {@link com.sapienter.jbilling.server.payment.tasks.SaveCreditCardExternallyTask }
     * can perform failure handling.
     *
     * If an obscured and stored credit card is encountered, this method should still return a
     * gateway key for the card and not a null value. It is up to the implementation
     * to decide whether or not to re-store the card or to leave it as-is.
     *
     * @param contact ContactDTO from NewContactEvent, may be null if triggered by NewCreditCardEvent
     * @param creditCard credit card to store, may be null if triggered by NewContactEvent without credit card.
     * @param ach ach to store
     * @return gateway key of stored credit card, null if storage failed
     */
    public String storeCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach);
    
    /**
     * Delete the existing credit card details or the Ach payment details.
     * 
     * This method should return null for storage failures, so that the
     * {@link com.sapienter.jbilling.server.payment.tasks.SaveCreditCardExternallyTask }
     * can perform failure handling.
     *
     * @param contact contact to process
     * @param creditCard credit card to process
     * @param ach ach to process
     * @return resulting unique gateway key for the credit card/contact
     */
    public String deleteCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach);
}
