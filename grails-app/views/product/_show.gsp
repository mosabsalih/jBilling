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

<%@ page import="com.sapienter.jbilling.server.util.Util"%>

<%--
  Product details template. This template shows a product and all the relevant product details.

  @author Brian Cowdery
  @since  16-Dec-2010
--%>


<div class="column-hold">
    <div class="heading">
	    <strong>${selectedProduct.internalNumber}</strong>
	</div>

	<div class="box">
        <!-- product info -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="product.detail.id"/></td>
                    <td class="value">${selectedProduct.id}</td>
                </tr>
                <tr>
                    <td><g:message code="product.detail.internal.number"/></td>
                    <td class="value">${selectedProduct.internalNumber}</td>
                </tr>
                <tr>
                    <td><g:message code="product.detail.gl.code"/></td>
                    <td class="value">${selectedProduct.glCode}</td>
                </tr>
                <tr>
                    <td><g:message code="product.detail.percentage"/></td>
                    <td class="value">
                        <g:if test="${selectedProduct.percentage}">
                            <g:formatNumber number="${selectedProduct.percentage}" formatName="percentage.format"/>
                        </g:if>
                        <g:else>
                            -
                        </g:else>
                    </td>
                </tr>

                <g:each var="price" in="${selectedProduct.itemPrices.sort{ it.currencyDTO.id }}">
                    <tr>
                        <td>${price.currencyDTO.code}:&nbsp;</td>
                        <td class="value">
                            <g:formatNumber number="${price.price}" type="currency" currencySymbol="${price.currencyDTO.symbol}"/>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <!-- percentage excluded categories -->
        <g:if test="${selectedProduct.percentage}">
        <table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
            <tbody>
                <tr class="price">
                    <td><g:message code="product.excludedCategories"/></td>
                    <td class="value">
                        <g:each var="category" status="i" in="${selectedProduct.excludedTypes.sort{ it.description }}">
                            ${category.description}<g:if test="${i < selectedProduct.excludedTypes.size()-1}">, </g:if>
                        </g:each>
                    </td>
                </tr>
            </tbody>
        </table>
        </g:if>        
        
        <!-- flags -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><em><g:message code="product.detail.decimal"/></em></td>
                    <td class="value"><em><g:formatBoolean boolean="${selectedProduct.hasDecimals > 0}"/></em></td>
                </tr>
                <tr>
                    <td><em><g:message code="product.detail.manual.pricing"/></em></td>
                    <td class="value"><em><g:formatBoolean boolean="${selectedProduct.priceManual > 0}"/></em></td>
                </tr>
            </tbody>
        </table>

        <p class="description">
            ${selectedProduct.description}
        </p>

        <!-- product categories cloud -->
        <div class="box-cards box-cards-open">
            <div class="box-cards-title">
                <span><g:message code="product.detail.categories.title"/></span>
            </div>
            <div class="box-card-hold">
                <div class="content">
                    <ul class="cloud">
                        <g:each var="category" in="${selectedProduct.itemTypes.sort{ it.description }}">
                            <li>
                                <g:link action="list" id="${category.id}">${category.description}</g:link>
                            </li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="btn-box">
        <sec:ifAllGranted roles="PRODUCT_41">
            <g:link action="editProduct" id="${selectedProduct.id}" class="submit edit"><span><g:message code="button.edit"/></span></g:link>
        </sec:ifAllGranted>

        <sec:ifAllGranted roles="PRODUCT_42">
            <a onclick="showConfirm('deleteProduct-${selectedProduct.id}');" class="submit delete"><span><g:message code="button.delete"/></span></a>
        </sec:ifAllGranted>
    </div>

    <g:render template="/confirm"
              model="['message': 'product.delete.confirm',
                      'controller': 'product',
                      'action': 'deleteProduct',
                      'id': selectedProduct.id,
                      'formParams': ['category': selectedCategoryId],
                      'ajax': true,
                      'update': 'column1',
                      'onYes': 'closePanel($(\'column2\'))'
                     ]"/>
</div>

