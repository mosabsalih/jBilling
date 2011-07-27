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

<%@ page import="com.sapienter.jbilling.server.util.Constants" %>

<div class="form-edit">
    <div class="heading">
        <strong><g:message code="currency.config.title"/></strong>
    </div>
    <div class="form-hold">
        <g:form name="save-currencies-form" url="[action: 'saveCurrencies']">
            <fieldset>
                <div class="form-columns single">
                    <g:applyLayout name="form/select">
                        <content tag="label"><g:message code="currency.config.label.default"/></content>
                        <content tag="label.for">defaultCurrencyId</content>
                        <g:select name="defaultCurrencyId" from="${currencies}"
                                optionKey="id"
                                optionValue="${{ it.getDescription(session['language_id']) }}"
                                value="${entityCurrency}"/>
                    </g:applyLayout>
                </div>


                <div class="form-columns single">
                    <table cellpadding="0" cellspacing="0" class="innerTable" width="100%">
                        <thead class="innerHeader">
                            <tr>
                                <th class=""></th>
                                <th class="left tiny2"><g:message code="currency.config.th.symbol"/></th>
                                <th class="left tiny2"><g:message code="currency.config.th.active"/></th>
                                <th class="left medium"><g:message code="currency.config.th.rate"/></th>
                                <th class="left medium"><g:message code="currency.config.th.sysRate"/></th>
                            </tr>
                        </thead>
                        <tbody>

                        <g:each var="currency" in="${currencies.sort{ it.id }}">
                            <tr>
                                <td class="innerContent">
                                    ${currency.getDescription(session['language_id'])}
                                    <g:hiddenField name="currencies.${currency.id}.id" value="${currency.id}"/>
                                </td>
                                <td class="innerContent">
                                    ${currency.symbol}
                                    <g:hiddenField name="currencies.${currency.id}.symbol" value="${currency.symbol}"/>
                                    <g:hiddenField name="currencies.${currency.id}.code" value="${currency.code}"/>
                                    <g:hiddenField name="currencies.${currency.id}.countryCode" value="${currency.countryCode}"/>
                                </td>
                                <td class="innerContent">
                                    <g:checkBox class="cb checkbox" name="currencies.${currency.id}.inUse" checked="${currency.inUse}"/>
                                </td>
                                <td class="innerContent">
                                    <div class="inp-bg inp4">
                                        <g:textField name="currencies.${currency.id}.rate" class="field" value="${formatNumber(number: currency.rate, formatName: 'decimal.format')}"/>
                                    </div>
                                </td>
                                <td class="innerContent" style="text-align: left;">

                                    <g:if test="${currency.id != 1}">
                                        %{-- editable rate --}%
                                        <div class="inp-bg inp4">
                                            <g:textField name="currencies.${currency.id}.sysRate" class="field" value="${formatNumber(number: currency.sysRate, formatName: 'decimal.format')}"/>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        %{-- USD always has a rate of 1.00 --}%
                                        <strong>
                                            <g:formatNumber number="${currency.sysRate}" type="currency" currencySymbol="${currency.symbol}"/>
                                        </strong>
                                    </g:else>

                                </td>
                            </tr>
                        </g:each>

                        </tbody>
                    </table>
                 </div>

                <!-- spacer -->
                <div>
                    <br/>&nbsp;
                </div>


            </fieldset>
        </g:form>
    </div>

    <div class="btn-box">
        <div class="row">
            <g:remoteLink controller="config" action="editCurrency" class="submit add" before="register(this);" onSuccess="render(data, next);">
                <span><g:message code="button.create"/></span>
            </g:remoteLink>
        </div>
        <div class="row">
            <a onclick="$('#save-currencies-form').submit();" class="submit save"><span><g:message code="button.save"/></span></a>
            <g:link controller="config" action="index" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link>
        </div>
    </div>
</div>