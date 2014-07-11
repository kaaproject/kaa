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

package org.kaaproject.kaa.server.common.dao.model.mongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.io.Serializable;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public final class LogEvent  implements ToDto<LogEventDto>, Serializable {

    private static final long serialVersionUID = 5104926263610142098L;
    
    @Id
    private String id;
    @Indexed
    @Field("endpoint_key")
    private String endpointKey;
    @Field("date_created")
    private long dateCreated;
    private DBObject event;

    public LogEvent() {
        
    }

    public LogEvent(LogEventDto dto) {
        this.id = dto.getId();
        this.endpointKey = dto.getEndpointKey();
        this.dateCreated = dto.getDateCreated();
        this.event = (DBObject) JSON.parse(dto.getEvent());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public DBObject getEvent() {
        return event;
    }

    public void setEvent(DBObject event) {
        this.event = event;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogEvent that = (LogEvent) o;
        
        if (endpointKey != null ? !endpointKey.equals(that.endpointKey) : that.endpointKey != null) {
            return false;
        }
        if (dateCreated != that.dateCreated) {
            return false;
        }
        if (event != null ? !event.equals(that.event) : that.event != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = endpointKey != null ? endpointKey.hashCode() : 0;
        result = 31 * result + (int) dateCreated;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogEventPack{" +
                "id='" + id + '\'' +
                ", endpointKey='" + endpointKey + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", event='" + event +
                '}';
    }

    @Override
    public LogEventDto toDto() {
        LogEventDto dto = new LogEventDto();
        dto.setId(id);
        dto.setEndpointKey(endpointKey);
        dto.setDateCreated(dateCreated);
        dto.setEvent(event != null ? event.toString() : null);
        return dto;
    }
}