/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.dao.DaoConstants;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoDaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Repository
public class EndpointProfileMongoDao extends AbstractVersionableMongoDao<MongoEndpointProfile, ByteBuffer> implements EndpointProfileDao<MongoEndpointProfile> {

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
        List<MongoEndpointProfile> mongoEndpointProfileList = find(query(new Criteria().orOperator(where(EP_GROUP_STATE + "." + ENDPOINT_GROUP_ID)
                .is(pageLink.getEndpointGroupId()), where(EP_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId())))
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
        Query query = Query.query(new Criteria().orOperator(where(EP_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId()),
                where(EP_GROUP_STATE + "." + ENDPOINT_GROUP_ID).is(pageLink.getEndpointGroupId())));
        query.skip(offs).limit(lim + 1);
        query.fields().include(DaoConstants.PROFILE).include(EP_SERVER_PROFILE_PROPERTY).include(EP_ENDPOINT_KEY_HASH).include(EP_APPLICATION_ID)
                .include(EP_PROFILE_VERSION).include(EP_SERVER_PROFILE_VERSION_PROPERTY).include(EP_USE_RAW_SCHEMA);
        List<EndpointProfileDto> endpointProfileDtoList = convertDtoList(mongoTemplate.find(query, getDocumentClass()));
        if (endpointProfileDtoList.size() == (lim + 1)) {
            String offset = Integer.toString(lim + offs);
            pageLink.setOffset(offset);
            endpointProfileDtoList.remove(lim);
        } else {
            pageLink.setNext(DaoConstants.LAST_PAGE_MESSAGE);
        }
        for (EndpointProfileDto ep : endpointProfileDtoList) {
            EndpointProfileBodyDto endpointProfileBodyDto = new EndpointProfileBodyDto(ep.getEndpointKeyHash(), ep.getClientProfileBody(),
                    ep.getServerProfileBody(),ep.getClientProfileVersion(), ep.getServerProfileVersion(), ep.getApplicationId());
            endpointProfileBodyDto.setEndpointKeyHash(ep.getEndpointKeyHash());
            profilesBody.add(endpointProfileBodyDto);
        }
        endpointProfilesBodyDto.setPageLinkDto(pageLink);
        endpointProfilesBodyDto.setEndpointProfilesBody(profilesBody);
        return endpointProfilesBodyDto;
    }

    private Long findVersionByKey(byte[] endpointKeyHash) {
        LOG.debug("Find endpoint profile version by key hash [{}] ", endpointKeyHash);
        Long version = null;
        Query query = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash));
        query.fields().include(OPT_LOCK);
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(query.getQueryObject());
        if (result != null) {
            version = (Long) result.get(OPT_LOCK);
        }
        return version;
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
        query.fields().include(DaoConstants.PROFILE).include(EP_SERVER_PROFILE_PROPERTY).include(EP_APPLICATION_ID)
                .include(EP_PROFILE_VERSION).include(EP_SERVER_PROFILE_VERSION_PROPERTY).include(EP_USE_RAW_SCHEMA);
        EndpointProfileDto pf = mongoTemplate.findOne(query, getDocumentClass()).toDto();
        if (pf != null) {
            endpointProfileBodyDto = new EndpointProfileBodyDto(endpointKeyHash, pf.getClientProfileBody(), pf.getServerProfileBody(),
                    pf.getClientProfileVersion(), pf.getServerProfileVersion(), pf.getApplicationId());
        }
        LOG.debug("[{}] Found client-side endpoint profile body {} with client-side endpoint profile version {} and server-side endpoint profile body {} " +
                "with server-side endpoint profile version {} and application id {}", endpointKeyHash, pf.getClientProfileBody(), pf.getServerProfileBody(),
                pf.getClientProfileVersion(), pf.getServerProfileVersion(), pf.getApplicationId());
        return endpointProfileBodyDto;
    }

    @Override
    public MongoEndpointProfile findEndpointIdByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Get count of endpoint profiles by endpoint key hash [{}] ", endpointKeyHash);
        Query query = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash));
        query.fields().include(ID);
        return findOne(query);
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
    public boolean checkSdkToken(String sdkToken) {
        LOG.debug("Checking for endpoint profiles with SDK token {}", sdkToken);
        return findOne(query(where(EP_SDK_TOKEN).is(sdkToken))) != null;
    }

    @Override
    public MongoEndpointProfile updateServerProfile(byte[] keyHash, int version, String serverProfile) {
        LOG.debug("Update server endpoint profile for endpoint with key hash {}, schema version is {}", keyHash, version);
        updateFirst(
                query(where(EP_ENDPOINT_KEY_HASH).is(keyHash)),
                update(EP_SERVER_PROFILE_PROPERTY, MongoDaoUtil.encodeReservedCharacteres((DBObject) JSON.parse(serverProfile)))
                .set(EP_SERVER_PROFILE_VERSION_PROPERTY, version));
        return findById(ByteBuffer.wrap(keyHash));
    }
}
