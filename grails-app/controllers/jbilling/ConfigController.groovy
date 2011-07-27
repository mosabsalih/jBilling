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

import com.sapienter.jbilling.common.SessionInternalError
import com.sapienter.jbilling.server.process.AgeingWS
import com.sapienter.jbilling.server.user.contact.db.ContactDTO
import com.sapienter.jbilling.server.user.contact.db.ContactMapDTO
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.Constants
import com.sapienter.jbilling.server.util.PreferenceTypeWS
import com.sapienter.jbilling.server.util.PreferenceWS
import com.sapienter.jbilling.server.util.db.PreferenceTypeDTO
import com.sapienter.jbilling.common.Util
import grails.plugins.springsecurity.Secured
import com.sapienter.jbilling.server.util.db.CurrencyDTO
import com.sapienter.jbilling.server.item.CurrencyBL
import com.sapienter.jbilling.server.util.CurrencyWS
import com.sapienter.jbilling.server.user.ContactWS
import com.sapienter.jbilling.server.user.CompanyWS

/**
 * ConfigurationController 
 *
 * @author Brian Cowdery
 * @since 03-Jan-2011
 */
@Secured(["MENU_99"])
class ConfigController {

    def breadcrumbService
	def webServicesSession
	def viewUtils
	def userSession

    /*
        Show/edit all preferences
     */

    def index = {
        def preferenceTypes = PreferenceTypeDTO.list()

        // show preference if given id
        def preferenceId = params.int('id')
        def selected = preferenceId ? preferenceTypes.find { it.id == preferenceId } : null

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)

