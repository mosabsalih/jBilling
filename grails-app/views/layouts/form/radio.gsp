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
  Layout for labeled and styled radio groups.

  Usage:
  
    <g:applyLayout name="form/radio">
        <content tag="label">Radio Group Label</content>

        <input type="radio" class="rb" name="radio_group_name" id="option_1" />
        <label for="option_1" class="rb">Option 1</label>

        <input type="radio" class="rb" name="radio_group_name" id="option_2" />
        <label for="option_2" class="rb">Option 2</label>        
    </g:applyLayout>


  @author Brian Cowdery
  @since  25-11-2010
--%>

%{-- todo: CSS/Javascript Doesn't allow more than 2 radio elements or allow HTML elements to be marked as 'checked'--}%
<div class="row">
    <label><g:pageProperty name="page.label"/></label>
    <g:layoutBody/>
</div>