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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;


import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.util.Context;
import org.hibernate.LockMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public abstract class AbstractDAS<T> extends HibernateDaoSupport {

    private static final Logger LOG = Logger.getLogger(AbstractDAS.class);
    private Class<T> persistentClass;

    // if querys will be run cached or not
    private boolean queriesCached = false;

    @SuppressWarnings("unchecked")
    public AbstractDAS() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        setSessionFactory((SessionFactory) Context.getBean(Context.Name.HIBERNATE_SESSION));
    }

    
    /**
     * Merges the entity, creating or updating as necessary
     *
     * @param newEntity entity to save/update
     * @return saved entity
     */
    @SuppressWarnings("unchecked")
    public T save(T newEntity) {
        T retValue = (T) getSession().merge(newEntity);
        return retValue;
    }
    
    public void delete(T entity) {
        //em.remove(entity);
        getHibernateTemplate().delete(entity);
    }

    public void refresh(T entity) {
        getHibernateTemplate().refresh(entity);
    }
    
    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    /**
     * This will load a proxy. If the row does not exist, it still returns an
     * object (not null) and  it will NOT throw an
     * exception (until the other fields are accessed).
     * Use this by default, if the row is missing, it is an error.
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public T find(Serializable id) {
        if (id == null) return null;
        return getHibernateTemplate().load(getPersistentClass(), id);
    }
    
    /**
     * This will hit the DB. If the row does not exist, it will NOT throw an
     * exception but it WILL return NULL
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public T findNow(Serializable id) {
        if (id == null) return null;
        return getHibernateTemplate().get(getPersistentClass(), id);
    }

    /**
     * This will lock the row for the duration of this transaction. Or wait until the row is
     * unlocked if it is already locked. It genererates a select ... for update
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public T findForUpdate(Serializable id) {
        if (id == null) {
            return null;
        }
        return getHibernateTemplate().get(getPersistentClass(), id, LockMode.UPGRADE);
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        crit.setCacheable(queriesCached);
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T findByExampleSingle(T exampleInstance, String... excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        crit.setCacheable(queriesCached);
        return (T) crit.uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
    public T makePersistent(T entity) {
        getHibernateTemplate().saveOrUpdate(entity);
        return entity;
    }

    public void makeTransient(T entity) {
        getHibernateTemplate().delete(entity);
    }

    public void flush() {
        getHibernateTemplate().flush();
    }

    public void clear() {
        getHibernateTemplate().clear();
    }

    /**
     * Returns true if a persisted record exsits for the given id.
     *
     * @param id primary key of entity
     * @return true if entity exists for id, false if entity does not exist
     */
    public boolean isIdPersisted(Serializable id) {
        Criteria criteria = getSession().createCriteria(getPersistentClass())
                .add(Restrictions.idEq(id))
                .setProjection(Projections.rowCount());

        return (criteria.uniqueResult() != null && ((Integer) criteria.uniqueResult()) > 0);
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        crit.setCacheable(queriesCached);
        return crit.list();
   }

    @SuppressWarnings("unchecked")
    protected T findByCriteriaSingle(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        crit.setCacheable(queriesCached);
        return (T) crit.uniqueResult();
   }

    protected void useCache() {
        queriesCached = true;
    }
    
    /**
     * Makes this DTO now attached to the session and part of the persistent context.
     * This WILL trigger an update, which is usually fine since the reason to reattach
     * is to modify the object.
     * @param dto
     */
    public void reattach(T dto) {
        getSession().update(dto);
    }

    /**
     * Places the DTO in the session without updates or version checkes.
     * You have to make sure that the DTO has not been modified to use this
     * @param dto
     */
    public void reattachUnmodified(T dto) {
        getSession().lock(dto, LockMode.NONE);
    }

    /**
     * Detaches the DTO from the session. Updates to the object will
     * no longer make it to the database.
     */
    public void detach(T dto) {
        getSession().flush(); // without this, get ready for the evil 'nonthreadsafe access to session'
        getSession().evict(dto);
    }
    
    protected void touch(List<T> list, String methodName) {
        try {
            Method toCall = persistentClass.getMethod(methodName);
            for(int f=0; list.size() < f; f++) {
                toCall.invoke(list.get(f));
            }
        } catch (Exception e) {
            throw new SessionInternalError("Error invoking method when touching proxy object", 
                    AbstractDAS.class, e);
            
        } 
    }
}
