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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = ConfigurationSchema.COLLECTION_NAME)
public final class ConfigurationSchema extends AbstractSchema<ConfigurationSchemaDto> {

    private static final long serialVersionUID = 4785397187237862244L;

    public static final String COLLECTION_NAME = "configuration_schema";

    @Field("base_schema")
    private String baseSchema;
    @Field("protocol_schema")
    private String protocolSchema;
    @Field("override_schema")
    private String overrideSchema;
    @Field("configuration_schema_status")
    private UpdateStatus status;

    public ConfigurationSchema() {
    }

    public ConfigurationSchema(ConfigurationSchemaDto dto) {
        super(dto);
        this.protocolSchema = dto.getProtocolSchema();
        this.baseSchema = dto.getBaseSchema();
        this.overrideSchema = dto.getOverrideSchema();
        this.status = dto.getStatus();
    }

    public String getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getProtocolSchema() {
        return protocolSchema;
    }

    public void setProtocolSchema(String protocolSchema) {
        this.protocolSchema = protocolSchema;
    }

    public String getOverrideSchema() {
        return overrideSchema;
    }

    public void setOverrideSchema(String overrideSchema) {
        this.overrideSchema = overrideSchema;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigurationSchema)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ConfigurationSchema schema = (ConfigurationSchema) o;

        if (baseSchema != null ? !baseSchema.equals(schema.baseSchema) : schema.baseSchema != null) {
            return false;
        }
        if (overrideSchema != null ? !overrideSchema.equals(schema.overrideSchema) : schema.overrideSchema != null) {
            return false;
        }
        if (protocolSchema != null ? !protocolSchema.equals(schema.protocolSchema) : schema.protocolSchema != null) {
            return false;
        }
        if (status != schema.status) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (baseSchema != null ? baseSchema.hashCode() : 0);
        result = 31 * result + (protocolSchema != null ? protocolSchema.hashCode() : 0);
        result = 31 * result + (overrideSchema != null ? overrideSchema.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationSchema{" +
                "baseSchema='" + baseSchema + '\'' +
                ", protocolSchema='" + protocolSchema + '\'' +
                ", overrideSchema='" + overrideSchema + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public ConfigurationSchemaDto toDto() {
        ConfigurationSchemaDto dto = super.toDto();
        dto.setProtocolSchema(protocolSchema);
        dto.setBaseSchema(baseSchema);
        dto.setOverrideSchema(overrideSchema);
        dto.setStatus(status);
        return dto;
    }

    @Override
    protected ConfigurationSchemaDto createDto() {
        return new ConfigurationSchemaDto();
    }
}
