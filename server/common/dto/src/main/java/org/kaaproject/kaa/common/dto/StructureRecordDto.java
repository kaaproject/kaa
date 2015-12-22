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

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class StructureRecordDto<T extends AbstractStructureDto> implements Serializable {

    private static final long serialVersionUID = -1326052725635723124L;

    protected T activeStructureDto;
    protected T inactiveStructureDto;

    public StructureRecordDto() {
    }

    public StructureRecordDto(T activeStructureDto, T inactiveStructureDto) {
        this.activeStructureDto = activeStructureDto;
        this.inactiveStructureDto = inactiveStructureDto;
    }

    public T getActiveStructureDto() {
        return activeStructureDto;
    }
    public void setActiveStructureDto(T activeStructureDto) {
        this.activeStructureDto = activeStructureDto;
    }
    public T getInactiveStructureDto() {
        return inactiveStructureDto;
    }
    public void setInactiveStructureDto(T inactiveStructureDto) {
        this.inactiveStructureDto = inactiveStructureDto;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return activeStructureDto == null && inactiveStructureDto == null;
    }

    @JsonIgnore
    public String getDescription() {
        return activeStructureDto != null ? activeStructureDto.getDescription() : inactiveStructureDto.getDescription();
    }

    @JsonIgnore
    public long getEndpointCount() {
        return activeStructureDto != null ? activeStructureDto.getEndpointCount() : 0;
    }

    @JsonIgnore
    public boolean hasActive() {
        return activeStructureDto != null;
    }

    @JsonIgnore
    public boolean hasDeprecated() {
        return activeStructureDto != null && activeStructureDto.getStatus()==UpdateStatus.DEPRECATED;
    }

    @JsonIgnore
    public boolean hasDraft() {
        return inactiveStructureDto != null;
    }

    @JsonIgnore
    public String getEndpointGroupId() {
        return activeStructureDto != null ? activeStructureDto.getEndpointGroupId() : inactiveStructureDto.getEndpointGroupId();
    }
   
}
