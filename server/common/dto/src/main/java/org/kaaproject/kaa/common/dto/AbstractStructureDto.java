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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public abstract class AbstractStructureDto implements HasId, Serializable {

    private static final long serialVersionUID = -983834466038615147L;

    protected String id;
    protected String applicationId;
    protected String endpointGroupId;
    protected int sequenceNumber;
    protected String description;
    protected long createdTime;
    protected long lastModifyTime;
    protected long activatedTime;
    protected long deactivatedTime;
    protected String createdUsername;
    protected String modifiedUsername;
    protected String activatedUsername;
    protected String deactivatedUsername;
    protected String body;
    protected UpdateStatus status;
    protected long endpointCount;
    protected Long version;

    public AbstractStructureDto() {
    }

    public AbstractStructureDto(AbstractStructureDto other) {
        this.id = other.id;
        this.applicationId = other.applicationId;
        this.endpointGroupId = other.endpointGroupId;
        this.sequenceNumber = other.sequenceNumber;
        this.description = other.description;
        this.createdTime = other.createdTime;
        this.lastModifyTime = other.lastModifyTime;
        this.activatedTime = other.activatedTime;
        this.deactivatedTime = other.deactivatedTime;
        this.createdUsername = other.createdUsername;
        this.modifiedUsername = other.modifiedUsername;
        this.activatedUsername = other.activatedUsername;
        this.deactivatedUsername = other.deactivatedUsername;
        this.body = other.body;
        this.status = other.status;
        this.endpointCount = other.endpointCount;
        this.version = other.version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(String endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateStatus status) {
        this.status = status;
    }

    public long getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(long endpointCount) {
        this.endpointCount = endpointCount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractStructureDto that = (AbstractStructureDto) o;

        if (lastModifyTime != that.lastModifyTime) {
            return false;
        }
        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (body != null ? !body.equals(that.body) : that.body != null) {
            return false;
        }
        if (endpointGroupId != null ? !endpointGroupId.equals(that.endpointGroupId) : that.endpointGroupId != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (endpointGroupId != null ? endpointGroupId.hashCode() : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + (int) (lastModifyTime ^ (lastModifyTime >>> 32));
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractStructureDto{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", endpointGroupId='" + endpointGroupId + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", lastModifyTime=" + lastModifyTime +
                ", status=" + status +
                ", version=" + version +
                '}';
    }

    public int incrementSeqNum() {
        return ++sequenceNumber;
    }
}
