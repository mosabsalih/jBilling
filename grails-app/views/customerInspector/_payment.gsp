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
  Payment data-table template for the customer inspector.

  @author Brian Cowdery
  @since  12-Jan-2011
--%>

<table class="dataTable" cellspacing="0" cellpadding="0">
    <tbody>
    <tr>
        <td><g:message code="payment.date"/></td>
        <td class="value"><g:formatDate date="${payment?.paymentDate ?: payment?.createDatetime}" formatName="date.pretty.format"/></td>

        <td><g:message code="payment.id"/></td>
        <td class="value"><g:link controller="payment" action="list" id="${payment?.id}">${payment?.id}</g:link></td>
    </tr>
    <tr>
        <td><g:message code="payment.amount"/></td>
        <td class="value"><g:formatNumber number="${payment?.amount}" type="currency" currencySymbol="${payment?.currency?.symbol}"/> &nbsp;</td>

        <td><g:message code="payment.balance"/></td>
        <td class="value"><g:formatNumber number="${payment?.balance}" type="currency" currencySymbol="${payment?.currency?.symbol}"/> &nbsp;</td>
    </tr>
    <tr>
        <td><g:message code="payment.result"/></td>
        <td class="value">${payment?.paymentResult.getDescription(session['language_id'])}</td>

        <td><g:message code="payment.attempt"/></td>
        <td class="value">${payment?.attempt ?: 0}</td>
    </tr>
    </tbody>
</table>