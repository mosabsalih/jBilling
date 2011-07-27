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
package com.sapienter.jbilling.server.user.tasks;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.PaymentSessionBean;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.system.event.Event;
import com.sapienter.jbilling.server.system.event.task.IInternalEventsTask;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CustomerDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.event.DynamicBalanceChangeEvent;
import com.sapienter.jbilling.server.util.PreferenceBL;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Automatic payment task designed to "top up" a customers pre-paid balance with a user
 * configured amount whenever the balance drops below a company wide threshold (configured
 * as a preference).
 *
 * This task subscribes to the {@link DynamicBalanceChangeEvent} which is fired whenever
 * the customers balance changes.
 *
 * @see com.sapienter.jbilling.server.user.balance.DynamicBalanceManagerTask
 *
 * @author Brian Cowdery
 * @since  10-14-2009
 */
public class AutoRechargeTask extends PluggableTask implements IInternalEventsTask {

    private static final Logger LOG = Logger.getLogger(AutoRechargeTask.class);

    @SuppressWarnings("unchecked")
    private static final Class<Event>[] events = new Class[]{
        DynamicBalanceChangeEvent.class
    };

    public Class<Event>[] getSubscribedEvents() {
        return events;
    }

    public void process(Event event) throws PluggableTaskException {
        if (!(event instanceof DynamicBalanceChangeEvent)) {
            throw new PluggableTaskException("Cannot process event " + event);
        }

        DynamicBalanceChangeEvent balanceEvent = (DynamicBalanceChangeEvent) event;
        UserDTO user = new UserBL(balanceEvent.getUserId()).getDto();
        CustomerDTO customer = user.getCustomer();

        LOG.debug("Processing " + event);

        if (!isEventProcessable(balanceEvent.getNewBalance(), user, customer)) {
            LOG.debug("Conditions not met, no recharge");
            return;
        }

        PaymentDTOEx payment = null;
        try {
            payment = PaymentBL.findPaymentInstrument(event.getEntityId(), user.getId());
        } catch (TaskException e) {
            throw new PluggableTaskException(e);
        }

        if (payment != null) {
            payment.setIsRefund(0);
            payment.setAttempt(1);
            payment.setAmount(customer.getAutoRecharge());
            payment.setCurrency(user.getCurrency());
            payment.setUserId(user.getId());
            payment.setPaymentDate(new Date());

            LOG.debug("Making automatic payment of $" + payment.getAmount() + " for user " + payment.getUserId());

            // can't use the managed bean, a new transaction will cause the CustomerDTO to get an
            // optimistic lock: this transaction and the new payment one both changing the same customer.dynamic_balance
            IPaymentSessionBean paymentSession = new PaymentSessionBean(); 

            Integer result = paymentSession.processAndUpdateInvoice(payment, null, balanceEvent.getEntityId());

            LOG.debug("Payment created with result: " + result);
        } else {
            LOG.debug("No payment instrument, no recharge");
        }
    }

    /**
     * Returns true if the auto-recharge criteria has been met and this event can be processed.
     *
     * @param newBalance new dynamic balance of the user
     * @param user user to validate
     * @param customer customer to validate
     * @return true if event can be processed, false if not.
     * @throws PluggableTaskException
     */
    private boolean isEventProcessable(BigDecimal newBalance, UserDTO user, CustomerDTO customer) {
        if (customer == null || customer.getAutoRecharge().compareTo(BigDecimal.ZERO) <= 0) {
            LOG.debug("Not a customer, or auto recharge value <= 0");
            return false;
        }

        BigDecimal threshold = getAutoRechargeThreshold(user.getEntity().getId());
        if (threshold != null && threshold.compareTo(newBalance) > 0) {
            if (!Constants.BALANCE_PRE_PAID.equals(customer.getBalanceType())) {
                LOG.debug("User " + user.getId() + " does not hold a pre-paid balance, cannot make automatic payment!");
                return false;
            }
        } else {
            LOG.debug("Company does not have a recharge preference, or this customer balance not reached the threshold");
            return false;
        }
        return true;
    }

    /**
     * Returns the set auto-recharge threshold for the given entity id, or null
     * if the company does not have a configured threshold.
     *
     * @param entityId entity id
     * @return auto-recharge threshold or null if not set
     */
    private BigDecimal getAutoRechargeThreshold(Integer entityId) {
        PreferenceBL preference = new PreferenceBL();
        try {
            preference.set(entityId, Constants.PREFERENCE_AUTO_RECHARGE_THRESHOLD);
        } catch (EmptyResultDataAccessException e) {            
            return null; // no threshold set
        }
        return new BigDecimal(preference.getFloat());
    }
    
}
