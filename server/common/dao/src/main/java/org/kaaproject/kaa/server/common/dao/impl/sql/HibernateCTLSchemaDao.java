package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Repository
public class HibernateCTLSchemaDao extends HibernateAbstractDao<CTLSchema> implements CTLSchemaDao<CTLSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDao.class);

    @Override
    public CTLSchema save(CTLSchema o) {
        if (o.getTenant() == null) {
            CTLSchemaMetaInfo metaInfo = o.getMetaInfo();
            CTLSchema ctlSchema = findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
            if (ctlSchema != null) {
                throw new RuntimeException("Can't save system ctl schema with same fqn and version.");
            }
        }
        return super.save(o);
    }

    @Override
    protected Class<CTLSchema> getEntityClass() {
        return CTLSchema.class;
    }

    @Override
    public CTLSchema findByFqnAndVersion(String fqn, Integer version) {
        CTLSchema ctlSchema = null;
        LOG.debug("Searching ctl schema by fqn [{}] and version [{}]", fqn, version);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterionWithAlias("metaInfo","mi",Restrictions.and(
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
}
