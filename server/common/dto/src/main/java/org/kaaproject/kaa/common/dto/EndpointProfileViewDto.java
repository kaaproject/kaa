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

package org.kaaproject.kaa.common.dto;

import org.kaaproject.avro.ui.shared.RecordField;

import java.io.Serializable;
import java.util.List;

public class EndpointProfileViewDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private EndpointProfileDto endpointProfileDto;
    private EndpointUserDto endpointUserDto;
    private ProfileSchemaDto profileSchemaDto;
    private List<EndpointGroupDto> groupDtoList;
    private RecordField endpointProfileRecord;
    private List<TopicDto> endpointNotificationTopics;

    public EndpointProfileViewDto() {}

    public EndpointProfileDto getEndpointProfileDto() {
        return endpointProfileDto;
    }

    public void setEndpointProfileDto(EndpointProfileDto endpointProfileDto) {
        this.endpointProfileDto = endpointProfileDto;
    }

    public EndpointUserDto getEndpointUserDto() {
        return endpointUserDto;
    }

    public void setEndpointUserDto(EndpointUserDto endpointUserDto) {
        this.endpointUserDto = endpointUserDto;
    }

    public ProfileSchemaDto getProfileSchemaDto() {
        return profileSchemaDto;
    }

    public void setProfileSchemaDto(ProfileSchemaDto profileSchemaDto) {
        this.profileSchemaDto = profileSchemaDto;
    }

    public List<EndpointGroupDto> getGroupDtoList() {
        return groupDtoList;
    }

    public void setGroupDtoList(List<EndpointGroupDto> groupDtoList) {
        this.groupDtoList = groupDtoList;
    }

    public RecordField getEndpointProfileRecord() {
        return endpointProfileRecord;
    }

    public void setEndpointProfileRecord(RecordField endpointProfileRecord) {
        this.endpointProfileRecord = endpointProfileRecord;
    }

    public List<TopicDto> getEndpointNotificationTopics() {
        return endpointNotificationTopics;
    }

    public void setEndpointNotificationTopics(List<TopicDto> endpointNotificationTopics) {
        this.endpointNotificationTopics = endpointNotificationTopics;
    }
}
