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

public abstract class EntityAddress {
    private final String tenantId;
    private final String appToken;

    public EntityAddress(String tenantId, String appToken) {
        super();
        this.tenantId = tenantId;
        this.appToken = appToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppToken() {
        return appToken;
    }

    abstract public byte[] getEntityId();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityAddress other = (EntityAddress) obj;
        if (appToken == null) {
            if (other.appToken != null)
                return false;
        } else if (!appToken.equals(other.appToken))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }

}
