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

import java.sql.SQLException;
import java.util.Date;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import java.util.Set;

/**
 *
 * This is the session facade for the invoices in general. It is a statless
 * bean that provides services not directly linked to a particular operation
 *
 * @author emilc
 **/
public interface IInvoiceSessionBean {

    public InvoiceDTO getInvoice(Integer invoiceId) throws SessionInternalError;

    public void create(Integer entityId, Integer userId,
            NewInvoiceDTO newInvoice) throws SessionInternalError;

    public String getFileName(Integer invoiceId) throws SessionInternalError;

    /**
     * The transaction requirements of this are not big. The 'atom' is 
     * just a single invoice. If the next one fails, it's ok that the
     * previous ones got updated. In fact, they should, since the email
     * has been sent.
     */
    public void sendReminders(Date today) throws SessionInternalError;

    public InvoiceDTO getInvoiceEx(Integer invoiceId, Integer languageId);

    public byte[] getPDFInvoice(Integer invoiceId) throws SessionInternalError;

    public void delete(Integer invoiceId, Integer executorId)
            throws SessionInternalError;

    /**
     * The real path is known only to the web server
     * It should have the token _FILE_NAME_ to be replaced by the generated file
     */
    public String generatePDFFile(java.util.Map map, String realPath) 
            throws SessionInternalError;

    // only for unit tests
    public Set<InvoiceDTO> getAllInvoices(Integer userId);
}    
