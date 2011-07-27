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

package com.sapienter.jbilling.server.security;

/**
 * Interface that marks a Web Service object as being "secure" within an entity. <code>WSSecured</code> objects
 * may only be accessed and modified by web service users (callers) within the same entity as the object being
 * accessed/modified.
 *
 * Implementing classes must be able to provide <strong>either</strong> an entity id or
 * user id for the owner of the object.
 *
 * @author Brian Cowdery
 * @since 01-11-2010
 */
public interface WSSecured {

    /**
     * Returns the entity ID of the company owning the secure object, or null
     * if the entity ID is not available.
     *
     * @return owning entity ID
     */
    public Integer getOwningEntityId();

    /**
     * Returns the user ID of the user owning the secure object, or null if the
     * user ID is not available.
     *
     * @return owning user ID
     */
    public Integer getOwningUserId();

}
