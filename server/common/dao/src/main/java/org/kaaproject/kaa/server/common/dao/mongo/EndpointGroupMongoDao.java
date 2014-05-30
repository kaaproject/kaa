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

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.server.common.dao.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class EndpointGroupMongoDao extends AbstractMongoDao<EndpointGroup> implements EndpointGroupDao<EndpointGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointGroupMongoDao.class);

    private static final String TOPICS = "topics";
    private static final String WEIGHT = "weight";

    @Override
    protected String getCollectionName() {
        return EndpointGroup.COLLECTION_NAME;
    }

    @Override
    protected Class<EndpointGroup> getDocumentClass() {
        return EndpointGroup.class;
    }

    @Override
    public List<EndpointGroup> findByApplicationId(String applicationId) {
        LOG.debug("Find endpoint groups by application id [{}] ", applicationId);
        Sort sortObject = new Sort(Direction.ASC, WEIGHT);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId))).with(sortObject));
    }

    @Override
    public EndpointGroup findByAppIdAndWeight(String applicationId, int weight) {
        LOG.debug("Find endpoint group by application id [{}] and weight [{}]", applicationId, weight);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId)).and(WEIGHT).is(weight)));
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        LOG.debug("Remove endpoint groups by application id [{}] ", applicationId);
        remove(query(where(APPLICATION_ID).is(new ObjectId(applicationId))));
    }

    @Override
    public EndpointGroup removeTopicFromEndpointGroup(String id, String topicId) {
        LOG.debug("Remove topic [{}] from endpoint group with id [{}] ", topicId, id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE).pull(TOPICS, topicId);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }

    @Override
    public List<EndpointGroup> findEndpointGroupsByTopicIdAndAppId(String appId, String topicId) {
        LOG.debug("Find endpoint groups by topic id [{}] and application id [{}] ", topicId, appId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(TOPICS).is(topicId)));
    }

    @Override
    public EndpointGroup addTopicToEndpointGroup(String id, String topicId) {
        LOG.debug("Add topic [{}] to endpoint group with id [{}] ", topicId, id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE).push(TOPICS, topicId);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }

    @Override
    public EndpointGroup updateEndpointGroupWeight(String id, Integer weight) {
        LOG.debug("Update endpoint group weight to [{}], endpoint group id [{}] ", weight, id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE).set(WEIGHT, weight);
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }
}
