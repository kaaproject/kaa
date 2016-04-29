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

package org.kaaproject.kaa.server.admin.shared.endpoint;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;

public class EndpointProfileViewDto implements Serializable {

    private static final long serialVersionUID = 758374275609746719L;

    private byte[] endpointKeyHash;
    private SdkProfileDto sdkProfileDto;
    private String userId;
    private String userExternalId;
    private VersionDto profileSchemaVersion;
    private String profileSchemaName;
    private VersionDto serverProfileSchemaVersion;
    private String serverProfileSchemaName;
    private RecordField profileRecord;
    private RecordField serverProfileRecord;
    private List<EndpointGroupDto> endpointGroups;
    private List<TopicDto> topics;

    public EndpointProfileViewDto() {
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserExternalId() {
        return userExternalId;
    }

    public void setUserExternalId(String userExternalId) {
        this.userExternalId = userExternalId;
    }

    public String getProfileSchemaName() {
        return profileSchemaName;
    }

    public void setProfileSchemaName(String profileSchemaName) {
        this.profileSchemaName = profileSchemaName;
    }

    public String getServerProfileSchemaName() {
        return serverProfileSchemaName;
    }

    public void setServerProfileSchemaName(String serverProfileSchemaName) {
        this.serverProfileSchemaName = serverProfileSchemaName;
    }

    public VersionDto getProfileSchemaVersion() {
        return profileSchemaVersion;
    }

    public void setProfileSchemaVersion(VersionDto profileSchemaVersion) {
        this.profileSchemaVersion = profileSchemaVersion;
    }

    public VersionDto getServerProfileSchemaVersion() {
        return serverProfileSchemaVersion;
    }

    public void setServerProfileSchemaVersion(VersionDto serverProfileSchemaVersion) {
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    public RecordField getProfileRecord() {
        return profileRecord;
    }

    public void setProfileRecord(RecordField profileRecord) {
        this.profileRecord = profileRecord;
    }

    public RecordField getServerProfileRecord() {
        return serverProfileRecord;
    }

    public void setServerProfileRecord(RecordField serverProfileRecord) {
        this.serverProfileRecord = serverProfileRecord;
    }

    public List<EndpointGroupDto> getEndpointGroups() {
        return endpointGroups;
    }

    public void setEndpointGroups(List<EndpointGroupDto> endpointGroups) {
        this.endpointGroups = endpointGroups;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
    }

    public SdkProfileDto getSdkProfileDto() {
        return sdkProfileDto;
    }

    public void setSdkProfileDto(SdkProfileDto sdkProfileDto) {
        this.sdkProfileDto = sdkProfileDto;
    }

}
