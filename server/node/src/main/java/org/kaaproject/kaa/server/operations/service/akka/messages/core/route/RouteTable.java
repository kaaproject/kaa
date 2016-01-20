package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RouteTable<T extends EntityClusterAddress> {

    private final String nodeId;
    private final Set<T> localRoutes;
    private final Set<T> remoteRoutes;

    public RouteTable(String nodeId) {
        super();
        this.nodeId = nodeId;
        this.localRoutes = new HashSet<>();
        this.remoteRoutes = new HashSet<>();
    }

    public boolean add(T address) {
        if (nodeId.equals(address.getNodeId())) {
            return localRoutes.add(address);
        } else {
            return remoteRoutes.add(address);
        }
    }

    public boolean remove(T address) {
        if (nodeId.equals(address.getNodeId())) {
            return localRoutes.remove(address);
        } else {
            return remoteRoutes.remove(address);
        }
    }

    public Set<T> getRoutes() {
        return Stream.concat(localRoutes.stream(), remoteRoutes.stream()).collect(Collectors.toSet());
    }

    public Set<T> getLocalRoutes() {
        return localRoutes;
    }

    public Set<T> getRemoteRoutes() {
        return remoteRoutes;
    }

    public void clear() {
        localRoutes.clear();
        remoteRoutes.clear();
    }
}
