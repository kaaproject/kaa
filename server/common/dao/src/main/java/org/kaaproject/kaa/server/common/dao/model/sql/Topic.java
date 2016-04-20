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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_TOPICS_ENDPOINT_GROUP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_TOPICS_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_TOPICS_TOPIC_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_SEQUENCE_NUMBER;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TOPIC_TYPE;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;

@Entity
@Table(name = TOPIC_TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {TOPIC_NAME, ENDPOINT_GROUP_APPLICATION_ID})
})
public class Topic extends GenericModel<TopicDto> implements Serializable {

    private static final long serialVersionUID = -5617352698933455002L;

    @Column(name = TOPIC_NAME)
    private String name;

    @Column(name = TOPIC_DESCRIPTION)
    protected String description;

    @Column(name = TOPIC_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = TOPIC_CREATED_TIME)
    protected long createdTime;

    @Column(name = TOPIC_SEQUENCE_NUMBER)
    private int sequenceNumber;

    @Column(name = TOPIC_TYPE)
    @Enumerated(EnumType.STRING)
    private TopicTypeDto type;

    @ManyToOne
    @JoinColumn(name = TOPIC_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = ENDPOINT_GROUP_TOPICS_TABLE_NAME,
            joinColumns = {
                    @JoinColumn(name = ENDPOINT_GROUP_TOPICS_TOPIC_ID)},
            inverseJoinColumns = {
                    @JoinColumn(name = ENDPOINT_GROUP_TOPICS_ENDPOINT_GROUP_ID)})
    private Set<EndpointGroup> endpointGroups = new HashSet<>();

    public Topic() {
    }

    public Topic(Long id) {
        this.id = id;
    }

    public Topic(TopicDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.name = dto.getName();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            this.sequenceNumber = dto.getSecNum();
            this.type = dto.getType();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
        }
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public TopicTypeDto getType() {
        return type;
    }

    public void setType(TopicTypeDto type) {
        this.type = type;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Set<EndpointGroup> getEndpointGroups() {
        return endpointGroups;
    }

    public void setEndpointGroups(Set<EndpointGroup> endpointGroups) {
        this.endpointGroups = endpointGroups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Topic other = (Topic) obj;
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
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
        return type == other.type;
    }

    public int incrementSeqNumber() {
        return ++sequenceNumber;
    }

    @Override
    protected TopicDto createDto() {
        return new TopicDto();
    }

    @Override
    protected GenericModel<TopicDto> newInstance(Long id) {
        return new Topic(id);
    }

    @Override
    public TopicDto toDto() {
        TopicDto dto = createDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        dto.setName(name);
        dto.setType(type);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        dto.setSecNum(sequenceNumber);
        return dto;
    }

    @Override
    public String toString() {
        return "Topic [name=" + name + ", description=" + description + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime
                + ", sequenceNumber=" + sequenceNumber + ", type=" + type + ", id=" + id + "]";
    }

}
