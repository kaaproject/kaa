package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_APP_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_USER_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class EndpointUserConfigurationMongoDao extends AbstractMongoDao<MongoEndpointUserConfiguration, String> implements EndpointUserConfigurationDao<MongoEndpointUserConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserMongoDao.class);

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
        LOG.debug("Save user specific configuration {}", dto);
        return save(new MongoEndpointUserConfiguration(dto));
    }

    @Override
    public MongoEndpointUserConfiguration findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        LOG.debug("Find user specific configuration by user id {}, application toke {} schema version {}", userId, appToken, schemaVersion);
        return findOne(query(where(USER_CONF_USER_ID).is(userId).and(USER_CONF_APP_TOKEN).is(appToken).and(USER_CONF_SCHEMA_VERSION).is(schemaVersion)));
    }

    @Override
    public List<MongoEndpointUserConfiguration> findByUserId(String userId) {
        LOG.debug("Find user specific configurations by user id {}", userId);
        return find(query(where(USER_CONF_USER_ID).is(userId)));
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        LOG.debug("Remove user specific configuration by user id {}, application toke {} schema version {}", userId, appToken, schemaVersion);
        remove(query(where(USER_CONF_USER_ID).is(userId).and(USER_CONF_APP_TOKEN).is(appToken).and(USER_CONF_SCHEMA_VERSION).is(schemaVersion)));
    }
}
