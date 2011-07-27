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
package com.sapienter.jbilling.server.user.db;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractGenericStatus;

@Entity
@DiscriminatorValue("subscriber_status")
public class SubscriberStatusDTO extends AbstractGenericStatus implements java.io.Serializable {

     private Set<UserDTO> baseUsers = new HashSet<UserDTO>(0);

    public SubscriberStatusDTO() {
    }

    
    public SubscriberStatusDTO(int statusValue) {
        this.statusValue = statusValue;
    }
    public SubscriberStatusDTO(int statusValue, Set<UserDTO> baseUsers) {
       this.statusValue = statusValue;
       this.baseUsers = baseUsers;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_USER_SUBSCRIBER_STATUS;
    }
   
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="subscriberStatus")
    public Set<UserDTO> getBaseUsers() {
        return this.baseUsers;
    }
    
    public void setBaseUsers(Set<UserDTO> baseUsers) {
        this.baseUsers = baseUsers;
    }




}


