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
  Quick edit form for the selected customer's notes.

  @author Brian Cowdery
  @since  26-Nov-2010
--%>

<div class="heading">
    <strong><g:message code="customer.detail.edit.note.title"/></strong>
</div>

<g:form id="notes-form" name="notes-form" url="[action: 'saveNotes']">
    <g:hiddenField name="id" value="${selected.id}"/>

    <div class="box">
        <div class="box-text">
            <label class="lb"><g:message code="customer.detail.note.title"/></label>
            <g:textArea name="notes" value="${selected.customer.notes}" rows="5" cols="60"/>
        </div>
    </div>

    <div class="btn-box buttons">
        <ul>
            <li><a class="submit save" onclick="$('#notes-form').submit();"><span><g:message code="button.save"/></span></a></li>
            <li><a class="submit cancel" onclick="closePanel(this);"><span><g:message code="button.cancel"/></span></a></li>
        </ul>
    </div>
</g:form>