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
  Text field with a date selector.

  @author Brian Cowdery
  @since  06-Jan-2011
--%>

<g:set var="jquerySelector" value="#${pageProperty(name: 'page.label.for').replaceAll('\\.','\\\\\\\\\\.')}"/>

<div class="row">
    <label for="<g:pageProperty name="page.label.for"/>"><g:pageProperty name="page.label"/></label>
    <div class="inp-bg date">
        <g:layoutBody/>
    </div>

    <script type="text/javascript">
        // wait to initialize the date picker if it's not visible
        setTimeout(
                function() {
                    var options = $.datepicker.regional['${session.locale.language}'];
                    if (options == null) options = $.datepicker.regional[''];

                    options.dateFormat = "${message(code: 'datepicker.jquery.ui.format')}";
                    options.showOn = "both";
                    options.buttonImage = "${resource(dir:'images', file:'icon04.gif')}";
                    options.buttonImageOnly = true;

                    <g:if test="${pageProperty(name: 'page.onClose')}">
                    options.onClose = ${pageProperty(name: 'page.onClose')}
                    </g:if>

                    $("${jquerySelector}").datepicker(options);
                },
                $('${jquerySelector}').is(":visible") ? 0 : 500
        );
    </script>
</div>

