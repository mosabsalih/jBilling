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

import com.sapienter.jbilling.server.util.PreferenceBL
import org.springframework.dao.EmptyResultDataAccessException
import org.hibernate.ObjectNotFoundException
/**
 * PreferencesTagLib 
 *
 * @author Brian Cowdery
 * @since 12/01/11
 */
class PreferencesTagLib {

    /**
     * Prints the preference value
     *
     * @param preferenceId ID of the preference to check
     */
    def preference = { attrs, body ->

        def preferenceId = assertAttribute('preferenceId', attrs, 'hasPreference') as Integer

        try {
            PreferenceBL preference = new PreferenceBL(session['company_id'], preferenceId);
            if (!preference.isNull())
                out << preference.getValueAsString()

        } catch (EmptyResultDataAccessException e) {
            /* ignore */
        } catch (ObjectNotFoundException e) {
            /* ignore */
        }
    }

    /**
     * Prints the tag body if the preference exists and is not null.
     *
     * @param preferenceId ID of the preference to check
     */
    def hasPreference = { attrs, body ->

        def preferenceId = assertAttribute('preferenceId', attrs, 'hasPreference') as Integer

        try {
            PreferenceBL preference = new PreferenceBL(session['company_id'], preferenceId);
            if (!preference.isNull())
                out << body()

        } catch (EmptyResultDataAccessException e) {
            /* ignore */
        } catch (ObjectNotFoundException e) {
            /* ignore */
        }
    }

    /**
     * Prints the tag body if the preference value or preference type default equals the given value.
     *
     * @param preferenceId ID of the preference to check
     * @param value to compare
     */
    def preferenceEquals = { attrs, body ->

        def preferenceId = assertAttribute('preferenceId', attrs, 'preferenceEquals') as Integer
        def value = assertAttribute('value', attrs, 'preferenceEquals') as String

        try {
            PreferenceBL preference = new PreferenceBL(session['company_id'], preferenceId)
            if (preference.getValueAsString().equals(value))
                out << body()

        } catch (EmptyResultDataAccessException e) {
            /* ignore */
            log.debug("empty result data access exception")

        } catch (ObjectNotFoundException e) {
            /* ignore */
            log.debug("object not found exception")
        }
    }

    /**
     * Prints the tag body if the preference value is equal, or if the preference is not set and has no
     * default value for the preference type. Useful for "default if not set" style preferences.
     *
     * @param preferenceId ID of the preference to check
     * @param value to compare
     */
    def preferenceIsNullOrEquals = { attrs, body ->

        def preferenceId = assertAttribute('preferenceId', attrs, 'preferenceEquals') as Integer
        def value = assertAttribute('value', attrs, 'preferenceEquals') as String

        try {
            PreferenceBL preference = new PreferenceBL(session['company_id'], preferenceId)
            if (!preference.isNull() && !preference.getValueAsString().equals(value))
                return

        } catch (EmptyResultDataAccessException e) {
            /* ignore */
        } catch (ObjectNotFoundException e) {
            /* ignore */
        }

        out << body()
    }

    protected assertAttribute(String name, attrs, String tag) {
        if (!attrs.containsKey(name)) {
            throwTagError "Tag [$tag] is missing required attribute [$name]"
        }
        attrs.remove name
    }
}
