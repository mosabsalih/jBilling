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

<%@ page contentType="text/html;charset=UTF-8" %>

<%--
  Order builder view.

  This view doubles as a way to render partial page templates by setting the 'template' parameter. This
  is used as a workaround for rendering AJAX responses from within the web-flow.

  @author Brian Cowdery
  @since 25-Jan-2011
--%>

<g:if test="${params.template}">
    <!-- render the template -->
    <g:render template="${params.template}"/>
</g:if>

<g:else>
    <!-- render the main builder view -->
    <html>
    <head>
        <meta name="layout" content="builder"/>

        <script type="text/javascript">
            $(document).ready(function() {
                $('#builder-tabs').tabs();
            });
        </script>
    </head>
    <body>
    <content tag="builder">
        <div id="builder-tabs">
            <ul>
                <li><a href="${createLink(action: 'edit', event: 'details')}"><g:message code="builder.details.title"/></a></li>
                <li><a href="${createLink(action: 'edit', event: 'products')}"><g:message code="builder.products.title"/></a></li>
            </ul>
        </div>
    </content>

    <content tag="review">
        <g:render template="review"/>
    </content>
    </body>
    </html>
</g:else>