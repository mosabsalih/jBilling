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
import com.sapienter.jbilling.server.item.db.ItemTypeDAS;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/*
 *
 * This is the session facade for the Item. All interaction from the client
 * to the server is made through calls to the methods of this class. This 
 * class uses helper classes (Business Logic -> BL) for the real logic.
 *
 * @author emilc
 * 
 */

@Transactional( propagation = Propagation.REQUIRED )
public class ItemSessionBean implements IItemSessionBean {

    //private static final Logger LOG = Logger.getLogger(ItemSessionBean.class);

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------  

    public Integer create(ItemDTO dto, Integer languageId) 
            throws SessionInternalError {
        try {
            ItemBL bl = new ItemBL();
            return bl.create(dto, languageId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    

    public void update(Integer executorId, ItemDTO dto, Integer languageId) 
            throws SessionInternalError {
        try {
            ItemBL bl = new ItemBL(dto.getId());
            bl.update(executorId, dto, languageId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    
    public boolean validateDecimals( Integer hasDecimals, Integer itemId ) {
        if( itemId == null ) { return true; }
        ItemBL bl = new ItemBL(itemId);
        return bl.validateDecimals( hasDecimals );
    }

    public ItemDTO get(Integer id, Integer languageId, Integer userId,
            Integer currencyId, Integer entityId, 
            List<PricingField> pricingFields) throws SessionInternalError {
        try {
            ItemBL itemBL = new ItemBL(id);
            itemBL.setPricingFields(pricingFields);
            return itemBL.getDTO(languageId, userId, entityId, currencyId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    } 

    public void delete(Integer executorId, Integer id) 
            throws SessionInternalError {
        try {
            ItemBL bl = new ItemBL(id);
            bl.delete(executorId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
        
    } 

    public Integer createType(ItemTypeDTO dto) 
            throws SessionInternalError {
        try {
            ItemTypeBL bl = new ItemTypeBL();
            bl.create(dto);
            return bl.getEntity().getId();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    
    public ItemTypeDTO getType(Integer id) 
            throws SessionInternalError {
        try {            
            ItemTypeDTO type = new ItemTypeDAS().find(id);
            ItemTypeDTO dto = new ItemTypeDTO();
            dto.setId(type.getId());
            dto.setEntity(type.getEntity());
            dto.setDescription(type.getDescription());
            dto.setOrderLineTypeId(type.getOrderLineTypeId());

            return dto;        
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void updateType(Integer executorId, ItemTypeDTO dto) 
            throws SessionInternalError {
        try {
            ItemTypeBL bl = new ItemTypeBL(dto.getId());
            bl.update(executorId, dto);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

    }

    /*
     * For now, this will delete permanently
     *
     */
     public void deleteType(Integer executorId, Integer itemTypeId) 
             throws SessionInternalError {
         try {
             
             ItemTypeBL bl = new ItemTypeBL(itemTypeId);
             bl.delete(executorId);

         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }

    public CurrencyDTO[] getCurrencies(Integer languageId, Integer entityId) 
            throws SessionInternalError {
        try {
            CurrencyBL bl = new CurrencyBL();
            return bl.getCurrencies(languageId, entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }
    
    public void setCurrencies(Integer entityId, CurrencyDTO[] currencies,
            Integer currencyId) 
            throws SessionInternalError {
        try {
            CurrencyBL bl = new CurrencyBL();
            bl.setCurrencies(entityId, currencies);
            bl.setEntityCurrency(entityId, currencyId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
        
    }   

    public Integer getEntityCurrency(Integer entityId) 
            throws SessionInternalError {
        try {
            CurrencyBL bl = new CurrencyBL();
            return bl.getEntityCurrency(entityId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
        
    }   
           
}
