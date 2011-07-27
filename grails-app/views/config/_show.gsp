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

<%@ page import="com.sapienter.jbilling.server.util.Constants" %>

<div class="column-hold">
    <div class="heading">
        <strong>
            <g:message code="preference.title"/>
            <em>${selected.id}</em>
        </strong>
    </div>

    <g:form name="save-preference-form" url="[action: 'save']">

    <div class="box">
        <p class="description">
            ${selected.getDescription(session['language_id'])}
        </p>

        <p>
            <em>${selected.getInstructions(session['language_id'])}</em>
        </p>

        <fieldset>
            <div class="form-columns">

                <g:set var="hasPreference" value="${false}"/>

                <g:each var="preference" status="index" in="${selected.preferences}">
                    <g:if test="${preference.jbillingTable.name == Constants.TABLE_ENTITY}">
                        <g:if test="${preference.foreignId == session['company_id']}">
                            <g:set var="hasPreference" value="${true}"/>
                            <g:render template="preference" model="[ preference: preference, type: selected]"/>
                        </g:if>
                    </g:if>
                </g:each>

                <g:if test="${!hasPreference}">
                    <g:render template="preference" model="[type: selected]"/>
                </g:if>

            </div>
        </fieldset>
    </div>

    </g:form>

    <div class="btn-box buttons">
        <ul>
            <li><a class="submit save" onclick="$('#save-preference-form').submit();"><span><g:message code="button.save"/></span></a></li>
            <li><a class="submit cancel" onclick="closePanel(this);"><span><g:message code="button.cancel"/></span></a></li>
        </ul>
    </div>

</div>
