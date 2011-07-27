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

import org.apache.log4j.Logger;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.db.ItemTypeDAS;
import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.DescriptionBL;
import com.sapienter.jbilling.server.util.audit.EventLogger;

import java.util.List;

public class ItemTypeBL {
    private static final Logger LOG = Logger.getLogger(ItemTypeBL.class);

    private ItemTypeDAS itemTypeDas = null;
    private ItemTypeDTO itemType = null;
    private EventLogger eLogger = null;
    
    public ItemTypeBL(Integer itemTypeId)  {
        init();
        set(itemTypeId);
    }
    
    public ItemTypeBL() {
        init();
    }
    
    private void init() {
        eLogger = EventLogger.getInstance();        
        itemTypeDas = new ItemTypeDAS();
    }

    public ItemTypeDTO getEntity() {
        return itemType;
    }
    
    public void set(Integer id) {
        itemType = itemTypeDas.find(id);
    }
    
    public void create(ItemTypeDTO dto) {
        itemType = new ItemTypeDTO();
        itemType.setEntity(dto.getEntity());
        itemType.setOrderLineTypeId(dto.getOrderLineTypeId());
        itemType.setDescription(dto.getDescription());
        itemType = itemTypeDas.save(itemType);
    }
    
    public void update(Integer executorId, ItemTypeDTO dto) 
            throws SessionInternalError {
        eLogger.audit(executorId, null, Constants.TABLE_ITEM_TYPE, 
                itemType.getId(), EventLogger.MODULE_ITEM_TYPE_MAINTENANCE, 
                EventLogger.ROW_UPDATED, null,  
                itemType.getDescription(), null);

        itemType.setDescription(dto.getDescription());
        itemType.setOrderLineTypeId(dto.getOrderLineTypeId());
    }
    
    public void delete(Integer executorId) {
        if (isInUse()) {
            throw new SessionInternalError("Cannot delete a non-empty item type, remove items before deleting.");
        }

        LOG.debug("Deleting item type: " + itemType.getId());
        Integer itemTypeId = itemType.getId();
        itemTypeDas.delete(itemType);
        itemTypeDas.flush();
        itemTypeDas.clear();

        // now remove all the descriptions 
        DescriptionBL desc = new DescriptionBL();
        desc.delete(Constants.TABLE_ITEM_TYPE, itemTypeId);

        eLogger.audit(executorId, null, Constants.TABLE_ITEM_TYPE, itemTypeId,
                EventLogger.MODULE_ITEM_TYPE_MAINTENANCE, 
                EventLogger.ROW_DELETED, null, null,null);

    }   

    public boolean isInUse() {
        return itemTypeDas.isInUse(itemType.getId());
    }

    /**
     * Returns all item types, or an empty array if none found.
     *
     * @return array of item types, empty if none found.
     */
    public ItemTypeWS[] getAllItemTypes() {
        List<ItemTypeDTO> results = new ItemTypeDAS().findAll();
        ItemTypeWS[] types = new ItemTypeWS[results.size()];

        int index = 0;
        for (ItemTypeDTO type : results)
            types[index++] = new ItemTypeWS(type);

        return types;
    }
}
