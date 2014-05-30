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

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.server.common.dao.UserDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class UserMongoDao extends AbstractMongoDao<User> implements UserDao<User> {

    private static final Logger LOG = LoggerFactory.getLogger(UserMongoDao.class);

    @Override
    protected String getCollectionName() {
        return User.COLLECTION_NAME;
    }

    @Override
    protected Class<User> getDocumentClass() {
        return User.class;
    }

    @Override
    public User findByExternalUid(String externalUid) {
        LOG.debug("Find user by external uid [{}] ", externalUid);
        return findOne(query(where(EXTERNAL_UID).is(externalUid)));
    }

    @Override
    public List<User> findByTenantId(String tenantId) {
        LOG.debug("Find user by tenant id [{}] ", tenantId);
        return find(query(where(TENANT_ID).is(new ObjectId(tenantId))));
    }

    @Override
    public List<User> findByTenantIdAndAuthority(String tenantId, String authority) {
        LOG.debug("Find user by tenant id [{}] and authority [{}]", tenantId, authority);
        return find(query(where(TENANT_ID).is(new ObjectId(tenantId)).and(AUTHORITY).is(authority)));
    }

    @Override
    public List<User> findByTenantIdAndAuthorities(String tenantId, String... authorities) {
        if (LOG.isDebugEnabled()) {
            String authoritiesString = "";
            for (int i = 0; i < authorities.length; i++) {
                if (i > 0) {
                    authoritiesString += ", ";
                }
                authoritiesString += authorities[i];
            }
            LOG.debug("Find user by tenant id [{}] and authorities [{}]", tenantId, authoritiesString);
        }
        return find(query(where(TENANT_ID).is(new ObjectId(tenantId)).and(AUTHORITY).in(Arrays.asList(authorities))));
    }

    @Override
    public void removeByTenantId(String tenantId) {
        LOG.debug("remove user by tenant id [{}] ", tenantId);
        remove(query(where(TENANT_ID).is(new ObjectId(tenantId))));
    }

    @Override
    public void removeByExternalUid(String externalUid) {
        LOG.debug("Remove user by external uid [{}] ", externalUid);
        remove(query(where(EXTERNAL_UID).is(externalUid)));
    }

}
