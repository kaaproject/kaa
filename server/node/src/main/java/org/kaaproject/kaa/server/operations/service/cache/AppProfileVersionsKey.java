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
 * The Class AppVersionKey is used to model pair of Application Token and
 * version. Either Profile or Configuration version or App seqNumber may be
 * used, which depends on context.
 * 
 * @author ashvayka
 */
public final class AppProfileVersionsKey implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The application token. */
    private final String applicationToken;

    /** The version. */
    private final Integer endpointProfileSchemaVersion;

    /** The version. */
    private final Integer serverProfileSchemaVersion;

    /**
     * Instantiates a new app version key.
     *
     * @param applicationToken
     *            the application token
     * @param endpointProfileSchemaVersion
     *            the endpoint profile schema version
     * @param serverProfileSchemaVersion
     *            the server profile schema version
     */
    public AppProfileVersionsKey(String applicationToken, Integer endpointProfileSchemaVersion, Integer serverProfileSchemaVersion) {
        super();
        this.applicationToken = applicationToken;
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public Integer getEndpointProfileSchemaVersion() {
        return endpointProfileSchemaVersion;
    }

    public Integer getServerProfileSchemaVersion() {
        return serverProfileSchemaVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + ((endpointProfileSchemaVersion == null) ? 0 : endpointProfileSchemaVersion.hashCode());
        result = prime * result + ((serverProfileSchemaVersion == null) ? 0 : serverProfileSchemaVersion.hashCode());
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
        AppProfileVersionsKey other = (AppProfileVersionsKey) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (endpointProfileSchemaVersion == null) {
            if (other.endpointProfileSchemaVersion != null) {
                return false;
            }
        } else if (!endpointProfileSchemaVersion.equals(other.endpointProfileSchemaVersion)) {
            return false;
        }
        if (serverProfileSchemaVersion == null) {
            if (other.serverProfileSchemaVersion != null) {
                return false;
            }
        } else if (!serverProfileSchemaVersion.equals(other.serverProfileSchemaVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AppProfileVersionsKey [applicationToken=" + applicationToken + ", endpointProfileSchemaVersion="
                + endpointProfileSchemaVersion + ", serverProfileSchemaVersion=" + serverProfileSchemaVersion + "]";
    }

}