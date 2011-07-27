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
  _breadcrumbs

  @author Brian Cowdery
  @since  23-11-2010
--%>

<!-- breadcrumbs -->
<div id="breadcrumbs" class="breadcrumbs">
    <div id="spinner" style="display: none;">
        <img src="${resource(dir:'images', file:'spinner.gif')}" alt="loading..." />
    </div>

    <script type="text/javascript">
        $('#spinner').ajaxStart(function() { $(this).show('fade'); });
        $('#spinner').ajaxStop(function() { $(this).hide('fade'); })
    </script>

    <ul>
        <g:each var="crumb" in="${session['breadcrumbs']}">
            <li>
                <g:link controller="${crumb.controller}" action="${crumb.action}" id="${crumb.objectId}">
                    <g:message code="${crumb.messageCode}" args="[crumb.description ?: crumb.objectId]"/>
                </g:link>
            </li>
        </g:each>

        <li class="shortcut"> 
            <g:remoteLink controller="shortcut" action="add" params="['template': 'shortcuts']" class="shortcut" update="messages">
                <span><g:message code="shortcut.add"/></span>
            </g:remoteLink>
        </li>
    </ul>
</div>
