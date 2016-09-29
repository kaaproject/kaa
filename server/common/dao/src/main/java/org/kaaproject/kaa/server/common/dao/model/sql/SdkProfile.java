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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_CONFIGURATION_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_DEFAULT_VERIFIER_TOKEN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_ENDPOINT_COUNT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_NOTIFICATION_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_PROFILE_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_PROFILE_TOKEN;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = SDK_PROFILE_TABLE_NAME)
public class SdkProfile extends GenericModel<SdkProfileDto> implements Serializable {

  private static final long serialVersionUID = -5963289882951330950L;
  @ManyToOne
  @JoinColumn(name = SDK_PROFILE_APPLICATION_ID, nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  protected Application application;
  @Column(name = SDK_PROFILE_TOKEN)
  private String token;
  @Column(name = SDK_PROFILE_NAME)
  private String name;

  @Column(name = SDK_PROFILE_CONFIGURATION_SCHEMA_VERSION)
  private Integer configurationSchemaVersion;

  @Column(name = SDK_PROFILE_PROFILE_SCHEMA_VERSION)
  private Integer profileSchemaVersion;

  @Column(name = SDK_PROFILE_NOTIFICATION_SCHEMA_VERSION)
  private Integer notificationSchemaVersion;

  @Column(name = SDK_PROFILE_LOG_SCHEMA_VERSION)
  private Integer logSchemaVersion;

  @ElementCollection
  @CollectionTable(name = "sdkprofile_aefmapids")
  private List<String> aefMapIds;

  @Column(name = SDK_PROFILE_DEFAULT_VERIFIER_TOKEN)
  private String defaultVerifierToken;

  @Column(name = SDK_PROFILE_CREATED_USERNAME)
  private String createdUsername;

  @Column(name = SDK_PROFILE_CREATED_TIME)
  private Long createdTime;

  @Column(name = SDK_PROFILE_ENDPOINT_COUNT)
  private Integer endpointCount = 0;

  public SdkProfile() {
  }


  /**
   * Instantiates a new Sdk profile with uniq identifier.
   *
   * @param id the identifier of new instance
   */
  public SdkProfile(Long id) {
    this.id = id;
  }


  /**
   * Instantiates a new SDKProfile based on passed dto object.
   *
   * @param dto data transfer object that used for creating new instance
   */
  public SdkProfile(SdkProfileDto dto) {
    if (dto != null) {
      this.id = ModelUtils.getLongId(dto.getId());

      this.name = dto.getName();
      this.configurationSchemaVersion = dto.getConfigurationSchemaVersion();
      this.profileSchemaVersion = dto.getProfileSchemaVersion();
      this.notificationSchemaVersion = dto.getNotificationSchemaVersion();
      this.logSchemaVersion = dto.getLogSchemaVersion();

      if (dto.getAefMapIds() != null) {
        this.aefMapIds = new ArrayList<>(dto.getAefMapIds().size());
        for (String id : dto.getAefMapIds()) {
          this.aefMapIds.add(id);
        }
      }

      this.defaultVerifierToken = dto.getDefaultVerifierToken();

      this.createdUsername = dto.getCreatedUsername();
      this.createdTime = dto.getCreatedTime();
      this.endpointCount = dto.getEndpointCount();

      Long applicationId = ModelUtils.getLongId(dto.getApplicationId());
      this.application = (applicationId != null) ? new Application(applicationId) : null;

      // An empty list is no different from a null field
      if (this.aefMapIds != null && this.aefMapIds.isEmpty()) {
        dto.setAefMapIds(null);
      }

      this.token = dto.getToken();

      if (this.aefMapIds != null) {
        dto.setAefMapIds(new ArrayList<String>(this.aefMapIds.size()));
        for (String id : this.aefMapIds) {
          dto.getAefMapIds().add(id);
        }
      }
    }
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getConfigurationSchemaVersion() {
    return configurationSchemaVersion;
  }

  public void setConfigurationSchemaVersion(Integer configurationSchemaVersion) {
    this.configurationSchemaVersion = configurationSchemaVersion;
  }

  public Integer getProfileSchemaVersion() {
    return profileSchemaVersion;
  }

  public void setProfileSchemaVersion(Integer profileSchemaVersion) {
    this.profileSchemaVersion = profileSchemaVersion;
  }

  public Integer getNotificationSchemaVersion() {
    return notificationSchemaVersion;
  }

  public void setNotificationSchemaVersion(Integer notificationSchemaVersion) {
    this.notificationSchemaVersion = notificationSchemaVersion;
  }

  public Integer getLogSchemaVersion() {
    return logSchemaVersion;
  }

  public void setLogSchemaVersion(Integer logSchemaVersion) {
    this.logSchemaVersion = logSchemaVersion;
  }

  public List<String> getAefMapIds() {
    return aefMapIds;
  }

  public void setAefMapIds(List<String> aefMapIds) {
    this.aefMapIds = aefMapIds;
  }

  public String getDefaultVerifierToken() {
    return defaultVerifierToken;
  }

  public void setDefaultVerifierToken(String defaultVerifierToken) {
    this.defaultVerifierToken = defaultVerifierToken;
  }

  public String getCreatedUsername() {
    return createdUsername;
  }

  public void setCreatedUsername(String createdUsername) {
    this.createdUsername = createdUsername;
  }

  public Long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Long createdTime) {
    this.createdTime = createdTime;
  }

  public Integer getEndpointCount() {
    return endpointCount;
  }

  public void setEndpointCount(Integer endpointCount) {
    this.endpointCount = endpointCount;
  }

  @Override
  protected SdkProfileDto createDto() {
    return new SdkProfileDto();
  }

  @Override
  protected GenericModel<SdkProfileDto> newInstance(Long id) {
    return new SdkProfile(id);
  }

  @Override
  public SdkProfileDto toDto() {
    SdkProfileDto dto = this.createDto();

    dto.setId(this.getStringId());
    dto.setToken(this.token);

    if (this.application != null) {
      dto.setApplicationId(this.application.getStringId());
      dto.setApplicationToken(this.application.getApplicationToken());
    }

    dto.setName(this.name);
    dto.setConfigurationSchemaVersion(this.configurationSchemaVersion);
    dto.setProfileSchemaVersion(this.profileSchemaVersion);
    dto.setNotificationSchemaVersion(this.notificationSchemaVersion);
    dto.setLogSchemaVersion(this.logSchemaVersion);

    if (this.aefMapIds != null) {
      dto.setAefMapIds(new ArrayList<String>(this.aefMapIds.size()));
      for (String id : this.aefMapIds) {
        dto.getAefMapIds().add(id);
      }
    }

    dto.setDefaultVerifierToken(this.defaultVerifierToken);
    dto.setCreatedUsername(this.createdUsername);
    dto.setCreatedTime(this.createdTime);
    dto.setEndpointCount(this.endpointCount);

    return dto;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SdkProfile that = (SdkProfile) obj;
    return Objects.equals(application, that.application)
        && Objects.equals(token, that.token)
        && Objects.equals(name, that.name)
        && Objects.equals(configurationSchemaVersion, that.configurationSchemaVersion)
        && Objects.equals(profileSchemaVersion, that.profileSchemaVersion)
        && Objects.equals(notificationSchemaVersion, that.notificationSchemaVersion)
        && Objects.equals(logSchemaVersion, that.logSchemaVersion)
        && Objects.equals(aefMapIds, that.aefMapIds)
        && Objects.equals(defaultVerifierToken, that.defaultVerifierToken)
        && Objects.equals(createdUsername, that.createdUsername)
        && Objects.equals(createdTime, that.createdTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        application,
        token,
        name,
        configurationSchemaVersion,
        profileSchemaVersion,
        notificationSchemaVersion,
        logSchemaVersion,
        aefMapIds,
        defaultVerifierToken,
        createdUsername,
        createdTime);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SdkProfile{");
    sb.append("application=").append(application);
    sb.append(", token='").append(token).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", configurationSchemaVersion=").append(configurationSchemaVersion);
    sb.append(", profileSchemaVersion=").append(profileSchemaVersion);
    sb.append(", notificationSchemaVersion=").append(notificationSchemaVersion);
    sb.append(", logSchemaVersion=").append(logSchemaVersion);
    sb.append(", aefMapIds=").append(aefMapIds);
    sb.append(", defaultVerifierToken='").append(defaultVerifierToken).append('\'');
    sb.append(", createdUsername='").append(createdUsername).append('\'');
    sb.append(", createdTime=").append(createdTime);
    sb.append(", endpointCount=").append(endpointCount);
    sb.append('}');
    return sb.toString();
  }
}
