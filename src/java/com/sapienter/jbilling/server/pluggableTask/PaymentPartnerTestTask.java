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

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.util.Constants;

/**
 * @author Emil
 */
public class PaymentPartnerTestTask
    extends PluggableTask
    implements PaymentTask {

    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#process(com.sapienter.betty.server.payment.PaymentDTOEx)
     */
    public boolean process(PaymentDTOEx paymentInfo)
            throws PluggableTaskException {
        if (paymentInfo.getPayoutId() == null) {
            return true;
        }
        paymentInfo.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
        return false;
    }

    /* (non-Javadoc)
     * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#failure(java.lang.Integer, java.lang.Integer)
     */
    public void failure(Integer userId, Integer retry) {
        // TODO Auto-generated method stub

    }
    
    public boolean preAuth(PaymentDTOEx paymentInfo) 
        throws PluggableTaskException {
        return true;

    }

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth, PaymentDTOEx paymentInfo) throws PluggableTaskException {
        // TODO Auto-generated method stub
        return true;
    }
}
