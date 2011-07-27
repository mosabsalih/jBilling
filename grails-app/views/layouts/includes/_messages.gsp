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
  messages

  @author Brian Cowdery
  @since  23-11-2010
--%>

<%-- show flash messages if available --%>
<%-- either 'flash.message', 'flash.info', 'flash.warn' or 'flash.error' --%>
<%-- will also print all messages from 'flash.errorMessages' as an unordered list --%>
<div id="messages">

    <!-- hidden div for javascript validation errors -->
    <div id="error-messages" class="msg-box error" style="display: none;">
        <img src="${resource(dir:'images', file:'icon14.gif')}" alt="${message(code:'error.icon.alt',default:'Error')}"/>
        <strong><g:message code="flash.error.title"/></strong>
        <ul></ul>
    </div>

    <g:if test='${session.message}'>
        <div class="msg-box successfully">
            <img src="${resource(dir:'images', file:'icon20.gif')}" alt="${message(code:'success.icon.alt',default:'Success')}"/>
            <strong><g:message code="flash.success.title"/></strong>
            <p><g:message code="${session.message}" args="${session.args}"/></p>

            <g:set var="message" value="" scope="session"/>
        </div>
    </g:if>

    <g:if test='${session.error}'>
        <div class="msg-box error">
            <img src="${resource(dir:'images', file:'icon14.gif')}" alt="${message(code:'error.icon.alt',default:'Error')}"/>
            <strong><g:message code="flash.error.title"/></strong>
            <p><g:message code="${session.error}" args="${session.args}"/></p>

            <g:set var="error" value="" scope="session"/>
        </div>
    </g:if>

    <g:if test='${flash.message}'>
        <div class="msg-box successfully">
            <img src="${resource(dir:'images', file:'icon20.gif')}" alt="${message(code:'success.icon.alt',default:'Success')}"/>
            <strong><g:message code="flash.success.title"/></strong>
            <p><g:message code="${flash.message}" args="${flash.args}"/></p>
        </div>
    </g:if>

    <g:if test='${flash.info}'>
        <div class="msg-box info">
            <img src="${resource(dir:'images', file:'icon34.gif')}" alt="${message(code:'info.icon.alt',default:'Information')}"/>
            <strong><g:message code="flash.info.title"/></strong>
            <p><g:message code="${flash.info}" args="${flash.args}"/></p>
        </div>
    </g:if>

    <g:if test='${flash.warn}'>
        <div class="msg-box warn">
            <img src="${resource(dir:'images', file:'icon32.gif')}" alt="${message(code:'warn.icon.alt',default:'Warning')}"/>
            <strong><g:message code="flash.warn.title"/></strong>
            <p><g:message code="${flash.warn}" args="${flash.args}"/></p>
        </div>
    </g:if>

    <g:if test='${flash.error}'>
        <div class="msg-box error">
            <img src="${resource(dir:'images', file:'icon14.gif')}" alt="${message(code:'error.icon.alt',default:'Error')}"/>
            <strong><g:message code="flash.error.title"/></strong>
            <p><g:message code="${flash.error}" args="${flash.args}"/></p>
        </div>
    </g:if>

    <g:if test="${flash.errorMessages}">
        <div class="msg-box error">
            <img src="${resource(dir:'images', file:'icon14.gif')}" alt="${message(code:'error.icon.alt',default:'Error')}"/>
            <strong><g:message code="flash.validation.error.title"/></strong>
            <ul>
                <g:each var="message" in="${flash.errorMessages}">
                    <li>${message}</li>
                </g:each>
            </ul>
        </div>
    </g:if>

    <%-- clear message once displayed --%>
    <g:set var="message" value="" scope="flash"/>
    <g:set var="info" value="" scope="flash"/>
    <g:set var="warn" value="" scope="flash"/>
    <g:set var="error" value="" scope="flash"/>
    <g:set var="errorMessages" value="" scope="flash"/>
</div>


