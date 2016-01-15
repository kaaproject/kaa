package org.kaaproject.kaa.server.operations.service.route;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.thrift.TException;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftClusterEntityType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityClusterAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftRouteOperation;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EntityClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;
import org.kaaproject.kaa.server.thrift.NeighborConnection;
import org.kaaproject.kaa.server.thrift.NeighborTemplate;
import org.kaaproject.kaa.server.thrift.Neighbors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultClusterService implements ClusterService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultClusterService.class);

    @Autowired
    private OperationsServerConfig operationsServerConfig;

    /** ID is thriftHost:thriftPort */
    private volatile String id;

    private volatile Neighbors<MessageTemplate, ThriftEntityRouteMessage> neighbors;

    private volatile OperationsNode operationsNode;

    private volatile OperationsServerResolver resolver;

    private ClusterServiceListener listener;

    public DefaultClusterService() {
        super();
    }

    @PostConstruct
    public void initBean() {
        LOG.info("Init default cluster service.");
        neighbors = new Neighbors<MessageTemplate, ThriftEntityRouteMessage>(new MessageTemplate(),
                operationsServerConfig.getMaxNumberNeighborConnections());
    }
    
    @Override
    public String getNodeId() {
        return id;
    }

    @Override
    public void setZkNode(OperationsNode operationsNode) {
        this.operationsNode = operationsNode;
        this.id = Neighbors.getServerID(this.operationsNode.getNodeInfo().getConnectionInfo());
        neighbors.setZkNode(id, operationsNode);
        if (resolver != null) {
            updateResolver(this.resolver);
        }
    }

    @Override
    public void setResolver(OperationsServerResolver resolver) {
        this.resolver = resolver;
        if (operationsNode != null) {
            updateResolver(this.resolver);
        }
    }

    private void updateResolver(final OperationsServerResolver resolver) {
        operationsNode.addListener(new OperationsNodeListener() {
            @Override
            public void onNodeUpdated(OperationsNodeInfo node) {
                LOG.debug("Update of node {} is pushed to resolver {}", node, resolver);
                resolver.onNodeUpdated(node);
            }

            @Override
            public void onNodeRemoved(OperationsNodeInfo node) {
                LOG.debug("Remove of node {} is pushed to resolver {}", node, resolver);
                resolver.onNodeRemoved(node);
                notifyListener();
            }

            @Override
            public void onNodeAdded(OperationsNodeInfo node) {
                LOG.debug("Add of node {} is pushed to resolver {}", node, resolver);
                resolver.onNodeAdded(node);
                notifyListener();
            }

            private void notifyListener() {
                if (listener != null) {
                    listener.onClusterUpdated();
                }
            }
        });

        for (OperationsNodeInfo info : operationsNode.getCurrentOperationServerNodes()) {
            resolver.onNodeUpdated(info);
        }
    }

    @Override
    public void setListener(ClusterServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isMainEntityNode(String entityId) {
        OperationsNodeInfo info = resolver.getNode(entityId);
        if (info == null) {
            return false;
        }
        LOG.trace("comparing {} to {} for entity {}", id, info.getConnectionInfo(), entityId);
        return id.equals(Neighbors.getServerID(info.getConnectionInfo()));

    }

    @Override
    public String getEntityNode(String entityId) {
        OperationsNodeInfo info = resolver.getNode(entityId);
        if (info != null) {
            return Neighbors.getServerID(info.getConnectionInfo());
        }
        return null;
    }

    @Override
    public String sendRouteMessage(EndpointRouteMessage msg) {
        String entityId = msg.getAddress().getEndpointKey().toString();
        String serverId = getEntityNode(entityId);
        NeighborConnection<MessageTemplate, ThriftEntityRouteMessage> server = neighbors.getNeghborConnection(serverId);
        if (server == null) {
            LOG.warn("specified server {} not found in neighbors list", serverId);
        } else {
            sendMessagesToServer(server, Collections.singleton(toThriftMsg(msg)));
        }
        return serverId;
    }

    @Override
    public void onEntityRouteMessages(List<ThriftEntityRouteMessage> msgs) {
        for (ThriftEntityRouteMessage msg : msgs) {
            switch (msg.getAddress().getEntityType()) {
            case ENDPOINT:
                listener.onRouteMsg(fromThriftMsg(msg));
                break;
            }
        }
    }

    private EndpointRouteMessage fromThriftMsg(ThriftEntityRouteMessage source) {
        return new EndpointRouteMessage(fromThriftAddress(source.getAddress()), fromThriftOperation(source.getOperation()));
    }

    private RouteOperation fromThriftOperation(ThriftRouteOperation operation) {
        switch (operation) {
        case ADD:
            return RouteOperation.ADD;
        case UPDATE:
            return RouteOperation.UPDATE;
        case DELETE:
            return RouteOperation.DELETE;
        default:
            return RouteOperation.DELETE;
        }
    }

    private EndpointClusterAddress fromThriftAddress(ThriftEntityClusterAddress source) {
        EndpointObjectHash endpointKey = EndpointObjectHash.fromBytes(source.getEntityId());
        return new EndpointClusterAddress(source.getNodeId(), source.getTenantId(), source.getApplicationToken(), endpointKey);
    }

    private void sendMessagesToServer(NeighborConnection<MessageTemplate, ThriftEntityRouteMessage> server,
            Collection<ThriftEntityRouteMessage> messages) {
        try {
            LOG.trace("Sending to server {} messages: {}", server.getId(), messages);
            server.sendMessages(messages);
        } catch (InterruptedException e) {
            LOG.error("Error sending events to server: ", e);
        }
    }

    private ThriftEntityRouteMessage toThriftMsg(EndpointRouteMessage source) {
        ThriftEntityRouteMessage msg = new ThriftEntityRouteMessage();
        msg.setAddress(toAddress(source.getAddress()));
        msg.setOperation(toOperation(source.getOperation()));
        return msg;
    }

    private ThriftRouteOperation toOperation(RouteOperation operation) {
        switch (operation) {
        case ADD:
            return ThriftRouteOperation.ADD;
        case UPDATE:
            return ThriftRouteOperation.UPDATE;
        case DELETE:
            return ThriftRouteOperation.DELETE;
        default:
            return ThriftRouteOperation.DELETE;
        }
    }

    private ThriftEntityClusterAddress toAddress(EndpointClusterAddress source) {
        ThriftEntityClusterAddress address = toEntityAddress(source);
        address.setEntityType(ThriftClusterEntityType.ENDPOINT);
        return address;
    }

    private ThriftEntityClusterAddress toEntityAddress(EntityClusterAddress source) {
        ThriftEntityClusterAddress address = new ThriftEntityClusterAddress();
        address.setNodeId(source.getNodeId());
        address.setTenantId(source.getTenantId());
        address.setApplicationToken(source.getAppToken());
        address.setEntityId(source.getEntityId());
        return address;
    }

    @Override
    public void shutdown() {
        LOG.info("Cluster Service shutdown()....");
        neighbors.shutdown();
    }

    private static class MessageTemplate implements NeighborTemplate<ThriftEntityRouteMessage> {

        public MessageTemplate() {
            super();
        }

        @Override
        public void process(Iface client, List<ThriftEntityRouteMessage> messages) throws TException {
            client.onEntityRouteMessages(messages);
        }

        @Override
        public void onServerError(String serverId, Exception e) {
            LOG.warn("Failed to send data to [{}]", serverId);
        }
    }
}
