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
  Address data-table template for the customer inspector.

  @author Brian Cowdery
  @since  11-Jan-2011
--%>

<table class="dataTable" cellspacing="0" cellpadding="0">
    <tbody>
    <tr>
        <td><g:message code="customer.detail.contact.name"/></td>
        <td class="value">${contact.firstName} ${contact.initial} ${contact.lastName}</td>

        <td><g:message code="customer.detail.contact.organization"/></td>
        <td class="value">${contact.organizationName}</td>
    </tr>
    <tr>
        <td><g:message code="customer.detail.contact.telephone"/></td>
        <td class="value">
            <g:phoneNumber countryCode="${contact?.phoneCountryCode}" 
                    areaCode="${contact?.phoneAreaCode}" number="${contact?.phoneNumber}"/>
        </td>

        <td><g:message code="customer.detail.contact.fax"/></td>
        <td class="value">
            <g:if test="${contact?.faxCountryCode}">${contact?.faxCountryCode}.</g:if>
            <g:if test="${contact?.faxAreaCode}">${contact?.faxAreaCode}.</g:if>
            ${contact?.faxNumber}
        </td>

        <td><g:message code="customer.detail.user.email"/></td>
        <td class="value">${contact?.email}</td>
    </tr>
    <tr>
        <td><g:message code="customer.detail.contact.address"/></td>
        <td class="value">${contact?.address1} ${contact?.address2}</td>
    </tr>
    <tr>
        <td><g:message code="customer.detail.contact.city"/></td>
        <td class="value">${contact?.city}</td>

        <td><g:message code="customer.detail.contact.state"/></td>
        <td class="value">${contact?.stateProvince}</td>

        <td><g:message code="customer.detail.contact.zip"/></td>
        <td class="value">${contact?.postalCode}</td>
    </tr>
    <tr>
        <td><g:message code="customer.detail.contact.country"/></td>
        <td class="value">${contact?.countryCode}</td>
    </tr>
    <tr>
        <td><g:message code="prompt.include.in.notifications"/></td>
        <td class="value"><g:formatBoolean boolean="${ (contact?.include instanceof Boolean) ? contact.include : (contact.include > 0) }"/></td>
    </tr>
    </tbody>
</table>



