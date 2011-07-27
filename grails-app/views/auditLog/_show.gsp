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
  Shows details of a selected log.

  @author Emiliano Conde
--%>

<div class="column-hold">
    <div class="heading">
        <strong>
            <g:message code="log.refund.title"/>
        </strong>
    </div>

    <div class="box">
        <!-- log details -->
        <table class="dataTable" cellspacing="0" cellpadding="0">
            <tbody>
                <tr>
                    <td><g:message code="log.id"/></td>
                    <td class="value">${selected.id}</td>
                </tr>
                <tr>
                    <td><g:message code="log.user"/></td>
                    <td class="value">
                        <g:if test="${selected.baseUser != null}">
                        ${selected.baseUser.id} ${selected.baseUser.userName}
                        </g:if>
                        <g:else>
                            <g:message code="log.no_user"/>
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="log.table"/></td>
                    <td class="value">${selected.jbillingTable.name}</td>
                </tr>
                <tr>
                    <td><g:message code="log.foreign_id"/></td>
                    <td class="value">
                        <g:if test="${selected.jbillingTable.name == 'base_user'}">
                            <g:remoteLink controller="customer" action="show" id="${selected.foreignId}" before="register(this);" onSuccess="render(data, next);">
                                 ${selected.foreignId}
                            </g:remoteLink>
                        </g:if>
                        <g:elseif test="${selected.jbillingTable.name == 'purchase_order'}">
                            <g:remoteLink controller="order" action="show" id="${selected.foreignId}" before="register(this);" onSuccess="render(data, next);">
                                 ${selected.foreignId}
                            </g:remoteLink>
                        </g:elseif>
                        <g:elseif test="${selected.jbillingTable.name == 'payment'}">
                            <g:remoteLink controller="payment" action="show" id="${selected.foreignId}" before="register(this);" onSuccess="render(data, next);">
                                 ${selected.foreignId}
                            </g:remoteLink>
                        </g:elseif>
                        <g:elseif test="${selected.jbillingTable.name == 'invoice'}">
                            <g:remoteLink controller="invoice" action="show" id="${selected.foreignId}" before="register(this);" onSuccess="render(data, next);">
                                 ${selected.foreignId}
                            </g:remoteLink>
                        </g:elseif>
                        <g:elseif test="${selected.jbillingTable.name == 'pluggable_task'}">
                            <g:remoteLink controller="plugin" action="show" params="template:show" id="${selected.foreignId}" before="register(this);" onSuccess="render(data, next);">
                                 ${selected.foreignId}
                            </g:remoteLink>
                        </g:elseif>
                        <g:else>
                            ${selected.foreignId}
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td><g:message code="log.date"/></td>
                    <td class="value"><g:formatDate date="${selected.createDatetime}" formatName="date.timeSecs.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="log.level"/></td>
                    <td class="value">${selected.levelField}</td>
                </tr>
                <tr>
                    <td><g:message code="log.module"/></td>
                    <td class="value">${selected.eventLogModule.id}</td>
                </tr>
                <tr>
                    <td><g:message code="log.message"/></td>
                    <td class="value">${selected.eventLogMessage.getDescription(session['language_id'])}</td>
                </tr>
                <tr>
                    <td><g:message code="log.old_fields"/></td>
                    <td class="value">${selected.oldNum} ${selected.oldStr} ${selected.oldDate}</td>
                </tr>
                <tr>
                    <td><g:message code="log.affected_user"/></td>
                    <td class="value">
                        <g:if test="${selected.affectedUser != null}">
                            <g:remoteLink controller="customer" action="show" id="${selected.affectedUser.id}" before="register(this);" onSuccess="render(data, next);">
                                    ${selected.affectedUser.id}
                            </g:remoteLink>
                            <g:remoteLink controller="customer" action="show" id="${selected.affectedUser.id}" before="register(this);" onSuccess="render(data, next);">
                                    ${selected.affectedUser.userName}
                            </g:remoteLink>
                        </g:if>
                    </td>
                </tr>
            </tbody>
        </table>

    </div>

    <div class="btn-box">
        <!-- No buttons, but we need the bottom lines -->
        <div class="row">
        </div>
    </div>
</div>