/*
 * Copyright 2015 CyberVision, Inc.
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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_APPLICATION_ID_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_ID_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEPENDENCY_PROP;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_SCOPE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ALIAS_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TENANT_ID_ALIAS;

@Repository
public class HibernateCTLSchemaDao extends HibernateAbstractDao<CTLSchema> implements CTLSchemaDao<CTLSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDao.class);

    @Override
    protected Class<CTLSchema> getEntityClass() {
        return CTLSchema.class;
    }
    
    private Criterion buildScopeCriterion(CTLSchemaScopeDto... scopes) {
        List<Criterion> scopeCriterions = new ArrayList<>();
        for (CTLSchemaScopeDto scope : scopes) {
              scopeCriterions.add(Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_SCOPE, scope));
        }
        if (scopeCriterions.size() == 1) {
            return scopeCriterions.get(0);
        } else {
            return Restrictions.or(scopeCriterions.toArray(new Criterion[scopeCriterions.size()]));
        }
    }

    @Override
    public CTLSchema findByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching ctl schema by fqn [{}] and version [{}]", fqn, version);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterionWithAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS, Restrictions.and(
                    Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_VERSION, version),
                    Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn),
                    Restrictions.or(
                            tenantId != null
                                    ? Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS, Long.valueOf(tenantId))
                                    : Restrictions.isNull(CTL_SCHEMA_TENANT_ID_ALIAS),
                                    buildScopeCriterion(CTLSchemaScopeDto.SYSTEM))
            ));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: [{}].", fqn, version, ctlSchema);
        } else {
            LOG.debug("[{},{}] Search result: [{}].", fqn, version, ctlSchema != null);
        }
        return ctlSchema;
    }

    @Override
    public List<CTLSchema> findSystemSchemas() {
        LOG.debug("Searching system ctl metadata");
        List<CTLSchema> schemas = findListByCriterionWithAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                buildScopeCriterion(CTLSchemaScopeDto.SYSTEM));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: [{}].", Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("Search result: [{}].", schemas.size());
        }
        return schemas;
    }

    @Override
    public List<CTLSchema> findTenantSchemasByTenantId(String tenantId) {
        LOG.debug("Searching ctl schemas by tenant id [{}]", tenantId);
        List<CTLSchema> availableSchemas = findListByCriterionWithAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                Restrictions.and(Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS, Long.valueOf(tenantId)),
                        buildScopeCriterion(CTLSchemaScopeDto.TENANT)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", tenantId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", tenantId, availableSchemas.size());
        }
        return availableSchemas;
    }

    @Override
    public List<CTLSchema> findByApplicationId(String appId) {
        LOG.debug("Searching ctl schemas by application id [{}]", appId);
        List<CTLSchema> schemas = findListByCriterion(Restrictions.eq(CTL_SCHEMA_APPLICATION_ID_ALIAS, Long.valueOf(appId)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", appId, Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", appId, schemas.size());
        }
        return schemas;
    }

    @Override
    public CTLSchema findLatestByFqn(String fqn) {
        LOG.debug("Searching latest ctl schema by fqn [{}]", fqn);
        Criteria criteria = getCriteria().createAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS)
                .add(Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn)).addOrder(Order.desc(CTL_SCHEMA_META_INFO_ALIAS_VERSION))
                .setMaxResults(FIRST);
        CTLSchema latestSchema = findOneByCriteria(criteria);
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", fqn, latestSchema);
        } else {
            LOG.debug("[{}] Search result: [{}].", fqn, latestSchema != null);
        }
        return latestSchema;
    }

    @Override
    public void removeByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        CTLSchema ctlSchema = findByFqnAndVerAndTenantId(fqn, version, tenantId);
        if (ctlSchema != null) {
            remove(ctlSchema);
        }
        LOG.debug("Removed ctl schema by fqn [{}], version [{}] tenant id [{}]", fqn, version, tenantId);
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

    @Override
    public List<CTLSchema> findAvailableSchemas(String tenantId) {
        LOG.debug("Searching available ctl schemas for tenant with id [{}]", tenantId);
        List<CTLSchema> availableSchemas;
        if (tenantId != null) {
            availableSchemas = this.findListByCriterionWithAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS,
                    Restrictions.and(
                            Restrictions.or(
                                            Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS, Long.valueOf(tenantId)),
                                            Restrictions.isNull(CTL_SCHEMA_TENANT_ID_ALIAS)
                                           ), 
                            buildScopeCriterion(CTLSchemaScopeDto.SYSTEM, CTLSchemaScopeDto.TENANT)
                                    )
            );
        } else {
            availableSchemas = this.findSystemSchemas();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", tenantId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", tenantId, availableSchemas.size());
        }
        return availableSchemas;
    }
    
}
