package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public class EndpointRouteMessage extends RouteMessage<EndpointClusterAddress> {

    public EndpointRouteMessage(EndpointClusterAddress address, RouteOperation operation) {
        super(address, operation);
    }

}
