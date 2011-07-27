
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
  Report types list.

  @author Brian Cowdery
  @since  07-Mar-2011
--%>

<div class="table-box">
    <div class="table-scroll">
        <table id="report-types" cellspacing="0" cellpadding="0">
            <thead>
                <tr>
                    <th><g:message code="report.th.type"/></th>
                    <th class="small"><g:message code="report.th.count"/></th>
                </tr>
            </thead>
            <tbody>

            <g:each var="type" in="${types}">

                <tr id="type-${type.id}" class="${selectedTypeId == type.id ? 'active' : ''}">
                    <td>
                        <g:remoteLink class="cell double" action="reports" id="${type.id}" before="register(this);" onSuccess="render(data, next);">
                            <strong>${type.getDescription(session['language_id'])}</strong>
                            <em></em>
                        </g:remoteLink>
                    </td>
                    <td class="small">
                        <g:remoteLink class="cell" action="reports" id="${type.id}" before="register(this);" onSuccess="render(data, next);">
                            <span>${type.reports.size()}</span>
                        </g:remoteLink>
                    </td>
                </tr>

            </g:each>

            </tbody>
        </table>
    </div>
</div>

<div class="btn-box">
    <div class="row"></div>
</div>



