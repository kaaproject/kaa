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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;

@Entity
@Table(name = SDK_PROFILE_TABLE_NAME)
public class SdkProfile extends GenericModel<SdkProfileDto> implements Serializable {

    private static final long serialVersionUID = -5963289882951330950L;

    @Column(name = SDK_PROFILE_TOKEN)
    private String token;

    @ManyToOne
    @JoinColumn(name = SDK_PROFILE_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected Application application;

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

    public SdkProfile(Long id) {
        this.id = id;
    }

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SdkProfile other = (SdkProfile) o;

        if (application != null ? !application.equals(other.application) : other.application != null) {
            return false;
        }
        if (token != null ? !token.equals(other.token) : other.token != null) {
            return false;
        }
        if (name != null ? !name.equals(other.name) : other.name != null) {
            return false;
        }
        if (configurationSchemaVersion != null ? !configurationSchemaVersion.equals(other.configurationSchemaVersion) : other.configurationSchemaVersion != null) {
            return false;
        }
        if (profileSchemaVersion != null ? !profileSchemaVersion.equals(other.profileSchemaVersion) : other.profileSchemaVersion != null) {
            return false;
        }
        if (notificationSchemaVersion != null ? !notificationSchemaVersion.equals(other.notificationSchemaVersion) : other.notificationSchemaVersion != null) {
            return false;
        }
        if (logSchemaVersion != null ? !logSchemaVersion.equals(other.logSchemaVersion) : other.logSchemaVersion != null) {
            return false;
        }
        if (aefMapIds != null ? !aefMapIds.equals(other.aefMapIds) : other.aefMapIds != null) {
            return false;
        }
        if (defaultVerifierToken != null ? !defaultVerifierToken.equals(other.defaultVerifierToken) : other.defaultVerifierToken != null) {
            return false;
        }
        if (createdUsername != null ? !createdUsername.equals(other.createdUsername) : other.createdUsername != null) {
            return false;
        }
        if (createdTime != null ? !createdTime.equals(other.createdTime) : other.createdTime != null) {
            return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (configurationSchemaVersion != null ? configurationSchemaVersion.hashCode() : 0);
        result = 31 * result + (profileSchemaVersion != null ? profileSchemaVersion.hashCode() : 0);
        result = 31 * result + (notificationSchemaVersion != null ? notificationSchemaVersion.hashCode() : 0);
        result = 31 * result + (logSchemaVersion != null ? logSchemaVersion.hashCode() : 0);
        result = 31 * result + (aefMapIds != null ? aefMapIds.hashCode() : 0);
        result = 31 * result + (defaultVerifierToken != null ? defaultVerifierToken.hashCode() : 0);
        result = 31 * result + (createdUsername != null ? createdUsername.hashCode() : 0);
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "SdkToken{" +
                "token='" + token + '\'' +
                ", application=" + application +
                ", name=" + name +
                ", configurationSchemaVersion=" + configurationSchemaVersion +
                ", profileSchemaVersion=" + profileSchemaVersion +
                ", notificationSchemaVersion=" + notificationSchemaVersion +
                ", logSchemaVersion=" + logSchemaVersion +
                ", aefMapIds=" + (aefMapIds != null ? Arrays.toString(aefMapIds.toArray()) : null) +
                ", defaultVerifierToken=" + defaultVerifierToken +
                ", createdUsername=" + createdUsername +
                ", createdTime=" + createdTime +
                ", endpointCount=" + endpointCount +
                '}';
    }
}
