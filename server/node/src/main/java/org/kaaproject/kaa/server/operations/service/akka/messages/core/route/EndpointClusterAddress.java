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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointClusterAddress extends EntityClusterAddress {
 
    private final EndpointObjectHash endpointKey;

    public EndpointClusterAddress(EntityClusterAddress parent, EndpointObjectHash endpointKey) {
        this(parent.getNodeId(), parent.getTenantId(), parent.getAppToken(), endpointKey);
    }

    public EndpointClusterAddress(String nodeId, String tenantId, String appToken, EndpointObjectHash endpointKey) {
        super(nodeId, tenantId, appToken);
        this.endpointKey = endpointKey;
    }

    public EndpointObjectHash getEndpointKey() {
        return endpointKey;
    }
    
    public EndpointAddress toEndpointAddress(){
        return new EndpointAddress(getTenantId(), getAppToken(), endpointKey);
    }

    @Override
    public byte[] getEntityId() {
        return endpointKey.getData();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endpointKey == null) ? 0 : endpointKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointClusterAddress other = (EndpointClusterAddress) obj;
        if (endpointKey == null) {
            if (other.endpointKey != null)
                return false;
        } else if (!endpointKey.equals(other.endpointKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EndpointClusterAddress [endpointKey=" + endpointKey + ", getNodeId()=" + getNodeId() + ", getTenantId()="
                + getTenantId() + ", getAppToken()=" + getAppToken() + "]";
    }

}
