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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.parseId;

import com.datastax.driver.core.utils.Bytes;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointNotification;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.NotificationTypeCodec;
import org.kaaproject.kaa.server.common.utils.Utils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

@Table(name = CassandraModelConstants.ET_NF_COLUMN_FAMILY_NAME)
public final class CassandraEndpointNotification implements EndpointNotification, Serializable {

  @Transient
  private static final long serialVersionUID = -6770166693195322360L;

  @PartitionKey
  @Column(name = CassandraModelConstants.ET_NF_ENDPOINT_KEY_HASH_PROPERTY)
  private ByteBuffer endpointKeyHash;
  @Column(name = CassandraModelConstants.ET_NF_SEQ_NUM_PROPERTY)
  private Integer seqNum;
  @Column(name = CassandraModelConstants.ET_NF_ID_PROPERTY)
  private String id;
  @Column(name = CassandraModelConstants.ET_NF_NOTIFICATION_TYPE_PROPERTY,
      codec = NotificationTypeCodec.class)
  private NotificationTypeDto type;
  @Column(name = CassandraModelConstants.ET_NF_APPLICATION_ID_PROPERTY)
  private String applicationId;
  @Column(name = CassandraModelConstants.ET_NF_SCHEMA_ID_PROPERTY)
  private String schemaId;
  @Column(name = CassandraModelConstants.ET_NF_VERSION_PROPERTY)
  private int nfVersion;
  @ClusteringColumn
  @Column(name = CassandraModelConstants.ET_NF_LAST_MOD_TIME_PROPERTY)
  private Date lastModifyTime;
  @Column(name = CassandraModelConstants.ET_NF_BODY_PROPERTY)
  private ByteBuffer body;
  @Column(name = CassandraModelConstants.ET_NF_EXPIRED_AT_PROPERTY)
  private Date expiredAt;
  @Column(name = CassandraModelConstants.ET_NF_TOPIC_ID_PROPERTY)
  private String topicId;

  public CassandraEndpointNotification() {
  }

  public CassandraEndpointNotification(String id) {
    parseStringId(id);
  }

  /**
   * Create new instance of <code>CassandraTopicListEntry</code>.
   *
   * @param dto data transfer object contain data that assign on fields of new instance
   */
  public CassandraEndpointNotification(EndpointNotificationDto dto) {
    this.endpointKeyHash = ByteBuffer.wrap(dto.getEndpointKeyHash());
    NotificationDto notificationDto = dto.getNotificationDto();
    if (notificationDto != null) {
      this.seqNum = notificationDto.getSecNum();
      this.type = notificationDto.getType();
      this.applicationId = notificationDto.getApplicationId();
      this.schemaId = notificationDto.getSchemaId();
      this.nfVersion = notificationDto.getNfVersion();
      this.lastModifyTime = notificationDto.getLastTimeModify();
      this.body = getByteBuffer(notificationDto.getBody());
      this.expiredAt = notificationDto.getExpiredAt();
      this.topicId = notificationDto.getTopicId();

    }
    this.id = dto.getId() != null ? dto.getId() : generateId();
  }


