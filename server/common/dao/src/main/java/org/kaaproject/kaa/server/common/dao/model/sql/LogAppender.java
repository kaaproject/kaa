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

import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.*;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;

import javax.persistence.Column;
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
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.server.common.dao.model.sql.avro.DaoAvroUtil;

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

    @ManyToOne
    @JoinColumn(name = LOG_APPENDER_LOG_SCHEMA_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LogSchema logSchema;

    @Column(name = LOG_APPENDER_STATUS)
    @Enumerated(EnumType.STRING)
    private LogAppenderStatusDto status;

    @Column(name = LOG_APPENDER_TYPE)
    @Enumerated(EnumType.STRING)
    private LogAppenderTypeDto type;

    @Column(name = LOG_APPENDER_DESCRIPTION, length = 1000)
    protected String description;

    @Column(name = LOG_APPENDER_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = LOG_APPENDER_CREATED_TIME)
    protected long createdTime;


    @Lob
    @Column(name = LOG_APPENDER_PROPERTIES)
    private byte[] properties;

    public LogAppender() {
    }

    public LogAppender(LogAppenderDto dto) {
        if (dto != null) {
            this.id = getLongId(dto.getId());
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            Long schemaId = getLongId(dto.getSchema());
            this.logSchema = schemaId != null ? new LogSchema(schemaId) : null;
            this.name = dto.getName();
            this.status = dto.getStatus();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            this.type = dto.getType();
            this.properties = DaoAvroUtil.convertParametersToBytes(dto.getProperties());
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

    public LogSchema getLogSchema() {
        return logSchema;
    }

    public void setLogSchema(LogSchema logSchema) {
        this.logSchema = logSchema;
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

    public byte[] getProperties() {
        return properties;
    }

    public void setProperties(byte[] properties) {
        this.properties = properties;
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
        dto.setSchema(new SchemaDto(logSchema.getStringId(), logSchema.getMajorVersion(), logSchema.getMinorVersion()));
        dto.setStatus(status);
        dto.setType(type);
        dto.setProperties(DaoAvroUtil.convertParametersFromBytes(properties));
        return dto;
    }

    @Override
    protected LogAppenderDto createDto() {
        return new LogAppenderDto();
    }

	@Override
	public String toString() {
		return "LogAppender [name=" + name + ", status=" + status + ", type="
				+ type + ", description=" + description + ", createdUsername="
				+ createdUsername + ", createdTime=" + createdTime + "]";
	}
}
