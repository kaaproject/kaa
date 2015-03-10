package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_APP_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_USER_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class EndpointUserConfigurationMongoDao extends AbstractMongoDao<MongoEndpointUserConfiguration, String> implements EndpointUserConfigurationDao<MongoEndpointUserConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserConfigurationMongoDao.class);

    @Override
    protected String getCollectionName() {
        return USER_CONFIGURATION;
    }

    @Override
    protected Class<MongoEndpointUserConfiguration> getDocumentClass() {
        return MongoEndpointUserConfiguration.class;
    }

    @Override
    public MongoEndpointUserConfiguration save(EndpointUserConfigurationDto dto) {
        LOG.debug("Saving user specific configuration {}", dto);
        MongoEndpointUserConfiguration userConfiguration = save(new MongoEndpointUserConfiguration(dto));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Saving result: {}", userConfiguration);
        } else {
            LOG.debug("Saving result: {}", userConfiguration != null);
        }
        return userConfiguration;
    }

    @Override
    public MongoEndpointUserConfiguration findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        LOG.debug("Searching for user specific configuration by user id {}, application token {} and schema version {}", userId, appToken, schemaVersion);
        MongoEndpointUserConfiguration userConfiguration = findOne(query(where(USER_CONF_USER_ID).is(userId).and(USER_CONF_APP_TOKEN).is(appToken).and(USER_CONF_SCHEMA_VERSION).is(schemaVersion)));
        if (LOG.isTraceEnabled()) {
            LOG.debug("[{},{},{}] Search result: {}.", userId, appToken, schemaVersion, userConfiguration);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", userId, appToken, schemaVersion, userConfiguration != null);
        }
        return userConfiguration;
    }

    @Override
    public List<MongoEndpointUserConfiguration> findByUserId(String userId) {
        LOG.debug("Searching for user specific configurations by user id {}", userId);
        List<MongoEndpointUserConfiguration> configurationList = find(query(where(USER_CONF_USER_ID).is(userId)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", userId, Arrays.toString(configurationList.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", userId, configurationList.size());
        }
        return configurationList;
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        remove(query(where(USER_CONF_USER_ID).is(userId).and(USER_CONF_APP_TOKEN).is(appToken).and(USER_CONF_SCHEMA_VERSION).is(schemaVersion)));
        LOG.debug("Removed user specific configuration by user id {}, application token {} and schema version {}", userId, appToken, schemaVersion);
    }
}
