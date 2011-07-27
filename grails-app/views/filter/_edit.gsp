
<div class="column-hold">
    <div class="heading">
        <strong>
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

<g:if test="${selected}">
                <g:message code="filters.save.edit.title" args="[selected.name]"/>
            </g:if>
            <g:else>
                <g:message code="filters.save.new.title"/>
            </g:else>
        </strong>
    </div>

    <div class="box">
        <fieldset>
            <div class="form-columns">
                <g:applyLayout name="form/text">
                    <content tag="label"><g:message code="filters.save.label.id"/></content>

                    <g:if test="${selected}">
                        ${selected.id}
                        <g:hiddenField name="id" value="${selected?.id}"/>
                    </g:if>
                    <g:else>
                        <em><g:message code="prompt.id.new"/></em>
                    </g:else>
                </g:applyLayout>


                <g:if test="${selected}">
                    <g:applyLayout name="form/text">
                        <content tag="label"><g:message code="filters.save.label.name"/></content>
                        ${selected.name}
                    </g:applyLayout>
                </g:if>
                <g:else>
                    <g:applyLayout name="form/input">
                        <content tag="label"><g:message code="filters.save.label.name"/></content>
                        <content tag="label.for">name</content>
                        <g:textField class="field" name="name" value="${selected?.name}"/>
                    </g:applyLayout>
                </g:else>
            </div>
        </fieldset>

        <!-- spacer -->
        <div>
            <br/>&nbsp;
        </div>

        <!-- filter values -->
        <table cellpadding="0" cellspacing="0" class="innerTable" width="80%">
            <thead>
            <tr class="innerHeader">
                <th><g:message code="filter.values.th.field"/></th>
                <th><g:message code="filter.values.th.value"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="filter" in="${selected?.filters ?: filters}">
                <g:if test="${filter.value}">
                    <tr class="innerContent">
                        <td>${filter.field}</td>
                        <td>${filter.value}</td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="btn-box">
        <g:if test="${!selected}">
            <a class="submit save" onclick="$('#filter-save-form').submit();">
                <span><g:message code="button.save"/></span>
            </a>
        </g:if>
        <g:if test="${selected}">
            <g:remoteLink class="submit delete" controller="filter" action="delete" id="${selected.id}" update="filtersets">
                <span><g:message code="button.delete"/></span>
            </g:remoteLink>
        </g:if>
    </div>
</div>