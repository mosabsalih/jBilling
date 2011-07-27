
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

<%--
  Reports list

  @author Brian Cowdery
  @since  07-Mar-2011
--%>

<%-- list of reports --%>
<g:if test="${reports}">
    <div class="table-box">
        <div class="table-scroll">
            <table id="reports" cellspacing="0" cellpadding="0">
                <thead>
                    <tr>
                        <th><g:message code="report.th.name"/></th>
                    </tr>
                </thead>
                <tbody>

                <g:each var="report" in="${reports}">

                    <tr id="report-${report.id}" class="${selected?.id == report.id ? 'active' : ''}">
                        <td>
                            <g:remoteLink class="cell double" action="show" id="${report.id}" params="[template: 'show']" before="register(this);" onSuccess="render(data, next);">
                                <strong><g:message code="${report.name}"/></strong>
                                <em>${report.fileName}</em>
                            </g:remoteLink>
                        </td>
                    </tr>

                </g:each>

                </tbody>
            </table>
        </div>
    </div>
</g:if>

<%-- no report to show --%>
<g:if test="${!reports}">
    <div class="heading"><strong><em><g:message code="report.type.no.reports.title"/></em></strong></div>
    <div class="box">
        <g:if test="${selectedTypeId}">
            <em><g:message code="report.type.no.reports.warning"/></em>
        </g:if>
        <g:else>
            <em><g:message code="report.type.not.selected.message"/></em>
        </g:else>
    </div>
</g:if>

<%-- list pager and buttons --%>
<div class="pager-box">
    <g:set var="paginateAction" value="${actionName == 'reports' ? 'reports' : 'allReports'}"/>

    <div class="row">
        <div class="results">
            <g:render template="/layouts/includes/pagerShowResults" model="[steps: [10, 20, 50], action: paginateAction, update: 'column2']"/>
        </div>
    </div>

    <div class="row">
        <util:remotePaginate controller="report" action="${paginateAction}" id="${selectedTypeId}" total="${reports?.totalCount ?: 0}" update="column2"/>
    </div>
</div>

<div class="btn-box">
    <g:remoteLink action="allReports" update="column2" class="submit show"><span><g:message code="button.show.all"/></span></g:remoteLink>
</div>
