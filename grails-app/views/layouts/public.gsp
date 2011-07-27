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

<%@ page import="com.sapienter.jbilling.server.user.db.CompanyDTO" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />

        <title><g:layoutTitle default="jBilling" /></title>

        <link rel="shortcut icon" href="${resource(dir:'images', file:'favicon.ico')}" type="image/x-icon" />

        <link media="all" rel="stylesheet" href="${resource(dir:'css', file:'all.css')}" type="text/css" />
        <!--[if lt IE 8]><link rel="stylesheet" href="${resource(dir:'css', file:'lt7.css')}" type="text/css" media="screen"/><![endif]-->

        <g:javascript library="jquery" plugin="jquery"/>
        <g:javascript library="slideBlock" />
        <g:javascript library="clearinput" />
        <g:javascript library="main" />
        <g:javascript library="form" />
        <g:javascript library="checkbox" />

        <g:layoutHead/>
    </head>
    <body>
        <div id="wrapper">
            <!-- header -->
            <div id="header">
                <h1><a href="${resource(dir:'')}"></a></h1>
            </div>
            <div id="navigation">
                <ul></ul>
            </div>

            <!-- content -->
            <div id="main">
                <g:layoutBody />
            </div>
        </div>
    </body>
</html>