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

import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpSession
import com.sapienter.jbilling.server.item.db.ItemDTO
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.item.db.ItemTypeDTO
import com.sapienter.jbilling.server.item.ItemTypeBL
import java.io.Serializable

class ProductService implements Serializable {

    static transactional = true

    def messageSource

    /**
     * Returns a list of products filtered by simple criteria. The given filterBy parameter will
     * be used match either the ID, internalNumber or description of the product. The typeId parameter
     * can be used to restrict results to a single product type.
     *
     * @param company company
     * @param params parameter map containing filter criteria
     * @return filtered list of products
     */
    def getFilteredProducts(CompanyDTO company, GrailsParameterMap params) {

        // default filterBy message used in the UI
        def defaultFilter = messageSource.resolveCode('products.filter.by.default', session.locale).format((Object[]) [])

        // filter on item type, item id and internal number
        def products = ItemDTO.createCriteria().list() {
            and {
                if (params.filterBy && params.filterBy != defaultFilter) {
                    or {
                        eq('id', params.int('filterBy'))
                        ilike('internalNumber', "%${params.filterBy}%")
                    }
                }

                if (params.typeId) {
                    itemTypes {
                        eq('id', params.int('typeId'))
                    }
                }

                eq('deleted', 0)
                eq('entity', company)
            }
            order('id', 'desc')
        }

        // if no results found, try filtering by description
        if (!products && params.filterBy) {
            products = ItemDTO.createCriteria().list() {
                and {
                    eq('deleted', 0)
                    eq('entity', company)
                }
                order('id', 'desc')
            }.findAll {
                it.getDescription(session['language_id']).toLowerCase().contains(params.filterBy.toLowerCase())
            }
        }

        return products
    }

    /**
     * Returns a list of visible item types.
     *
     * @return list of item types
     */
    def getItemTypes() {
        log.debug("getting item types")

        return ItemTypeDTO.createCriteria().list() {
            and {
                eq('entity', new CompanyDTO(session['company_id']))
            }
            order('id', 'desc')
        }
    }

    /**
     * Returns the HTTP session
     *
     * @return http session
     */
    def HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
    }
}
