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

package com.sapienter.jbilling.server.util.db;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Abstract class for status classes. The get/setId() methods maps to
 * the status_value, instead of the primary key. Allows use of status
 * constants as the id.
 */
@Entity
@Table(name = "generic_status")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="dtype",
    discriminatorType = DiscriminatorType.STRING
)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public abstract class AbstractGenericStatus extends AbstractDescription {

    protected int id;
    protected Integer statusValue;

    @Id 
    @Column(name="id", unique=true, nullable=false)
    public Integer getPrimaryKey() {
        return id;
    }
    
    public void setPrimaryKey(Integer id) {
        this.id = id;
    }

    @Column(name="status_value", unique=true, nullable=false)
    public int getId() {
        return statusValue;
    }
    
    public void setId(int statusValue) {
        this.statusValue = statusValue;
    }
}
