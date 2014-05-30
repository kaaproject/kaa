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

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

/**
 * The Class DeltaCacheKey is used to model key of cache entry for delta calculation.
 * Contains appToken, appSeqNumber, list of active endpoint groups and old configuration hash. 
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
    
    /**
     * Instantiates a new delta cache key.
     *
     * @param appConfigVersionKey the app config version key
     * @param endpointGroups the endpoint groups
     * @param endpointConfHash the endpoint conf hash
     */
    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups, EndpointObjectHash endpointConfHash) {
        this.appConfigVersionKey = appConfigVersionKey;
        this.endpointGroups = endpointGroups;
        this.endpointConfHash = endpointConfHash;
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appConfigVersionKey == null) ? 0 : appConfigVersionKey.hashCode());
        result = prime * result + ((endpointConfHash == null) ? 0 : endpointConfHash.hashCode());
        result = prime * result + ((endpointGroups == null) ? 0 : endpointGroups.hashCode());
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
        return true;
    }
}
