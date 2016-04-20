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

package org.kaaproject.kaa.common.dto.logs;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;

public class LogAppenderDto extends PluginDto {

    private static final long serialVersionUID = 8035147059931231619L;

    private String applicationToken;
    private String tenantId;
    private int minLogSchemaVersion;
    private int maxLogSchemaVersion;
    private boolean confirmDelivery = true;
    private List<LogHeaderStructureDto> headerStructure;

    public LogAppenderDto() {
        super();
    }

    public LogAppenderDto(LogAppenderDto logAppenderDto) {
        super(logAppenderDto);
        this.applicationToken = logAppenderDto.getApplicationToken();
        this.tenantId = logAppenderDto.getTenantId();
        this.minLogSchemaVersion = logAppenderDto.getMinLogSchemaVersion();
        this.maxLogSchemaVersion = logAppenderDto.getMaxLogSchemaVersion();
        this.confirmDelivery = logAppenderDto.isConfirmDelivery();
        this.headerStructure = new ArrayList<LogHeaderStructureDto>(logAppenderDto.getHeaderStructure());
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public int getMinLogSchemaVersion() {
        return minLogSchemaVersion;
    }

    public void setMinLogSchemaVersion(int minLogSchemaVersion) {
        this.minLogSchemaVersion = minLogSchemaVersion;
    }

    public int getMaxLogSchemaVersion() {
        return maxLogSchemaVersion;
    }

    public void setMaxLogSchemaVersion(int maxLogSchemaVersion) {
        this.maxLogSchemaVersion = maxLogSchemaVersion;
    }

    public List<LogHeaderStructureDto> getHeaderStructure() {
        return headerStructure;
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        this.headerStructure = headerStructure;
    }

    public boolean isConfirmDelivery() {
        return confirmDelivery;
    }

    public void setConfirmDelivery(boolean confirmDelivery) {
        this.confirmDelivery = confirmDelivery;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + (confirmDelivery ? 1231 : 1237);
        result = prime * result
                + ((headerStructure == null) ? 0 : headerStructure.hashCode());
        result = prime * result + maxLogSchemaVersion;
        result = prime * result + minLogSchemaVersion;
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogAppenderDto other = (LogAppenderDto) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (confirmDelivery != other.confirmDelivery) {
            return false;
        }
        if (headerStructure == null) {
            if (other.headerStructure != null) {
                return false;
            }
        } else if (!headerStructure.equals(other.headerStructure)) {
            return false;
        }
        if (maxLogSchemaVersion != other.maxLogSchemaVersion) {
            return false;
        }
        if (minLogSchemaVersion != other.minLogSchemaVersion) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogAppenderDto [applicationToken=");
        builder.append(applicationToken);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", minLogSchemaVersion=");
        builder.append(minLogSchemaVersion);
        builder.append(", maxLogSchemaVersion=");
        builder.append(maxLogSchemaVersion);
        builder.append(", confirmDelivery=");
        builder.append(confirmDelivery);
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }


}
