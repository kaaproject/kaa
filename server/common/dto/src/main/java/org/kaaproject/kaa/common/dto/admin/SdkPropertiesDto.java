/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.common.dto.admin;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

public class SdkPropertiesDto implements HasId, Serializable {

    private static final long serialVersionUID = 2433663439327120870L;

    public static final String SDK_KEY_PARAMETER = "sdkKey";

    private String id;
    private String applicationId;
    private Integer configurationSchemaVersion;
    private Integer profileSchemaVersion;
    private Integer notificationSchemaVersion;
    private Integer logSchemaVersion;
    private SdkPlatform targetPlatform;
    private List<String> aefMapIds;
    private String defaultVerifierToken;
    private String applicationToken;
    private String name;
    private String token;
    private String createdUsername;
    private Long createdTime;
    private Integer endpointCount;

    public SdkPropertiesDto() {
    }

    public SdkPropertiesDto(String applicationId, Integer configurationSchemaVersion,
                            Integer profileSchemaVersion, Integer notificationSchemaVersion,
                            Integer logSchemaVersion,
                            SdkPlatform targetPlatform, List<String> aefMapIds,
                            String defaultVerifierToken, String applicationToken) {
        super();
        this.applicationId = applicationId;
        this.configurationSchemaVersion = configurationSchemaVersion;
        this.profileSchemaVersion = profileSchemaVersion;
        this.notificationSchemaVersion = notificationSchemaVersion;
        this.logSchemaVersion = logSchemaVersion;
        this.targetPlatform = targetPlatform;
        this.aefMapIds = aefMapIds;
        this.defaultVerifierToken = defaultVerifierToken;
        this.applicationToken = applicationToken;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public SdkPlatform getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(SdkPlatform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public List<String> getAefMapIds() {
        return aefMapIds;
    }

    public void setAefMapIds(List<String> aefMapIds) {
        this.aefMapIds = aefMapIds;
    }

    public Integer getLogSchemaVersion() {
        return logSchemaVersion;
    }

    public void setLogSchemaVersion(Integer logSchemaVersion) {
        this.logSchemaVersion = logSchemaVersion;
    }

    public String getDefaultVerifierToken() {
        return defaultVerifierToken;
    }

    public void setDefaultVerifierToken(String defaultVerifierToken) {
        this.defaultVerifierToken = defaultVerifierToken;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SdkPropertiesDto that = (SdkPropertiesDto) o;

        if (aefMapIds != null ? !aefMapIds.equals(that.aefMapIds) : that.aefMapIds != null) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)  {
            return false;
        }
        if (applicationToken != null ? !applicationToken.equals(that.applicationToken) : that.applicationToken != null) {
            return false;
        }
        if (configurationSchemaVersion != null ? !configurationSchemaVersion.equals(that.configurationSchemaVersion) : that.configurationSchemaVersion != null) {
            return false;
        }
        if (defaultVerifierToken != null ? !defaultVerifierToken.equals(that.defaultVerifierToken) : that.defaultVerifierToken != null) {
            return false;
        }
        if (logSchemaVersion != null ? !logSchemaVersion.equals(that.logSchemaVersion) : that.logSchemaVersion != null) {
            return false;
        }
        if (notificationSchemaVersion != null ? !notificationSchemaVersion.equals(that.notificationSchemaVersion) : that.notificationSchemaVersion != null) {
            return false;
        }
        if (profileSchemaVersion != null ? !profileSchemaVersion.equals(that.profileSchemaVersion) : that.profileSchemaVersion != null) {
            return false;
        }
        if (targetPlatform != that.targetPlatform) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (token != null ? !token.equals(that.token) : that.token != null) {
            return false;
        }
        if (createdUsername != null ? !createdUsername.equals(that.createdUsername) : that.createdUsername != null) {
            return false;
        }
        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) {
            return false;
        }
        if (endpointCount != null ? !endpointCount.equals(that.endpointCount) : that.endpointCount != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (configurationSchemaVersion != null ? configurationSchemaVersion.hashCode() : 0);
        result = 31 * result + (profileSchemaVersion != null ? profileSchemaVersion.hashCode() : 0);
        result = 31 * result + (notificationSchemaVersion != null ? notificationSchemaVersion.hashCode() : 0);
        result = 31 * result + (logSchemaVersion != null ? logSchemaVersion.hashCode() : 0);
        result = 31 * result + (targetPlatform != null ? targetPlatform.hashCode() : 0);
        result = 31 * result + (aefMapIds != null ? aefMapIds.hashCode() : 0);
        result = 31 * result + (defaultVerifierToken != null ? defaultVerifierToken.hashCode() : 0);
        result = 31 * result + (applicationToken != null ? applicationToken.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (createdUsername != null ? createdUsername.hashCode() : 0);
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
        result = 31 * result + (endpointCount != null ? endpointCount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SdkPropertiesDto{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", configurationSchemaVersion=" + configurationSchemaVersion +
                ", profileSchemaVersion=" + profileSchemaVersion +
                ", notificationSchemaVersion=" + notificationSchemaVersion +
                ", logSchemaVersion=" + logSchemaVersion +
                ", targetPlatform=" + targetPlatform +
                ", aefMapIds=" + aefMapIds +
                ", defaultVerifierToken='" + defaultVerifierToken + '\'' +
                ", applicationToken='" + applicationToken + '\'' +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", createdUsername='" + createdUsername + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", endpointCount='" + endpointCount + '\'' +
                '}';
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
