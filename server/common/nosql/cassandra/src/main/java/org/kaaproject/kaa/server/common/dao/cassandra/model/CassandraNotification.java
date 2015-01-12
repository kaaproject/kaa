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

package org.kaaproject.kaa.server.common.dao.cassandra.model;

import com.datastax.driver.mapping.EnumType;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.model.Notification;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_EXPIRED_AT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_LAST_MOD_TIME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_NOTIFICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_NOTIFICATION_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_SCHEMA_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_SEQ_NUM_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_TOPIC_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_VERSION_PROPERTY;


@Table(name = NF_COLUMN_FAMILY_NAME)
public final class CassandraNotification implements Notification, Serializable {

    @Transient
    private static final long serialVersionUID = 348872010210481058L;

    @PartitionKey(value = 0)
    @Column(name = NF_TOPIC_ID_PROPERTY)
    private String topicId;

    @PartitionKey(value = 1)
    @Column(name = NF_NOTIFICATION_TYPE_PROPERTY)
    @Enumerated(EnumType.STRING)
    private NotificationTypeDto type;

    @Column(name = NF_NOTIFICATION_ID_PROPERTY)
    private String id;
    @Column(name = NF_APPLICATION_ID_PROPERTY)
    private String applicationId;
    @Column(name = NF_SCHEMA_ID_PROPERTY)
    private String schemaId;

    @ClusteringColumn(value = 0)
    @Column(name = NF_VERSION_PROPERTY)
    private int version;
    @ClusteringColumn(value = 1)
    @Column(name = NF_SEQ_NUM_PROPERTY)
    private int seqNum;

    @Column(name = NF_LAST_MOD_TIME_PROPERTY)
    private Date lastModifyTime;
    @Column(name = NF_BODY_PROPERTY)
    private ByteBuffer body;
    @Column(name = NF_EXPIRED_AT_PROPERTY)
    private Date expiredAt;

    public CassandraNotification() {

    }

    public CassandraNotification(NotificationDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.schemaId = dto.getSchemaId();
        this.topicId = dto.getTopicId();
        this.version = dto.getVersion();
        this.lastModifyTime = dto.getLastTimeModify();
        this.type = dto.getType();
        this.body = getByteBuffer(dto.getBody());
        this.expiredAt = dto.getExpiredAt();
        this.seqNum = dto.getSecNum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public NotificationTypeDto getType() {
        return type;
    }

    public void setType(NotificationTypeDto type) {
        this.type = type;
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

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraNotification that = (CassandraNotification) o;

        if (seqNum != that.seqNum) return false;
        if (version != that.version) return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (expiredAt != null ? !expiredAt.equals(that.expiredAt) : that.expiredAt != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (lastModifyTime != null ? !lastModifyTime.equals(that.lastModifyTime) : that.lastModifyTime != null)
            return false;
        if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) return false;
        if (topicId != null ? !topicId.equals(that.topicId) : that.topicId != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topicId != null ? topicId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
        result = 31 * result + version;
        result = 31 * result + seqNum;
        result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (expiredAt != null ? expiredAt.hashCode() : 0);
        return result;
    }

    @Override
    public NotificationDto toDto() {
        NotificationDto dto = new NotificationDto();
        dto.setId(id);
        dto.setApplicationId(applicationId);
        dto.setSchemaId(schemaId);
        dto.setTopicId(topicId);
        dto.setLastTimeModify(lastModifyTime);
        dto.setVersion(version);
        dto.setType(type);
        dto.setBody(body != null ? getBytes(body) : null);
        dto.setExpiredAt(expiredAt);
        dto.setSecNum(seqNum);
        return dto;
    }
}
