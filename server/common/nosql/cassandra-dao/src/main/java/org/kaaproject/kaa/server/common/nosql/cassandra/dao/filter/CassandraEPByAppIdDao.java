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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY;

@Repository
public class CassandraEPByAppIdDao extends AbstractCassandraDao<CassandraEPByAppId, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraEPByAppIdDao.class);

    @Override
    protected Class<CassandraEPByAppId> getColumnFamilyClass() {
        return CassandraEPByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME;
    }

    public ByteBuffer[] getEPIdsListByAppId(String appId) {
        LOG.debug("Try to find endpoint key hash list by application id {}", appId);
        List<CassandraEPByAppId> filter = findListByStatement(select().from(getColumnFamilyName()).where(eq(EP_BY_APP_ID_APPLICATION_ID_PROPERTY, appId)));
        ByteBuffer[] result = new ByteBuffer[filter.size()];
        int i = 0;
        for (CassandraEPByAppId ep : filter) {
            result[i++] = ep.getEndpointKeyHash();
        }
        return result;
    }
}
