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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.model.Notification;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_BODY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_EXPIRED_AT;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_LAST_MODIFY_TIME;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_SEQ_NUM;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_TOPIC_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_TYPE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.NF_VERSION;

@Document(collection = MongoModelConstants.NOTIFICATION)
public final class MongoNotification implements Notification, Serializable {

    private static final long serialVersionUID = 348872010210481058L;

    @Id
    private String id;
    @Field(NF_APPLICATION_ID)
    private String applicationId;
    @Field(NF_SCHEMA_ID)
    private String schemaId;
    @Field(NF_TOPIC_ID)
    private String topicId;
    @Field(NF_VERSION)
    private int nfVersion;
    @LastModifiedDate
    @Field(NF_LAST_MODIFY_TIME)
    private Date lastModifyTime;
    @Field(NF_TYPE)
    private NotificationTypeDto type;
    @Field(NF_BODY)
    private byte[] body;
    @Indexed(expireAfterSeconds = 0)
    @Field(NF_EXPIRED_AT)
    private Date expiredAt;
    @Field(NF_SEQ_NUM)
    private int secNum;

    public MongoNotification() {
    }

    public MongoNotification(NotificationDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.schemaId = dto.getSchemaId();
        this.topicId = dto.getTopicId();
        this.nfVersion = dto.getNfVersion();
        this.lastModifyTime = dto.getLastTimeModify();
        this.type = dto.getType();
        this.body = getArrayCopy(dto.getBody());
        this.expiredAt = dto.getExpiredAt();
        this.secNum = dto.getSecNum();
    }

    public String getId() {
        return id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public String getTopicId() {
        return topicId;
    }

    public int getNfVersion() {
        return nfVersion;
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
        if (!(o instanceof MongoNotification)) {
            return false;
        }

        MongoNotification that = (MongoNotification) o;

        if (secNum != that.secNum) {
            return false;
        }
        if (nfVersion != that.nfVersion) {
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
        result = 31 * result + nfVersion;
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
                ", nfVersion=" + nfVersion +
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
        dto.setApplicationId(applicationId);
        dto.setSchemaId(schemaId);
        dto.setTopicId(topicId);
        dto.setLastTimeModify(lastModifyTime);
        dto.setNfVersion(nfVersion);
        dto.setType(type);
        dto.setBody(getArrayCopy(body));
        dto.setExpiredAt(expiredAt);
        dto.setSecNum(secNum);
        return dto;
    }
}
