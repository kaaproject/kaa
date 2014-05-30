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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

@Document(collection = EndpointGroup.COLLECTION_NAME)
public final class EndpointGroup implements ToDto<EndpointGroupDto>, Serializable {

    private static final long serialVersionUID = 7886434688560108952L;

    public static final String COLLECTION_NAME = "endpoint_group";

    @Id
    private String id;
    @Indexed
    private String name;
    @Field("description")
    private String description;
    @Field("created_username")
    private String createdUsername;
    @Field("created_time")
    private long createdTime;
    @Field("application_id")
    private ObjectId applicationId;
    private int weight;
    @Field("seq_num")
    private int sequenceNumber;
    private List<String> topics;
    @Field("endpoint_count")
    private long endpointCount;

    public EndpointGroup() {
    }

    public EndpointGroup(EndpointGroupDto dto) {
        this.id = dto.getId();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        this.weight = dto.getWeight();
        this.sequenceNumber = dto.getSequenceNumber();
        this.topics = dto.getTopics();
        this.endpointCount = dto.getEndpointCount();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public long getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(long endpointCount) {
        this.endpointCount = endpointCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndpointGroup that = (EndpointGroup) o;

        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (weight != that.weight) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (topics != null ? !topics.equals(that.topics) : that.topics != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + weight;
        result = 31 * result + sequenceNumber;
        result = 31 * result + (topics != null ? topics.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointGroup{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", applicationId=" + applicationId +
                ", weight=" + weight +
                ", sequenceNumber=" + sequenceNumber +
                ", topics=" + topics +
                '}';
    }

    @Override
    public EndpointGroupDto toDto() {
        EndpointGroupDto dto = new EndpointGroupDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        dto.setApplicationId(idToString(applicationId));
        dto.setName(name);
        dto.setSequenceNumber(sequenceNumber);
        dto.setWeight(weight);
        dto.setTopics(topics);
        dto.setEndpointCount(endpointCount);
        return dto;
    }
}
