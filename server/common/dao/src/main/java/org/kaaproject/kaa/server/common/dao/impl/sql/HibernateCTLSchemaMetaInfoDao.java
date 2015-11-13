package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Repository
public class HibernateCTLSchemaMetaInfoDao extends HibernateAbstractDao<CTLSchemaMetaInfo> implements CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaMetaInfoDao.class);

    public HibernateCTLSchemaMetaInfoDao() {
    }

    @Override
    protected Class<CTLSchemaMetaInfo> getEntityClass() {
        return CTLSchemaMetaInfo.class;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CTLSchemaMetaInfo save(CTLSchemaMetaInfo metaInfo) {
        CTLSchemaMetaInfo uniqueMetaInfo = findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
        if (uniqueMetaInfo == null) {
            uniqueMetaInfo = super.save(metaInfo);
        }
        return uniqueMetaInfo;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CTLSchemaMetaInfo incrementCount(CTLSchemaMetaInfo metaInfo) {
        CTLSchemaMetaInfo uniqueMetaInfo = findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
        if (uniqueMetaInfo != null) {
            uniqueMetaInfo.incrementCount();
        }
        return uniqueMetaInfo;
    }

    @Override
    @Transactional(readOnly = true)
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
