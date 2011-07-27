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

<%@ page import="com.sapienter.jbilling.server.user.db.CompanyDTO" %>

<head>
    <meta name="layout" content="public" />

    <title><g:message code="login.page.title"/></title>

    <script type='text/javascript'>
        $(document).ready(function() {
            $('#login input[name="j_username"]').focus();
        });
    </script>
</head>
<body>

    <g:render template="/layouts/includes/messages"/>

    <div id="login" class="form-edit">
        <div class="heading">
            <strong><g:message code="login.prompt.title"/></strong>
        </div>
        <div class="form-hold">
            <form action='${postUrl}' method='POST' id='login-form' autocomplete='off'>
                <fieldset>

                    <div class="form-columns">
                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="login.prompt.username"/></content>
                            <content tag="label.for">username</content>
                            <g:textField class="field" name="j_username" value="${params.userName}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="login.prompt.password"/></content>
                            <content tag="label.for">password</content>
                            <g:passwordField class="field" name="j_password"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/select">
                            <content tag="label"><g:message code="login.prompt.client.id"/></content>
                            <content tag="label.for">client_id</content>
                            <g:select name="j_client_id"
                                      from="${CompanyDTO.list()}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${params.companyId}"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/checkbox">
                            <content tag="label"><g:message code="login.prompt.remember.me"/></content>
                            <content tag="label.for">${rememberMeParameter}</content>
                            <g:checkBox class="cb checkbox" name="${rememberMeParameter}" checked="${hasCookie}"/>
                        </g:applyLayout>

                        <br/>
                    </div>

                    <div class="buttons">
                        <ul>
                            <li>
                                <a href="#" class="submit save" onclick="$('#login form').submit();">
                                    <span><g:message code="login.button.submit"/></span>
                                </a>
                            </li>
                        </ul>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>

</body>
</html>