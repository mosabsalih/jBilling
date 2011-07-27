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
 * FilterSet
 
 * @author Brian Cowdery
 * @since  03-12-2010
 */
class FilterSet implements Serializable {

    static mapping = {
        id generator: 'org.hibernate.id.enhanced.TableGenerator',
            params: [
                table_name: 'jbilling_seqs',
                segment_column_name: 'name',
                value_column_name: 'next_id',
                segment_value: 'filter_set'
            ]
        filters cascade: "all,delete-orphan"
    }

    static hasMany = [filters: Filter]   

    String name
    Integer userId

    public String toString ( ) {
        return "FilterSet{id=${id}, name=${name}, userId=${userId}, filters=${filters?.size()}}"
    }
}
