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

package com.sapienter.jbilling.client.user

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.server.user.UserWS
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
import com.sapienter.jbilling.server.entity.CreditCardDTO
import com.sapienter.jbilling.common.Constants
import com.sapienter.jbilling.server.user.ContactWS
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.entity.AchDTO
import org.apache.log4j.Logger
import org.springframework.security.authentication.encoding.PasswordEncoder
import com.sapienter.jbilling.server.util.Context

/**
 * UserHelper 
 *
 * @author Brian Cowdery
 * @since 04/04/11
 */
class UserHelper {

    private static def log = Logger.getLogger(this)

    /**
     * Constructs a UserWS object and populates it with data from the given parameter map. The user
     * and all associated objects (ContactWS, CreditCarDTO, AchDTO, custom contact fields etc.) are
     * also bound as needed.
     *
     * @param user user object to bind parameters to
     * @param params parameters to bind
     * @return bound UserWS object
     */
    static def UserWS bindUser(UserWS user, GrailsParameterMap params) {
        bindData(user, params, 'user')

        // default main role to TYPE_CUSTOMER if not set
        if (!user.mainRoleId) {
            user.setMainRoleId(Constants.TYPE_CUSTOMER)
        }

        log.debug("User ${user}")

        // bind credit card object if parameters present
        if (params.creditCard.any { key, value -> value }) {
            def creditCard = new CreditCardDTO()
            bindData(creditCard, params, 'creditCard')
            bindExpiryDate(creditCard, params)
            user.setCreditCard(creditCard)

            log.debug("Credit card ${creditCard}")

            // set automatic payment type
            if (params.creditCardAutoPayment)
                user.setAutomaticPaymentType(Constants.AUTO_PAYMENT_TYPE_CC)
        }

        // bind ach object if parameters present
        if (params.ach.any { key, value -> value }) {
            def ach = new AchDTO()
            bindData(ach, params, 'ach')
            user.setAch(ach)

            log.debug("Ach ${ach}")

            // set automatic payment type
            if (params.achAutoPayment)
                user.setAutomaticPaymentType(Constants.AUTO_PAYMENT_TYPE_ACH)
        }

        return user
    }

    /**
     * Binds user contacts. The given UserWS object will be populated with the primary contact type, and the
     * given list will be populated with all remaining bound secondary contacts.
     *
     * @param user user object to bind primary contact to
     * @param contacts list to populate with remaining secondary contacts
     * @param company company
     * @param params parameters to bind
     * @return list of bound secondary contact types
     */
    static def Object[] bindContacts(UserWS user, List contacts, CompanyDTO company, GrailsParameterMap params) {
        def contactTypes = company.contactTypes
        def primaryContactTypeId = params.int('primaryContactTypeId')

        // bind primary user contact and custom contact fields
        def contact = new ContactWS()
        bindData(contact, params, "contact-${params.primaryContactTypeId}")
        contact.type = primaryContactTypeId
		contact.include = params.get("contact-${params.primaryContactTypeId}.include") ? 1 : 0

        if (params.contactField) {
            contact.fieldIDs = new Integer[params.contactField.size()]
            contact.fieldValues = new Integer[params.contactField.size()]
            params.contactField.eachWithIndex { id, value, i ->
                contact.fieldIDs[i] = id.toInteger()
                contact.fieldValues[i] = value
            }
        }

        user.setContact(contact)

        log.debug("Primary contact: ${contact}")


        // bind secondary contact types
        contactTypes.findAll{ it.id != primaryContactTypeId }.each{
            // bind if contact object if parameters present
            if (params["contact-${it.id}"].any { key, value -> value }) {
                def otherContact = new ContactWS()
                bindData(otherContact, params, "contact-${it.id}")
                otherContact.type = it.id

				//checkbox values are not bound automatically since it throws a data conversion error
				otherContact.include = params.get("contact-${it.id}.include") ? 1 : 0

                contacts << otherContact;
            }
        }

        log.debug("Secondary contacts: ${contacts}")

        return contacts;
    }


    /**
     * Binds the password parameters to the given new user object, ensuring that the password entered is
     * valid and that if the user already exists that the old password is verified before changing.
     *
     * @param newUser user to bind password to
     * @param oldUser existing user (may be null)
     * @param params parameters to bind
     */
    static def bindPassword(UserWS newUser, UserWS oldUser, GrailsParameterMap params, flash) {
        if (oldUser) {
            // validate that the entered confirmation password matches the users existing password
            if (params.newPassword) {
                PasswordEncoder passwordEncoder = Context.getBean(Context.Name.PASSWORD_ENCODER)
                if (!passwordEncoder.isPasswordValid(oldUser.password, params.oldPassword, null)) {
                    flash.error = 'current.password.doesnt.match.existing'
                    return
                }
            }
        } else {
            // validate that the new user has a password
            if (!params.newPassword) {
                flash.error = 'password.required'
                return
            }
        }

        // verify passwords
        if (params.newPassword == params.verifiedPassword) {
            if (params.newPassword) newUser.setPassword(params.newPassword)

        } else {
            flash.error = 'passwords.dont.match'
        }
    }

    private static def bindExpiryDate(CreditCardDTO creditCard, GrailsParameterMap params) {
        Integer expiryMonth = params.int('expiryMonth')
        Integer expiryYear = params.int('expiryYear')

        if (expiryMonth && expiryYear)  {
            Calendar calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(Calendar.MONTH, expiryMonth - 1) // calendar API months start at 0
            calendar.set(Calendar.YEAR, expiryYear)

            creditCard.expiry = calendar.getTime()
        }
    }

    private static def bindData(Object model, modelParams, String prefix) {
        def args = [ model, modelParams, [exclude:[], include:[]]]
        if (prefix) args << prefix

        new BindDynamicMethod().invoke(model, 'bind', (Object[]) args)
    }

    static def getDisplayName(user, contact) {
        if (contact?.firstName || contact?.lastName) {
            return "${contact.firstName} ${contact.lastName}".trim()

        } else if (contact?.organizationName) {
            return "${contact.organizationName}".trim()
        }

        return user?.userName
    }
}
