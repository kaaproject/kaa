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

package org.kaaproject.kaa.server.operations.service.cluster;

import java.util.List;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;

public interface ClusterService {

    /**
     * Used to set ZooKepper node.
     * 
     * @param operationsNode
     *            the operations node
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
     * Register cluster route listener, used to inform cluster engine on
     * Operations Server thrift interface calls.
     * 
     * @param listener
     *            EventServiceListener
     */
    void setListener(ClusterServiceListener listener);

    /**
     * Returns id of the current node
     * @return id of the current node
     */
    String getNodeId();
    
    /**
     * Checks if global entity actor for specified entity is located on current
     * node
     * 
     * @param entityId
     *            to check
     * @return true if global entity actor is located on this node, false
     *         otherwise.
     */
    boolean isMainEntityNode(EndpointObjectHash entityId);
    
    /**
     * Checks if global entity actor for specified entity is located on current
     * node
     * 
     * @param entityId
     *            to check
     * @return true if global entity actor is located on this node, false
     *         otherwise.
     */
    boolean isMainEntityNode(byte[] entityId);
    
    /**
     * Returns id of the node that should contain global entity actor
     * 
     * @param entityId
     *            the entity id
     * @return id of the global entity actor node
     */
    String getEntityNode(EndpointObjectHash entityId);

    /**
     * Returns id of the node that should contain global entity actor
     * 
     * @param entityId
     *            the entity id
     * @return id of the global entity actor node
     */
    String getEntityNode(byte[] entityId);

    /**
     * Send EndpointRouteMessage to the node that contains global entity actor.
     * 
     * @param msg
     *            the endpoint route message
     * @return id of the global entity actor node
     */
    String sendRouteMessage(EndpointRouteMessage msg);
    
    /**
     * Send unicast notification message to specified node
     * @param nodeId - id of the server node
     * @param msg the unicast notification message
     */
    void sendUnicastNotificationMessage(String nodeId, ThriftUnicastNotificationMessage msg);

    /**
     * Send server profile update message to specified node
     * @param nodeId - id of the server node
     * @param msg the unicast notification message
     */
    void sendServerProfileUpdateMessage(String nodeId, ThriftServerProfileUpdateMessage msg);

    /**
     * Process entity route messages
     * 
     * @param msgs
     *            the entity route messages
     */
    void onEntityRouteMessages(List<ThriftEntityRouteMessage> msgs);
    
    /**
     * Process unicast notification message
     * @param msg the unicast notification message
     */
    void onUnicastNotificationMessage(ThriftUnicastNotificationMessage msg);

    /**
     * Process server profile update message
     * @param msg the server profile update message
     */
    void onServerProfileUpdateMessage(ThriftServerProfileUpdateMessage msg);

    /**
     * Process endpoint de-registration message 
     * @param msg the endpoint de-registration message
     */
    void onEndpointDeregistrationMessage(ThriftEndpointDeregistrationMessage msg);

    /**
     * Stops service.
     */
    void shutdown();

}
