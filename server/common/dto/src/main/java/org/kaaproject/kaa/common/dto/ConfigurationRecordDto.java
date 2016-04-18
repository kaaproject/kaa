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

public class ConfigurationRecordDto extends StructureRecordDto<ConfigurationDto> implements Serializable, Comparable<ConfigurationRecordDto> {

    private static final long serialVersionUID = 5838762122987694212L;

    public ConfigurationRecordDto() {
        super();
    }

    public ConfigurationRecordDto(ConfigurationDto activeConfiguration, ConfigurationDto inactiveConfiguration) {
        super(activeConfiguration, inactiveConfiguration);
    }

    @JsonIgnore
    public int getSchemaVersion() {
      return activeStructureDto != null ? activeStructureDto.getSchemaVersion() : inactiveStructureDto.getSchemaVersion();
    }

    @JsonIgnore
    public String getSchemaId() {
        return activeStructureDto != null ? activeStructureDto.getSchemaId() : inactiveStructureDto.getSchemaId();
    }
    
    public static List<ConfigurationRecordDto> convertToConfigurationRecords(Collection<ConfigurationDto> configurations) {
        Map<String, ConfigurationRecordDto> configurationRecordsMap = new HashMap<>();
        for (ConfigurationDto configuration : configurations) {
            ConfigurationRecordDto configurationRecord = configurationRecordsMap.get(configuration.getSchemaId());
            if (configurationRecord == null) {
                configurationRecord = new ConfigurationRecordDto();
                configurationRecordsMap.put(configuration.getSchemaId(), configurationRecord);
            }
            if (configuration.getStatus()==UpdateStatus.ACTIVE) {
                configurationRecord.setActiveStructureDto(configuration);
            } else if (configuration.getStatus()==UpdateStatus.INACTIVE) {
                configurationRecord.setInactiveStructureDto(configuration);
            }
        }
        return new ArrayList<>(configurationRecordsMap.values());
    }

    @Override
    public int compareTo(ConfigurationRecordDto o) {
        return this.getSchemaVersion() - o.getSchemaVersion();
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
