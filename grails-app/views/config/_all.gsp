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

<%@ page import="com.sapienter.jbilling.server.util.Constants; org.apache.commons.lang.StringUtils; org.apache.commons.lang.WordUtils" contentType="text/html;charset=UTF-8" %>

<%--
  Shows a list of all preferences

  @author Brian Cowdery
  @since  01-Apr-2011
--%>

<div class="table-box">
    <table id="users" cellspacing="0" cellpadding="0">
        <thead>
            <tr>
                <th><g:message code="preference.th.type"/></th>
                <th class="medium2"><g:message code="preference.th.value"/></th>
            </tr>
        </thead>

        <tbody>
            <g:each var="type" in="${preferenceTypes}">
                <tr id="type-${type.id}" class="${selected?.id == type.id ? 'active' : ''}">
                    <td>
                        <g:remoteLink class="cell double" action="show" id="${type.id}" before="register(this);" onSuccess="render(data, next);">
                            <strong>${StringUtils.abbreviate(type.getDescription(session['language_id']), 50)}</strong>
                            <em>Id: ${type.id}</em>
                        </g:remoteLink>
                    </td>

                    <td class="medium2">
                        <g:remoteLink class="cell" action="show" id="${type.id}" before="register(this);" onSuccess="render(data, next);">

                            <g:if test="${type.preferences}">
                                %{
                                    def preference = type.preferences.find{
                                                        it.jbillingTable.name == Constants.TABLE_ENTITY && it.foreignId == session['company_id']
                                                    } ?: type.preferences.asList().first()
                                }%

                                ${preference.value}
                            </g:if>
                            <g:else>
                                ${type.defaultValue}
                            </g:else>

                        </g:remoteLink>
                    </td>
                </tr>
            </g:each>

        </tbody>
    </table>
</div>

<div class="btn-box">
    <g:remoteLink action='edit' class="submit add" before="register(this);" onSuccess="render(data, next);">
        <span><g:message code="button.create"/></span>
    </g:remoteLink>
</div>