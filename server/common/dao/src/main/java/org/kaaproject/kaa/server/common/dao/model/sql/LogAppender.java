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

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_CONFIRM_DELIVERY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_MAX_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_MIN_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_TABLE_NAME;

@Entity
@Table(name = LOG_APPENDER_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public class LogAppender extends Plugin<LogAppenderDto> implements Serializable {

    private static final long serialVersionUID = 8884800929390746097L;

    @Column(name = LOG_APPENDER_MIN_LOG_SCHEMA_VERSION)
    private int minLogSchemaVersion;

    @Column(name = LOG_APPENDER_MAX_LOG_SCHEMA_VERSION)
    private int maxLogSchemaVersion;

    @Column(name = LOG_APPENDER_CONFIRM_DELIVERY)
    private boolean confirmDelivery;

    @ElementCollection(targetClass = LogHeaderStructureDto.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "header_structure")
    @Column(name = "structure_field", nullable = false)
    private List<LogHeaderStructureDto> headerStructure;

    public LogAppender() {
        super();
    }
    
    public LogAppender(Long id) {
        this.id = id;
    }

    public LogAppender(LogAppenderDto dto) {
        super(dto);
        if (dto != null) {
            this.minLogSchemaVersion = dto.getMinLogSchemaVersion();
            this.maxLogSchemaVersion = dto.getMaxLogSchemaVersion();
            this.confirmDelivery = dto.isConfirmDelivery();
            this.headerStructure = dto.getHeaderStructure();
        }
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

    public List<LogHeaderStructureDto> getHeaderStructure() {
        return headerStructure;
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        this.headerStructure = headerStructure;
    }

    @Override
    public LogAppenderDto toDto() {
        LogAppenderDto dto = super.toDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationToken(application.getApplicationToken());
            dto.setTenantId(application.getTenant().getStringId());
        }
        dto.setMinLogSchemaVersion(minLogSchemaVersion);
        dto.setMaxLogSchemaVersion(maxLogSchemaVersion);
        dto.setConfirmDelivery(confirmDelivery);
        dto.setHeaderStructure(headerStructure != null ? new ArrayList<>(headerStructure) : new ArrayList<LogHeaderStructureDto>());
        return dto;
    }

    @Override
    protected LogAppenderDto createDto() {
        return new LogAppenderDto();
    }

    @Override
    protected GenericModel<LogAppenderDto> newInstance(Long id) {
        return new LogAppender(id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (confirmDelivery ? 1231 : 1237);
        result = prime * result
                + ((headerStructure == null) ? 0 : headerStructure.hashCode());
        result = prime * result + maxLogSchemaVersion;
        result = prime * result + minLogSchemaVersion;
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
        LogAppender other = (LogAppender) obj;
        if (confirmDelivery != other.confirmDelivery) {
            return false;
        }
        if (headerStructure == null) {
            if (other.headerStructure != null) {
                return false;
            }
        } else if (!headerStructure.equals(other.headerStructure)) {
            return false;
        }
        if (maxLogSchemaVersion != other.maxLogSchemaVersion) {
            return false;
        }
        if (minLogSchemaVersion != other.minLogSchemaVersion) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogAppender [minLogSchemaVersion=");
        builder.append(minLogSchemaVersion);
        builder.append(", maxLogSchemaVersion=");
        builder.append(maxLogSchemaVersion);
        builder.append(", confirmDelivery=");
        builder.append(confirmDelivery);
        builder.append(", headerStructure=");
        builder.append(headerStructure);
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }



}
