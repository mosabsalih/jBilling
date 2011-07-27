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

/*
 * Created on Apr 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sapienter.jbilling.server.pluggableTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.util.Constants;

/**
 * @author Emil
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PaymentEmailAuthorizeNetTask extends PaymentAuthorizeNetTask {
	
	
	// pluggable task parameters names
    public static final ParameterDescription PARAMETER_EMAIL_ADDRESS = 
        new ParameterDescription("email_address", true, ParameterDescription.Type.STR);
    
    public static final List<ParameterDescription> descriptions = new ArrayList<ParameterDescription>() {
        { 
        	descriptions.add(PARAMETER_EMAIL_ADDRESS); 
        }
    };

	
    public boolean process(PaymentDTOEx paymentInfo) 
            throws PluggableTaskException {
        Logger log = Logger.getLogger(PaymentEmailAuthorizeNetTask.class);
        boolean retValue = super.process(paymentInfo);
        String address = (String) parameters.get(PARAMETER_EMAIL_ADDRESS.getName());
        try {
            UserBL user = new UserBL(paymentInfo.getUserId());
            String message;
            if (new Integer(paymentInfo.getPaymentResult().getId()).equals(Constants.RESULT_OK)) {
                message = "payment.success";
            } else {
                message = "payment.fail";
            }
            String params[] = new String[6];
            params[0] = paymentInfo.getUserId().toString();
            params[1] = user.getEntity().getUserName();
            params[2] = paymentInfo.getId() + "";
            params[3] = paymentInfo.getAmount().toString();
            if (paymentInfo.getAuthorization() != null) {
                params[4] = paymentInfo.getAuthorization().getTransactionId();
                params[5] = paymentInfo.getAuthorization().getApprovalCode();
            } else {
                params[4] = "Not available";
                params[5] = "Not available";
            }
            log.debug("Bkp 6 " + params[0] + " " + params[1] + " " + params[2] + " " + params[3] + " " + params[4] + " " + params[5] + " ");
            NotificationBL.sendSapienterEmail(address, 
                    user.getEntity().getEntity().getId(), message, null, 
                    params);
        } catch (Exception e) {
            
            log.warn("Cant send receit email");
        }
        
        return retValue;
    }
}
