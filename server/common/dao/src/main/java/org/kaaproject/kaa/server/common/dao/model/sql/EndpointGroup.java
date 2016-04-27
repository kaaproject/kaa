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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_ENDPOINT_COUNT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_SEQUENCE_NUMBER;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_WEIGHT;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getTopic;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getTopicIds;

@Entity
@Table(name = ENDPOINT_GROUP_TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {ENDPOINT_GROUP_WEIGHT, ENDPOINT_GROUP_APPLICATION_ID}),
        @UniqueConstraint(columnNames = {ENDPOINT_GROUP_NAME, ENDPOINT_GROUP_APPLICATION_ID})})
public class EndpointGroup extends GenericModel<EndpointGroupDto> implements Serializable {

    private static final long serialVersionUID = -2160369956685033697L;

    @Column(name = ENDPOINT_GROUP_NAME)
    private String name;

    @Column(name = ENDPOINT_GROUP_SEQUENCE_NUMBER)
    private int sequenceNumber;

    @Column(name = ENDPOINT_GROUP_WEIGHT)
    private int weight;

    @Column(name = ENDPOINT_GROUP_ENDPOINT_COUNT)
    private long endpointCount;

    @Column(name = ENDPOINT_GROUP_DESCRIPTION, length = 255)
    private String description;

    @Column(name = ENDPOINT_GROUP_CREATED_USERNAME)
    private String createdUsername;

    @Column(name = ENDPOINT_GROUP_CREATED_TIME)
    private long createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ENDPOINT_GROUP_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @ManyToMany(mappedBy = "endpointGroups")
    private Set<Topic> topics = new HashSet<>();

    public EndpointGroup() {
    }

    public EndpointGroup(Long id) {
        this.id = id;
    }

    public EndpointGroup(EndpointGroupDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.name = dto.getName();
            this.sequenceNumber = dto.getSequenceNumber();
            this.weight = dto.getWeight();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            this.topics = getTopic(dto.getTopics());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(long endpointCount) {
        this.endpointCount = endpointCount;
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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    public String getApplicationId() {
        return application != null ? application.getStringId() : null;
    }

    @Override
    public String toString() {
        return "EndpointGroup [name=" + name + ", sequenceNumber=" + sequenceNumber + ", weight=" + weight + ", endpointCount=" + endpointCount
                + ", description=" + description + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime + ", id=" + id + "]";
    }

    protected EndpointGroupDto createDto() {
        return new EndpointGroupDto();
    }

    @Override
    protected GenericModel<EndpointGroupDto> newInstance(Long id) {
        return new EndpointGroup(id);
    }

    @Override
    public EndpointGroupDto toDto() {
        EndpointGroupDto dto = createDto();
        dto.setId(getStringId());
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        dto.setName(name);
        dto.setSequenceNumber(sequenceNumber);
        dto.setWeight(weight);
        dto.setTopics(getTopicIds(topics));
        return dto;
    }

    @Override
    public int hashCode() {
        final int prime = 39;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + weight;
        result = prime * result + Long.valueOf(endpointCount).hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + Long.valueOf(createdTime).hashCode();
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointGroup other = (EndpointGroup) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (weight != other.weight) {
            return false;
        }
        if (endpointCount != other.endpointCount) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
        return true;
    }
}
