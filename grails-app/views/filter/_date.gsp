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
  _created

  @author Brian Cowdery
  @since  30-11-2010
--%>



<div id="${filter.name}">
    <span class="title <g:if test='${filter.value}'>active</g:if>"><g:message code="filters.${filter.field}.title"/></span>
    <g:remoteLink class="delete" controller="filter" action="remove" params="[name: filter.name]" update="filters"/>
    
    <div class="slide">
        <fieldset>
            <div class="input-row">
                <div class="input-bg">
                    <a href="#" onclick="$('#filters\\.${filter.name}\\.startDateValue').datepicker('show')"></a>
                    <g:textField name="filters.${filter.name}.startDateValue" value="${formatDate(date: filter.startDateValue, formatName: 'datepicker.format')}"/>
                </div>
                <label for="filters.${filter.name}.startDateValue"><g:message code="filters.date.from.label"/></label>
            </div>

            <div class="input-row">
                <div class="input-bg">
                    <a href="#" onclick="$('#filters\\.${filter.name}\\.endDateValue').datepicker('show')"></a>
                    <g:textField name="filters.${filter.name}.endDateValue" value="${formatDate(date:filter.endDateValue, formatName: 'datepicker.format')}"/>
                </div>
                <label for="filters.${filter.name}.endDateValue"><g:message code="filters.date.to.label"/></label>
            </div>
        </fieldset>

        <script type="text/javascript">
            $(function() {
                $("#filters\\.${filter.name}\\.startDateValue").datepicker({dateFormat: "${message(code: 'datepicker.jquery.ui.format')}" });
                $("#filters\\.${filter.name}\\.endDateValue").datepicker({dateFormat: "${message(code: 'datepicker.jquery.ui.format')}" });
            });
        </script>
    </div>
</div>