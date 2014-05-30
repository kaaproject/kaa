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

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.common.dto.UpdateStatus.DEPRECATED;
import static org.kaaproject.kaa.common.dto.UpdateStatus.INACTIVE;
import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class ProfileFilterMongoDao extends AbstractMongoDao<ProfileFilter> implements ProfileFilterDao<ProfileFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileFilterMongoDao.class);

    @Override
    protected String getCollectionName() {
        return ProfileFilter.COLLECTION_NAME;
    }

    @Override
    protected Class<ProfileFilter> getDocumentClass() {
        return ProfileFilter.class;
    }

    @Override
    public List<ProfileFilter> findActiveByProfileSchemaId(String profileSchemaId) {
        LOG.debug("Find profile filter by profile schema id [{}] ", profileSchemaId);
        return find(query(where(SCHEMA_ID).is(new ObjectId(profileSchemaId))
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public List<ProfileFilter> findAllByProfileSchemaId(String profileSchemaId) {
        LOG.debug("Find profile filter by profile schema id [{}] ", profileSchemaId);
        return find(query(where(SCHEMA_ID).is(new ObjectId(profileSchemaId))));
    }

    @Override
    public ProfileFilter findActiveByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Find profile filter by endpoint group id [{}] ", endpointGroupId);
        Sort sortObject = new Sort(Direction.DESC, SEQUENCE_NUMBER);
        return findOne(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId))
                .and(STATUS).is(ACTIVE.name())).with(sortObject));
    }

    @Override
    public List<ProfileFilter> findActualByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Find actual profile filters by endpoint group id [{}] ", endpointGroupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId)).
                and(STATUS).ne(DEPRECATED.name())));
    }

    @Override
    public List<ProfileFilter> findActualBySchemaIdAndGroupId(String schemaId, String groupId) {
        LOG.debug("Find actual profile filters by schema id and group id [{}] ", schemaId, groupId);
        return find(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(groupId)).
                and(SCHEMA_ID).is(new ObjectId(schemaId)).
                and(STATUS).ne(DEPRECATED.name())));
    }

    @Override
    public ProfileFilter findLatestDeprecated(String schemaId, String groupId) {
        LOG.debug("Find latest deprecated profile filter by schema id and group id [{}] ", schemaId, groupId);
        Sort sortObject = new Sort(Direction.DESC, SEQUENCE_NUMBER);
        return findOne(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(groupId)).
                and(SCHEMA_ID).is(new ObjectId(schemaId)).
                and(STATUS).is(DEPRECATED.name())).with(sortObject));
    }

    @Override
    public void removeByProfileSchemaId(String profileSchemaId) {
        LOG.debug("Remove profile filter by profile schema id [{}] ", profileSchemaId);
        remove(query(where(SCHEMA_ID).is(new ObjectId(profileSchemaId))));
    }

    @Override
    public void removeByEndpointGroupId(String endpointGroupId) {
        LOG.debug("Remove profile filter by endpoint group id [{}] ", endpointGroupId);
        remove(query(where(ENDPOINT_GROUP_ID).is(new ObjectId(endpointGroupId))));
    }

    @Override
    public List<ProfileFilter> findByAppIdAndSchemaVersion(String appId, int schemaVersion) {
        LOG.debug("Find profile filter by application id [{}] and major schema version [{}] ", appId, schemaVersion);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(MAJOR_VERSION).is(schemaVersion)
                .and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public ProfileFilter findInactiveFilter(String schemaId, String groupId) {
        LOG.debug("Find inactive profile filter by profile schema id [{}] and group id [{}]", schemaId, groupId);
        return findOne(query(where(SCHEMA_ID).is(new ObjectId(schemaId)).and(ENDPOINT_GROUP_ID)
                .is(new ObjectId(groupId)).and(STATUS).is(INACTIVE.name())));
    }

    @Override
    public ProfileFilter findLatestFilter(String schemaId, String groupId) {
        LOG.debug("Find latest active profile filter by profile schema id [{}] and group id [{}]", schemaId, groupId);
        Sort sortObject = new Sort(Direction.DESC, SEQUENCE_NUMBER);
        ProfileFilter config = findOne(query(where(SCHEMA_ID).is(new ObjectId(schemaId)).and(ENDPOINT_GROUP_ID)
                .is(new ObjectId(groupId)).and(STATUS).is(ACTIVE.name())).with(sortObject));
        return config;
    }

    @Override
    public long findActiveFilterCount(String schemaId, String groupId) {
        LOG.debug("Find latest active profile filter by profile schema id [{}] and group id [{}]", schemaId, groupId);
        return count(query(where(SCHEMA_ID).is(new ObjectId(schemaId)).and(ENDPOINT_GROUP_ID)
                .is(new ObjectId(groupId)).and(STATUS).is(ACTIVE.name())));
    }

    @Override
    public ProfileFilter activate(String id, String activatedUsername) {
        LOG.debug("Activate profile filter and increment seq_num, found by id [{}] ", id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE)
                .set(STATUS, UpdateStatus.ACTIVE.name())
                .set(ACTIVATED_TIME, System.currentTimeMillis())
                .set(ACTIVATED_USERNAME, activatedUsername);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, options);
    }

    @Override
    public ProfileFilter deactivate(String id, String deactivatedUsername) {
        LOG.debug("Deactivate profile filter, found by id [{}] ", id);
        Update update = new Update().
                set(STATUS, UpdateStatus.DEPRECATED.name()).
                set(DEACTIVATED_TIME, System.currentTimeMillis())
                .set(DEACTIVATED_USERNAME, deactivatedUsername);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, options);
    }

    @Override
    public ProfileFilter deactivateOldFilter(String schemaId, String groupId, String deactivatedUsername) {
        LOG.debug("Deactivate old profile filters, by profile schema id [{}] and group id [{}]", schemaId, groupId);
        Update update = new Update().set(STATUS, UpdateStatus.DEPRECATED.name())
            .set(DEACTIVATED_TIME, System.currentTimeMillis())
            .set(DEACTIVATED_USERNAME, deactivatedUsername);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return findAndModify(query(where(SCHEMA_ID).is(new ObjectId(schemaId))
                .and(ENDPOINT_GROUP_ID).is(new ObjectId(groupId))
                .and(STATUS).is(ACTIVE.name())), update, options);
    }

}
