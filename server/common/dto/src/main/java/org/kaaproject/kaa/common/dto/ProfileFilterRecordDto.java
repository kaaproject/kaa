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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfileFilterRecordDto implements Serializable {

    private static final long serialVersionUID = 4493137983752138274L;
    
    private ProfileFilterDto activeProfileFilter; 
    private ProfileFilterDto inactiveProfileFilter;
    
    public ProfileFilterRecordDto() {
    }
    
    public ProfileFilterRecordDto(ProfileFilterDto activeProfileFilter, ProfileFilterDto inactiveProfileFilter) {
        this.activeProfileFilter = activeProfileFilter;
        this.inactiveProfileFilter = inactiveProfileFilter;
    }
    
    public ProfileFilterDto getActiveProfileFilter() {
        return activeProfileFilter;
    }

    public void setActiveProfileFilter(ProfileFilterDto activeProfileFilter) {
        this.activeProfileFilter = activeProfileFilter;
    }

    public ProfileFilterDto getInactiveProfileFilter() {
        return inactiveProfileFilter;
    }

    public void setInactiveProfileFilter(ProfileFilterDto inactiveProfileFilter) {
        this.inactiveProfileFilter = inactiveProfileFilter;
    }

    public static List<ProfileFilterRecordDto> formStructureRecords(List<StructureRecordDto<ProfileFilterDto>> records) {
        List<ProfileFilterRecordDto> result = new ArrayList<>();
        for (StructureRecordDto<ProfileFilterDto> record : records) {
            ProfileFilterRecordDto profileFilterRecord = new ProfileFilterRecordDto(record.getActiveStructureDto(), record.getInactiveStructureDto());
            result.add(profileFilterRecord);
        }
        return result;
    }
    
    public static ProfileFilterRecordDto fromStructureRecord(StructureRecordDto<ProfileFilterDto> record) {
        return new ProfileFilterRecordDto(record.getActiveStructureDto(), record.getInactiveStructureDto());
    }
}
