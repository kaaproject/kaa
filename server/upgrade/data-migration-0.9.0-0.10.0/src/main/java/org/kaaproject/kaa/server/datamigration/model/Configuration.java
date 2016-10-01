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

package org.kaaproject.kaa.server.datamigration.model;

import java.io.Serializable;

public class Configuration implements Serializable {

  private static final long serialVersionUID = -1176562073;

  private byte[] configurationBody;
  private Integer configurationSchemasVersion;
  private Long id;
  private Long configurationSchemasId;

  public Configuration() {
  }

  public Configuration(Configuration value) {
    this.configurationBody = value.configurationBody;
    this.configurationSchemasVersion = value.configurationSchemasVersion;
    this.id = value.id;
    this.configurationSchemasId = value.configurationSchemasId;
  }

  public Configuration(
      byte[] configurationBody,
      Integer configurationSchemsVersion,
      Long id,
      Long configurationSchemsId
  ) {
    this.configurationBody = configurationBody;
    this.configurationSchemasVersion = configurationSchemsVersion;
    this.id = id;
    this.configurationSchemasId = configurationSchemsId;
  }

  public byte[] getConfigurationBody() {
    return this.configurationBody;
  }

  public void setConfigurationBody(byte[] configurationBody) {
    this.configurationBody = configurationBody;
  }

  public Integer getConfigurationSchemasVersion() {
    return this.configurationSchemasVersion;
  }

  public void setConfigurationSchemasVersion(Integer configurationSchemasVersion) {
    this.configurationSchemasVersion = configurationSchemasVersion;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getConfigurationSchemasId() {
    return this.configurationSchemasId;
  }

  public void setConfigurationSchemasId(Long configurationSchemasId) {
    this.configurationSchemasId = configurationSchemasId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("configuration.Configuration[");

    sb.append("configurationBody=").append(new String(configurationBody));
    sb.append(", configurationSchemasVersion=").append(configurationSchemasVersion);
    sb.append(", id=").append(id);
    sb.append(", configurationSchemasId=").append(configurationSchemasId);

    sb.append("]");
    return sb.toString();
  }
}
