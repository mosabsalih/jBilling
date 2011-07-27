/*
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
 */

package com.sapienter.jbilling.server.provisioning.config;

public class Request implements Comparable<Request> {
    private int order = 0;
    private String submit = null;
    private String rollback = null;
    private boolean postResult = false;
    private String continueOnType = null;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public String getRollback() {
        return rollback;
    }

    public void setRollback(String rollback) {
        this.rollback = rollback;
    }

    public boolean getPostResult() {
        return postResult;
    }

    public void setPostResult(boolean postResult) {
        this.postResult = postResult;
    }

    public String getContinueOnType() {
        return continueOnType;
    }

    public void setContinueOnType(String continueOnType) {
        this.continueOnType = continueOnType;
    }

    public int compareTo(Request other) {
        return order - other.order;
    }
}
