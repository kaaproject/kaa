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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPBySdkToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
@Repository
public class CassandraEPBySdkTokenDao extends AbstractCassandraDao<CassandraEPBySdkToken, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraEPBySdkTokenDao.class);

    @Override
    protected Class<CassandraEPBySdkToken> getColumnFamilyClass() {
        return CassandraEPBySdkToken.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME;
    }

    /**
     * @deprecated This method needs additional testing and thus is not
     *             recommended to use as of October, 2015.
     */
    public ByteBuffer[] getEPIdsBySdkToken(String sdkToken) {
        LOG.debug("Trying to find a list of enpoint key hash values by SDK token {}", sdkToken);

        Statement query = QueryBuilder
                .select()
                .from(this.getColumnFamilyName())
                .where(QueryBuilder.eq(EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY, sdkToken));

        List<CassandraEPBySdkToken> queryResult = this.findListByStatement(query);
        ByteBuffer[] result = new ByteBuffer[queryResult.size()];

        int i = 0;
        for (CassandraEPBySdkToken endpointProfile : queryResult) {
            result[i++] = endpointProfile.getEndpointKeyHash();
        }

        LOG.debug("Endpoint profile hash values found: {}", Arrays.toString(result));

        return result;
    }
}
