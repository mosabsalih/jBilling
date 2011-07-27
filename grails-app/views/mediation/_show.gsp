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

<%@ page import="org.joda.time.Period; com.sapienter.jbilling.server.util.Constants;" %>

<%--
    @author Vikas Bodani
    @since 18 Feb 2011
 --%>

<div class="column-hold">
    <div class="heading">
        <strong><g:message code="mediation.process.title"/> <em>${selected.id}</em>
        </strong>
    </div>
 
    <div class="box">

        <!-- mediation process info -->
        <table cellspacing="0" cellpadding="0" class="dataTable">
            <tbody>
                <tr>
                    <td><g:message code="mediation.label.id"/></td>
                    <td class="value">${selected.id}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.config"/></td>
                    <td class="value">${selected.configuration.name}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.start.time"/></td>
                    <td class="value"><g:formatDate date="${selected.startDatetime}" formatName="date.timeSecs.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.end.time"/></td>
                    <td class="value"><g:formatDate date="${selected.endDatetime}" formatName="date.timeSecs.format"/></td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.total.runtime"/></td>
                    <td class="value">
                        <g:if test="${selected.startDatetime && selected.endDatetime}">
                            <g:set var="runtime" value="${new Period(selected.startDatetime?.time, selected.endDatetime?.time)}"/>
                            <g:message code="mediation.runtime.format" args="[runtime.getHours(), runtime.getMinutes(), runtime.getSeconds()]"/>
                        </g:if>
                        <g:else>
                            -
                        </g:else>
                    </td>
                </tr>
            </tbody>
        </table>


        <!-- separator -->
        <div>
            <hr/>
        </div>


        <!-- mediation process stats -->
        <table cellspacing="0" cellpadding="0" class="dataTable">
            <tbody>
                <tr>
                    <td><g:message code="mediation.label.records"/></td>
                    <td class="value">${recordCount}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.orders.affected"/></td>
                    <td class="value">${selected.ordersAffected}</td>
                </tr>

                %{
                    def doneBillable = 0;
                    def doneNotBillable = 0;
                    def errorDetected = 0;
                    def errorDeclared = 0;

                    selected.records?.each {
                        if (it.recordStatus.id == Constants.MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE) doneBillable++;
                        if (it.recordStatus.id == Constants.MEDIATION_RECORD_STATUS_DONE_AND_NOT_BILLABLE) doneNotBillable++;
                        if (it.recordStatus.id == Constants.MEDIATION_RECORD_STATUS_ERROR_DETECTED) errorDetected++;
                        if (it.recordStatus.id == Constants.MEDIATION_RECORD_STATUS_ERROR_DECLARED) errorDeclared++;
                    }
                }%

                <tr>
                    <td><g:message code="mediation.label.done.billable"/></td>
                    <td class="value">${doneBillable}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.done.not.billable"/></td>
                    <td class="value">${doneNotBillable}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.errors.detected"/></td>
                    <td class="value">${errorDetected}</td>
                </tr>
                <tr>
                    <td><g:message code="mediation.label.errors.declared"/></td>
                    <td class="value">${errorDeclared}</td>
                </tr>
            </tbody>
        </table>

    </div>
    <div class="btn-box"></div>
</div>