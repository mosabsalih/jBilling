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

<%@ page import="com.sapienter.jbilling.common.Constants" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <script type="text/javascript">
        function clearInvoiceSelection() {
            $(':input[type=radio][name=invoiceId]').attr('checked','');
        }
    </script>
</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:if test="${payment.isRefund > 0}">
                <g:message code="payment.confirm.refund.title"/>
            </g:if>
            <g:else>
                <g:message code="payment.confirm.payment.title"/>
            </g:else>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="payment-link-form" action="applyPayment">
            <fieldset>

                <!-- invoices to pay -->

                    <div id="invoices" class="box-cards box-cards-open">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="payment.payable.invoices.title"/></span></a>
                        </div>
                        <div class="box-card-hold">

                            <g:if test="${invoices}">
                                <table cellpadding="0" cellspacing="0" class="innerTable">
                                    <thead class="innerHeader">
                                    <tr>
                                        <th><g:message code="invoice.label.number"/></th>
                                        <th><g:message code="invoice.label.payment.attempts"/></th>
                                        <th><g:message code="invoice.label.total"/></th>
                                        <th><g:message code="invoice.label.balance"/></th>
                                        <th><g:message code="invoice.label.duedate"/></th>
                                        <th><!-- action --> &nbsp;</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="invoice" in="${invoices}">
                                        <g:set var="currency" value="${currencies.find { it.id == invoice.currencyId }}"/>

                                        <tr>
                                            <td class="innerContent">
                                                <g:applyLayout name="form/radio">
                                                    <g:radio id="invoice-${invoice.id}" name="invoiceId" value="${invoice.id}" checked="${invoice.id == invoiceId}"/>
                                                    <label for="invoice-${invoice.id}" class="rb">
                                                        <g:message code= "payment.link.invoice" args="[invoice.number]"/>
                                                    </label>
                                                </g:applyLayout>
                                            </td>
                                            <td class="innerContent">
                                                ${invoice.paymentAttempts}
                                            </td>
                                            <td class="innerContent">
                                                <g:formatNumber number="${invoice.getTotalAsDecimal()}" type="currency" currencySymbol="${currency.symbol}"/>
                                            </td>
                                            <td class="innerContent">
                                                <g:formatNumber number="${invoice.getBalanceAsDecimal()}" type="currency" currencySymbol="${currency.symbol}"/>
                                            </td>
                                            <td class="innerContent">
                                                <g:formatDate date="${invoice.dueDate}"/>
                                            </td>
                                            <td class="innerContent">
                                                <g:link controller="invoice" action="list" id="${invoice.id}">
                                                    <g:message code= "payment.link.view.invoice" args="[invoice.number]"/>
                                                </g:link>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>

                                <div class="btn-row">
                                    <a onclick="clearInvoiceSelection();" class="submit delete"><span><g:message code="button.clear"/></span></a>
                                </div>
                            </g:if>
                            <g:else>
                                <!-- no payable invoices -->
                                <div class="form-columns">
                                    <em><g:message code="payment.no.payable.invoices" args="[user.userId]"/></em>
                                </div>
                            </g:else>
                        </div>
                    </div>


                <!-- payment details  -->
                <div class="form-columns">
                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.id"/></content>
                            <span>${payment.id}</span>
                            <g:hiddenField name="id" value="${payment?.id}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.attempt"/></content>
                            <span>${payment.attempt}</span>
                        </g:applyLayout>

                        <g:set var="currency" value="${currencies.find { it.id == payment?.currencyId }}"/>
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="prompt.user.currency"/></content>
                            <span>${currency?.getDescription(session['language_id']) ?: payment.currencyId}</span>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.amount"/></content>
                            <content tag="label.for">payment.amount</content>
                            <span><g:formatNumber number="${payment.amount}" formatName="money.format"/></span>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.date"/></content>
                            <content tag="label.for">payment.paymentDate</content>
                            <g:set var="paymentDate" value="${payment?.paymentDate ?: new Date()}"/>
                            <span><g:formatDate date="${paymentDate}"/></span>
                        </g:applyLayout>
                    </div>

                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.user.id"/></content>
                            <span><g:link controller="customer" action="list" id="${user.userId}">${user.userId}</g:link></span>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="prompt.login.name"/></content>
                            <span>${user.userName}</span>
                            <g:hiddenField name="userId" value="${user.userId}"/>
                        </g:applyLayout>

                        <g:if test="${user.contact?.firstName || user.contact?.lastName}">
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.customer.name"/></content>
                                <em>${user.contact.firstName} ${user.contact.lastName}</em>
                            </g:applyLayout>
                        </g:if>

                        <g:if test="${user.contact?.organizationName}">
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.organization.name"/></content>
                                <em>${user.contact.organizationName}</em>
                            </g:applyLayout>
                        </g:if>
                    </div>
                </div>

                <!-- spacer -->
                <div>
                    <br/>&nbsp;
                </div>

                <!-- credit card -->
                <g:if test="${payment?.creditCard}">
                    <g:set var="creditCard" value="${payment?.creditCard}"/>

                    <div id="creditCard" class="box-cards ${creditCard ? 'box-cards-open' : ''}">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="prompt.credit.card"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.credit.card"/></content>
                                        <content tag="label.for">creditCard.number</content>

                                        %{-- obscure credit card by default, or if the preference is explicitly set --}%
                                        <g:if test="${preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                                            <g:set var="creditCardNumber" value="${creditCard.number.replaceAll('^\\d{12}','************')}"/>
                                            <span>${creditCardNumber}</span>
                                        </g:if>
                                        <g:else>
                                            <span>${creditCard?.number}</span>
                                        </g:else>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.name.on.card"/></content>
                                        <content tag="label.for">creditCard.name</content>
                                        <span>${creditCard?.name}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.expiry.date"/></content>
                                        <content tag="label.for">expiryMonth</content>
                                        <span>
                                            <g:formatDate date="${creditCard?.expiry}" format="MM"/>
                                            /
                                            <g:formatDate date="${creditCard?.expiry}" format="yyyy"/>
                                        </span>
                                    </g:applyLayout>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>

                <!-- ach -->
                <g:if test="${payment?.ach}">
                    <g:set var="ach" value="${payment?.ach}"/>

                    <div id="ach" class="box-cards ${ach ? 'box-cards-open' : ''}">
                        <div class="box-cards-title">
                            <a class="btn-open" href="#"><span><g:message code="prompt.ach"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.aba.routing.num"/></content>
                                        <content tag="label.for">ach.abaRouting</content>
                                        <span>${ach?.abaRouting}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.bank.acc.num"/></content>
                                        <content tag="label.for">ach.bankAccount</content>
                                        <span>${ach?.bankAccount}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.bank.name"/></content>
                                        <content tag="label.for">ach.bankName</content>
                                        <span>${ach?.bankName}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.name.customer.account"/></content>
                                        <content tag="label.for">ach.accountName</content>
                                        <span>${ach?.accountName}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.account.type" /></content>

                                        <g:if test="${ach?.accountType == 1}">
                                            <span><g:message code="label.account.checking"/></span>
                                        </g:if>
                                        <g:elseif test="${ach?.accountType == 2}">
                                            <span><g:message code="label.account.savings"/></span>
                                        </g:elseif>
                                    </g:applyLayout>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>

                <!-- cheque -->
                <g:if if="cheque" test="${payment?.cheque}">
                    <g:set var="cheque" value="${payment?.cheque}"/>

                    <div id="cheque" class="box-cards ${cheque ? 'box-cards-open' : ''}">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="prompt.cheque"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.cheque.bank"/></content>
                                        <content tag="label.for">cheque.bank</content>
                                        <span>${cheque?.bank}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.cheque.number"/></content>
                                        <content tag="label.for">cheque.number</content>
                                        <span>${cheque?.number}</span>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/text">
                                        <content tag="label"><g:message code="prompt.cheque.date"/></content>
                                        <content tag="label.for">cheque.date</content>
                                        <span><g:formatDate date="${cheque?.date}" formatName="date.format"/></span>
                                    </g:applyLayout>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>

                <!-- box text -->
                <div class="box-text">
                    <label><g:message code="payment.notes"/></label>
                    <ul>
                        <li><p>${payment?.paymentNotes}</p></li>
                    </ul>
                </div>

                <div class="buttons">
                    <ul>
                        <g:if test="${invoices}">
                            <li>
                                <a onclick="$('#payment-link-form').submit()" class="submit payment"><span><g:message code="button.link.payment"/></span></a>
                            </li>
                        </g:if>
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