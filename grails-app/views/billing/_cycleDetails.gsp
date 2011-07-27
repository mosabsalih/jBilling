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

<%@page import="com.sapienter.jbilling.server.process.BillingProcessBL"%>
<%@page import="com.sapienter.jbilling.common.CommonConstants;com.sapienter.jbilling.server.util.db.CurrencyDTO"%>
<%@page import="com.sapienter.jbilling.server.process.db.ProcessRunUserDAS"%>

<div class="table-info" >
    <em><g:message code="billing.details.label.process.id"/>:
        <strong> ${process?.id} </strong> 
    </em> 
    <em><g:message code="billing.details.label.start.date"/>:
        <strong> 
            <g:formatDate date="${process?.billingDate}" formatName="date.pretty.format"/>
        </strong>
    </em> 
    <em><g:message code="billing.details.label.end.date"/>:
        <strong>
            <g:formatDate date="${BillingProcessBL.getEndOfProcessPeriod(process)}" formatName="date.pretty.format"/>
        </strong>
    </em>
    <em><g:message code="billing.details.label.number.runs" />:
        <strong>${process?.processRuns?.size()} </strong></em>
</div>

<g:if test="${process?.isReview}">
<div class="table-info">
        <p><strong><g:message code="billing.details.is.review"/></strong>
        <g:message code="billing.details.is.review.explain"/></p>
</div>
<div class="table-info">
    <p>
    <strong>
        <g:if test="${ CommonConstants.REVIEW_STATUS_GENERATED.intValue() == reviewConfiguration?.reviewStatus}">
            <g:message code="billing.details.review.generated"/>
        </g:if>
        <g:if test="${ CommonConstants.REVIEW_STATUS_APPROVED.intValue() == reviewConfiguration?.reviewStatus}">
            <g:message code="billing.details.review.approved"/>
        </g:if>
        <g:if test="${ CommonConstants.REVIEW_STATUS_DISAPPROVED.intValue() == reviewConfiguration?.reviewStatus}">
            <g:message code="billing.details.review.disapproved"/>
        </g:if>
    </strong>
    </p>
</div>
<div class="table-info">
    <sec:access url="/billing/approve">
        &nbsp;<br>
        <a onclick="showConfirm('approve-'+${process?.id});" class="submit">
            <span><g:message code="billing.details.approve"/></span>
        </a>
    </sec:access>

    <sec:access url="/billing/disapprove">
        &nbsp;
        <a onclick="showConfirm('disapprove-'+${process?.id});" class="submit">
            <span><g:message code="billing.details.disapprove"/></span>
        </a><br>&nbsp;
    </sec:access>
</div>
</g:if>
<div class="table-info"></div>

