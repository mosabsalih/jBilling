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
package com.sapienter.jbilling.server.item.db;

import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class ItemTypeDAS extends AbstractDAS<ItemTypeDTO> {

    public static final String PLANS_INTERNAL_CATEGORY_NAME = "plans";

    /**
     * Returns true if the given item type ID is in use.
     *
     * @param typeId type id
     * @return true if in use, false if not
     */
    public boolean isInUse(Integer typeId) {
        Criteria criteria = getSession().createCriteria(getPersistentClass())
                .add(Restrictions.eq("id", typeId))
                .createAlias("items", "item")
                .add(Restrictions.eq("item.deleted", 0)) // item type contains non-deleted items
                .setProjection(Projections.count("item.id"));

        criteria.setComment("ItemTypeDTO.isInUse");

        return (criteria.uniqueResult() != null && ((Integer) criteria.uniqueResult()) > 0);
    }
}
