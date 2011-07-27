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

import com.sapienter.jbilling.server.util.Context;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.provider.CacheProviderFacade;

import java.io.Serializable;

/**
 * Abstract DAS class for status classes. The AbstractDAS find and
 * findNow methods are overridden to get objects by their 
 * status_value. Allows use of status constants as the id.
 *
 * @author emilc
 */
public abstract class AbstractGenericStatusDAS<T> extends AbstractDAS<T> {

    private static final Logger LOG = Logger.getLogger(AbstractGenericStatusDAS.class);

    private CacheProviderFacade cache;
    private CachingModel cacheModel;

    protected AbstractGenericStatusDAS() {
        super();
        cache = (CacheProviderFacade) Context.getBean(Context.Name.CACHE);
        cacheModel = (CachingModel) Context.getBean(Context.Name.CACHE_MODEL_READONLY);
    }

    /**
     * Returns the GenericStatus instance for the given status value.
     *
     * Note statusId is the "status_value" of the generic status instance, not the primary key,
     * to allow statuses to be queried using {@link com.sapienter.jbilling.common.CommonConstants} values.
     *
     * @param statusId status id (value)
     * @return found generic status object for the given ID
     */
    @Override
    @SuppressWarnings("unchecked")
    public T find(Serializable statusId) {
        if (statusId != null) {
            T value = (T) cache.getFromCache(getCacheKey(statusId), cacheModel);
            if (value == null) {
                value = findByCriteriaSingle(Restrictions.eq("id", statusId));
                if (value != null)
                    cache.putInCache(getCacheKey(statusId), cacheModel, value);
            }            
            return value;
        }
        return null;
    }

    @Override
    public T findNow(Serializable statusId) {
        return find(statusId);
    }

    /**
     * Return a serializable cache key for the given status Id. The cache key will
     * be constructed using the implemnting GenericStatus class, scoping the cache
     * to the status type.
     * 
     * Example:
     *      "SubscriberStatusDTO.1"
     *      "MediationRecordStatusDTO.2"
     *
     * @param statusId status id
     * @return cache key
     */
    public String getCacheKey(Serializable statusId) {
        return getPersistentClass().getSimpleName() + "." + statusId;
    }
}
