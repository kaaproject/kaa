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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_APPENDER_CLASS_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_MAX_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_MIN_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_CONFIRM_DELIVERY;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_RAW_CONFIGURATION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_STATUS;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.LOG_APPENDER_TYPE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;

@Entity
@Table(name = LOG_APPENDER_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public final class LogAppender extends GenericModel<LogAppenderDto> implements Serializable {

    private static final long serialVersionUID = 8884800929390746097L;

    @Column(name = LOG_APPENDER_NAME)
    private String name;

    @ManyToOne
    @JoinColumn(name = LOG_APPENDER_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @Column(name = LOG_APPENDER_MIN_LOG_SCHEMA_VERSION)
    private int minLogSchemaVersion;

    @Column(name = LOG_APPENDER_MAX_LOG_SCHEMA_VERSION)
    private int maxLogSchemaVersion;
    
    @Column(name = LOG_APPENDER_CONFIRM_DELIVERY)
    private boolean confirmDelivery;
    
    @Column(name = LOG_APPENDER_STATUS)
    @Enumerated(EnumType.STRING)
    private LogAppenderStatusDto status;

    @Column(name = LOG_APPENDER_DESCRIPTION, length = 1000)
    private String description;

    @Column(name = LOG_APPENDER_CREATED_USERNAME)
    private String createdUsername;

    @Column(name = LOG_APPENDER_CREATED_TIME)
    private long createdTime;

    @Column(name = LOG_APPENDER_TYPE_NAME)
    private String typeName;
    
    @Column(name = LOG_APPENDER_APPENDER_CLASS_NAME)
    private String appenderClassName;

    @Lob
    @Column(name = LOG_APPENDER_RAW_CONFIGURATION)
    private byte[] rawConfiguration;

    @ElementCollection(targetClass = LogHeaderStructureDto.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "header_structure")
    @Column(name = "structure_field", nullable = false)
    private List<LogHeaderStructureDto> headerStructure;

    public LogAppender() {
    }

    public LogAppender(LogAppenderDto dto) {
        if (dto != null) {
            this.id = getLongId(dto.getId());
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            this.minLogSchemaVersion = dto.getMinLogSchemaVersion();
            this.maxLogSchemaVersion = dto.getMaxLogSchemaVersion();
            this.confirmDelivery = dto.isConfirmDelivery();
            this.name = dto.getName();
            this.status = dto.getStatus();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            this.typeName = dto.getTypeName();
            this.appenderClassName = dto.getAppenderClassName();
            this.rawConfiguration = dto.getRawConfiguration();
            this.headerStructure = dto.getHeaderStructure();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public int getMinLogSchemaVersion() {
        return minLogSchemaVersion;
    }

    public void setMinLogSchemaVersion(int minLogSchemaVersion) {
        this.minLogSchemaVersion = minLogSchemaVersion;
    }

    public int getMaxLogSchemaVersion() {
        return maxLogSchemaVersion;
    }

    public void setMaxLogSchemaVersion(int maxLogSchemaVersion) {
        this.maxLogSchemaVersion = maxLogSchemaVersion;
    }

    public boolean isConfirmDelivery() {
		return confirmDelivery;
	}

	public void setConfirmDelivery(boolean confirmDelivery) {
		this.confirmDelivery = confirmDelivery;
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

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
    }

    public List<LogHeaderStructureDto> getHeaderStructure() {
        return headerStructure;
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        this.headerStructure = headerStructure;
    }

    @Override
    public LogAppenderDto toDto() {
        LogAppenderDto dto = createDto();
        dto.setId(getStringId());
        dto.setApplicationId(application.getStringId());
        dto.setApplicationToken(application.getApplicationToken());
        dto.setTenantId(application.getTenant().getStringId());
        dto.setName(name);
        dto.setCreatedTime(createdTime);
        dto.setCreatedUsername(createdUsername);
        dto.setDescription(description);
        dto.setMinLogSchemaVersion(minLogSchemaVersion);
        dto.setMaxLogSchemaVersion(maxLogSchemaVersion);
        dto.setConfirmDelivery(confirmDelivery);
        dto.setStatus(status);
        dto.setTypeName(typeName);
        dto.setAppenderClassName(appenderClassName);
        dto.setRawConfiguration(rawConfiguration);
        dto.setHeaderStructure(headerStructure != null ? new ArrayList<>(headerStructure) : new ArrayList<LogHeaderStructureDto>());
        return dto;
    }

    @Override
    protected LogAppenderDto createDto() {
        return new LogAppenderDto();
    }

    @Override
    public String toString() {
        return "LogAppender [name=" + name + ", application=" + application
                + ", minLogSchemaVersion=" + minLogSchemaVersion
                + ", maxLogSchemaVersion=" + maxLogSchemaVersion
                + ", confirmDelivery=" + confirmDelivery + ", status="
                + status + ", description=" + description
                + ", createdUsername=" + createdUsername + ", createdTime="
                + createdTime + ", typeName=" + typeName
                + ", appenderClassName=" + appenderClassName
                + ", rawConfiguration=" + Arrays.toString(rawConfiguration)
                + ", headerStructure=" + headerStructure + "]";
    }

}
