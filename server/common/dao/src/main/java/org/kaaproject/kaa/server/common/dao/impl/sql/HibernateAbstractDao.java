/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.server.common.dao.impl.SqlDao;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@SuppressWarnings("unchecked")
public abstract class HibernateAbstractDao<T extends GenericModel<?>> implements SqlDao<T> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateAbstractDao.class);

    public static final int FIRST = 1;
    private static final int MAX_TIMEOUT = 30000;

    @Autowired
    private SessionFactory sessionFactory;

    protected abstract Class<T> getEntityClass();

    @Override
    public Session getSession(FlushMode flushMode) {
        Session session = sessionFactory.getCurrentSession();
        session.setFlushMode(flushMode);
        return session;
    }

    @Override
    public Session getSession() {
        return getSession(FlushMode.AUTO);
    }

    @Override
    public void refresh(Object object) {
        getSession().refresh(object);
    }

    protected Criteria getCriteria() {
        return getSession().createCriteria(getEntityClass());
    }

    protected Query getQuery(String hql) {
        return getSession().createQuery(hql);
    }


    protected List<T> findListByCriterion(Criterion criterion) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entities by criterion [{}] ", className, criterion);
        Criteria criteria = getCriteria();
        criteria.add(criterion);
        List<T> resultList = criteria.list();
        if (resultList == null) {
            resultList = Collections.emptyList();
        }
        return resultList;
    }

    protected List<Long> toLongIds(List<String> ids) {
        List<Long> lids = Collections.emptyList();
        if (ids != null && !ids.isEmpty()) {
            lids = new ArrayList<>();
            for (String id : ids) {
                try {
                    Long lid = Long.parseLong(id);
                    lids.add(lid);
                } catch (NumberFormatException e) {
                    LOG.warn("Can't convert string id {} to Long id", id);
                }
            }
        }
        return lids;
    }

    protected List<T> findListByCriterionWithAlias(String path, String alias, Criterion criterion) {
        return findListByCriterionWithAlias(path, alias, null, criterion);
    }

    protected List<T> findListByCriterionWithAlias(String path, String alias, JoinType type, Criterion criterion) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entities by criterion [{}] ", className, criterion);
        Criteria criteria = getCriteria();
        if (type == null) {
            criteria.createAlias(path, alias);
        } else {
            criteria.createAlias(path, alias, type);
        }
        criteria.add(criterion);
        List<T> resultList = criteria.list();
        if (resultList == null) {
            resultList = Collections.emptyList();
        }
        return resultList;
    }

    protected List<T> findListByCriteria(Criteria criteria) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entities by criteria [{}] ", className, criteria);
        List<T> resultList = criteria.list();
        if (resultList == null) {
            resultList = Collections.emptyList();
        }
        return resultList;
    }

    protected T findOneByCriterion(Criterion criterion) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entity by criterion [{}] ", className, criterion);
        Criteria criteria = getCriteria();
        criteria.add(criterion);
        return (T) criteria.uniqueResult();
    }

    protected T findOneByCriterionWithLock(Criterion criterion, LockMode lockMode) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entity by criterion [{}] ", className, criterion);
        Criteria criteria = getCriteria();
        criteria.setLockMode(lockMode);
        criteria.add(criterion);
        return (T) criteria.uniqueResult();
    }

    protected T findOneByCriterionWithAlias(String path, String alias, Criterion criterion) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entity by criterion [{}] ", className, criterion);
        Criteria criteria = getCriteria();
        criteria.createAlias(path, alias);
        criteria.add(criterion);
        return (T) criteria.uniqueResult();
    }

    protected T findOneByCriteria(Criteria criteria) {
        String className = getSimpleClassName();
        LOG.trace("Searching {} entity by criteria [{}] ", className, criteria);
        return (T) criteria.uniqueResult();
    }

    @Override
    public T save(T o, boolean flush) {
        LOG.debug("Saving {} entity {}", getEntityClass(), o);
        Session session = getSession();
        o = (T) session.merge(o);
        if (flush) {
            session.flush();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Saving result: {}", o);
        } else {
            LOG.debug("Saving result: {}", o != null);
        }
        return o;
    }

    @Override
    public T save(T o) {
        return save(o, false);
    }

    public T update(T o) {
        LOG.trace("Updated {} entity: {} ", getSimpleClassName(), o);
        getSession().update(o);
        LOG.trace("[{}] Update result: {}", getSimpleClassName(), o != null);
        return o;
    }

    @Override
    public T persist(T o) {
        LOG.debug("Persisting {} entity {}", getEntityClass(), o);
        Session session = getSession();
        session.persist(o);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Persisting result: {}", o);
        } else {
            LOG.debug("Persisting result: {}", o != null);
        }
        return o;
    }

    @Override
    public <V> V save(V o, Class<?> clazz) {
        LOG.debug("Saving {} entity {}", clazz, o);
        Session session = getSession();
        session.saveOrUpdate(o);
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Saving result: {}", clazz, o);
        } else {
            LOG.debug("[{}] Saving result: {}", clazz, o != null);
        }
        return o;
    }

    @Override
    public List<T> find() {
        List<T> resultList = getCriteria().list();
        if (resultList == null) {
            resultList = Collections.emptyList();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: {}.", Arrays.toString(resultList.toArray()));
        } else {
            LOG.debug("Search result: {}.", resultList.size());
        }
        return resultList;
    }

    @Override
    public T findById(String id) {
        return findById(id, false);
    }

    @Override
    public T findById(String id, boolean lazy) {
        T result = null;
        String className = getSimpleClassName();
        LOG.debug("Searching {} entity by id [{}] ", className, id);
        if (isNotBlank(id)) {
            Session session = getSession();
            if (lazy) {
                result = (T) session.load(getEntityClass(), Long.parseLong(id));
            } else {
                result = (T) session.get(getEntityClass(), Long.parseLong(id));
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", id, result);
        } else {
            LOG.debug("[{}] Search result: {}.", id, result != null);
        }
        return result;
    }

    @Override
    public void removeAll() {
        Session session = getSession();
        session.delete(getEntityClass());
        LOG.debug("Removed all {} entities ", getSimpleClassName());
    }

    @Override
    public void removeById(String id) {
        if (isNotBlank(id)) {
            Session session = getSession();
            session.delete(findById(id, true));
            LOG.debug("Removed {} entity by id [{}]", getSimpleClassName(), id);
        }
    }

    @Override
    public Session.LockRequest lockRequest(LockOptions lockOptions) {
        int timeout = lockOptions.getTimeOut();
        if (timeout > MAX_TIMEOUT) {
            lockOptions.setTimeOut(MAX_TIMEOUT);
        }
        LOG.debug("Build lock request with options {}", lockOptions);
        return getSession().buildLockRequest(lockOptions);
    }

    protected void remove(T o) {
        if (o != null) {
            getSession().delete(o);
            LOG.debug("Removed entity {} ", o);
        }
    }

    protected void removeList(List<T> list) {
        if (list != null && !list.isEmpty()) {
            Session session = getSession();
            for (T o : list) {
                session.delete(o);
            }
            LOG.debug("Removed list of {} entities ", list);
        }
    }

    private String getSimpleClassName() {
        return getEntityClass().getSimpleName();
    }
}
