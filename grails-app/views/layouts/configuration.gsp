<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
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

<g:render template="/layouts/includes/head"/>

    <g:javascript library="panels"/>

    <script type="text/javascript">
        function renderRecentItems() {
            $.ajax({
                url: "${resource(dir:'')}/recentItem",
                global: false,
                success: function(data) { $("#recent-items").replaceWith(data) }
            });
        }

        $(document).ajaxSuccess(function(e, xhr, settings) {
            renderRecentItems();
        });

        /*
            Highlight clicked rows in the configuration side menu
         */
        $(document).ready(function() {
            $('#left-column ul.list li').click(function() {
                $(this).parents('ul.list').find('li.active').removeClass('active');
                $(this).addClass('active');
            });
        });
    </script>

    <g:layoutHead/>
</head>
<body>
<div id="wrapper">
    <g:render template="/layouts/includes/header"/>

    <div id="main">
        <g:render template="/layouts/includes/breadcrumbs"/>

        <div id="left-column">
            <!-- configuration menu -->
            <div class="menu-items">
                <ul class="list">
                    <li class="${pageProperty(name: 'page.menu.item') == 'all' ? 'active' : ''}">
                        <g:link controller="config">
                            <g:message code="configuration.menu.all"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'aging' ? 'active' : ''}">
                        <g:link controller="config" action="aging">
                            <g:message code="configuration.menu.aging"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'billing' ? 'active' : ''}">
                        <g:link controller="billingconfiguration" action="index">
                            <g:message code="configuration.menu.billing"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'blacklist' ? 'active' : ''}">
                        <g:link controller="blacklist" action="list">
                            <g:message code="configuration.menu.blacklist"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'company' ? 'active' : ''}">
                        <g:link controller="config" action="company">
                            <g:message code="configuration.menu.company"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'currency' ? 'active' : ''}">
                        <g:link controller="config" action="currency">
                            <g:message code="configuration.menu.currencies"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'email' ? 'active' : ''}">
                        <g:link controller="config" action="email">
                            <g:message code="configuration.menu.email"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'invoices' ? 'active' : ''}">
                        <g:link controller="config" action="invoice">
                            <g:message code="configuration.menu.invoices"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'mediation' ? 'active' : ''}">
                        <g:link controller="mediationConfig" action="list">
                            <g:message code="configuration.menu.mediation"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'notification' ? 'active' : ''}">
                        <g:link controller="notifications">
                            <g:message code="configuration.menu.notification"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'periods' ? 'active' : ''}">
                        <g:link controller="orderPeriod" action="list">
                            <g:message code="configuration.menu.order.periods"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'plugins' ? 'active' : ''}">
                        <g:link controller="plugin">
                            <g:message code="configuration.menu.plugins"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'users' ? 'active' : ''}">
                        <g:link controller="user" action="list">
                            <g:message code="configuration.menu.users"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'contactType' ? 'active' : ''}">
                        <g:link controller="contactTypeConfig">
                            <g:message code="configuration.menu.contactType"/>
                        </g:link>
                    </li>
                    <li class="${pageProperty(name: 'page.menu.item') == 'customContactField' ? 'active' : ''}">
                        <g:link controller="contactFieldConfig">
                            <g:message code="configuration.menu.customContactField"/>
                        </g:link>
                    </li>
                </ul>
            </div>

            <!-- shortcuts -->
            <!-- <g:render template="/layouts/includes/shortcuts"/> -->
            <inc:include controller="shortcut" action="index"/>
            
            <!-- recently viewed items -->
            <g:render template="/layouts/includes/recent"/>
        </div>

        <!-- content columns -->
        <div class="columns-holder">
            <g:render template="/layouts/includes/messages"/>

            <!-- viewport of visible columns -->
            <div id="viewport">
                <div class="column panel">
                    <div id="column1" class="column-hold">
                        <g:pageProperty name="page.column1"/>
                    </div>
                </div>

                <div class="column panel">
                    <div id="column2" class="column-hold">
                        <g:pageProperty name="page.column2"/>
                    </div>
                </div>
            </div>

            <!-- template for new column-->
            <div id="panel-template" class="column panel">
                <div class="column-hold"></div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
