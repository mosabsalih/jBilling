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

<%@ page import="com.sapienter.jbilling.server.user.contact.db.ContactFieldTypeDTO" %>

<g:set var="ipAddressType" value="${ContactFieldTypeDTO.list().find{ it.promptKey ==~ /.*ip_address.*/ }}"/>

<div class="form-edit">
    <div class="heading">
        <strong><g:message code="blacklist.title"/></strong>
    </div>
    <div class="form-hold">
        <fieldset>
            <div class="form-columns single">
                <div class="column single">

                    <!-- blacklist upload -->
                    <g:uploadForm name="save-blacklist-form" url="[action: 'save']">
                        <g:applyLayout name="form/text">
                            <content tag="label"><g:message code="blacklist.label.csv.file"/></content>
                            <input type="file" name="csv"/>
                        </g:applyLayout>

                        <g:applyLayout name="form/radio">
                            <content tag="label">&nbsp;</content>

                            <g:radio class="rb" id="csvUpload.add" name="csvUpload" value="add" />
                            <label class="rb" for="csvUpload.add"><g:message code="blacklist.label.upload.type.add"/></label>

                            <g:radio class="rb" id="csvUpload.modify" name="csvUpload" value="modify" checked="${true}"/>
                            <label class="rb" for="csvUpload.modify"><g:message code="blacklist.label.upload.type.upload"/></label>
                        </g:applyLayout>

                        <div class="btn-row">
                            <br/>
                            <a onclick="$('#save-blacklist-form').submit();" class="submit save"><span><g:message code="button.update"/></span></a>
                        </div>
                    </g:uploadForm>


                    <!-- separator -->
                    <div>
                        <hr/>
                    </div>

                    <!-- blacklist entry list -->
                    <g:formRemote name="filter-form" url="[action: 'filter']" update="blacklist">
                        <g:applyLayout name="form/input">
                            <content tag="label"><g:message code="filters.title"/></content>
                            <content tag="label.for">filterBy</content>
                            <g:textField name="filterBy" class="field default" placeholder="${message(code: 'blacklist.filter.by.default')}" value="${params.filterBy}"/>
                        </g:applyLayout>

                        <script type="text/javascript">
                            $(function() {
                                $('#filterBy').blur(function() { $('#filter-form').submit(); });
                                placeholder();
                            });
                        </script>
                    </g:formRemote>


                    <div id="blacklist" style="height: 400px; overflow-y: scroll; margin: 10px 0; border: 1px solid #bbb;">
                        <g:render template="entryList" model="[blacklist: blacklist]"/>
                    </div>

                    <strong><g:message code="blacklist.label.entries" args="[blacklist.size()]"/></strong>

                    <!-- spacer -->
                    <div>
                        <br/>&nbsp;
                    </div>

                </div>
            </div>
        </fieldset>
    </div>
</div>