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

import org.kaaproject.kaa.server.common.dao.TenantDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class TenantMongoDao extends AbstractMongoDao<Tenant> implements TenantDao<Tenant> {

    private static final Logger LOG = LoggerFactory.getLogger(TenantMongoDao.class);

    @Override
    protected String getCollectionName() {
        return Tenant.COLLECTION_NAME;
    }

    @Override
    protected Class<Tenant> getDocumentClass() {
        return Tenant.class;
    }

    @Override
    public Tenant findByName(String tenantName) {
        LOG.debug("Find tenant by tenant name [{}] ", tenantName);
        return findOne(query(where(NAME).is(tenantName)));
    }

    @Override
    public void removeByName(String tenantName) {
        LOG.debug("Remove tenant by tenant name [{}] ", tenantName);
        remove(query(where(NAME).is(tenantName)));
    }
}
