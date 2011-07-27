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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.DiscriminatorValue;

import com.sapienter.jbilling.server.process.db.AgeingEntityStepDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractGenericStatus;

@Entity
@DiscriminatorValue("user_status")
public class UserStatusDTO extends AbstractGenericStatus implements java.io.Serializable {

     private int canLogin;
     private Set<AgeingEntityStepDTO> ageingEntitySteps = new HashSet<AgeingEntityStepDTO>(0);
     private Set<UserDTO> baseUsers = new HashSet<UserDTO>(0);

    public UserStatusDTO() {
    }

    public UserStatusDTO(Integer statusvalue) {
        this.statusValue = statusValue;
    }
    
    public UserStatusDTO(int statusValue, int canLogin) {
        this.statusValue = statusValue;
        this.canLogin = canLogin;
    }
    
    public UserStatusDTO(int statusValue, int canLogin, Set<AgeingEntityStepDTO> ageingEntitySteps, Set<UserDTO> baseUsers) {
       this.statusValue = statusValue;
       this.canLogin = canLogin;
       this.ageingEntitySteps = ageingEntitySteps;
       this.baseUsers = baseUsers;
    }
    
    @Transient
    protected String getTable() {
        return Constants.TABLE_USER_STATUS;
    }
    
    @Column(name="can_login", nullable=false)
    public int getCanLogin() {
        return this.canLogin;
    }
    
    public void setCanLogin(int canLogin) {
        this.canLogin = canLogin;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="userStatus")
    public Set<AgeingEntityStepDTO> getAgeingEntitySteps() {
        return this.ageingEntitySteps;
    }
    
    public void setAgeingEntitySteps(Set<AgeingEntityStepDTO> ageingEntitySteps) {
        this.ageingEntitySteps = ageingEntitySteps;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="userStatus")
    public Set<UserDTO> getBaseUsers() {
        return this.baseUsers;
    }
    
    public void setBaseUsers(Set<UserDTO> baseUsers) {
        this.baseUsers = baseUsers;
    }




}


