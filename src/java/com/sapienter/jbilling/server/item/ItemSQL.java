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

/**
 * @author Emil
 */
public interface ItemSQL {
    
    // the general list of items, shows always the description of
    // the entity. This then prevents items not showing up because
    // the logged user has a differenct language
    static final String list = 
        "select a.id, a.id, a.internal_number, b.content " +
        "  from item a, international_description b, jbilling_table c," +
        "       entity e " +
        " where a.entity_id = e.id " +
        "   and e.id = ? " +
        "   and a.deleted = 0 " +
        "   and b.table_id = c.id " +
        "   and c.name = 'item' " +
        "   and b.foreign_id = a.id " +
        "   and b.language_id = e.language_id " +
        "   and b.psudo_column = 'description' " +
        " order by a.internal_number";

    static final String listType = 
        "select a.id, a.id, a.description " +
        "  from item_type a " +
        " where a.entity_id = ? ";

    static final String listUserPrice = 
        "select d.id, a.id, a.internal_number, b.content, d.price " +
        "  from item a, international_description b, jbilling_table c, " + 
        "       item_user_price d " +
        " where a.entity_id = ? " +
        "   and d.user_id = ? " +
        "   and a.id = d.item_id " +
        "   and a.deleted = 0 " +
        "   and b.table_id = c.id " +
        "   and c.name = 'item' " +
        "   and b.foreign_id = a.id " +
        "   and b.language_id = ? " +
        "   and b.psudo_column = 'description' " +
        " order by 1";

    static final String listPromotion = 
        "select b.id, b.code, b.since, b.until, b.once, c.content" +
        "  from item a, promotion b, international_description c, jbilling_table d  " +
        " where a.entity_id = ? " +
        "   and a.deleted = 0 " +
        "   and c.table_id = d.id " +
        "   and d.name = 'item' " +
        "   and c.foreign_id = a.id " +
        "   and c.language_id = ? " +
        "   and c.psudo_column = 'description' " +
        "   and a.id = b.item_id " +
        " order by 1";

}
