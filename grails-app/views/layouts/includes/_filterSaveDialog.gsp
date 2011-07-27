
<div id="filter-save-dialog" title="Save Filters">
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

<g:formRemote name="filter-save-form" url="[controller: 'filter', action: 'save']" update="filtersets">
        <div id="filtersets" class="columns-holder">

            <!-- content rendered using ajax -->

        </div>
    </g:formRemote>
</div>

<script type="text/javascript">
    $(function() {
        $('#filter-save-dialog').dialog({
            autoOpen: false,
            height: 500,
            width: 820,
            modal: true,
            buttons: {
                Close: function() {
                    $(this).dialog("close");
                }
            },
            open: function() {
                $('#filtersets').load("${createLink(controller: 'filter', action: 'filtersets')}");
            },
            close: function() {
                $('#filters').load("${createLink(controller: 'filter', action: 'filters')}");
            }
        });
    });
</script>