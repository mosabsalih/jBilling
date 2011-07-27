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

<g:set var="isNew" value="${!payment || !payment?.id || payment?.id == 0}"/>

<html>
<head>
    <meta name="layout" content="main"/>

    <script type="text/javascript">
        function togglePaymentType(element) {
            $('.box-cards.payment-type').not(element).each(function () {
                // toggle slide
                closeSlide(this);
                $(this).find(':input').attr('disabled','true');

                // toggle "process now" for cheque payments
                if ($(element).attr('id') == 'cheque') {
                    $('#processNow').attr('checked','').attr('disabled','true');
                } else {
                    $('#processNow').attr('disabled','');
                }
            });

            $(element).find(':input').attr('disabled','');
        }

        function clearInvoiceSelection() {
            $(':input[type=radio][name=invoiceId]').attr('checked','');
        }

        <g:if test="${isNew}">
        $(document).ready(function() {
            // populate payment amount with selected invoice balance
            $('#invoices input[name=invoiceId]').change(function() {
                $('#payment\\.amountAsDecimal').val($('#invoice-' + $(this).val() + '-balance').val());
                var currid= $('#invoice-' + $(this).val() + '-curid').val();
                $('#payment\\.currencyId :selected').removeAttr('selected');
                $('#payment\\.currencyId option[value='+ currid +']').attr('selected','selected');
            });
        });
        </g:if>
    </script>
