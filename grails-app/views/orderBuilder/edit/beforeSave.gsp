
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
  An example information page that is rendered before the order is saved.

  To enable this page, change the builder() web-flow state "save" transition to
  go to either the checkItem() or beforeSave() states.

  @author Brian Cowdery
  @since 17-Feb-2011
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

</head>
<body>
<div class="form-edit">

    <div class="heading">
        <strong>
            <g:message code="order.review.id" args="[order.id ?: '']"/>
        </strong>
    </div>

    <div class="form-hold">
        <div class="form-columns">
            <p>
                Your page content goes here.<br/>
            </p>
        </div>


        <div class="buttons">
            <ul>
                <li>
                    <g:link class="submit save" action="edit" params="[_eventId: 'save']">
                        <span><g:message code="button.save"/></span>
                    </g:link>
                </li>

                <li>
                    <g:link class="submit cancel" action="edit" params="[_eventId: 'cancel']">
                        <span><g:message code="button.cancel"/></span>
                    </g:link>
                </li>
            </ul>
        </div>
    </div>


</div>
</body>
</html>