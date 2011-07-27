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

import com.sapienter.jbilling.server.report.db.ReportDTO
import grails.plugins.springsecurity.Secured

import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.report.db.ReportTypeDTO
import com.sapienter.jbilling.server.report.ReportBL
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import com.sapienter.jbilling.server.report.ReportExportFormat
import com.sapienter.jbilling.client.util.DownloadHelper
import com.sapienter.jbilling.server.report.db.ReportParameterDTO

/**
 * ReportController 
 *
 * @author Brian Cowdery
 * @since 07/03/11
 */
@Secured(["MENU_96"])
class ReportController {

    static pagination = [ max: 10, offset: 0 ]

    def viewUtils
    def filterService
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def getReportTypes() {
        return ReportTypeDTO.list()
    }

    def getReports(Integer typeId) {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset

        return ReportDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {

            if (typeId) {
                eq('type.id', typeId)
            }

            entities {
                eq('id', session['company_id'])
            }

            order('id', 'desc')
        }
    }

    def list = {
        def types = getReportTypes()
        def reports = params.id ? getReports(params.int('id')) : null
        def type = params.id ? reports.get(0)?.type : null

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, params.int('id'), type?.getDescription(session['language_id']))

        render view: 'list', model: [ types: types, reports: reports, selectedTypeId: type?.id ]
    }

    def reports = {
        def typeId = params.int('id')
        def reports = typeId ? getReports(typeId) : null

        breadcrumbService.addBreadcrumb(controllerName, 'list', null, typeId, reports?.get(0)?.type?.getDescription(session['language_id']))

        render template: 'reports', model: [ reports: reports, selectedTypeId: typeId ]
    }

    def allReports = {
        def reports = getReports(null)
        render template: 'reports', model: [ reports: reports ]
    }

    def show = {
        ReportDTO report = ReportDTO.get(params.int('id'))
        breadcrumbService.addBreadcrumb(controllerName, actionName, null, report?.id, report ? message(code: report.name) : null)

        if (params.template) {
            // render requested template, usually "_show.gsp"
            render template: params.template, model: [ selected: report ]

        } else {
            // render default "list" view - needed so a breadcrumb can link to a reports by id
            def typeId = report?.type?.id
            def types = getReportTypes()
            def reports = getReports(typeId)

            render view: 'list', model: [ types: types, reports: reports, selected: report, selectedTypeId: typeId ]
        }
    }

    /**
     * Runs the given report using the entered report parameters. If no format is selected, the report
     * will be rendered as HTML. If an export format is selected, then the generated file will be sent
     * to the browser.
     */
    def run = {
        def report = ReportDTO.get(params.int('id'))
        bindParameters(report, params)

        def runner = new ReportBL(report, session['locale'], session['company_id'])

        if (params.format) {
            // export to selected format
            def format = ReportExportFormat.valueOf(params.format)
            def export = runner.export(format);
            DownloadHelper.sendFile(response, export.fileName, export.contentType, export.bytes)

        } else {
            // render as HTML
            def imageUrl = createLink(controller: 'report', action: 'images', params: [name: '']).toString()
            runner.renderHtml(response, session, imageUrl)
        }
    }

    /**
     * Returns image data generated by the jasper report HTML rendering.
     *
     * Rendering a jasper report to HTML produces a map of images that is stored in the session. This action
     * retrieves images by name and returns the bytes to the browser. The jasper report HTML contains <code>img</code>
     * tags that look to this action as their source.
     */
    def images = {
        Map images = session[ReportBL.SESSION_IMAGE_MAP]
        response.outputStream << images.get(params.name)
    }

    def bindParameters(ReportDTO report, GrailsParameterMap params) {
        params.each { name, value ->
            ReportParameterDTO<?> parameter = report.getParameter(name)
            if (parameter) bindData(parameter, ['value': value])
        }
    }
}
