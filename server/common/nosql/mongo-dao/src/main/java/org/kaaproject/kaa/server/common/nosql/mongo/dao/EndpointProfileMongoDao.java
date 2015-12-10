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

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_GROUP_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_PROFILE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ACCESS_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CF_GROUP_STATE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ENDPOINT_KEY_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NF_GROUP_STATE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SDK_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SERVER_PROFILE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SERVER_PROFILE_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.dao.DaoConstants;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.DBObject;

@Repository
public class EndpointProfileMongoDao extends AbstractMongoDao<MongoEndpointProfile, ByteBuffer> implements EndpointProfileDao<MongoEndpointProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileMongoDao.class);

    @Override
    protected String getCollectionName() {
        return ENDPOINT_PROFILE;
    }

    @Override
    protected Class<MongoEndpointProfile> getDocumentClass() {
        return MongoEndpointProfile.class;
    }

    @Override
    public EndpointProfilesPageDto findByEndpointGroupId(PageLinkDto pageLink) {
        LOG.debug("Find endpoint profiles by endpoint group id [{}] ", pageLink.getEndpointGroupId());
        EndpointProfilesPageDto endpointProfilesPageDto = new EndpointProfilesPageDto();
        int lim = Integer.valueOf(pageLink.getLimit());
        int offs = Integer.valueOf(pageLink.getOffset());
        List<MongoEndpointProfile> mongoEndpointProfileList = find(query(new Criteria().orOperator(where(EP_CF_GROUP_STATE + "." + ENDPOINT_GROUP_ID)
                .is(pageLink.getEndpointGroupId()), where(EP_NF_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId())))
                .skip(offs)
                .limit(lim + 1));
        if (mongoEndpointProfileList.size() == (lim + 1)) {
            String offset = Integer.toString(lim + offs);
            pageLink.setOffset(offset);
            mongoEndpointProfileList.remove(lim);
        } else {
            pageLink.setNext(DaoConstants.LAST_PAGE_MESSAGE);
        }
        endpointProfilesPageDto.setPageLinkDto(pageLink);
        endpointProfilesPageDto.setEndpointProfiles(convertDtoList(mongoEndpointProfileList));
        return endpointProfilesPageDto;
    }

    @Override
    public EndpointProfilesBodyDto findBodyByEndpointGroupId(PageLinkDto pageLink) {
        LOG.debug("Find endpoint profiles body by endpoint group id [{}] ", pageLink.getEndpointGroupId());
        EndpointProfilesBodyDto endpointProfilesBodyDto = new EndpointProfilesBodyDto();
        List<EndpointProfileBodyDto> profilesBody = new ArrayList<>();
        int lim = Integer.valueOf(pageLink.getLimit());
        int offs = Integer.valueOf(pageLink.getOffset());
        Query query = Query.query(new Criteria().orOperator(where(EP_CF_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId()),
                where(EP_NF_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId())));
        query.skip(offs).limit(lim + 1);
        query.fields().include(DaoConstants.PROFILE).include(EP_ENDPOINT_KEY_HASH).include(EP_APPLICATION_ID);
        List<MongoEndpointProfile> mongoEndpointProfileList = mongoTemplate.find(query, getDocumentClass());
        if (mongoEndpointProfileList.size() == (lim + 1)) {
            String offset = Integer.toString(lim + offs);
            pageLink.setOffset(offset);
            mongoEndpointProfileList.remove(lim);
        } else {
            pageLink.setNext(DaoConstants.LAST_PAGE_MESSAGE);
        }
        for (MongoEndpointProfile ep : mongoEndpointProfileList) {
            EndpointProfileBodyDto endpointProfileBodyDto = new EndpointProfileBodyDto(ep.getEndpointKeyHash(), ep.getProfileAsString(), ep.getApplicationId());
            endpointProfileBodyDto.setEndpointKeyHash(ep.getEndpointKeyHash());
            profilesBody.add(endpointProfileBodyDto);
        }
        endpointProfilesBodyDto.setPageLinkDto(pageLink);
        endpointProfilesBodyDto.setEndpointProfilesBody(profilesBody);
        return endpointProfilesBodyDto;
    }

    @Override
    public MongoEndpointProfile findByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Find endpoint profile by endpoint key hash [{}] ", endpointKeyHash);
        DBObject dbObject = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public EndpointProfileBodyDto findBodyByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Find endpoint profile body by endpoint key hash [{}] ", endpointKeyHash);
        EndpointProfileBodyDto endpointProfileBodyDto = null;
        Query query = Query.query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash));
        query.fields().include(DaoConstants.PROFILE).include(EP_APPLICATION_ID);
        MongoEndpointProfile pf = mongoTemplate.findOne(query, getDocumentClass());
        if (pf != null) {
            endpointProfileBodyDto = new EndpointProfileBodyDto(endpointKeyHash, pf.getProfileAsString(), pf.getApplicationId());
        }
        return endpointProfileBodyDto;
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Get count of endpoint profiles by endpoint key hash [{}] ", endpointKeyHash);
        DBObject dbObject = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)).getQueryObject();
        return mongoTemplate.getDb().getCollection(getCollectionName()).count(dbObject);
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Remove endpoint profile by endpoint key hash [{}] ", endpointKeyHash);
        mongoTemplate.remove(query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)), getCollectionName());
    }

    @Override
    public void removeByAppId(String appId) {
        LOG.debug("Remove endpoint profile by application id [{}] ", appId);
        remove(query(where(EP_APPLICATION_ID).is(appId)));
    }

    @Override
    public MongoEndpointProfile findByAccessToken(String endpointAccessToken) {
        LOG.debug("Find endpoint profile by access token [{}] ", endpointAccessToken);
        DBObject dbObject = query(where(EP_ACCESS_TOKEN).is(endpointAccessToken)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public List<MongoEndpointProfile> findByEndpointUserId(String endpointUserId) {
        LOG.debug("Find endpoint profiles by endpoint user id [{}] ", endpointUserId);
        return find(query(where(EP_USER_ID).is(endpointUserId)));
    }

    @Override
    public MongoEndpointProfile findById(ByteBuffer key) {
        MongoEndpointProfile profile = null;
        if (key != null) {
            profile = findByKeyHash(key.array());
        }
        return profile;
    }

    @Override
    public void removeById(ByteBuffer key) {
        if (key != null) {
            removeByKeyHash(key.array());
        }
    }

    @Override
    public MongoEndpointProfile save(EndpointProfileDto dto) {
        return save(new MongoEndpointProfile(dto));
    }

    @Override
    public List<MongoEndpointProfile> findBySdkToken(String sdkToken) {
        LOG.debug("Searching for endpoint profiles by SDK token {} ", sdkToken);
        return find(query(where(EP_SDK_TOKEN).is(sdkToken)));
    }

    @Override
    public boolean checkSdkToken(String sdkToken) {
        LOG.debug("Checking for endpoint profiles with SDK token {}", sdkToken);
        return findOne(query(where(EP_SDK_TOKEN).is(sdkToken))) != null;
    }

    @Override
    public MongoEndpointProfile updateServerProfile(byte[] keyHash, int version, String serverProfile) {
        LOG.debug("Update server endpoint profile for endpoint with key hash {}, schema version is {}", keyHash, version);
        updateFirst(
                query(where(EP_ENDPOINT_KEY_HASH).is(keyHash)),
                update(EP_SERVER_PROFILE_PROPERTY, serverProfile)
                .set(EP_SERVER_PROFILE_VERSION_PROPERTY, version));
        return findById(ByteBuffer.wrap(keyHash));
    }
}
