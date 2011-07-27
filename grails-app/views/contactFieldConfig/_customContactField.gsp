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

<%@ page contentType="text/html;charset=UTF-8" %>

<div class="form-edit" style="width:450px">

    <div class="heading">
        <strong><g:message code="configuration.title.contact.fields"/></strong>
    </div>

    <div class="form-hold">
        <g:form name="save-customcontactfields-form" action="save">
            <g:hiddenField name="recCnt" value="${types?.size()}"/>
            <fieldset>
                <div class="form-columns" style="width:450px">
                    <div class="one_column" style="padding-right: 0px;">
                    <table class="innerTable" id="custom_fields" style="width: 100%;">
                        <thead class="innerHeader">
                             <tr>
                                <th><g:message code="contact.field.name"/></th>
                                <th class="medium"><g:message code="contact.field.datatype"/></th>
                                <th class="medium"><g:message code="contact.field.isReadOnly"/></th>
                             </tr>
                         </thead>
                         <tbody>
                            <g:each status="iter" var="type" in="${types}">
                                <tr>
                                    <td>
                                        <g:textField class="field" style="float: right;width: 150px" name="obj[${iter}].description" 
                                            value="${type.getDescriptionDTO(session['language_id'])?.content}"/>
                                    </td>
                                    <td class="medium">
                                        <g:select style="float: right; position: relative; width:90px"  class="field" name="obj[${iter}].dataType" from="['String','Integer', 'Decimal', 'Boolean']"
                                            value="${type?.dataType}" />
                                    </td>
                                    <td class="medium">
                                        <g:select style="float: center; width: 50px;position: relative;" class="field" 
                                            name="obj[${iter}].readOnly" keys="[1,0]" from="['Yes', 'No']" value="${type.readOnly}"/>
                                        <g:hiddenField name="obj[${iter}].companyId" value="${session['company_id']}"/>
                                        <g:hiddenField name="obj[${iter}].id" value="${type.id}"/>
                                    </td>
                                </tr>
                            </g:each>
                            <tr>
                                <td><g:textField class="field" style="float: right;width: 150px" name="description" value=""/>
                                </td>
                                <td class="medium">
                                    <g:select style="float: right; position: relative; width:90px" class="field" 
                                        name="dataType" from="['','String','Integer', 'Decimal', 'Boolean']" value="" />
                                </td>
                                <td class="medium">
                                    <g:select style="float: center; width: 50px;position: relative;" class="field" 
                                        name="readOnly" keys="[1,0]" from="['Yes', 'No']" value=""/>
                                    <g:hiddenField name="companyId" value="${session['company_id']}"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    </div>
                    <div class="row">&nbsp;<br></div>
                </div>
            </fieldset>
             <div class="btn-box">
                <a onclick="$('#save-customcontactfields-form').submit();" class="submit save"><span><g:message code="button.save"/></span></a>
                <g:link controller="config" action="index" class="submit cancel"><span><g:message code="button.cancel"/></span></g:link>
            </div>
        </g:form>
    </div>
</div>