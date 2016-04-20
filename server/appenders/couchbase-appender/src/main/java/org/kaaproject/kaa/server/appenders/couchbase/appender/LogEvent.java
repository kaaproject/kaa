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

package org.kaaproject.kaa.server.appenders.couchbase.appender;

import java.io.Serializable;
import java.util.Map;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import com.couchbase.client.java.document.json.JsonObject;

@Document
public final class LogEvent implements Serializable {

    private static final long serialVersionUID = 5621981297013754001L;

    @Id
    private String id;
    
    @Field
    private RecordHeader header;
    
    @Field
    private Map<String, Object> event;

    public LogEvent() {}

    public LogEvent(RecordHeader header, LogEventDto dto) {
        this.id = dto.getId();
        this.header = header;
        this.event = JsonObject.fromJson(dto.getEvent()).toMap();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object>  getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object>  event) {
        this.event = event;
    }

    public RecordHeader getHeader() {
        return header;
    }

    public void setHeader(RecordHeader header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return "LogEvent [id=" + id + ", header=" + header + ", event=" + event + "]";
    }

}