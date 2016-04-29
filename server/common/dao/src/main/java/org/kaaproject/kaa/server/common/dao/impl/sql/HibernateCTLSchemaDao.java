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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_ID_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_PROP;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_VERSION;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HibernateCTLSchemaDao extends HibernateAbstractDao<CTLSchema> implements CTLSchemaDao<CTLSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDao.class);

    @Override
    protected Class<CTLSchema> getEntityClass() {
        return CTLSchema.class;
    }

    private Criterion buildScopeCriterion(String tenantId, String applicationId) {
        if (isBlank(tenantId)) {
           // SYSTEM - tenantId=null && appId=null 
            return Restrictions.and(Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID),
                                    Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID));
        } else {
            if (isBlank(applicationId)) {
                // TENANT - (tenantId=id or tenantId=null) && appId=null 
                return Restrictions.and(Restrictions.or(
                                            Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID, Long.valueOf(tenantId)),
                                            Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID)
                                                       ),
                                        Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID));
            } else {
                // APPLICATION - (tenantId=id or tenantId=null) && (appId=id or appId=null)
                return Restrictions.and(Restrictions.or(
                                            Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID, Long.valueOf(tenantId)),
                                            Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID)
                                                       ),
                                        Restrictions.or(
                                            Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID, Long.valueOf(applicationId)),
                                            Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID)));
            }
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CTLSchema save(CTLSchema o) {
        return super.save(o);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CTLSchema save(CTLSchema o, boolean flush) {
        return super.save(o, flush);
    }

    @Override
    public List<CTLSchema> findSystemSchemas() {
        LOG.debug("Searching system ctl metadata");
        List<CTLSchema> schemas = findListByCriterionWithAlias(
                CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                buildScopeCriterion(null, null));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: [{}].", Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("Search result: [{}].", schemas.size());
        }
        return schemas;
    }

    @Override
    public List<CTLSchema> findAvailableSchemasForTenant(String tenantId) {
        LOG.debug("Searching available ctl schemas for tenant with id [{}]", tenantId);
        List<CTLSchema> availableSchemas = findListByCriterionWithAlias(
                CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                buildScopeCriterion(tenantId, null));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", tenantId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", tenantId, availableSchemas.size());
        }
        return availableSchemas;
    }

    @Override
    public List<CTLSchema> findAvailableSchemasForApplication(String tenantId, String appId) {
        LOG.debug("Searching available ctl schemas for application by tenant id [{}] and application id [{}]", tenantId, appId);
        List<CTLSchema> availableSchemas = findListByCriterionWithAlias(
                CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                buildScopeCriterion(tenantId, appId));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}][{}] Search result: [{}].", tenantId, appId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}][{}] Search result: [{}].", tenantId, appId, availableSchemas.size());
        }
        return availableSchemas;
    }

    @Override
    public CTLSchema findByFqnAndVerAndTenantIdAndApplicationId(String fqn, 
            Integer version, String tenantId, String applicationId) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching ctl schema by fqn [{}], version [{}], tenant [{}] and applicationId [{}]", 
                fqn, version, tenantId, applicationId);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterionWithAlias(
                    CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                    Restrictions.and(Restrictions.eq(CTL_SCHEMA_VERSION, version),
                                     Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn),
                                     tenantId != null
                                         ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID, Long.valueOf(tenantId))
                                                 : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID),
                                     applicationId != null
                                         ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID, Long.valueOf(applicationId))
                                                 : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{},{}] Search result: [{}].", fqn, version, tenantId, applicationId, ctlSchema);
        } else {
            LOG.debug("[{},{},{},{}] Search result: [{}].", fqn, version, tenantId, applicationId, ctlSchema != null);
        }
        return ctlSchema;
    }

    @Override
    public CTLSchema findByMetaInfoIdAndVer(String metaInfoId, Integer version) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching ctl schema by meta info id [{}] and version [{}]", 
                metaInfoId, version);
        if (isNotBlank(metaInfoId) && version != null) {
            ctlSchema = findOneByCriterionWithAlias(
                    CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                    Restrictions.and(Restrictions.eq(CTL_SCHEMA_VERSION, version),
                                     Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_ID, Long.valueOf(metaInfoId))));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: [{}].", metaInfoId, version, ctlSchema);
        } else {
            LOG.debug("[{},{}] Search result: [{}].", metaInfoId, version, ctlSchema != null);
        }
        return ctlSchema;
    }

    @Override
    public CTLSchema findAnyByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId,
            String applicationId) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching any ctl schema by fqn [{}], version [{}], tenant [{}] and applicationId [{}]", 
                fqn, version, tenantId, applicationId);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterionWithAlias(
                    CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                    Restrictions.and(Restrictions.eq(CTL_SCHEMA_VERSION, version),
                                     Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn),
                                     buildScopeCriterion(tenantId, applicationId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{},{}] Search result: [{}].", fqn, version, tenantId, applicationId, ctlSchema);
        } else {
            LOG.debug("[{},{},{},{}] Search result: [{}].", fqn, version, tenantId, applicationId, ctlSchema != null);
        }
        return ctlSchema;
    }

    @Override
    public CTLSchema findLatestByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        LOG.debug("Searching latest ctl schema by fqn [{}], tenantId [{}] and applicationId [{}]", fqn, tenantId, applicationId);
        Criteria criteria = getCriteria().createAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS)
                .add(Restrictions.and(
                        Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn),
                        tenantId != null
                                ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID, Long.valueOf(tenantId))
                                : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID),
                        applicationId != null
                                ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID, Long.valueOf(applicationId))
                                : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID))
                ).addOrder(Order.desc(CTL_SCHEMA_VERSION))
                .setMaxResults(FIRST);
        CTLSchema latestSchema = findOneByCriteria(criteria);
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: [{}].", fqn, tenantId, applicationId, latestSchema);
        } else {
            LOG.debug("[{},{},{}] Search result: [{}].", fqn, tenantId, applicationId, latestSchema != null);
        }
        return latestSchema;
    }

    @Override
    public CTLSchema findLatestByMetaInfoId(String metaInfoId) {
        CTLSchema latestSchema = null;
        LOG.debug("Searching latest version of ctl schema by meta info id [{}]", 
                metaInfoId);
        if (isNotBlank(metaInfoId)) {
            Criteria criteria = getCriteria().createAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS)
                                             .add(Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_ID, Long.valueOf(metaInfoId)))
                                             .addOrder(Order.desc(CTL_SCHEMA_VERSION))
                                             .setMaxResults(FIRST);
            latestSchema = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", metaInfoId, latestSchema);
        } else {
            LOG.debug("[{}] Search result: [{}].", metaInfoId, latestSchema != null);
        }
        return latestSchema;
    }

    @Override
    public List<CTLSchema> findAllByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        LOG.debug("Searching available version of ctl schema by fqn [{}], tenantId [{}] and applicationId [{}]", fqn, tenantId, applicationId);
        Criteria criteria = getCriteria().createAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS)
                .add(Restrictions.and(
                        Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn),                    
                        tenantId != null
                                ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID, Long.valueOf(tenantId))
                                : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_TENANT_ID),
                        applicationId != null
                                ? Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID, Long.valueOf(applicationId))
                                : Restrictions.isNull(CTL_SCHEMA_META_INFO_ALIAS_APPLICATION_ID))
                ).addOrder(Order.asc(CTL_SCHEMA_VERSION));
        List<CTLSchema> schemas = findListByCriteria(criteria);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: [{}].", Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("Search result: [{}].", schemas.size());
        }
        return schemas;
    }

    @Override
    public List<CTLSchema> findAllByMetaInfoId(String metaInfoId) {
        LOG.debug("Searching available version of ctl schema by meta info id [{}]", metaInfoId);
        Criteria criteria = getCriteria().createAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS)
                .add(Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_ID, Long.valueOf(metaInfoId))                    
                ).addOrder(Order.asc(CTL_SCHEMA_VERSION));
        List<CTLSchema> schemas = findListByCriteria(criteria);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: [{}].", Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("Search result: [{}].", schemas.size());
        }
        return schemas;
    }

    @Override
    public List<CTLSchema> findDependentSchemas(String schemaId) {
        LOG.debug("Searching dependents ctl schemas for schema with id [{}]", schemaId);
        List<CTLSchema> dependentsList = findListByCriterionWithAlias(CTL_SCHEMA_DEPENDENCY_PROP, CTL_SCHEMA_DEPENDENCY_ALIAS,
                JoinType.INNER_JOIN, Restrictions.eq(CTL_SCHEMA_DEPENDENCY_ID_ALIAS, Long.valueOf(schemaId)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", schemaId, Arrays.toString(dependentsList.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", schemaId, dependentsList.size());
        }
        return dependentsList;
    }

}
