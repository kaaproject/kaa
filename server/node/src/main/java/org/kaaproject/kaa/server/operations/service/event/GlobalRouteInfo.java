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

import java.util.Arrays;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointRouteUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRouteUpdateType;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;

public final class GlobalRouteInfo extends ClusterRouteInfo {

    private final int cfVersion;
    private final byte[] ucfHash;
    
    private GlobalRouteInfo(String tenantId, String userId, RouteTableAddress address, int cfVersion, byte[] ucfHash,
            RouteOperation routeOperation) {
        super(tenantId, userId, address, routeOperation);
        this.cfVersion = cfVersion;
        this.ucfHash = ucfHash;
    }

    public static GlobalRouteInfo add(String tenantId, String userId, RouteTableAddress address, int cfVersion, byte[] ucfHash){
        return new GlobalRouteInfo(tenantId, userId, address, cfVersion, ucfHash, RouteOperation.ADD);
    }
    
    public static GlobalRouteInfo delete(String tenantId, String userId, RouteTableAddress address){
        return new GlobalRouteInfo(tenantId, userId, address, 0, null, RouteOperation.DELETE);
    }

    public int getCfVersion() {
        return cfVersion;
    }

    public byte[] getUcfHash() {
        return ucfHash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GlobalRouteInfo [cfVersion=");
        builder.append(cfVersion);
        builder.append(", ucfHash=");
        builder.append(Arrays.toString(ucfHash));
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

    public boolean isLocal() {
        return getAddress().getServerId() == null;
    }

    public static GlobalRouteInfo fromThrift(EndpointRouteUpdate message) {
        RouteTableAddress address = new RouteTableAddress(EndpointObjectHash.fromBytes(message.getRouteAddress().getEndpointKey()), message
                .getRouteAddress().getApplicationToken(), message.getRouteAddress().getOperationsServerId());
        RouteOperation operation = message.getUpdateType() == EventRouteUpdateType.ADD ? RouteOperation.ADD : RouteOperation.DELETE;
        return new GlobalRouteInfo(message.getTenantId(), message.getUserId(), address, message.getCfSchemaVersion(), message.getUcfHash(),
                operation);
    }

}
