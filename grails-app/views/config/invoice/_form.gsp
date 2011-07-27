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

<div class="form-edit">
    <div class="heading">
        <strong><g:message code="invoice.config.title"/></strong>
    </div>
    <div class="form-hold">
        <g:uploadForm name="save-invoice-form" url="[action: 'saveInvoice']">
            <fieldset>
                <div class="form-columns">
                    <div class="column single">

                        <!-- invoice numbering -->
                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="invoice.config.label.number"/></content>
                            <content tag="label.for">number</content>
                            <g:textField name="number" class="field" value="${number.value ?: number.preferenceType.defaultValue}"/>

                        </g:applyLayout>

                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="invoice.config.label.prefix"/></content>
                            <content tag="label.for">prefix</content>
                            <g:textField name="prefix" class="field" value="${prefix.value ?: prefix.preferenceType.defaultValue}"/>
                        </g:applyLayout>


                        <!-- spacer -->
                        <div>
                            <br/>&nbsp;
                        </div>


                        <!-- invoice logo upload -->
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="invoice.config.label.logo"/></content>
                            <img src="${createLink(action: 'entityLogo')}" alt="logo"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label">&nbsp;</content>
                            ${logoPath}
                        </g:applyLayout>

                        <g:applyLayout name="form/text">
                            <content tag="label">&nbsp;</content>
                            <input type="file" name="logo"/>
                        </g:applyLayout>


                        <!-- spacer -->
                        <div>
                            <br/>&nbsp;
                        </div>

                    </div>
                </div>
            </fieldset>
        </g:uploadForm>
    </div>

    <div class="btn-box">
        <a onclick="$('#save-invoice-form').submit();" class="submit save"><span><g:message code="button.save"/></span></a>
        <g:link controller="config" action="index" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link>
    </div>
</div>