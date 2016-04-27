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
