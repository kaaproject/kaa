/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

/**
 * The Class DeltaCacheKey is used to model key of cache entry for delta
 * calculation. Contains appToken, appSeqNumber, list of active endpoint groups
 * and old configuration hash.
 * 
 * @author ashvayka
 */
public final class DeltaCacheKey implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The app id. */
    private final AppVersionKey appConfigVersionKey;

    /** The endpoint groups. */
    private final List<EndpointGroupStateDto> endpointGroups;

    /** The endpoint conf hash. */
    private final EndpointObjectHash endpointConfHash;

    /** Supports only resync delta encoded using base schema */
    private final boolean resyncOnly;

    /** The user conf hash. */
    private final EndpointObjectHash userConfHash;

    /**
     * Instantiates a new delta cache key.
     *
     * @param appConfigVersionKey
     *            the app config version key
     * @param endpointGroups
     *            the endpoint groups
     * @param userConfHash
     *            the user conf hash
     * @param endpointConfHash
     *            the endpoint conf hash
     */
    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups, EndpointObjectHash userConfHash,
            EndpointObjectHash endpointConfHash) {
        this(appConfigVersionKey, endpointGroups, userConfHash, endpointConfHash, false);
    }

    /**
     * Instantiates a new delta cache key.
     *
     * @param appConfigVersionKey
     *            the app config version key
     * @param endpointGroups
     *            the endpoint groups
     * @param userConfHash
     *            the user conf hash
     * @param endpointConfHash
     *            the endpoint conf hash
     * @param resyncOnly
     *            indicates that client want to receive resync based on base
     *            schema
     */
    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups,
            EndpointObjectHash userConfHash, EndpointObjectHash endpointConfHash, boolean resyncOnly) {
        this.appConfigVersionKey = appConfigVersionKey;
        this.userConfHash = userConfHash;
        this.endpointGroups = endpointGroups;
        this.endpointConfHash = endpointConfHash;
        this.resyncOnly = resyncOnly;
    }

    /**
     * Gets the app config version key.
     *
     * @return the app config version key
     */
    public AppVersionKey getAppConfigVersionKey() {
        return appConfigVersionKey;
    }

    /**
     * Gets the endpoint groups.
     *
     * @return the endpoint groups
     */
    public List<EndpointGroupStateDto> getEndpointGroups() {
        return endpointGroups;
    }

    /**
     * Gets the endpoint conf hash.
     *
     * @return the endpoint conf hash
     */
    public EndpointObjectHash getEndpointConfHash() {
        return endpointConfHash;
    }

    /**
     * Indicate that client supports only resync delta encoded using base
     * schema.
     *
     * @return the resync only flag
     */
    public boolean isResyncOnly() {
        return resyncOnly;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public EndpointObjectHash getUserConfHash() {
        return userConfHash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appConfigVersionKey == null) ? 0 : appConfigVersionKey.hashCode());
        result = prime * result + ((endpointConfHash == null) ? 0 : endpointConfHash.hashCode());
        result = prime * result + ((endpointGroups == null) ? 0 : endpointGroups.hashCode());
        result = prime * result + (resyncOnly ? 1231 : 1237);
        result = prime * result + ((userConfHash == null) ? 0 : userConfHash.hashCode());
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
        DeltaCacheKey other = (DeltaCacheKey) obj;
        if (appConfigVersionKey == null) {
            if (other.appConfigVersionKey != null) {
                return false;
            }
        } else if (!appConfigVersionKey.equals(other.appConfigVersionKey)) {
            return false;
        }
        if (endpointConfHash == null) {
            if (other.endpointConfHash != null) {
                return false;
            }
        } else if (!endpointConfHash.equals(other.endpointConfHash)) {
            return false;
        }
        if (endpointGroups == null) {
            if (other.endpointGroups != null) {
                return false;
            }
        } else if (!endpointGroups.equals(other.endpointGroups)) {
            return false;
        }
        if (resyncOnly != other.resyncOnly) {
            return false;
        }
        if (userConfHash == null) {
            if (other.userConfHash != null) {
                return false;
            }
        } else if (!userConfHash.equals(other.userConfHash)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DeltaCacheKey [appConfigVersionKey=");
        builder.append(appConfigVersionKey);
        builder.append(", endpointGroups=");
        builder.append(endpointGroups);
        builder.append(", endpointConfHash=");
        builder.append(endpointConfHash);
        builder.append(", resyncOnly=");
        builder.append(resyncOnly);
        builder.append(", userConfHash=");
        builder.append(userConfHash);
        builder.append("]");
        return builder.toString();
    }
}
