
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.process.PeriodOfTime;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.PreferenceBL;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This task will copy all the lines on the orders and invoices
 * to the new invoice, considering the periods involved for each
 * order, but not the fractions of perios. It will not copy the
 * lines that are taxes.
 * The quantity and total of each line will be multiplied by the
 * amount of periods.
 * @author Emil Created on 27-Apr-2003
 */
public class BasicCompositionTask extends PluggableTask implements InvoiceCompositionTask {

    private static final Logger LOG = Logger.getLogger(BasicCompositionTask.class);

    private Locale locale = null;

    public void apply(NewInvoiceDTO invoiceDTO, Integer userId) throws TaskException {

        /*
            Process each order being included in this invoice
         */
        for (int orderIndex = 0; orderIndex < invoiceDTO.getOrders().size(); orderIndex++) {

            OrderDTO order = (OrderDTO) invoiceDTO.getOrders().get(orderIndex);
            BigDecimal orderContribution = BigDecimal.ZERO;

            // add customer notes
            if (Integer.valueOf(1).equals(order.getNotesInInvoice()) && !StringUtils.isBlank(order.getNotes())) {
                if (invoiceDTO.getCustomerNotes() == null) {
                    invoiceDTO.setCustomerNotes(order.getNotes());
                } else {
                    invoiceDTO.setCustomerNotes(invoiceDTO.getCustomerNotes() + " " + order.getNotes());
                }
            }

            /*
                Add order lines - excluding taxes
             */
            for (OrderLineDTO orderLine : order.getLines()) {

                // skip deleted lines
                if (orderLine.getDeleted() == 1) {
                    continue;
                }

                for (PeriodOfTime period : invoiceDTO.getPeriods().get(orderIndex)) {
                    LOG.debug("Adding order line from " + order.getId()
                              + ", quantity " + orderLine.getQuantity()
                              + ", price " + orderLine.getPrice());

                    LOG.debug("Period: " + period);

                    InvoiceLineDTO invoiceLine = null;

                    if (orderLine.getOrderLineType().getId() == Constants.ORDER_LINE_TYPE_ITEM) {
                        // compose order line description
                        String desc = composeDescription(order, period, orderLine.getDescription());

                        // determine the invoice line type (one-time, recurring, line from sub-account)
                        Integer type;
                        if (userId.equals(order.getUser().getId())) {
                            if (Constants.ORDER_PERIOD_ONCE.equals(order.getPeriodId())) {
                                type = Constants.INVOICE_LINE_TYPE_ITEM_ONETIME;
                            } else {
                                type = Constants.INVOICE_LINE_TYPE_ITEM_RECURRING;
                            }
                        } else {
                            type = Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT;
                        }


                        BigDecimal periodAmount = calculatePeriodAmount(orderLine.getAmount(), period);
                        invoiceLine = new InvoiceLineDTO(null,
                                                         desc,
                                                         periodAmount,
                                                         orderLine.getPrice(),
                                                         orderLine.getQuantity(),
                                                         type,
                                                         0,
                                                         orderLine.getItemId(),
                                                         order.getUser().getId(),
                                                         null);

                        // link invoice line to the order that originally held the charge
                        invoiceLine.setOrderId(order.getId());
                        orderContribution = orderContribution.add(periodAmount);

                    } else if (orderLine.getOrderLineType().getId() == Constants.ORDER_LINE_TYPE_TAX) {
                        // tax items
                        int taxLineIndex = getTaxLineIndex(invoiceDTO.getResultLines(), orderLine.getDescription());
                        if (taxLineIndex >= 0) {
                            // tax already exists, add the total
                            invoiceLine = (InvoiceLineDTO) invoiceDTO.getResultLines().get(taxLineIndex);
                            BigDecimal periodAmount = calculatePeriodAmount(orderLine.getAmount(), period);
                            invoiceLine.setAmount(invoiceLine.getAmount().add(periodAmount));
                            orderContribution = orderContribution.add(periodAmount);

                        } else {
                            // tax has not yet been added, add a new invoice line
                            BigDecimal periodAmount = calculatePeriodAmount(orderLine.getAmount(), period);
                            invoiceLine = new InvoiceLineDTO(null,
                                                             orderLine.getDescription(),
                                                             periodAmount,
                                                             orderLine.getPrice(),
                                                             null,
                                                             Constants.INVOICE_LINE_TYPE_TAX,
                                                             0,
                                                             orderLine.getItemId(),
                                                             order.getUser().getId(),
                                                             null);

                            orderContribution = orderContribution.add(periodAmount);
                        }


                    } else if (orderLine.getOrderLineType().getId() == Constants.ORDER_LINE_TYPE_PENALTY) {
                        // penalty items
                        BigDecimal periodAmount = calculatePeriodAmount(orderLine.getAmount(), period);
                        invoiceLine = new InvoiceLineDTO(null,
                                                         orderLine.getDescription(),
                                                         periodAmount,
                                                         null,
                                                         null,
                                                         Constants.INVOICE_LINE_TYPE_PENALTY,
                                                         0,
                                                         orderLine.getItemId(),
                                                         order.getUser().getId(),
                                                         null);

                        orderContribution = orderContribution.add(periodAmount);
                    }

                    // for the invoice to make sense when it is displayed,
                    // each line has to be rounded to the decimals shown
                    invoiceDTO.addResultLine(invoiceLine);
                }
            }

            // save the order contribution
            saveOrderTotalContributionToInvoice(order.getId(), invoiceDTO, orderContribution);
        }


        /*
            add delegated invoices
         */
        for (InvoiceDTO invoice : invoiceDTO.getInvoices()) {
            // the whole invoice will be added as a single line
            // The text of this line has to be i18n
            InvoiceBL bl = new InvoiceBL(invoice.getId());
            Locale locale = getLocale(bl.getEntity().getUserId());

            ResourceBundle bundle = ResourceBundle.getBundle("entityNotifications", locale);
            SimpleDateFormat df = new SimpleDateFormat(bundle.getString("format.date"));

            StringBuilder delLine = new StringBuilder();
            delLine.append(bundle.getString("invoice.line.delegated"));
            delLine.append(" ").append(invoice.getPublicNumber()).append(" ");
            delLine.append(bundle.getString("invoice.line.delegated.due"));
            delLine.append(" ").append(df.format(invoice.getDueDate()));

            InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null,
                                                            delLine.toString(),
                                                            invoice.getBalance(),
                                                            null,
                                                            null,
                                                            Constants.INVOICE_LINE_TYPE_DUE_INVOICE,
                                                            0,
                                                            null,
                                                            null,
                                                            0);

            invoiceDTO.addResultLine(invoiceLine);
        }
    }

    /**
     * Returns the index of a tax line with the matching description. Used to find an existing
     * tax line so that similar taxes can be consolidated;
     *
     * @param lines invoice lines
     * @param desc tax line description
     * @return index of tax line
     */
    protected int getTaxLineIndex(List lines, String desc) {
        for (int f = 0; f < lines.size(); f++) {
            InvoiceLineDTO line = (InvoiceLineDTO) lines.get(f);
            if (line.getTypeId() == Constants.ORDER_LINE_TYPE_TAX) {
                if (line.getDescription().equals(desc)) {
                    return f;
                }
            }
        }

        return -1;
    }

    /**
     * Saves the amount the order contributed to the invoice total.
     *
     * @param orderId order id
     * @param invoiceDTO new invoice
     * @param amount amount contributed by order
     */
    protected void saveOrderTotalContributionToInvoice(Integer orderId, NewInvoiceDTO invoiceDTO, BigDecimal amount) {
        Map<Integer, BigDecimal> orderTotalContributions =  invoiceDTO.getOrderTotalContributions();
        BigDecimal total = orderTotalContributions.get(orderId);
        if (total == null) {
            total = amount;
        } else {
            total = total.add(amount);
        }
        orderTotalContributions.put(orderId, total);
    }

    /**
     * Composes the actual invoice line description based off of set entity preferences and the
     * order period being processed.
     *
     * @param order order being processed
     * @param period period of time being processed
     * @param desc original order line description
     * @return invoice line description
     */
    public String composeDescription(OrderDTO order, PeriodOfTime period, String desc) {
        Locale locale = getLocale(order.getBaseUserByUserId().getId());
        ResourceBundle bundle = ResourceBundle.getBundle("entityNotifications", locale);

        StringBuilder lineDescription = new StringBuilder();
        lineDescription.append(desc);

        /*
            append the billing period to the order line for non one-time orders
         */
        if (order.getOrderPeriod().getId() != Constants.ORDER_PERIOD_ONCE) {
            // period ends at midnight of the next day (E.g., Oct 1 00:00, effectivley end-of-day Sept 30th).
            // subtract 1 day from the end so the period print out looks human readable
            DateMidnight start = period.getDateMidnightStart();
            DateMidnight end = period.getDateMidnightEnd().minusDays(1);

            DateTimeFormatter dateFormat = DateTimeFormat.forPattern(bundle.getString("format.date"));

            LOG.debug("Composing for period " + start + " to " + end);
            LOG.debug("Using date format: " + bundle.getString("format.date"));

            // now add this to the line
            lineDescription.append(" ");
            lineDescription.append(bundle.getString("invoice.line.period"));
            lineDescription.append(" ");

            lineDescription.append(dateFormat.print(start));
            lineDescription.append(" ");
            lineDescription.append(bundle.getString("invoice.line.to"));
            lineDescription.append(" ");
            lineDescription.append(dateFormat.print(end));

        }

        /*
            optionally append the order id if the entity has the preference set
         */
        if (appendOrderId(order.getBaseUserByUserId().getCompany().getId())) {
            lineDescription.append(bundle.getString("invoice.line.orderNumber"));
            lineDescription.append(" ");
            lineDescription.append(order.getId().toString());
        }

        return lineDescription.toString();
    }

    /**
     * Calculates a price based on the period of time used.
     *
     * This plug-in does not do any period price calculation. The given price will be returned
     * untouched (see {@link com.sapienter.jbilling.server.process.task.DailyProRateCompositionTask}).
     *
     * @param fullPrice full line price
     * @param period period to calculate amount for
     * @return period price
     */
    public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
        return fullPrice;
    }

    /**
     * Gets the locale for the give user.
     *
     * @param userId user to get locale for
     * @return users locale
     */
    protected Locale getLocale(Integer userId) {
        if (locale == null) {
            try {
                UserBL user = new UserBL(userId);
                locale = user.getLocale();
            } catch (Exception e) {
                throw new SessionInternalError("Exception occurred determining user locale for composition.", e);
            }
        }

        return locale;
    }

    /**
     * Returns true if the given entity wants the order ID appended to the invoice line description.
     *
     * @param entityId entity id
     * @return true if order ID should be appended, false if not.
     */
    protected boolean appendOrderId(Integer entityId) {
        PreferenceBL pref = new PreferenceBL();
        try {
            pref.set(entityId, Constants.PREFERENCE_ORDER_IN_INVOICE_LINE);
        } catch (Exception e) {
            /* use default value */
        }

        return pref.getInt() == 1;
    }
}
