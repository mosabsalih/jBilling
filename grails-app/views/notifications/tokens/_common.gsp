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

<div class="row">
    <div>
        <span>
            <u><g:message code="label.token.heading.user.contact"/></u>
        </span>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$contact');"
            class=""
        > <span> <g:message
                    code="label.token.users.contact.object"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$first_name');"
            class=""
        ><span><g:message
                    code="label.token.users.firstname"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$last_name');"
            class=""
        ><span><g:message
                    code="label.token.users.lastname"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$address1');"
            class=""
        ><span><g:message
                    code="label.token.users.address1"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$address2');"
            class=""
        ><span><g:message
                    code="label.token.users.address2"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$city');"
            class=""
        ><span><g:message
                    code="label.token.users.city"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$postal_code');"
            class=""
        ><span><g:message
                    code="label.token.users.postalcode"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$state_province');" class=""
        ><span><g:message
                    code="label.token.users.state"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <!-- user's company name -->
        <a href="javascript:void(0)"
            onclick="testfunc('$organization_name');" class=""
        ><span><g:message
                    code="label.token.users.companyname"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <span>
            <u><g:message code="label.token.heading.user.details"/></u>
        </span>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$user');"
            class=""
        ><span><g:message
                    code="label.token.user.object"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$username');"
            class=""
        ><span><g:message code="label.token.user.name" /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$password');"
            class=""
        ><span><g:message code="label.token.user.password" />
        </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$user_id');"
            class=""
        ><span><g:message code="label.token.user.id" /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$credit_card');"
            class=""
        ><span><g:message
                    code="label.token.user.credit_card"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$credit_card.ccNumberPlain');" class=""
        ><span><g:message
                    code="label.token.credit_card.last4"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$credit_card.ccExpiry');" class=""
        ><span><g:message
                    code="label.token.credit_card.expiry"
                /> </span> </a>
    </div>
</div>

<!-- 
</div>
    <div class="column">
 -->

<div class="row">
    <div>
        <span>
            <u><g:message code="label.token.company.details"/></u>
        </span>
    </div>
</div>

<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact');" class=""
        ><span><g:message
                    code="label.token.company.contact.details"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_name');" class=""
        ><span><g:message code="label.token.company.name"/> </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.firstName');" class=""
        ><span><g:message
                    code="label.token.company.contact.firstname"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.lastName');" class=""
        ><span><g:message
                    code="label.token.company.contact.lastname"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.address1');" class=""
        ><span><g:message
                    code="label.token.company.contact.address1"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.address2');" class=""
        ><span><g:message
                    code="label.token.company.contact.address2"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.city');" class=""
        ><span><g:message
                    code="label.token.company.contact.city"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.postalCode');" class=""
        ><span><g:message
                    code="label.token.company.contact.postalcode"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)"
            onclick="testfunc('$company_contact.stateProvince');"
            class=""
        ><span><g:message
                    code="label.token.company.contact.state"
                /> </span> </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$company_id');"
            class=""
        ><span><g:message
                    code="label.token.company.id"
                /> </span> </a>
    </div>
</div>