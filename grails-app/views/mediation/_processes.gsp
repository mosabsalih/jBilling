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

<div class="table-box">
	<div class="table-scroll">
    	<table id="processes" cellspacing="0" cellpadding="0">
			<thead>
				<tr>
					<th class="large">
                        <g:remoteSort action="list" sort="id" update="column1">
                            <g:message code="mediation.th.id" />
                        </g:remoteSort>
                    </th>
					<th class="small2">
                        <g:remoteSort action="list" sort="startDatetime" update="column1">
                            <g:message code="mediation.th.start.date" />
                        </g:remoteSort>
                    </th>
					<th class="small2">
                        <g:remoteSort action="list" sort="endDatetime" update="column1">
                            <g:message code="mediation.th.end.date" />
                        </g:remoteSort>
                    </th>
					<th class="small">
                        <g:message code="mediation.th.total.records" />
                    </th>
                    <th class="small">
                        <g:remoteSort action="list" sort="ordersAffected" update="column1">
                            <g:message code="mediation.th.orders.affected"/>
                        </g:remoteSort>
                    </th>
				</tr>
			</thead>
	
			<tbody>
				<g:each var="entry" in="${processes.entrySet()}">
                    <g:set var="proc" value="${entry.key}"/>
                    <g:set var="recordCount" value="${entry.value}"/>

					<tr id="mediation-${proc.id}" class="${proc?.id == processId ? 'active' : ''}">
						<td>
                            <g:remoteLink breadcrumb="id" class="cell double" action="show" id="${proc.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                <strong>${proc.id}</strong>
                                <em>${proc.configuration.name}</em>
                            </g:remoteLink>
                        </td>
						<td>
							<g:remoteLink breadcrumb="id" class="cell" action="show" id="${proc.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                <g:formatDate date="${proc.startDatetime}" formatName="date.timeSecs.format"/>
                            </g:remoteLink>
						</td>
                        <td>
                            <g:remoteLink breadcrumb="id" class="cell" action="show" id="${proc.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                <g:formatDate date="${proc.endDatetime}" formatName="date.timeSecs.format"/>
                            </g:remoteLink>
                        </td>
						<td>
                            <g:remoteLink breadcrumb="id" class="cell" action="show" id="${proc.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                ${recordCount}
                            </g:remoteLink>
                        </td>
                        <td>
                            <g:remoteLink breadcrumb="id" class="cell" action="show" id="${proc.id}" params="['template': 'show']" before="register(this);" onSuccess="render(data, next);">
                                ${proc.ordersAffected}
                            </g:remoteLink>
                        </td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>

<div class="pager-box">
    <div class="row">
        <div class="results">
            <g:render template="/layouts/includes/pagerShowResults" model="[steps: [10, 20, 50], update: 'column1']"/>
        </div>
    </div>

    <div class="row">
        <util:remotePaginate controller="mediation" action="index" params="${sortableParams(params: [partial: true])}" total="${processes?.totalCount ?: 0}" update="column1"/>
    </div>
</div>

<div class="btn-box">
    <div class="row"></div>
</div>
