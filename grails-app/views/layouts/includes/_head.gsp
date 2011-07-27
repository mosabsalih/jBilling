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
  Content for the head region of all jBilling layouts.

  @author Brian Cowdery
  @since  23-11-2010
--%>

<meta http-equiv="content-type" content="text/html; charset=utf-8" />

<title><g:layoutTitle default="jBilling" /></title>

<link rel="shortcut icon" href="${resource(dir:'images', file:'favicon.ico')}" type="image/x-icon" />

<g:javascript library="jquery" plugin="jquery"/>
<jqui:resources themeCss="${resource(dir:'jquery-ui/themes/jbilling/jquery-ui-1.8.7.custom.css')}" />
<g:javascript src="jquery-ui/i18n/jquery.ui.datepicker-${session.locale.language}.js"/>

<link media="all" rel="stylesheet" href="${resource(dir:'css', file:'all.css')}" type="text/css" />
<!--[if lt IE 8]><link rel="stylesheet" href="${resource(dir:'css', file:'lt7.css')}" type="text/css" media="screen"/><![endif]-->

<g:if test="${ajaxListeners == null || ajaxListeners}">
    <script type="text/javascript">
        function renderMessages() {
            $.ajax({
                url: "${resource(dir:'')}/messages",
                global: false,
                async: false,
                success: function(data) { $("#messages").replaceWith(data); }
            });
        }

        function renderBreadcrumbs() {
            $.ajax({
                url: "${resource(dir:'')}/breadcrumb",
                global: false,
                success: function(data) { $("#breadcrumbs").replaceWith(data); }
           });
        }

        $(document).ajaxSuccess(function(e, xhr, settings) {
            renderMessages();
            renderBreadcrumbs();
        });
        $(document).ajaxError(function(e, xhr, settings) {
            renderMessages();
        });
    </script>
</g:if>

<g:javascript src="jquery-validate/jquery.validate.min.js"/>
<g:javascript src="jquery-validate/jquery.metadata.js"/>
<g:javascript src="jquery-validate/additional-methods.min.js"/>
<g:javascript src="jquery-validate/i18n/messages_${session.locale.language}.js"/>

<script type="text/javascript">
    $(document).ready(function() {
        $.validator.setDefaults({
            errorContainer: "#error-messages",
            errorLabelContainer: "#error-messages ul",
            wrapper: "li",
            meta: "validate"
        });
    })
</script>

<g:javascript library="datatable"/>
<g:javascript library="clearinput"/>
<g:javascript library="slideBlock"/>
<g:javascript library="main"/>

