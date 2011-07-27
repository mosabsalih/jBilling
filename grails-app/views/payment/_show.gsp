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

<%@ page import="com.sapienter.jbilling.common.Constants; com.sapienter.jbilling.server.user.contact.db.ContactDTO" %>

<%--
  Shows details of a selected payment.

  @author Brian Cowdery
  @since 04-Jan-2011
--%>

<g:set var="customer" value="${selected.baseUser.customer}"/>
<g:set var="contact" value="${ContactDTO.findByUserId(selected.baseUser.id)}"/>

<div class="column-hold">
    <div class="heading">
        <strong>
            <g:if test="${selected.isRefund > 0}">
                <g:message code="payment.refund.title"/>
            </g:if>
            <g:else>
                <g:message code="payment.payment.title"/>
            </g:else>
            <em>${selected.id}</em>
        </strong>
    </div>

    <div class="box">
        <!-- user details -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <g:if test="${contact?.firstName || contact?.lastName}">
                    <tr>
                        <td><g:message code="prompt.customer.name"/></td>
                        <td class="value">${contact.firstName} ${contact.lastName}</td>
                    </tr>
                </g:if>

                <g:if test="${contact?.organizationName}">
                    <tr>
                        <td><g:message code="prompt.organization.name"/></td>
                        <td class="value">${contact.organizationName}</td>
                    </tr>
                </g:if>
                <tr>
                    <td><g:message code="payment.user.id"/></td>
                    <td class="value">
                        <sec:access url="/customer/show">
                            <g:remoteLink controller="customer" action="show" id="${selected?.baseUser?.id}" before="register(this);" onSuccess="render(data, next);">
                                ${selected.baseUser.id}
                            </g:remoteLink>
                        </sec:access>
                        <sec:noAccess url="/customer/show">
                            ${selected.baseUser.id}
                        </sec:noAccess>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="payment.label.user.name"/></td>
                    <td class="value">${selected.baseUser.userName}</td>
                </tr>
            </tbody>
        </table>

        <!-- payment details -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="payment.date"/></td>
                    <td class="value"><g:formatDate date="${selected.paymentDate ?: selected.createDatetime}" formatName="date.pretty.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="payment.amount"/></td>
                    <td class="value"><g:formatNumber number="${selected.amount}" type="currency" currencySymbol="${selected.currencyDTO.symbol}"/></td>
                </tr>
                <tr>
                    <td><g:message code="payment.result"/></td>
                    <td class="value">${selected.paymentResult.getDescription(session['language_id'])}</td>
                </tr>
            </tbody>
        </table>

        <hr/>

        <!-- payment balance -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="payment.id"/></td>
                    <td class="value">${selected.id}</td>
                </tr>
                <tr>
                    <td><g:message code="payment.balance"/></td>
                    <td class="value">
                        <g:formatNumber number="${selected.balance}" type="currency" currencySymbol="${selected.currencyDTO.symbol}"/>

                        <sec:access url="/payment/link">
                            <g:if test="${selected.balance.compareTo(BigDecimal.ZERO) > 0}">
                                &nbsp; - &nbsp;
                                <g:link controller="payment" action="link" id="${selected.id}">
                                    <g:message code="payment.link.invoice.pay" />
                                </g:link>
                            </g:if>
                        </sec:access>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="payment.attempt"/></td>
                    <td class="value">${selected.attempt ?: 0}</td>
                </tr>
                <tr>
                    <td><g:message code="payment.is.preauth"/></td>
                    <td class="value"><em><g:formatBoolean boolean="${selected.isPreauth > 0}"/></em></td>
                </tr>
            </tbody>
        </table>

        <!-- list of linked invoices -->
        <g:if test="${selected.invoicesMap}">
            <table cellpadding="0" cellspacing="0" class="innerTable">
                <thead class="innerHeader">
                    <tr>
                        <th><g:message code="payment.invoice.payment"/></th>
                        <th><g:message code="payment.invoice.payment.amount"/></th>
                        <th><g:message code="payment.invoice.payment.date"/></th>
                        <th><!-- action --> &nbsp;</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each var="invoicePayment" in="${selected.invoicesMap}">
                    <tr>
                        <td class="innerContent">
                            <sec:access url="/invoice/show">
                                <g:remoteLink controller="invoice" action="show" id="${invoicePayment.invoiceEntity.id}" before="register(this);" onSuccess="render(data, next);">
                                    <g:message code="payment.link.invoice" args="[invoicePayment.invoiceEntity.number]"/>
                                </g:remoteLink>
                            </sec:access>
                            <sec:noAccess url="/invoice/show">
                                <g:message code="payment.link.invoice" args="[invoicePayment.invoiceEntity.number]"/>
                            </sec:noAccess>
                        </td>
                        <td class="innerContent">
                            <g:formatNumber number="${invoicePayment.amount}" type="currency" currencySymbol="${selected.currencyDTO.symbol}"/>
                        </td>
                        <td class="innerContent">
                            <g:formatDate date="${invoicePayment.createDatetime}"/>
                        </td>
                        <td class="innerContent">
                            <sec:access url="/payment/unlink">
                                <g:remoteLink action="unlink" id="${selected.id}" params="[invoiceId: invoicePayment.invoiceEntity.id]" before="register(this);" onSuccess="render(data, second);">
                                    <g:message code="payment.link.unlink"/>
                                </g:remoteLink>
                            </sec:access>
                        </td>
                    </tr>
                    </g:each>
                </tbody>
            </table>
        </g:if>
    </div>

    <!-- payment notes -->
    <g:if test="${selected.paymentNotes}">
        <div class="heading">
            <strong><g:message code="payment.notes"/></strong>
        </div>
        <div class="box">
            <p>${selected.paymentNotes}</p>
        </div>
    </g:if>

    <!-- payment authorization -->
    <g:if test="${selected.paymentAuthorizations}">
        <g:set var="authorization" value="${selected.paymentAuthorizations.sort { it.createdDate }?.first()}"/>

        <div class="heading">
            <strong><g:message code="payment.authorization.title" /></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="payment.authorization.date" /></td>
                        <td class="value"><g:formatDate date="${authorization.createDate}"/></td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.processor" /></td>
                        <td class="value">${authorization.processor}</td>
                    </tr>
                    <g:if test="${authorization.code1}">
                        <tr>
                            <td><g:message code="payment.code.1" /></td>
                            <td class="value">${authorization.code1}</td>
                        </tr>
                    </g:if>
                    <g:if test="${authorization.code2}">
                        <tr>
                            <td><g:message code="payment.code.2" /></td>
                            <td class="value">${authorization.code2}</td>
                        </tr>
                    </g:if>
                    <g:if test="${authorization.code3}">
                        <tr>
                            <td><g:message code="payment.code.3" /></td>
                            <td class="value">${authorization.code3}</td>
                        </tr>
                    </g:if>
                    <tr>
                        <td><g:message code="payment.approval.code" /></td>
                        <td class="value">${authorization.approvalCode}</td>
                    </tr>
                    <g:if test="${authorization.avs}">
                        <tr>
                            <td><g:message code="payment.avs.code" /></td>
                            <td class="value">${authorization.avs}</td>
                        </tr>
                    </g:if>
                    <g:if test="${authorization.cardCode}">
                        <tr>
                            <td><g:message code="payment.card.code" /></td>
                            <td class="value">${authorization.cardCode}</td>
                        </tr>
                    </g:if>
                    <g:if test="${authorization.md5}">
                        <tr>
                            <td><g:message code="payment.md5.sum" /></td>
                            <td class="value">${authorization.md5}</td>
                        </tr>
                    </g:if>
                    <g:if test="${authorization.transactionId}">
                        <tr>
                            <td><g:message code="payment.transaction.id" /></td>
                            <td class="value"> ${authorization.transactionId}</td>
                        </tr>
                    </g:if>
                    <tr>
                        <td><g:message code="payment.response.message" /></td>
                        <td class="value">${authorization.responseMessage}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>


    <!-- credit card details -->
    <g:if test="${selected.creditCard}">
        <g:set var="creditCard" value="${selected.creditCard}"/>

        <div class="heading">
            <strong><g:message code="payment.credit.card"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="payment.credit.card.name"/></td>
                        <td class="value">${creditCard.name}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.credit.card.type"/></td>
                        <td class="value">${selected.paymentMethod.getDescription(session['language_id'])}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.credit.card.number"/></td>
                        <td class="value">
                            %{-- obscure credit card by default, or if the preference is explicitly set --}%
                            <g:if test="${preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                                <g:set var="creditCardNumber" value="${creditCard.number.replaceAll('^\\d{12}','************')}"/>
                                ${creditCardNumber}
                            </g:if>
                            <g:else>
                                ${creditCard.number}
                            </g:else>
                        </td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.credit.card.expiry"/></td>
                        <td class="value"><g:formatDate date="${creditCard.ccExpiry}" formatName="credit.card.date.format"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <!-- ACH banking details -->
    <g:if test="${selected.ach}">
        <g:set var="ach" value="${selected.ach}"/>

        <div class="heading">
            <strong><g:message code="payment.ach"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="payment.ach.account.name"/></td>
                        <td class="value">${ach.accountName}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.ach.bank.name"/></td>
                        <td class="value">${ach.bankName}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.ach.routing.number"/></td>
                        <td class="value">${ach.abaRouting}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.ach.account.number"/></td>
                        <td class="value">${ach.bankAccount}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.ach.account.type"/></td>
                        <td class="value">
                            <g:if test="${ach.accountType == 1}">
                                <g:message code="label.account.checking"/>
                            </g:if>
                            <g:else>
                                <g:message code="label.account.savings"/>
                            </g:else>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <!-- cheque details -->
    <g:if test="${selected.paymentInfoCheque}">
        <g:set var="cheque" value="${selected.paymentInfoCheque}"/>

        <div class="heading">
            <strong><g:message code="payment.cheque"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="payment.cheque.bank"/></td>
                        <td class="value">${cheque.bank}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.cheque.number"/></td>
                        <td class="value">${cheque.chequeNumber}</td>
                    </tr>
                    <tr>
                        <td><g:message code="payment.cheque.date"/></td>
                        <td class="value"><g:formatDate date="${cheque.date}" formatName="date.pretty.format"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <div class="btn-box">
        <!-- edit or delete unlinked payments -->
        <div class="row">
            <g:if test="${!selected.invoicesMap}">
                <sec:ifAllGranted roles="PAYMENT_31">
                    <g:if test="${selected.paymentResult.id == Constants.RESULT_ENTERED}">
                        <g:link action="edit" id="${selected.id}" class="submit edit"><span><g:message code="button.edit"/></span></g:link>
                    </g:if>
                </sec:ifAllGranted>

                <sec:ifAllGranted roles="PAYMENT_32">
                    <a onclick="showConfirm('delete-${selected.id}');" class="submit delete"><span><g:message code="button.delete"/></span></a>
                </sec:ifAllGranted>
            </g:if>
            <g:else>
                <em><g:message code="payment.cant.edit.linked"/></em>
            </g:else>
        </div>
    </div>

    <g:render template="/confirm"
              model="[message: 'payment.delete.confirm',
                      controller: 'payment',
                      action: 'delete',
                      id: selected.id,
                      ajax: true,
                      update: 'column1',
                      onYes: 'closePanel(\'#column2\')'
                     ]"/>

</div>