package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

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
            ctlSchema = findOneByCriterionWithAlias("metaInfo", "mi", Restrictions.and(
                    Restrictions.eq("mi.version", version),
                    Restrictions.eq("mi.fqn", fqn)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", fqn, version, ctlSchema);
        } else {
            LOG.debug("[{},{}] Search result: {}.", fqn, version, ctlSchema != null);
        }
        return ctlSchema;
    }

    @Override
    public List<CTLSchema> findSystemSchemas() {
        return null;
    }

    @Override
    public List<CTLSchema> findByTenantId(String tenantId) {
        return null;
    }

    @Override
    public List<CTLSchema> findByApplicationId(String appId) {
        return null;
    }

    @Override
    public CTLSchema findLatestByFqn(String fqn) {
        return null;
    }

    @Override
    public CTLSchema updateScope(CTLSchemaDto ctlSchema) {
        return null;
    }

    @Override
    public void removeByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {

    }

    @Override
    public List<CTLSchema> findDependentsSchemas(Long schemaId) {
        List<CTLSchema> dependentsList = findListByCriterionWithAlias("dependencySet", "dep", JoinType.INNER_JOIN,
                Restrictions.eq("dep.id", schemaId));
        return dependentsList;
    }
}
