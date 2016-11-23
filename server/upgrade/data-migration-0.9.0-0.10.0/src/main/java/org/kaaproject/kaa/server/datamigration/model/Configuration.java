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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Configuration implements Serializable {

  private static final long serialVersionUID = -1176562073;

  private byte[] configurationBody;
  private Integer configurationSchemasVersion;
  private Long id;
  private Long configurationSchemasId;

  public Configuration() {
  }

  /**
   * Copy constructor.
   */
  public Configuration(Configuration configuration) {
    this.configurationBody = configuration.configurationBody;
    this.configurationSchemasVersion = configuration.configurationSchemasVersion;
    this.id = configuration.id;
    this.configurationSchemasId = configuration.configurationSchemasId;
  }

  /**
   * Instantiates a new Configuration.
   */
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
