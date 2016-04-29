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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_CONFIGURATION_CONF_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_CONFIGURATION_CONF_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_CONFIGURATION_CONF_PROPERTY;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointConfiguration;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;


@Table(name = ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME)
public final class CassandraEndpointConfiguration implements EndpointConfiguration, Serializable {

    @Transient
    private static final long serialVersionUID = -5682011223088285599L;

    @PartitionKey
    @Column(name = ENDPOINT_CONFIGURATION_CONF_HASH_PROPERTY)
    private ByteBuffer configurationHash;
    @Column(name = ENDPOINT_CONFIGURATION_CONF_PROPERTY)
    private ByteBuffer configuration;
    @Column(name = ENDPOINT_CONFIGURATION_CONF_ID_PROPERTY)
    private String id;

    public CassandraEndpointConfiguration() {
    }

    public CassandraEndpointConfiguration(EndpointConfigurationDto dto) {
        this.configuration = getByteBuffer(dto.getConfiguration());
        this.configurationHash = getByteBuffer(dto.getConfigurationHash());
    }

    public ByteBuffer getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(ByteBuffer configurationHash) {
        this.configurationHash = configurationHash;
    }

    public ByteBuffer getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ByteBuffer configuration) {
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CassandraEndpointConfiguration that = (CassandraEndpointConfiguration) o;

        if (configuration != null ? !configuration.equals(that.configuration) : that.configuration != null) {
            return false;
        }
        if (configurationHash != null ? !configurationHash.equals(that.configurationHash) : that.configurationHash != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationHash != null ? configurationHash.hashCode() : 0;
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointConfiguration{" +
                "configurationHash=" + configurationHash +
                ", configuration=" + configuration +
                '}';
    }

    @Override
    public EndpointConfigurationDto toDto() {
        EndpointConfigurationDto dto = new EndpointConfigurationDto();
        dto.setConfiguration(getBytes(configuration));
        dto.setConfigurationHash(getBytes(configurationHash));
        return dto;
    }
}
