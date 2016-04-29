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

import java.util.Collection;
import java.util.List;

import org.kaaproject.kaa.server.common.thrift.gen.operations.Message;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;

/**
 * EventService interface. Provides ability to send event messages:
 * UserRouteInfo - used to notify all neighbors that specified user want to
 * create specific event group RouteInfo - used to notify specified list of
 * neighbors that specified endpoint want be part of event group
 * RemoteEndpointEvent - used to send event to specific event group
 * 
 * @author Andrey Panasenko
 * @author Andrew Svhayka
 *
 */
public interface EventService {

    /**
     * Stop Event service.
     */
    void shutdown();

    /**
     * Send Remote Endpoint Event to specified in event neighbor, neighbor used
     * from remoteEndpointEvent.getRecipient().getServerId();
     * 
     * @param event
     *            RemoteEndpointEvent
     */
    void sendEvent(RemoteEndpointEvent event);

    /**
     * Send RouteInfo to specified list of operations servers. null in
     * serverIdList mean broadcast to all servers.
     * 
     * @param routeInfo
     *            RouteInfo
     * @param serverIdList
     *            list of operations servers in thriftHost:thriftPort format.
     */
    void sendRouteInfo(RouteInfo routeInfo, String... serverIdList);

    /**
     * Send collection of RouteInfos to specified list of operations servers.
     * null in serverIdList mean broadcast to all servers.
     * 
     * @param routeInfos
     *            Collection of type RouteInfo
     * @param serverIdList
     *            list of operations servers in thriftHost:thriftPort format.
     */
    void sendRouteInfo(Collection<RouteInfo> routeInfos, String... serverIdList);

    /**
     * Send UserRouteInfo to all neighbors,
     * 
     * @param routeInfo
     *            UserRouteInfo
     */
    void sendUserRouteInfo(org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo);

    /**
     * Register event route engine listener, used to inform route engine on
     * Operations Server thrift interface calls.
     * 
     * @param listener
     *            EventServiceListener
     */
    void addListener(EventServiceListener listener);

    /**
     * Deregister event route engine listener
     * 
     * @param listener
     *            EventServiceListener
     */
    void removeListener(EventServiceListener listener);

    /**
     * Operations Server thrift interface, used to receive unified event message
     * which includes RouteInfo,UserRouteInfo and Event messages
     * 
     * @param messages
     *            List of type EventMessage
     */
    void sendEventMessage(List<Message> messages);

    /**
     * Used to set ZooKepper node.
     * 
     * @param operationsNode the operations node
     */
    void setZkNode(OperationsNode operationsNode);

    /**
     * Used to set {@link OperationsServerResolver}.
     * 
     * @param resolver
     *            to set
     */
    void setResolver(OperationsServerResolver resolver);

    /**
     * Sends routing information about endpoint to global user actor
     * 
     * @param route the route
     */
    void sendEndpointRouteInfo(GlobalRouteInfo route);

    /**
     * Sends configuration update information to specific endpoint actor;
     * 
     * @param serverId the server id
     * @param update the update
     */
    void sendEndpointStateInfo(String serverId, EndpointUserConfigurationUpdate update);

    /**
     * Checks if global user actor for specified user is located on current node
     * 
     * @param userId
     *            to check
     * @return true if global user actor is located on this node, false
     *         otherwise.
     */
    boolean isMainUserNode(String userId);

    /**
     * Returns id of the node that should contain global user actor
     * @param userId the user id
     * @return id of the global user actor node
     */
    String getUserNode(String userId);
}
