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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.server.common.utils.Utils;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(name = CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME)
public class CassandraEpByAppId implements Serializable {

  @Transient
  private static final long serialVersionUID = 4620788066149588088L;

  @PartitionKey
  @Column(name = CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY)
  private String appId;
  @ClusteringColumn
  @Column(name = CassandraModelConstants.EP_BY_APP_ID_ENDPOINT_KEY_HASH_PROPERTY)
  private ByteBuffer endpointKeyHash;

  public CassandraEpByAppId() {
  }

  public CassandraEpByAppId(String appId, ByteBuffer endpointKeyHash) {
    this.appId = appId;
    this.endpointKeyHash = endpointKeyHash;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public ByteBuffer getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
    this.endpointKeyHash = endpointKeyHash;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    CassandraEpByAppId that = (CassandraEpByAppId) object;

    if (appId != null ? !appId.equals(that.appId) : that.appId != null) {
      return false;
    }

    if (endpointKeyHash != null
        ? !endpointKeyHash.equals(that.endpointKeyHash)
        : that.endpointKeyHash != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = appId != null ? appId.hashCode() : 0;
    result = 31 * result + (endpointKeyHash != null ? endpointKeyHash.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CassandraEpByAppId{"
        + "appId='" + appId + '\''
        + ", endpointKeyHash=" + Utils.encodeHexString(endpointKeyHash)
        + '}';
  }
}
