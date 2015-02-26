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

import java.util.Collection;
import java.util.List;

import org.kaaproject.kaa.server.common.thrift.gen.operations.EventMessage;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;

/**
 * EventService interface.
 * Provides ability to send event messages:
 *  UserRouteInfo - used to notify all neighbors that specified user want to create specific event group
 *  RouteInfo  - used to notify specified list of neighbors that specified endpoint want be part of event group
 *  RemoteEndpointEvent - used to send event to specific event group
 * @author Andrey Panasenko
 *
 */
public interface EventService {

    /**
     * OperationsServerConfig getter.
     * @return OperationsServerConfig
     */
    public OperationsServerConfig getConfig();

    /**
     * OperationsServerConfig setter.
     * @param OperationsServerConfig config
     */
    public void setConfig(OperationsServerConfig config);

    /**
     * Return list of all neighbor connections
     * @return List<NeighborConnection>
     */
    public List<NeighborConnection> getNeighbors();

    /**
     * Stop Event service.
     */
    public void shutdown();

    /**
     * Send Remote Endpoint Event to specified in event neighbor,
     * neighbor used from remoteEndpointEvent.getRecipient().getServerId();
     * @param event RemoteEndpointEvent
     */
    public void sendEvent(RemoteEndpointEvent event);

    /**
     * Send RouteInfo to specified list of operations servers.
     * null in serverIdList mean broadcast to all servers.
     * @param routeInfo RouteInfo
     * @param serverIdList list of operations servers in thriftHost:thriftPort format.
     */
    public void sendRouteInfo(RouteInfo routeInfo, String... serverIdList);

    /**
     * Send collection of RouteInfos to specified list of operations servers.
     * null in serverIdList mean broadcast to all servers.
     * @param routeInfos Collection<RouteInfo>
     * @param serverIdList list of operations servers in thriftHost:thriftPort format.
     */
    public void sendRouteInfo(Collection<RouteInfo> routeInfos, String... serverIdList);

    /**
     * Send UserRouteInfo to all neighbors,
     * @param routeInfo UserRouteInfo
     */
    public void sendUserRouteInfo(org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo);

    /**
     * Register event route engine listener, used to inform route engine on
     * Operations Server thrift interface calls.
     * @param listener EventServiceListener
     */
    public void addListener(EventServiceListener listener);

    /**
     * Deregister event route engine listener
     * @param listener EventServiceListener
     */
    public void removeListener(EventServiceListener listener);

    /**
     * Operations Server thrift interface, used to receive unified event message
     * which includes RouteInfo,UserRouteInfo and Event messages
     * @param messages List<EventMessage>
     */
    public void sendEventMessage(List<EventMessage> messages);

    /**
     * Used to set ZooKepper node.
     * @param operationsNode
     */
    public void setZkNode(OperationsNode operationsNode);

    /**
     * Used to get ZooKepper node.
     * @param operationsNode
     */
    public OperationsNode getZkNode();

    public boolean isMainUserNode(String userId);

    public void sendEndpointInfo(GlobalRouteInfo routeInfo);

    public String getServerId();


}
