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

public class LogAppenderRestDto extends AbstractDetailDto implements HasId, Serializable {

    private static final long serialVersionUID = 8035147059935996619L;

    private String id;
    private String applicationId;
    private String applicationToken;
    private String tenantId;
    private SchemaDto schema;
    private LogAppenderStatusDto status;
    private String typeName;
    private String appenderClassName;
    private String configuration;
    private List<LogHeaderStructureDto> headerStructure;
    
    public LogAppenderRestDto() {
        super();
    }
    
    public LogAppenderRestDto(LogAppenderDto logAppenderDto) {
        super(logAppenderDto);
        this.id = logAppenderDto.getId();
        this.applicationId = logAppenderDto.getApplicationId();
        this.applicationToken = logAppenderDto.getApplicationToken();
        this.tenantId = logAppenderDto.getTenantId();
        this.schema = logAppenderDto.getSchema();
        this.status = logAppenderDto.getStatus();
        this.typeName = logAppenderDto.getTypeName();
        this.appenderClassName = logAppenderDto.getAppenderClassName();
        this.headerStructure = logAppenderDto.getHeaderStructure();
    }
    
    public LogAppenderDto toLogAppenderDto() {
        LogAppenderDto logAppenderDto = new LogAppenderDto(this);
        logAppenderDto.setId(this.id);
        logAppenderDto.setApplicationId(this.applicationId);
        logAppenderDto.setApplicationToken(this.applicationToken);
        logAppenderDto.setTenantId(this.tenantId);
        logAppenderDto.setSchema(this.schema);
        logAppenderDto.setStatus(this.status);
        logAppenderDto.setTypeName(this.typeName);
        logAppenderDto.setAppenderClassName(this.appenderClassName);
        logAppenderDto.setHeaderStructure(this.headerStructure);
        return logAppenderDto;
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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getAppenderClassName() {
        return appenderClassName;
    }

    public void setAppenderClassName(String appenderClassName) {
        this.appenderClassName = appenderClassName;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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
        result = prime
                * result
                + ((appenderClassName == null) ? 0 : appenderClassName
                        .hashCode());
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result
                + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result
                + ((headerStructure == null) ? 0 : headerStructure.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result
                + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogAppenderRestDto other = (LogAppenderRestDto) obj;
        if (appenderClassName == null) {
            if (other.appenderClassName != null) {
                return false;
            }
        } else if (!appenderClassName.equals(other.appenderClassName)) {
            return false;
        }
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
            return false;
        }
        if (headerStructure == null) {
            if (other.headerStructure != null) {
                return false;
            }
        } else if (!headerStructure.equals(other.headerStructure)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        if (typeName == null) {
            if (other.typeName != null) {
                return false;
            }
        } else if (!typeName.equals(other.typeName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LogAppenderRestDto [id=" + id + ", applicationId="
                + applicationId + ", applicationToken=" + applicationToken
                + ", tenantId=" + tenantId + ", schema=" + schema + ", status="
                + status + ", typeName=" + typeName + ", appenderClassName="
                + appenderClassName + ", configuration=" + configuration
                + ", headerStructure=" + headerStructure + "]";
    }

}