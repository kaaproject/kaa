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

import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;


public final class UserRouteInfo {

    private final String serverId;
    private final String tenantId;
    private final String userId;
    private final RouteOperation routeOperation;

    public UserRouteInfo(String tenantId, String userId) {
        this(tenantId, userId, null, RouteOperation.ADD);
    }

    public UserRouteInfo(String tenantId, String userId, String serverId, RouteOperation routeOperation) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.serverId = serverId;
        this.routeOperation = routeOperation;
    }

    public String getServerId() {
        return serverId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public RouteOperation getRouteOperation() {
        return routeOperation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserRouteInfo [serverId=");
        builder.append(serverId);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", routeOperation=");
        builder.append(routeOperation);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((routeOperation == null) ? 0 : routeOperation.hashCode());
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
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
        UserRouteInfo other = (UserRouteInfo) obj;
        if (routeOperation != other.routeOperation) {
            return false;
        }
        if (serverId == null) {
            if (other.serverId != null) {
                return false;
            }
        } else if (!serverId.equals(other.serverId)) {
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
}
