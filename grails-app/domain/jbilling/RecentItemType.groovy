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
 * RecentItemType
 
 * @author Brian Cowdery
 * @since  07-12-2010
 */
enum RecentItemType {

    INVOICE             ("invoice", "list", null, "icon09.gif", "recent.item.invoice.title"),
    ORDER               ("order", "showListAndOrder", null, "icon10.gif", "recent.item.order.title"),
    PRODUCT             ("product", "show", null, "icon13.gif", "recent.item.product.title"),
    CUSTOMER            ("customer", "list", null, "icon12.gif", "recent.item.customer.title"),
    PAYMENT             ("payment", "list", null, "icon11.gif", "recent.item.payment.title"),
    PLUGIN              ("plugin", "list", null, "icon13.gif", "recent.item.plugin.title"),
    BILLINGPROCESS      ("billing", "show", null, "icon13.gif", "recent.item.billing.process.title"),
	MEDIATIONPROCESS    ("mediation", "show", null, "icon13.gif", "recent.item.mediation.process.title");

    String controller
    String action
    Map params
    String icon
    String messageCode

    def RecentItemType(controller, action, params, icon, messageCode) {
        this.controller = controller
        this.action = action
        this.params = params
        this.icon = icon
        this.messageCode = messageCode
    }
}
