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

package org.kaaproject.kaa.server.operations.service.event;

import java.util.List;

public final class RouteInfo {

    private final String tenantId;
    private final String userId;
    private final RouteTableAddress address;
    private final List<EventClassFamilyVersion> ecfVersions;
    private final RouteOperation routeOperation;

    public RouteInfo(String tenantId, String userId, RouteTableAddress address, List<EventClassFamilyVersion> ecfVersions) {
        this(tenantId, userId, address, ecfVersions, RouteOperation.ADD);
    }

    public RouteInfo(String tenantId, String userId, RouteTableAddress address, List<EventClassFamilyVersion> ecfVersions, RouteOperation routeOperation) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.address = address;
        this.ecfVersions = ecfVersions;
        this.routeOperation = routeOperation;
    }

    public static RouteInfo deleteRouteFromAddress(String tenantId, String userId, RouteTableAddress address){
        return new RouteInfo(tenantId, userId, address, null, RouteOperation.DELETE);
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public RouteTableAddress getAddress() {
        return address;
    }

    public List<EventClassFamilyVersion> getEcfVersions() {
        return ecfVersions;
    }

    public RouteOperation getRouteOperation() {
        return routeOperation;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((ecfVersions == null) ? 0 : ecfVersions.hashCode());
        result = prime * result + ((routeOperation == null) ? 0 : routeOperation.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        RouteInfo other = (RouteInfo) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (ecfVersions == null) {
            if (other.ecfVersions != null) {
                return false;
            }
        } else if (!ecfVersions.equals(other.ecfVersions)) {
            return false;
        }
        if (routeOperation != other.routeOperation) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RouteInfo [tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", address=");
        builder.append(address);
        builder.append(", ecfVersions=");
        builder.append(ecfVersions);
        builder.append(", routeOperation=");
        builder.append(routeOperation);
        builder.append("]");
        return builder.toString();
    }


}
