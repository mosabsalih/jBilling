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

import java.text.SimpleDateFormat
import javax.servlet.http.HttpSession
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder
import java.io.Serializable
import com.sapienter.jbilling.client.filters.FilterFactory
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

/**
 * FilterService

 * @author Brian Cowdery
 * @since  30-11-2010
 */
class FilterService implements Serializable {

    private static final String SESSION_CURRENT_FILTER_TYPE = "current_filter_type";

    /**
     * Fetches the filters for the given type and sets the filter values from the UI
     * input fields if the "applyFilter" request parameter is present.
     *
     * Filters are available in the session (and can also be set elsewhere in the session)
     * using the key FilterType.name() + "_FILTERS".
     *
     * @param type filter type
     * @param params request parameters from controller action
     * @return filters for the given filter type
     */
    def Object getFilters(FilterType type, GrailsParameterMap params) {
        def session = getSession()
        def key = getSessionKey(type)

        // clear filter values when switching filter types
        def currentType = session[SESSION_CURRENT_FILTER_TYPE]
        if (currentType && type != currentType) {
            getCurrentFilters()?.each { it.clear() }
        }

        /*
            Fetch filters for the given filter type. If the filters are already
            in the session, use the existing filters instead of fetching new ones.
         */
        def filters = session[key] ?: FilterFactory.getFilters(type)

        // update filters with values from request parameters
        if (params?.boolean("applyFilter")) {
            filters.each { it.clear() }
            params.filters.each{ filterName, filterParams ->
                if (filterParams instanceof Map) {
                    def filter = filters.find{ it.name == filterName }
                    bindData(filter, filterParams, null);
                }
            }
        }

        // store filters in session for next request
        session[SESSION_CURRENT_FILTER_TYPE] = type;
        session[key] = filters
        return filters
    }

    /**
     * Adds a filter to the session list for the given type. If the filter already exists
     * the current filter will be replaced.
     *
     * Note that this is not persisted across sessions unless the user saves the filter set.
     *
     * @param type type of filter list
     * @param filter filter with value to set
     * @param reset reset other filters by clearing their value, optional; defaults to true
     */
    def void setFilter(FilterType type, Filter filter, boolean reset = true) {
        def filters = getFilters(type, null)

        if (filters) {
            def index = filters.indexOf(filter)
            if (index >= 0) {
                filters.putAt(index, filter)
            } else {
                filters.add(filter)
            }

            // clear all other filters values
            if (reset) {
                filters.each {
                    if (!it.equals(filter)) {
                        it.clear()
                    }
                }
            }

            session[getSessionKey(type)] = filters
        }
    }

    /**
     * Returns the current filters based on the last set used. For example, if you had previously
     * fetched customer filters, this method would return the customer filters.
     *
     * @return current filter list
     */
    def Object getCurrentFilters() {
        def type = (FilterType) session[SESSION_CURRENT_FILTER_TYPE]
        return type ? getFilters(type, null) : null
    }

    /**
     * Loads the filters for the given FilterSet id, updating the filter list
     * in the session for current usage.
     *
     * @param filterSetId filter set id
     * @return filter list
     */
    def Object loadFilters(Integer filterSetId) {
        def filterset = FilterSet.get(filterSetId)
        def type = (FilterType) session[SESSION_CURRENT_FILTER_TYPE]
        session[getSessionKey(type)] = filterset.filters
        return filterset.filters
    }

    /**
     * Changes the visibility of a filter so that it appears in the filter pane.
     *
     * @param name filter name to show
     * @return updated filter list
     */
    def Object showFilter(String name) {
        def filters = getCurrentFilters()
        filters?.each{
            if (it.name == name)
                it.visible = true
        }

        def type = (FilterType) session[SESSION_CURRENT_FILTER_TYPE]
        session[getSessionKey(type)] = filters
        return filters
    }

    /**
     * Changes the visibility of the filter so that it is removed from the filter pane. This
     * method also clears the filter's set value so that it's effect on the entity criteria
     * will be removed.
     *
     * @param name filter name to remove
     * @return updated filter list
     */
    def Object removeFilter(String name) {
        def filters = getCurrentFilters()
        filters?.each{
            if (it.name == name) {
                it.visible = false
                it.clear()
            }
        }

        def type = (FilterType) session[SESSION_CURRENT_FILTER_TYPE]
        session[getSessionKey(type)] = filters
        return filters
    }

    /**
     * Returns the HTTP session
     *
     * @return http session
     */
    def HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
    }

    /**
     * Returns the session attribute key for the given set of filters.
     *
     * @param type filter type
     * @return session attribute key
     */
    def String getSessionKey(FilterType type) {
        return "${type.name()}_FILTERS"
    }

    private static def bindData(Object model, modelParams, String prefix) {
        if (model != null) {
            def args = [ model, modelParams, [exclude:[], include:[]]]
            if (prefix) args << prefix

            new BindDynamicMethod().invoke(model, 'bind', (Object[]) args)
        }
    }
}
