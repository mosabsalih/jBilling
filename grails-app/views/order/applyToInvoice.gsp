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
            $("#invoice-details").replaceWith('<div id="invoice-details" class="box-card-hold"></div>')
        }

        function onInvoiceChange(invId) {
            $.ajax({
                url: "/jbilling/invoice/snapshot/" + invId,
                global: false,
                success: function(data) { $("#invoice-details").replaceWith(data) }
            });
            $('#invoice-details').visibility='visible';
        }
        
        $(document).ready(function() {
            //radio select or change
            $(':input[type=radio][name=invoiceId]').change(function() {
                //alert('Selected Invoice ID ' + $(this).val());
                $.ajax({
                    url: "/jbilling/invoice/snapshot/" + $(this).val(),
                    global: false,
                    success: function(data) { $("#invoice-details").replaceWith(data) }
                });
                $('#invoice-details').visibility='visible';
            });
        });
    </script>
</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:message code="order.label.apply.to.invoice" args="[orderId]"/>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="order-invoice-form" action="confirm">
            <fieldset>
                <!-- invoices to pay -->
                <g:if test="${invoices}">
                    <div class="box-card-hold">
                        <table cellpadding="0" cellspacing="0" class="innerTable">
                            <thead class="innerHeader">
                            <tr>
                                <th><g:message code="invoice.label.number"/></th>
                                <%-- <th><g:message code="invoice.label.payment.attempts"/></th>  --%>
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
                                    </td><%--
                                    <td class="innerContent">
                                        ${invoice.paymentAttempts}
                                    </td> --%>
                                    <td class="innerContent">
                                        <g:formatNumber number="${invoice.getTotalAsDecimal()}" type="currency" currencyCode="${currency.code}"/>
                                        <g:hiddenField name="invoice-${invoice.id}-amount" value="${formatNumber(number: invoice.total, formatName: 'money.format')}"/>
                                    </td>
                                    <td class="innerContent">
                                        <g:formatNumber number="${invoice.getBalanceAsDecimal()}" type="currency" currencyCode="${currency.code}"/>
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
                </g:if>

            </fieldset>
                <div id="invoice-details" style="visibility:hidden;" class="box-card-hold">
                </div>
        </g:form>
    </div>
</div>

</body>
</html>
