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

package org.kaaproject.kaa.common.dto.logs;

import java.util.Arrays;

public class LogAppenderDto extends LogAppenderBaseDto {

    private static final long serialVersionUID = 8035147059935996619L;

    private byte[] rawConfiguration;

    public LogAppenderDto() {
        super();
    }

    public LogAppenderDto(LogAppenderBaseDto detailsDto) {
        super(detailsDto);
    }

    public LogAppenderDto(LogAppenderDto detailsDto) {
        super(detailsDto);
        this.rawConfiguration = detailsDto.rawConfiguration;
    }

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(rawConfiguration);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogAppenderDto other = (LogAppenderDto) obj;
        if (!Arrays.equals(rawConfiguration, other.rawConfiguration))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogAppenderDto [rawConfiguration=");
        builder.append(Arrays.toString(rawConfiguration));
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}