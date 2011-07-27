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

import java.util.List;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

/*
 *
 * This is the session facade for the Item. All interaction from the client
 * to the server is made through calls to the methods of this class. This 
 * class uses helper classes (Business Logic -> BL) for the real logic.
 *
 * @author emilc
 * 
 */

public interface IItemSessionBean {

    public Integer create(ItemDTO dto, Integer languageId) 
            throws SessionInternalError;    

    public void update(Integer executorId, ItemDTO dto, Integer languageId) 
            throws SessionInternalError;
    
    public boolean validateDecimals( Integer hasDecimals, Integer itemId );

    public ItemDTO get(Integer id, Integer languageId, Integer userId,
            Integer currencyId, Integer entityId, List<PricingField> 
            pricingFields) throws SessionInternalError;


    public void delete(Integer executorId, Integer id) 
            throws SessionInternalError;

    public Integer createType(ItemTypeDTO dto) throws SessionInternalError;
    
    public ItemTypeDTO getType(Integer id) throws SessionInternalError;

    public void updateType(Integer executorId, ItemTypeDTO dto) 
            throws SessionInternalError;

    /*
     * For now, this will delete permanently
     *
     */
     public void deleteType(Integer executorId, Integer itemTypeId) 
             throws SessionInternalError;

    public CurrencyDTO[] getCurrencies(Integer languageId, Integer entityId) 
            throws SessionInternalError;
    
    public void setCurrencies(Integer entityId, CurrencyDTO[] currencies,
            Integer currencyId) throws SessionInternalError;

    public Integer getEntityCurrency(Integer entityId) 
            throws SessionInternalError;
}
