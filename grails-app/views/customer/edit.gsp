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

<%@ page import="com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO; com.sapienter.jbilling.server.user.db.CompanyDTO; com.sapienter.jbilling.server.user.permisson.db.RoleDTO; com.sapienter.jbilling.common.Constants; com.sapienter.jbilling.server.util.db.LanguageDTO" %>
<html>
<head>
    <meta name="layout" content="main" />

    <script type="text/javascript">
        $(document).ready(function() {
            $('#contactType').change(function() {
                var selected = $('#contact-' + $(this).val());
                $(selected).show();
                $('div.contact').not(selected).hide();
            }).change();
        });
    </script>
</head>
<body>
<div class="form-edit">

    <g:set var="isNew" value="${!user || !user?.userId || user?.userId == 0}"/>

    <div class="heading">
        <strong>
            <g:if test="${isNew}">
                <g:message code="customer.create.title"/>
            </g:if>
            <g:else>
                <g:message code="customer.edit.title"/>
            </g:else>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="user-edit-form" action="save">
            <fieldset>
                <div class="form-columns">

                    <!-- user details column -->
                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="prompt.customer.number"/></content>

                            <g:if test="${!isNew}">
                                <span>
                                    <g:link controller="customerInspector" action="inspect" id="${user.userId}" title="${message(code: 'customer.inspect.link')}">${user.userId}</g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <em><g:message code="prompt.id.new"/></em>
                            </g:else>

                            <g:hiddenField name="user.userId" value="${user?.userId}"/>
                        </g:applyLayout>

                        <g:if test="${isNew}">
                            <g:applyLayout name="form/input">
                                <content tag="label"><g:message code="prompt.login.name"/></content>
                                <content tag="label.for">user.userName</content>
                                <g:textField class="field" name="user.userName" value="${user?.userName}"/>
                            </g:applyLayout>
                        </g:if>
                        <g:else>
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.login.name"/></content>

                                ${user?.userName}
                                <g:hiddenField name="user.userName" value="${user?.userName}"/>
                            </g:applyLayout>
                        </g:else>

                        <g:if test="${!isNew}">
                             <g:applyLayout name="form/input">
                                <content tag="label"><g:message code="prompt.current.password"/></content>
                                <content tag="label.for">oldPassword</content>
                                <g:passwordField class="field" name="oldPassword"/>
                            </g:applyLayout>
                        </g:if>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="prompt.password"/></content>
                            <content tag="label.for">newPassword</content>
                            <g:passwordField class="field" name="newPassword"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="prompt.verify.password"/></content>
                            <content tag="label.for">verifiedPassword</content>
                            <g:passwordField class="field" name="verifiedPassword"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.user.status"/></content>
                            <content tag="label.for">user.statusId</content>
                            <g:userStatus name="user.statusId" value="${user?.statusId}" languageId="${session['language_id']}" />
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.user.subscriber.status"/></content>
                            <content tag="label.for">user.subscriberStatusId</content>
                            <g:subscriberStatus name="user.subscriberStatusId" value="${user?.subscriberStatusId}" languageId="${session['language_id']}" />
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.user.language"/></content>
                            <content tag="label.for">user.languageId</content>
                            <g:select name="user.languageId" from="${LanguageDTO.list()}"
                                    optionKey="id" optionValue="description" value="${user?.languageId}"  />
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.user.currency"/></content>
                            <content tag="label.for">user.currencyId</content>
                            <g:select name="user.currencyId"
                                      from="${currencies}"
                                      optionKey="id"
                                      optionValue="${{it.getDescription(session['language_id'])}}"
                                      value="${user?.currencyId}" />
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.preferred.auto.payment"/></content>
                            <content tag="label.for">user.automaticPaymentType</content>
                            <g:select name="user.automaticPaymentType"
                                      from="${[Constants.AUTO_PAYMENT_TYPE_CC, Constants.AUTO_PAYMENT_TYPE_ACH]}"
                                      valueMessagePrefix="auto.payment.type"
                                      value="${user?.automaticPaymentType}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="prompt.partner.id"/></content>
                            <content tag="label.for">user.partnerId</content>
                            <g:textField class="field" name="user.partnerId" value="${user?.partnerId}"/>
                        </g:applyLayout>

                        <g:if test="${parent?.customerId}">
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.parent.id"/></content>
                                <g:link action="list" id="${parent.userId}">${parent.userId} ${parent.userName}</g:link>
                                <g:hiddenField class="field" name="user.parentId" value="${parent.userId}"/>
                            </g:applyLayout>
                        </g:if>
                        <g:else>
                            <g:applyLayout name="form/input">
                                <content tag="label"><g:message code="prompt.parent.id"/></content>
                                <content tag="label.for">user.parentId</content>
                                <g:textField class="field" name="user.parentId" value="${user?.parentId}"/>
                            </g:applyLayout>
                        </g:else>

                        <g:applyLayout name="form/checkbox">
                            <content tag="label"><g:message code="prompt.allow.sub.accounts"/></content>
                            <content tag="label.for">user.isParent</content>
                            <g:checkBox class="cb checkbox" name="user.isParent" checked="${user?.isParent}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/checkbox">
                            <content tag="label"><g:message code="prompt.invoice.if.child"/></content>
                            <content tag="label.for">user.invoiceChild</content>
                            <g:checkBox class="cb checkbox" name="user.invoiceChild" checked="${user?.invoiceChild}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/checkbox">
                            <content tag="label"><g:message code="prompt.exclude.ageing"/></content>
                            <content tag="label.for">user.excludeAgeing</content>
                            <g:checkBox class="cb checkbox" name="user.excludeAgeing" checked="${user?.excludeAgeing}"/>
                        </g:applyLayout>
                    </div>

                    <!-- contact information column -->
                    <g:set var="contactTypes" value="${company.contactTypes.asList()}"/>
                    <g:set var="primaryContactType" value="${contactTypes.find{ it.isPrimary == 1 }}"/>
                    <g:hiddenField name="primaryContactTypeId" value="${primaryContactType.id}"/>

                    <div class="column">
                        <g:if test="${contactTypes.size > 1}">
                            <g:applyLayout name="form/select">
                                <content tag="label"><g:message code="prompt.contact.type"/></content>
                                <g:select name="contactType"
                                          from="${contactTypes}"
                                          optionKey="id"
                                          optionValue="${{it.getDescription(session['language_id'])}}"
                                          value="${primaryContactType.id}"/>
                            </g:applyLayout>
                        </g:if>
                        <g:else>
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.contact.type"/></content>
                                <span>${(contact?.type ?: contactTypes?.get(0)).getDescription(session['language_id'])}</span>
                            </g:applyLayout>
                        </g:else>

                        <!-- print a hidden block for each contact type, will be toggled by contact type dropdown -->
                        <g:each var="contactType" in="${contactTypes}">
                            <g:set var="contact" value="${contacts.find{ it.type == contactType.id }}"/>
                            <g:render template="contact" model="[contactType: contactType, contact: contact]"/>
                        </g:each>

                        <br/>&nbsp;

                        <!-- custom contact fields -->
                        <g:each var="ccf" in="${company.contactFieldTypes.sort{ it.id }}">
                            <g:set var="fieldIndex" value="${user?.contact?.fieldIDs?.findIndexOf{ it == ccf.id }}"/>
                            <g:set var="fieldValue" value="${user?.contact?.fieldValues?.getAt(fieldIndex)}"/>

                            <g:applyLayout name="form/input">
                                <content tag="label"><g:message code="${ccf.getDescription(session['language_id'])}"/></content>
                                <g:textField class="field" name="contactField.${ccf.id}" value="${fieldValue}"/>
                            </g:applyLayout>
                        </g:each>
                    </div>
                </div>

                <!-- separator -->
                <div class="form-columns">
                    <hr/>
                </div>

                <!-- dynamic balance and invoice delivery -->
                <div class="form-columns">
                    <div class="column">
                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.balance.type"/></content>
                            <content tag="label.for">user.balanceType</content>
                            <g:select from="[Constants.BALANCE_NO_DYNAMIC, Constants.BALANCE_PRE_PAID, Constants.BALANCE_CREDIT_LIMIT]"
                                      valueMessagePrefix="customer.balance.type"
                                      name="user.balanceType"
                                      value="${user?.balanceType}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="prompt.credit.limit"/></content>
                            <content tag="label.for">user.creditLimitAsDecimal</content>
                            <g:textField class="field" name="user.creditLimitAsDecimal" value="${formatNumber(number: user?.creditLimitAsDecimal ?: 0, formatName: 'money.format')}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="prompt.auto.recharge"/></content>
                            <content tag="label.for">user.autoRechargeAsDecimal</content>
                            <g:textField class="field" name="user.autoRechargeAsDecimal" value="${formatNumber(number: user?.autoRechargeAsDecimal ?: 0, formatName: 'money.format')}"/>
                        </g:applyLayout>
                    </div>

                    <div class="column">
                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="prompt.invoice.delivery.method"/></content>
                            <content tag="label.for">user.invoiceDeliveryMethodId</content>
                            <g:select from="${company.invoiceDeliveryMethods.sort{ it.id }}"
                                      optionKey="id"
                                      valueMessagePrefix="customer.invoice.delivery.method"
                                      name="user.invoiceDeliveryMethodId"
                                      value="${user?.invoiceDeliveryMethodId}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="prompt.due.date.override"/></content>
                            <content tag="label.for">user.dueDateValue</content>

                            <div class="inp-bg inp4">
                                <g:textField class="field" name="user.dueDateValue" value="${user?.dueDateValue}"/>
                            </div>

                            <div class="select4">
                                <g:select from="${company.orderPeriods.collect{ it.periodUnit }}"
                                        optionKey="id"
                                        optionValue="${{it.getDescription(session['language_id'])}}"
                                        name="user.dueDateUnitId"
                                        value="${user?.dueDateUnitId}"/>
                            </div>
                        </g:applyLayout>
                    </div>
                </div>

                <!-- spacer -->
                <div>
                    <br/>&nbsp;
                </div>

                <!-- credit card -->
                <g:set var="creditCard" value="${user?.creditCard}"/>
                <g:hiddenField name="creditCard.id" value="${creditCard?.id}"/>

                <div id="creditCard" class="box-cards ${creditCard ? 'box-cards-open' : ''}">
                    <div class="box-cards-title">
                        <a class="btn-open"><span><g:message code="prompt.credit.card"/></span></a>
                    </div>
                    <div class="box-card-hold">
                        <div class="form-columns">
                            <div class="column">
                                <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.name.on.card"/></content>
                                    <content tag="label.for">creditCard.name</content>
                                    <g:textField class="field" name="creditCard.name" value="${creditCard?.name}" />
                                </g:applyLayout>

                                <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.credit.card.number"/></content>
                                    <content tag="label.for">creditCard.number</content>

                                    %{-- obscure credit card by default, or if the preference is explicitly set --}%
                                    <g:if test="${!isNew && creditCard && preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                                        <g:set var="creditCardNumber" value="${creditCard.number.replaceAll('^\\d{12}','************')}"/>
                                        <g:textField class="field" name="creditCard.number" value="${creditCardNumber}" />
                                    </g:if>
                                    <g:else>
                                        <g:textField class="field" name="creditCard.number" value="${creditCard?.number}" />
                                    </g:else>
                                </g:applyLayout>

                                <g:applyLayout name="form/text">
                                    <content tag="label"><g:message code="prompt.expiry.date"/></content>
                                    <content tag="label.for">expiryMonth</content>
                                    <span>
                                        <g:textField class="text" name="expiryMonth" maxlength="2" size="2" value="${formatDate(date: creditCard?.expiry, format:'MM')}" />
                                        -
                                        <g:textField class="text" name="expiryYear" maxlength="4" size="4" value="${formatDate(date: creditCard?.expiry, format:'yyyy')}"/>
                                        mm/yyyy
                                    </span>
                                </g:applyLayout>
                            </div>

                            <g:if test="${!isNew && creditCard}">
                                <div class="column">
                                    <div class="btn-row">
                                        <a class="submit delete" onclick="$('#creditCard :input').val(''); $('#deleteCreditCard').val('true'); closeSlide('#creditCard');">
                                            <span><g:message code="button.delete"/></span>
                                        </a>

                                        <g:hiddenField name="deleteCreditCard" value=""/>
                                    </div>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>

                <!-- ach -->
                <g:set var="ach" value="${user?.ach}"/>
                <g:hiddenField name="ach.id" value="${ach?.id}"/>

                <div id="ach" class="box-cards ${ach ? 'box-cards-open' : ''}">
                    <div class="box-cards-title">
                        <a class="btn-open" href="#"><span><g:message code="prompt.ach"/></span></a>
                    </div>
                    <div class="box-card-hold">
                        <div class="form-columns">
                            <div class="column">
                               <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.aba.routing.num"/></content>
                                    <content tag="label.for">ach.abaRouting</content>
                                    <g:textField class="field" name="ach.abaRouting" value="${ach?.abaRouting}" />
                                </g:applyLayout>

                               <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.bank.acc.num"/></content>
                                    <content tag="label.for">ach.bankAccount</content>
                                    <g:textField class="field" name="ach.bankAccount" value="${ach?.bankAccount}" />
                                </g:applyLayout>

                               <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.bank.name"/></content>
                                    <content tag="label.for">ach.bankName</content>
                                    <g:textField class="field" name="ach.bankName" value="${ach?.bankName}" />
                                </g:applyLayout>

                               <g:applyLayout name="form/input">
                                    <content tag="label"><g:message code="prompt.name.customer.account"/></content>
                                    <content tag="label.for">ach.accountName</content>
                                    <g:textField class="field" name="ach.accountName" value="${ach?.accountName}" />
                                </g:applyLayout>

                                <g:applyLayout name="form/radio">
                                    <content tag="label"><g:message code="prompt.account.type" /></content>

                                    <g:radio class="rb" id="ach.accountType.checking" name="ach.accountType" value="1" checked="${ach?.accountType == 1}"/>
                                    <label class="rb" for="ach.accountType.checking"><g:message code="label.account.checking"/></label>

                                    <g:radio class="rb" id="ach.accountType.savings" name="ach.accountType" value="2" checked="${ach?.accountType == 2}"/>
                                    <label class="rb" for="ach.accountType.savings"><g:message code="label.account.savings"/></label>
                                </g:applyLayout>
                            </div>

                            <g:if test="${!isNew && ach}">
                                <div class="column">
                                    <div class="btn-row">
                                        <a class="submit delete" onclick="$('#ach :input').val(''); $('#deleteAch').val('true'); closeSlide('#ach');">
                                            <span><g:message code="button.delete"/></span>
                                        </a>

                                        <g:hiddenField name="deleteAch" value=""/>
                                    </div>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>

                <!-- box text -->
                <div class="box-text">
                    <label><g:message code="prompt.notes"/></label>
                    <g:textArea name="user.notes" value="${user?.notes}" rows="5" cols="60"/>
                </div>

                <div class="buttons">
                    <ul>
                        <li>
                            <a onclick="$('#user-edit-form').submit()" class="submit save"><span><g:message code="button.save"/></span></a>
                        </li>
                        <li>
                            <g:link action="list" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link>
                        </li>
                    </ul>
                </div>

            </fieldset>
        </g:form>
    </div>
</div>
</body>
</html>