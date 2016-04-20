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

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.VersionDto;

public class SchemaVersions implements Serializable {

    private static final long serialVersionUID = 2648014748733736078L;

    private List<VersionDto> configurationSchemaVersions;
    private List<VersionDto> profileSchemaVersions;
    private List<VersionDto> notificationSchemaVersions;
    private List<VersionDto> logSchemaVersions;

    public SchemaVersions() {
    }

    public List<VersionDto> getConfigurationSchemaVersions() {
        return configurationSchemaVersions;
    }

    public void setConfigurationSchemaVersions(
            List<VersionDto> configurationSchemaVersions) {
        this.configurationSchemaVersions = configurationSchemaVersions;
    }

    public List<VersionDto> getProfileSchemaVersions() {
        return profileSchemaVersions;
    }

    public void setProfileSchemaVersions(List<VersionDto> profileSchemaVersions) {
        this.profileSchemaVersions = profileSchemaVersions;
    }

    public List<VersionDto> getNotificationSchemaVersions() {
        return notificationSchemaVersions;
    }

    public void setNotificationSchemaVersions(
            List<VersionDto> notificationSchemaVersions) {
        this.notificationSchemaVersions = notificationSchemaVersions;
    }

    public List<VersionDto> getLogSchemaVersions() {
        return logSchemaVersions;
    }

    public void setLogSchemaVersions(List<VersionDto> logSchemaVersions) {
        this.logSchemaVersions = logSchemaVersions;
    }

}
