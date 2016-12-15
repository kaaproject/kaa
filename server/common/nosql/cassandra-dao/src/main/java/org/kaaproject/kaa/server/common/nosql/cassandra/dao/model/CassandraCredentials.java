/**
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_STATUS_PROPERTY;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.kaaproject.kaa.server.common.utils.Utils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Table(name = CREDENTIALS_COLUMN_FAMILY_NAME)
public class CassandraCredentials implements Credentials, Serializable {

  @Transient
  private static final long serialVersionUID = 5814711856025319827L;

  @PartitionKey
  @Column(name = CREDENTIALS_APPLICATION_ID_PROPERTY)
  private String applicationId;
  @ClusteringColumn
  @Column(name = CREDENTIALS_ID_PROPERTY)
  private String id;
  @Column(name = CREDENTIALS_BODY_PROPERTY)
  private ByteBuffer cassandraCredentialsBody;
  @Column(name = CREDENTIALS_STATUS_PROPERTY)
  private String cassandraCredentialsStatus;

  public CassandraCredentials() {
  }


  /**
   * Create new instance of <code>CassandraCredentials</code>.
   *
   * @param applicationId is application id
   * @param dto data transfer object contain data that
   *            assign on fields of new instance
   */
  public CassandraCredentials(String applicationId, CredentialsDto dto) {
    this.applicationId = applicationId;
    this.id = dto.getId();
    this.cassandraCredentialsBody = getByteBuffer(dto.getCredentialsBody());
    this.cassandraCredentialsStatus = dto.getStatus().toString();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public byte[] getCredentialsBody() {
    byte[] buffer = cassandraCredentialsBody.array();
    return Arrays.copyOf(buffer, buffer.length);
  }

  @Override
  public CredentialsStatus getStatus() {
    return CredentialsStatus.valueOf(cassandraCredentialsStatus);
  }

  public String getApplicationId() {
    return this.applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public ByteBuffer getCassandraCredentialsBody() {
    return ByteBuffer.wrap(this.getCredentialsBody());
  }

  public void setCassandraCredentialsBody(ByteBuffer cassandraCredentialsBody) {
    byte[] buffer = cassandraCredentialsBody.array();
    this.cassandraCredentialsBody = ByteBuffer.wrap(Arrays.copyOf(buffer, buffer.length));
  }

  public String getCassandraCredentialsStatus() {
    return this.cassandraCredentialsStatus;
  }

  public void setCassandraCredentialsStatus(String cassandraCredentialsStatus) {
    this.cassandraCredentialsStatus = cassandraCredentialsStatus;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    CassandraCredentials that = (CassandraCredentials) object;

    if (!applicationId.equals(that.applicationId)) {
      return false;
    }

    if (!cassandraCredentialsStatus.equals(that.cassandraCredentialsStatus)) {
      return false;
    }

    if (!cassandraCredentialsBody.equals(that.cassandraCredentialsBody)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = applicationId.hashCode();
    result = 31 * result + cassandraCredentialsBody.hashCode();
    result = 31 * result + cassandraCredentialsStatus.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "CassandraCredentials{"
        + "applicationId='" + applicationId + '\''
        + ", id='" + id + '\''
        + ", credentialsBody=" + Utils.encodeHexString(cassandraCredentialsBody)
        + ", status='" + cassandraCredentialsStatus + '\''
        + '}';
  }

  @Override
  public CredentialsDto toDto() {
    CredentialsDto credentialsDto = new CredentialsDto();
    credentialsDto.setId(id);
    credentialsDto.setCredentialsBody(getBytes(cassandraCredentialsBody));
    credentialsDto.setStatus(CredentialsStatus.valueOf(cassandraCredentialsStatus));
    return credentialsDto;
  }
}
