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

<%@ page import="com.sapienter.jbilling.server.user.db.CompanyDTO; com.sapienter.jbilling.server.item.db.ItemTypeDTO" %>

<%--
  Shows the product list and provides some basic filtering capabilities.

  @author Brian Cowdery
  @since 23-Jan-2011
--%>

<div id="product-box">

    <!-- filter -->
    <div class="form-columns">
        <g:formRemote name="products-filter-form" url="[action: 'edit']" update="ui-tabs-2" method="GET">
            <g:hiddenField name="_eventId" value="products"/>
            <g:hiddenField name="execution" value="${flowExecutionKey}"/>

            <g:applyLayout name="form/input">
                <content tag="label"><g:message code="filters.title"/></content>
                <content tag="label.for">filterBy</content>
                <g:textField name="filterBy" class="field default" placeholder="${message(code: 'products.filter.by.default')}" value="${params.filterBy}"/>
            </g:applyLayout>

            <g:applyLayout name="form/select">
                <content tag="label"><g:message code="order.label.products.category"/></content>
                <content tag="label.for">typeId</content>
                <g:select name="typeId" from="${itemTypes}"
                          noSelection="['': message(code: 'filters.item.type.empty')]"
                          optionKey="id" optionValue="description"
                          value="${params.typeId}"/>
            </g:applyLayout>
        </g:formRemote>

        <script type="text/javascript">
            $(function() {
                $('#products-filter-form :input[name=filterBy]').blur(function() { $('#products-filter-form').submit(); });
                $('#products-filter-form :input[name=typeId]').change(function() { $('#products-filter-form').submit(); });
                placeholder();
            });
        </script>
    </div>

    <!-- product list -->
    <div class="table-box tab-table">
        <div class="table-scroll">
            <table id="products" cellspacing="0" cellpadding="0">
                <tbody>

                <g:each var="product" in="${products}">
                    <tr>
                        <td>
                            <g:remoteLink class="cell double" action="edit" id="${product.id}" params="[_eventId: 'addLine']" update="column2" method="GET">
                                <strong>${product.getDescription(session['language_id'])}</strong>
                                <em><g:message code="table.id.format" args="[product.id]"/></em>
                            </g:remoteLink>
                        </td>
                        <td class="small">
                            <g:remoteLink class="cell double" action="edit" id="${product.id}" params="[_eventId: 'addLine']" update="column2" method="GET">
                                <span>${product.internalNumber}</span>
                            </g:remoteLink>
                        </td>
                        <td class="medium">
                            <g:remoteLink class="cell double" action="edit" id="${product.id}" params="[_eventId: 'addLine']" update="column2" method="GET">
                                <g:if test="${product.percentage}">
                                    %<g:formatNumber number="${product.percentage}" formatName="money.format"/>
                                </g:if>
                                <g:else>
                                    <g:set var="prices" value="${product.itemPrices?.asList()?.sort{ it.currencyDTO.id }}"/>
                                    <g:set var="price" value="${!prices.isEmpty() ? prices.first() : null}"/>
                                    <g:formatNumber number="${price?.price}" type="currency" currencySymbol="${price?.currencyDTO?.symbol}"/>
                                </g:else>
                            </g:remoteLink>
                        </td>
                    </tr>
                </g:each>

                </tbody>
            </table>
        </div>
    </div>

</div>