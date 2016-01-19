package org.kaaproject.kaa.server.operations.service.cluster;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;

public interface ClusterServiceListener {

    /**
     * Process remote EndpointRouteMessage.
     * 
     * @param msg
     *            the endpoint route message
     */ 
    void onRouteMsg(EndpointRouteMessage msg);

    /**
     * Process endpoint actor message
     * 
     * @param msg
     */
    void onEndpointActorMsg(EndpointActorMsg msg);

    
    /**
     * Reports update of cluster topology;
     */
    void onClusterUpdated();
    
}
