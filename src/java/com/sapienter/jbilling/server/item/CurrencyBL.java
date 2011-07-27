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

package com.sapienter.jbilling.server.item;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.JNDILookup;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import com.sapienter.jbilling.server.util.db.CurrencyExchangeDAS;
import com.sapienter.jbilling.server.util.db.CurrencyExchangeDTO;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Emil
 */
public class CurrencyBL {
    private static final Logger LOG = Logger.getLogger(CurrencyBL.class);


    private static final Integer SYSTEM_RATE_ENTITY_ID = 0;

    private CurrencyDAS currencyDas = null;
    private CurrencyExchangeDAS exchangeDas = null;

    private CurrencyDTO currency = null;

    public CurrencyBL() {
        init();
    }

    public CurrencyBL(Integer currencyId) {
        init();
        set(currencyId);
    }

    public CurrencyBL(CurrencyDAS currencyDas, CurrencyExchangeDAS exchangeDas) {
        this.currencyDas = currencyDas;
        this.exchangeDas = exchangeDas;
    }

    private void init() {
        currencyDas = new CurrencyDAS();
        exchangeDas = new CurrencyExchangeDAS();
    }

    public void set(Integer id)  {
        currency = currencyDas.find(id);
    }

    public CurrencyDTO getEntity() {
        return currency;
    }

    public Integer create(CurrencyDTO dto, Integer entityId) {
        if (dto != null) {

            /*
                Simplify currency creation; Set exchange rates from transient CurrencyDTO#getRate() and
                CurrencyDTO#getSysRate() if no currency exchanges have been mapped.
             */
            if (dto.getCurrencyExchanges().isEmpty()) {
                // set optional exchange rate
                if (dto.getRate() != null) {
                    CurrencyExchangeDTO exchangeRate = new CurrencyExchangeDTO();
                    exchangeRate.setEntityId(entityId);
                    exchangeRate.setCurrency(dto);
                    exchangeRate.setRate(dto.getRateAsDecimal());
                    exchangeRate.setCreateDatetime(new Date());

                    dto.getCurrencyExchanges().add(exchangeRate);
                }

                // set system rate
                CurrencyExchangeDTO sysRate = new CurrencyExchangeDTO();
                sysRate.setEntityId(SYSTEM_RATE_ENTITY_ID);
                sysRate.setCurrency(dto);
                sysRate.setRate(dto.getSysRate() != null ? dto.getSysRate() : BigDecimal.ONE);
                sysRate.setCreateDatetime(new Date());

                dto.getCurrencyExchanges().add(sysRate);

            }

            this.currency = currencyDas.save(dto);

            // add active currencies to the company map
            if (dto.getInUse()) {
                CompanyDTO company = new CompanyDAS().find(entityId);
                company.getCurrencies().add(this.currency);
            }

            return this.currency.getId();
        }

        LOG.error("Cannot save a null CurrencyDTO!");
        return null;
    }

    public void update(CurrencyDTO dto, Integer entityId) {
        if (currency != null) {
            currency.setSymbol(dto.getSymbol());
            currency.setCode(dto.getCode());
            currency.setCountryCode(dto.getCountryCode());

            currency.getCurrencyExchanges().clear();

            // set optional exchange rate
            if (dto.getRate() != null) {
                CurrencyExchangeDTO exchangeRate = new CurrencyExchangeDTO();
                exchangeRate.setEntityId(entityId);
                exchangeRate.setCurrency(currency);
                exchangeRate.setRate(dto.getRateAsDecimal());
                exchangeRate.setCreateDatetime(new Date());

                currency.getCurrencyExchanges().add(exchangeRate);
            }

            // set system rate
            CurrencyExchangeDTO sysRate = new CurrencyExchangeDTO();
            sysRate.setEntityId(SYSTEM_RATE_ENTITY_ID);
            sysRate.setCurrency(currency);
            sysRate.setRate(dto.getSysRate() != null ? dto.getSysRate() : BigDecimal.ONE);
            sysRate.setCreateDatetime(new Date());

            currency.getCurrencyExchanges().add(sysRate);


            // add active currencies to the company map
            CompanyDTO company = new CompanyDAS().find(entityId);
            if (dto.getInUse()) {
                company.getCurrencies().add(currency);
            } else {
                company.getCurrencies().remove(currency);
            }

        } else {
            LOG.error("Cannot update, CurrencyDTO not found or not set!");
        }
    }

    public Integer getEntityCurrency(Integer entityId) {
        CompanyDTO entity = new CompanyDAS().find(entityId);
        return entity.getCurrencyId();
    }

    public void setEntityCurrency(Integer entityId, Integer currencyId) {
        CompanyDTO entity = new CompanyDAS().find(entityId);
        entity.setCurrency(new CurrencyDAS().find(currencyId));
    }

