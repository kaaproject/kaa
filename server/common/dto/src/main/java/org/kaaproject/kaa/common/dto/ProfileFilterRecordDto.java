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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProfileFilterRecordDto extends StructureRecordDto<ProfileFilterDto> implements Serializable, Comparable<ProfileFilterRecordDto> {

    private static final long serialVersionUID = 4493137983752138274L;

    public ProfileFilterRecordDto() {
        super();
    }

    public ProfileFilterRecordDto(ProfileFilterDto activeProfileFilter, ProfileFilterDto inactiveProfileFilter) {
        super(activeProfileFilter, inactiveProfileFilter);
    }

    @JsonIgnore
    public Integer getEndpointProfileSchemaVersion() {
        return activeStructureDto != null ? activeStructureDto.getEndpointProfileSchemaVersion() : inactiveStructureDto.getEndpointProfileSchemaVersion();
    }

    @JsonIgnore
    public Integer getServerProfileSchemaVersion() {
        return activeStructureDto != null ? activeStructureDto.getServerProfileSchemaVersion() : inactiveStructureDto.getServerProfileSchemaVersion();
    }

    @JsonIgnore
    public String getEndpointProfileSchemaId() {
        return activeStructureDto != null ? activeStructureDto.getEndpointProfileSchemaId() : inactiveStructureDto.getEndpointProfileSchemaId();
    }

    @JsonIgnore
    public String getServerProfileSchemaId() {
        return activeStructureDto != null ? activeStructureDto.getServerProfileSchemaId() : inactiveStructureDto.getServerProfileSchemaId();
    }

    public static List<ProfileFilterRecordDto> convertToProfileFilterRecords(Collection<ProfileFilterDto> profileFilters) {
        Map<ProfileVersionPairDto, ProfileFilterRecordDto> profileFiltterRecordsMap = new HashMap<>();
        for (ProfileFilterDto profileFilter : profileFilters) {
            ProfileVersionPairDto versionPair = new ProfileVersionPairDto(profileFilter.getEndpointProfileSchemaId(), 
                    profileFilter.getEndpointProfileSchemaVersion(), 
                    profileFilter.getServerProfileSchemaId(),
                    profileFilter.getServerProfileSchemaVersion());
            ProfileFilterRecordDto profileFilterRecord = profileFiltterRecordsMap.get(versionPair);
            if (profileFilterRecord == null) {
                profileFilterRecord = new ProfileFilterRecordDto();
                profileFiltterRecordsMap.put(versionPair, profileFilterRecord);
            }
            if (profileFilter.getStatus()==UpdateStatus.ACTIVE) {
                profileFilterRecord.setActiveStructureDto(profileFilter);
            } else if (profileFilter.getStatus()==UpdateStatus.INACTIVE) {
                profileFilterRecord.setInactiveStructureDto(profileFilter);
            }
        }
        return new ArrayList<>(profileFiltterRecordsMap.values());
    }

    @Override
    public int compareTo(ProfileFilterRecordDto o) {
        int endpointProfileShemaVersion = getEndpointProfileSchemaVersion() != null ? 
                                                getEndpointProfileSchemaVersion() : -1;
        int otherEndpointProfileShemaVersion = o.getEndpointProfileSchemaVersion() != null ? 
                                                    o.getEndpointProfileSchemaVersion() : -1;      
        int result = endpointProfileShemaVersion - otherEndpointProfileShemaVersion;
        if (result == 0) {
            int serverProfileShemaVersion = getServerProfileSchemaVersion() != null ? 
                                                    getServerProfileSchemaVersion() : -1;
            int otherServerProfileShemaVersion = o.getServerProfileSchemaVersion() != null ? 
                                                        o.getServerProfileSchemaVersion() : -1;    
            result = serverProfileShemaVersion - otherServerProfileShemaVersion;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
