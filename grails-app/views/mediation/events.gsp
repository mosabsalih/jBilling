<html>
<head>
	<meta name="layout" content="main"/>
</head>
<body>

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

<g:set var="currency" value="${invoice?.currency ?: order?.currency}"/>

    %{-- Invoice summary if invoice set --}%
    <g:if test="${invoice}">
        <div class="table-info" >
            <em>
                <g:message code="event.summary.invoice.id"/>
                <strong>${invoice.id}</strong>
            </em>
            <em>
                <g:message code="event.summary.invoice.due.date"/>
                <strong><g:formatDate date="${invoice.dueDate}" formatName="date.pretty.format"/></strong>
            </em>
            <em>
                <g:message code="event.summary.invoice.total"/>
                <strong><g:formatNumber number="${invoice.total}" type="currency" currencySymbol="${currency.symbol}"/></strong>
            </em>
        </div>
    </g:if>

    %{-- Order summary if order set --}%
    <g:if test="${order}">
        <div class="table-info" >
            <em>
                <g:message code="event.summary.order.id"/>
                <strong>${order.id}</strong>
            </em>
            <em>
                <g:message code="event.summary.order.total"/>
                <strong><g:formatNumber number="${order.total}" type="currency" currencySymbol="${currency.symbol}"/></strong>
            </em>
        </div>
    </g:if>

    <div class="table-area">
        <table>
            <thead>
                <tr>
                    <td class="first"><g:message code="event.th.id"/></td>
                    <td><g:message code="event.th.key"/></td>
                    <td><g:message code="event.th.date"/></td>
                    <td><g:message code="event.th.description"/></td>
                    <td><g:message code="event.th.quantity"/></td>
                    <td class="last"><g:message code="event.th.amount"/></td>
                </tr>
            </thead>
            <tbody>

                <!-- events list -->
                <g:set var="totalQuantity" value="${BigDecimal.ZERO}"/>
                <g:set var="totalAmount" value="${BigDecimal.ZERO}"/>

                <g:each var="record" in="${records}">
                    <g:each var="event" in="${record.lines}">

                        <g:set var="totalQuantity" value="${totalQuantity.add(event.quantity)}"/>
                        <g:set var="totalAmount" value="${totalAmount.add(event.amount)}"/>

                        <tr>
                            <td class="col02">
                                ${event.id}
                            </td>
                            <td>
                                ${record.key}
                            </td>
                            <td>
                                <g:formatDate date="${event.eventDate}" formatName="date.pretty.format"/>
                            </td>
                            <td class="col03">
                                ${event.description ?: '-'}
                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber number="${event.quantity}" formatName="decimal.format"/>
                                </strong>
                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber number="${event.amount}"  type="currency" currencySymbol="${currency.symbol}"/>
                                </strong>
                            </td>
                        </tr>

                    </g:each>
                </g:each>

                <!-- subtotals -->
                <tr class="bg">
                    <td class="col02"></td>
                    <td></td>
                    <td></td>
                    <td></td>

                    <td>
                        <strong><g:formatNumber number="${totalQuantity}" formatName="decimal.format"/></strong>
                    </td>
                    <td>
                        <strong><g:formatNumber number="${totalAmount}" type="currency" currencySymbol="${currency.symbol}"/></strong>
                    </td>
                </tr>

            </tbody>
        </table>
    </div>

</body>
</html>