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
package com.sapienter.jbilling.server.mediation.db;

import java.util.ArrayList;
import java.util.List;

import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import org.hibernate.Query;

import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class MediationRecordLineDAS extends AbstractDAS<MediationRecordLineDTO> {
    
    private static final String findByOrder =
            " select a " +
            "   from MediationRecordLineDTO a " +
            "  where a.orderLine.purchaseOrder.id = :orderId " +
            "    and a.orderLine.deleted = 0 " +
            "  order by a.orderLine.id, a.id";
    
    public List<MediationRecordLineDTO> getByOrder(Integer orderId) {
        Query query = getSession().createQuery(findByOrder);
        query.setParameter("orderId", orderId);
        return query.list();
    }

    private static final String FIND_BY_INVOICE_HQL =
        "select recordLine " +
            "from MediationRecordLineDTO recordLine " +
            "    inner join recordLine.orderLine.purchaseOrder as purchaseOrder " +
            "    inner join purchaseOrder.orderProcesses orderProcess " +
            "where orderProcess.invoice.id = :invoiceId";

    /**
     * Find all MediationRecordLineDTO events incorporated into the given
     * invoice.
     *
     * @param invoiceId invoice id
     * @return list of mediation events, empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<MediationRecordLineDTO> findByInvoice(Integer invoiceId) {
        Query query = getSession().createQuery(FIND_BY_INVOICE_HQL);
        query.setParameter("invoiceId", invoiceId);
        return query.list();
    }

}