  public ByteBuffer getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
    this.endpointKeyHash = endpointKeyHash;
  }

  /**
   * Generate new id using <code>endpointKeyHash</code> and <code>lastModifyTime</code>.
   *
   * @return id
   */
  public String generateId() {
    String id = null;
    if (endpointKeyHash != null) {
      StringBuilder builder = new StringBuilder(Bytes.toHexString(endpointKeyHash));
      builder.append(CassandraModelConstants.KEY_DELIMITER).append(lastModifyTime.getTime());
      id = builder.toString();
    }
    return id;
  }

  /**
   * Id consist of endpoint key hash and last modify time, we get these data and assign values on
   * fields <code>endpointKeyHash</code> and <code>lastModifyTime</code>.
   *
   * @param id is id to parsing
   */
  public void parseStringId(String id) {
    String[] ids = parseId(id);
    if (ids != null && ids.length == 2) {
      endpointKeyHash = Bytes.fromHexString(ids[0]);
      lastModifyTime = new Date(Long.valueOf(ids[1]));
    }
  }

  public Integer getSeqNum() {
    return seqNum;
  }

  public void setSeqNum(Integer seqNum) {
    this.seqNum = seqNum;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public NotificationTypeDto getType() {
    return type;
  }

  public void setType(NotificationTypeDto type) {
    this.type = type;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  public int getNfVersion() {
    return nfVersion;
  }

  public void setNfVersion(int nfVersion) {
    this.nfVersion = nfVersion;
  }

  public Date getLastModifyTime() {
    return lastModifyTime;
  }

  public void setLastModifyTime(Date lastModifyTime) {
    this.lastModifyTime = lastModifyTime;
  }

  public ByteBuffer getBody() {
    return body;
  }

  public void setBody(ByteBuffer body) {
    this.body = body;
  }

  public Date getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(Date expiredAt) {
    this.expiredAt = expiredAt;
  }

  public String getTopicId() {
    return topicId;
  }

  public void setTopicId(String topicId) {
    this.topicId = topicId;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    CassandraEndpointNotification that = (CassandraEndpointNotification) object;

    if (nfVersion != that.nfVersion) {
      return false;
    }

    if (applicationId != null
        ? !applicationId.equals(that.applicationId)
        : that.applicationId != null) {
      return false;
    }

    if (body != null ? !body.equals(that.body) : that.body != null) {
      return false;
    }

    if (endpointKeyHash != null
        ? !endpointKeyHash.equals(that.endpointKeyHash)
        : that.endpointKeyHash != null) {
      return false;
    }

    if (expiredAt != null ? !expiredAt.equals(that.expiredAt) : that.expiredAt != null) {
      return false;
    }

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }

    if (lastModifyTime != null
        ? !lastModifyTime.equals(that.lastModifyTime)
        : that.lastModifyTime != null) {
      return false;
    }

    if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) {
      return false;
    }

    if (seqNum != null ? !seqNum.equals(that.seqNum) : that.seqNum != null) {
      return false;
    }

    if (type != that.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = endpointKeyHash != null ? endpointKeyHash.hashCode() : 0;
    result = 31 * result + (seqNum != null ? seqNum.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
    result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
    result = 31 * result + nfVersion;
    result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + (expiredAt != null ? expiredAt.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CassandraEndpointNotification{"
        + "endpointKeyHash=" + Utils.encodeHexString(endpointKeyHash)
        + ", seqNum=" + seqNum
        + ", id='" + id + '\''
        + ", type=" + type
        + ", applicationId='" + applicationId + '\''
        + ", schemaId='" + schemaId + '\''
        + ", nfVersion=" + nfVersion
        + ", lastModifyTime=" + lastModifyTime
        + ", body=" + Utils.encodeHexString(body)
        + ", expiredAt=" + expiredAt
        + '}';
  }

  @Override
  public EndpointNotificationDto toDto() {
    EndpointNotificationDto dto = new EndpointNotificationDto();
    dto.setId(id != null ? id : generateId());
    dto.setEndpointKeyHash(endpointKeyHash != null ? endpointKeyHash.array() : null);
    NotificationDto notificationDto = new NotificationDto();
    notificationDto.setSecNum(seqNum);
    notificationDto.setType(type);
    notificationDto.setApplicationId(applicationId);
    notificationDto.setSchemaId(schemaId);
    notificationDto.setNfVersion(nfVersion);
    notificationDto.setLastTimeModify(lastModifyTime);
    notificationDto.setBody(getBytes(body));
    notificationDto.setExpiredAt(expiredAt);
    notificationDto.setTopicId(topicId);
    dto.setNotificationDto(notificationDto);
    return dto;
  }
}
