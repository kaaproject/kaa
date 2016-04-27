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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_EVENT_CLASS_FAMILY_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_FAMILY_MAP_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_MAP_APPLICATION_EVENT_FAMILY_MAP_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = APPLICATION_EVENT_FAMILY_MAP_TABLE_NAME)
public class ApplicationEventFamilyMap extends GenericModel<ApplicationEventFamilyMapDto> {

    private static final long serialVersionUID = 3766947955702551264L;

    @ManyToOne
    @JoinColumn(name = APPLICATION_EVENT_FAMILY_MAP_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @ManyToOne
    @JoinColumn(name = APPLICATION_EVENT_FAMILY_MAP_EVENT_CLASS_FAMILY_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventClassFamily ecf;

    @Column(name = APPLICATION_EVENT_FAMILY_MAP_VERSION)
    private int version;

    @Column(name = APPLICATION_EVENT_FAMILY_MAP_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = APPLICATION_EVENT_FAMILY_MAP_CREATED_TIME)
    protected long createdTime;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = APPLICATION_EVENT_MAP_APPLICATION_EVENT_FAMILY_MAP_ID, nullable = false)
    private List<ApplicationEventMap> eventMaps;

    public ApplicationEventFamilyMap() {
    }

    public ApplicationEventFamilyMap(Long id) {
        this.id = id;
    }

    public ApplicationEventFamilyMap(ApplicationEventFamilyMapDto dto) {
        this.id = getLongId(dto.getId());
        Long applicationId = getLongId(dto.getApplicationId());
        if (applicationId != null) {
            this.application = new Application(applicationId);
        }
        Long ecfId = getLongId(dto.getEcfId());
        if (ecfId != null) {
            this.ecf = new EventClassFamily(ecfId);
        }
        this.version = dto.getVersion();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        if (dto.getEventMaps() != null) {
            this.eventMaps = new ArrayList<>(dto.getEventMaps().size());
            for (ApplicationEventMapDto eventMap : dto.getEventMaps()) {
                this.eventMaps.add(new ApplicationEventMap(eventMap));
            }
        }
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public EventClassFamily getEcf() {
        return ecf;
    }

    public void setEcf(EventClassFamily ecf) {
        this.ecf = ecf;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<ApplicationEventMap> getEventMaps() {
        return eventMaps;
    }

    public void setEventMaps(List<ApplicationEventMap> eventMaps) {
        this.eventMaps = eventMaps;
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
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((ecf == null) ? 0 : ecf.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        ApplicationEventFamilyMap other = (ApplicationEventFamilyMap) obj;
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
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
        if (ecf == null) {
            if (other.ecf != null) {
                return false;
            }
        } else if (!ecf.equals(other.ecf)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return version == other.version;
    }

    @Override
    protected ApplicationEventFamilyMapDto createDto() {
        return new ApplicationEventFamilyMapDto();
    }

    @Override
    protected GenericModel<ApplicationEventFamilyMapDto> newInstance(Long id) {
        return new ApplicationEventFamilyMap(id);
    }

    @Override
    public ApplicationEventFamilyMapDto toDto() {
        ApplicationEventFamilyMapDto dto = createDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        if (ecf != null) {
            dto.setEcfId(ecf.getStringId());
            dto.setEcfName(ecf.getName());
        }
        dto.setVersion(version);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        if (eventMaps != null) {
            List<ApplicationEventMapDto> eventMapsDto = new ArrayList<>(eventMaps.size());
            for (ApplicationEventMap eventMap : eventMaps) {
                eventMapsDto.add(eventMap.toDto());
            }
            dto.setEventMaps(eventMapsDto);
        }
        return dto;
    }

    @Override
    public String toString() {
        return "ApplicationEventFamilyMap [ecf=" + ecf + ", version=" + version + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime
                + ", id=" + id + "]";
    }

}