</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:if test="${!isNew}">
                <g:if test="${payment.isRefund > 0}">
                    <g:message code="payment.edit.refund.title"/>
                </g:if>
                <g:else>
                    <g:message code="payment.edit.payment.title"/>
                </g:else>
            </g:if>
            <g:else>
                <g:message code="payment.new.payment.title"/>
            </g:else>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="payment-edit-form" action="confirm">
            <fieldset>

                <!-- invoices to pay -->
                <g:if test="${invoices}">
                    <div id="invoices" class="box-cards box-cards-open">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="payment.payable.invoices.title"/></span></a>
                        </div>
                        <div class="box-card-hold">

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
                                <g:set var="selectedInvoiceCurrencyId" value=""/>
                                <g:each var="invoice" in="${invoices}">
                                    <g:set var="currency" value="${currencies.find { it.id == invoice.currencyId }}"/>
                                    <g:if test="${invoice.id == invoiceId}">
                                        <g:set var="selectedInvoiceCurrencyId" value="${invoice.currencyId}"/>
                                    </g:if>
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
                                            <g:hiddenField name="invoice-${invoice.id}-curid" value="${currency.id}"/>
                                        </td>
                                        <td class="innerContent">
                                            <g:formatNumber number="${invoice.getTotalAsDecimal()}" type="currency" currencySymbol="${currency.symbol}"/>
                                            <g:hiddenField name="invoice-${invoice.id}-amount" value="${formatNumber(number: invoice.total, formatName: 'money.format')}"/>
                                        </td>
                                        <td class="innerContent">
                                            <g:formatNumber number="${invoice.getBalanceAsDecimal()}" type="currency" currencySymbol="${currency.symbol}"/>
                                            <g:hiddenField name="invoice-${invoice.id}-balance" value="${formatNumber(number: invoice.balance, formatName: 'money.format')}"/>
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

                        </div>
                    </div>
                </g:if>

                <!-- payment details  -->
                <div class="form-columns">
                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.id"/></content>

                            <g:if test="${!isNew}"><span>${payment.id}</span></g:if>
                            <g:else><span><em><g:message code="prompt.id.new"/></em></span></g:else>

                            <g:hiddenField name="payment.id" value="${payment?.id}"/>
                        </g:applyLayout>

                        <g:if test="${!isNew}">
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="payment.attempt"/></content>
                                <span>${payment.attempt}</span>
                                <g:hiddenField name="payment.attempt" value="${payment?.attempt}"/>
                            </g:applyLayout>
                        </g:if>

                        <g:if test="${!isNew}">
                            <g:set var="currency" value="${currencies.find { it.id == payment?.currencyId }}"/>

                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="prompt.user.currency"/></content>
                                <span>${currency?.getDescription(session['language_id']) ?: payment.currencyId}</span>
                                <g:hiddenField name="payment.currencyId" value="${payment?.currencyId}"/>
                            </g:applyLayout>
                        </g:if>
                        <g:else>
                            <g:applyLayout name="form/select">
                                <content tag="label"><g:message code="prompt.user.currency"/></content>
                                <content tag="label.for">payment.currencyId</content>
                                <g:select name="payment.currencyId"
                                          from="${currencies}" 
                                          value="${selectedInvoiceCurrencyId}" 
                                          optionKey="id"
                                          optionValue="${{it.getDescription(session['language_id'])}}"/>
                            </g:applyLayout>
                        </g:else>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="payment.amount"/></content>
                            <content tag="label.for">payment.amountAsDecimal</content>
                            <g:set var="paymentAmount" value="${payment?.amount ?: invoices?.find{ it.id == invoiceId }?.balance }"/>
                            <g:textField class="field" name="payment.amountAsDecimal" value="${formatNumber(number: paymentAmount, formatName: 'money.format')}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/date">
                            <content tag="label"><g:message code="payment.date"/></content>
                            <content tag="label.for">payment.paymentDate</content>
                            <g:set var="paymentDate" value="${payment?.paymentDate ?: new Date()}"/>
                            <g:textField class="field" name="payment.paymentDate" value="${formatDate(date: paymentDate, formatName: 'datepicker.format')}"/>
                        </g:applyLayout>

                        <g:if test="${isNew}">
                            <g:applyLayout name="form/checkbox">
                                <content tag="label"><g:message code="payment.is.refund.payment"/></content>
                                <content tag="label.for">isRefund</content>
                                <g:checkBox class="cb checkbox" name="isRefund" checked="${payment?.isRefund}"/>
                            </g:applyLayout>
                        </g:if>
                        <g:else>
                            <g:applyLayout name="form/text">
                                <content tag="label"><g:message code="payment.is.refund.payment"/></content>
                                <span><g:formatBoolean boolean="${payment?.isRefund > 0}"/></span>
                                <g:hiddenField name="payment.isRefund" value="${payment?.isRefund}"/>
                            </g:applyLayout>
                        </g:else>

                        <g:if test="${isNew}">
                            <g:applyLayout name="form/checkbox">
                                <content tag="label"><g:message code="payment.process.realtime"/></content>
                                <content tag="label.for">processNow</content>
                                <g:checkBox class="cb checkbox" name="processNow" value="${processNow}"/>
                            </g:applyLayout>
                        </g:if>
                    </div>

                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="payment.user.id"/></content>
                            <span><g:link controller="customer" action="list" id="${user.userId}">${user.userId}</g:link></span>
                            <g:hiddenField name="payment.userId" value="${user.userId}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="prompt.login.name"/></content>
                            <span>${user.userName}</span>
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
                %{
                    def creditCardAllowed = paymentMethods.find {
                        it.id == Constants.PAYMENT_METHOD_VISA ||
                        it.id == Constants.PAYMENT_METHOD_MASTERCARD ||
                        it.id == Constants.PAYMENT_METHOD_AMEX ||
                        it.id == Constants.PAYMENT_METHOD_DISCOVERY ||
                        it.id == Constants.PAYMENT_METHOD_DINERS ||
                        it.id == Constants.PAYMENT_METHOD_GATEWAY_KEY
                    }
                }%

                <g:if test="${(creditCardAllowed && isNew) || payment?.creditCard}">
                    <g:set var="creditCard" value="${payment?.creditCard}"/>

                    <div id="creditCard" class="box-cards ${creditCard ? 'box-cards-open' : ''} payment-type" onOpen="togglePaymentType('#creditCard');">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="prompt.credit.card"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:hiddenField name="creditCard.id" value="${creditCard?.id}"/>

                                    <g:applyLayout name="form/input">
                                        <content tag="label"><g:message code="prompt.name.on.card"/></content>
                                        <content tag="label.for">creditCard.name</content>
                                        <g:textField class="field" name="creditCard.name" value="${creditCard?.name}" />
                                    </g:applyLayout>

                                    <g:applyLayout name="form/input">
                                        <content tag="label"><g:message code="prompt.credit.card.number"/></content>
                                        <content tag="label.for">creditCard.number</content>

                                        %{-- obscure credit card by default, or if the preference is explicitly set --}%
                                        <g:if test="${creditCard?.number && preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                                            <g:set var="creditCardNumber" value="${creditCard.number.replaceAll('^\\d{12}','************')}"/>
                                            <g:if test="${creditCardNumber.size() < 16}">
                                                <g:set var="creditCardNumber" value="************${creditCardNumber}"/>
                                            </g:if>

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

                            </div>
                        </div>
                    </div>

                    <g:if test="${isNew && payment?.creditCard}">
                        <script type="text/javascript">
                            /*
                                Clear the default credit card ID if any of the input fields are
                                changed when creating a new payment.
                             */
                            $(function() {
                                $('#creditCard :input').change(function() {
                                    $('#creditCard\\.id').val('');
                                    $('#creditCard :input').unbind('change');
                                });
                            });
                        </script>
                    </g:if>

                </g:if>

                <!-- ach -->
                %{
                    def achAllowed = paymentMethods.find { it.id == Constants.PAYMENT_METHOD_ACH }
                }%

                <g:if test="${(achAllowed && isNew) || payment?.ach}">
                    <g:set var="ach" value="${payment?.ach}"/>

                    <div id="ach" class="box-cards ${ach ? 'box-cards-open' : ''} payment-type" onOpen="togglePaymentType('#ach');">
                        <div class="box-cards-title">
                            <a class="btn-open" href="#"><span><g:message code="prompt.ach"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:hiddenField name="ach.id" value="${ach?.id}"/>

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
                            </div>
                        </div>
                    </div>

                    <g:if test="${isNew && payment?.ach}">
                        <script type="text/javascript">
                            /*
                                Clear the default ach ID if any of the input fields are
                                changed when creating a new payment.
                             */
                            $(function() {
                                $('#ach :input').change(function() {
                                    $('#ach\\.id').val('');
                                    $('#ach :input').unbind('change');
                                });
                            });
                        </script>
                    </g:if>

                </g:if>

                <!-- cheque -->
                %{
                    def chequeAllowed = paymentMethods.find { it.id == Constants.PAYMENT_METHOD_CHEQUE }
                }%

                <g:if if="cheque" test="${(chequeAllowed && isNew) || payment?.cheque}">
                    <g:set var="cheque" value="${payment?.cheque}"/>

                    <div id="cheque" class="box-cards ${cheque ? 'box-cards-open' : ''} payment-type" onOpen="togglePaymentType('#cheque');">
                        <div class="box-cards-title">
                            <a class="btn-open"><span><g:message code="prompt.cheque"/></span></a>
                        </div>
                        <div class="box-card-hold">
                            <div class="form-columns">
                                <div class="column">
                                    <g:hiddenField name="cheque.id" value="${cheque?.id}"/>

                                    <g:applyLayout name="form/input">
                                        <content tag="label"><g:message code="prompt.cheque.bank"/></content>
                                        <content tag="label.for">cheque.bank</content>
                                        <g:textField class="field" name="cheque.bank" value="${cheque?.bank}"/>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/input">
                                        <content tag="label"><g:message code="prompt.cheque.number"/></content>
                                        <content tag="label.for">cheque.number</content>
                                        <g:textField class="field" name="cheque.number" value="${cheque?.number}"/>
                                    </g:applyLayout>

                                    <g:applyLayout name="form/date">
                                        <content tag="label"><g:message code="prompt.cheque.date"/></content>
                                        <content tag="label.for">cheque.date</content>
                                        <g:textField class="field" name="cheque.date" value="${formatDate(date: cheque?.date, formatName:'datepicker.format')}"/>
                                    </g:applyLayout>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>

                <!-- box text -->
                <div class="box-text">
                    <label for="payment.paymentNotes"><g:message code="payment.notes"/></label>
                    <g:textArea name="payment.paymentNotes" value="${payment?.paymentNotes}" rows="5" cols="60"/>
                </div>

                <div class="buttons">
                    <ul>
                        <li>
                            <a onclick="$('#payment-edit-form').submit()" class="submit payment">
                                <span><g:message code="button.review.payment"/></span>
                            </a>
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