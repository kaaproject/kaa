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
import org.kaaproject.kaa.common.dto.HistoryDto;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.HISTORY_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.HISTORY_CHANGE_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.HISTORY_LAST_MODIFY_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.HISTORY_SEQUENCE_NUMBER;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.HISTORY_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = HISTORY_TABLE_NAME)
public class History extends GenericModel<HistoryDto> implements Serializable {

    private static final long serialVersionUID = 2061028534480732230L;

    @Column(name = HISTORY_SEQUENCE_NUMBER)
    private int sequenceNumber;

    @Column(name = HISTORY_LAST_MODIFY_TIME)
    private long lastModifyTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = HISTORY_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = HISTORY_CHANGE_ID, nullable = false)
    private Change change;

    public History() {
    }

    public History(Long id) {
        this.id = id;
    }

    public History(HistoryDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.sequenceNumber = dto.getSequenceNumber();
            this.change = new Change(dto.getChange());
            this.lastModifyTime = dto.getLastModifyTime();
            Long applicationId = getLongId(dto.getApplicationId());
            if (applicationId != null) {
                this.application = new Application(applicationId);
            }
        }
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    @Override
    protected HistoryDto createDto() {
        return new HistoryDto();
    }

    @Override
    protected GenericModel<HistoryDto> newInstance(Long id) {
        return new History(id);
    }

    @Override
    public HistoryDto toDto() {
        HistoryDto dto = createDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        dto.setChange(change != null ? change.toDto() : null);
        dto.setSequenceNumber(sequenceNumber);
        dto.setLastModifyTime(lastModifyTime);
        return dto;
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + sequenceNumber;
        result = prime * result + Long.valueOf(lastModifyTime).hashCode();
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + ((change == null) ? 0 : change.hashCode());
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
        History other = (History) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (lastModifyTime != other.lastModifyTime) {
            return false;
        }
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
        if (change == null) {
            if (other.change != null) {
                return false;
            }
        } else if (!change.equals(other.change)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "History [sequenceNumber=" + sequenceNumber + ", lastModifyTime=" + lastModifyTime + ", change=" + change + ", id=" + id + "]";
    }

}
