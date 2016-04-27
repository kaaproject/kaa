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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.thrift.TException;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointRouteUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointStateUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Event;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventMessageType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRoute;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRouteUpdateType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Message;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RouteAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserRouteInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;
import org.kaaproject.kaa.server.sync.platform.AvroEncDec;
import org.kaaproject.kaa.server.thrift.NeighborConnection;
import org.kaaproject.kaa.server.thrift.NeighborTemplate;
import org.kaaproject.kaa.server.thrift.Neighbors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * EventService interface realization Class. Accept UserRouteInfo, RouteInfo and
 * RemoteEndpointEvent Repacks this messages into thrift data structures and
 * send through network. After receiving such packs from network repacks it back
 * to UserRouteInfo, RouteInfo and RemoteEndpointEvent
 * 
 * @author Andrey Panasenko
 * @author Andrew Shvayka
 *
 */
@Service
public class DefaultEventService implements EventService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventService.class);

    private static final AtomicLong eventSequence = new AtomicLong(UUID.randomUUID().getLeastSignificantBits()); //NOSONAR

    @Autowired
    private OperationsServerConfig operationsServerConfig;

    /** ID is thriftHost:thriftPort */
    private volatile String id;

    private volatile Neighbors<MessageTemplate, Message> neighbors;

    private volatile OperationsNode operationsNode;

    private volatile OperationsServerResolver resolver;

    /** Listeners list which registered to receive event from thrift server. */
    private Set<EventServiceListener> listeners;

    /** AVRO event converter */
    private final ThreadLocal<AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>> eventConverter = new ThreadLocal<AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>>() {
        @Override
        protected AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event> initialValue() {
            return new AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>(
                    org.kaaproject.kaa.common.endpoint.gen.Event.class);
        }
    };

    /**
     * Default constructor.
     */
    public DefaultEventService() {
        super();
    }

    @PostConstruct
    public void initBean() {
        LOG.info("Init default event service.");
        listeners = Collections.newSetFromMap(new ConcurrentHashMap<EventServiceListener, Boolean>());
        neighbors = new Neighbors<MessageTemplate, Message>(KaaThriftService.OPERATIONS_SERVICE, new MessageTemplate(this),
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#sendEvent
     * (org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent)
     */
    @Override
    public void sendEvent(RemoteEndpointEvent remoteEndpointEvent) {
        String serverId = remoteEndpointEvent.getRecipient().getServerId();
        NeighborConnection<MessageTemplate, Message> server = neighbors.getNeghborConnection(serverId);
        if (server == null) {
            LOG.debug("sendRouteInfo() specified server {} not found in neighbors list", serverId);
            notifyListenersOnServerProblem(serverId);
            return;
        }
        RouteAddress routeAddress = new RouteAddress(ByteBuffer.wrap(remoteEndpointEvent.getRecipient().getEndpointKey().getData()),
                remoteEndpointEvent.getRecipient().getApplicationToken(), serverId);
        ByteBuffer eventData;
        try {
            org.kaaproject.kaa.server.sync.Event eventSource = remoteEndpointEvent.getEvent().getEvent();
            eventData = ByteBuffer.wrap(eventConverter.get().toByteArray(AvroEncDec.convert(eventSource)));
            org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointEvent endpointEvent = new org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointEvent(
                    remoteEndpointEvent.getEvent().getId().toString(),
                    ByteBuffer.wrap(remoteEndpointEvent.getEvent().getSender().getData()), eventData, remoteEndpointEvent.getEvent()
                            .getCreateTime(), remoteEndpointEvent.getEvent().getVersion());
            Event event = new Event(remoteEndpointEvent.getUserId(), remoteEndpointEvent.getTenantId(), endpointEvent, routeAddress);
            server.sendMessages(packMessage(event));
        } catch (IOException e1) {
            LOG.error("Error on converting Event to byte array: skiping this event message", e1);
        } catch (InterruptedException e) {
            LOG.error("Error sending events to server: ", e);
            notifyListenersOnServerProblem(serverId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#sendRouteInfo
     * (org.kaaproject.kaa.server.operations.service.event.RouteInfo,
     * java.lang.String[])
     */
    @Override
    public void sendRouteInfo(RouteInfo routeInfo, String... serverIdList) {
        Collection<RouteInfo> routeInfos = new ArrayList<>();
        routeInfos.add(routeInfo);
        sendRouteInfo(routeInfos, serverIdList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#sendRouteInfo
     * (java.util.Collection, java.lang.String[])
     */
    @Override
    public void sendRouteInfo(Collection<RouteInfo> routeInfos, String... serverIdList) {
        List<EventRoute> routes = transformEventRouteFromRouteInfoCollection(routeInfos);
        String[] listServers = serverIdList;
        if (listServers == null || listServers.length <= 0) {
            List<NeighborConnection<MessageTemplate, Message>> servers = neighbors.getNeighbors();
            listServers = servers.toArray(new String[servers.size()]);
        }
        for (String serverId : listServers) {
            sendMessagesToServer(packMessage(routes), serverId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#
     * sendUserRouteInfo
     * (org.kaaproject.kaa.server.operations.service.event.UserRouteInfo)
     */
    @Override
    public void sendUserRouteInfo(org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo) {
        LOG.debug("EventService: sendUserRouteInfo().....");
        List<NeighborConnection<MessageTemplate, Message>> servers = neighbors.getNeighbors();

        UserRouteInfo userRoute = new UserRouteInfo(routeInfo.getUserId(), routeInfo.getTenantId(), id,
                transformUpdateType(routeInfo.getRouteOperation()));
        List<Message> messages = packMessage(userRoute);
        for (NeighborConnection<MessageTemplate, Message> server : servers) {
            LOG.debug("Send UserRouteInfo {} to neighbor {}....", userRoute, server.getId());
            sendMessagesToServer(server, messages);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#shutdown
     * ()
     */
    @Override
    public void shutdown() {
        LOG.info("Event Service shutdown()....");
        listeners.clear();
        neighbors.shutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#addListener
     * (org.kaaproject.kaa.server.operations.service.event.EventServiceListener)
     */
    @Override
    public void addListener(EventServiceListener listener) {
        listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#
     * removeListener
     * (org.kaaproject.kaa.server.operations.service.event.EventServiceListener)
     */
    @Override
    public void removeListener(EventServiceListener listener) {
        listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#
     * sendEventMessage(java.util.List)
     */
    @Override
    public void sendEventMessage(List<Message> messages) {
        for (Message message : messages) {
            switch (message.getType()) {
            case ROUTE_UPDATE:
                onRouteUpdate(message.getRoute());
                break;
            case USER_ROUTE_INFO:
                onUserRouteInfo(message.getUserRoute());
                break;
            case EVENT:
                onEvent(message.getEvent());
                break;
            case ENDPOINT_ROUTE_UPDATE:
                onEndpointRouteUpdate(message.getEndpointRouteUpdate());
                break;
            case ENDPOINT_STATE_UPDATE:
                onEndpointStateUpdate(message.getEndpointStateUpdate());
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void sendEndpointRouteInfo(GlobalRouteInfo routeInfo) {
        LOG.trace("calculating server for user {}", routeInfo.getUserId());
        String serverId = Neighbors.getServerID(resolver.getNode(routeInfo.getUserId()).getConnectionInfo());
        sendMessagesToServer(packMessage(routeInfo), serverId);
    }

    @Override
    public void sendEndpointStateInfo(String serverId, EndpointUserConfigurationUpdate update) {
        sendMessagesToServer(packMessage(update), serverId);
    }

    @Override
    public boolean isMainUserNode(String userId) {
        OperationsNodeInfo info = resolver.getNode(userId);
        if (info == null) {
            return false;
        }
        LOG.trace("comparing {} to {} for user {}", id, info.getConnectionInfo(), userId);
        return id.equals(Neighbors.getServerID(info.getConnectionInfo()));
    }

    @Override
    public String getUserNode(String userId) {
        OperationsNodeInfo info = resolver.getNode(userId);
        if (info != null) {
            return Neighbors.getServerID(info.getConnectionInfo());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.event.EventService#setZkNode
     * (org.kaaproject.kaa.server.common.zk.operations.OperationsNode)
     */
    @Override
    public void setZkNode(OperationsNode operationsNode) {
        this.operationsNode = operationsNode;
        this.id = Neighbors.getServerID(KaaThriftService.OPERATIONS_SERVICE, this.operationsNode.getNodeInfo().getConnectionInfo());
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

    /**
     * Repack list of EventRoute messages to list of EventMessage
     * 
     * @param routes
     *            List<EventRoute>
     * @return List<EventMessage>
     */
    private List<Message> packMessage(List<EventRoute> routes) {
        EventMessageType type = EventMessageType.ROUTE_UPDATE;
        List<Message> messages = new LinkedList<>();
        for (EventRoute route : routes) {
            messages.add(new Message(type, getEventId(), null, route, null, null, null));
        }
        return messages;
    }

    /**
     * Pack UserRouteInfo into list of EventMessage
     * 
     * @param userRoute
     *            UserRouteInfo
     * @return List<EventMessage>
     */
    private List<Message> packMessage(UserRouteInfo userRoute) {
        return Collections.singletonList(new Message(EventMessageType.USER_ROUTE_INFO, getEventId(), null, null, userRoute, null, null));
    }

    /**
     * Pack Event into list of EventMessage
     * 
     * @param event
     *            Event
     * @return List<EventMessage>
     */
    private List<Message> packMessage(Event event) {
        return Collections.singletonList(new Message(EventMessageType.EVENT, getEventId(), event, null, null, null, null));
    }

    /**
     * Notify EventService listeners if specified Operations Servers failed.
     * 
     * @param id
     *            Operations server id String
     */
    protected void notifyListenersOnServerProblem(String id) {
        for (EventServiceListener listener : listeners) {
            listener.onServerError(id);
        }
    }

    /**
     * Transform Collection<RouteInfo> into List<EventRoute>
     * 
     * @param routeInfos
     *            Collection<RouteInfo>
     * @return List<EventRoute>
     */
    private List<EventRoute> transformEventRouteFromRouteInfoCollection(Collection<RouteInfo> routeInfos) {
        List<EventRoute> routes = new ArrayList<>();
        HashMap<UserTenantKey, List<org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo>> routeInfosTh = new HashMap<>(); // NOSONAR
        for (RouteInfo ri : routeInfos) {
            org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo riTh = new org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo(
                    transformUpdateType(ri.getRouteOperation()), transformECFV(ri.getEcfVersions()), ri.getAddress().getApplicationToken(),
                    ByteBuffer.wrap(ri.getAddress().getEndpointKey().getData()));
            UserTenantKey key = new UserTenantKey(ri.getUserId(), ri.getTenantId());

            if (!routeInfosTh.containsKey(key)) {
                routeInfosTh.put(key, new ArrayList<org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo>());
            }
            routeInfosTh.get(key).add(riTh);
        }
        for (UserTenantKey key : routeInfosTh.keySet()) {
            routes.add(new EventRoute(key.getUserId(), key.getTenantId(), routeInfosTh.get(key), id));
        }
        return routes;
    }

    /**
     * Map RouteOperation into EventRouteUpdateType
     * 
     * @param operation
     *            RouteOperation
     * @return EventRouteUpdateType
     */
    private static EventRouteUpdateType transformUpdateType(RouteOperation operation) {
        switch (operation) {
        case ADD:
            return EventRouteUpdateType.ADD;
        case DELETE:
            return EventRouteUpdateType.DELETE;
        case UPDATE:
            return EventRouteUpdateType.UPDATE;
        default:
            break;
        }
        return EventRouteUpdateType.UPDATE;
    }

    /**
     * Map EventRouteUpdateType into RouteOperation
     * 
     * @param updateType
     *            EventRouteUpdateType
     * @return RouteOperation
     */
    private static RouteOperation transformUpdateType(EventRouteUpdateType updateType) {
        switch (updateType) {
        case ADD:
            return RouteOperation.ADD;
        case DELETE:
            return RouteOperation.DELETE;
        default:
            return RouteOperation.UPDATE;
        }
    }

    /**
     * Transform List<EventClassFamilyVersion> into Thrift
     * List<EventClassFamilyVersion>
     * 
     * @param ecfVersions
     *            List<EventClassFamilyVersion>
     * @return thrift List<EventClassFamilyVersion>
     */
    private static List<org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion> transformECFV(
            List<EventClassFamilyVersion> ecfVersions) {
        List<org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion> ecfvThL = new ArrayList<>();
        if (ecfVersions != null) {
            for (EventClassFamilyVersion ecfv : ecfVersions) {
                org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion ecfvTh = new org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion();
                ecfvTh.setEndpointClassFamilyId(ecfv.getEcfId());
                ecfvTh.setEndpointClassFamilyVersion(ecfv.getVersion());
                ecfvThL.add(ecfvTh);
            }
        }
        return ecfvThL;
    }

    /**
     * Next event sequence getter.
     * 
     * @return long
     */
    private long getEventId() {
        return eventSequence.getAndIncrement();
    }

    private void sendMessagesToServer(List<Message> messages, String serverId) {
        NeighborConnection<MessageTemplate, Message> server = neighbors.getNeghborConnection(serverId);
        if (server == null) {
            LOG.debug("specified server {} not found in neighbors list", serverId);
            notifyListenersOnServerProblem(serverId);
            return;
        }

        sendMessagesToServer(server, messages);
    }

    private void sendMessagesToServer(NeighborConnection<MessageTemplate, Message> server, List<Message> messages) {
        try {
            LOG.trace("Sending to server {} messages: {}", server.getId(), messages);
            server.sendMessages(messages);
        } catch (InterruptedException e) {
            LOG.error("Error sending events to server: ", e);
            notifyListenersOnServerProblem(server.getId());
        }
    }

    private void onEndpointRouteUpdate(EndpointRouteUpdate update) {
        LOG.debug("Updating {} listeners with {}", listeners.size(), update);
        GlobalRouteInfo msg = GlobalRouteInfo.fromThrift(update);
        for (EventServiceListener listener : listeners) {
            listener.onEndpointRouteUpdate(msg);
        }
    }

    private void onEndpointStateUpdate(EndpointStateUpdate update) {
        LOG.debug("Updating {} listeners with {}", listeners.size(), update);
        EndpointUserConfigurationUpdate msg = EndpointUserConfigurationUpdate.fromThrift(update);
        for (EventServiceListener listener : listeners) {
            listener.onEndpointStateUpdate(msg);
        }
    }

    /**
     * Transform EventRoute message from thrift service into RouteInfo and push
     * it to EventService listeners.
     * 
     * @param route
     *            EventRoute
     */
    private void onRouteUpdate(EventRoute route) {
        LOG.debug("onEventRouteUpdate .... {} routes updated in {} listeners", route.getRouteInfo().size(), listeners.size());
        for (EventServiceListener listener : listeners) {
            for (org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo routeInfo : route.getRouteInfo()) {
                String applicationToken = routeInfo.getApplicationToken();
                EndpointObjectHash endpointKey = EndpointObjectHash.fromBytes(routeInfo.getEndpointId());

                RouteTableAddress address = new RouteTableAddress(endpointKey, applicationToken, route.getOperationsServerId());

                List<EventClassFamilyVersion> ecfVersions = new ArrayList<>();
                for (org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion ecfv : routeInfo
                        .getEventClassFamilyVersion()) {
                    EventClassFamilyVersion ecf = new EventClassFamilyVersion(ecfv.getEndpointClassFamilyId(),
                            ecfv.getEndpointClassFamilyVersion());
                    ecfVersions.add(ecf);
                }
                listener.onRouteInfo(new RouteInfo(route.getTenantId(), route.getUserId(), address, ecfVersions));
            }
        }
    }

    /**
     * Transform UserRouteInfo message from thrift service into UserRouteInfo
     * and push it to EventService listeners.
     * 
     * @param userRoute
     *            UserRouteInfo object
     */
    private void onUserRouteInfo(UserRouteInfo userRoute) {
        LOG.debug("eventUserRouteInfo .... User routes updates in {} listeners", listeners.size());
        LOG.debug("UserRouteInfo UserId={}; TenantId={}; OperationServerId={}", userRoute.getUserId(), userRoute.getTenantId(),
                userRoute.getOperationsServerId());
        for (EventServiceListener listener : listeners) {
            org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo = new org.kaaproject.kaa.server.operations.service.event.UserRouteInfo(
                    userRoute.getTenantId(), userRoute.getUserId(), userRoute.getOperationsServerId(),
                    transformUpdateType(userRoute.getUpdateType()));
            listener.onUserRouteInfo(routeInfo);
        }
    }

    /**
     * Transform Event message from thrift service into RemoteEndpointEvent and
     * push it to EventService listeners.
     * 
     * @param event
     *            Event
     */
    private void onEvent(Event event) {
        LOG.debug("onEvent .... event in {} listeners", listeners.size());
        LOG.debug("Event: {}", event.toString());
        for (EventServiceListener listener : listeners) {
            org.kaaproject.kaa.server.sync.Event localEvent;
            try {
                localEvent = AvroEncDec.convert(eventConverter.get().fromByteArray(event.getEndpointEvent().getEventData()));
                EndpointEvent endpointEvent = new EndpointEvent(EndpointObjectHash.fromBytes(event.getEndpointEvent().getSender()),
                        localEvent, UUID.fromString(event.getEndpointEvent().getUuid()), event.getEndpointEvent().getCreateTime(), event
                                .getEndpointEvent().getVersion());
                RouteTableAddress recipient = new RouteTableAddress(EndpointObjectHash.fromBytes(event.getRouteAddress().getEndpointKey()),
                        event.getRouteAddress().getApplicationToken(), event.getRouteAddress().getOperationsServerId());
                RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(event.getTenantId(), event.getUserId(), endpointEvent, recipient);
                listener.onEvent(remoteEvent);
            } catch (IOException e) {
                LOG.error("Error on converting byte array to Event: skiping this event message", e);
            }

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
            }

            @Override
            public void onNodeAdded(OperationsNodeInfo node) {
                LOG.debug("Add of node {} is pushed to resolver {}", node, resolver);
                resolver.onNodeAdded(node);
            }
        });

        for (OperationsNodeInfo info : operationsNode.getCurrentOperationServerNodes()) {
            resolver.onNodeUpdated(info);
        }
    }

    private List<Message> packMessage(GlobalRouteInfo routeInfo) {
        EventMessageType type = EventMessageType.ENDPOINT_ROUTE_UPDATE;
        List<Message> messages = new LinkedList<>();

        EndpointRouteUpdate route = new EndpointRouteUpdate();
        route.setTenantId(routeInfo.getTenantId());
        route.setUserId(routeInfo.getUserId());
        route.setUpdateType(transformUpdateType(routeInfo.getRouteOperation()));
        route.setCfSchemaVersion(routeInfo.getCfVersion());
        route.setUcfHash(routeInfo.getUcfHash());

        String opsServerId = routeInfo.getAddress().getServerId();
        if (opsServerId == null) {
            opsServerId = id;
        }

        RouteAddress routeAddress = new RouteAddress(ByteBuffer.wrap(routeInfo.getAddress().getEndpointKey().getData()), routeInfo
                .getAddress().getApplicationToken(), opsServerId);
        route.setRouteAddress(routeAddress);

        messages.add(new Message(type, getEventId(), null, null, null, route, null));

        return messages;
    }

    private List<Message> packMessage(EndpointUserConfigurationUpdate update) {
        EventMessageType type = EventMessageType.ENDPOINT_STATE_UPDATE;
        List<Message> messages = new LinkedList<>();

        EndpointStateUpdate msg = new EndpointStateUpdate();
        msg.setTenantId(update.getTenantId());
        msg.setUserId(update.getUserId());
        msg.setApplicationToken(update.getApplicationToken());
        msg.setEndpointKey(update.getKey().getData());
        msg.setUcfHash(update.getHash());

        messages.add(new Message(type, getEventId(), null, null, null, null, msg));

        return messages;
    }

    private static class MessageTemplate implements NeighborTemplate<Message> {

        private final DefaultEventService service;

        public MessageTemplate(DefaultEventService service) {
            super();
            this.service = service;
        }

        @Override
        public void process(Iface client, List<Message> messages) throws TException {
            client.sendMessages(messages);
        }

        @Override
        public void onServerError(String serverId, Exception e) {
            service.notifyListenersOnServerProblem(serverId);
        }
    }
}