<div class="table-area">
<table>
    <thead>
        <tr>
            <td class="first"><g:message code="billing.details.label.start.date.short"/>&nbsp;&nbsp;</td>
            <td><g:message code="billing.details.label.end.date.short"/>&nbsp;</td>
            <td><g:message code="billing.details.label.payment.processing.ended"/></td>
            <td><g:message code="billing.details.label.process.result"/></td>
            <td><g:message code="billing.details.label.process.users"/></td>
            <td><g:message code="billing.details.label.invoices.generated"/></td>
            <td><g:message code="billing.details.label.total.invoiced"/></td>
            <td class="col04"><g:message code="billing.details.label.total.paid"/></td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td><g:message code="billing.details.label.total.not.paid"/></td>
            <td class="last"><g:message code="billing.details.label.currency"/></td>
        </tr>
    </thead>
    <tbody>
    <g:set var="allTTLFailed" value="${new BigDecimal(0)}"/>
    <g:set var="allTTLPaid" value="${new BigDecimal(0)}"/>
    <g:set var="processRunUserDAS" value="${new ProcessRunUserDAS()}"/>
    <g:set var="currencyNow" value="${null}"/>
    <g:set var="diffCurrncy" value="${false}"/>
    
    <g:each var="run" in="${process.processRuns}">
        <g:set var="ttlInvcd" value="${new BigDecimal(0)}"/>
        <g:set var="ttlSuccessAmt" value="${new BigDecimal(0)}"/>
        <g:set var="ttlFailedAmt" value="${new BigDecimal(0)}"/>
        
        <g:each status="idx" var="cur" in="${countAndSumByCurrency}">
            <tr>
                <g:if test="${idx == 0}">
                    <td class="col02">
                        <g:formatDate date="${run?.started}" formatName="date.pretty.format"/><br>
                        <g:formatDate date="${run?.started}" format="hh:mm:ss a"/>
                    </td>
                    <td>
                        <g:formatDate date="${run?.finished}" formatName="date.pretty.format"/><br>
                        <g:formatDate date="${run?.finished}" format="hh:mm:ss a"/>
                    </td>
                    <td>
                        <g:if test="${run.paymentFinished != null}">
                            <g:formatDate date="${run?.paymentFinished}" formatName="date.pretty.format"/><br>
                            <g:formatDate date="${run?.paymentFinished}" format="hh:mm:ss a"/>
                        </g:if>
                    </td>
                    <td>${run?.status?.getDescription(session.language_id) }</td>
                    <td><g:message code="billing.details.users.succeeded"/>: 
                            ${processRunUserDAS?.findSuccessfullUsersCount(run?.id)}
                        <br><g:message code="billing.details.users.failed"/>: 
                            ${processRunUserDAS?.findFailedUsersCount(run?.id)}</td>
                    <td>
                        <g:message code="billing.details.label.from.invoices"/>:&nbsp;
                        <g:link action="showInvoices" id="${process?.id}">
                            ${invoicesGenerated}
                        </g:link><br>
                        <g:message code="billing.details.label.from.orders"/>:&nbsp;
                        <g:link action="showOrders" id="${process?.id}">
                            ${process?.orderProcesses?.size()}
                        </g:link>
                    </td>
                </g:if>
                <g:else>
                    <td class="col02"></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </g:else>
                
                <td></td>
                <td class="col01">
                    <g:each var="pymArr" in="${mapOfPaymentListByCurrency?.get( cur[2]?.getId() as Integer )}">
                        <g:set var="diffCurrncy" value="${(null == currencyNow? diffCurrncy: ( diffCurrncy || !(currencyNow.getId().equals(cur[2]?.getId() as Integer))) )}"/>
                        <g:set var="currencyNow" value="${new CurrencyDTO(cur[2]?.getId() as Integer)}"/>
                        <!-- Compute Total Paid (Sum of all successful payments for all currencies) -->
                        <g:set var="ttlSuccessAmt" value="${(ttlSuccessAmt as BigDecimal).add( (pymArr as Object[])[1] as BigDecimal )}"/>
                        <g:set var="allTTLPaid" value="${(allTTLPaid as BigDecimal).add(ttlSuccessAmt as BigDecimal)}"/>
                        
                        <em>
                        <g:formatNumber number="${((pymArr as Object[])[1]?: 0) as BigDecimal}" 
                            type="currency" currencySymbol="${currencyNow?.symbol}"/>
                        </em>
                    </g:each>
                    <em><b>
                        <g:formatNumber number="${(ttlSuccessAmt ?: 0) as BigDecimal}" 
                            type="currency" currencySymbol="${currencyNow?.symbol}"/>
                   </b></em>
                </td>
                <td>
                    <g:each var="pymArr" in="${mapOfPaymentListByCurrency?.get(cur[2]?.getId() as Integer)}">
                        <em>${(pymArr as Object[])[0]?.getDescription(session.language_id)}</em>
                    </g:each>
                    <em><b style="font-style: normal"><g:message code="billing.details.label.total"/></b></em>
                </td>
                <td></td>
                <td>${cur[2]?.getDescription(session.language_id)}</td>
            </tr>
            <g:set var="ttlInvcd" value="${(ttlInvcd as BigDecimal).add(cur[1] as BigDecimal)}"/>
        </g:each>
        
        <!-- Compute Total Not Paid (Sum of all failed payments) -->
        <g:each var="failedAmt" in="${failedAmountsByCurrency}">
            <g:set var="ttlFailedAmt" value="${(ttlFailedAmt as BigDecimal).add(failedAmt as BigDecimal)}"/>
            <g:set var="allTTLFailed" value="${(allTTLFailed as BigDecimal).add(ttlFailedAmt as BigDecimal)}"/>
        </g:each>
    
    </g:each>
    <tr class="bg">
        <td class="col02"></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <!-- If there are different currencies, do not show the Total Summary Row -->
        <g:if test="${diffCurrncy}">
            <td></td><td></td><td></td><td></td>
        </g:if>
        <g:else>
            <td><strong><!-- Total Invoiced -->
                    <g:formatNumber number="${((allTTLPaid as BigDecimal).add(allTTLFailed as BigDecimal)?: 0) as BigDecimal}" 
                        type="currency" currencySymbol="${currencyNow?.symbol}"/>
            </strong></td>
            <td class="col01"><em>
                    <g:formatNumber number="${ (allTTLPaid?:0) as BigDecimal}" 
                        type="currency" currencySymbol="${currencyNow?.symbol}"/>
            </em></td>
            <td></td>
            <td><strong>
                    <g:formatNumber number="${(allTTLFailed?:0) as BigDecimal}" 
                        type="currency" currencySymbol="${currencyNow?.symbol}"/>
            </strong></td>
        </g:else>
        <td></td>
    </tr>
    </tbody>
</table>
</div>

<g:render template="/confirm"
     model="['message':'billing.details.approve.confirm',
             'controller':'billing',
             'action':'approve',
             'id':process.id,
            ]"/>
<g:render template="/confirm"
     model="['message':'billing.details.disapprove.confirm',
             'controller':'billing',
             'action':'disapprove',
             'id':process.id,
            ]"/>