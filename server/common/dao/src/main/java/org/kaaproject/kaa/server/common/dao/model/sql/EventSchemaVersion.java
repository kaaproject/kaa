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

import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_SCHEMA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_VERSION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = EVENT_SCHEMA_VERSION_TABLE_NAME)
public class EventSchemaVersion extends GenericModel<EventSchemaVersionDto> {

    private static final long serialVersionUID = -7490111487256831990L;

    @Lob
    @Column(name = EVENT_SCHEMA_VERSION_SCHEMA)
    private String schema;

    @Column(name = EVENT_SCHEMA_VERSION_VERSION)
    private int version;

    @Column(name = EVENT_SCHEMA_VERSION_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = EVENT_SCHEMA_VERSION_CREATED_TIME)
    protected long createdTime;

    public EventSchemaVersion() {
    }

    public EventSchemaVersion(EventSchemaVersionDto dto) {
        this.id = getLongId(dto.getId());
        this.schema = dto.getSchema();
        this.version = dto.getVersion();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
    }

    public EventSchemaVersion(Long id) {
        this.id = id;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + version;
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
        EventSchemaVersion other = (EventSchemaVersion) obj;
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
        return version == other.version;
    }

    @Override
    protected EventSchemaVersionDto createDto() {
        return new EventSchemaVersionDto();
    }

    @Override
    protected GenericModel<EventSchemaVersionDto> newInstance(Long id) {
        return new EventSchemaVersion(id);
    }

    @Override
    public EventSchemaVersionDto toDto() {
        EventSchemaVersionDto dto = createDto();
        dto.setId(getStringId());
        dto.setVersion(version);
        dto.setSchema(schema);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        return dto;
    }

    @Override
    public String toString() {
        return "EventSchemaVersion [schema=" + schema + ", version=" + version + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime
                + ", id=" + id + "]";
    }
}
