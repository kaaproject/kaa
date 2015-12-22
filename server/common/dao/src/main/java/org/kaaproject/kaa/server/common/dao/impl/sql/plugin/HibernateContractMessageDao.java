package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ContractMessageDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.ContractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.FQN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateContractMessageDao extends HibernateAbstractDao<ContractMessage> implements ContractMessageDao<ContractMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateContractMessageDao.class);

    @Override
    public ContractMessage findByFqnAndVersion(String fqn, Integer version) {
        LOG.debug("Searching for a contract message by fqn and version [{}, {}]", fqn, version);
        ContractMessage contractMessage = null;
        if (StringUtils.isNotBlank(fqn) && (version != null)) {
            contractMessage = findOneByCriterion(Restrictions.and(
                    Restrictions.eq(FQN_PROPERTY, fqn),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        LOG.debug("Found contract message: {}", contractMessage);
        return contractMessage;
    }

    @Override
    protected Class<ContractMessage> getEntityClass() {
        return ContractMessage.class;
    }
}
