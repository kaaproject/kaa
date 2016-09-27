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

import java.io.Serializable;

public class EndpointSpecificConfigurationDto implements Serializable {

    private static final long serialVersionUID = -1443936688020191482L;

    private String endpointKeyHash;
    private Integer configurationVersion;
    private String configuration;
    private Long version;

    public EndpointSpecificConfigurationDto() {
    }

    public EndpointSpecificConfigurationDto(String endpointKeyHash, Integer configurationVersion, String configuration, Long version) {
        this.endpointKeyHash = endpointKeyHash;
        this.configurationVersion = configurationVersion;
        this.configuration = configuration;
        this.version = version;
    }

    public Integer getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(Integer configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(String endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointSpecificConfigurationDto that = (EndpointSpecificConfigurationDto) o;

        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null)
            return false;
        if (configurationVersion != null ? !configurationVersion.equals(that.configurationVersion) : that.configurationVersion != null)
            return false;
        return configuration != null ? configuration.equals(that.configuration) : that.configuration == null;

    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? endpointKeyHash.hashCode() : 0;
        result = 31 * result + (configurationVersion != null ? configurationVersion.hashCode() : 0);
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointSpecificConfigurationDto{" +
                "endpointKeyHash='" + endpointKeyHash + '\'' +
                ", schemaVersion=" + configurationVersion +
                ", configuration='" + configuration + '\'' +
                '}';
    }
}
