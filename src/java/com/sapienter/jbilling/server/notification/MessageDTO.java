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

package com.sapienter.jbilling.server.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.sapienter.jbilling.common.Constants;

public class MessageDTO implements Serializable {
    // message type definitions (synch with DB)
    public static final Integer TYPE_INVOICE_EMAIL = new Integer(1);
    public static final Integer TYPE_AGEING = new Integer(2); // take from 2 to 9
    public static final Integer TYPE_CLERK_PAYOUT = new Integer(10);
    public static final Integer TYPE_PAYOUT = new Integer(11);
    public static final Integer TYPE_INVOICE_PAPER = new Integer(12);
    public static final Integer TYPE_ORDER_NOTIF = new Integer(13); // take from 13 to 15
    public static final Integer TYPE_PAYMENT = new Integer(16); // 16 & 17
    public static final Integer TYPE_INVOICE_REMINDER = new Integer(18);
    public static final Integer TYPE_CREDIT_CARD = new Integer(19);
    public static final Integer TYPE_FORGETPASSWORD_EMAIL = new Integer(20);
    
    // max length of a line (as defined in DB schema
    public static final Integer LINE_MAX = new Integer(1000);
    // most messages are emails. If they have an attachment the file name is here
    private String attachmentFile = null;
    
    private Integer typeId;
    private Integer languageId;
    private Boolean useFlag;
    private Integer deliveryMethodId;
    /*
     * The parameters to be used to get the replacements in the text
     */
    private HashMap parameters = null;
    // this is the message itself, after being loaded from the DB
    private List content = null;
    
    public MessageDTO() {
        parameters = new HashMap();
        content = new Vector();
        deliveryMethodId = Constants.D_METHOD_EMAIL;
    }
    /**
     * @return
     */
    public MessageSection[] getContent() {
        return (MessageSection[]) content.toArray(new MessageSection[0]);
    }
    
    public void setContent(MessageSection[] lines) {
        for (int f = 0; f < lines.length; f++) {
            addSection(lines[f]);
        }
    }

    /**
     * @return
     */
    public HashMap getParameters() {
        return parameters;
    }

    /**
     * @return
     */
    public Integer getTypeId() {
        return typeId;
    }

    /**
     * @param string
     */
    public void addSection(MessageSection line) {
        content.add(line);
    }

    /**
     * @param hashtable
     */
    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * @param integer
     */
    public void setTypeId(Integer integer) {
        typeId = integer;
    }

    public boolean validate() {
        if (typeId == null || parameters == null || content == null ||
                content.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * @return
     */
    public Integer getLanguageId() {
        return languageId;
    }

    /**
     * @param languageId
     */
    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    /**
     * @return
     */
    public Integer getDeliveryMethodId() {
        return deliveryMethodId;
    }

    /**
     * @param deliveryMethodId
     */
    public void setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    public Boolean getUseFlag() {
        return useFlag;
    }
    public void setUseFlag(Boolean useFlag) {
        this.useFlag = useFlag;
    }
    
    public String toString(){
        String ret = "language = " + languageId + " type = " + typeId + " use = " +
                useFlag + " content = ";
        for (int f = 0; f < content.size(); f++) {
            ret += "[" + content.get(f) + "]";
        }
        
        return ret;
    }
    public String getAttachmentFile() {
        return attachmentFile;
    }
    public void setAttachmentFile(String attachmentFile) {
        this.attachmentFile = attachmentFile;
    }
    
    public void setContentSize(int i) {
        ((Vector) content).setSize(i);
    }
}
