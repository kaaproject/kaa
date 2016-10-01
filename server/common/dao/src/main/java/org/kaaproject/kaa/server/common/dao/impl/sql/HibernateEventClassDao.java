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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECFV_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECFV_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECFV_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.FQN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.common.dao.impl.EventClassDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class HibernateEventClassDao extends HibernateAbstractDao<EventClass>
        implements EventClassDao<EventClass> {

  private static final Logger LOG = LoggerFactory.getLogger(HibernateEventClassDao.class);

  @Override
  protected Class<EventClass> getEntityClass() {
    return EventClass.class;
  }

  @Override
  public List<EventClass> findByEcfvId(String ecfvId) {
    List<EventClass> eventClasses = Collections.emptyList();
    LOG.debug("Searching event classes by ecfv id [{}] ", ecfvId);
    if (isNotBlank(ecfvId)) {
      eventClasses = findListByCriterionWithAlias(ECFV_PROPERTY, ECFV_ALIAS, Restrictions.eq(
              ECFV_REFERENCE, Long.valueOf(ecfvId)));
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("[{}] Search result: {}.", ecfvId, Arrays.toString(eventClasses.toArray()));
    } else {
      LOG.debug("[{}] Search result: {}.", ecfvId, eventClasses.size());
    }
    return eventClasses;
  }

  @Override
  public List<EventClass> findByEcfvIdVersionAndType(String ecfvId, int version,
                                                     EventClassType type) {
    LOG.debug("Searching event class by ecfv id [{}] version [{}] and type [{}]",
            ecfvId, version, type);
    List<EventClass> eventClasses = Collections.emptyList();
    if (isNotBlank(ecfvId)) {
      List<Criterion> predicates = new ArrayList<>();
      predicates.add(Restrictions.eq(ECFV_REFERENCE, Long.valueOf(ecfvId)));
      predicates.add(Restrictions.eq(VERSION_PROPERTY, version));
      if (type != null) {
        predicates.add(Restrictions.eq(EVENT_CLASS_TYPE_PROPERTY, type));
      }
      eventClasses = findListByCriterionWithAlias(ECFV_PROPERTY, ECFV_ALIAS,
          Restrictions.and(predicates.toArray(new Criterion[]{})));
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("[{},{},{}] Search result: {}.", ecfvId, version, type,
              Arrays.toString(eventClasses.toArray()));
    } else {
      LOG.debug("[{},{},{}] Search result: {}.", ecfvId, version, type, eventClasses.size());
    }
    return eventClasses;
  }

  @Override
  public void removeByEcfvId(String ecfvId) {
    if (isNotBlank(ecfvId)) {
      List<EventClass> eventClasses = findListByCriterionWithAlias(ECFV_PROPERTY, ECFV_ALIAS,
          Restrictions.eq(ECFV_REFERENCE, Long.valueOf(ecfvId)));
      removeList(eventClasses);
    }
    LOG.debug("Removed event class by ecfv id [{}] ", ecfvId);
  }

  @Override
  public List<EventClass> findByTenantIdAndFqn(String tenantId, String fqn) {
    LOG.debug("Searching event classes by tenant id [{}] and fqn [{}]", tenantId, fqn);
    List<EventClass> eventClasses = Collections.emptyList();
    if (isNotBlank(tenantId)) {
      eventClasses = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
          Restrictions.and(
              Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
              Restrictions.eq(FQN_PROPERTY, fqn)));
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("[{},{}] Search result: {}.", tenantId, fqn, Arrays.toString(
              eventClasses.toArray()));
    } else {
      LOG.debug("[{},{}] Search result: {}.", tenantId, fqn, eventClasses.size());
    }
    return eventClasses;
  }

  @Override
  public EventClass findByTenantIdAndFqnAndVersion(String tenantId, String fqn, int version) {
    LOG.debug("Searching event classes by tenant id [{}], fqn [{}] and version [{}]",
            tenantId, fqn, version);
    EventClass eventClass = null;
    if (isNotBlank(tenantId)) {
      eventClass = findOneByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
          Restrictions.and(
              Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
              Restrictions.eq(FQN_PROPERTY, fqn),
              Restrictions.eq(VERSION_PROPERTY, version)
          ));
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("[{},{},{}] Search result: {}.", tenantId, fqn, version, eventClass);
    } else {
      LOG.debug("[{},{},{}] Search result: {}.", tenantId, fqn, version, eventClass != null);
    }
    return eventClass;
  }

}
