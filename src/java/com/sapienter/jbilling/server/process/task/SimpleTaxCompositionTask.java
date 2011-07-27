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

package com.sapienter.jbilling.server.process.task;

import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.item.ItemBL;
import com.sapienter.jbilling.server.item.db.ItemDAS;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.pluggableTask.InvoiceCompositionTask;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
import com.sapienter.jbilling.server.util.Constants;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Set;

/**
 * This plug-in calculates taxes for invoice.
 *
 * Plug-in parameters:
 *
 *      tax_item_id                 (required) The item that will be added to an invoice with the taxes
 *
 *      custom_contact_field_id     (optional) The id of CCF that if its value is 'true' or 'yes' for a customer,
 *                                  then the customer is considered exempt. Exempt customers do not get the tax
 *                                  added to their invoices.
 *      item_exempt_category_id     (optional) The id of an item category that, if the item belongs to, it is
 *                                  exempt from taxes
 *
 * @author Alexander Aksenov
 * @since 30.04.11
 */
public class SimpleTaxCompositionTask extends PluggableTask
        implements InvoiceCompositionTask {

    private static final Logger LOG = Logger.getLogger(SimpleTaxCompositionTask.class);

    // plug-in parameters
    // mandatory parameters
    protected final static String PARAM_TAX_ITEM_ID = "tax_item_id";
    // optional, may be empty
    protected final static String PARAM_CUSTOM_CONTACT_FIELD_ID = "custom_contact_field_id";
    protected final static String PARAM_ITEM_EXEMPT_CATEGORY_ID = "item_exempt_category_id";

    @Override
    public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException {
        ItemDTO taxItem;
        Integer itemExemptCategoryId = null;
        Integer customContactFieldId = null;
        try {
            String paramValue = getParameter(PARAM_TAX_ITEM_ID, "");
            if (paramValue == null || "".equals(paramValue.trim())) {
                throw new TaskException("Tax item id is not defined!");
            }
            taxItem = new ItemDAS().find(new Integer(paramValue));
            if (taxItem == null) {
                throw new TaskException("Tax item not found!");
            }
            paramValue = getParameter(PARAM_ITEM_EXEMPT_CATEGORY_ID, "");
            if (paramValue != null && !"".equals(paramValue.trim())) {
                itemExemptCategoryId = new Integer(paramValue);
            }
            paramValue = getParameter(PARAM_CUSTOM_CONTACT_FIELD_ID, "");
            if (paramValue != null && !"".equals(paramValue.trim())) {
                customContactFieldId = new Integer(paramValue);
            }
        } catch (NumberFormatException e) {
            LOG.error("Incorrect plugin configuration", e);
            throw new TaskException(e);
        }

        if (!isTaxCalculationNeeded(userId, customContactFieldId)) {
            return;
        }

        if (taxItem.getPercentage() != null) {
            // tax calculation as percentage of full cost
            //calculate total to include result lines 
            invoice.calculateTotal();
            BigDecimal invoiceAmountSum = invoice.getTotal();

            //remove carried balance from tax calculation 
            //to avoid double taxation
            LOG.debug("Percentage Price. Carried balance is " + invoice.getCarriedBalance());
            if ( null != invoice.getCarriedBalance() ){
                invoiceAmountSum = invoiceAmountSum.subtract(invoice.getCarriedBalance());
            }
            
            LOG.debug("Exempt Category " + itemExemptCategoryId);
            if (itemExemptCategoryId != null) {
                // find exemp items and subtract price
                for (int i = 0; i < invoice.getResultLines().size(); i++) {
                    InvoiceLineDTO invoiceLine = (InvoiceLineDTO) invoice.getResultLines().get(i);
                    ItemDTO item = invoiceLine.getItem();
                    
                    if (item != null) {
                        Set<ItemTypeDTO> itemTypes = new ItemDAS().find(item.getId()).getItemTypes();
                        for (ItemTypeDTO itemType : itemTypes) {
                            if (itemType.getId() == itemExemptCategoryId) {
                                LOG.debug("Item " + item.getDescription() + " is Exempt. Category " + itemType.getId());
                                invoiceAmountSum = invoiceAmountSum.subtract(invoiceLine.getAmount());
                                break;
                            }
                        }
                    }
                }
            }

            LOG.debug("Calculating tax on = " + invoiceAmountSum);
            
            BigDecimal taxValue = invoiceAmountSum.multiply(taxItem.getPercentage()).divide(
                    BigDecimal.valueOf(100L), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND
            );
            //if (taxValue.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null, "Tax line for percentage tax item " + taxItem.getId(),
                    taxValue, taxValue, BigDecimal.ONE, Constants.INVOICE_LINE_TYPE_TAX, 0,
                    taxItem.getId(), userId, null);
            invoice.addResultLine(invoiceLine);
            //}
        } else {
            LOG.debug("Flat Price."); 
            ItemBL itemBL = new ItemBL(taxItem);
            BigDecimal price = itemBL.getPriceByCurrency(taxItem, userId, invoice.getCurrency().getId());

            if (price != null && price.compareTo(BigDecimal.ZERO) != 0) {
                // tax as additional invoiceLine
                InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null, "Tax line with flat price for tax item " + taxItem.getId(),
                        price, price, BigDecimal.ONE, Constants.INVOICE_LINE_TYPE_TAX, 0,
                        taxItem.getId(), userId, null);
                invoice.addResultLine(invoiceLine);
            }
        }
    }

    private boolean isTaxCalculationNeeded(Integer userId, Integer customContactFieldId) {
        if (customContactFieldId == null) {
            return true;
        }
        ContactDTO contactDto = new ContactDAS().findPrimaryContact(userId);
        if (contactDto == null) {
            return true;
        }

        for (ContactFieldDTO contactField : contactDto.getFields()) {
            if (contactField.getType().getId() == customContactFieldId) {
                String value = contactField.getContent();
                if ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
        throw new UnsupportedOperationException("Can't call this method");
    }
}
