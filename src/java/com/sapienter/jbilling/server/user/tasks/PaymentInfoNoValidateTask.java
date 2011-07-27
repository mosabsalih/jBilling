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

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentInfoTask;
import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.user.AchBL;
import com.sapienter.jbilling.server.user.CreditCardBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.util.Constants;

/**
 * This creates payment dto. It now only goes and fetches the credit card
 * of the given user. It doesn't need to initialize the rest of the payment
 * information (amount, etc), only the info for the payment processor, 
 * usually cc info but it could be electronic cheque, etc...
 * This task should consider that the user is a partner and is being paid
 * (like a refund) and therefore fetch some other information, as getting 
 * paid with a cc seems not to be the norm.
 * @author Emil
 */
public class PaymentInfoNoValidateTask 
        extends PluggableTask implements PaymentInfoTask {

    private static final Logger LOG = Logger.getLogger(PaymentInfoNoValidateTask.class);
    /** 
     * This will return an empty payment dto with only the credit card/ach set
     * if a valid credit card is found for the user. Otherwise null.
     * It will check the customer's preference for the automatic payment type.
     */
    public PaymentDTOEx getPaymentInfo(Integer userId) 
            throws TaskException {
        PaymentDTOEx retValue = null;
        try {
            Integer method = Constants.AUTO_PAYMENT_TYPE_CC; // def to cc
            UserBL userBL = new UserBL(userId);
            CreditCardBL ccBL = new CreditCardBL();
            if (userBL.getEntity().getCustomer() != null) {
                // now non-customers only use credit cards
                method = userBL.getEntity().getCustomer().getAutoPaymentType();
                if (method == null) { 
                    method = Constants.AUTO_PAYMENT_TYPE_CC;
                }
            }
            
            if (method.equals(Constants.AUTO_PAYMENT_TYPE_CC)) {
                if (userBL.getEntity().getCreditCards().isEmpty()) {
                    // no credit cards entered! no payment ...
                } else {
                    // go around the provided cards and get one that is sendable
                    // to the processor
                    for (Iterator it = userBL.getEntity().getCreditCards().
                            iterator(); it.hasNext(); ) {
                        ccBL.set(((CreditCardDTO) it.next()).getId());
                        // takes the first one, no validation
                        retValue = new PaymentDTOEx();
                        retValue.setCreditCard(ccBL.getDTO());
                        retValue.setPaymentMethod(new PaymentMethodDAS().find(ccBL.getPaymentMethod()));
                        break;
                    }
                }
            } else if (method.equals(Constants.AUTO_PAYMENT_TYPE_ACH)) {
                AchDTO ach =  null;
                if (userBL.getEntity().getAchs().size() > 0) {
                    AchBL bl = new AchBL(((AchDTO)userBL.getEntity().getAchs().toArray()[0]).getId());
                    ach = bl.getEntity();
                }
                if (ach == null) {
                    // no info, no payment
                } else {
                    retValue = new PaymentDTOEx();
                    retValue.setAch(new AchDTO(0, ach.getAbaRouting(),
                            ach.getBankAccount(), ach.getAccountType(),
                            ach.getBankName(), ach.getAccountName(), ach.getGatewayKey()));
                    retValue.setPaymentMethod(new PaymentMethodDAS().find(Constants.PAYMENT_METHOD_ACH));
                }
            }
        } catch (Exception e) {
            throw new TaskException(e);
        }
        if (retValue == null) {
            LOG.debug("Could not find payment instrument for user " + userId);
        }
        return retValue;
    }

}
