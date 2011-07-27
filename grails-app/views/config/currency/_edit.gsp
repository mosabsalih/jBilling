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

<%@ page contentType="text/html;charset=UTF-8" %>

<%--
  Shows an edit form for a currency (used to create new currencies).

  @author Brian Cowdery
  @since  07-Apr-2011
--%>

<div class="column-hold">
    <div class="heading">
        <strong><g:message code="currency.config.new.currency.title"/></strong>
    </div>

    <g:form name="save-currency-form" url="[action: 'saveCurrency']">

    <div class="box">
        <fieldset>
            <div class="form-columns">
                <g:hiddenField name="id" value="${currency?.id}"/>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.name"/></content>
                    <content tag="label.for">description</content>
                    <g:textField name="description" class="field" value="${currency?.description}"/>
                </g:applyLayout>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.code"/></content>
                    <content tag="label.for">code</content>
                    <g:textField name="code" class="field" value="${currency?.code}" maxlength="3"/>
                </g:applyLayout>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.symbol"/></content>
                    <content tag="label.for">symbol</content>
                    <g:textField name="symbol" class="field" value="${currency?.symbol}" maxlength="10"/>
                </g:applyLayout>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.countryCode"/></content>
                    <content tag="label.for">countryCode</content>
                    <g:textField name="countryCode" class="field" value="${currency?.countryCode}" maxlength="2"/>
                </g:applyLayout>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.rate"/></content>
                    <content tag="label.for">rate</content>
                    <g:textField name="rate" class="field" value="${currency?.rate}"/>
                </g:applyLayout>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="currency.config.label.sysRate"/></content>
                    <content tag="label.for">sysRate</content>
                    <g:textField name="sysRate" class="field" value="${formatNumber(number: currency?.sysRate ?: BigDecimal.ONE, formatName: 'decimal.format')}"/>
                </g:applyLayout>

                <g:applyLayout name="form/checkbox">
                    <content tag="label"><g:message code="currency.config.label.active"/></content>
                    <content tag="label.for">inUse</content>
                    <g:checkBox name="inUse" class="cb" value="${currency?.inUse}"/>
                </g:applyLayout>

            </div>
        </fieldset>
    </div>

    </g:form>

    <div class="btn-box buttons">
        <ul>
            <li><a class="submit save" onclick="$('#save-currency-form').submit();"><span><g:message code="button.save"/></span></a></li>
            <li><a class="submit cancel" onclick="closePanel(this);"><span><g:message code="button.cancel"/></span></a></li>
        </ul>
    </div>
</div>