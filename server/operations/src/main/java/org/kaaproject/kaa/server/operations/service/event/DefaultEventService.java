/*
 * Copyright 2014 CyberVision, Inc.
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
import java.util.Random;
import java.util.UUID;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Event;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventMessageType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRoute;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventRouteUpdateType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RouteAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserRouteInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * EventService interface realization Class.
 * Accept UserRouteInfo, RouteInfo and RemoteEndpointEvent
 * Repacks this messages into thrift data structures and send through network.
 * After receiving such packs from network repacks it back to UserRouteInfo, RouteInfo and RemoteEndpointEvent
 * @author Andrey Panasenko
 *
 */
@Service
public class DefaultEventService implements EventService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultEventService.class);

    /** neigbors collector */
    private final Neighbors neighbors;

    /** The operations server config. */
    private OperationsServerConfig operationsServerConfig;

    /** Listeners list which registered to receive event from thrift server. */
    private final List<EventServiceListener> listeners;

    /** event sequence, used to mark every EventMessage */
    private static long eventSequence = 0;

    /** Used as random sequence shift, to achieve unique event message id across all operations servers */
    static {
        Random rnd = new Random();
        eventSequence = rnd.nextInt(1000000);
        eventSequence = eventSequence * 1000000000;
    }

    /** AVRO event converter */
    ThreadLocal<AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>> eventConverter = new ThreadLocal<AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>>(){
        @Override
        protected AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event> initialValue() {
            return new AvroByteArrayConverter<org.kaaproject.kaa.common.endpoint.gen.Event>(org.kaaproject.kaa.common.endpoint.gen.Event.class);
        }
    };


    /**
     * Key Class, used to unique repack sending messages.
     */
    public class Key {
        private String userId;
        private String tenantId;
        /**
         * @param userId2
         * @param tenantId2
         */
        public Key(String userId, String tenantId) {
            this.userId = userId;
            this.tenantId = tenantId;
        }
        /**
         * @return the userId
         */
        public String getUserId() {
            return userId;
        }
        /**
         * @return the tenantId
         */
        public String getTenantId() {
            return tenantId;
        }
        /**
         * @param userId the userId to set
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }
        /**
         * @param tenantId the tenantId to set
         */
        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (tenantId == null) {
                if (other.tenantId != null) {
                    return false;
                }
            } else if (!tenantId.equals(other.tenantId)) {
                return false;
            }
            if (userId == null) {
                if (other.userId != null) {
                    return false;
                }
            } else if (!userId.equals(other.userId)) {
                return false;
            }
            return true;
        }
        private DefaultEventService getOuterType() {
            return DefaultEventService.this;
        }

    }

    /**
     * Default constructor.
     */
    public DefaultEventService() {
        listeners = Collections.synchronizedList(new LinkedList<EventServiceListener>());
        neighbors = new Neighbors(this);
    }


    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#getConfig()
     */
    @Override
    public OperationsServerConfig getConfig() {
        return operationsServerConfig;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#setConfig(org.kaaproject.kaa.server.operations.service.http.OperationsServerConfig)
     */
    @Override
    public void setConfig(OperationsServerConfig config) {
        this.operationsServerConfig = config;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#sendEvent(org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent)
     */
    @Override
    public void sendEvent(RemoteEndpointEvent remoteEndpointEvent) {
        String serverId = remoteEndpointEvent.getRecipient().getServerId();
        NeighborConnection server = neighbors.getNeghborConnection(serverId);
        if (server == null) {
            LOG.info("sendRouteInfo() specified server {} not found in neighbors list", serverId);
            notifyListenersOnServerProblem(serverId);
            return;
        }
        RouteAddress routeAddress = new RouteAddress(
                ByteBuffer.wrap(remoteEndpointEvent.getRecipient().getEndpointKey().getData()),
                remoteEndpointEvent.getRecipient().getApplicationToken(),
                serverId);
        ByteBuffer eventData;
        try {
            eventData = ByteBuffer.wrap(eventConverter.get().toByteArray(remoteEndpointEvent.getEvent().getEvent()));
            org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointEvent endpointEvent
                = new org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointEvent(
                    remoteEndpointEvent.getEvent().getId().toString(),
                    ByteBuffer.wrap(remoteEndpointEvent.getEvent().getSender().getData()),
                    eventData ,
                    remoteEndpointEvent.getEvent().getCreateTime(),
                    remoteEndpointEvent.getEvent().getVersion());
            Event event = new Event(
                    remoteEndpointEvent.getUserId(),
                    remoteEndpointEvent.getTenantId(),
                    endpointEvent ,
                    routeAddress );
            server.sendEventMessage(packMessage(event));
        } catch (IOException e1) {
            LOG.error("Error on converting Event to byte array: skiping this event message",e1);
        } catch (InterruptedException e) {
            LOG.error("Error sending events to server: ",e);
            notifyListenersOnServerProblem(serverId);
        }
    }


    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#sendRouteInfo(org.kaaproject.kaa.server.operations.service.event.RouteInfo, java.lang.String[])
     */
    @Override
    public void sendRouteInfo(RouteInfo routeInfo, String... serverIdList) {
        Collection<RouteInfo> routeInfos = new ArrayList<>();
        routeInfos.add(routeInfo);
        sendRouteInfo(routeInfos, serverIdList);
    }


    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#sendRouteInfo(java.util.Collection, java.lang.String[])
     */
    @Override
    public void sendRouteInfo(Collection<RouteInfo> routeInfos, String... serverIdList) {
        List<EventRoute> routes = transformEventRouteFromRouteInfoCollection(routeInfos);
        String[] listServers = serverIdList;
        if (listServers == null || listServers.length <= 0) {
            List<NeighborConnection> servers = neighbors.getNeighbors();
            listServers = servers.toArray(new String[servers.size()]);
        }
        for(String serverId : listServers) {
            NeighborConnection server = neighbors.getNeghborConnection(serverId);
            if (server == null) {
                LOG.info("sendRouteInfo() specified server {} not found in neighbors list", serverId);
                notifyListenersOnServerProblem(serverId);
                continue;
            }

            try {
                server.sendEventMessage(packMessage(routes));
            } catch (InterruptedException e) {
                LOG.error("Error sending events to server: ",e);
                notifyListenersOnServerProblem(serverId);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#addListener(org.kaaproject.kaa.server.operations.service.event.EventServiceListener)
     */
    @Override
    public void addListener(EventServiceListener listener) {
        listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#removeListener(org.kaaproject.kaa.server.operations.service.event.EventServiceListener)
     */
    @Override
    public void removeListener(EventServiceListener listener) {
        while(listeners.remove(listener)); //NOSONAR

    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#sendUserRouteInfo(org.kaaproject.kaa.server.operations.service.event.UserRouteInfo)
     */
    @Override
    public void sendUserRouteInfo(org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo) {
        LOG.debug("EventService: sendUserRouteInfo().....");
        List<NeighborConnection> servers = neighbors.getNeighbors();

        for(NeighborConnection server : servers) {
            LOG.debug("EventService: sendUserRouteInfo() neighbor {}....", server.getId());
            UserRouteInfo userRoute = new UserRouteInfo(
                    routeInfo.getUserId(),
                    routeInfo.getTenantId(),
                    neighbors.getSelfId(),
                    transformUpdateType(routeInfo.getRouteOperation()));
            try {
                server.sendEventMessage(packMessage(userRoute));
                LOG.debug("EventService: sendUserRouteInfo() neighbor {}.... Done.", server.getId());
            } catch (InterruptedException e) {
                LOG.error("Error sending sendUserRouteInfo()",e);
                notifyListenersOnServerProblem(server.getId());
            }
        }
    }

    /**
     * Repack list of EventRoute messages to list of EventMessage
     * @param routes List<EventRoute>
     * @return List<EventMessage>
     */
    private List<EventMessage> packMessage(List<EventRoute> routes) {
        EventMessageType type = EventMessageType.ROUTE_UPDATE;
        List<EventMessage> messages = new LinkedList<>();
        for(EventRoute route : routes) {
            EventMessage message = new EventMessage(type,  getEventId(), null, route, null);
            messages.add(message);
        }
        return messages;
    }

    /**
     * Pack UserRouteInfo into list of EventMessage
     * @param userRoute UserRouteInfo
     * @return List<EventMessage>
     */
    private List<EventMessage> packMessage(UserRouteInfo userRoute) {
        EventMessageType type = EventMessageType.USER_ROUTE_INFO;
        List<EventMessage> messages = new LinkedList<>();

        EventMessage message = new EventMessage(type,  getEventId(), null, null, userRoute);
        messages.add(message);

        return messages;
    }

    /**
     * Pack Event into list of EventMessage
     * @param event Event
     * @return List<EventMessage>
     */
    private List<EventMessage> packMessage(Event event) {
        EventMessageType type = EventMessageType.EVENT;
        List<EventMessage> messages = new LinkedList<>();
        EventMessage message = new EventMessage(type,  getEventId(), event, null, null);
        messages.add(message);
        return messages;
    }

    /**
     * Notify EventService listeners if specified Operations Servers failed.
     * @param id Operations server id String
     */
    protected void notifyListenersOnServerProblem(String id) {
        for(EventServiceListener listener : listeners) {
            listener.onServerError(id);
        }
    }

    /**
     * Transform Collection<RouteInfo> into List<EventRoute>
     * @param routeInfos Collection<RouteInfo>
     * @return List<EventRoute>
     */
    private List<EventRoute> transformEventRouteFromRouteInfoCollection(Collection<RouteInfo> routeInfos) {
        List<EventRoute> routes = new ArrayList<>();
        HashMap<Key,List<org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo>> routeInfosTh = new HashMap<>(); //NOSONAR
        for(RouteInfo ri : routeInfos) {
            org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo riTh
                = new org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo(
                    transformUpdateType(ri.getRouteOperation()),
                    transformECFV(ri.getEcfVersions()),
                    ri.getAddress().getApplicationToken(),
                    ByteBuffer.wrap(ri.getAddress().getEndpointKey().getData()));
            Key key = new Key(ri.getUserId(), ri.getTenantId());

            if (!routeInfosTh.containsKey(key)) {
                routeInfosTh.put(key, new ArrayList<org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo>());
            }
            routeInfosTh.get(key).add(riTh);
        }
        for(Key key : routeInfosTh.keySet()) {

            EventRoute route = new EventRoute(
                    key.getUserId(),
                    key.getTenantId(),
                    routeInfosTh.get(key),
                    neighbors.getSelfId());
            routes.add(route);
        }
        return routes;
    }

    /**
     * Map RouteOperation into EventRouteUpdateType
     * @param operation RouteOperation
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
        }
        return EventRouteUpdateType.UPDATE;
    }

    /**
     * Map EventRouteUpdateType into RouteOperation
     * @param updateType EventRouteUpdateType
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
     * Transform List<EventClassFamilyVersion> into Thrift List<EventClassFamilyVersion>
     * @param ecfVersions List<EventClassFamilyVersion>
     * @return thrift List<EventClassFamilyVersion>
     */
    private static List<org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion> transformECFV(List<EventClassFamilyVersion> ecfVersions) {
        List<org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion> ecfvThL = new ArrayList<>();
        if(ecfVersions != null){
            for(EventClassFamilyVersion ecfv : ecfVersions) {
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
     * @return long
     */
    private synchronized long getEventId() {
        return eventSequence++;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#getNeighbors()
     */
    @Override
    public List<NeighborConnection> getNeighbors() {
        return neighbors.getNeighbors();
    }
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#shutdown()
     */
    @Override
    public void shutdown() {
        LOG.info("Event Service shutdown()....");
        listeners.clear();
        neighbors.shutdown();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#sendEventMessage(java.util.List)
     */
    @Override
    public void sendEventMessage(List<EventMessage> messages) {
        for(EventMessage message : messages) {
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
            default:
                break;
            }
        }

    }

    /**
     * Transform EventRoute message from thrift service into RouteInfo and push it to EventService listeners.
     * @param route EventRoute
     */
    private void onRouteUpdate(EventRoute route) {
        LOG.debug("onEventRouteUpdate .... {} routes updated in {} listeners",route.getRouteInfo().size(), listeners.size());
        for(EventServiceListener listener : listeners) {
            for(org.kaaproject.kaa.server.common.thrift.gen.operations.RouteInfo routeInfo : route.getRouteInfo()) {
                String applicationToken = routeInfo.getApplicationToken();
                EndpointObjectHash endpointKey = EndpointObjectHash.fromBytes(routeInfo.getEndpointId());

                RouteTableAddress address = new RouteTableAddress(endpointKey, applicationToken, route.getOperationsServerId());

                List<EventClassFamilyVersion> ecfVersions = new ArrayList<>();
                for(org.kaaproject.kaa.server.common.thrift.gen.operations.EventClassFamilyVersion ecfv : routeInfo.getEventClassFamilyVersion()) {
                    EventClassFamilyVersion ecf = new EventClassFamilyVersion(
                            ecfv.getEndpointClassFamilyId(),
                            ecfv.getEndpointClassFamilyVersion());
                    ecfVersions.add(ecf);
                }
                listener.onRouteInfo(new RouteInfo(route.getTenantId(), route.getUserId(), address, ecfVersions));
            }
        }
    }

    /**
     * Transform UserRouteInfo message from thrift service into UserRouteInfo and push it to EventService listeners.
     * @param UserRouteInfo
     */
    private void onUserRouteInfo(UserRouteInfo userRoute) {
        LOG.debug("eventUserRouteInfo .... User routes updates in {} listeners", listeners.size());
        LOG.debug("UserRouteInfo UserId={}; TenantId={}; OperationServerId={}",userRoute.getUserId(), userRoute.getTenantId(), userRoute.getOperationsServerId());
        for(EventServiceListener listener : listeners) {
            org.kaaproject.kaa.server.operations.service.event.UserRouteInfo routeInfo
                = new org.kaaproject.kaa.server.operations.service.event.UserRouteInfo(
                        userRoute.getTenantId(),
                        userRoute.getUserId(),
                        userRoute.getOperationsServerId(),
                        transformUpdateType(userRoute.getUpdateType()));
            listener.onUserRouteInfo(routeInfo);
        }
    }

    /**
     * Transform Event message from thrift service into RemoteEndpointEvent and push it to EventService listeners.
     * @param event Event
     */
    private void onEvent(Event event) {
        LOG.debug("onEvent .... event in {} listeners", listeners.size());
        LOG.debug("Event: {}",event.toString());
        for(EventServiceListener listener : listeners) {
            org.kaaproject.kaa.common.endpoint.gen.Event localEvent;
            try {
                localEvent = eventConverter.get().fromByteArray(event.getEndpointEvent().getEventData());
                EndpointEvent endpointEvent = new EndpointEvent(
                        EndpointObjectHash.fromBytes(event.getEndpointEvent().getSender()),
                        localEvent ,
                        UUID.fromString(event.getEndpointEvent().getUuid()),
                        event.getEndpointEvent().getCreateTime(),
                        event.getEndpointEvent().getVersion());
                RouteTableAddress recipient = new RouteTableAddress(
                        EndpointObjectHash.fromBytes(event.getRouteAddress().getEndpointKey()),
                        event.getRouteAddress().getApplicationToken(),
                        event.getRouteAddress().getOperationsServerId());
                RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(
                        event.getTenantId(),
                        event.getUserId(),
                        endpointEvent, recipient );
                listener.onEvent(remoteEvent );
            } catch (IOException e) {
                LOG.error("Error on converting byte array to Event: skiping this event message",e);
            }

        }
    }
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.event.EventService#setZkNode(org.kaaproject.kaa.server.common.zk.operations.OperationsNode)
     */
    @Override
    public void setZkNode(OperationsNode operationsNode) {
        neighbors.setZkNode(operationsNode);
    }

}
