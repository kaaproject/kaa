package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteTable;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;

public class GlobalEndpointActorMessageProcessor extends AbstractEndpointActorMessageProcessor<GlobalEndpointActorState> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalEndpointActorMessageProcessor.class);

    private final String nodeId;
    private final RouteTable<EndpointClusterAddress> routes;
    private final ClusterService clusterService;

    public GlobalEndpointActorMessageProcessor(AkkaContext context, String appToken, EndpointObjectHash key, String actorKey) {
        super(new GlobalEndpointActorState(Base64Util.encode(key.getData()), actorKey), context.getOperationsService(), appToken, key,
                actorKey, Base64Util.encode(key.getData()), context.getGlobalEndpointTimeout());
        clusterService = context.getClusterService();
        nodeId = context.getClusterService().getNodeId();
        routes = new RouteTable<>(nodeId);
    }

    public void processRouteMessage(EndpointRouteMessage message) {
        LOG.debug("[{}] Processing {} operation for address {}", endpointKey, message.getOperation(), message.getAddress());
        switch (message.getOperation()) {
        case ADD:
        case UPDATE:
            routes.add(message.getAddress());
            break;
        case DELETE:
            routes.remove(message.getAddress());
            break;
        default:
            break;
        }
    }

    public void processClusterUpdate(ActorContext context) {
        if (!clusterService.isMainEntityNode(key)) {
            LOG.debug("[{}] No longer a global endpoint node for {}", endpointKey);
            routes.clear();
            context.stop(context.self());
        }
    }

    public void processEndpointActorMsg(EndpointActorMsg msg) {
        if (msg instanceof ThriftEndpointActorMsg) {
            processThriftMsg((ThriftEndpointActorMsg<?>) msg);
        }
    }

    private void processThriftMsg(ThriftEndpointActorMsg<?> msg) {
        Object thriftMsg = msg.getMsg();
        if (thriftMsg instanceof ThriftServerProfileUpdateMessage) {
            processServerProfileUpdateMsg(msg.getAddress(), (ThriftServerProfileUpdateMessage) thriftMsg);
        } else if (thriftMsg instanceof ThriftUnicastNotificationMessage) {
            processUnicastNotificationMsg(msg.getAddress(), (ThriftUnicastNotificationMessage) thriftMsg);
        }
    }

    private void processServerProfileUpdateMsg(EndpointAddress address, ThriftServerProfileUpdateMessage thriftMsg) {
        // TODO Auto-generated method stub
    }

    private void processUnicastNotificationMsg(EndpointAddress address, ThriftUnicastNotificationMessage thriftMsg) {
        // TODO Auto-generated method stub

    }

}
