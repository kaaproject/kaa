package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.PluginDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CLASS_NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernatePluginDao extends HibernateAbstractDao<Plugin> implements PluginDao<Plugin> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernatePluginDao.class);

    @Override
    public Plugin findByNameAndVersion(String name, Integer version) {
        LOG.debug("Searching plugin by name and version [{}, {}]", name, version);
        Plugin plugin = null;
        if (StringUtils.isNotBlank(name) && (version != null)) {
            plugin = findOneByCriterion(Restrictions.and(
                    Restrictions.eq(NAME_PROPERTY, name),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        LOG.debug("Found plugin: {}", plugin);
        return plugin;
    }

    @Override
    public Plugin findByClassName(String className) {
        LOG.debug("Searching plugin by class name [{}]", className);
        Plugin plugin = null;
        if (StringUtils.isNotBlank(className)) {
            plugin = findOneByCriterion(Restrictions.eq(CLASS_NAME_PROPERTY, className));
        }
        LOG.debug("Found plugin: {}", plugin);
        return plugin;
    }

    @Override
    protected Class<Plugin> getEntityClass() {
        return Plugin.class;
    }
}
