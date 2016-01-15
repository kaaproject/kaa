package org.kaaproject.kaa.server.operations.service.route;

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
     * Reports update of cluster topology;
     */
    void onClusterUpdated();
    
}
