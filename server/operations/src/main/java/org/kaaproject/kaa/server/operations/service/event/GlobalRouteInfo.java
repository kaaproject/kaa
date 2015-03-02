package org.kaaproject.kaa.server.operations.service.event;

import java.util.Arrays;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRouteUpdateType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.GlobalRouteUpdate;

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
        builder.append("]");
        return builder.toString();
    }

    public static GlobalRouteInfo fromThrift(GlobalRouteUpdate message) {
        RouteTableAddress address = new RouteTableAddress(EndpointObjectHash.fromBytes(message.getRouteAddress().getEndpointKey()), message
                .getRouteAddress().getApplicationToken());
        RouteOperation operation = message.getUpdateType() == EventRouteUpdateType.ADD ? RouteOperation.ADD : RouteOperation.DELETE;
        return new GlobalRouteInfo(message.getTenantId(), message.getUserId(), address, message.getCfSchemaVersion(), message.getUcfHash(),
                operation);
    }

}
