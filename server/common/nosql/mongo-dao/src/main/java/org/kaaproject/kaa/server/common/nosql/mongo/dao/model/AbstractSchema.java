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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * The Class AbstractSchema.
 *
 * @param <T> the generic type
 */
public abstract class AbstractSchema<T extends AbstractSchemaDto> implements ToDto<T>, Serializable {

    private static final long serialVersionUID = -5456898222268100468L;

    @Id
    protected String id;
    @Field("application_id")
    protected String applicationId;
    @Field("major_version")
    protected int majorVersion;
    @Field("minor_version")
    protected int minorVersion;
    protected String schema;
    @Field("name")
    protected String name;
    @Field("description")
    protected String description;
    @Field("created_username")
    protected String createdUsername;
    @Field("created_time")
    protected long createdTime;
    @Field("endpoint_count")
    protected long endpointCount;

    public AbstractSchema() {
    }

    public AbstractSchema(T dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.majorVersion = dto.getMajorVersion();
        this.minorVersion = dto.getMinorVersion();
        this.schema = dto.getSchema();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        this.endpointCount = dto.getEndpointCount();
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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
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
        if (!(o instanceof AbstractSchema)) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }

        AbstractSchema<?> that = (AbstractSchema<?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (majorVersion != that.majorVersion) {
            return false;
        }
        if (minorVersion != that.minorVersion) {
            return false;
        }
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractSchema [id=" + id + ", applicationId=" + applicationId
                + ", majorVersion=" + majorVersion + ", minorVersion="
                + minorVersion + ", schema=" + schema + ", name=" + name
                + ", description=" + description + ", createdUsername="
                + createdUsername + ", createdTime=" + createdTime
                + ", endpointCount=" + endpointCount + "]";
    }

    @Override
    public T toDto() {
        T dto = createDto();
        dto.setId(id);
        dto.setApplicationId(applicationId);
        dto.setMajorVersion(majorVersion);
        dto.setMinorVersion(minorVersion);
        dto.setSchema(schema);
        dto.setName(name);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        dto.setEndpointCount(endpointCount);
        return dto;
    }

    public SchemaDto toSchemaDto() {
        SchemaDto dto = new SchemaDto();
        dto.setId(id);
        dto.setMajorVersion(majorVersion);
        dto.setMinorVersion(minorVersion);
        return dto;
    }

    protected abstract T createDto();
}
