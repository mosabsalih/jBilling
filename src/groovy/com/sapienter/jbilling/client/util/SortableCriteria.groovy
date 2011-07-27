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

package com.sapienter.jbilling.client.util

import grails.orm.HibernateCriteriaBuilder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

/**
 * Sortable 
 *
 * @author Brian Cowdery
 * @since 08/06/11
 */
class SortableCriteria {

    static def sort(GrailsParameterMap params, builder) {
        def sort = params.sort?.tokenize(',')?.collect { it.trim() }

        if (params.alias) {
            // explicit alias definitions
            params.alias.each { alias, aliasPath ->
                builder.createAlias(aliasPath, alias)
            }

        } else {
            // try and automatically add aliases for sorted associations
            def associations = sort.findAll{ it.contains('.') }
            associations.collect{ it.substring(0, it.indexOf('.')) }.unique().each {
                builder.createAlias(it, it)
            }
        }

        // add order by clauses
        sort.each {
            builder.order(it, params.order)
        }
    }

}

