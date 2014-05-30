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

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.getArrayCopy;

@Document(collection = EndpointConfiguration.COLLECTION_NAME)
public final class EndpointConfiguration implements ToDto<EndpointConfigurationDto>, Serializable {

    private static final long serialVersionUID = -5646769700581347085L;

    public static final String COLLECTION_NAME = "endpoint_configuration";

    @Id
    private String id;
    @Indexed
    @Field("configuration_hash")
    private byte[] configurationHash;
    private byte[] configuration;

    public EndpointConfiguration() {
    }

    public EndpointConfiguration(EndpointConfigurationDto dto) {
        this.id = dto.getId();
        this.configuration = dto.getConfiguration();
        this.configurationHash = dto.getConfigurationHash();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(byte[] configurationHash) {
        this.configurationHash = getArrayCopy(configurationHash);
    }

    public byte[] getConfiguration() {
        return configuration;
    }

    public void setConfiguration(byte[] configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointConfiguration)) {
            return false;
        }

        EndpointConfiguration that = (EndpointConfiguration) o;

        if (!Arrays.equals(configuration, that.configuration)) {
            return false;
        }
        if (!Arrays.equals(configurationHash, that.configurationHash)) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (configurationHash != null ? Arrays.hashCode(configurationHash) : 0);
        result = 31 * result + (configuration != null ? Arrays.hashCode(configuration) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointConfiguration{" +
                "id='" + id + '\'' +
                ", configurationHash=" + Arrays.toString(configurationHash) +
                ", configuration='" + configuration + '\'' +
                '}';
    }

    @Override
    public EndpointConfigurationDto toDto() {
        EndpointConfigurationDto dto = new EndpointConfigurationDto();
        dto.setId(id);
        dto.setConfiguration(configuration);
        dto.setConfigurationHash(configurationHash);
        return dto;
    }
}
