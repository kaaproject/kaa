package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ActorClassifier;
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

    @Override
    protected void processThriftMsg(ActorContext context, ThriftEndpointActorMsg<?> msg) {
        Object thriftMsg = msg.getMsg();
        if (thriftMsg instanceof ThriftServerProfileUpdateMessage) {
            processServerProfileUpdateMsg(context, (ThriftServerProfileUpdateMessage) thriftMsg);
        } else if (thriftMsg instanceof ThriftUnicastNotificationMessage) {
            processUnicastNotificationMsg(context, (ThriftUnicastNotificationMessage) thriftMsg);
        }
    }

    private void processServerProfileUpdateMsg(ActorContext context, ThriftServerProfileUpdateMessage thriftMsg) {
        // TODO Auto-generated method stub
    }

    private void processUnicastNotificationMsg(ActorContext context, ThriftUnicastNotificationMessage thriftMsg) {
        ThriftUnicastNotificationMessage localMsg = new ThriftUnicastNotificationMessage(thriftMsg);
        localMsg.setActorClassifierIsSet(false);
        for (EndpointClusterAddress address : routes.getLocalRoutes()) {
            LOG.info("Forwarding {} to local endpoint actor {}", localMsg, address);
            ThriftEndpointActorMsg<ThriftUnicastNotificationMessage> msg = new ThriftEndpointActorMsg<ThriftUnicastNotificationMessage>(
                    address.toEndpointAddress(), new ActorClassifier(false), localMsg);
            context.parent().tell(msg, context.self());
        }
        for (EndpointClusterAddress address : routes.getRemoteRoutes()) {
            LOG.info("Forwarding {} to remote endpoint actor {}", localMsg, address);
            clusterService.sendUnicastNotificationMessage(address.getNodeId(), localMsg);
        }
    }

}
