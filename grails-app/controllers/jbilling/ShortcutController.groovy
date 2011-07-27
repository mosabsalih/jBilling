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

import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class ShortcutController {

	def breadcrumbService

	def index = {
		session['shortcuts'] = getShortcuts().asList()
        render template: "/layouts/includes/shortcuts"
    }

	/**
	 * Returns a list of user's shortcuts.
	 *
	 * @return list of user's Shortcuts
	 */
	def Object getShortcuts() {
		return Shortcut.withCriteria {
			eq("userId", session["user_id"])
			order("id", "asc")
		}
	}

	def add = {
        def crumbs = breadcrumbService.getBreadcrumbs()
		def lastCrumb= !crumbs.isEmpty() ? crumbs.getAt(-1) : null

		if (lastCrumb) {
			def shortcuts= getShortcuts().asList()
			Shortcut shortcut= new Shortcut(controller: lastCrumb.controller, action: lastCrumb.action, name: lastCrumb.name, objectId: lastCrumb.objectId)
			shortcut.userId= session['user_id']
			
			Shortcut exists= shortcuts.find { it == shortcut }
			if (exists) {
				log.debug "${exists.id}"
				flash.info = 'shortcuts.save.exists'
			} else {
				shortcut.save()
				flash.message = 'shortcuts.save.success'
			}
		}
   }
	
	def remove = {
		def shortcuts= getShortcuts().asList()
		Shortcut exists= shortcuts.find { it.id == params.id as Integer}
		if (exists) {
			exists.delete()
			log.info shortcuts.remove(exists) 
			flash.message = 'shortcuts.remove.success'
		}
		log.info shortcuts.size
		session['shortcuts']= shortcuts
		//render template: "/layouts/includes/shortcuts"
	}
}
