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

<%@ page import="com.sapienter.jbilling.common.Constants; jbilling.SearchType" %>

<%--
  Page header for all common jBilling layouts.

  This contains the jBilling top-navigation bar, search form and main navigation menu.

  @author Brian Cowdery
  @since  23-11-2010
--%>

<!-- header -->
<div id="header">
    <h1><a href="${resource(dir:'')}">jBilling</a></h1>
    <div class="search">        
        <g:form controller="search" name="search-form">
            <fieldset>
                <input type="image" class="btn" src="${resource(dir:'images', file:'icon-search.gif')}" onclick="$('#search-form').submit()"/>
                <div class="input-bg">                    
                    <g:textField name="id" value="${cmd?.id ?: message(code:'search.title')}" class="default"/>
                    <a href="#" class="open"></a>
                    <div class="popup">
                        <div class="top-bg">
                            <div class="btm-bg">
                                <sec:access url="/customer/list">
                                    <div class="input-row">
                                        <g:radio id="customers" name="type" value="CUSTOMERS" checked="${!cmd || cmd?.type?.toString() == 'CUSTOMERS'}"/>
                                        <label for="customers"><g:message code="search.option.customers"/></label>
                                    </div>
                                </sec:access>
                                <sec:access url="/order/list">
                                    <div class="input-row">
                                        <g:radio id="orders" name="type" value="ORDERS" checked="${cmd?.type?.toString() == 'ORDERS'}"/>
                                        <label for="orders"><g:message code="search.option.orders"/></label>
                                    </div>
                                </sec:access>
                                <sec:access url="/invoice/list">
                                    <div class="input-row">
                                        <g:radio id="invoices" name="type" value="INVOICES" checked="${cmd?.type?.toString() == 'INVOICES'}"/>
                                        <label for="invoices"><g:message code="search.option.invoices"/></label>
                                    </div>
                                </sec:access>
                                <sec:access url="/payment/list">
                                    <div class="input-row">
                                        <g:radio id="payments" name="type" value="PAYMENTS" checked="${cmd?.type?.toString() == 'PAYMENTS'}"/>
                                        <label for="payments"><g:message code="search.option.payments"/></label>
                                    </div>
                                </sec:access>
                            </div>
                        </div>
                    </div>
                </div>
            </fieldset>
        </g:form>
    </div>

    <ul class="top-nav">
        <li><g:message code="topnav.greeting"/> <sec:loggedInUserInfo field="plainUsername"/></li>
        <li>
            <g:if test="${session['main_role_id'] == Constants.TYPE_CUSTOMER}">
                <g:link controller="customer" action="edit" id="${session['user_id']}">
                    <img src="${resource(dir:'images', file:'icon25.gif')}" alt="account" />
                    <g:message code="topnav.link.account"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="user" action="edit" id="${session['user_id']}">
                    <img src="${resource(dir:'images', file:'icon25.gif')}" alt="account" />
                    <g:message code="topnav.link.account"/>
                </g:link>
            </g:else>
        </li>
        <li>
            <a href="http://www.jbilling.com/professional-services/training">
                <img src="${resource(dir:'images', file:'icon26.gif')}" alt="training" />
                <g:message code="topnav.link.training"/>
            </a>
        </li>
        <li>
            <a href="http://www.jbilling.com/product/documentation">
                <img src="${resource(dir:'images', file:'icon27.gif')}" alt="help" />
                <g:message code="topnav.link.help"/>
            </a>
        </li>
        <li>
            <g:link controller='logout'>
                <img src="${resource(dir:'images', file:'icon28.gif')}" alt="logout" /><g:message code="topnav.link.logout"/>
            </g:link>
        </li>
    </ul>

    <div id="navigation">
        <%-- select the current menu item based on the controller name --%>
        <ul>
            <sec:access url="/customer/list">
                <li class="${controllerName == 'customer' ? 'active' : ''}">
                    <g:link controller="customer"><span><g:message code="menu.link.customers"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/invoice/list">
                <li class="${controllerName == 'invoice' ? 'active' : ''}">
                    <g:link controller="invoice"><span><g:message code="menu.link.invoices"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/payment/list">
                <li class="${controllerName == 'payment' ? 'active' : ''}">
                    <g:link controller="payment"><span><g:message code="menu.link.payments.refunds"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/order/list">
                <li class="${controllerName == 'order' ? 'active' : ''}">
                    <g:link controller="order"><span><g:message code="menu.link.orders"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/billing/list">
                <li class="${controllerName == 'billing' ? 'active' : ''}">
                    <g:link controller="billing"><span><g:message code="menu.link.billing"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/mediation/list">
                <li class="${controllerName == 'mediation' ? 'active' : ''}">
                    <g:link controller="mediation"><span><g:message code="menu.link.mediation"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/report/list">
                <li class="${controllerName == 'report' ? 'active' : ''}">
                    <g:link controller="report"><span><g:message code="menu.link.reports"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/product/list">
                <li class="${controllerName == 'product' ? 'active' : ''}">
                    <g:link controller="product"><span><g:message code="menu.link.products"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:access url="/plan/list">
                <li class="${controllerName == 'plan' ? 'active' : ''}">
                    <g:link controller="plan"><span><g:message code="menu.link.plans"/></span><em></em></g:link>
                </li>
            </sec:access>
            <sec:ifAllGranted roles="MENU_99">
                %{
                    def isConfiguration = controllerName == 'config' ||
                                          controllerName == 'contactFieldConfig' ||
                                          controllerName == 'contactTypeConfig' ||
                                          controllerName == 'billingconfiguration' ||
                                          controllerName == 'blacklist' ||
                                          controllerName == 'mediationConfig' ||
                                          controllerName == 'notifications' ||
                                          controllerName == 'orderPeriod' ||
                                          controllerName == 'plugin' ||
                                          controllerName == 'user'
                }%

                <li class="${isConfiguration ? 'active' : ''}">
                    <g:link controller="config"><span><g:message code="menu.link.configuration"/></span><em></em></g:link>
                </li>
            </sec:ifAllGranted>
        </ul>
    </div>
</div>
