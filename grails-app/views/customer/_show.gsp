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

<%@ page import="com.sapienter.jbilling.server.customer.CustomerBL; com.sapienter.jbilling.common.Constants; com.sapienter.jbilling.server.user.UserBL;" %>

<%--
  Shows details of a selected user.

  @author Brian Cowdery
  @since  23-Nov-2010
--%>

<g:set var="customer" value="${selected.customer}"/>

<div class="column-hold">
    <!-- user notes -->
    <div class="heading">
        <strong>
            <g:if test="${contact?.firstName || contact?.lastName}">
                ${contact.firstName} ${contact.lastName}
            </g:if>
            <g:else>
                ${selected.userName}
            </g:else>
            <em><g:if test="${contact}">${contact.organizationName}</g:if></em>
        </strong>
    </div>
    <div class="box edit">
        <g:remoteLink action="show" id="${selected.id}" params="[template: 'notes']" before="register(this);" onSuccess="render(data, next);" class="edit"/>
        <g:if test="${customer?.notes}">
            <p>${customer.notes}</p>
        </g:if>
        <g:else>
            <p><em><g:message code="customer.detail.note.empty.message"/></em></p>
        </g:else>
    </div>

    <!-- user details -->
    <div class="heading">
        <strong><g:message code="customer.detail.user.title"/></strong>
    </div>
    <div class="box">
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="customer.detail.user.user.id"/></td>
                    <td class="value">
                        <sec:access url="/customerInspector/inspect">
                            <g:link controller="customerInspector" action="inspect" id="${selected.id}" title="${message(code: 'customer.inspect.link')}">
                                ${selected.id}
                                <img src="${resource(dir: 'images', file: 'magnifier.png')}" alt="inspect customer"/>
                            </g:link>
                        </sec:access>
                        <sec:noAccess url="/customerInspector/inspect">
                            ${selected.id}
                        </sec:noAccess>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.user.username"/></td>
                    <td class="value">${selected.userName}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.user.status"/></td>
                    <td class="value">${selected.userStatus.description}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.user.created.date"/></td>
                    <td class="value"><g:formatDate date="${selected.createDatetime}" formatName="date.pretty.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.user.email"/></td>
                    <td class="value"><a href="mailto:${contact?.email}">${contact?.email}</a></td>
                </tr>

                <g:if test="${customer?.parent}">
                    <!-- empty spacer row --> 
                    <tr>
                        <td colspan="2"><br/></td>
                    </tr>
                    <tr>
                        <td><g:message code="prompt.parent.id"/></td>
                        <td class="value">
                            <g:remoteLink action="show" id="${customer.parent.baseUser.id}" before="register(this);" onSuccess="render(data, next);">
                                ${customer.parent.baseUser.id} - ${customer.parent.baseUser.userName}
                            </g:remoteLink>
                        </td>
                    </tr>
                    <tr>
                        <td><g:message code="customer.invoice.if.child.label"/></td>
                        <td class="value">
                            <g:if test="${customer.invoiceChild > 0}">
                                <g:message code="customer.invoice.if.child.true"/>
                            </g:if>
                            <g:else>
                                <g:set var="parent" value="${new CustomerBL(customer.id).getInvoicableParent()}"/>
                                <g:remoteLink action="show" id="${parent.baseUser.id}" before="register(this);" onSuccess="render(data, next);">
                                    <g:message code="customer.invoice.if.child.false" args="[ parent.baseUser.id ]"/>
                                </g:remoteLink>
                            </g:else>
                        </td>
                    </tr>
                </g:if>

                <g:if test="${customer?.children}">
                    <!-- empty spacer row --> 
                    <tr>
                        <td colspan="2"><br/></td>
                    </tr>
                    
                    <!-- direct sub-accounts -->
                    <g:each var="account" in="${customer.children}">
                        <tr>
                            <td><g:message code="customer.subaccount.title" args="[ account.baseUser.id ]"/></td>
                            <td class="value">
                                <g:remoteLink action="show" id="${account.baseUser.id}" before="register(this);" onSuccess="render(data, next);">
                                    ${account.baseUser.userName}
                                </g:remoteLink>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
            </tbody>
        </table>
    </div>

    <!-- user payment details -->
    <div class="heading">
        <strong><g:message code="customer.detail.payment.title"/></strong>
    </div>
    <div class="box">
        <!-- show most recent order, invoice and payment -->
        <g:set var="order" value="${selected.orders ? selected.orders.asList().sort{ it.createDate }.last() : null}"/>
        <g:set var="invoice" value="${selected.invoices ? selected.invoices.asList().sort{ it.createDatetime }.last() : null}"/>
        <g:set var="payment" value="${selected.payments ? selected.payments.asList().sort{ it.paymentDate ?: it.createDatetime }.last() : null}"/>

        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
            <tr>
                <td>Last Order Date</td>

                <td class="value">
                    <sec:access url="/order/show">
                        <g:remoteLink controller="order" action="show" id="${order?.id}" before="register(this);" onSuccess="render(data, next);">
                            <g:formatDate date="${order?.createDate}" formatName="date.pretty.format"/>
                        </g:remoteLink>
                    </sec:access>
                    <sec:noAccess url="/order/show">
                        <g:formatDate date="${order?.createDate}" formatName="date.pretty.format"/>
                    </sec:noAccess>
                </td>
                <td class="value">
                    <sec:access url="/order/list">
                        <g:link controller="order" action="user" id="${selected.id}">
                            <g:message code="customer.show.all.orders"/>
                        </g:link>
                    </sec:access>
                </td>
            </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.invoiced.date"/></td>
                    <td class="value">
                        <sec:access url="/invoice/show">
                            <g:remoteLink controller="invoice" action="show" id="${invoice?.id}" before="register(this);" onSuccess="render(data, next);">
                                <g:formatDate date="${invoice?.createDatetime}" formatName="date.pretty.format"/>
                            </g:remoteLink>
                        </sec:access>
                        <sec:noAccess url="/invoice/show">
                            <g:formatDate date="${invoice?.createDatetime}" formatName="date.pretty.format"/>
                        </sec:noAccess>
                    </td>
                    <td class="value">
                        <sec:access url="/invoice/list">
                            <g:link controller="invoice" action="user" id="${selected.id}">
                                <g:message code="customer.show.all.invoices"/>
                            </g:link>
                        </sec:access>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.paid.date"/></td>
                    <td class="value">
                        <sec:access url="/payment/show">
                            <g:remoteLink controller="payment" action="show" id="${payment?.id}" before="register(this);" onSuccess="render(data, next);">
                                <g:formatDate date="${payment?.paymentDate ?: payment?.createDatetime}" formatName="date.pretty.format"/>
                            </g:remoteLink>
                        </sec:access>
                        <sec:noAccess url="/payment/show">
                            <g:formatDate date="${payment?.paymentDate ?: payment?.createDatetime}" formatName="date.pretty.format"/>
                        </sec:noAccess>
                    </td>
                    <td class="value">
                        <sec:access url="/payment/list">
                            <g:link controller="payment" action="user" id="${selected.id}">
                                <g:message code="customer.show.all.payments"/>
                            </g:link>
                        </sec:access>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.due.date"/></td>
                    <td class="value"><g:formatDate date="${invoice?.dueDate}" formatName="date.pretty.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.invoiced.amount"/></td>
                    <td class="value"><g:formatNumber number="${invoice?.total}" type="currency" currencySymbol="${selected.currency.symbol}"/></td>
                </tr>
                <tr>
                    <td><g:message code="invoice.label.status"/></td>
                    <td class="value">
                        <g:if test="${invoice?.invoiceStatus?.id == Constants.INVOICE_STATUS_UNPAID}">
                            <g:link controller="payment" action="edit" params="[userId: selected.id, invoiceId: invoice.id]" title="${message(code: 'invoice.pay.link')}">
                                ${invoice.invoiceStatus.getDescription(session['language_id'])}
                            </g:link>
                        </g:if>
                        <g:else>
                            ${invoice?.invoiceStatus?.getDescription(session['language_id'])}
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.amount.owed"/></td>
                    <td class="value"><g:formatNumber number="${new UserBL().getBalance(selected.id)}" type="currency"  currencySymbol="${selected.currency.symbol}"/></td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.payment.lifetime.revenue"/></td>
                    <td class="value"><g:formatNumber number="${revenue}" type="currency"  currencySymbol="${selected.currency.symbol}"/></td>
                </tr>
            </tbody>
        </table>

        <hr/>

        <g:set var="card" value="${selected.creditCards ? selected.creditCards.asList().first() : null}"/>
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="customer.detail.payment.credit.card"/></td>
                    <td class="value">
                        %{-- obscure credit card by default, or if the preference is explicitly set --}%
                        <g:if test="${card?.number && preferenceIsNullOrEquals(preferenceId: Constants.PREFERENCE_HIDE_CC_NUMBERS, value: 1, true)}">
                            <g:set var="creditCardNumber" value="${card.number.replaceAll('^\\d{12}','************')}"/>
                            ${creditCardNumber}
                        </g:if>
                        <g:else>
                            ${card?.number}
                        </g:else>
                    </td>
                </tr>

                <tr>
                    <td><g:message code="customer.detail.payment.credit.card.expiry"/></td>
                    <td class="value"><g:formatDate date="${card?.ccExpiry}" formatName="credit.card.date.format"/></td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- contact details -->    
    <div class="heading">
        <strong><g:message code="customer.detail.contact.title"/></strong>
    </div>
    <g:if test="${contact}">
    <div class="box">

        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="customer.detail.contact.telephone"/></td>
                    <td class="value">
                        <g:if test="${contact.phoneCountryCode}">${contact.phoneCountryCode}.</g:if>
                        <g:if test="${contact.phoneAreaCode}">${contact.phoneAreaCode}.</g:if>
                        ${contact.phoneNumber}
                    </td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.contact.address"/></td>
                    <td class="value">${contact.address1} ${contact.address2}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.contact.city"/></td>
                    <td class="value">${contact.city}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.contact.state"/></td>
                    <td class="value">${contact.stateProvince}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.contact.country"/></td>
                    <td class="value">${contact.countryCode}</td>
                </tr>
                <tr>
                    <td><g:message code="customer.detail.contact.zip"/></td>
                    <td class="value">${contact.postalCode}</td>
                </tr>
            </tbody>
        </table>
    </div>
    </g:if>

    <div class="btn-box">
        <div class="row">
            <sec:ifAllGranted roles="ORDER_20">
                <g:link controller="orderBuilder" action="edit" params="[userId: selected.id]" class="submit order"><span><g:message code="button.create.order"/></span></g:link>
            </sec:ifAllGranted>

            <sec:ifAllGranted roles="PAYMENT_30">
                <g:link controller="payment" action="edit" params="[userId: selected.id]" class="submit payment"><span><g:message code="button.make.payment"/></span></g:link>
            </sec:ifAllGranted>
        </div>
        <div class="row">
            <sec:ifAllGranted roles="CUSTOMER_11">
                <g:link action="edit" id="${selected.id}" class="submit edit"><span><g:message code="button.edit"/></span></g:link>
            </sec:ifAllGranted>

            <sec:ifAllGranted roles="CUSTOMER_12">
                <a onclick="showConfirm('delete-${selected.id}');" class="submit delete"><span><g:message code="button.delete"/></span></a>
            </sec:ifAllGranted>

            <sec:ifAllGranted roles="CUSTOMER_10">
                <g:if test="${customer?.isParent > 0}">
                    <g:link action="edit" params="[parentId: selected.id]" class="submit add"><span><g:message code="customer.add.subaccount.button"/></span></g:link>
                </g:if>
            </sec:ifAllGranted>
        </div>
    </div>

    <g:render template="/confirm"
              model="['message': 'customer.delete.confirm',
                      'controller': 'customer',
                      'action': 'delete',
                      'id': selected.id,
                      'ajax': true,
                      'update': 'column1',
                      'onYes': 'closePanel(\'#column2\')'
                     ]"/>

</div>