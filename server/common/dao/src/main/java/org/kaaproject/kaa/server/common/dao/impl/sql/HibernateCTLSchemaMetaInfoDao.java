package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Repository
public class HibernateCTLSchemaMetaInfoDao extends HibernateAbstractDao<CTLSchemaMetaInfo> implements CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaMetaInfoDao.class);

    @Override
    protected Class<CTLSchemaMetaInfo> getEntityClass() {
        return CTLSchemaMetaInfo.class;
    }

    @Override
    public CTLSchemaMetaInfo findByFqnAndVersion(String fqn, Integer version) {
        CTLSchemaMetaInfo ctlSchema = null;
        LOG.debug("Searching ctl metadata by fqn [{}] and version [{}]", fqn, version);
        if (isNotBlank(fqn) && version != null) {
            ctlSchema = findOneByCriterion(Restrictions.and(
                    Restrictions.eq("version", version),
                    Restrictions.eq("fqn", fqn)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", fqn, version, ctlSchema);
        } else {
            LOG.debug("[{},{}] Search result: {}.", fqn, version, ctlSchema != null);
        }
        return ctlSchema;
    }
}
