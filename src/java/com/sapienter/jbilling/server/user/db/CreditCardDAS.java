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
package com.sapienter.jbilling.server.user.db;

import java.util.List;


import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.hibernate.Query;

public class CreditCardDAS extends AbstractDAS<CreditCardDTO> {

    private static final String findByLastDigits =
            " select b.id " +
            "   from UserDTO b join b.creditCards c " +
            "  where b.company.id = :entity " +
            "    and c.ccNumberPlain = :plain " +
            "    and b.deleted = 0 " +
            "    and c.deleted = 0";
    
    private static final String findByCreditCard =
            " select distinct bu.userName " +
            " 	from UserDTO bu, PaymentDTO p, CreditCardDTO cc " + 
            " where cc.rawNumber = :number " +
            " 	and cc.id = p.creditCard.id " +
            " 	and p.baseUser.id = bu.id";

    public List<Integer> findByLastDigits(Integer entityId, String plain) {
        Query query = getSession().createQuery(findByLastDigits);
        query.setParameter("entity", entityId);
        query.setParameter("plain", plain);
        query.setComment("CreditCardDAS.findByLastDigits " + entityId + " " + plain);
        return query.list();
    }

    public List<String> findByNumber(String number){
        Query query = getSession().createQuery(findByCreditCard);
        query.setParameter("number", number);
        query.setComment("CreditCardDAS.findByCreditCard " + number);
        return query.list();
    }
}
