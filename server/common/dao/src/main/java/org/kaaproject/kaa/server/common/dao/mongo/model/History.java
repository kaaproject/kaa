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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

@Document(collection = History.COLLECTION_NAME)
@CompoundIndex(name = "app_sn_idx", def = "{'application_id' : 1, 'sequence_number' : 1}")
public final class History implements ToDto<HistoryDto>, Serializable {

    private static final long serialVersionUID = -6812542781712914401L;

    public static final String COLLECTION_NAME = "history";

    @Field("_id")
    private String id;
    @Field("seq_num")
    private int sequenceNumber;
    @Field("application_id")
    private ObjectId applicationId;
    private Change change;
    @Field("last_modify_time")
    private long lastModifyTime;

    public History() {
    }

    public History(HistoryDto dto) {
        this.id = dto.getId();
        this.sequenceNumber = dto.getSequenceNumber();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.change = new Change(dto.getChange());
        this.lastModifyTime = dto.getLastModifyTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        History history = (History) o;

        if (lastModifyTime != history.lastModifyTime) {
            return false;
        }
        if (sequenceNumber != history.sequenceNumber) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(history.applicationId) : history.applicationId != null) {
            return false;
        }
        if (change != null ? !change.equals(history.change) : history.change != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sequenceNumber;
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (change != null ? change.hashCode() : 0);
        result = 31 * result + (int) (lastModifyTime ^ (lastModifyTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "History{" +
                "id='" + id + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", applicationId=" + applicationId +
                ", change=" + change +
                ", lastModifyTime=" + lastModifyTime +
                '}';
    }

    @Override
    public HistoryDto toDto() {
        HistoryDto dto = new HistoryDto();
        dto.setId(id);
        dto.setApplicationId(idToString(applicationId));
        dto.setChange(change != null ? change.toDto() : null);
        dto.setSequenceNumber(sequenceNumber);
        dto.setLastModifyTime(lastModifyTime);
        return dto;
    }
}
