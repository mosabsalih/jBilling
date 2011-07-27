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
  Layout for labeled and styled form input elements.

  Usage:

    <g:applyLayout name="form/input">
        <content tag="label">Field Label</content>
        <content tag="label.for">element_id</content>
        <input type="text" class="field" name="name" id="element_id"/>
    </g:applyLayout>

	style: Use a content tag 'style' to apply additional css class to layoutBody.

  @author Brian Cowdery
  @since  25-11-2010
--%>

<div class="row">    
    <label for="<g:pageProperty name="page.label.for"/>"><g:pageProperty name="page.label"/></label>
    <div class="inp-bg <g:pageProperty name="page.style"/>">
        <g:layoutBody/>
    </div>
</div>