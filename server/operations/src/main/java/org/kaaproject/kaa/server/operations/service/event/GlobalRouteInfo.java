package org.kaaproject.kaa.server.operations.service.event;

public final class GlobalRouteInfo extends ClusterRouteInfo{

    private final int cfVersion;
    
    public GlobalRouteInfo(String tenantId, String userId, RouteTableAddress address, int cfVersion, RouteOperation routeOperation) {
        super(tenantId, userId, address, routeOperation);
        this.cfVersion = cfVersion;
    }

    public int getCfVersion() {
        return cfVersion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GlobalRouteInfo [cfVersion=");
        builder.append(cfVersion);
        builder.append("]");
        return builder.toString();
    }

}
