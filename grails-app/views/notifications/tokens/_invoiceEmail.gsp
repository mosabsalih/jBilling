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
            <u><g:message code="label.token.invoice.email"/></u>
        </span>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$total');"
            class=""
        ><span><g:message code="label.token.invoice.total.due" />
        </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$id');" class=""><span><g:message
                    code="label.token.invoice.id"
                />
        </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$number');"
            class=""
        ><span><g:message code="label.token.invoice.number" />
        </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$due_date');"
            class=""
        ><span><g:message code="label.token.invoice.due.date" />
        </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$notes');"
            class=""
        ><span><g:message code="label.token.invoice.notes" />
        </span>
        </a>
    </div>
</div>
<div class="row">
    <div>
        <a href="javascript:void(0)" onclick="testfunc('$invoice');"
            class=""
        ><span><g:message
                    code="label.token.invoice.dto.object"
                />
        </span>
        </a>
    </div>
</div>
