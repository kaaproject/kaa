/*
 * Copyright 2014-2015 CyberVision, Inc.
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
    private ServerProfileSchemaDto serverProfileSchemaDto;
    private RecordField endpointProfileRecord;
    private RecordField serverProfileRecord;
    private List<EndpointGroupDto> groupDtoList;
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

    public ServerProfileSchemaDto getServerProfileSchemaDto() {
        return serverProfileSchemaDto;
    }

    public void setServerProfileSchemaDto(ServerProfileSchemaDto serverProfileSchemaDto) {
        this.serverProfileSchemaDto = serverProfileSchemaDto;
    }

    public RecordField getEndpointProfileRecord() {
        return endpointProfileRecord;
    }

    public void setEndpointProfileRecord(RecordField endpointProfileRecord) {
        this.endpointProfileRecord = endpointProfileRecord;
    }

    public RecordField getServerProfileRecord() {
        return serverProfileRecord;
    }

    public void setServerProfileRecord(RecordField serverProfileRecord) {
        this.serverProfileRecord = serverProfileRecord;
    }

    public List<EndpointGroupDto> getGroupDtoList() {
        return groupDtoList;
    }

    public void setGroupDtoList(List<EndpointGroupDto> groupDtoList) {
        this.groupDtoList = groupDtoList;
    }

    public List<TopicDto> getEndpointNotificationTopics() {
        return endpointNotificationTopics;
    }

    public void setEndpointNotificationTopics(List<TopicDto> endpointNotificationTopics) {
        this.endpointNotificationTopics = endpointNotificationTopics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointProfileViewDto that = (EndpointProfileViewDto) o;

        if (endpointProfileDto != null ? !endpointProfileDto.equals(that.endpointProfileDto) : that.endpointProfileDto != null)
            return false;
        if (endpointUserDto != null ? !endpointUserDto.equals(that.endpointUserDto) : that.endpointUserDto != null)
            return false;
        if (profileSchemaDto != null ? !profileSchemaDto.equals(that.profileSchemaDto) : that.profileSchemaDto != null)
            return false;
        if (groupDtoList != null ? !groupDtoList.equals(that.groupDtoList) : that.groupDtoList != null) return false;
        if (endpointProfileRecord != null ? !endpointProfileRecord.equals(that.endpointProfileRecord) : that.endpointProfileRecord != null)
            return false;
        return !(endpointNotificationTopics != null ? !endpointNotificationTopics.equals(that.endpointNotificationTopics) : that.endpointNotificationTopics != null);

    }

    @Override
    public int hashCode() {
        int result = endpointProfileDto != null ? endpointProfileDto.hashCode() : 0;
        result = 31 * result + (endpointUserDto != null ? endpointUserDto.hashCode() : 0);
        result = 31 * result + (profileSchemaDto != null ? profileSchemaDto.hashCode() : 0);
        result = 31 * result + (groupDtoList != null ? groupDtoList.hashCode() : 0);
        result = 31 * result + (endpointProfileRecord != null ? endpointProfileRecord.hashCode() : 0);
        result = 31 * result + (endpointNotificationTopics != null ? endpointNotificationTopics.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointProfileViewDto{" +
                "endpointProfileDto=" + endpointProfileDto +
                ", endpointUserDto=" + endpointUserDto +
                ", profileSchemaDto=" + profileSchemaDto +
                ", groupDtoList=" + groupDtoList +
                ", endpointProfileRecord=" + endpointProfileRecord +
                ", endpointNotificationTopics=" + endpointNotificationTopics +
                '}';
    }
}
