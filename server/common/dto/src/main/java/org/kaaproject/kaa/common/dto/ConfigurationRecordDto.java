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

public class ConfigurationRecordDto implements Serializable {

    private static final long serialVersionUID = 5838762122987694212L;
    
    private ConfigurationDto activeConfiguration; 
    private ConfigurationDto inactiveConfiguration;

    public ConfigurationRecordDto(ConfigurationDto activeConfiguration, ConfigurationDto inactiveConfiguration) {
        this.activeConfiguration = activeConfiguration;
        this.inactiveConfiguration = inactiveConfiguration;
    }
    
    public ConfigurationDto getActiveConfiguration() {
        return activeConfiguration;
    }

    public void setActiveConfiguration(ConfigurationDto activeConfiguration) {
        this.activeConfiguration = activeConfiguration;
    }

    public ConfigurationDto getInactiveConfiguration() {
        return inactiveConfiguration;
    }

    public void setInactiveConfiguration(ConfigurationDto inactiveConfiguration) {
        this.inactiveConfiguration = inactiveConfiguration;
    }

    public static List<ConfigurationRecordDto> formStructureRecords(List<StructureRecordDto<ConfigurationDto>> records) {
        List<ConfigurationRecordDto> result = new ArrayList<>();
        for (StructureRecordDto<ConfigurationDto> record : records) {
            ConfigurationRecordDto configurationRecord = new ConfigurationRecordDto(record.getActiveStructureDto(), record.getInactiveStructureDto());
            result.add(configurationRecord);
        }
        return result;
    }
    
    public static ConfigurationRecordDto fromStructureRecord(StructureRecordDto<ConfigurationDto> record) {
        return new ConfigurationRecordDto(record.getActiveStructureDto(), record.getInactiveStructureDto());
    }
}
