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

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;


/**
 * The Class ConfigurationIdKey is used to model unique key for configuration id lookup (from cache/db).
 * UK consist of applicationToken, applicationSeqNumber, configuration schema version and endpoint group.
 * Indeed, only one configuration can be active for particular application, endpoint group and version in a single period of time (applicationSeqNumber)
 * 
 * @author ashvayka
 */
public final class ConfigurationIdKey implements Serializable{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6999369799797467377L;
    
    
    /** The application token. */
    private final String applicationToken;
    
    /** The application seq number. */
    private final int applicationSeqNumber;
    
    /** The config schema version. */
    private final int configSchemaVersion;
    
    /** The endpoint group id. */
    private final String endpointGroupId;
    
    /**
     * Instantiates a new configuration id key.
     *
     * @param applicationId the application id
     * @param applicationSeqNumber the application seq number
     * @param configSchemaVersion the config schema version
     */
    public ConfigurationIdKey(String applicationId, int applicationSeqNumber, int configSchemaVersion) {
        this(applicationId, applicationSeqNumber, configSchemaVersion, null);
    }

    /**
     * Instantiates a new configuration id key.
     *
     * @param applicationToken the application token
     * @param applicationSeqNumber the application seq number
     * @param configSchemaVersion the config schema version
     * @param endpointGroupId the endpoint group id
     */
    public ConfigurationIdKey(String applicationToken, int applicationSeqNumber, int configSchemaVersion,
            String endpointGroupId) {
        super();
        this.applicationToken = applicationToken;
        this.applicationSeqNumber = applicationSeqNumber;
        this.configSchemaVersion = configSchemaVersion;
        this.endpointGroupId = endpointGroupId;
    }

    /**
     * Gets the application token.
     *
     * @return the application token
     */
    public String getApplicationToken() {
        return applicationToken;
    }

    /**
     * Gets the config schema version.
     *
     * @return the config schema version
     */
    public int getConfigSchemaVersion() {
        return configSchemaVersion;
    }

    /**
     * Gets the endpoint group id.
     *
     * @return the endpoint group id
     */
    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    /**
     * Sets the endpoint group id.
     *
     * @param endpointGroupId the new endpoint group id
     * @return the configuration id key
     */
    public ConfigurationIdKey copyWithNewEGId(String endpointGroupId) {
        return new ConfigurationIdKey(this.applicationToken, this.applicationSeqNumber, this.configSchemaVersion, endpointGroupId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + applicationSeqNumber;
        result = prime * result + configSchemaVersion;
        result = prime * result + ((endpointGroupId == null) ? 0 : endpointGroupId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        ConfigurationIdKey other = (ConfigurationIdKey) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (applicationSeqNumber != other.applicationSeqNumber) {
            return false;
        }
        if (configSchemaVersion != other.configSchemaVersion) {
            return false;
        }
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
            return false;
        }
        return true;
    }
}