    @SuppressWarnings("unchecked")
    public CurrencyDTO[] getCurrencies(Integer languageId, Integer entityId) throws NamingException, SQLException {

        CurrencyDTO[] currencies = getSymbols();

        for (CurrencyDTO currency : currencies) {
            set(currency.getId());
            currency.setName(this.currency.getDescription(languageId));

            // find system rate
            if (currency.getId() == 1) {
                currency.setSysRate(new BigDecimal("1.0"));
            } else {
                currency.setSysRate(exchangeDas.findExchange(0, currency.getId()).getRate());
            }

            // find entity specific rate
            CurrencyExchangeDTO exchange = exchangeDas.findExchange(entityId, currency.getId());
            if (exchange != null)
                currency.setRate(exchange.getRate().toString());

            // set in-use flag
            currency.setInUse(entityHasCurrency(entityId, currency.getId()));
        }

        return currencies;
    }
    
    public void setCurrencies(Integer entityId, CurrencyDTO[] currencies) throws NamingException, ParseException {
        EntityBL entity = new EntityBL(entityId);

        // start by wiping out the existing data for this entity
        entity.getEntity().getCurrencies().clear();
        for (Iterator it = exchangeDas.findByEntity(entityId).iterator(); it.hasNext(); ) {
            CurrencyExchangeDTO exchange = (CurrencyExchangeDTO) it.next();
            exchangeDas.delete(exchange);
        }

        for (int f = 0; f < currencies.length; f++) {
            if (currencies[f].getInUse()) {
                set(currencies[f].getId());

                entity.getEntity().getCurrencies().add(new CurrencyDAS().find(currency.getId()));

                if (currencies[f].getRate() != null) {
                    CurrencyExchangeDTO exchange = new CurrencyExchangeDTO();
                    exchange.setCreateDatetime(Calendar.getInstance().getTime());
                    exchange.setCurrency(new CurrencyDAS().find(currencies[f].getId()));
                    exchange.setEntityId(entityId);
                    exchange.setRate(currencies[f].getRateAsDecimal());
                    exchangeDas.save(exchange);
                }
            }
        }
    }

    public CurrencyDTO[] getSymbols() throws NamingException, SQLException {
        List<CurrencyDTO> currencies = new CurrencyDAS().findAll();
        return currencies.toArray(new CurrencyDTO[currencies.size()]);
    }

    /**
     * Ok, this is cheating, but heck is easy and fast.
     * @param entityId
     * @param currencyId
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    private boolean entityHasCurrency(Integer entityId, Integer currencyId) throws SQLException, NamingException {
        boolean retValue = false;
        JNDILookup jndi = JNDILookup.getFactory();
        Connection conn = jndi.lookUpDataSource().getConnection();
        PreparedStatement stmt = conn.prepareStatement(
                "select 1 " +
                "  from currency_entity_map " +
                " where currency_id = ? " +
                "   and entity_id = ?");

        stmt.setInt(1, currencyId);
        stmt.setInt(2, entityId);
        ResultSet result = stmt.executeQuery();

        if (result.next()) {
            retValue = true;
        }
        
        result.close();
        stmt.close();
        conn.close();
        
        return retValue;
    }


    /*
        Currency conversion
     */

    public BigDecimal convert(Integer fromCurrencyId, Integer toCurrencyId, BigDecimal amount, Integer entityId)
            throws SessionInternalError {

        LOG.debug("Converting " + fromCurrencyId + " to " + toCurrencyId + " am " + amount + " en " + entityId);
        if (fromCurrencyId.equals(toCurrencyId)) {
            return amount; // mmm.. no conversion needed
        }

        // make the conversions
        return convertPivotToCurrency(toCurrencyId, convertToPivot(fromCurrencyId, amount, entityId), entityId);
    }

    public BigDecimal convertToPivot(Integer currencyId, BigDecimal amount, Integer entityId)
            throws SessionInternalError {

        if (currencyId.equals(1)) {
            return amount; // this is already in the pivot
        }

        // make the conversion itself
        CurrencyExchangeDTO exchange = findExchange(entityId, currencyId);
        return amount.divide(exchange.getRate(), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
    }

    public BigDecimal convertPivotToCurrency(Integer currencyId, BigDecimal amount, Integer entityId)
            throws SessionInternalError {

        if (currencyId.equals(1)) {
            return amount; // this is already in the pivot
        }
        if ( amount.compareTo(BigDecimal.ZERO) == 0) {
        	return BigDecimal.ZERO;
        }

        CurrencyExchangeDTO exchange = findExchange(entityId, currencyId);

        // make the conversion itself
        return amount.multiply(exchange.getRate());
    }

    public CurrencyExchangeDTO findExchange(Integer entityId, Integer currencyId) throws SessionInternalError {
        CurrencyExchangeDTO exchange = exchangeDas.findExchange(entityId, currencyId);
        if (exchange == null) {
            // this entity doesn't have this exchange defined
            // 0 is the default, don't try to use null, it won't work
            exchange = exchangeDas.findExchange(0, currencyId);
            if (exchange == null) {
                throw new SessionInternalError("Currency " + currencyId + " doesn't have a defualt exchange");
            }
        }

        return exchange;
    }
}
