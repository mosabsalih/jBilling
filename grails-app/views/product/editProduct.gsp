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

<%@ page import="com.sapienter.jbilling.server.util.db.CurrencyDTO; com.sapienter.jbilling.server.util.db.LanguageDTO; com.sapienter.jbilling.server.item.db.ItemTypeDTO" %>

<html>
<head>
    <meta name="layout" content="main" />

    <script type="text/javascript">
        $(document).ready(function() {
            $('#product\\.percentageAsDecimal').blur(function() {
                if ($(this).val()) {
                    $('#pricing :input').val('').attr('disabled', 'true');
                    $('#product\\.excludedTypes').attr('disabled', '');
                    closeSlide('#pricing');
                } else {
                    $('#pricing :input').attr('disabled', '');
                    $('#product\\.excludedTypes').val('').attr('disabled', 'true');
                    openSlide('#pricing');
                }
            }).blur();
        });
    </script>
</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:if test="${product}">
                <g:message code="product.edit.title"/>
            </g:if>
            <g:else>
                <g:message code="product.add.title"/>
            </g:else>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="save-product-form" action="saveProduct">
            <fieldset>
                <!-- product info -->
                <div class="form-columns">
                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="product.id"/></content>

                            <g:if test="${product}">${product?.id}</g:if>
                            <g:else><em><g:message code="prompt.id.new"/></em></g:else>

                            <g:hiddenField name="product.id" value="${product?.id}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="product.description"/></content>
                            <content tag="label.for">product.description</content>
                            <g:textField class="field" name="product.description" value="${product?.description}" size="40"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="product.percentage"/></content>
                            <content tag="label.for">product.percentageAsDecimal</content>
                            <g:textField class="field" name="product.percentageAsDecimal" value="${formatNumber(number: product?.percentage, formatName: 'decimal.format')}" size="5"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/checkbox">
                            <content tag="label"><g:message code="product.allow.decimal.quantity"/></content>
                            <content tag="label.for">product.hasDecimals</content>
                            <g:checkBox class="cb checkbox" name="product.hasDecimals" checked="${product?.hasDecimals > 0}"/>
                        </g:applyLayout>
                    </div>

                    <div class="column">
                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="product.internal.number"/></content>
                            <content tag="label.for">product.number</content>
                            <g:textField class="field" name="product.number" value="${product?.number}" size="40"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="product.gl.code"/></content>
                            <content tag="label.for">product.glCode</content>
                            <g:textField class="field" name="product.glCode" value="${product?.glCode}" size="40"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="product.categories"/></content>
                            <content tag="label.for">product.types</content>

                            <g:set var="types" value="${product?.types?.collect{ it as Integer }}"/>
                            <g:select name="product.types" multiple="true"
                                      from="${categories}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${types ?: categoryId}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="product.excludedCategories"/></content>
                            <content tag="label.for">product.excludedTypes</content>

                            <g:set var="types" value="${product?.excludedTypes?.collect{ it as Integer }}"/>
                            <g:select name="product.excludedTypes" multiple="true"
                                      from="${categories}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${types}"/>
                        </g:applyLayout>
                    </div>
                </div>

                <!-- spacer -->
                <div>
                    <br/>&nbsp;
                </div>

                <!-- pricing controls -->
                <div id="pricing" class="box-cards box-cards-open">
                    <div class="box-cards-title">
                        <a class="btn-open" href="#"><span><g:message code="product.prices"/></span></a>
                    </div>
                    <div class="box-card-hold">
                        <div class="form-columns">
                            <div class="column">
                                <g:each var="currency" in="${currencies.sort{ it.id }}">
                                    <g:applyLayout name="form/input">
                                        <content tag="label">${currency.getDescription(session['language_id'])}</content>
                                        <content tag="label.for">prices.${currency.id}</content>

                                        <g:set var="itemPrice" value="${product?.prices?.find { it.currencyId == currency.id }}"/>
                                        <g:textField class="field" name="prices.${currency.id}" value="${formatNumber(number: itemPrice?.price, formatName: 'money.format')}"/>
                                    </g:applyLayout>
                                </g:each>
                            </div>

                            <div class="column">
                                <g:applyLayout name="form/checkbox">
                                    <content tag="label"><g:message code="product.allow.manual.pricing"/></content>
                                    <content tag="label.for">product.priceManual</content>
                                    <g:checkBox class="cb checkbox" name="product.priceManual" checked="${product?.priceManual > 0}"/>
                                </g:applyLayout>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- spacer -->
                <div>
                    <br/>&nbsp;
                </div>

                <div class="buttons">
                    <ul>
                        <li><a onclick="$('#save-product-form').submit();" class="submit save"><span><g:message code="button.save"/></span></a></li>
                        <li><g:link controller="product" action="list" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link></li>
                    </ul>
                </div>

            </fieldset>
        </g:form>
    </div>

</div>
</body>
</html>
