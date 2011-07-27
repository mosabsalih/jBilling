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

package com.sapienter.jbilling.server.customer;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.ContactDTOEx;

@Transactional( propagation = Propagation.REQUIRED )
public class CustomerSessionBean implements ICustomerSessionBean {

    private static final Logger LOG = Logger.getLogger(
            CustomerSessionBean.class);

    public ContactDTOEx getPrimaryContactDTO(Integer userId)
            throws SessionInternalError {
        try {
            ContactBL bl = new ContactBL();
            bl.set(userId);
            return bl.getDTO();
        } catch (Exception e) {
            LOG.error("Exception retreiving the customer contact", e);
            throw new SessionInternalError("Customer primary contact");
        }
    }
}
