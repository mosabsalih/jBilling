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

<%@ page import="com.sapienter.jbilling.client.user.UserHelper; com.sapienter.jbilling.common.Constants" %>
<table cellpadding="0" cellspacing="0" class="blacklist" width="100%">
    <thead>
    <tr>
        <th class="medium"><g:message code="blacklist.th.name"/></th>
        <th class="small2"><g:message code="blacklist.th.credit.card"/></th>
        <th class="small2"><g:message code="blacklist.th.ip.address"/></th>
    </tr>
    </thead>

    <tbody>
    <g:each var="entry" status="i" in="${blacklist}">
        <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
            <td id="entry-${entry.id}">
                <g:remoteLink class="cell" action="show" id="${entry.id}" before="register(this);" onSuccess="render(data, next);">
                    <g:set var="name" value="${UserHelper.getDisplayName(entry.user, entry.contact)}"/>
                    ${name ?: entry.user?.id ?: entry.contact?.userId ?: entry.contact?.id}
                </g:remoteLink>
            </td>
            <td>
                <g:remoteLink class="cell" action="show" id="${entry.id}" before="register(this);" onSuccess="render(data, next);">
                %{-- obscure credit card by default, or if the preference is explicitly set --}%
                    <g:if test="${entry.creditCard?.number && preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                        <g:set var="creditCardNumber" value="${entry.creditCard.number.replaceAll('^\\d{12}','************')}"/>
                        ${creditCardNumber}
                    </g:if>
                    <g:else>
                        ${entry.creditCard?.number}
                    </g:else>
                </g:remoteLink>
            </td>
            <td>
                <g:remoteLink class="cell" action="show" id="${entry.id}" before="register(this);" onSuccess="render(data, next);">
                    <g:if test="${ipAddressType}">
                        <g:set var="field" value="${entry.contact?.fields?.find{ it.type.id == ipAddressType.id }}"/>
                        ${field?.content}
                    </g:if>
                </g:remoteLink>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>