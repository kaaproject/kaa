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

import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.*;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = EVENT_CLASS_FAMILY_VERSION_TABLE_NAME)
public class EventClassFamilyVersion extends GenericModel<EventClassFamilyVersionDto> {

    private static final long serialVersionUID = -7490111487256831990L;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = EVENT_CLASS_EVENT_CLASS_FAMILY_VERSION_ID, nullable = false)
    private List<EventClass> records;

    @Column(name = EVENT_CLASS_FAMILY_VERSION_VERSION)
    private int version;

    @Column(name = EVENT_CLASS_FAMILY_VERSION_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = EVENT_CLASS_FAMILY_VERSION_CREATED_TIME)
    protected long createdTime;

    public EventClassFamilyVersion() {
    }

    public EventClassFamilyVersion(EventClassFamilyVersionDto dto) {
        this.id = getLongId(dto.getId());
        this.version = dto.getVersion();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();

        if (dto.getRecords() != null) {
            this.records = new ArrayList<>(dto.getRecords().size());
            for (EventClassDto record : dto.getRecords()) {
                this.records.add(new EventClass(record));
            }
        }
    }

    public EventClassFamilyVersion(Long id) {
        this.id = id;
    }

    public List<EventClass> getRecords() {
        return records;
    }

    public void setRecords(List<EventClass> records) {
        this.records = records;
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
        result = prime * result + version;
        result = prime * result + ((records == null) ? 0 : records.hashCode());
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
        EventClassFamilyVersion other = (EventClassFamilyVersion) obj;
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
        } else if (records == null) {
            if (other.records != null) {
                return false;
            }
        } else if (!records.equals(other.records)) {
            return false;
        }
        return version == other.version;
    }

    @Override
    protected EventClassFamilyVersionDto createDto() {
        return new EventClassFamilyVersionDto();
    }

    @Override
    protected GenericModel<EventClassFamilyVersionDto> newInstance(Long id) {
        return new EventClassFamilyVersion(id);
    }

    @Override
    public EventClassFamilyVersionDto toDto() {
        EventClassFamilyVersionDto dto = createDto();
        dto.setId(getStringId());
        dto.setVersion(version);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);

        if (records != null) {
            List<EventClassDto> recordsDto = new ArrayList<>(records.size());
            for (EventClass record : records) {
                recordsDto.add(record.toDto());
            }
            dto.setRecords(recordsDto);
        }

        return dto;
    }

    @Override
    public String toString() {
        return "EventClassFamilyVersion [version=" + version + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime
                + ", id=" + id + "]";
    }
}
