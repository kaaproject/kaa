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

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gte;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY;

import com.datastax.driver.core.Statement;

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEpByEndpointGroupId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.List;

@Repository
public class CassandraEpByEndpointGroupIdDao
    extends AbstractCassandraDao<CassandraEpByEndpointGroupId, String> {

  private static final Logger LOG =
      LoggerFactory.getLogger(CassandraEpByEndpointGroupIdDao.class);

  @Override
  protected Class<CassandraEpByEndpointGroupId> getColumnFamilyClass() {
    return CassandraEpByEndpointGroupId.class;
  }

  @Override
  protected String getColumnFamilyName() {
    return CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME;
  }

  private ByteBuffer[] getEndpointKeyHash(List<CassandraEpByEndpointGroupId> filter) {
    ByteBuffer[] endpointKeyHash = new ByteBuffer[filter.size()];
    int pos = 0;
    for (CassandraEpByEndpointGroupId ep : filter) {
      endpointKeyHash[pos++] = ep.getEndpointKeyHash();
    }
    return endpointKeyHash;
  }

  /**
   * Get endpoints ids.
   *
   * @param pageLink contains searching parameters (offset, etc.)
   * @return endpoints ids
   */
  public ByteBuffer[] findEpByEndpointGroupId(PageLinkDto pageLink) {
    String endpointGroupId = pageLink.getEndpointGroupId();
    String limit = pageLink.getLimit();
    ByteBuffer endpointKey = getByteBuffer(Base64.decodeBase64(pageLink.getOffset()));
    Statement queryStatement;
    if ("0".equals(pageLink.getOffset())) {
      queryStatement = select().from(getColumnFamilyName())
          .where(eq(
              EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY, endpointGroupId))
          .limit(Integer.valueOf(limit) + 1);
      LOG.debug("Try to find first page of endpoint key hash by group id {} with limit {}",
          endpointGroupId, limit);
    } else {
      queryStatement = select().from(getColumnFamilyName())
          .where(eq(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY, endpointGroupId))
          .and(gte(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY, endpointKey))
          .limit(Integer.valueOf(limit) + 1);
      LOG.debug("Try to find endpoint key hash list by endpoint group id {} "
              + "with limit {} start from keyHash {}",
          endpointGroupId, limit, Utils.encodeHexString(endpointKey));
    }
    List<CassandraEpByEndpointGroupId> filter = findListByStatement(queryStatement);
    return getEndpointKeyHash(filter);
  }
}
