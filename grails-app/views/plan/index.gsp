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

<%@ page import="com.sapienter.jbilling.server.user.contact.db.ContactDTO" %>
<html>
<head>
    <meta name="layout" content="panels" />
</head>
<body>

<content tag="column1">
    <div class="table-box">
        <div class="table-scroll">
            <table id="plans" cellspacing="0" cellpadding="0">
                <thead>
                    <tr>
                        <th><g:message code="plan.th.name"/></th>
                        <th class="medium"><g:message code="plan.th.item.number"/></th>
                        <th class="small"><g:message code="plan.th.products"/></th>
                    </tr>
                </thead>

                <tbody>
                    <tr id="plan-1" class="active">
                        <td>
                            <strong>Simple discount plan</strong>
                            <em><g:message code="table.id.format" args="[1]"/></em>
                        </td>
                        <td>
                            <strong>PLAN-A</strong>
                        </td>
                        <td>
                            <span>2</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="btn-box">
        <a href="#" class="submit add"><span><g:message code="button.create"/></span></a>
    </div>
</content>

<content tag="column2">
    <div class="column-hold">
        <div class="heading">
            <strong>PLAN-A</strong>
        </div>

        <!-- plan details -->
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0">
                <tbody>
                    <tr>
                        <td><g:message code="plan.id"/></td>
                        <td class="value">1</td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.item.internal.number"/></td>
                        <td class="value">PLAN-A</td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.item.description"/></td>
                        <td class="value">Simple discount plan</td>
                    </tr>
                    <tr>
                        <td><g:message code="order.label.period"/></td>
                        <td class="value">Monthly</td>
                    </tr>
                    <tr>
                        <td>USD</td>
                        <td class="value">
                            $20.00
                        </td>
                    </tr>
                </tbody>
            </table>

            <p class="description">
                Example pricing plan that discounts multiple items when subscribed to.
            </p>
        </div>

        <!-- plan prices -->
        <div class="heading">
            <strong><g:message code="builder.products.title"/></strong>
        </div>
        <div class="box">
            <table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
                <tbody>
                    <!-- example long distance call pricing -->
                    <tr>
                        <td><g:message code="product.internal.number"/></td>
                        <td class="value" colspan="3">
                            CALL-LD
                        </td>
                    </tr>
                    <tr>
                        <td><g:message code="product.description"/></td>
                        <td class="value" colspan="3">
                            Long distance call
                        </td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.item.precedence"/></td>
                        <td class="value">-1</td>
                    </tr>
                    <tr>
                        <td colspan="4">&nbsp;</td>
                    </tr>
                    <tr class="price">
                        <td><g:message code="plan.model.type"/></td>
                        <td class="value"><g:message code="price.strategy.GRADUATED"/></td>
                        <td><g:message code="plan.model.rate"/></td>
                        <td class="value">$0.50</td>
                    </tr>
                    <tr class="attribute">
                        <td></td><td></td>
                        <td><g:message code="included"/></td>
                        <td class="value">500</td>
                    </tr>

                    <tr><td colspan="4"><hr/></td></tr>

                    <!-- example service fee -->
                    <tr>
                        <td><g:message code="product.internal.number"/></td>
                        <td class="value" colspan="3">SERVICE-FEE</td>
                    </tr>
                    <tr>
                        <td><g:message code="product.description"/></td>
                        <td class="value" colspan="3">Service Fee</td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.item.precedence"/></td>
                        <td class="value">-1</td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.item.bundled.quantity"/></td>
                        <td class="value">1</td>

                        <td><g:message code="plan.bundle.label.add.if.exists"/></td>
                        <td class="value"><g:message code="plan.bundle.add.if.exists.false"/></td>
                    </tr>
                    <tr>
                        <td><g:message code="plan.bundle.period"/></td>
                        <td class="value">Monthly</td>

                        <td><g:message code="plan.bundle.target.customer"/></td>
                        <td class="value"><g:message code="bundle.target.customer.SELF"/></td>
                    </tr>
                    <tr><td colspan="4">&nbsp;</td></tr>
                    <tr class="price">
                        <td><g:message code="plan.model.type"/></td>
                        <td class="value"><g:message code="price.strategy.METERED"/></td>

                        <td><g:message code="plan.model.rate"/></td>
                        <td class="value">$10.00</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="btn-box">
            <a href="#" class="submit edit"><span><g:message code="button.edit"/></span></a>
            <a href="#" class="submit delete"><span><g:message code="button.delete"/></span></a>
        </div>
    </div>
</content>

</body>
</html>