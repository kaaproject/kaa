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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAccessToken;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CassandraEPByAccessTokenDao extends AbstractCassandraDao<CassandraEPByAccessToken, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraEPByAccessTokenDao.class);
    @Override
    protected Class<CassandraEPByAccessToken> getColumnFamilyClass() {
        return CassandraEPByAccessToken.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME;
    }

    public ByteBuffer findEPIdByAccessToken(String accessToken) {
        LOG.debug("Try to find endpoint key hash by access token {}", accessToken);
        ByteBuffer endpointKeyHash = null;
        CassandraEPByAccessToken result = findById(accessToken);
        if (result != null) {
            endpointKeyHash = result.getEndpointKeyHash();
        }
        return endpointKeyHash;
    }
}
