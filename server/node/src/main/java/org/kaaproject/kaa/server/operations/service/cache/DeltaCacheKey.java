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

    private static final long serialVersionUID = 1L;

    private final AppVersionKey appConfigVersionKey;

    private final List<EndpointGroupStateDto> endpointGroups;

    private final EndpointObjectHash endpointConfHash;

    // indicates that client want to receive resync based on base schema
    private final boolean resyncOnly;

    private final boolean useConfigurationRawSchema;

    private final EndpointObjectHash userConfHash;


    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups, EndpointObjectHash userConfHash,
            EndpointObjectHash endpointConfHash) {
        this(appConfigVersionKey, endpointGroups, userConfHash, endpointConfHash, true, false);
    }

    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups, EndpointObjectHash userConfHash,
                         EndpointObjectHash endpointConfHash, boolean useConfigurationRawSchema) {
        this(appConfigVersionKey, endpointGroups, userConfHash, endpointConfHash, useConfigurationRawSchema, false);
    }

    public DeltaCacheKey(AppVersionKey appConfigVersionKey, List<EndpointGroupStateDto> endpointGroups,
                         EndpointObjectHash userConfHash, EndpointObjectHash endpointConfHash, boolean useConfigurationRawSchema, boolean resyncOnly) {
        this.appConfigVersionKey = appConfigVersionKey;
        this.userConfHash = userConfHash;
        this.endpointGroups = endpointGroups;
        this.endpointConfHash = endpointConfHash;
        this.useConfigurationRawSchema = useConfigurationRawSchema;
        this.resyncOnly = resyncOnly;

    }


    public AppVersionKey getAppConfigVersionKey() {
        return appConfigVersionKey;
    }


    public List<EndpointGroupStateDto> getEndpointGroups() {
        return endpointGroups;
    }


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

    public boolean isUseConfigurationRawSchema() {
        return useConfigurationRawSchema;
    }

    public EndpointObjectHash getUserConfHash() {
        return userConfHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeltaCacheKey that = (DeltaCacheKey) o;

        if (resyncOnly != that.resyncOnly) return false;
        if (useConfigurationRawSchema != that.useConfigurationRawSchema) return false;
        if (appConfigVersionKey != null ? !appConfigVersionKey.equals(that.appConfigVersionKey) : that.appConfigVersionKey != null)
            return false;
        if (endpointGroups != null ? !endpointGroups.equals(that.endpointGroups) : that.endpointGroups != null)
            return false;
        if (endpointConfHash != null ? !endpointConfHash.equals(that.endpointConfHash) : that.endpointConfHash != null)
            return false;
        return userConfHash != null ? userConfHash.equals(that.userConfHash) : that.userConfHash == null;

    }

    @Override
    public int hashCode() {
        int result = appConfigVersionKey != null ? appConfigVersionKey.hashCode() : 0;
        result = 31 * result + (endpointGroups != null ? endpointGroups.hashCode() : 0);
        result = 31 * result + (endpointConfHash != null ? endpointConfHash.hashCode() : 0);
        result = 31 * result + (resyncOnly ? 1 : 0);
        result = 31 * result + (useConfigurationRawSchema ? 1 : 0);
        result = 31 * result + (userConfHash != null ? userConfHash.hashCode() : 0);
        return result;
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
        builder.append(", useRawSchema=");
        builder.append(useConfigurationRawSchema);
        builder.append(", userConfHash=");
        builder.append(userConfHash);
        builder.append("]");
        return builder.toString();
    }
}
