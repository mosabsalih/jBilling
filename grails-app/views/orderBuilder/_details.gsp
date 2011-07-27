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

<%--
  Order details form. Allows editing of primary order attributes.

  @author Brian Cowdery
  @since 23-Jan-2011
--%>

<div id="details-box">
    <g:formRemote name="order-details-form" url="[action: 'edit']" update="column2" method="GET">
        <g:hiddenField name="_eventId" value="update"/>
        <g:hiddenField name="execution" value="${flowExecutionKey}"/>

        <div class="form-columns">
            <g:applyLayout name="form/select">
                <content tag="label"><g:message code="order.label.period"/></content>
                <content tag="label.for">period</content>
                <g:select from="${orderPeriods}"
                          optionKey="id" optionValue="${{it.getDescription(session['language_id'])}}"
                          name="period"
                          value="${order?.period}"/>
            </g:applyLayout>

            <g:applyLayout name="form/select">
                <content tag="label"><g:message code="order.label.billing.type"/></content>
                <content tag="label.for">billingTypeId</content>
                <g:select from="${orderBillingTypes}"
                          optionKey="id" optionValue="${{it.getDescription(session['language_id'])}}"
                          name="billingTypeId"
                          value="${order?.billingTypeId}"/>
            </g:applyLayout>

            <g:applyLayout name="form/select">
                <content tag="label"><g:message code="order.label.status"/></content>
                <content tag="label.for">statusId</content>
                <g:select from="${orderStatuses}"
                          optionKey="statusValue" optionValue="${{it.getDescription(session['language_id'])}}"
                          name="statusId"
                          value="${order?.statusId}" 
                          disabled="${(!order.id || order.id == 0)}"/>
            </g:applyLayout>

            <g:applyLayout name="form/date">
                <content tag="label"><g:message code="order.label.active.since"/></content>
                <content tag="label.for">activeSince</content>
                <content tag="onClose">
                    function() {
                        $('#order-details-form').submit();
                    }
                </content>
                <g:textField class="field" name="activeSince" value="${formatDate(date: order?.activeSince, formatName: 'datepicker.format')}"/>
            </g:applyLayout>

            <g:applyLayout name="form/date">
                <content tag="label"><g:message code="order.label.active.until"/></content>
                <content tag="label.for">activeUntil</content>
                <content tag="onClose">
                    function() {
                        $('#order-details-form').submit();
                    }
                </content>
                <g:textField class="field" name="activeUntil" value="${formatDate(date: order?.activeUntil, formatName: 'datepicker.format')}"/>
            </g:applyLayout>

            <g:preferenceEquals preferenceId="${Constants.PREFERENCE_USE_PRO_RATING}" value="1">
                <g:applyLayout name="form/date">
                    <content tag="label"><g:message code="order.label.cycle.start"/></content>
                    <content tag="label.for">cycleStarts</content>
                    <content tag="onClose">
                        function() {
                            $('#order-details-form').submit();
                        }
                    </content>
                    <g:textField class="field" name="cycleStarts" value="${formatDate(date: order?.cycleStarts, formatName: 'datepicker.format')}"/>
                </g:applyLayout>
            </g:preferenceEquals>

            <g:preferenceEquals preferenceId="${Constants.PREFERENCE_USE_ORDER_ANTICIPATION}" value="1">
                <g:applyLayout name="form/input">
                    <content tag="label"><g:message code="order.label.anticipate.period"/></content>
                    <content tag="label.for">anticipatePeriods</content>
                    <g:textField class="field" name="anticipatePeriods" value="${order?.anticipatePeriods}"/>
                </g:applyLayout>
            </g:preferenceEquals>

            <g:applyLayout name="form/text">
                <content tag="label"><g:message code="prompt.due.date.override"/></content>
                <content tag="label.for">dueDateValue</content>

                <div class="inp-bg inp4">
                    <g:textField class="field" name="dueDateValue" value="${order?.dueDateValue}"/>
                </div>
                <div class="select4">
                    <g:select from="${periodUnits}"
                              optionKey="id"
                              optionValue="${{it.getDescription(session['language_id'])}}"
                              name="dueDateUnitId"
                              value="${order?.dueDateUnitId ?: Constants.PERIOD_UNIT_DAY}"/>
                </div>
            </g:applyLayout>

            <g:preferenceEquals preferenceId="${Constants.PREFERENCE_USE_CURRENT_ORDER}" value="1">
                <g:applyLayout name="form/checkbox">
                    <content tag="label"><g:message code="order.label.main.subscription"/></content>
                    <content tag="label.for">isCurrent</content>
                    <g:checkBox class="cb checkbox" name="isCurrent" value="${order?.isCurrent > 0}"/>
                </g:applyLayout>
            </g:preferenceEquals>

            <g:applyLayout name="form/checkbox">
                <content tag="label"><g:message code="order.label.notify.on.expire"/></content>
                <content tag="label.for">notify</content>
                <g:checkBox class="cb checkbox" name="notify" checked="${order?.notify > 0}"/>
            </g:applyLayout>
        </div>

        <hr/>

        <div class="form-columns">
            <div class="box-text">
                <label class="lb"><g:message code="prompt.notes"/></label>
                <g:textArea name="notes" rows="5" cols="60" value="${order?.notes}"/>
            </div>

            <g:applyLayout name="form/checkbox">
                <content tag="label"><g:message code="order.label.include.notes"/></content>
                <content tag="label.for">notesInInvoice</content>
                <g:checkBox class="cb checkbox" name="notesInInvoice" value="${order?.notesInInvoice > 0}"/>
            </g:applyLayout>
        </div>
    </g:formRemote>

    <script type="text/javascript">
        var orderStatus = $('#statusId').val();

        $(function() {
            $('#period').change(function() {
                if ($(this).val() == ${Constants.ORDER_PERIOD_ONCE}) {
                    $('#billingTypeId').val(${Constants.ORDER_BILLING_POST_PAID});
                    $('#billingTypeId').attr('disabled', true);
                } else {
                    $('#billingTypeId').attr('disabled', '');
                }
            }).change();

            $('#statusId').change(function() {
                if ($(this).val() == ${Constants.ORDER_STATUS_SUSPENDED}) {
                    $('#status-suspended-dialog').dialog('open');
                } else {
                    orderStatus = $(this).val();
                }
            });

            $('#order-details-form').find('input:checkbox').change(function() {
                $('#order-details-form').submit();
            });

            $('#order-details-form').find('input:text.hasDatepicker, select').change(function() {
                $('#order-details-form').submit();
            });

            $('#order-details-form').find('input:text, textarea').blur(function() {
                $('#order-details-form').submit();
            });

            $('#status-suspended-dialog').dialog({
                 autoOpen: false,
                 height: 200,
                 width: 375,
                 modal: true,
                 buttons: {
                     '<g:message code="prompt.yes"/>': function() {
                         $(this).dialog('close');
                     },
                     '<g:message code="prompt.no"/>': function() {
                         $('#statusId').val(orderStatus);
                         $(this).dialog('close');
                     }
                 }
             });
        });
    </script>

    <!-- confirmation dialog for status changes -->
    <div id="status-suspended-dialog" title="${message(code: 'popup.confirm.title')}">
        <table style="margin: 3px 0 0 10px">
            <tbody>
            <tr>
                <td valign="top">
                    <img src="${resource(dir:'images', file:'icon34.gif')}" alt="confirm">
                </td>
                <td class="col2" style="padding-left: 7px">
                    <g:message code="order.prompt.set.suspended" args="[order?.id]"/>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
