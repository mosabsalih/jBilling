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

package jbilling

/**
 * RecentItem
 
 * @author Brian Cowdery
 * @since  07-12-2010
 */
class RecentItem implements Serializable {

    static mapping = {
        id generator: 'org.hibernate.id.enhanced.TableGenerator',
           params: [
           table_name: 'jbilling_seqs',
           segment_column_name: 'name',
           value_column_name: 'next_id',
           segment_value: 'recent_item'
           ]
    }

    static constraints = {
        userId(nullable: false)
        objectId(nullable: false)
        type(nullable: false)
    }

    Integer userId
    Integer objectId
    RecentItemType type

    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        RecentItem that = (RecentItem) o;

        if (objectId != that.objectId) return false;
        if (type != that.type) return false;
        if (userId != that.userId) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    def String toString() {
        return "RecentItem{id=${id}, type=${type}, objectId=${objectId}}"
    }
}
