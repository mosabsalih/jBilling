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

<%@page import="com.sapienter.jbilling.server.order.db.OrderLineTypeDTO"%>

<%--
  Categories list

  @author Brian Cowdery
  @since  16-Dec-2010
--%>

<div class="table-box">
    <div class="table-scroll">
        <table id="categories" cellspacing="0" cellpadding="0">
            <thead>
                <tr>
                    <th><g:message code="product.category.th.name"/></th>
                    <th class="small"><g:message code="product.category.th.type"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each var="category" in="${categories}">
                <g:set var="lineType" value="${new OrderLineTypeDTO(category.orderLineTypeId, 0)}"/>

                    <tr id="category-${category.id}" class="${selectedCategoryId == category.id ? 'active' : ''}">
                        <td>
                            <g:remoteLink class="cell double" action="products" id="${category.id}" before="register(this);" onSuccess="render(data, next);">
                                <strong>${category.description}</strong>
                                <em><g:message code="table.id.format" args="[category.id]"/></em>
                            </g:remoteLink>
                        </td>
                        <td class="small">
                            <g:remoteLink class="cell" action="products" id="${category.id}" before="register(this);" onSuccess="render(data, next);">
                                <span>${lineType.description}</span>
                            </g:remoteLink>
                        </td>
                    </tr>

                </g:each>
            </tbody>
        </table>
    </div>
</div>

<g:if test="${categories?.totalCount > params.max}">
    <div class="pager-box">
        <div class="row left">
            <g:render template="/layouts/includes/pagerShowResults" model="[steps: [10, 20, 50], action: 'categories', update: 'column1']"/>
        </div>
        <div class="row">
            <util:remotePaginate controller="product" action="categories" total="${categories.totalCount}" update="column1"/>
        </div>
    </div>
</g:if>

<div class="btn-box">
    <sec:ifAllGranted roles="PRODUCT_CATEGORY_50">
        <g:link action="editCategory" class="submit add"><span><g:message code="button.create.category"/></span></g:link>
    </sec:ifAllGranted>

    <sec:ifAllGranted roles="PRODUCT_CATEGORY_51">
        <a href="#" onclick="return editCategory();" class="submit edit"><span><g:message code="button.edit"/></span></a>
    </sec:ifAllGranted>
</div>


<!-- edit category control form -->
<g:form name="category-edit-form" url="[action: 'editCategory']">
    <g:hiddenField name="id" value="${selectedCategoryId}"/>
</g:form>

<script type="text/javascript">
    function editCategory() {
        $('#category-edit-form input#id').val(getSelectedId('#categories'));
        $('#category-edit-form').submit();
        return false;
    }
</script>