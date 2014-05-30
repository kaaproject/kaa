/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.mongo;

import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.common.dto.UpdateStatus.DEPRECATED;
import static org.kaaproject.kaa.common.dto.UpdateStatus.INACTIVE;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationMongoDao extends AbstractMongoDao<Configuration> implements ConfigurationDao<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationMongoDao.class);

    @Override
    protected String getCollectionName() {
        return Configuration.COLLECTION_NAME;
    }

    @Override
    protected Class<Configuration> getDocumentClass() {
        return Configuration.class;
    }

    @Override
    public Configuration findConfigurationByAppIdAndVersion(String applicationId, int version) {
        LOG.debug("Find configuration by application id [{}] and version [{}]", applicationId, version);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId)).and(MAJOR_VERSION).is(version)
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public Configuration findConfigurationByEndpointGroupIdAndVersion(String endpointGroupId, int version) {
        LOG.debug("Find configuration by endpoint group id [{}] and version [{}]", endpointGroupId, version);
        return findOne(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId)).and(MAJOR_VERSION).is(version)
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public List<Configuration> findActiveByApplicationId(String applicationId) {
        LOG.debug("Find active configurations by application id [{}] ", applicationId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId))
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public List<Configuration> findAllByApplicationId(String applicationId) {
        LOG.debug("Find all configurations by application id [{}] ", applicationId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId))));
    }

    @Override
    public Configuration findLatestActiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        LOG.debug("Find latest active configuration by configuration schema id [{}] and group id [{}] ", schemaId, groupId);
        Sort sortObject = new Sort(Direction.DESC, SEQUENCE_NUMBER);
        Configuration config = findOne(
                query(where(SCHEMA_ID).is(new ObjectId(schemaId))
                        .and(ENDPOINT_GROUP_ID).is(new ObjectId(groupId))
                        .and(STATUS).is(ACTIVE.name())).with(sortObject)
        );
        return config;
    }

    @Override
    public Configuration findInactiveByConfigurationSchemaId(String configurationSchemaId) {
        LOG.debug("Find inactive configuration by configuration schema id [{}] ", configurationSchemaId);
        return findOne(query(where(SCHEMA_ID).is(new ObjectId(configurationSchemaId))
                .and(STATUS).is(INACTIVE.name())));
    }

    @Override
    public Configuration findInactiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        LOG.debug("Find inactive configuration by configuration schema id [{}] and endpoint group id [{}] ", schemaId, groupId);
        return findOne(query(where(SCHEMA_ID).is(new ObjectId(schemaId))
                .and(ENDPOINT_GROUP_ID).is(new ObjectId(groupId))
                .and(STATUS).is(INACTIVE.name())));
    }

    @Override
    public List<Configuration> findAllByConfigurationSchemaId(String configurationSchemaId) {
        LOG.debug("Find all configuration  by configuration schema id [{}] ", configurationSchemaId);
        return find(query(where(SCHEMA_ID).is(new ObjectId(configurationSchemaId))));
    }

    @Override
    public List<Configuration> findActiveByConfigurationSchemaId(String configurationSchemaId) {
        LOG.debug("Find active configuration  by configuration schema id [{}] ", configurationSchemaId);
        return find(query(where(SCHEMA_ID).is(new ObjectId(configurationSchemaId))
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public List<Configuration> findAllByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Find all configurations by endpoint group id [{}] ", endpointGroupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId))));
    }

    @Override
    public List<Configuration> findActiveByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Find configurations by endpoint group id [{}] ", endpointGroupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId))
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public List<Configuration> findActualByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Find actual configurations by endpoint group id [{}] ", endpointGroupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId)).
                and(STATUS).ne(DEPRECATED.name())));
    }

    @Override
    public List<Configuration> findActualBySchemaIdAndGroupId(String schemaId, String groupId) {
        LOG.debug("Find actual configurations by schema id and group id [{}] ", schemaId, groupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(groupId)).
                and(SCHEMA_ID).is(new ObjectId(schemaId)).
                and(STATUS).ne(DEPRECATED.name())));
    }

    @Override
    public Configuration findLatestDeprecated(String schemaId, String groupId) {
        LOG.debug("Find latest deprecated configuration by schema id and group id [{}] ", schemaId, groupId);
        Sort sortObject = new Sort(Direction.DESC, SEQUENCE_NUMBER);
        return findOne(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(groupId)).
                and(SCHEMA_ID).is(new ObjectId(schemaId)).
                and(STATUS).is(DEPRECATED.name())).with(sortObject));
    }

    @Override
    public void removeByConfigurationSchemaId(String configurationSchemaId) {
        LOG.debug("Remove configurations  by configuration schema id [{}] ", configurationSchemaId);
        remove(query(where(SCHEMA_ID).is(new ObjectId(configurationSchemaId))));
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        LOG.debug("Remove configurations by application id [{}] ", applicationId);
        remove(query(where(APPLICATION_ID).is(new ObjectId(applicationId))));
    }

    @Override
    public void removeByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Remove configurations by endpoint group id [{}] ", endpointGroupId);
        remove(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId))));
    }

    @Override
    public Configuration activate(String id, String activatedUsername) {
        LOG.debug("Activate configuration and increment seq_num, found by id [{}] ", id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE).
                set(STATUS, UpdateStatus.ACTIVE.name()).
                set(ACTIVATED_TIME, System.currentTimeMillis())
                .set(ACTIVATED_USERNAME, activatedUsername);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, options);
    }

    @Override
    public Configuration deactivate(String id, String deactivatedUsername) {
        LOG.debug("Deactivate configuration, found by id [{}] ", id);
        Update update = new Update().
                set(STATUS, UpdateStatus.DEPRECATED.name()).
                set(DEACTIVATED_TIME, System.currentTimeMillis())
                .set(DEACTIVATED_USERNAME, deactivatedUsername);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, options);
    }

    @Override
    public Configuration deactivateOldConfiguration(String schemaId, String groupId, String deactivatedUsername) {
        LOG.debug("Deactivate old configuration, by configuration schema id [{}] and endpoint group id [{}] ", schemaId, groupId);
        Update update = new Update().set(STATUS, UpdateStatus.DEPRECATED.name())
                .set(DEACTIVATED_TIME, System.currentTimeMillis())
                .set(DEACTIVATED_USERNAME, deactivatedUsername);
        return findAndModify(query(where(SCHEMA_ID).is(new ObjectId(schemaId))
                .and(ENDPOINT_GROUP_ID).is(new ObjectId(groupId))
                .and(STATUS).is(ACTIVE.name())), update, returnNew);
    }

}
