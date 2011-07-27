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

<html>
<head>
<sec:ifLoggedIn>
        <meta name="layout" content="main" />
    </sec:ifLoggedIn>

    <sec:ifNotLoggedIn>
        <meta name="layout" content="public" />
    </sec:ifNotLoggedIn>

    <title><g:message code="exception.page.title"/></title>
</head>

<body>

<g:if test="${exception}">
    <div class="msg-box error wide">
        <img src="${resource(dir:'images', file:'icon14.gif')}" alt="${message(code:'error.icon.alt',default:'Error')}"/>
        <strong><g:message code="flash.exception.message.title"/></strong>
        <p>
            <g:message code="flash.exception.message"/>
        </p>
    </div>


    <div class="form-edit">
        <div class="heading">
            <strong><g:message code="exception.code.title" args="[request.'javax.servlet.error.status_code']"/></strong>
        </div>

        <div class="form-hold">
            <div class="form-columns">

                <!-- error details -->
                <table cellpadding="0" cellspacing="0" class="dataTable">
                    <tr>
                        <td><g:message code="exception.uri"/></td>
                        <td class="value">${request['javax.servlet.error.request_uri']}</td>
                    </tr>

                    <tr>
                        <td><g:message code="exception.message"/></td>
                        <td class="value">${exception.message?.encodeAsHTML()}</td>
                    </tr>

                    <tr>
                        <td><g:message code="exception.cause"/></td>
                        <td class="value">${exception.cause?.message?.encodeAsHTML()}</td>
                    </tr>

                    <tr>
                        <td><g:message code="exception.source"/></td>
                        <td class="value">
                            <g:message code="exception.message" args="[exception.className, exception.lineNumber]"/>
                        </td>
                    </tr>
                </table>

                <g:if test="${exception.codeSnippet}">
                    <div class="code">
                        <g:each var="cs" in="${exception.codeSnippet}">
                            ${cs?.encodeAsHTML()}<br />
                        </g:each>
                    </div>
                </g:if>
            </div>

            <!-- stack trace -->
            <div class="box-cards">
                <div class="box-cards-title">
                    <a class="btn-open"><span><g:message code="exception.stack.trace.title"/></span></a>
                </div>
                <div class="box-card-hold">
                    <div class="form-columns">

                        <div class="code stacktrace">
                            <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}</g:each></pre>
                        </div>

                    </div>
                </div>
            </div>

            <!-- spacer -->
            <div>
                <br/>&nbsp;
            </div>
        </div>
    </div>
</g:if>


</body>
</html>