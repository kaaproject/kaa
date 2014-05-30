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

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.AbstractStructureDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * The Class AbstractStructure.
 *
 * @param <T> the generic type
 */
public abstract class AbstractStructure<T extends AbstractStructureDto> implements ToDto<T>, Serializable {

    private static final long serialVersionUID = -1626213843897829497L;

    @Id
    protected String id;
    @Field("application_id")
    protected ObjectId applicationId;
    @Field("schema_id")
    protected ObjectId schemaId;
    @Field("endpoint_group_id")
    protected ObjectId endpointGroupId;
    @Field("seq_num")
    protected int sequenceNumber;
    @Field("major_version")
    protected int majorVersion;
    @Field("minor_version")
    protected int minorVersion;
    @Field("description")
    protected String description;
    @Field("created_time")
    protected long createdTime;
    @Field("last_modify_time")
    protected long lastModifyTime;
    @Field("activated_time")
    protected long activatedTime;
    @Field("deactivated_time")
    protected long deactivatedTime;
    @Field("created_username")
    protected String createdUsername;
    @Field("modified_username")
    protected String modifiedUsername;
    @Field("activated_username")
    protected String activatedUsername;
    @Field("deactivated_username")
    protected String deactivatedUsername;
    protected UpdateStatus status;
    protected String body;
    @Field("endpoint_count")
    protected long endpointCount;

    public AbstractStructure() {
    }

    public AbstractStructure(T dto) {
        this.id = dto.getId();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.schemaId = idToObjectId(dto.getSchemaId());
        this.endpointGroupId = idToObjectId(dto.getEndpointGroupId());
        this.sequenceNumber = dto.getSequenceNumber();
        this.majorVersion = dto.getMajorVersion();
        this.minorVersion = dto.getMinorVersion();
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
        this.body = dto.getBody();
        this.endpointCount = dto.getEndpointCount();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public ObjectId getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(ObjectId schemaId) {
        this.schemaId = schemaId;
    }

    public ObjectId getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(ObjectId endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateStatus status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

        AbstractStructure<?> that = (AbstractStructure<?>) o;

        if (lastModifyTime != that.lastModifyTime) {
            return false;
        }
        if (majorVersion != that.majorVersion) {
            return false;
        }
        if (minorVersion != that.minorVersion) {
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
        if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) {
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
        result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
        result = 31 * result + (endpointGroupId != null ? endpointGroupId.hashCode() : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + (int) (lastModifyTime ^ (lastModifyTime >>> 32));
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractStructure{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", profileSchemaId=" + schemaId +
                ", endpointGroupId=" + endpointGroupId +
                ", sequenceNumber=" + sequenceNumber +
                ", majorVersion=" + majorVersion +
                ", minorVersion=" + minorVersion +
                ", lastModifyTime=" + lastModifyTime +
                ", status=" + status +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public T toDto() {
        T dto = createDto();
        dto.setId(id);
        dto.setApplicationId(idToString(applicationId));
        dto.setSchemaId(idToString(schemaId));
        dto.setEndpointGroupId(idToString(endpointGroupId));
        dto.setSequenceNumber(sequenceNumber);
        dto.setMajorVersion(majorVersion);
        dto.setMinorVersion(minorVersion);
        dto.setDescription(description);
        dto.setCreatedTime(createdTime);
        dto.setLastModifyTime(lastModifyTime);
        dto.setActivatedTime(activatedTime);
        dto.setDeactivatedTime(deactivatedTime);
        dto.setCreatedUsername(createdUsername);
        dto.setModifiedUsername(modifiedUsername);
        dto.setActivatedUsername(activatedUsername);
        dto.setDeactivatedUsername(deactivatedUsername);
        dto.setBody(body);
        dto.setStatus(status);
        dto.setEndpointCount(endpointCount);
        return dto;
    }

    protected abstract T createDto();

}
