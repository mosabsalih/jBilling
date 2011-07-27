<%@ page import="com.sapienter.jbilling.server.user.contact.db.ContactDTO" %>

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
  Payment list table.

  @author Brian Cowdery
  @since  04-Jan-2011
--%>

<div class="table-box">
    <div class="table-scroll">
        <table id="payments" cellspacing="0" cellpadding="0">
            <thead>
                <tr>
                    <th class="small">
                        <g:remoteSort action="list" sort="id" update="column1">
                            <g:message code="payment.th.id"/>
                        </g:remoteSort>
                    </th>
                    <th class="large">
                        <g:remoteSort action="list" sort="contact.firstName, contact.lastName, contact.organizationName, baseUser.userName" alias="[contact: 'baseUser.contact']" update="column1">
                            <g:message code="invoice.label.customer"/>
                        </g:remoteSort>
                    </th>
                    <th class="medium">
                        <g:remoteSort action="list" sort="paymentDate" update="column1">
                            <g:message code="payment.th.date"/>
                        </g:remoteSort>
                    </th>
                    <th class="tiny">
                        <g:remoteSort action="list" sort="isRefund" update="column1">
                            <g:message code="payment.th.payment.or.refund"/>
                        </g:remoteSort>
                    </th>
                    <th class="small">
                        <g:remoteSort action="list" sort="amount" update="column1">
                            <g:message code="payment.th.amount"/>
                        </g:remoteSort>
                    </th>
                    <th class="small">
                        <g:remoteSort action="list" sort="paymentMethod.id" update="column1">
                            <g:message code="payment.th.method"/>
                        </g:remoteSort>
                    </th>
                    <th class="small">
                        <g:remoteSort action="list" sort="paymentResult.id" update="column1">
                            <g:message code="payment.th.result"/>
                        </g:remoteSort>
                    </th>
                </tr>
            </thead>

            <tbody>
            <g:each var="payment" in="${payments}">
                
                <g:set var="contact" value="${ContactDTO.findByUserId(payment?.baseUser?.id)}"/>
                
                <tr id="payment-${payment.id}" class="${selected?.id == payment.id ? 'active' : ''}">

                    <td>
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <span>${payment.id}</span>
                        </g:remoteLink>
                    </td>
                    <td>
                        <g:remoteLink breadcrumb="id" class="cell double" action="show" id="${payment.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                            <strong>
                                <g:if test="${contact?.firstName || contact?.lastName}">
                                    ${contact.firstName} &nbsp;${contact.lastName}
                                </g:if>
                                <g:else>
                                    ${payment?.baseUser?.userName}
                                </g:else>
                            </strong>
                            <em>${contact?.organizationName}</em>
                        </g:remoteLink>
                    </td>
                    <td class="medium">
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <span><g:formatDate date="${payment.paymentDate}" formatName="date.pretty.format"/></span>
                        </g:remoteLink>
                    </td>
                    <td class="tiny">
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <g:if test="${payment.isRefund > 0}">
                                <span>R</span>
                            </g:if>
                            <g:else>
                                <span>P</span>
                            </g:else>
                        </g:remoteLink>
                    </td>
                    <td class="small">
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <span><g:formatNumber number="${payment.amount}" type="currency" currencySymbol="${payment.currencyDTO.symbol}"/></span>
                        </g:remoteLink>
                    </td>
                    <td class="small">
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <span>${payment.paymentMethod.getDescription(session['language_id'])}</span>
                        </g:remoteLink>
                    </td>
                    <td class="small">
                        <g:remoteLink class="cell" action="show" id="${payment.id}" before="register(this);" onSuccess="render(data, next);">
                            <span>${payment.paymentResult.getDescription(session['language_id'])}</span>
                        </g:remoteLink>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>

<div class="pager-box">
    <div class="row">
        <div class="results">
            <g:render template="/layouts/includes/pagerShowResults" model="[steps: [10, 20, 50], update: 'column1']"/>
        </div>
        <div class="download">
            <sec:access url="/payment/csv">
                <g:link action="csv" id="${selected?.id}">
                    <g:message code="download.csv.link"/>
                </g:link>
            </sec:access>
        </div>
    </div>

    <div class="row">
        <util:remotePaginate controller="payment" action="list" params="${sortableParams(params: [partial: true])}" total="${payments?.totalCount ?: 0}" update="column1"/>
    </div>
</div>

<div class="btn-box">
    <sec:access url="/payment/create">
        <g:link action="create" class="submit payment"><span><g:message code="button.create.payment"/></span></g:link>
    </sec:access>
</div>