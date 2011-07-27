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

<%@ page import="com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO; com.sapienter.jbilling.server.payment.blacklist.db.BlacklistDTO; com.sapienter.jbilling.server.util.Constants" %>

<div class="column-hold">
    <div class="heading">
        <strong>
            <g:message code="blacklist.entry.title"/>
            <em>${selected.id}</em>
        </strong>
    </div>

    <div class="box">
        <fieldset>
            <div class="form-columns">

                <table cellpadding="0" cellspacing="0" class="dataTable">
                    <tbody>
                        <tr>
                            <td><g:message code="blacklist.entry.label.type"/></td>
                            <td class="value">
                                <g:message code="blacklist.type.${selected.type}"/>
                            </td>
                        </tr>
                        <tr>
                            <td><g:message code="blacklist.entry.label.source"/></td>
                            <td class="value">
                                <g:message code="blacklist.source.${selected.source}"/>
                            </td>
                        </tr>

                        <g:if test="${selected.type == BlacklistDTO.TYPE_USER_ID}">
                            <tr>
                                <td><g:message code="blacklist.entry.label.user.id"/></td>
                                <td class="value">${selected.user?.id}</td>
                            </tr>
                        </g:if>

                        <g:if test="${selected.type == BlacklistDTO.TYPE_NAME}">
                            <tr>
                                <td><g:message code="blacklist.entry.label.name"/></td>
                                <td class="value">
                                    <g:if test="${selected.contact?.firstName || selected.contact?.lastName}">
                                        ${selected.contact.firstName} ${selected.contact.lastName}
                                    </g:if>
                                </td>
                            </tr>
                            <tr>
                                <td><g:message code="blacklist.entry.label.organization.name"/></td>
                                <td class="value">${selected.contact?.organizationName}</td>
                            </tr>
                            <tr>
                                <td><g:message code="blacklist.entry.label.email"/></td>
                                <td class="value">${selected.contact?.email}</td>
                            </tr>
                        </g:if>

                        <g:if test="${selected.type == BlacklistDTO.TYPE_IP_ADDRESS}">
                            <g:set var="ipAddressType" value="${ContactFieldTypeDTO.list().find{ it.promptKey ==~ /.*ip_address.*/ }}"/>
                            <g:if test="${ipAddressType}">
                                <tr>
                                    <td><g:message code="blacklist.entry.label.ip.address"/></td>
                                    <td class="value">
                                        <g:set var="field" value="${selected.contact?.fields?.find{ it.type.id == ipAddressType.id }}"/>
                                        ${field?.content}
                                    </td>
                                </tr>
                            </g:if>
                        </g:if>

                        <g:if test="${selected.type == BlacklistDTO.TYPE_PHONE_NUMBER}">
                            <tr>
                                <td><g:message code="blacklist.entry.label.phone.number"/></td>
                                <td class="value">
                                    <g:if test="${selected.contact?.phoneCountryCode}">${selected.contact?.phoneCountryCode}.</g:if>
                                    <g:if test="${selected.contact?.phoneAreaCode}">${selected.contact?.phoneAreaCode}.</g:if>
                                    ${selected.contact?.phoneNumber}
                                </td>
                            </tr>
                        </g:if>

                    </tbody>
                </table>

            </div>
        </fieldset>
    </div>

    <g:if test="${selected.type == BlacklistDTO.TYPE_CC_NUMBER}">
        <div class="heading">
            <strong><g:message code="blacklist.entry.credit.card.title"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="customer.detail.payment.credit.card"/></td>
                        <td class="value">
                            %{-- obscure credit card by default, or if the preference is explicitly set --}%
                            <g:if test="${selected.creditCard?.number && preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                                <g:set var="creditCardNumber" value="${selected.creditCard.number.replaceAll('^\\d{12}','************')}"/>
                                ${creditCardNumber}
                            </g:if>
                            <g:else>
                                ${selected.creditCard?.number}
                            </g:else>
                        </td>
                    </tr>

                    <tr>
                        <td><g:message code="customer.detail.payment.credit.card.expiry"/></td>
                        <td class="value"><g:formatDate date="${selected.creditCard?.ccExpiry}"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <g:if test="${selected.type == BlacklistDTO.TYPE_ADDRESS}">
        <div class="heading">
            <strong><g:message code="blacklist.entry.address.title"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="customer.detail.contact.address"/></td>
                        <td class="value">${selected.contact?.address1} ${selected.contact?.address2}</td>
                    </tr>
                    <tr>
                        <td><g:message code="customer.detail.contact.city"/></td>
                        <td class="value">${selected.contact?.city}</td>
                    </tr>
                    <tr>
                        <td><g:message code="customer.detail.contact.state"/></td>
                        <td class="value">${selected.contact?.stateProvince}</td>
                    </tr>
                    <tr>
                        <td><g:message code="customer.detail.contact.country"/></td>
                        <td class="value">${selected.contact?.countryCode}</td>
                    </tr>
                    <tr>
                        <td><g:message code="customer.detail.contact.zip"/></td>
                        <td class="value">${selected.contact?.postalCode}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <div class="btn-box buttons">
        <div class="row"></div>
    </div>

</div>
