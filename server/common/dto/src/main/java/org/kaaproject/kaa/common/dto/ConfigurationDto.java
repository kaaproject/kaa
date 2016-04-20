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


public class ConfigurationDto extends AbstractStructureDto {

    private static final long serialVersionUID = 1766336602276590007L;

    private int schemaVersion;
    private String schemaId;
    private String protocolSchema;

    public ConfigurationDto() {
        super();
    }

    public ConfigurationDto(ConfigurationDto other) {
        super(other);
        this.schemaId = other.schemaId;
        this.schemaVersion = other.getSchemaVersion();
        this.protocolSchema = other.protocolSchema;
    }

    public String getProtocolSchema() {
        return protocolSchema;
    }

    public void setProtocolSchema(String protocolSchema) {
        this.protocolSchema = protocolSchema;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ConfigurationDto that = (ConfigurationDto) o;

        if (schemaId != null ? !schemaId.equals(that.schemaId) : that.schemaId != null) {
            return false;
        }
        return protocolSchema != null ? protocolSchema.equals(that.protocolSchema) : that.protocolSchema == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (schemaId != null ? schemaId.hashCode() : 0);
        result = 31 * result + (protocolSchema != null ? protocolSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationDto{" +
                "schemaId='" + schemaId + '\'' +
                ", protocolSchema='" + protocolSchema + '\'' +
                '}';
    }
}
