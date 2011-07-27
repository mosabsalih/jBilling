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

<%@page import="com.sapienter.jbilling.server.util.db.CurrencyDTO" %>
<%@ page import="com.sapienter.jbilling.server.process.db.BillingProcessDTO"%>
	
<div class="table-box">
	<div class="table-scroll">
    	<table id="processes" cellspacing="0" cellpadding="0">
			<thead>
				<tr>
					<th class="small">
                        <g:remoteSort action="list" sort="id" update="column1">
                            <g:message code="label.billing.cycle.id" />
                        </g:remoteSort>
                    </th>
					<th class="medium">
                        <g:remoteSort action="list" sort="billingDate" update="column1">
                            <g:message code="label.billing.cycle.date" />
                        </g:remoteSort>
                    </th>
					<th class="small">

                        <g:message code="label.billing.invoice.count" />
                    </th>
					<th class="medium">
                        <g:message code="label.billing.total.invoiced" />
                    </th>
					<th class="small">
                        <g:message code="label.billing.currency.code" />
                    </th>
				</tr>
			</thead>
	
			<tbody>
				<g:each var="dto" in="${lstBillingProcesses}">
					<tr id="process-${dto.id}" class="${selected?.id == dto.id ? 'active' : ''} ${dto?.isReview > 0 ? 'isReview' : ''}" 
                        onmouseover="this.style.cursor='hand'" 
                        onclick="javascript: document.location.href='/jbilling/billing/show/${dto.id}'">
						<td class="small">${dto.id}</td>
						<td class="medium">
                            <g:formatDate date="${dto.billingDate}" formatName="date.pretty.format"/>
						</td>
						<g:if test="${dataHashMap[dto.id] != null}">
                            <td class="small">${dataHashMap[dto.id][0]}</td>
                            <td class="medium">
                                <g:formatNumber number="${(dataHashMap[dto.id][1]?: 0) as BigDecimal}"
                                    type="currency" currencySymbol="${dataHashMap[dto.id][2]?.symbol}"/>
                            </td>
                            <td class="small">${dataHashMap[dto.id][2].code}</td>
                        </g:if>
                        <g:else>
                            <td class="small"></td>
                            <td class="medium"></td>
                            <td class="small"></td>
                        </g:else>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>

<g:if test="${lstBillingProcesses?.totalCount > params.max}">
    <div class="pager-box">
        <util:remotePaginate controller="billing" action="index" params="[applyFilter: true]" total="${lstBillingProcesses?.totalCount}" update="column1"/>
    </div>
</g:if>

<div class="pager-box">
    <div class="row">
        <div class="results">
            <g:render template="/layouts/includes/pagerShowResults" model="[steps: [10, 20, 50], update: 'column1']"/>
        </div>
    </div>

    <div class="row">
        <util:remotePaginate controller="mediation" action="list" params="${sortableParams(params: [partial: true])}" total="${orders?.totalCount ?: 0}" update="column1"/>
    </div>
</div>

<div class="btn-box">
    <div class="row"></div>
</div>
