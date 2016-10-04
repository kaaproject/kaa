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

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEpBySdkToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bohdan Khablenko
 * @since v0.8.0
 */
@Repository
public class CassandraEpBySdkTokenDao extends AbstractCassandraDao<CassandraEpBySdkToken, String> {

  private static final Logger LOG = LoggerFactory.getLogger(CassandraEpBySdkTokenDao.class);

  @Override
  protected Class<CassandraEpBySdkToken> getColumnFamilyClass() {
    return CassandraEpBySdkToken.class;
  }

  @Override
  protected String getColumnFamilyName() {
    return EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME;
  }

  /**
   * @deprecated This method needs additional testing and thus isn't recommended to use as of
    October, 2015.
   */
  public ByteBuffer[] getEpIdsBySdkToken(String sdkToken) {
    LOG.debug("Trying to find a list of enpoint key hash values by SDK token {}", sdkToken);

    Statement query = QueryBuilder
        .select()
        .from(this.getColumnFamilyName())
        .where(QueryBuilder.eq(EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY, sdkToken));

    List<CassandraEpBySdkToken> queryResult = this.findListByStatement(query);
    ByteBuffer[] result = new ByteBuffer[queryResult.size()];

    int pos = 0;
    for (CassandraEpBySdkToken endpointProfile : queryResult) {
      result[pos++] = endpointProfile.getEndpointKeyHash();
    }

    LOG.debug("Endpoint profile hash values found: {}", Arrays.toString(result));

    return result;
  }
}
