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

@Getter
@Setter
public class Schema {

  private Long id;

  private int version;

  private String name;

  private String description;

  private String createdUsername;

  private long createdTime;

  private Long appId;

  private String schems;

  private String type;


  public Schema() {}

  /**
   * Instantiates a new Schema.
   *
   */
  public Schema(Long id, int version, String name,
                String description, String createdUsername,
                long createdTime, Long appId, String schems, String type) {
    this.id = id;
    this.version = version;
    this.name = name;
    this.description = description;
    this.createdUsername = createdUsername;
    this.createdTime = createdTime;
    this.appId = appId;
    this.schems = schems;
    this.type = type;
  }



}
