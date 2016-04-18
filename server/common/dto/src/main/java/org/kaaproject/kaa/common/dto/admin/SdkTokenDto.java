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

public class SdkTokenDto implements Serializable {

    private static final long serialVersionUID = 7784970390585790120L;
    
    protected Integer configurationSchemaVersion;
    protected Integer profileSchemaVersion;
    protected Integer notificationSchemaVersion;
    protected Integer logSchemaVersion;
    protected List<String> aefMapIds;
    protected String defaultVerifierToken;
    protected String applicationToken;
    protected String name;
    
    public SdkTokenDto() {
        super();
    }
    
    public SdkTokenDto(Integer configurationSchemaVersion,
            Integer profileSchemaVersion, Integer notificationSchemaVersion,
            Integer logSchemaVersion, List<String> aefMapIds,
            String defaultVerifierToken, String applicationToken, String name) {
        super();
        this.configurationSchemaVersion = configurationSchemaVersion;
        this.profileSchemaVersion = profileSchemaVersion;
        this.notificationSchemaVersion = notificationSchemaVersion;
        this.logSchemaVersion = logSchemaVersion;
        this.aefMapIds = aefMapIds;
        this.defaultVerifierToken = defaultVerifierToken;
        this.applicationToken = applicationToken;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((aefMapIds == null) ? 0 : aefMapIds.hashCode());
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime
                * result
                + ((configurationSchemaVersion == null) ? 0
                        : configurationSchemaVersion.hashCode());
        result = prime
                * result
                + ((defaultVerifierToken == null) ? 0 : defaultVerifierToken
                        .hashCode());
        result = prime
                * result
                + ((logSchemaVersion == null) ? 0 : logSchemaVersion.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime
                * result
                + ((notificationSchemaVersion == null) ? 0
                        : notificationSchemaVersion.hashCode());
        result = prime
                * result
                + ((profileSchemaVersion == null) ? 0 : profileSchemaVersion
                        .hashCode());
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
        SdkTokenDto other = (SdkTokenDto) obj;
        if (aefMapIds == null) {
            if (other.aefMapIds != null) {
                return false;
            }
        } else if (!aefMapIds.equals(other.aefMapIds)) {
            return false;
        }
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (configurationSchemaVersion == null) {
            if (other.configurationSchemaVersion != null) {
                return false;
            }
        } else if (!configurationSchemaVersion
                .equals(other.configurationSchemaVersion)) {
            return false;
        }
        if (defaultVerifierToken == null) {
            if (other.defaultVerifierToken != null) {
                return false;
            }
        } else if (!defaultVerifierToken.equals(other.defaultVerifierToken)) {
            return false;
        }
        if (logSchemaVersion == null) {
            if (other.logSchemaVersion != null) {
                return false;
            }
        } else if (!logSchemaVersion.equals(other.logSchemaVersion)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (notificationSchemaVersion == null) {
            if (other.notificationSchemaVersion != null) {
                return false;
            }
        } else if (!notificationSchemaVersion
                .equals(other.notificationSchemaVersion)) {
            return false;
        }
        if (profileSchemaVersion == null) {
            if (other.profileSchemaVersion != null) {
                return false;
            }
        } else if (!profileSchemaVersion.equals(other.profileSchemaVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SdkTokenDto [configurationSchemaVersion=");
        builder.append(configurationSchemaVersion);
        builder.append(", profileSchemaVersion=");
        builder.append(profileSchemaVersion);
        builder.append(", notificationSchemaVersion=");
        builder.append(notificationSchemaVersion);
        builder.append(", logSchemaVersion=");
        builder.append(logSchemaVersion);
        builder.append(", aefMapIds=");
        builder.append(aefMapIds);
        builder.append(", defaultVerifierToken=");
        builder.append(defaultVerifierToken);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append(", name=");
        builder.append(name);
        builder.append("]");
        return builder.toString();
    }
    
}
