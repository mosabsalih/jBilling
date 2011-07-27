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

package com.sapienter.jbilling.server.user.balance;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.event.NewOrderEvent;
import com.sapienter.jbilling.server.order.event.NewQuantityEvent;
import com.sapienter.jbilling.server.order.event.OrderDeletedEvent;
import com.sapienter.jbilling.server.order.event.OrderAddedOnInvoiceEvent;
import com.sapienter.jbilling.server.payment.event.PaymentDeletedEvent;
import com.sapienter.jbilling.server.payment.event.PaymentSuccessfulEvent;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.system.event.task.IInternalEventsTask;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.event.DynamicBalanceChangeEvent;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import java.math.BigDecimal;
import org.apache.log4j.Logger;

/**
 *
 * @author emilc
 */
public class DynamicBalanceManagerTask extends PluggableTask implements IInternalEventsTask {
    private static final Logger LOG = Logger.getLogger(DynamicBalanceManagerTask.class);

    @SuppressWarnings("unchecked")
    private static final Class<Event> events[] = new Class[] { 
        PaymentSuccessfulEvent.class,
        OrderDeletedEvent.class,
        NewOrderEvent.class,
        PaymentDeletedEvent.class,
        OrderAddedOnInvoiceEvent.class,
        NewQuantityEvent.class
    };

    public Class<Event>[] getSubscribedEvents() {
        return events;
    }

    public void process(Event event) throws PluggableTaskException {
        updateDynamicBalance(event.getEntityId(), determineUserId(event), determineAmount(event));
    }

    private BigDecimal determineAmount(Event event) {
        if (event instanceof PaymentSuccessfulEvent) {
            PaymentSuccessfulEvent payment = (PaymentSuccessfulEvent) event;
            return payment.getPayment().getAmount();

        } else if (event instanceof OrderDeletedEvent) {
            OrderDeletedEvent order = (OrderDeletedEvent) event;
            if (order.getOrder().getOrderPeriod().getId() ==  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                return order.getOrder().getTotal();
            } else {
                return BigDecimal.ZERO;
            }

        } else if (event instanceof NewOrderEvent) {
            NewOrderEvent order = (NewOrderEvent) event;
            if (order.getOrder().getOrderPeriod().getId() ==  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                return order.getOrder().getTotal().multiply(new BigDecimal(-1));
            } else {
                return BigDecimal.ZERO;
            }

        } else if (event instanceof PaymentDeletedEvent) {
            PaymentDeletedEvent payment = (PaymentDeletedEvent) event;
            return payment.getPayment().getAmount().multiply(new BigDecimal(-1));

        } else if (event instanceof OrderAddedOnInvoiceEvent) {

            OrderAddedOnInvoiceEvent orderOnInvoiceEvent = (OrderAddedOnInvoiceEvent) event;
            OrderAddedOnInvoiceEvent order = (OrderAddedOnInvoiceEvent) event;
            if (order.getOrder().getOrderPeriod().getId() !=  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                return orderOnInvoiceEvent.getTotalInvoiced().multiply(new BigDecimal(-1));
            } else {
                return BigDecimal.ZERO;
            }

        } else if (event instanceof NewQuantityEvent) {
            NewQuantityEvent nq = (NewQuantityEvent) event;

            if (new OrderDAS().find(nq.getOrderId()).getOrderPeriod().getId() ==
                    com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                BigDecimal newTotal, oldTotal;
                // new order line, or old one updated?
                if (nq.getNewOrderLine() == null) {
                    // new
                    oldTotal = BigDecimal.ZERO;
                    newTotal = nq.getOrderLine().getAmount();
                    if (nq.getNewQuantity().compareTo(BigDecimal.ZERO) == 0) {
                        // it is a delete
                        newTotal = newTotal.multiply(new BigDecimal(-1));
                    }
                } else {
                    // old
                    oldTotal = nq.getOrderLine().getAmount();
                    newTotal = nq.getNewOrderLine().getAmount();
                }
                return newTotal.subtract(oldTotal).multiply(new BigDecimal(-1));
                
            } else {
                return BigDecimal.ZERO;
            }
        }  else {
            LOG.error("Can not determine amount for event " + event);
            return null;
        }
    }

    private int determineUserId(Event event) {
        if (event instanceof PaymentSuccessfulEvent) {
            PaymentSuccessfulEvent payment = (PaymentSuccessfulEvent) event;
            return payment.getPayment().getUserId();
        } else if (event instanceof OrderDeletedEvent) {
            OrderDeletedEvent order = (OrderDeletedEvent) event;
            return order.getOrder().getBaseUserByUserId().getId();
        } else if (event instanceof NewOrderEvent) {
            NewOrderEvent order = (NewOrderEvent) event;
            return order.getOrder().getBaseUserByUserId().getId();
        } else if (event instanceof PaymentDeletedEvent) {
            PaymentDeletedEvent payment = (PaymentDeletedEvent) event;
            return payment.getPayment().getBaseUser().getId();
        } else if (event instanceof OrderAddedOnInvoiceEvent) {
            OrderAddedOnInvoiceEvent order = (OrderAddedOnInvoiceEvent) event;
            return order.getOrder().getBaseUserByUserId().getId();
        } else if (event instanceof NewQuantityEvent) {
            NewQuantityEvent nq = (NewQuantityEvent) event;
            return new OrderDAS().find(nq.getOrderId()).getBaseUserByUserId().getId();
        }  else {
            LOG.error("Can not determine user for event " + event);
            return 0;
        }
    }

    private void updateDynamicBalance(Integer entityId, Integer userId, BigDecimal amount) {
        UserDTO user = new UserDAS().find(userId);
        CustomerDTO customer = user.getCustomer();

        // get the parent customer that pays, if it exists
        if (customer != null) {
            while (customer.getParent() != null
                    && (customer.getInvoiceChild() == null || customer.getInvoiceChild() == 0)) {                
                customer =  customer.getParent(); // go up one level
            }
        }

        // fail fast condition, no dynamic balance or ammount is zero
        if (customer == null
                || customer.getBalanceType() == Constants.BALANCE_NO_DYNAMIC
                || amount.compareTo(BigDecimal.ZERO) == 0) {
            LOG.debug("Nothing to update");
            return;
        }

        LOG.debug("Updating dynamic balance for " + amount);

        BigDecimal balance = (customer.getDynamicBalance() == null ? BigDecimal.ZERO : customer.getDynamicBalance());

        // register the event, before the balance is changed
        new EventLogger().auditBySystem(entityId,
                                        customer.getBaseUser().getId(),
                                        com.sapienter.jbilling.server.util.Constants.TABLE_CUSTOMER,
                                        user.getCustomer().getId(),
                                        EventLogger.MODULE_USER_MAINTENANCE,
                                        EventLogger.DYNAMIC_BALANCE_CHANGE,
                                        null,
                                        balance.toString(),
                                        null);

        if (customer.getBalanceType() == Constants.BALANCE_CREDIT_LIMIT) {
            customer.setDynamicBalance(balance.subtract(amount));

        } else if (customer.getBalanceType() == Constants.BALANCE_PRE_PAID) {
            customer.setDynamicBalance(balance.add(amount));

        } else {
            customer.setDynamicBalance(balance);
        }

        if (!balance.equals(customer.getDynamicBalance())) {
            DynamicBalanceChangeEvent event = new DynamicBalanceChangeEvent(user.getEntity().getId(),
                                                                            user.getUserId(),
                                                                            customer.getDynamicBalance(), // new
                                                                            balance);                     // old
            EventManager.process(event);
        }
    }
}
