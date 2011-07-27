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
enum SearchType {
    CUSTOMERS, ORDERS, INVOICES, PAYMENTS, BILLINGPROCESS, MEDIATIONPROCESS
}

class SearchCommand {
    Integer id
    String type
}

/**
 * SearchController
 *
 * @author Brian Cowdery
 * @since 15-Dec-2010
 */
class SearchController {

    def filterService
    def recentItemService
    def breadcrumbService

    def index = { SearchCommand cmd ->

        // add a filter to limit the list by the ID searched
        def filter = new Filter(type: FilterType.ALL, constraintType: FilterConstraint.EQ, field: 'id', template: 'id', visible: true, integerValue: cmd.id)

        // redirect to the controller of the type being searched
        def type = Enum.valueOf(SearchType.class, cmd.type)
        switch (type) {
            case SearchType.CUSTOMERS:
                filterService.setFilter(FilterType.CUSTOMER, filter)
                redirect(controller: 'customer', action: 'list', id: cmd.id)
                break

            case SearchType.ORDERS:
                filterService.setFilter(FilterType.ORDER, filter)
                redirect(controller: 'order', action: 'list', id: cmd.id)
                break

            case SearchType.INVOICES:
                filterService.setFilter(FilterType.INVOICE, filter)
                redirect(controller: 'invoice', action: 'list', id: cmd.id)
                break

            case SearchType.PAYMENTS:
                filterService.setFilter(FilterType.PAYMENT, filter)
                redirect(controller: 'payment', action: 'list', id: cmd.id)
                break
				
			case SearchType.BILLINGPROCESS:
				filterService.setFilter(FilterType.BILLINGPROCESS, filter)
				redirect(controller: 'billing', action: 'index', id: cmd.id)
				break
				
			case SearchType.MEDIATIONPROCESS:
				filterService.setFilter(FilterType.MEDIATIONPROCESS, filter)
				redirect(controller: 'mediation', action: 'index', id: cmd.id)
				break
        }
    }
}
