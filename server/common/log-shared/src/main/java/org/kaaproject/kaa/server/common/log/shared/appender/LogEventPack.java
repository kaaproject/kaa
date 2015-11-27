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

import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;

public class LogEventPack {

    private final EndpointProfileDataDto profileDto;

    private final long dateCreated;

    private final List<LogEvent> events;

    private final int logSchemaVersion;

    private LogSchema logSchema;

    private String userId;

    private ProfileInfo clientProfile;

    private ProfileInfo serverProfile;

    public LogEventPack(EndpointProfileDataDto profileDto, long dateCreated, int logSchemaVersion, List<LogEvent> events) {
        this.profileDto = profileDto;
        this.dateCreated = dateCreated;
        this.logSchemaVersion = logSchemaVersion;
        this.events = events;
    }

    public String getEndpointKey() {
        return profileDto.getEndpointKey();
    }

    public EndpointProfileDataDto getProfileDto() {
        return profileDto;
    }

    public long getDateCreated() {
        return dateCreated;
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

    public List<LogEvent> getEvents() {
        return events;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProfileInfo getClientProfile() {
        return clientProfile;
    }

    public void setClientProfile(ProfileInfo clientProfile) {
        this.clientProfile = clientProfile;
    }

    public ProfileInfo getServerProfile() {
        return serverProfile;
    }

    public void setServerProfile(ProfileInfo serverProfile) {
        this.serverProfile = serverProfile;
    }

}
