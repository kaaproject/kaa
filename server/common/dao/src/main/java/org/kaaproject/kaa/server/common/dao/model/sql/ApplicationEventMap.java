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
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_MAP_ACTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_MAP_EVENT_CLASS_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_MAP_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_EVENT_MAP_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = APPLICATION_EVENT_MAP_TABLE_NAME)
public class ApplicationEventMap extends GenericModel<ApplicationEventMapDto> {

    private static final long serialVersionUID = 3766947955702551264L;

    @ManyToOne
    @JoinColumn(name = APPLICATION_EVENT_MAP_EVENT_CLASS_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventClass eventClass;

    @Column(name = APPLICATION_EVENT_MAP_FQN)
    private String fqn;

    @Column(name = APPLICATION_EVENT_MAP_ACTION)
    @Enumerated(EnumType.STRING)
    private ApplicationEventAction action;

    public ApplicationEventMap() {
    }

    public ApplicationEventMap(Long id) {
        this.id = id;
    }

    public ApplicationEventMap(ApplicationEventMapDto dto) {
        this.id = getLongId(dto.getId());
        Long eventClassId = getLongId(dto.getEventClassId());
        if (eventClassId != null) {
            this.eventClass = new EventClass(eventClassId);
        }
        this.fqn = dto.getFqn();
        this.action = dto.getAction();
    }

    public EventClass getEventClass() {
        return eventClass;
    }

    public void setEventClass(EventClass eventClass) {
        this.eventClass = eventClass;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public ApplicationEventAction getAction() {
        return action;
    }

    public void setAction(ApplicationEventAction action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((eventClass == null) ? 0 : eventClass.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
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
        ApplicationEventMap other = (ApplicationEventMap) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        if (eventClass == null) {
            if (other.eventClass != null) {
                return false;
            }
        } else if (!eventClass.equals(other.eventClass)) {
            return false;
        }
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        return true;
    }

    @Override
    protected ApplicationEventMapDto createDto() {
        return new ApplicationEventMapDto();
    }

    @Override
    protected GenericModel<ApplicationEventMapDto> newInstance(Long id) {
        return new ApplicationEventMap(id);
    }

    @Override
    public ApplicationEventMapDto toDto() {
        ApplicationEventMapDto dto = createDto();
        dto.setId(getStringId());
        if (eventClass != null) {
            dto.setEventClassId(eventClass.getStringId());
        }
        dto.setFqn(fqn);
        dto.setAction(action);
        return dto;
    }

    @Override
    public String toString() {
        return "ApplicationEventMap [fqn=" + fqn + ", action=" + action + ", id=" + id + "]";
    }

}
