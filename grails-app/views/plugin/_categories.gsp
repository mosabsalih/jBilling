<div class="table-box">
<table cellpadding="0" cellspacing="0">
    <thead>
       <tr class="active">
        <th>%{--
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

<g:message code="plugins.category.list.title"/></th>
       </tr>
    </thead>
    <tbody>
    <g:each in="${categories}" status="idx" var="dto">
    <tr>
        <td>
        <g:remoteLink action="plugins" id="${dto.id}" before="register(this);" 
                                   onSuccess="render(data, next);"
                                   params="[template:'show']">
             <strong>
                ${dto.getDescription(session['language_id'])}
		     </strong>
		     <em>
                ${dto.getInterfaceName()}
		     </em>
        </g:remoteLink>
        </td>
    </tr>
    </g:each>
    </tbody>
</table>
</div>