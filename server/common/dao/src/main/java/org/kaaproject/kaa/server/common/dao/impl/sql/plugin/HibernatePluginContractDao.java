package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.kaaproject.kaa.server.common.dao.impl.PluginContractDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginContract;
import org.springframework.stereotype.Repository;

@Repository
public class HibernatePluginContractDao extends HibernateAbstractDao<PluginContract> implements PluginContractDao<PluginContract> {

    @Override
    protected Class<PluginContract> getEntityClass() {
        return PluginContract.class;
    }
}
