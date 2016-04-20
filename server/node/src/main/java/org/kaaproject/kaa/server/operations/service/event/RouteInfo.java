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

package org.kaaproject.kaa.server.operations.service.event;

import java.util.List;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;

public final class RouteInfo extends ClusterRouteInfo{

    private final List<EventClassFamilyVersion> ecfVersions;

    public RouteInfo(String tenantId, String userId, RouteTableAddress address, List<EventClassFamilyVersion> ecfVersions) {
        this(tenantId, userId, address, ecfVersions, RouteOperation.ADD);
    }

    public RouteInfo(String tenantId, String userId, RouteTableAddress address, List<EventClassFamilyVersion> ecfVersions, RouteOperation routeOperation) {
        super(tenantId, userId, address, routeOperation);
        this.ecfVersions = ecfVersions;
    }

    public static RouteInfo deleteRouteFromAddress(String tenantId, String userId, RouteTableAddress address){
        return new RouteInfo(tenantId, userId, address, null, RouteOperation.DELETE);
    }

    public List<EventClassFamilyVersion> getEcfVersions() {
        return ecfVersions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ecfVersions == null) ? 0 : ecfVersions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RouteInfo other = (RouteInfo) obj;
        if (ecfVersions == null) {
            if (other.ecfVersions != null) {
                return false;
            }
        } else if (!ecfVersions.equals(other.ecfVersions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RouteInfo [ecfVersions=");
        builder.append(ecfVersions);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", address=");
        builder.append(address);
        builder.append(", routeOperation=");
        builder.append(routeOperation);
        builder.append("]");
        return builder.toString();
    }
}
