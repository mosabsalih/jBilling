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

import java.io.Serializable;

import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;


public class AuthorizeNetResponseDTO implements Serializable {
    private PaymentAuthorizationDTO dbRow = null;
    
    public AuthorizeNetResponseDTO(String rawResponse) {
        // wow, how easy this is ?!! :)
        String fields[] = rawResponse.split(",", -1);
        dbRow = new PaymentAuthorizationDTO();
        
        dbRow.setCode1(fields[0]); // code 
        dbRow.setCode2(fields[1]); // subcode
        dbRow.setCode3(fields[2]); // reason code
        dbRow.setResponseMessage(fields[3]); // a string with plain text with a reason for this result
        dbRow.setApprovalCode(fields[4]); 
        dbRow.setAvs(fields[5]);
        dbRow.setTransactionId(fields[6]);
        dbRow.setMD5(fields[37]);
        dbRow.setCardCode(fields[38]);        
    }
    
    public String toString() {
        return "[" +
            "code=" + dbRow.getCode1() + "," +
        "subCode=" + dbRow.getCode2() + "," +
        "reasonCode=" + dbRow.getCode3() + "," +
        "reasonText=" + dbRow.getResponseMessage() + "," +
        "approvalCode=" + dbRow.getApprovalCode() + "," +
        "AVSResultCode=" + dbRow.getAvs() + "," +
        "transactionId=" + dbRow.getTransactionId() + "," +
        "MD5Hash=" + dbRow.getMD5() + "," +
        "cardCode=" + dbRow.getCardCode() + 
        "]";
    }
    
    public PaymentAuthorizationDTO getPaymentAuthorizationDTO() {
        return dbRow;
    }

}
