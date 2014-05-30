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
import org.kaaproject.kaa.server.common.dao.HistoryDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class HistoryMongoDao extends AbstractMongoDao<History> implements HistoryDao<History> {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryMongoDao.class);

    @Override
    protected String getCollectionName() {
        return History.COLLECTION_NAME;
    }

    @Override
    protected Class<History> getDocumentClass() {
        return History.class;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Remove history by id [{}]", id);
        remove(query(where(ID).is(id)));
    }

    @Override
    public List<History> findByAppId(String appId) {
        LOG.debug("Find history by application id [{}]", appId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId))));
    }

    @Override
    public History findBySeqNumber(String appId, int seqNum) {
        LOG.debug("Find history by application id [{}] and sequence number [{}]", appId, seqNum);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(SEQUENCE_NUMBER).is(seqNum)));
    }

    @Override
    public List<History> findBySeqNumberStart(String appId, int startSeqNum) {
        LOG.debug("Find history range by application id [{}] and start sequence number [{}]", appId, startSeqNum);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(SEQUENCE_NUMBER).gt(startSeqNum)));
    }

    @Override
    public List<History> findBySeqNumberRange(String appId, int startSeqNum, int endSeqNum) {
        LOG.debug("Find history range by application id [{}] and start sequence number [{}] end sequence number [{}] ",
                appId, startSeqNum, endSeqNum);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(SEQUENCE_NUMBER).gt(startSeqNum).lte(endSeqNum)));
    }

}
