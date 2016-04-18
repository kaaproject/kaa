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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_ACTIVATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_ACTIVATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_DEACTIVATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_DEACTIVATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_ENDPOINT_COUNT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_ENDPOINT_GROUP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_LAST_MODIFY_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_MODIFIED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_OPTIMISTIC_LOCK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_SEQUENCE_NUMBER;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_STATUS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ABSTRACT_STRUCTURE_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;

@Entity
@Table(name = ABSTRACT_STRUCTURE_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractStructure<T extends AbstractStructureDto> extends GenericModel<T> implements Serializable {

    private static final long serialVersionUID = 5871567091169424733L;

    @Column(name = ABSTRACT_STRUCTURE_SEQUENCE_NUMBER)
    protected int sequenceNumber;

    @Column(name = ABSTRACT_STRUCTURE_DESCRIPTION)
    protected String description;

    @Column(name = ABSTRACT_STRUCTURE_CREATED_TIME)
    protected long createdTime;

    @Column(name = ABSTRACT_STRUCTURE_LAST_MODIFY_TIME)
    protected long lastModifyTime;

    @Column(name = ABSTRACT_STRUCTURE_ACTIVATED_TIME)
    protected long activatedTime;

    @Column(name = ABSTRACT_STRUCTURE_DEACTIVATED_TIME)
    protected long deactivatedTime;

    @Column(name = ABSTRACT_STRUCTURE_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = ABSTRACT_STRUCTURE_MODIFIED_USERNAME)
    protected String modifiedUsername;

    @Column(name = ABSTRACT_STRUCTURE_ACTIVATED_USERNAME)
    protected String activatedUsername;

    @Column(name = ABSTRACT_STRUCTURE_DEACTIVATED_USERNAME)
    protected String deactivatedUsername;

    @Column(name = ABSTRACT_STRUCTURE_ENDPOINT_COUNT)
    protected long endpointCount;

    @Column(name = ABSTRACT_STRUCTURE_STATUS)
    @Enumerated(EnumType.STRING)
    protected UpdateStatus status;

    @ManyToOne
    @JoinColumn(name = ABSTRACT_STRUCTURE_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected Application application;

    @ManyToOne
    @JoinColumn(name = ABSTRACT_STRUCTURE_ENDPOINT_GROUP_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected EndpointGroup endpointGroup;

    @Version
    @Column(name = ABSTRACT_STRUCTURE_OPTIMISTIC_LOCK)
    private Long version;

    public Long getVersion() {
        return version;
    }

    public AbstractStructure() {
    }

    public AbstractStructure(Long id) {
        this.id = id;
    }

    public AbstractStructure(AbstractStructureDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.sequenceNumber = dto.getSequenceNumber();
            this.description = dto.getDescription();
            this.createdTime = dto.getCreatedTime();
            this.lastModifyTime = dto.getLastModifyTime();
            this.activatedTime = dto.getActivatedTime();
            this.deactivatedTime = dto.getDeactivatedTime();
            this.createdUsername = dto.getCreatedUsername();
            this.modifiedUsername = dto.getModifiedUsername();
            this.activatedUsername = dto.getActivatedUsername();
            this.deactivatedUsername = dto.getDeactivatedUsername();
            this.status = dto.getStatus();
            this.endpointCount = dto.getEndpointCount();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            Long groupId = getLongId(dto.getEndpointGroupId());
            this.endpointGroup = groupId != null ? new EndpointGroup(groupId) : null;
            this.version = dto.getVersion();
        }
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void incrementSequenceNumber() {
        sequenceNumber++;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public long getActivatedTime() {
        return activatedTime;
    }

    public void setActivatedTime(long activatedTime) {
        this.activatedTime = activatedTime;
    }

    public long getDeactivatedTime() {
        return deactivatedTime;
    }

    public void setDeactivatedTime(long deactivatedTime) {
        this.deactivatedTime = deactivatedTime;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public String getModifiedUsername() {
        return modifiedUsername;
    }

    public void setModifiedUsername(String modifiedUsername) {
        this.modifiedUsername = modifiedUsername;
    }

    public String getActivatedUsername() {
        return activatedUsername;
    }

    public void setActivatedUsername(String activatedUsername) {
        this.activatedUsername = activatedUsername;
    }

    public String getDeactivatedUsername() {
        return deactivatedUsername;
    }

    public void setDeactivatedUsername(String deactivatedUsername) {
        this.deactivatedUsername = deactivatedUsername;
    }

    public long getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(long endpointCount) {
        this.endpointCount = endpointCount;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateStatus status) {
        this.status = status;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public EndpointGroup getEndpointGroup() {
        return endpointGroup;
    }

    public void setEndpointGroup(EndpointGroup endpointGroup) {
        this.endpointGroup = endpointGroup;
    }

    @Override
    public T toDto() {
        T dto = createDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        if (endpointGroup != null) {
            dto.setEndpointGroupId(endpointGroup.getStringId());
        }
        dto.setSequenceNumber(sequenceNumber);
        dto.setDescription(description);
        dto.setCreatedTime(createdTime);
        dto.setLastModifyTime(lastModifyTime);
        dto.setActivatedTime(activatedTime);
        dto.setDeactivatedTime(deactivatedTime);
        dto.setCreatedUsername(createdUsername);
        dto.setModifiedUsername(modifiedUsername);
        dto.setActivatedUsername(activatedUsername);
        dto.setDeactivatedUsername(deactivatedUsername);
        dto.setStatus(status);
        dto.setEndpointCount(endpointCount);
        dto.setVersion(version);
        return dto;
    }

    public abstract String getBody();

    public abstract void setBody(String body);

    @Override
    public int hashCode() {
        final int prime = 33;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + Long.valueOf(createdTime).hashCode();
        result = prime * result + Long.valueOf(lastModifyTime).hashCode();
        result = prime * result + Long.valueOf(activatedTime).hashCode();
        result = prime * result + Long.valueOf(deactivatedTime).hashCode();
        result = prime * result + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((modifiedUsername == null) ? 0 : modifiedUsername.hashCode());
        result = prime * result + ((activatedUsername == null) ? 0 : activatedUsername.hashCode());
        result = prime * result + ((deactivatedUsername == null) ? 0 : deactivatedUsername.hashCode());
        result = prime * result + Long.valueOf(endpointCount).hashCode();
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + ((endpointGroup == null) ? 0 : endpointGroup.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
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
        AbstractStructure<T> other = (AbstractStructure<T>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (lastModifyTime != other.lastModifyTime) {
            return false;
        }
        if (activatedTime != other.activatedTime) {
            return false;
        }
        if (deactivatedTime != other.deactivatedTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (modifiedUsername == null) {
            if (other.modifiedUsername != null) {
                return false;
            }
        } else if (!modifiedUsername.equals(other.modifiedUsername)) {
            return false;
        }
        if (activatedUsername == null) {
            if (other.activatedUsername != null) {
                return false;
            }
        } else if (!activatedUsername.equals(other.activatedUsername)) {
            return false;
        }
        if (deactivatedUsername == null) {
            if (other.deactivatedUsername != null) {
                return false;
            }
        } else if (!deactivatedUsername.equals(other.deactivatedUsername)) {
            return false;
        }
        if (endpointCount != other.endpointCount) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
        if (endpointGroup == null) {
            if (other.endpointGroup != null) {
                return false;
            }
        } else if (!endpointGroup.equals(other.endpointGroup)) {
            return false;
        }
        return true;
    }
}
