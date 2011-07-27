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

package com.sapienter.jbilling.server.rule.task.test;

import java.util.List;

import com.sapienter.jbilling.server.item.db.ItemDAS;
import com.sapienter.jbilling.server.item.db.ItemDTO;

public class Product {
    private String name;
    private ItemDTO item;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public ItemDTO getItem() {
        if (item == null && name != null) {
            // lookup the id using the internalNumber
            List<ItemDTO> items = new ItemDAS().findItemsByInternalNumber(name);
            // just get first one
            if (!items.isEmpty()) {
                item = items.get(0);
            }
        }
        return item;
    }

    public Integer getItemId() {
        if (getItem() != null) {
            return item.getId();
        } else {
            return null;
        }
    }
}
