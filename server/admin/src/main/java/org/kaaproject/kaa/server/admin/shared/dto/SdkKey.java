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

package org.kaaproject.kaa.server.admin.shared.dto;

import java.io.Serializable;

public class SdkKey implements Serializable {

    private static final long serialVersionUID = 2433663439327120870L;

    public final static String SDK_KEY_PARAMETER = "sdkKey";

    private String applicationId;
    private Integer configurationSchemaVersion;
    private Integer profileSchemaVersion;
    private Integer notificationSchemaVersion;
    private SdkPlatform targetPlatform;

    public SdkKey() {
    }

    public SdkKey(String applicationId, Integer configurationSchemaVersion,
            Integer profileSchemaVersion, Integer notificationSchemaVersion,
            SdkPlatform targetPlatform) {
        super();
        this.applicationId = applicationId;
        this.configurationSchemaVersion = configurationSchemaVersion;
        this.profileSchemaVersion = profileSchemaVersion;
        this.notificationSchemaVersion = notificationSchemaVersion;
        this.targetPlatform = targetPlatform;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (targetPlatform != other.targetPlatform)
            return false;
        return true;
    }


}
