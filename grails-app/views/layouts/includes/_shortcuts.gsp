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
  _shortcuts

  @author Brian Cowdery
  @since  09-12-2010
--%>

<div id="shortcuts">
    <div class="heading">
        <a class="arrow open"><strong><g:message code="shortcut.title"/></strong></a>
        <div class="drop">
            <ul>
                <g:each var="shortcut" in="${session['shortcuts']}">
                    <li>
                        <g:remoteLink controller="shortcut" action="remove" params="[id: shortcut.id]" 
                            update="shortcuts" class="shortcut2"/>
                        <g:link controller="${shortcut.controller}" action="${shortcut.action}" id="${shortcut.objectId}">
                            <g:message code="${shortcut.messageCode}" args="[shortcut.objectId]"/>
                        </g:link>
                        
                    </li>
                </g:each>
            </ul>
        </div>
    </div>
</div>