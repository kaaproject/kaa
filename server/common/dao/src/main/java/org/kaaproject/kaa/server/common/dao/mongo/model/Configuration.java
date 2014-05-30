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

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.nio.charset.Charset;

@Document(collection = Configuration.COLLECTION_NAME)
public final class Configuration extends AbstractStructure<ConfigurationDto> {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final long serialVersionUID = 9086742443326104287L;

    public static final String COLLECTION_NAME = "configuration";

    @Field("protocol_schema")
    private String protocolSchema;

    public Configuration() {
    }

    public Configuration(ConfigurationDto dto) {
        super(dto);
        this.protocolSchema = dto.getProtocolSchema();
    }

    public String getProtocolSchema() {
        return protocolSchema;
    }

    public void setProtocolSchema(String protocolSchema) {
        this.protocolSchema = protocolSchema;
    }

    public byte[] getBinaryBody() {
        if (body != null) {
            return body.getBytes(UTF8);
        }
        return null;
    }

    public void setBinaryBody(byte[] binaryBody) {
        if (binaryBody != null) {
            this.body = new String(binaryBody, UTF8);
        } else {
            body = null;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Configuration) {
            Configuration that = (Configuration) other;
            if (protocolSchema != null ? !protocolSchema.equals(that.protocolSchema) : that.protocolSchema != null) {
                return false;
            }
            return super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (protocolSchema != null ? protocolSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", schemaId=" + schemaId +
                ", endpointGroupId=" + endpointGroupId +
                ", sequenceNumber=" + sequenceNumber +
                ", body='" + body + '\'' +
                ", protocolSchema='" + protocolSchema + '\'' +
                ", status=" + status +
                ", majorVersion=" + majorVersion +
                ", minorVersion=" + minorVersion +
                ", lastModifyTime=" + lastModifyTime +
                '}';
    }

    @Override
    public ConfigurationDto toDto() {
        ConfigurationDto dto = super.toDto();
        dto.setProtocolSchema(protocolSchema);
        return dto;
    }

    @Override
    protected ConfigurationDto createDto() {
        return new ConfigurationDto();
    }
}
