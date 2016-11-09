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

package org.kaaproject.kaa.server.appenders.mongo.appender;

import static com.mongodb.util.JSON.parse;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoDaoUtil.encodeReservedCharacteres;

import com.mongodb.DBObject;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document
public final class LogEvent implements Serializable {

  private static final long serialVersionUID = -2738374699172219071L;

  @Id
  private String id;
  private DBObject header;
  private DBObject event;
  private DBObject clientProfile;
  private DBObject serverProfile;

  public LogEvent() {

  }

  /**
   * Create new instance of <code>LogEvent</code>.
   *
   * @param dto           data transfer object, that contain id, header and event. use these data to
   *                      assign on appropriate field
   * @param clientProfile the client profile info
   * @param serverProfile the server profile info
   */
  public LogEvent(LogEventDto dto, ProfileInfo clientProfile, ProfileInfo serverProfile) {
    this.id = dto.getId();
    this.header = encodeReservedCharacteres((DBObject) parse(dto.getHeader()));
    this.event = encodeReservedCharacteres((DBObject) parse(dto.getEvent()));
    this.clientProfile = (clientProfile != null)
        ? encodeReservedCharacteres((DBObject) parse(clientProfile.getBody())) : null;

    this.serverProfile = (serverProfile != null)
        ? encodeReservedCharacteres((DBObject) parse(serverProfile.getBody())) : null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DBObject getEvent() {
    return event;
  }

  public void setEvent(DBObject event) {
    this.event = encodeReservedCharacteres(event);
  }

  public DBObject getHeader() {
    return header;
  }

  public void setHeader(DBObject header) {
    this.header = encodeReservedCharacteres(header);
  }

  public DBObject getClientProfile() {
    return clientProfile;
  }

  public void setClientProfile(DBObject clientProfile) {
    this.clientProfile = encodeReservedCharacteres(clientProfile);
  }

  public DBObject getServerProfile() {
    return serverProfile;
  }

  public void setServerProfile(DBObject serverProfile) {
    this.serverProfile = encodeReservedCharacteres(serverProfile);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("LogEvent[");
    sb.append("id='").append(id).append('\'');
    sb.append(", header=").append(header);
    sb.append(", event=").append(event);
    sb.append(", clientProfile=").append(clientProfile);
    sb.append(", serverProfile=").append(serverProfile);
    sb.append(']');
    return sb.toString();
  }

}
