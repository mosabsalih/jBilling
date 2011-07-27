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

import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;

public abstract class PaymentTaskBase extends PluggableTask implements PaymentTask {

    protected final String ensureGetParameter(String key) throws PluggableTaskException {
        Object value = parameters.get(key);
        if (false == value instanceof String) {
            throw new PluggableTaskException("Missed or wrong parameter for: " + key + ", string expected: " + value);
        }

        return (String) value;
    }

    protected final String getOptionalParameter(String key, String valueIfNull) {
        Object value = parameters.get(key);
        return (value instanceof String) ? (String) value : valueIfNull;
    }

    protected final boolean getBooleanParameter(String key) {
        return Boolean.parseBoolean(getOptionalParameter(key, "false"));
    }

    protected final void storeProcessedAuthorization(PaymentDTOEx paymentInfo,
            PaymentAuthorizationDTO auth) throws PluggableTaskException {

        new PaymentAuthorizationBL().create(auth, paymentInfo.getId());
        paymentInfo.setAuthorization(auth);
    }

    /**
     * Usefull for processors that want to use the same template method for
     * process() and preauth() methods
     */
    protected static final class Result {

        private final boolean myCallOtherProcessors;
        private final PaymentAuthorizationDTO myAuthorizationData;

        public Result(PaymentAuthorizationDTO data, boolean shouldCallOthers) {
            myAuthorizationData = data;
            myCallOtherProcessors = shouldCallOthers;
        }

        public PaymentAuthorizationDTO getAuthorizationData() {
            return myAuthorizationData;
        }

        public boolean shouldCallOtherProcessors() {
            return myCallOtherProcessors;
        }

        public String toString() {
            return "Result: myCallOtherProcessors " + myCallOtherProcessors +
                    " data " + myAuthorizationData;
        }
    }
    protected static final Result NOT_APPLICABLE = new Result(null, true);

    protected String getString(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }
}
