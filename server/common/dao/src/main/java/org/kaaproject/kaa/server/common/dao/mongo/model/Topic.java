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
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

@Document(collection = Topic.COLLECTION_NAME)
public final class Topic implements ToDto<TopicDto>, Serializable {

    private static final long serialVersionUID = 2358234005300512668L;

    public static final String COLLECTION_NAME = "topic";

    @Id
    private String id;
    @Field("application_id")
    private ObjectId applicationId;
    private String name;
    @Indexed
    @Field("topic_type")
    private TopicTypeDto type;
    @Field("description")
    protected String description;
    @Field("created_username")
    protected String createdUsername;
    @Field("created_time")
    protected long createdTime;
    @Field("seq_num")
    private int secNum;
    @Field("upd")
    private Update update;

    public Topic() {
    }

    public Topic(TopicDto dto) {
        this.id = dto.getId();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.name = dto.getName();
        this.type = dto.getType();
        this.description = dto.getDescription();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        this.secNum = dto.getSecNum();
    }

    public String getId() {
        return id;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public String getName() {
        return name;
    }

    public TopicTypeDto getType() {
        return type;
    }

    public int getSecNum() {
        return secNum;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Topic)) {
            return false;
        }

        Topic topic = (Topic) o;

        if (secNum != topic.secNum) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(topic.applicationId) : topic.applicationId != null) {
            return false;
        }
        if (name != null ? !name.equals(topic.name) : topic.name != null) {
            return false;
        }
        if (type != topic.type) {
            return false;
        }
        if (update != null ? !update.equals(topic.update) : topic.update != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + secNum;
        result = 31 * result + (update != null ? update.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Topic [id=" + id + ", applicationId=" + applicationId
                + ", name=" + name + ", type=" + type + ", description="
                + description + ", createdUsername=" + createdUsername
                + ", createdTime=" + createdTime + ", secNum=" + secNum
                + ", update=" + update + "]";
    }

    @Override
    public TopicDto toDto() {
        TopicDto dto = new TopicDto();
        dto.setId(id);
        dto.setApplicationId(idToString(applicationId));
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        dto.setSecNum(secNum);
        return dto;
    }
}
