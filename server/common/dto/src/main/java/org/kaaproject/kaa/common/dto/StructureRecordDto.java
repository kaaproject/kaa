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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureRecordDto<T extends AbstractStructureDto> implements Serializable, Comparable<StructureRecordDto<T>> {

    private static final long serialVersionUID = -1326052725635723124L;

    private T activeStructureDto;
    private T inactiveStructureDto;

    public StructureRecordDto() {

    }

    public StructureRecordDto(T activeStructureDto, T inactiveStructureDto) {
        this.activeStructureDto = activeStructureDto;
        this.inactiveStructureDto = inactiveStructureDto;
    }

    public boolean isEmpty() {
        return activeStructureDto == null && inactiveStructureDto == null;
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

    public int getMajorVersion() {
        return activeStructureDto != null ? activeStructureDto.getMajorVersion() : inactiveStructureDto.getMajorVersion();
    }

    public int getMinorVersion() {
        return activeStructureDto != null ? activeStructureDto.getMinorVersion() : inactiveStructureDto.getMinorVersion();
    }

    public String getDescription() {
        return activeStructureDto != null ? activeStructureDto.getDescription() : inactiveStructureDto.getDescription();
    }

    public long getEndpointCount() {
        return activeStructureDto != null ? activeStructureDto.getEndpointCount() : 0;
    }

    public boolean hasActive() {
        return activeStructureDto != null;
    }

    public boolean hasDeprecated() {
        return activeStructureDto != null && activeStructureDto.getStatus()==UpdateStatus.DEPRECATED;
    }

    public boolean hasDraft() {
        return inactiveStructureDto != null;
    }

    public String getSchemaId() {
        return activeStructureDto != null ? activeStructureDto.getSchemaId() : inactiveStructureDto.getSchemaId();
    }

    public String getEndpointGroupId() {
        return activeStructureDto != null ? activeStructureDto.getEndpointGroupId() : inactiveStructureDto.getEndpointGroupId();
    }

    public static <T extends AbstractStructureDto> List<StructureRecordDto<T>> convertToRecords(Collection<T> structures) {
        Map<String, StructureRecordDto<T>> recordsMap = new HashMap<>();
        for (T structure : structures) {
            StructureRecordDto<T> record = recordsMap.get(structure.getSchemaId());
            if (record == null) {
                record = new StructureRecordDto<T>();
                recordsMap.put(structure.getSchemaId(), record);
            }
            if (structure.getStatus()==UpdateStatus.ACTIVE) {
                record.setActiveStructureDto(structure);
            } else if (structure.getStatus()==UpdateStatus.INACTIVE) {
                record.setInactiveStructureDto(structure);
            }
        }
        return new ArrayList<>(recordsMap.values());
    }

    @Override
    public int compareTo(StructureRecordDto<T> o) { //NOSONAR
        int result = this.getMajorVersion() - o.getMajorVersion();
        if (result == 0) {
            result = this.getMinorVersion() - o.getMinorVersion();
        }
        return result;
    }
}
