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

package org.kaaproject.kaa.server.admin.shared.config;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.StructureRecordDto;

public class ConfigurationRecordViewDto extends StructureRecordDto<ConfigurationRecordFormDto> implements Serializable, Comparable<ConfigurationRecordViewDto> {

    private static final long serialVersionUID = 5838762122987694212L;
    
    public ConfigurationRecordViewDto() {
        super();
    }

    public ConfigurationRecordViewDto(ConfigurationRecordFormDto activeConfiguration, ConfigurationRecordFormDto inactiveConfiguration) {
        super(activeConfiguration, inactiveConfiguration);
    }

    public int getSchemaVersion() {
      return activeStructureDto != null ? activeStructureDto.getSchemaVersion() : inactiveStructureDto.getSchemaVersion();
    }
    
    public String getSchemaId() {
        return activeStructureDto != null ? activeStructureDto.getSchemaId() : inactiveStructureDto.getSchemaId();
    }

    @Override
    public int compareTo(ConfigurationRecordViewDto o) {
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
