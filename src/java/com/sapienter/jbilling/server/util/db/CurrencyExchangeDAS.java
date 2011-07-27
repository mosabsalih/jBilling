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
package com.sapienter.jbilling.server.util.db;

import java.util.List;

import org.hibernate.Query;


public class CurrencyExchangeDAS extends AbstractDAS<CurrencyExchangeDTO> {
    private static final String findExchangeSQL =
        "SELECT a " +
        "  FROM CurrencyExchangeDTO a " +
        " WHERE a.entityId = :entity " +
        "   AND a.currency.id = :currency";
    
    private static final String  findByEntitySQL =
        " SELECT a " +
        "   FROM CurrencyExchangeDTO a " +
        "  WHERE a.entityId = :entity";

    public CurrencyExchangeDTO findExchange(Integer entityId,Integer currencyId) {
        Query query = getSession().createQuery(findExchangeSQL);
        query.setParameter("entity", entityId);
        query.setParameter("currency", currencyId);
        return (CurrencyExchangeDTO) query.uniqueResult();
    }
    
    public List<CurrencyExchangeDTO> findByEntity(Integer entityId) {
        Query query = getSession().createQuery(findByEntitySQL);
        query.setParameter("entity", entityId);
        return query.list();
    }
}
