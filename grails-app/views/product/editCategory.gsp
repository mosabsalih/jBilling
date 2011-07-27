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

<%@ page import="com.sapienter.jbilling.server.order.db.OrderLineTypeDTO" %>

<html>
<head>
    <meta name="layout" content="main" />
</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:if test="${category}">
                <g:message code="product.category.edit.title"/>
            </g:if>
            <g:else>
                <g:message code="product.category.add.title"/>
            </g:else>
        </strong>
    </div>

    <div class="form-hold">
        <g:form name="save-category-form" action="saveCategory">
            <fieldset>
                <div class="form-columns">
                    <div class="column">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="product.category.id"/></content>

                            <g:if test="${category}">${category?.id}</g:if>
                            <g:else><em><g:message code="prompt.id.new"/></em></g:else>

                            <g:hiddenField name="id" value="${category?.id}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="product.category.type"/></content>
                            <g:select name="orderLineTypeId" from="${OrderLineTypeDTO.list()}"
                                      optionKey="id" optionValue="description"
                                      value="${category?.orderLineTypeId}" />
                        </g:applyLayout>
                    </div>

                    <div class="column">
                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="product.category.name"/></content>
                            <content tag="label.for">description</content>
                            <g:textField class="field" name="description" value="${category?.description}"/>
                        </g:applyLayout>
                    </div>
                </div>

                <div>
                    <br/>&nbsp;
                </div>

                <div class="buttons">
                    <ul>
                        <li><a onclick="$('#save-category-form').submit();" class="submit save"><span><g:message code="button.save"/></span></a></li>
                        <li><g:link action="list" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link></li>
                    </ul>
                </div>
            </fieldset>
        </g:form>
    </div>

</div>
</body>
</html>