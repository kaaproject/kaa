package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

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

    @Override
    public CTLSchema findByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching ctl schema by fqn [{}] and version [{}]", fqn, version);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterionWithAlias(CTL_SCHEMA_META_INFO_PROPERTY, CTL_SCHEMA_META_INFO_ALIAS, Restrictions.and(
                    Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_VERSION, version),
                    Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_FQN, fqn), tenantId != null
                            ? Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS, Long.valueOf(tenantId))
                            : Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_SCOPE, CTLSchemaScopeDto.SYSTEM))
            );
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
                Restrictions.eq(CTL_SCHEMA_META_INFO_ALIAS_SCOPE, CTLSchemaScopeDto.SYSTEM));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Search result: [{}].", Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("Search result: [{}].", schemas.size());
        }
        return schemas;
    }

    @Override
    public List<CTLSchema> findByTenantId(String tenantId) {
        LOG.debug("Searching ctl schemas by tenant id [{}]", tenantId);
        List<CTLSchema> availableSchemas = findListByCriterion(Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS, Long.valueOf(tenantId)));
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
    public List<CTLSchema> findDependentsSchemas(String schemaId) {
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
        List<CTLSchema> availableSchemas = findListByCriterion(Restrictions.or(Restrictions.eq(CTL_SCHEMA_TENANT_ID_ALIAS,
                Long.valueOf(tenantId)), Restrictions.isNull(CTL_SCHEMA_TENANT_ID_ALIAS)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: [{}].", tenantId, Arrays.toString(availableSchemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: [{}].", tenantId, availableSchemas.size());
        }
        return availableSchemas;
    }
}
