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

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CONFIGURATION_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION_CONFIGURATION_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION_KEY_HASH;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = EP_SPECIFIC_CONFIGURATION)
public class MongoEndpointSpecificConfiguration implements EndpointSpecificConfiguration, Serializable {

  private static final long serialVersionUID = 2913495348256356048L;

  @Id
  private String id;
  @Indexed
  @Field(EP_SPECIFIC_CONFIGURATION_KEY_HASH)
  private byte[] endpointKeyHash;
  @Field(EP_CONFIGURATION_VERSION)
  private Integer configurationVersion;
  @Field(EP_SPECIFIC_CONFIGURATION_CONFIGURATION)
  private String configuration;
  @Version
  @Field(EP_SPECIFIC_CONFIGURATION_CONFIGURATION_VERSION)
  private Long version;

  public MongoEndpointSpecificConfiguration() {
  }

  /**
   * All-args constructor.
   */
  public MongoEndpointSpecificConfiguration(EndpointSpecificConfigurationDto dto) {
    this.endpointKeyHash = dto.getEndpointKeyHash();
    this.configurationVersion = dto.getConfigurationSchemaVersion();
    this.configuration = dto.getConfiguration();
    this.version = dto.getVersion();
    generateId();
  }

  @Override
  public EndpointSpecificConfigurationDto toDto() {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
    dto.setEndpointKeyHash(this.getEndpointKeyHash());
    dto.setConfiguration(this.getConfiguration());
    dto.setConfigurationSchemaVersion(this.getConfigurationVersion());
    dto.setVersion(this.getVersion());
    return dto;
  }

  protected void generateId() {
    id = new String(endpointKeyHash) + '#' + configurationVersion;
  }

  public byte[] getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public void setEndpointKeyHash(byte[] endpointKeyHash) {
    this.endpointKeyHash = endpointKeyHash;
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

  @Override
  public Long getVersion() {
    return version;
  }

  @Override
  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
