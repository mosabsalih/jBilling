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
  Layout for labeled line of text (for use with values that should never be edited like IDs). You
  may also place a hidden field here as a convenient way of passing a record ID.

  Usage:

    <g:applyLayout name="form/input">
        <content tag="label">Field Label</content>
        ${textValue}
    </g:applyLayout>


  @author Brian Cowdery
  @since  20-Dec-2010
--%>

<div class="row">
    <label for="${pageProperty(name: 'page.label.for')}"><g:pageProperty name="page.label"/></label>
    <span><g:layoutBody/></span>
</div>