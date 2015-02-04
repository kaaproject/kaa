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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.DBCollection;

public abstract class AbstractMongoDao<T, K> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMongoDao.class);

    public static final String TENANT_ID = "tenant_id";
    public static final String SEQUENCE_NUMBER = "seq_num";
    public static final String SCHEMA_ID = "schema_id";
    public static final String APPLICATION_ID = "application_id";
    public static final String ENDPOINT_GROUP_ID = "endpoint_group_id";
    public static final String MAJOR_VERSION = "major_version";
    public static final String MINOR_VERSION = "minor_version";
    public static final String STATUS = "status";
    public static final String ACTIVATED_TIME = "activated_time";
    public static final String ACTIVATED_USERNAME = "activated_username";
    public static final String DEACTIVATED_TIME = "deactivated_time";
    public static final String DEACTIVATED_USERNAME = "deactivated_username";
    public static final String NAME = "name";
    public static final String EXTERNAL_UID = "external_uid";
    public static final String EXTERNAL_ID = "external_id";
    public static final String AUTHORITY = "authority";
    public static final String NOTIFICATION_SCHEMA_ID = "notification_schema_id";
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String UPDATE_PROCESSING_STATUS = "upd.status";
    public static final String UPDATE_SEQUENCE_NUMBER = "upd.seq_num";
    public static final String ECF_ID = "ecf_id";
    public static final String FQN = "fqn";
    public static final String CLASS_NAME = "class_name";
    public static final String VERSION = "version";
    public static final String TYPE = "type";

    protected static final String ID = "_id";
    protected static final int ONE = 1;

    protected FindAndModifyOptions returnNew = new FindAndModifyOptions().returnNew(true);

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected abstract String getCollectionName();

    protected abstract Class<T> getDocumentClass();

    protected DBCollection getPrimaryCollection() {
        return mongoTemplate.getCollection(getCollectionName());
    }

    protected List<T> find(Query query) {
        return mongoTemplate.find(query, getDocumentClass());
    }

    protected T findOne(Query query) {
        return mongoTemplate.findOne(query, getDocumentClass());
    }

    protected T findAndModify(Query query, Update update, FindAndModifyOptions options) {
        return mongoTemplate.findAndModify(query, update, options, getDocumentClass());
    }

    protected void updateFirst(Query query, Update update) {
        mongoTemplate.updateFirst(query, update, getDocumentClass());
    }

    protected void updateMulti(Query query, Update update) {
        mongoTemplate.updateMulti(query, update, getDocumentClass());
    }

    protected void remove(Query query) {
        mongoTemplate.remove(query, getDocumentClass());
    }

    protected long count(Query query) {
        return mongoTemplate.count(query, getDocumentClass());
    }

    public T save(T dto) {
        mongoTemplate.save(dto);
        return dto;
    }

    public <V> V save(V dto, Class<?> clazz) {
        LOG.debug("Save entity of {} class", clazz.getName());
        mongoTemplate.save(dto);
        return dto;
    }

    public List<T> find() {
        LOG.debug("Find  all documents from [{}] collection.", getCollectionName());
        return mongoTemplate.findAll(getDocumentClass());
    }

    public T findById(K key) {
        LOG.debug("Find document of collection [{}] by id [{}]", getCollectionName(), key);
        return mongoTemplate.findById(key, getDocumentClass());
    }

    public void removeAll() {
        LOG.debug("Remove all documents from [{}] collection.", getCollectionName());
        mongoTemplate.dropCollection(getDocumentClass());
    }

    public void removeById(K key) {
        LOG.debug("Remove document of collection [{}] by id [{}]", getCollectionName(), key);
        T object = findById(key);
        if (object != null) {
            mongoTemplate.remove(object);
        }
    }
}
