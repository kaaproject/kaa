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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_CONFIGURATION;

import java.io.Serializable;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointConfiguration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = ENDPOINT_CONFIGURATION)
public final class MongoEndpointConfiguration implements EndpointConfiguration, Serializable {

    private static final long serialVersionUID = -5646769700581347085L;

    @Id
    private byte[] configurationHash;
    private byte[] configuration;
    
    public MongoEndpointConfiguration() {
    }

    public MongoEndpointConfiguration(EndpointConfigurationDto dto) {
        this.configuration = dto.getConfiguration();
        this.configurationHash = dto.getConfigurationHash();
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MongoEndpointConfiguration that = (MongoEndpointConfiguration) o;

        if (!Arrays.equals(configuration, that.configuration)) {
            return false;
        }
        if (!Arrays.equals(configurationHash, that.configurationHash)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationHash != null ? Arrays.hashCode(configurationHash) : 0;
        result = 31 * result + (configuration != null ? Arrays.hashCode(configuration) : 0);
        return result;
    }

    @Override
    public EndpointConfigurationDto toDto() {
        EndpointConfigurationDto dto = new EndpointConfigurationDto();
        dto.setConfiguration(configuration);
        dto.setConfigurationHash(configurationHash);
        return dto;
    }

}
