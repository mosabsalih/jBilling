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

<%@ page import="com.sapienter.jbilling.server.util.Util" %>
<%@ page import="com.sapienter.jbilling.client.util.Constants" %>
<%@ page import="com.sapienter.jbilling.server.item.db.ItemDTO; com.sapienter.jbilling.server.item.db.ItemDAS"%>

<div class="column-hold">

    <g:set var="currency" value="${currencies.find{ it.id == order.currencyId}}"/>

    <div class="heading">
        <strong><g:message code="order.label.details"/>&nbsp;<em>${order?.id}</em></strong>
    </div>
    
    <!-- Order Details -->
    <div class="box">
        <table class="dataTable">
            <tr>
                <td colspan="2">
                    <strong>
                        <g:if test="${user?.contact?.firstName || user?.contact?.lastName}">
                            ${user.contact.firstName}&nbsp;${user.contact.lastName}
                        </g:if>
                        <g:else>
                            ${user?.userName}
                        </g:else>
                    </strong><br>
                    <em>${user?.contact?.organizationName}</em>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td><td></td>
            </tr>
            <tr>
                <td><g:message code="order.label.user.id"/>:</td>
                <td class="value">
                    <sec:access url="/customer/show">
                        <g:remoteLink controller="customer" action="show" id="${user?.id}" before="register(this);" onSuccess="render(data, next);">
                            ${user?.id}
                        </g:remoteLink>
                    </sec:access>
                    <sec:noAccess url="/customer/show">
                        ${user?.id}
                    </sec:noAccess>
                </td>
            </tr>
            <tr>
                <td><g:message code="order.label.user.name" />:</td>
                <td class="value">${user?.userName}</td>
            </tr>
        </table>
        
        <table class="dataTable">
            <tr><td><g:message code="order.label.create.date"/>:</td>
                <td class="value">
                    <g:formatDate date="${order?.createDate}" formatName="date.pretty.format"/>
                </td>
            </tr>
            <tr><td><g:message code="order.label.active.since"/>:</td>
                <td class="value">
                    <g:formatDate date="${order?.activeSince}" formatName="date.pretty.format"/>
                </td>
            </tr>
            <tr><td><g:message code="order.label.active.until"/>:</td>
                <td class="value">
                    <g:formatDate date="${order?.activeUntil}" formatName="date.pretty.format"/>
                </td>
            </tr>
            <tr><td><g:message code="order.label.cycle.start"/>:</td>
                <td class="value">
                    <g:formatDate date="${order?.cycleStarts}" formatName="date.pretty.format"/>
                </td>
            </tr>
            <tr><td><g:message code="order.label.next.invoice"/>:</td>
                <td class="value">
                    <g:if test="${order?.nextBillableDay}">
                        <g:formatDate date="${order?.nextBillableDay}"  formatName="date.pretty.format"/>
                    </g:if>
                    <g:else>
                        <g:formatDate date="${order?.cycleStarts ?: order?.activeSince ?: order?.createDate}"  formatName="date.pretty.format"/>
                    </g:else>
                </td>
            </tr>
            <tr>
                <td><g:message code="order.label.period"/>:</td><td class="value">${order.periodStr}</td>
            </tr>
            <tr>
                <td><g:message code="order.label.total"/>:</td>
                <td class="value">
                    <g:formatNumber number="${order.totalAsDecimal}" type="currency" currencySymbol="${currency.symbol}"/>
                </td>
            </tr>
            <tr>
                <td><g:message code="order.label.status"/>:</td>
                <td class="value">${order?.statusStr}</td>
            </tr>
            <tr>
                <td><g:message code="order.label.billing.type"/>:</td>
                <td class="value">${order?.billingTypeStr}</td>
            </tr>
        </table>
    </div>
    
    <div class="heading">
        <strong><g:message code="order.label.notes"/></strong>
    </div>
    
    <!-- Order Notes -->
    <div class="box">
        <g:if test="${order?.notes}">
            <p>${order?.notes}</p>
        </g:if>
        <g:else>
            <p><em><g:message code="order.prompt.no.notes"/></em></p>
        </g:else>
    </div>
    
    <div class="heading">
        <strong><g:message code="order.label.lines"/></strong>
    </div>
    
    <!-- Order Lines -->
    <div class="box">
        <g:if test="${order?.orderLines}">
            <table class="innerTable" >
                <thead class="innerHeader">
                     <tr>
                        <th><g:message code="order.label.line.item"/></th>
                        <th><g:message code="order.label.line.descr"/></th>
                        <th><g:message code="order.label.line.qty"/></th>
                        <th><g:message code="order.label.line.price"/></th>
                        <th><g:message code="order.label.line.total"/></th>
                     </tr>
                 </thead>
                 <tbody>
                     <g:each var="line" in="${order.orderLines}" status="idx">
                         <tr>
                            <td class="innerContent">
                                <g:set var="itemDto" value="${new ItemDAS().find(line?.itemId)}"/>
                                <sec:access url="/product/show">
                                   <g:remoteLink controller="product" action="show" id="${line?.itemId}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                        ${line?.itemId}
                                   </g:remoteLink>
                                </sec:access>
                                <sec:noAccess url="/product/show">
                                    ${line?.itemId}
                                </sec:noAccess>
                            </td>
                            <td class="innerContent">
                                ${line.description}
                            </td>
                            <td class="innerContent">
                                <g:formatNumber number="${line.quantityAsDecimal ?: BigDecimal.ZERO}" formatName="decimal.format"/>
                            </td>
                            <td class="innerContent">
                                <g:formatNumber number="${line.priceAsDecimal ?: BigDecimal.ZERO}" type="currency" currencySymbol="${currency.symbol}"/>
                            </td>
                            <td class="innerContent">
                                <g:formatNumber number="${line.amountAsDecimal ?: BigDecimal.ZERO}" type="currency" currencySymbol="${currency.symbol}"/>
                            </td>
                         </tr>
                     </g:each>
                 </tbody>
           </table>
        </g:if>
        <g:else>
            <em><g:message code="order.prompt.no.lines"/></em>
        </g:else>
    </div>
    
    <!-- Invoices Generated -->
    <g:if test="${order?.generatedInvoices}">
        <div class="heading">
            <strong><g:message code="order.label.invoices.generated"/></strong>
        </div>
        
        <div class="box">
            <table class="innerTable" >
                <thead class="innerHeader">
                     <tr>
                        <th><g:message code="order.invoices.id"/></th>
                        <th><g:message code="order.invoices.date"/></th>
                        <th><g:message code="order.invoices.total"/></th>
                     </tr>
                </thead>
                <tbody>
                     <g:each var="invoice" in="${order.generatedInvoices}" status="idx">
                         <g:set var="currency" value="${currencies.find{ it.id == invoice.currencyId}}"/>

                         <tr>
                            <td class="innerContent">
                                <sec:access url="/invoice/show">
                                    <g:remoteLink controller="invoice" action="show" id="${invoice.id}" before="register(this);" onSuccess="render(data, next);">
                                        ${invoice.id}
                                    </g:remoteLink>
                                </sec:access>
                                <sec:noAccess url="/invoice/show">
                                    ${invoice.id}
                                </sec:noAccess>
                            </td>
                            <td class="innerContent">
                                <g:formatDate format="dd-MMM-yyyy HH:mm:ss a" date="${invoice?.createDateTime}"/>
                            </td>
                            <td class="innerContent">
                                <g:formatNumber number="${invoice.totalAsDecimal}" type="currency" currencySymbol="${currency.symbol}"/>
                            </td>
                         </tr>
                     </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    
    <div class="btn-box">
        <div class="row">
            <sec:ifAllGranted roles="ORDER_23">
                <g:if test="${Constants.ORDER_STATUS_ACTIVE == order.statusId}">
                    <a href="${createLink (action: 'generateInvoice', params: [id: order?.id])}" class="submit order">
                        <span><g:message code="order.button.generate"/></span>
                    </a>
                    <a href="${createLink (action: 'applyToInvoice', params: [id: order?.id, userId: user?.id])}" class="submit order">
                        <span><g:message code="order.button.apply.invoice"/></span>
                    </a>
                </g:if>
            </sec:ifAllGranted>

            <sec:ifAllGranted roles="ORDER_21">
                <a href="${createLink (controller: 'orderBuilder', action: 'edit', params: [id: order?.id])}" class="submit edit">
                    <span><g:message code="order.button.edit"/></span>
                </a>
            </sec:ifAllGranted>
        </div>
        <div class="row">
            <sec:ifAllGranted roles="ORDER_22">
                <a onclick="showConfirm('deleteOrder-' + ${order?.id});" class="submit delete">
                    <span><g:message code="order.button.delete"/></span>
                </a>
            </sec:ifAllGranted>

            <g:link class="submit show" controller="mediation" action="order" id="${order.id}">
                <span><g:message code="button.view.events" /></span>
            </g:link>
	   </div>
    </div>
</div>

<g:render template="/confirm"
     model="[message: 'order.prompt.are.you.sure',
             controller: 'order',
             action: 'deleteOrder',
             id: order.id,
            ]"/>