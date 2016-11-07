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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.thrift.TException;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftActorClassifier;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftClusterEntityType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityClusterAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftRouteOperation;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.node.service.thrift.OperationsServiceMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ActorClassifier;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EntityClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
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

    private volatile Neighbors<MessageTemplate, OperationsServiceMsg> neighbors;

    private volatile OperationsNode operationsNode;

    private volatile OperationsServerResolver resolver;

    private ClusterServiceListener listener;

    public DefaultClusterService() {
        super();
    }

    @PostConstruct
    public void initBean() {
        LOG.info("Init default cluster service.");
        neighbors = new Neighbors<MessageTemplate, OperationsServiceMsg>(KaaThriftService.OPERATIONS_SERVICE, new MessageTemplate(),
                operationsServerConfig.getMaxNumberNeighborConnections());
    }
    
    @PreDestroy
    public void onStop() {
        if (neighbors != null) {
            LOG.info("Shutdown of control service neighbors started!");
            neighbors.shutdown();
            LOG.info("Shutdown of control service neighbors complete!");
        }
    }

    @Override
    public String getNodeId() {
        return id;
    }

    @Override
    public void setZkNode(OperationsNode operationsNode) {
        this.operationsNode = operationsNode;
        this.id = Neighbors.getServerID(this.operationsNode.getNodeInfo().getConnectionInfo());
        neighbors.setZkNode(KaaThriftService.OPERATIONS_SERVICE, this.operationsNode.getNodeInfo().getConnectionInfo(), operationsNode);
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
    public boolean isMainEntityNode(EndpointObjectHash entityId) {
        return isMainEntityNode(entityId.getData());
    }

    @Override
    public boolean isMainEntityNode(byte[] entityId) {
        String entityIdStr = Base64Util.encode(entityId);
        OperationsNodeInfo info = resolver.getNode(entityIdStr);
        if (info == null) {
            return false;
        }
        String nodeId = Neighbors.getServerID(info.getConnectionInfo());
        LOG.trace("Comparing {} to {} for entity {}", id, nodeId, entityIdStr);
        return id.equals(nodeId);

    }

    @Override
    public String getEntityNode(EndpointObjectHash entityId) {
        return getEntityNode(entityId.getData());
    }

    @Override
    public String getEntityNode(byte[] entityId) {
        OperationsNodeInfo info = resolver.getNode(Base64Util.encode(entityId));
        if (info != null) {
            return Neighbors.getServerID(info.getConnectionInfo());
        }
        return null;
    }

    @Override
    public String sendRouteMessage(EndpointRouteMessage msg) {
        String serverId = getEntityNode(msg.getAddress().getEndpointKey());
        sendServerProfileUpdateMessage(serverId, OperationsServiceMsg.fromRoute(toThriftMsg(msg)));
        return serverId;
    }

    @Override
    public void sendUnicastNotificationMessage(String serverId, ThriftUnicastNotificationMessage msg) {
        sendServerProfileUpdateMessage(serverId, OperationsServiceMsg.fromNotification(msg));
    }

    @Override
    public void sendServerProfileUpdateMessage(String serverId, ThriftServerProfileUpdateMessage msg) {
        sendServerProfileUpdateMessage(serverId, OperationsServiceMsg.fromServerProfileUpdateMessage(msg));
    }

    private void sendServerProfileUpdateMessage(String serverId, OperationsServiceMsg msg) {
        NeighborConnection<MessageTemplate, OperationsServiceMsg> server = neighbors.getNeghborConnection(serverId);
        if (server == null) {
            LOG.warn("Specified server {} not found in neighbors list", serverId);
        } else {
            sendMessagesToServer(server, Collections.singleton(msg));
        }
    }

    @Override
    public void onEntityRouteMessages(List<ThriftEntityRouteMessage> msgs) {
        for (ThriftEntityRouteMessage msg : msgs) {
            switch (msg.getAddress().getAddress().getEntityType()) {
            case ENDPOINT:
                listener.onRouteMsg(fromThriftMsg(msg));
                break;
            default:
                LOG.error("Unknown entity type: {}", msg.getAddress().getAddress().getEntityType());
            }
        }
    }

    @Override
    public void onUnicastNotificationMessage(ThriftUnicastNotificationMessage msg) {
        EndpointAddress address = fromThriftAddress(msg.getAddress());
        ActorClassifier classifier = fromThriftActorClassifier(msg.getActorClassifier());
        listener.onEndpointActorMsg(new ThriftEndpointActorMsg<ThriftUnicastNotificationMessage>(address, classifier, msg));
    }

    @Override
    public void onServerProfileUpdateMessage(ThriftServerProfileUpdateMessage msg) {
        EndpointAddress address = fromThriftAddress(msg.getAddress());
        ActorClassifier classifier = fromThriftActorClassifier(msg.getActorClassifier());
        listener.onEndpointActorMsg(new ThriftEndpointActorMsg<ThriftServerProfileUpdateMessage>(address, classifier, msg));
    }
    
    @Override
    public void onEndpointDeregistrationMessage(ThriftEndpointDeregistrationMessage msg) {
        EndpointAddress address = fromThriftAddress(msg.getAddress());
        ActorClassifier classifier = fromThriftActorClassifier(msg.getActorClassifier());
        listener.onEndpointActorMsg(new ThriftEndpointActorMsg<ThriftEndpointDeregistrationMessage>(address, classifier, msg));
    }

    private EndpointAddress fromThriftAddress(ThriftEntityAddress source) {
        return new EndpointAddress(source.getTenantId(), source.getApplicationToken(), EndpointObjectHash.fromBytes(source.getEntityId()));
    }

    private ActorClassifier fromThriftActorClassifier(ThriftActorClassifier actorClassifier) {
        return ActorClassifier.valueOf(actorClassifier.name());
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
        ThriftEntityAddress address = source.getAddress();
        EndpointObjectHash endpointKey = EndpointObjectHash.fromBytes(address.getEntityId());
        return new EndpointClusterAddress(source.getNodeId(), address.getTenantId(), address.getApplicationToken(), endpointKey);
    }

    private void sendMessagesToServer(NeighborConnection<MessageTemplate, OperationsServiceMsg> server,
            Collection<OperationsServiceMsg> messages) {
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
        ThriftEntityClusterAddress address = toEntityClusterAddress(source);
        address.getAddress().setEntityType(ThriftClusterEntityType.ENDPOINT);
        return address;
    }

    private ThriftEntityClusterAddress toEntityClusterAddress(EntityClusterAddress source) {
        ThriftEntityClusterAddress address = new ThriftEntityClusterAddress();
        address.setNodeId(source.getNodeId());
        address.setAddress(toEntityAddress(source));
        return address;
    }

    private ThriftEntityAddress toEntityAddress(EntityClusterAddress source) {
        ThriftEntityAddress address = new ThriftEntityAddress();
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

    private static class MessageTemplate implements NeighborTemplate<OperationsServiceMsg> {

        public MessageTemplate() {
            super();
        }

        @Override
        public void process(Iface client, List<OperationsServiceMsg> messages) throws TException {
            OperationsServiceMsg.dispatch(client, messages);
        }

        @Override
        public void onServerError(String serverId, Exception e) {
            LOG.warn("Failed to send data to [{}]", serverId);
        }
    }

}
