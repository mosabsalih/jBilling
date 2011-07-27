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


package com.sapienter.jbilling.server.invoice;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.system.event.Event;

/**
 *
 * @author emilc
 */
public class NewInvoiceEvent implements Event {

    private final InvoiceDTO invoice;
    private final Integer entityId;
    private final Integer userId;

    public NewInvoiceEvent(InvoiceDTO invoice) {
        entityId = invoice.getBaseUser().getEntity().getId();
        userId = invoice.getBaseUser().getId();
        this.invoice = invoice;
    }

    public String getName() {
        return "New invoice event";
    }

    public Integer getEntityId() {
        return entityId;
    }

    /**
     * Warning, the invoice returned is in the hibernate session.
     * Any changes will be reflected in the database.
     * @return
     */
    public InvoiceDTO getInvoice() {
        return invoice;
    }

    public Integer getUserId() {
        return userId;
    }

}
