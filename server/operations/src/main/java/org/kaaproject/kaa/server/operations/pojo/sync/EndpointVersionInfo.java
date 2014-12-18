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
package org.kaaproject.kaa.server.operations.pojo.sync;

import java.util.List;

public class EndpointVersionInfo {
    private int configVersion;
    private int profileVersion;
    private int systemNfVersion;
    private int userNfVersion;
    private List<EventClassFamilyVersionInfo> eventFamilyVersions;
    private int logSchemaVersion;

    /**
     * Default constructor. Note that this does not initialize fields to their
     * default values from the schema. If that is desired then one should use
     * {@link \#newBuilder()}.
     */
    public EndpointVersionInfo() {
    }

    /**
     * All-args constructor.
     */
    public EndpointVersionInfo(Integer configVersion, Integer profileVersion, Integer systemNfVersion, Integer userNfVersion,
            List<EventClassFamilyVersionInfo> eventFamilyVersions, Integer logSchemaVersion) {
        this.configVersion = configVersion;
        this.profileVersion = profileVersion;
        this.systemNfVersion = systemNfVersion;
        this.userNfVersion = userNfVersion;
        this.eventFamilyVersions = eventFamilyVersions;
        this.logSchemaVersion = logSchemaVersion;
    }

    /**
     * Gets the value of the 'configVersion' field.
     */
    public Integer getConfigVersion() {
        return configVersion;
    }

    /**
     * Sets the value of the 'configVersion' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfigVersion(Integer value) {
        this.configVersion = value;
    }

    /**
     * Gets the value of the 'profileVersion' field.
     */
    public Integer getProfileVersion() {
        return profileVersion;
    }

    /**
     * Sets the value of the 'profileVersion' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setProfileVersion(Integer value) {
        this.profileVersion = value;
    }

    /**
     * Gets the value of the 'systemNfVersion' field.
     */
    public Integer getSystemNfVersion() {
        return systemNfVersion;
    }

    /**
     * Sets the value of the 'systemNfVersion' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setSystemNfVersion(Integer value) {
        this.systemNfVersion = value;
    }

    /**
     * Gets the value of the 'userNfVersion' field.
     */
    public Integer getUserNfVersion() {
        return userNfVersion;
    }

    /**
     * Sets the value of the 'userNfVersion' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserNfVersion(Integer value) {
        this.userNfVersion = value;
    }

    /**
     * Gets the value of the 'eventFamilyVersions' field.
     */
    public List<EventClassFamilyVersionInfo> getEventFamilyVersions() {
        return eventFamilyVersions;
    }

    /**
     * Sets the value of the 'eventFamilyVersions' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventFamilyVersions(List<EventClassFamilyVersionInfo> value) {
        this.eventFamilyVersions = value;
    }

    /**
     * Gets the value of the 'logSchemaVersion' field.
     */
    public Integer getLogSchemaVersion() {
        return logSchemaVersion;
    }

    /**
     * Sets the value of the 'logSchemaVersion' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setLogSchemaVersion(Integer value) {
        this.logSchemaVersion = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + configVersion;
        result = prime * result + ((eventFamilyVersions == null) ? 0 : eventFamilyVersions.hashCode());
        result = prime * result + logSchemaVersion;
        result = prime * result + profileVersion;
        result = prime * result + systemNfVersion;
        result = prime * result + userNfVersion;
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
        EndpointVersionInfo other = (EndpointVersionInfo) obj;
        if (configVersion != other.configVersion) {
            return false;
        }
        if (eventFamilyVersions == null) {
            if (other.eventFamilyVersions != null) {
                return false;
            }
        } else if (!eventFamilyVersions.equals(other.eventFamilyVersions)) {
            return false;
        }
        if (logSchemaVersion != other.logSchemaVersion) {
            return false;
        }
        if (profileVersion != other.profileVersion) {
            return false;
        }
        if (systemNfVersion != other.systemNfVersion) {
            return false;
        }
        if (userNfVersion != other.userNfVersion) {
            return false;
        }
        return true;
    }

}
