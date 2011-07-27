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

<%@ page import="com.sapienter.jbilling.server.util.db.LanguageDTO"%>
<div class="column-hold">
	
	<div class="heading">
		<strong style="width: 100%">
			${dto?.notificationMessageType?.getDescription(languageDto.id)}
			<!-- <g:message code="prompt.edit.notification"/> -->
		</strong>
	</div>
	
	<div class="box">
		<table class="dataTable">
            <tr>
        		<td><g:message code="title.notification.active" />:</td>
        		<td class="value"><g:if test="${(dto?.getUseFlag() > 0)}">
        				<g:message code="prompt.yes"/>
        			</g:if>
        			<g:else>
        				<g:message code="prompt.no"/>
        			</g:else>
        		</td>
            </tr>
            <tr>
        		<td><g:message code="notification.label.language"/>:</td>
        		<td class="value">${languageDto.getDescription()}</td>
    		</tr>
    		<g:set var="flag" value="${true}" />
            <tr>
    		<td><g:message code="prompt.edit.notification.subject" />:</td>
    		<td class="value">
    			<g:each in="${dto?.getNotificationMessageSections()}"
    				var="section">
    				<g:if test="${section.section == 1}">
    					<g:hiddenField
    						name="messageSections[${section.section}].id"
    						value="${section.id}" />
    					<g:hiddenField
    						name="messageSections[${section.section}].section"
    						value="${section.section}" />
    					<g:set var="tempContent" value="" />
    					<g:each in="${section.getNotificationMessageLines().sort{it.id}}"
    						var="line">
    						<g:set var="tempContent"
    							value="${tempContent=tempContent + line?.getContent()}" />
    					</g:each>
    					${tempContent}
    					<g:set var="flag" value="${false}" />
    				</g:if>
    			</g:each>
    		</td></tr>
    	
    		<g:set var="flag" value="${true}" />
            <tr>
    		<td><g:message code="prompt.edit.notification.bodytext" />:</td>
    		<td class="value"><g:each in="${dto?.getNotificationMessageSections()}"
    				var="section">
    				<g:if test="${section.section == 2}">
    					<g:hiddenField
    						name="messageSections[${section.section}].id"
    						value="${section.id}" />
    					<g:hiddenField
    						name="messageSections[${section.section}].section"
    						value="${section.section}" />
    					<g:set var="tempContent" value="" />
    					<g:each in="${section.getNotificationMessageLines().sort{it.id}}"
    						var="line">
    						<g:set var="tempContent"
    							value="${tempContent=tempContent + line?.getContent()}" />
    					</g:each>
    					${tempContent}
    					<g:set var="flag" value="${false}" />
    				</g:if>
    			</g:each>
    		</td></tr>
    	
    		<g:set var="flag" value="${true}" />
            <tr>
    		<td><g:message code="prompt.edit.notification.bodyhtml" />:</td>
    		<td class="value"><g:each in="${dto?.getNotificationMessageSections()}"
    				var="section">
    				<g:if test="${section?.section == 3}">
    					<g:hiddenField
    						name="messageSections[${section.section}].id"
    						value="${section?.id}" />
    					<g:hiddenField
    						name="messageSections[${section.section}].section"
    						value="${section?.section}" />
    					<g:set var="tempContent" value="" />
    					<g:each in="${section?.getNotificationMessageLines().sort{it.id}}"
    						var="line">
    						<g:set var="tempContent"
    							value="${tempContent=tempContent + line?.getContent()}" />
    					</g:each>
    					${tempContent}
    					<g:set var="flag" value="${false}" />
    				</g:if>
    			</g:each>
    		</td></tr>
    	</table>
	</div>
	
	<div class="btn-box">
	    <a href="${createLink(action: 'edit', params: [id:messageTypeId])}" class="submit edit">
	    	<span><g:message code="button.edit"/></span></a>
	</div>
</div>