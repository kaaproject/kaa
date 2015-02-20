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

package org.kaaproject.kaa.server.common.log.shared.appender;

import java.util.List;

public class LogEventPack {

    private String endpointKey;

    private long dateCreated;

    private int logSchemaVersion;

    private LogSchema logSchema;

    private List<LogEvent> events;

    private String userId;

    public LogEventPack() {

    }

    public LogEventPack(String endpointKey, long dateCreated, LogSchema logSchema, List<LogEvent> events) {
        this.endpointKey = endpointKey;
        this.dateCreated = dateCreated;
        this.logSchema = logSchema;
        this.events = events;
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

    public LogSchema getLogSchema() {
        return logSchema;
    }

    public void setLogSchema(LogSchema logSchema) {
        this.logSchema = logSchema;
    }

    public int getLogSchemaVersion() {
        return logSchemaVersion;
    }

    public void setLogSchemaVersion(int logSchemaVersion) {
        this.logSchemaVersion = logSchemaVersion;
    }

    public List<LogEvent> getEvents() {
        return events;
    }

    public void setEvents(List<LogEvent> events) {
        this.events = events;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "LogEventPack [endpointKey=" + endpointKey + ", dateCreated=" + dateCreated + ", logSchemaVersion=" + logSchemaVersion + ", logSchema="
                + logSchema + ", events=" + events + "]";
    }

}
