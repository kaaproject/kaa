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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_CONFIGURATION_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIGURATION_VERSION_PROPERTY;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(name = CassandraModelConstants.EPS_CONFIGURATION_COLUMN_FAMILY_NAME)
public final class CassandraEndpointSpecificConfiguration implements EndpointSpecificConfiguration, Serializable {

  @Transient
  private static final long serialVersionUID = -8639669282952330290L;

  @PartitionKey
  @Column(name = EPS_CONFIGURATION_KEY_HASH_PROPERTY)
  private ByteBuffer endpointKeyHash;
  @ClusteringColumn
  @Column(name = EP_CONFIGURATION_VERSION_PROPERTY)
  private Integer configurationVersion;
  @Column(name = EPS_CONFIGURATION_CONFIGURATION_BODY_PROPERTY)
  private String configuration;
  @Column(name = OPT_LOCK)
  private Long version;

  public CassandraEndpointSpecificConfiguration() {
  }

  /**
   * Create new instance of <code>CassandraEndpointSpecificConfiguration</code>.
   *
   * @param dto is data transfer object contain data that
   *            assign on fields of new instance
   */
  public CassandraEndpointSpecificConfiguration(EndpointSpecificConfigurationDto dto) {
    this.endpointKeyHash = getByteBuffer(dto.getEndpointKeyHash());
    this.configurationVersion = dto.getConfigurationSchemaVersion();
    this.configuration = dto.getConfiguration();
    this.version = dto.getVersion();
  }

  @Override
  public EndpointSpecificConfigurationDto toDto() {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
    dto.setEndpointKeyHash(getBytes(this.getEndpointKeyHash()));
    dto.setConfiguration(this.getConfiguration());
    dto.setConfigurationSchemaVersion(this.getConfigurationVersion());
    dto.setVersion(this.getVersion());
    return dto;
  }

  public ByteBuffer getEndpointKeyHash() {
    return endpointKeyHash;
  }

  public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
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
