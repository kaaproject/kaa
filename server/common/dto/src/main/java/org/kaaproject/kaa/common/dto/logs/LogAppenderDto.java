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

package org.kaaproject.kaa.common.dto.logs;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.AbstractDetailDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;

public class LogAppenderDto extends AbstractDetailDto implements HasId, Serializable {

    private static final long serialVersionUID = 8035147059935996619L;

    private String id;
    private String applicationId;
    private String applicationToken;
    private String tenantId;
    private SchemaDto schema;
    private LogAppenderStatusDto status;
    private LogAppenderTypeDto type;
    private LogAppenderParametersDto properties;
    private List<LogHeaderStructureDto> headerStructure;

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

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public SchemaDto getSchema() {
        return schema;
    }

    public void setSchema(SchemaDto schema) {
        this.schema = schema;
    }

    public LogAppenderStatusDto getStatus() {
        return status;
    }

    public void setStatus(LogAppenderStatusDto status) {
        this.status = status;
    }

    public LogAppenderTypeDto getType() {
        return type;
    }

    public void setType(LogAppenderTypeDto type) {
        this.type = type;
    }

    public LogAppenderParametersDto getProperties() {
        return properties;
    }

    public void setProperties(LogAppenderParametersDto properties) {
        this.properties = properties;
    }

    public List<LogHeaderStructureDto> getHeaderStructure() {
        return headerStructure;
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        this.headerStructure = headerStructure;
    }

    public String getSchemaVersion() {
        StringBuilder version = new StringBuilder();
        if (schema != null) {
            version.append(schema.getMajorVersion()).append(".").append(schema.getMinorVersion());
        }
        return version.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result
                + ((headerStructure == null) ? 0 : headerStructure.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogAppenderDto other = (LogAppenderDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        if (applicationToken == null) {
            if (other.applicationToken != null)
                return false;
        } else if (!applicationToken.equals(other.applicationToken))
            return false;
        if (headerStructure == null) {
            if (other.headerStructure != null)
                return false;
        } else if (!headerStructure.equals(other.headerStructure))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (schema == null) {
            if (other.schema != null)
                return false;
        } else if (!schema.equals(other.schema))
            return false;
        if (status != other.status)
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogAppenderDto [id=" + id + ", applicationId=" + applicationId
                + ", applicationToken=" + applicationToken + ", tenantId="
                + tenantId + ", schema=" + schema + ", status=" + status
                + ", type=" + type + ", properties=" + properties
                + ", headerStructure=" + headerStructure + "]";
    }

}