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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

@Document(collection = Notification.COLLECTION_NAME)
public final class Notification implements ToDto<NotificationDto>, Serializable {

    private static final long serialVersionUID = 348872010210481058L;

    public static final String COLLECTION_NAME = "notification";

    @Id
    private String id;
    @Field("application_id")
    private ObjectId applicationId;
    @Field("notification_schema_id")
    private ObjectId schemaId;
    @Field("topic_id")
    private ObjectId topicId;
    private int version;
    @LastModifiedDate
    @Field("last_modify_time")
    private Date lastModifyTime;
    @Field("notification_type")
    private NotificationTypeDto type;
    private byte[] body;
    @Indexed(expireAfterSeconds = 0)
    @Field("expired_at")
    private Date expiredAt;
    @Field("seq_num")
    private int secNum;

    public Notification() {
    }

    public Notification(NotificationDto dto) {
        this.id = dto.getId();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.schemaId = idToObjectId(dto.getSchemaId());
        this.topicId = idToObjectId(dto.getTopicId());
        this.version = dto.getVersion();
        this.lastModifyTime = dto.getLastTimeModify();
        this.type = dto.getType();
        this.body = getArrayCopy(dto.getBody());
        this.expiredAt = dto.getExpiredAt();
        this.secNum = dto.getSecNum();
    }

    public String getId() {
        return id;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public ObjectId getSchemaId() {
        return schemaId;
    }

    public ObjectId getTopicId() {
        return topicId;
    }

    public int getVersion() {
        return version;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public NotificationTypeDto getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }

    public int getSecNum() {
        return secNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }

        Notification that = (Notification) o;

        if (secNum != that.secNum) {
            return false;
        }
        if (version != that.version) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (!Arrays.equals(body, that.body)) {
            return false;
        }
        if (expiredAt != null ? !expiredAt.equals(that.expiredAt) : that.expiredAt != null) {
            return false;
        }
        if (lastModifyTime != null ? !lastModifyTime.equals(that.lastModifyTime) : that.lastModifyTime != null) {
            return false;
        }
        if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) {
            return false;
        }
        if (topicId != null ? !topicId.equals(that.topicId) : that.topicId != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
        result = 31 * result + (topicId != null ? topicId.hashCode() : 0);
        result = 31 * result + version;
        result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
        result = 31 * result + (expiredAt != null ? expiredAt.hashCode() : 0);
        result = 31 * result + secNum;
        return result;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", schemaId=" + schemaId +
                ", topicId=" + topicId +
                ", version=" + version +
                ", lastModifyTime=" + lastModifyTime +
                ", type=" + type +
                ", body=" + Arrays.toString(body) +
                ", expiredAt=" + expiredAt +
                ", secNum=" + secNum +
                '}';
    }

    @Override
    public NotificationDto toDto() {
        NotificationDto dto = new NotificationDto();
        dto.setId(id);
        dto.setApplicationId(idToString(applicationId));
        dto.setSchemaId(idToString(schemaId));
        dto.setTopicId(idToString(topicId));
        dto.setLastTimeModify(lastModifyTime);
        dto.setVersion(version);
        dto.setType(type);
        dto.setBody(getArrayCopy(body));
        dto.setExpiredAt(expiredAt);
        dto.setSecNum(secNum);
        return dto;
    }
}
