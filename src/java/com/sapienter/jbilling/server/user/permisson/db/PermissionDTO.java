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


import com.sapienter.jbilling.client.authentication.InitializingGrantedAuthority;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDescription;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "permission")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class PermissionDTO extends AbstractDescription implements Serializable, InitializingGrantedAuthority {

    private int id;
    private PermissionTypeDTO permissionType;
    private Integer foreignId;
    private Set<PermissionUserDTO> permissionUsers = new HashSet<PermissionUserDTO>(0);
    private Set<RoleDTO> roles = new HashSet<RoleDTO>(0);

    private String authority;

    public PermissionDTO() {
    }

    public PermissionDTO(int id) {
        this.id = id;
    }

    public PermissionDTO(int id, PermissionTypeDTO permissionType) {
        this.id = id;
        this.permissionType = permissionType;
    }

    public PermissionDTO(int id, PermissionTypeDTO permissionType, Integer foreignId,
                         Set<PermissionUserDTO> permissionUsers, Set<RoleDTO> roles) {
        this.id = id;
        this.permissionType = permissionType;
        this.foreignId = foreignId;
        this.permissionUsers = permissionUsers;
        this.roles = roles;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    public PermissionTypeDTO getPermissionType() {
        return this.permissionType;
    }

    public void setPermissionType(PermissionTypeDTO permissionType) {
        this.permissionType = permissionType;
    }

    @Column(name = "foreign_id")
    public Integer getForeignId() {
        return this.foreignId;
    }

    public void setForeignId(Integer foreignId) {
        this.foreignId = foreignId;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "permission")
    public Set<PermissionUserDTO> getPermissionUsers() {
        return this.permissionUsers;
    }

    public void setPermissionUsers(Set<PermissionUserDTO> permissionUsers) {
        this.permissionUsers = permissionUsers;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "permission_role_map",
               joinColumns = {@JoinColumn(name = "permission_id", updatable = false)},
               inverseJoinColumns = {@JoinColumn(name = "role_id", updatable = false)}
    )
    public Set<RoleDTO> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    @Transient
    protected String getTable() {
        return Constants.TABLE_PERMISSION;
    }

    /**
     * Initialize the authority value
     */
    public void initializeAuthority() {
        String type = getPermissionType().getDescription();
        authority = type.toUpperCase().trim().replaceAll(" ", "_") + "_" + getId();
    }

    /**
     * Returns an authority string representing the granted permission. This string
     * is constructed of the {@link PermissionTypeDTO} description and the permission
     * id.
     *
     * Authority strings are in uppercase with all spaces replaced with underscores.
     *
     * e.g., "SERVER_ACCESS_137", "WEB_SERVICES_120", "ORDER_66" (kill all the jedi)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionDTO that = (PermissionDTO) o;

        if (id != that.id) return false;
        if (foreignId != null ? !foreignId.equals(that.foreignId) : that.foreignId != null) return false;
        if (permissionType != null ? !permissionType.equals(that.permissionType) : that.permissionType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (permissionType != null ? permissionType.hashCode() : 0);
        result = 31 * result + (foreignId != null ? foreignId.hashCode() : 0);
        return result;
    }
}


