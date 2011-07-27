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

<%@ page import="com.sapienter.jbilling.server.util.db.CountryDTO" %>
<div id="contact-${contactType.id}" class="contact" style="${contactType.isPrimary > 0 ? '' : 'display: none;'}">

    <g:hiddenField name="contact-${contactType?.id}.id" value="${contact?.id}"/>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.organization.name"/></content>
        <content tag="label.for">contact-${contactType?.id}.organizationName</content>
        <g:textField class="field" name="contact-${contactType?.id}.organizationName" value="${contact?.organizationName}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.first.name"/></content>
        <content tag="label.for">contact-${contactType?.id}.firstName</content>
        <g:textField class="field" name="contact-${contactType?.id}.firstName" value="${contact?.firstName}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.last.name"/></content>
        <content tag="label.for">contact-${contactType?.id}.lastName</content>
        <g:textField class="field" name="contact-${contactType?.id}.lastName" value="${contact?.lastName}" />
    </g:applyLayout>

    <g:applyLayout name="form/text">
        <content tag="label"><g:message code="prompt.phone.number"/></content>
        <content tag="label.for">contact-${contactType?.id}.phoneCountryCode</content>
        <span>
            <g:textField class="field" name="contact-${contactType?.id}.phoneCountryCode" value="${contact?.phoneCountryCode}" maxlength="3" size="2"/>
            -
            <g:textField class="field" name="contact-${contactType?.id}.phoneAreaCode" value="${contact?.phoneAreaCode}" maxlength="5" size="3"/>
            -
            <g:textField class="field" name="contact-${contactType?.id}.phoneNumber" value="${contact?.phoneNumber}" maxlength="10" size="8"/>
        </span>
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.email"/></content>
        <content tag="label.for">contact-${contactType?.id}.email</content>
        <g:textField class="field" name="contact-${contactType?.id}.email" value="${contact?.email}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.address1"/></content>
        <content tag="label.for">contact-${contactType?.id}.address1</content>
        <g:textField class="field" name="contact-${contactType?.id}.address1" value="${contact?.address1}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.address2"/></content>
        <content tag="label.for">contact-${contactType?.id}.address2</content>
        <g:textField class="field" name="contact-${contactType?.id}.address2" value="${contact?.address2}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.city"/></content>
        <content tag="label.for">contact-${contactType?.id}.city</content>
        <g:textField class="field" name="contact-${contactType?.id}.city" value="${contact?.city}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.state"/></content>
        <content tag="label.for">contact-${contactType?.id}.stateProvince</content>
        <g:textField class="field" name="contact-${contactType?.id}.stateProvince" value="${contact?.stateProvince}" />
    </g:applyLayout>

    <g:applyLayout name="form/input">
        <content tag="label"><g:message code="prompt.zip"/></content>
        <content tag="label.for">contact-${contactType?.id}.postalCode</content>
        <g:textField class="field" name="contact-${contactType?.id}.postalCode" value="${contact?.postalCode}" />
    </g:applyLayout>

    <g:applyLayout name="form/select">
        <content tag="label"><g:message code="prompt.country"/></content>
        <content tag="label.for">contact-${contactType?.id}.countryCode</content>

        <g:select name="contact-${contactType?.id}.countryCode"
                  from="${CountryDTO.list()}"
                  optionKey="code"
                  optionValue="${{ it.getDescription(session['language_id']) }}"
                  noSelection="['': message(code: 'default.no.selection')]"
                  value="${contact?.countryCode}"/>
    </g:applyLayout>

    <g:applyLayout name="form/checkbox">
        <content tag="label"><g:message code="prompt.include.in.notifications"/></content>
        <content tag="label.for">contact-${contactType?.id}.include</content>
        <g:checkBox class="cb checkbox" name="contact-${contactType?.id}.include" checked="${contact?.include > 0}"/>
    </g:applyLayout>
</div>
