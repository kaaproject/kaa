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
import java.util.Arrays;

public class EndpointSpecificConfigurationDto implements Serializable {

  private static final long serialVersionUID = -1443936688020191482L;

  private byte[] endpointKeyHash;
  private Integer configurationSchemaVersion;
  private String configuration;
  private Long version;

  public EndpointSpecificConfigurationDto() {
  }

  /**
   * All-args constructor.
   */
  public EndpointSpecificConfigurationDto(byte[] endpointKeyHash, Integer configurationSchemaVersion, String configuration, Long version) {
    this.endpointKeyHash = endpointKeyHash;
    this.configurationSchemaVersion = configurationSchemaVersion;
    this.configuration = configuration;
    this.version = version;
  }

  public Integer getConfigurationSchemaVersion() {
    return configurationSchemaVersion;
  }

  public void setConfigurationSchemaVersion(Integer configurationSchemaVersion) {
    this.configurationSchemaVersion = configurationSchemaVersion;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public byte[] getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public void setEndpointKeyHash(byte[] endpointKeyHash) {
    this.endpointKeyHash = endpointKeyHash;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    EndpointSpecificConfigurationDto that = (EndpointSpecificConfigurationDto) obj;

    if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
      return false;
    }
    if (configurationSchemaVersion != null ? !configurationSchemaVersion.equals(that.configurationSchemaVersion) : that.configurationSchemaVersion != null) {
      return false;
    }
    return configuration != null ? configuration.equals(that.configuration) : that.configuration == null;

  }

  @Override
  public int hashCode() {
    int result = endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0;
    result = 31 * result + (configurationSchemaVersion != null ? configurationSchemaVersion.hashCode() : 0);
    result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EndpointSpecificConfigurationDto{"
        + "endpointKeyHash='" + Arrays.toString(endpointKeyHash) + '\''
        + ", schemaVersion=" + configurationSchemaVersion
        + ", configuration='" + configuration + '\''
        + '}';
  }
}
