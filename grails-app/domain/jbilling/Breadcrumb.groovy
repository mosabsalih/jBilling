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
 * History breadcrumb.
 *
 * Breadcrumbs represent viewable pages within jBilling. This does not include ajax actions and other
 * requests that cannot be reached by a URI (i.e., breadcrumbs can be bookmarked).
 *
 * @author Brian Cowdery
 * @since 14-Dec-2010
 */
class Breadcrumb implements Serializable {

    static transients = [ "messageCode" ]

    static mapping = {
        id generator: 'org.hibernate.id.enhanced.TableGenerator',
           params: [
           table_name: 'jbilling_seqs',
           segment_column_name: 'name',
           value_column_name: 'next_id',
           segment_value: 'breadcrumb'
           ]
    }

    static constraints = {
        action(blank: true, nullable: true)
        name(blank: true, nullable: true)
        objectId(nullable: true)
        description(nullable: true)
    }

    Integer userId
    String controller
    String action
    String name
    Integer objectId
    String description

    def String getMessageCode() {
        StringBuilder builder = new StringBuilder();

        builder.append("breadcrumb")
        if (controller) builder.append('.').append(controller)
        if (action) builder.append('.').append(action)
        if (name) builder.append('.').append(name)
        if (!name && objectId) builder.append('.id')

       return builder.toString()
    }

    @Override
    def boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        Breadcrumb that = (Breadcrumb) o;

        if (action != that.action) return false;
        if (controller != that.controller) return false;
        if (name != that.name) return false;
        if (objectId != that.objectId) return false;
        return true;
    }

    @Override
    def int hashCode() {
        int result;

        result = controller.hashCode();
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        return result;
    }

    @Override
    def String toString() {
        return "Breadcrumb{id=${id}, userId=${userId}, controller=${controller}, action=${action}, objectId=${objectId}, name=${name}, description=${description}}"
    }
}
