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

<%@ page import="com.sapienter.jbilling.server.item.db.ItemDTO" %>

<%--
  Renders an OrderLineWS as an editable row for the order builder preview pane.

  @author Brian Cowdery
  @since 24-Jan-2011
--%>

<g:set var="product" value="${ItemDTO.get(line.itemId)}"/>
<g:set var="quantityNumberFormat" value="${product?.hasDecimals ? 'money.format' : 'default.number.format'}"/>
<g:set var="editable" value="${index == params.int('newLineIndex')}"/>

<g:formRemote name="line-${index}-update-form" url="[action: 'edit']" update="column2" method="GET">
    <g:hiddenField name="_eventId" value="updateLine"/>
    <g:hiddenField name="execution" value="${flowExecutionKey}"/>

    <li id="line-${index}" class="line ${editable ? 'active' : ''}">
        <span class="description">
            ${line.description}
        </span>
        <span class="sub-total">
            <g:set var="subTotal" value="${formatNumber(number: line.getAmountAsDecimal(), type: 'currency', currencySymbol: user.currency.symbol)}"/>
            <g:message code="order.review.line.total" args="[subTotal]"/>
        </span>
        <span class="qty-price">
            <g:set var="quantity" value="${formatNumber(number: line.getQuantityAsDecimal(), formatName: quantityNumberFormat)}"/>
            <g:if test="${product?.percentage}">
                <g:set var="percentage" value="%${formatNumber(number: product.percentage)}"/>
                <g:message code="order.review.quantity.by.price" args="[quantity, percentage]"/>
            </g:if>
            <g:else>
                <g:set var="price" value="${formatNumber(number: line.getPriceAsDecimal(), type: 'currency', currencySymbol: user.currency.symbol)}"/>
                <g:message code="order.review.quantity.by.price" args="[quantity, price]"/>
            </g:else>
        </span>
        <div style="clear: both;"></div>
    </li>

    <li id="line-${index}-editor" class="editor ${editable ? 'open' : ''}">
        <div class="box">
            <div class="form-columns">

                <sec:ifAllGranted roles="ORDER_26">
                    <g:applyLayout name="form/input">
                        <content tag="label"><g:message code="order.label.line.price"/></content>
                        <content tag="label.for">line-${index}.priceAsDecimal</content>
                        <g:textField name="line-${index}.priceAsDecimal" class="field" value="${formatNumber(number: line.getPriceAsDecimal() ?: BigDecimal.ZERO, formatName: 'money.format')}"/>
                    </g:applyLayout>
                </sec:ifAllGranted>

                <sec:ifAllGranted roles="ORDER_27">
                    <g:applyLayout name="form/input">
                        <content tag="label"><g:message code="order.label.line.descr"/></content>
                        <content tag="label.for">line-${index}.description</content>
                        <g:textField name="line-${index}.description" class="field" value="${line.description}"/>
                    </g:applyLayout>
                </sec:ifAllGranted>

                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="order.label.quantity"/></content>
                    <content tag="label.for">line-${index}.quantityAsDecimal</content>
                    <g:textField name="line-${index}.quantityAsDecimal" class="field" value="${formatNumber(number: line.getQuantityAsDecimal() ?: BigDecimal.ONE, formatName: quantityNumberFormat)}"/>
                </g:applyLayout>

                <g:hiddenField name="index" value="${index}"/>
            </div>
        </div>

        <div class="btn-box">
            <a class="submit save" onclick="$('#line-${index}-update-form').submit();"><span><g:message code="button.update"/></span></a>
            <g:remoteLink class="submit cancel" action="edit" params="[_eventId: 'removeLine', index: index]" update="column2" method="GET">
                <span><g:message code="button.remove"/></span>
            </g:remoteLink>
        </div>
    </li>

</g:formRemote>