        [ preferenceTypes : preferenceTypes, selected : selected ]
    }

    def show = {
        def selected = PreferenceTypeDTO.get(params.int('id'))

        render template: 'show', model: [ selected : selected ]
    }

    def save = {
        def type = new PreferenceTypeWS()
        bindData(type, params, 'type')

        def preference = new PreferenceWS()
        bindData(preference, params, 'preference')
        preference.preferenceType = type

        try {
            webServicesSession.updatePreference(preference)

            flash.message = 'preference.updated'
            flash.args = [ type.id ]

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
        }

        redirect action: index, id: type.id
    }


    /*
        Ageing configuration
     */

	
	def aging = {
		log.debug "config.aging ${session['language_id']}"
		AgeingWS[] array= webServicesSession.getAgeingConfiguration(session['language_id'] as Integer)
		breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)
		def gracePeriod= userSession.getEntityPreference(session['company_id'] as Integer, Constants.PREFERENCE_GRACE_PERIOD)
		[ageingSteps: array, gracePeriod:gracePeriod]
	}
	
	def saveAging = {
		
		def cnt = params.recCnt.toInteger()
		log.debug "Records Count: ${cnt}"
		
		AgeingWS[] array= new AgeingWS[cnt]
		for (int i=0; i < cnt; i++) {
			log.debug "${params['obj[' + i + '].statusId']}"
			AgeingWS ws= new AgeingWS()
			bindData(ws, params["obj["+i+"]"])
			array[i]= ws
		}
		
		for (AgeingWS dto: array) { 
			log.debug "Printing: ${dto.toString()}"
		}
		try {
			webServicesSession.saveAgeingConfiguration(array, params.int('gracePeriod'), session['language_id'] as Integer)
			flash.message= 'config.ageing.updated'
		} catch (SessionInternalError e){
			viewUtils.resolveException(flash, session.locale, e);
		} catch (Exception e) {
			log.error e.getMessage()
			flash.error = 'config.error.saving.ageing'
		}
		redirect (action: 'aging')
	}

    /*
        Company configuration    
     */
    
	def company = {
		CompanyWS company = webServicesSession.getCompany()
		breadcrumbService.addBreadcrumb(controllerName, actionName, null, null)

		[ company: company ]
	}
	
	def saveCompany= {
		try {
			CompanyWS company= new CompanyWS(session['company_id'].intValue())

			// Contact Type 1 is always Company Contact
			ContactWS contact = new ContactWS()
			bindData(company, params, ['id'])
			bindData(contact, params, ['id'])
            company.setContact(contact)

            webServicesSession.updateCompany(company)
            
			flash.message = 'config.company.save.success'

		} catch (SessionInternalError e){
			viewUtils.resolveException(flash, session.locale, e);

		} catch (Exception e) {
			flash.error = 'config.company.save.error'
		}

		redirect action: company
	}

    /*
        Invoice display configuration
     */

    def invoice = {
        def number = webServicesSession.getPreference(Constants.PREFERENCE_INVOICE_NUMBER)
        def prefix = webServicesSession.getPreference(Constants.PREFERENCE_INVOICE_PREFIX)

        [ number: number, prefix: prefix, logoPath: entityLogoPath ]
    }

    def entityLogo = {
        def logo = new File(getEntityLogoPath())
        response.outputStream << logo.getBytes()
    }

    def saveInvoice = {
        def number = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_INVOICE_NUMBER), value: params.number)
        def prefix = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_INVOICE_PREFIX), value: params.prefix)

        try {
            webServicesSession.updatePreferences((PreferenceWS[]) [ number, prefix ])

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
            render view: 'invoice', model: [ number: number, prefix: prefix, logoPath: entityLogoPath ]
            return
        }

        // save uploaded file
        def logo = request.getFile('logo');
        if (!logo.empty) {
            logo.transferTo(new File(getEntityLogoPath()))
        }

        flash.message = 'preferences.updated'
        redirect action: invoice
    }

    def String getEntityLogoPath() {
        return Util.getSysProp("base_dir") + "${File.separator}logos${File.separator}entity-${session['company_id']}.jpg"
    }


    /*
        Currencies
     */

    def currency = {
        def currency = new CurrencyBL()

        def entityCurrency = currency.getEntityCurrency(session['company_id'])
        def currencies = currency.getCurrencies(session['language_id'], session['company_id'])


        [ entityCurrency: entityCurrency, currencies: currencies ]
    }

    def saveCurrencies = {
        def defaultCurrencyId = params.int('defaultCurrencyId')

        // build a list of currencies
        def currencies = []
        params.currencies.each { k, v ->
            if (v instanceof Map) {
                def currency = new CurrencyWS()
                bindData(currency, v, ['_inUse'])
                currency.defaultCurrency = (currency.id == defaultCurrencyId)

                currencies << currency
            }
        }

        // update all currencies
        try {
            webServicesSession.updateCurrencies((CurrencyWS[]) currencies)

            flash.message = 'currencies.updated'

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
        }

        redirect action: 'currency'
    }

    def editCurrency = {
        // only shows edit template to create new currencies.
        // currencies can be edited from the main currency config form
        render template: 'currency/edit', model: [ currency: null ]
    }

    def saveCurrency = {
        def currency = new CurrencyWS()
        bindData(currency, params)

        try {
            webServicesSession.createCurrency(currency)

            flash.message = 'currency.created'
            flash.args = [ currency.code ]

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
            chain action: 'currency', model: [ currency: currency ]
            return
        }

        redirect action: 'currency'
    }


    /*
        Email settings
     */

    def email = {
        def selfDeliver = webServicesSession.getPreference(Constants.PREFERENCE_PAPER_SELF_DELIVERY)
        def customerNotes = webServicesSession.getPreference(Constants.PREFERENCE_TYPE_INCLUDE_CUSTOMER_NOTES)
        def daysForNotification1 = webServicesSession.getPreference(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1)
        def daysForNotification2 = webServicesSession.getPreference(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S2)
        def daysForNotification3 = webServicesSession.getPreference(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S3)
        def useInvoiceReminders = webServicesSession.getPreference(Constants.PREFERENCE_USE_INVOICE_REMINDERS)
        def firstReminder = webServicesSession.getPreference(Constants.PREFERENCE_FIRST_REMINDER)
        def nextReminder = webServicesSession.getPreference(Constants.PREFERENCE_NEXT_REMINDER)

        [
                selfDeliver: selfDeliver,
                customerNotes: customerNotes,
                daysForNotification1: daysForNotification1,
                daysForNotification2: daysForNotification2,
                daysForNotification3: daysForNotification3,
                useInvoiceReminders: useInvoiceReminders,
                firstReminder: firstReminder,
                nextReminder: nextReminder
        ]
    }


    def saveEmail = {
        def selfDeliver = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_PAPER_SELF_DELIVERY), value: params.selfDeliver ? '1' : '0')
        def customerNotes = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_TYPE_INCLUDE_CUSTOMER_NOTES), value: params.customerNotes ? '1' : '0')
        def daysForNotification1 = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1), value: params.daysForNotification1)
        def daysForNotification2 = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S2), value: params.daysForNotification2)
        def daysForNotification3 = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S3), value: params.daysForNotification3)
        def useInvoiceReminders = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_USE_INVOICE_REMINDERS), value: params.useInvoiceReminders ? '1' : '0')
        def firstReminder = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_FIRST_REMINDER), value: params.firstReminder)
        def nextReminder = new PreferenceWS(preferenceType: new PreferenceTypeWS(id: Constants.PREFERENCE_NEXT_REMINDER), value: params.nextReminder)

        try {
            webServicesSession.updatePreferences((PreferenceWS[]) [ selfDeliver, customerNotes, daysForNotification1, daysForNotification2, daysForNotification3, useInvoiceReminders, firstReminder, nextReminder ])

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
            render view: 'email', model: [
                   selfDeliver: selfDeliver,
                   customerNotes: customerNotes,
                   daysForNotification1: daysForNotification1,
                   daysForNotification2: daysForNotification2,
                   daysForNotification3: daysForNotification3,
                   useInvoiceReminders: useInvoiceReminders,
                   firstReminder: firstReminder,
                   nextReminder: nextReminder
                   ]
            return
        }

        flash.message = 'preferences.updated'
        redirect action: email
    }
}
