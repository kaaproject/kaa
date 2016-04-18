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

package org.kaaproject.kaa.common.dto.admin;

import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;

import java.io.Serializable;
import java.util.List;

public class SdkProfileViewDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private SdkProfileDto sdkProfile;
    List<ApplicationEventFamilyMapDto> aefMapDtoList;
    private String configurationSchemaName;
    private String configurationSchemaId;
    private String profileSchemaName;
    private String profileSchemaId;
    private String notificationSchemaName;
    private String notificationSchemaId;
    private String logSchemaName;
    private String logSchemaId;

    public SdkProfileViewDto() {
    }

    public SdkProfileDto getSdkProfile() {
        return sdkProfile;
    }

    public void setSdkProfile(SdkProfileDto sdkProfile) {
        this.sdkProfile = sdkProfile;
    }

    public List<ApplicationEventFamilyMapDto> getAefMapDtoList() {
        return aefMapDtoList;
    }

    public void setAefMapDtoList(List<ApplicationEventFamilyMapDto> aefMapDtoList) {
        this.aefMapDtoList = aefMapDtoList;
    }

    public void setConfigurationSchemaName(String configurationSchemaName) {
        this.configurationSchemaName = configurationSchemaName;
    }

    public String getConfigurationSchemaName() {
        return configurationSchemaName;
    }

    public void setConfigurationSchemaId(String configurationSchemaId) {
        this.configurationSchemaId = configurationSchemaId;
    }

    public String getConfigurationSchemaId() {
        return configurationSchemaId;
    }

    public void setProfileSchemaName(String profileSchemaName) {
        this.profileSchemaName = profileSchemaName;
    }

    public String getProfileSchemaName() {
        return profileSchemaName;
    }

    public void setProfileSchemaId(String profileSchemaId) {
        this.profileSchemaId = profileSchemaId;
    }

    public String getProfileSchemaId() {
        return profileSchemaId;
    }

    public void setNotificationSchemaName(String notificationSchemaName) {
        this.notificationSchemaName = notificationSchemaName;
    }

    public String getNotificationSchemaName() {
        return notificationSchemaName;
    }

    public void setNotificationSchemaId(String notificationSchemaId) {
        this.notificationSchemaId = notificationSchemaId;
    }

    public String getNotificationSchemaId() {
        return notificationSchemaId;
    }

    public void setLogSchemaName(String logSchemaName) {
        this.logSchemaName = logSchemaName;
    }

    public String getLogSchemaName() {
        return logSchemaName;
    }

    public void setLogSchemaId(String logSchemaId) {
        this.logSchemaId = logSchemaId;
    }

    public String getLogSchemaId() {
        return logSchemaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SdkProfileViewDto that = (SdkProfileViewDto) o;

        if (sdkProfile != null ? !sdkProfile.equals(that.sdkProfile) : that.sdkProfile != null) {
            return false;
        }
        if (aefMapDtoList != null ? !aefMapDtoList.equals(that.aefMapDtoList) : that.aefMapDtoList != null) {
            return false;
        }
        if (configurationSchemaName != null ? !configurationSchemaName.equals(that.configurationSchemaName) : that.configurationSchemaName != null) {
            return false;
        }
        if (configurationSchemaId != null ? !configurationSchemaId.equals(that.configurationSchemaId) : that.configurationSchemaId != null) {
            return false;
        }
        if (profileSchemaName != null ? !profileSchemaName.equals(that.profileSchemaName) : that.profileSchemaName != null) {
            return false;
        }
        if (profileSchemaId != null ? !profileSchemaId.equals(that.profileSchemaId) : that.profileSchemaId != null) {
            return false;
        }
        if (notificationSchemaName != null ? !notificationSchemaName.equals(that.notificationSchemaName) : that.notificationSchemaName != null) {
            return false;
        }
        if (notificationSchemaId != null ? !notificationSchemaId.equals(that.notificationSchemaId) : that.notificationSchemaId != null) {
            return false;
        }
        if (logSchemaName != null ? !logSchemaName.equals(that.logSchemaName) : that.logSchemaName != null) {
            return false;
        }
        return !(logSchemaId != null ? !logSchemaId.equals(that.logSchemaId) : that.logSchemaId != null);

    }

    @Override
    public int hashCode() {
        int result = sdkProfile != null ? sdkProfile.hashCode() : 0;
        result = 31 * result + (aefMapDtoList != null ? aefMapDtoList.hashCode() : 0);
        result = 31 * result + (configurationSchemaName != null ? configurationSchemaName.hashCode() : 0);
        result = 31 * result + (configurationSchemaId != null ? configurationSchemaId.hashCode() : 0);
        result = 31 * result + (profileSchemaName != null ? profileSchemaName.hashCode() : 0);
        result = 31 * result + (profileSchemaId != null ? profileSchemaId.hashCode() : 0);
        result = 31 * result + (notificationSchemaName != null ? notificationSchemaName.hashCode() : 0);
        result = 31 * result + (notificationSchemaId != null ? notificationSchemaId.hashCode() : 0);
        result = 31 * result + (logSchemaName != null ? logSchemaName.hashCode() : 0);
        result = 31 * result + (logSchemaId != null ? logSchemaId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SdkProfileViewDto{" +
                "sdkProfile=" + sdkProfile +
                ", aefMapDtoList=" + aefMapDtoList +
                ", configurationSchemaName='" + configurationSchemaName + '\'' +
                ", configurationSchemaId='" + configurationSchemaId + '\'' +
                ", profileSchemaName='" + profileSchemaName + '\'' +
                ", profileSchemaId='" + profileSchemaId + '\'' +
                ", notificationSchemaName='" + notificationSchemaName + '\'' +
                ", notificationSchemaId='" + notificationSchemaId + '\'' +
                ", logSchemaName='" + logSchemaName + '\'' +
                ", logSchemaId='" + logSchemaId + '\'' +
                '}';
    }
}
