/*
 * Copyright 2014 CyberVision, Inc.
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

public class SdkKey implements Serializable {

    private static final long serialVersionUID = 2433663439327120870L;

    public final static String SDK_KEY_PARAMETER = "sdkKey";

    private String applicationId;
    private Integer configurationSchemaVersion;
    private Integer profileSchemaVersion;
    private Integer notificationSchemaVersion;
    private Integer logSchemaVersion;
    private SdkPlatform targetPlatform;
    private List<String> aefMapIds;

    public SdkKey() {
    }

    public SdkKey(String applicationId, Integer configurationSchemaVersion,
            Integer profileSchemaVersion, Integer notificationSchemaVersion,
            Integer logSchemaVersion,
            SdkPlatform targetPlatform, List<String> aefMapIds) {
        super();
        this.applicationId = applicationId;
        this.configurationSchemaVersion = configurationSchemaVersion;
        this.profileSchemaVersion = profileSchemaVersion;
        this.notificationSchemaVersion = notificationSchemaVersion;
        this.logSchemaVersion = logSchemaVersion;
        this.targetPlatform = targetPlatform;
        this.aefMapIds = aefMapIds;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((aefMapIds == null) ? 0 : aefMapIds.hashCode());
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime
                * result
                + ((configurationSchemaVersion == null) ? 0
                        : configurationSchemaVersion.hashCode());
        result = prime
                * result
                + ((notificationSchemaVersion == null) ? 0
                        : notificationSchemaVersion.hashCode());
        result = prime
                * result
                + ((profileSchemaVersion == null) ? 0 : profileSchemaVersion
                        .hashCode());
        result = prime
                * result
                + ((logSchemaVersion == null) ? 0 : logSchemaVersion
                        .hashCode());
        result = prime * result
                + ((targetPlatform == null) ? 0 : targetPlatform.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SdkKey other = (SdkKey) obj;
        if (aefMapIds == null) {
            if (other.aefMapIds != null)
                return false;
        } else if (!aefMapIds.equals(other.aefMapIds))
            return false;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        if (configurationSchemaVersion == null) {
            if (other.configurationSchemaVersion != null)
                return false;
        } else if (!configurationSchemaVersion
                .equals(other.configurationSchemaVersion))
            return false;
        if (notificationSchemaVersion == null) {
            if (other.notificationSchemaVersion != null)
                return false;
        } else if (!notificationSchemaVersion
                .equals(other.notificationSchemaVersion))
            return false;
        if (profileSchemaVersion == null) {
            if (other.profileSchemaVersion != null)
                return false;
        } else if (!profileSchemaVersion.equals(other.profileSchemaVersion))
            return false;
        if (logSchemaVersion == null) {
            if (other.logSchemaVersion != null)
                return false;
        } else if (!logSchemaVersion.equals(other.logSchemaVersion))
            return false;
        if (targetPlatform != other.targetPlatform)
            return false;
        return true;
    }

}
