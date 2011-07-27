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

<script type="text/javascript">
$(function() {
    $(".numericOnly").keydown(function(event){
    	// Allow only backspace, delete, left & right 
        if ( event.keyCode==37 || event.keyCode== 39 || event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 ) {
            // let it happen, don't do anything
        }
        else {
            // Ensure that it is a number and stop the keypress
            if (event.keyCode < 48 || event.keyCode > 57 ) {
                event.preventDefault(); 
            }   
        }
    });
});
</script>

<div class="form-edit" style="width:100%">

    <div class="heading">
        <strong><g:message code="mediation.config.title"/></strong>
    </div>

    <div class="form-hold">
        <g:form name="save-customcontactfields-form" action="save">
            <g:hiddenField name="recCnt" value="${types?.size()}"/>
            <fieldset>
                <div class="form-columns" style="width:100%">
                    <div class="one_column" style="width: 100%;padding-right: 0px;">
                    <table class="innerTable" id="custom_fields" style="width: 100%;">
                        <thead class="innerHeader">
                             <tr>
                                <th><g:message code="mediation.config.id"/></th>
                                <th class="medium"><g:message code="mediation.config.name"/></th>
                                <th class="small"><g:message code="mediation.config.order"/></th>
                                <th class="large"><g:message code="mediation.config.plugin"/></th>
                                <th></th>
                             </tr>
                         </thead>
                         <tbody>
                            <g:each var="type" in="${types}">
                                <tr>
                                    <td>${type.id}</td>
                                    <td class="medium">
                                        <g:textField class="inp-bg" style="width: 180px" name="obj[${type.id}].name" 
                                            value="${type.name}"/>
                                    </td>
                                    <td class="small">
	                                    <g:textField class="inp-bg numericOnly inp2" name="obj[${type.id}].orderValue"  
                                            value="${type.orderValue}"/>
                                    </td>
                                    <td class="large">
                                        <g:select name="obj[${type.id}].pluggableTaskId" from="${readers}"
                                              optionKey="id" optionValue="${{'(Id:' + it.id + ') ' + it.type?.getDescription(session.language_id)}}"
                                              value="${type?.pluggableTaskId}" style="float: center;width: 100%"/>
                                    </td>
                                    <td>
                                        <a onclick="showConfirm('delete-${type.id}');" class="delete" style="
                                            width:9px;
                                            height:9px;
                                            text-indent:-9999px;
                                            background:url(../images/icon03.gif) no-repeat;
                                            float:right;
                                            margin:11px 8px 0 0;
                                            padding:0;"/>
                                        <g:render template="/confirm" model="['message': 'mediation.config.delete.confirm',
                                                  'controller': 'mediationConfig',
                                                  'action': 'delete',
                                                  'id': type.id,
                                                  'ajax': true,
                                                  'update': 'column1',
                                                  'onYes': 'closePanel(\'#column2\')'
                                                 ]"/>
                                    </td>
                                </tr>
                            </g:each>
                            <tr>
                                <td>New</td>
                                <td class="medium">
                                    <g:textField class="inp-bg" style="width: 180px" name="name" value=""/>
                                </td>
                                <td class="small"><g:textField class="inp-bg numericOnly inp2" name="orderValue"  
                                    value=""/></td>
                                <td class="large">
                                    <g:select name="pluggableTaskId" from="${readers}"
                                         optionKey="id" 
                                         optionValue="${{'(Id:' + it.id + ') ' + it.type?.getDescription(session.language_id)}}" 
                                         value="" style="float: center;width: 100%"/>
                                </td>
                                <td></td>
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