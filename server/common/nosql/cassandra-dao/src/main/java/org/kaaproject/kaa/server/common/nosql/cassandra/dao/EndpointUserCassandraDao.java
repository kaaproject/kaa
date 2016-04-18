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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_EXTERNAL_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_TENANT_ID_PROPERTY;

import java.util.UUID;

import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.core.querybuilder.Update;

public class EndpointUserCassandraDao extends AbstractVersionableCassandraDao<CassandraEndpointUser, String> implements EndpointUserDao<CassandraEndpointUser> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserCassandraDao.class);
    
    @Override
    protected Class<CassandraEndpointUser> getColumnFamilyClass() {
        return CassandraEndpointUser.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_USER_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointUser save(CassandraEndpointUser user) {
        if (isBlank(user.getId())) {
            user.generateId();
        } 
        LOG.trace("Save endpoint user {}", user);
        return super.save(user);
    }
    

    @Override
    public CassandraEndpointUser save(EndpointUserDto dto) {
        return save(new CassandraEndpointUser(dto));
    }

    @Override
    public CassandraEndpointUser findByExternalIdAndTenantId(String externalId, String tenantId) {
        LOG.debug("Try to find endpoint user by external id {} and tenant id {}", externalId, tenantId);
        Where where = select().from(getColumnFamilyName()).where(eq(EP_USER_EXTERNAL_ID_PROPERTY, externalId)).and(eq(EP_USER_TENANT_ID_PROPERTY, tenantId));
        LOG.trace("Try to find endpoint user by cql select {}", where);
        CassandraEndpointUser endpointUser = findOneByStatement(where);
        LOG.trace("Found {} endpoint user", endpointUser);
        return endpointUser;
    }

    @Override
    public void removeByExternalIdAndTenantId(String externalId, String tenantId) {
        LOG.debug("Try to remove endpoint user by external id {} and tenant id {}", externalId, tenantId);
        execute(delete().from(getColumnFamilyName()).where(eq(EP_USER_EXTERNAL_ID_PROPERTY, externalId)).and(eq(EP_USER_TENANT_ID_PROPERTY, tenantId)));
    }

    @Override
    public String generateAccessToken(String externalId, String tenantId) {
        LOG.debug("Generating access token for endpoint user with external id {} and tenant id {}", externalId, tenantId);
        String accessToken = UUID.randomUUID().toString();
        Update.Where query = update(getColumnFamilyName()).with(set(CassandraModelConstants.EP_USER_ACCESS_TOKEN_PROPERTY, accessToken))
                .where(eq(EP_USER_EXTERNAL_ID_PROPERTY, externalId))
                .and(eq(EP_USER_TENANT_ID_PROPERTY, tenantId));
        execute(query);
        LOG.trace("Generated access token {} for endpoint user by query {}", accessToken, query);
        return accessToken;
    }

    @Override
    public boolean checkAccessToken(String externalId, String tenantId, String accessToken) {
        LOG.debug("Check access token [{}] for endpoint user with external id {} and tenant id {}", accessToken, externalId, tenantId);
        boolean result = false;
        CassandraEndpointUser endpointUser = findByExternalIdAndTenantId(externalId, tenantId);
        if (endpointUser != null && accessToken != null) {
            result = accessToken.equals(endpointUser.getAccessToken());
        }
        return result;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Try to remove endpoint user by id {}", id);
        CassandraEndpointUser endpointUser = new CassandraEndpointUser(id);
        removeByExternalIdAndTenantId(endpointUser.getExternalId(), endpointUser.getTenantId());
    }

    @Override
    public CassandraEndpointUser findById(String id) {
        LOG.debug("Try to find endpoint user by id {}", id);
        CassandraEndpointUser endpointUser = new CassandraEndpointUser(id);
        endpointUser = findByExternalIdAndTenantId(endpointUser.getExternalId(), endpointUser.getTenantId());
        LOG.trace("Found endpoint user {} by id {}", endpointUser, id);
        return endpointUser;
    }



}
