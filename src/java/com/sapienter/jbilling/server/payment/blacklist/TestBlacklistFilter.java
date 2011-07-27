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
package com.sapienter.jbilling.server.payment.blacklist;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDAS;
import com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.UserDAS;

public class TestBlacklistFilter implements BlacklistFilter {

    private static final Logger LOG = Logger.getLogger(TestBlacklistFilter.class);

    public Result checkPayment(PaymentDTOEx paymentInfo) {
        return checkUser(paymentInfo.getUserId());
    }

    public Result checkUser(Integer userId) {
        // checks if the userId is blacklisted (which would probably be set by
        // customer service through the GUI)
        List<BlacklistDTO> blacklist = new BlacklistDAS().findByUserType(
                userId, BlacklistDTO.TYPE_USER_ID);

        if (!blacklist.isEmpty()) {
            // id blacklisted, but first lets test blacklist db functionality
            if (!testDB()) {
                return new Result(true, "Problems with blacklist db test");
            }
            return new Result(true, "This user is blacklisted");
        }
        return new Result(false, null);
    }

    // returns true if success
    private boolean testDB() {
        BlacklistDAS blacklistDas = new BlacklistDAS();

        // try adding a blacklist entry
        BlacklistDTO entry = new BlacklistDTO();
        entry.setCompany(new CompanyDAS().find(1));
        entry.setCreateDate(new Date());
        entry.setType(BlacklistDTO.TYPE_USER_ID); 
        entry.setSource(BlacklistDTO.SOURCE_EXTERNAL_UPLOAD);
        entry.setUser(new UserDAS().find(1001)); 
        blacklistDas.save(entry);
        blacklistDas.flush();

        // try getting it back
        List<BlacklistDTO> blacklist = blacklistDas.findByEntityType(1, 
                BlacklistDTO.TYPE_USER_ID);

        if (blacklist.size() != 2) {
            // didn't work
            LOG.debug("Returned blacklist size is: " + blacklist.size());
            return false;
        }

        entry = blacklist.get(1);
        if (entry.getType() != BlacklistDTO.TYPE_USER_ID) {
            LOG.debug("Blacklist entry type is: " + entry.getType());
            return false;
        }

        if (entry.getUser().getId() != 1001) {
            LOG.debug("user id is: " + entry.getUser().getId());
            return false;
        }

        return true;
    }

    public String getName() {
        return "Test blacklist filter";
    }
}
