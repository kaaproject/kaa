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

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.CONFIGURATION_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_GROUP_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.PROFILE_FILTER_ID;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

public final class EndpointGroupState implements ToDto<EndpointGroupStateDto>, Serializable {

  private static final long serialVersionUID = -1658174097110691624L;

  @Field(ENDPOINT_GROUP_ID)
  private String endpointGroupId;
  @Field(PROFILE_FILTER_ID)
  private String profileFilterId;
  @Field(CONFIGURATION_ID)
  private String configurationId;

  public EndpointGroupState() {
  }

  /**
   * Create new instance of <code>EndpointGroupState</code>.
   * @param dto data transfer object contain data that
   *            assign on fields of new instance
   */
  public EndpointGroupState(EndpointGroupStateDto dto) {
    this.endpointGroupId = dto.getEndpointGroupId();
    this.profileFilterId = dto.getProfileFilterId();
    this.configurationId = dto.getConfigurationId();
  }

  public String getEndpointGroupId() {
    return endpointGroupId;
  }

  public void setEndpointGroupId(String endpointGroupId) {
    this.endpointGroupId = endpointGroupId;
  }

  public String getProfileFilterId() {
    return profileFilterId;
  }

  public void setProfileFilterId(String profileFilterId) {
    this.profileFilterId = profileFilterId;
  }

  public String getConfigurationId() {
    return configurationId;
  }

  public void setConfigurationId(String configurationId) {
    this.configurationId = configurationId;
  }

  @Override
  public final boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    EndpointGroupState that = (EndpointGroupState) object;

    if (configurationId != null
        ? !configurationId.equals(that.configurationId)
        : that.configurationId != null) {
      return false;
    }
    if (endpointGroupId != null
        ? !endpointGroupId.equals(that.endpointGroupId)
        : that.endpointGroupId != null) {
      return false;
    }
    return !(profileFilterId != null
        ? !profileFilterId.equals(that.profileFilterId)
        : that.profileFilterId != null);
  }

  @Override
  public final int hashCode() {
    int result = endpointGroupId != null ? endpointGroupId.hashCode() : 0;
    result = 31 * result + (profileFilterId != null ? profileFilterId.hashCode() : 0);
    result = 31 * result + (configurationId != null ? configurationId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EndpointGroupStateDto{"
        + "endpointGroupId=" + endpointGroupId
        + ", profileFilterId=" + profileFilterId
        + ", configurationId=" + configurationId
        + '}';
  }

  @Override
  public EndpointGroupStateDto toDto() {
    EndpointGroupStateDto groupStateDto = new EndpointGroupStateDto();
    groupStateDto.setEndpointGroupId(endpointGroupId);
    groupStateDto.setProfileFilterId(profileFilterId);
    groupStateDto.setConfigurationId(configurationId);
    return groupStateDto;
  }
}
