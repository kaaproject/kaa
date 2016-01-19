package org.kaaproject.kaa.server.operations.service.cluster;

import java.util.List;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
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
     * Stops service.
     */
    void shutdown();

}
