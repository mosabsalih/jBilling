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

<g:set var="languageId" value="${session['language_id'] as Integer}"/>
<g:set var="entityId" value="${session['company_id'] as Integer}"/>

<div class="table-box">
    <table cellpadding="0" cellspacing="0">
        <thead>
            <th><g:message code="title.notification" /></th>
            <th><g:message code="title.notification.active" /></th>
        </thead>
        <tbody>
		<g:each in="${lstByCategory}" status="idx" var="dto">
			<tr class="${dto.id == messageTypeId ? 'active' : ''}">
    			<td><g:remoteLink breadcrumb="id" class="cell" action="show" id="${dto.id}" params="['template': 'show']"
    	                   before="register(this);" onSuccess="render(data, next);">
    				    <strong>${dto.getDescription(languageId)}</strong></g:remoteLink></td>
                <td>
    				<g:set var="flag" value="${true}"/> 
    				<g:each status="iter" var="var" in="${dto.getNotificationMessages()}">
    					<g:if test="${flag}">
    						<g:if test="${languageId == var.language.id 
    							&& var.entity.id == entityId && var.useFlag > 0}">
    								<g:set var="flag" value="${false}"/>
    						</g:if>
    					</g:if>
    				</g:each> 
    				<span class="block">
    					<span>
        					<g:if test="${flag}">
        						<g:message code="prompt.no"/>
        					</g:if>
        					<g:else>
        						<g:message code="prompt.yes"/>
        					</g:else>
    					</span>
    				</span>
    			</td>
            </tr>
		</g:each>
	</tbody>
    </table>
</div>
<div class="btn-box">
</div>
