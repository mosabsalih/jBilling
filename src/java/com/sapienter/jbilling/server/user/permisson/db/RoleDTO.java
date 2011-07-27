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
package com.sapienter.jbilling.server.user.permisson.db;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.sapienter.jbilling.client.authentication.InitializingGrantedAuthority;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OrderBy;

import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "role")
@TableGenerator(
        name="role_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="role",
        allocationSize = 10
        )
public class RoleDTO extends AbstractDescription implements Serializable, InitializingGrantedAuthority {

    public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";
    public static final Integer AUTHORITY_LANGUAGE_ID = 1; // authority values in english

    private int id;
    private Set<UserDTO> baseUsers = new HashSet<UserDTO>(0);
    private Set<PermissionDTO> permissions = new HashSet<PermissionDTO>(0);

    private String authority;

    public RoleDTO() {
    }

    public RoleDTO(int id) {
        this.id = id;
    }

    public RoleDTO(int id, Set<UserDTO> baseUsers, Set<PermissionDTO> permissions) {
        this.id = id;
        this.baseUsers = baseUsers;
        this.permissions = permissions;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "role_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_role_map",
               joinColumns = {@JoinColumn(name = "role_id", updatable = false)},
               inverseJoinColumns = {@JoinColumn(name = "user_id", updatable = false)}
    )
    public Set<UserDTO> getBaseUsers() {
        return this.baseUsers;
    }

    public void setBaseUsers(Set<UserDTO> baseUsers) {
        this.baseUsers = baseUsers;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "permission_role_map",
               joinColumns = {@JoinColumn(name = "role_id", updatable = false)},
               inverseJoinColumns = {@JoinColumn(name = "permission_id", updatable = false)}
    )
    @OrderBy(clause = "permission_id")
    public Set<PermissionDTO> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_ROLE;
    }

    @Transient
    public String getTitle(Integer languageId) {
        return getDescription(languageId, "title");
    }

    /**
     * Initialize the authority value
     */
    public void initializeAuthority() {
        String title = getTitle(AUTHORITY_LANGUAGE_ID);
        if (title != null)
            authority = ROLE_AUTHORITY_PREFIX + title.toUpperCase().trim().replaceAll(" ", "_");
    }

    /**
     * Returns an authority string representing the granted role of a user. This
     * string is constructed of the role "title" in the format "ROLE_TITLE".
     *
     * Authority strings are in uppercase with all spaces replaced with underscores.
     *
     * e.g., "ROLE_ADMIN", "ROLE_CLERK", "ROLE_USER"
     * 
     * @return authority string
     */
    @Transient
    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return getAuthority();
    }
}


