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

package com.sapienter.jbilling.server.payment.db;

import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author abimael
 *
 */
public class PaymentInvoiceMapDAS extends AbstractDAS<PaymentInvoiceMapDTO> {

    public PaymentInvoiceMapDTO create(InvoiceDTO invoice, PaymentDTO payment, BigDecimal realAmount) {
        PaymentInvoiceMapDTO map = new PaymentInvoiceMapDTO();
        map.setInvoiceEntity(invoice);
        map.setPayment(payment);
        map.setAmount(realAmount);
        map.setCreateDatetime(Calendar.getInstance().getTime());

        return save(map);
    }

    public void deleteAllWithInvoice(InvoiceDTO invoice) {
        InvoiceDTO inv = new InvoiceDAS().find(invoice.getId());
        Criteria criteria = getSession().createCriteria(PaymentInvoiceMapDTO.class);
        criteria.add(Restrictions.eq("invoiceEntity", inv));

        List<PaymentInvoiceMapDTO> results = criteria.list();

        if (results != null && !results.isEmpty()) {
            for (PaymentInvoiceMapDTO paym : results) {
                delete(paym);
            }
        }
    }
}
