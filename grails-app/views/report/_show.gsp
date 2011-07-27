%{--
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
  --}%

<%@ page import="com.sapienter.jbilling.server.report.ReportExportFormat"%>

<%--
  Report details template.

  @author Brian Cowdery
  @since  07-Mar-2011
--%>

<div class="column-hold">
    <div class="heading">
        <strong><g:message code="${selected.name}"/></strong>
    </div>

    <g:form name="run-report-form" url="[action: 'run', id: selected.id]" target="_blank" method="GET">
        <div class="box">
            <!-- report info -->
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                <tr>
                    <td><g:message code="report.label.id"/></td>
                    <td class="value">${selected.id}</td>
                </tr>
                <tr>
                    <td><g:message code="report.label.type"/></td>
                    <td class="value">${selected.type.getDescription(session['language_id'])}</td>
                </tr>
                <tr>
                    <td><g:message code="report.label.design"/></td>
                    <td class="value">
                        <em title="${selected.reportFilePath}">${selected.fileName}</em>
                    </td>
                </tr>
                </tbody>
            </table>

            <!-- report description -->
            <p class="description">
                ${selected.getDescription(session['language_id'])}
            </p>

            <hr/>

            <!-- report parameters -->
            <g:render template="/report/${selected.type.name}/${selected.name}"/>

            <br/>&nbsp;
        </div>

        <div class="btn-box">
            <a class="submit edit" onclick="$('#run-report-form').submit();">
                <span><g:message code="button.run.report"/></span>
            </a>

            <span>
                <g:select name="format"
                          from="${ReportExportFormat.values()}"
                          noSelection="['': message(code: 'report.format.HTML')]"
                          valueMessagePrefix="report.format"/>
            </span>
        </div>

    </g:form>
</div>

