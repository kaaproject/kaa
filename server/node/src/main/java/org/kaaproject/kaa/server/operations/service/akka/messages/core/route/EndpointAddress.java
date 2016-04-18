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

public class EndpointAddress extends EntityAddress {

    private final EndpointObjectHash endpointKey;

    public EndpointAddress(String tenantId, String appToken, EndpointObjectHash endpointKey) {
        super(tenantId, appToken);
        this.endpointKey = endpointKey;
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
        EndpointAddress other = (EndpointAddress) obj;
        if (endpointKey == null) {
            if (other.endpointKey != null)
                return false;
        } else if (!endpointKey.equals(other.endpointKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EndpointAddress [endpointKey=" + endpointKey + ", getTenantId()=" + getTenantId() + ", getAppToken()=" + getAppToken()
                + "]";
    }

}
