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

package org.kaaproject.kaa.common.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.kaaproject.kaa.common.dto.HasId;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties({"schemaForm"})
public class EventClassFamilyVersionDto implements HasId, Serializable {

  private static final long serialVersionUID = -6565622945148633465L;

  private String id;
  private List<EventClassDto> records;
  private int version;
  private String createdUsername;
  private long createdTime;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public List<EventClassDto> getRecords() {
    return records;
  }

  public void setRecords(List<EventClassDto> records) {
    this.records = records;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getCreatedUsername() {
    return createdUsername;
  }

  public void setCreatedUsername(String createdUsername) {
    this.createdUsername = createdUsername;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
    result = prime * result
        + ((createdUsername == null) ? 0 : createdUsername.hashCode());
    result = prime * result + version;
    result = prime * result + ((records == null) ? 0 : records.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EventClassFamilyVersionDto other = (EventClassFamilyVersionDto) obj;
    if (createdTime != other.createdTime) {
      return false;
    }
    if (createdUsername == null) {
      if (other.createdUsername != null) {
        return false;
      }
    } else if (!createdUsername.equals(other.createdUsername)) {
      return false;
    }
    if (records == null) {
      if (other.records != null) {
        return false;
      }
    } else if (!records.equals(other.records)) {
      return false;
    }
    return version == other.version;
  }

  @Override
  public String toString() {
    return "EventClassFamilyVersionDto [version="
        + version + ", createdUsername=" + createdUsername
        + ", createdTime=" + createdTime + "]";
  }

}
