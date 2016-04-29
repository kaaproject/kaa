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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.EndpointEventTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage.EventDeliveryStatus;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointRouteUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RemoteEndpointEventMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.event.EndpointECFVersionMap;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.EventDeliveryTable;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.event.EventStorage;
import org.kaaproject.kaa.server.operations.service.event.GlobalRouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RouteTable;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.operations.service.event.UserRouteInfo;
import org.kaaproject.kaa.server.sync.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Terminated;

public class LocalUserActorMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LocalUserActorMessageProcessor.class);

    private final CacheService cacheService;

    private final EventService eventService;

    private final String userId;

    private final String tenantId;

    private final RouteTable routeTable;

    private final EndpointECFVersionMap versionMap;

    private final EventStorage eventStorage;

    private final EventDeliveryTable eventDeliveryTable;

    private final Map<String, EndpointObjectHash> endpoints;

    private final long eventTimeout;

    private boolean firstConnectRequestToActor = true;

    private final Map<RouteTableAddress, GlobalRouteInfo> localRoutes;

    private String mainUserNode;

    LocalUserActorMessageProcessor(AkkaContext context, String userId, String tenantId) {
        super();
        this.cacheService = context.getCacheService();
        this.eventService = context.getEventService();
        this.eventTimeout = context.getEventTimeout();
        this.userId = userId;
        this.tenantId = tenantId;
        this.endpoints = new HashMap<>();
        this.routeTable = new RouteTable();
        this.versionMap = new EndpointECFVersionMap();
        this.eventStorage = new EventStorage();
        this.eventDeliveryTable = new EventDeliveryTable();
        this.localRoutes = new HashMap<RouteTableAddress, GlobalRouteInfo>();
        this.mainUserNode = eventService.getUserNode(userId);
    }

    void processEndpointConnectMessage(ActorContext context, EndpointUserConnectMessage message) {
        RouteTableAddress address = new RouteTableAddress(message.getKey(), message.getAppToken());

        // register endpoint for send/receive events
        registerEndpointForEvents(context, message, address);

        // report existence of this endpoint to global user actor
        addGlobalRoute(context, message, address);

        endpoints.put(getActorPathName(message.getOriginator()), address.getEndpointKey());
    }

    void processClusterUpdate(ActorContext context) {
        String newNode = eventService.getUserNode(userId);
        if (!mainUserNode.equals(newNode)) {
            LOG.trace("User node changed from {} to {}", mainUserNode, newNode);
            mainUserNode = newNode;
            for (GlobalRouteInfo route : localRoutes.values()) {
                sendGlobalRouteUpdate(context, route);
            }
        }
    }

    void processEndpointDisconnectMessage(ActorContext context, EndpointUserDisconnectMessage message) {
        List<String> actorsToRemove = new LinkedList<>();
        for (Entry<String, EndpointObjectHash> entry : endpoints.entrySet()) {
            if (entry.getValue().equals(message.getKey())) {
                actorsToRemove.add(entry.getKey());
            }
        }
        for (String actor : actorsToRemove) {
            LOG.debug("[{}] removed endpoint actor [{}]", userId, actor);
            endpoints.remove(actor);
        }
        removeEndpoint(context, message.getKey());
    }

    void processEndpointEventSendMessage(ActorContext context, EndpointEventSendMessage message) {
        EndpointObjectHash sender = message.getKey();
        List<Event> events = message.getEvents();
        for (Event event : events) {
            processEvent(context, new EndpointEvent(sender, event));
        }
    }

    void processRemoteEndpointEventMessage(ActorContext context, RemoteEndpointEventMessage message) {
        LOG.debug("[{}] Processing remote event message: {}", userId, message);
        EndpointEvent localEvent = message.getEvent().toLocalEvent();
        processEvent(context, localEvent);
    }

    void processEndpointEventTimeoutMessage(ActorContext context, EndpointEventTimeoutMessage message) {
        LOG.debug("[{}] processing event timeout message for [{}]", userId, message.getEvent().getId());
        if (eventStorage.clear(message.getEvent())) {
            LOG.debug("[{}] removed event [{}] from storage", userId, message.getEvent().getId());
        }
        if (eventDeliveryTable.clear(message.getEvent())) {
            LOG.debug("[{}] removed event [{}] from delivery table", userId, message.getEvent().getId());
        }
    }

    void processEndpointEventDeliveryMessage(ActorContext context, EndpointEventDeliveryMessage message) {
        LOG.debug("[{}] processing event delivery message for [{}] with status {}", userId, message.getMessage().getAddress(),
                message.getStatus());
        boolean success = message.getStatus() == EventDeliveryStatus.SUCCESS;
        RouteTableAddress address = message.getMessage().getAddress();
        for (EndpointEvent event : message.getMessage().getEndpointEvents()) {
            if (success) {
                LOG.debug("[{}] registering successful delivery of event [{}] to address {}", userId, event.getId(), address);
                eventDeliveryTable.registerDeliverySuccess(event, address);
            } else {
                LOG.debug("[{}] registering failure to delivery of event [{}] to address {}", userId, event.getId(), address);
                eventDeliveryTable.registerDeliveryFailure(event, address);
            }
        }
    }

    void processRouteInfoMessage(ActorContext context, RouteInfoMessage message) {
        RouteInfo routeInfo = message.getRouteInfo();
        if (RouteOperation.DELETE.equals(routeInfo.getRouteOperation())) {
            LOG.debug("[{}] Removing all routes from route table by address {}", userId, routeInfo.getAddress());
            routeTable.removeByAddress(routeInfo.getAddress());
        } else {
            for (EventClassFamilyVersion ecfVersion : routeInfo.getEcfVersions()) {
                RouteTableKey key = new RouteTableKey(routeInfo.getAddress().getApplicationToken(), ecfVersion);
                LOG.debug("[{}] Updating route table with key {} and address {}", userId, key, routeInfo.getAddress());
                updateRouteTable(context, key, routeInfo.getAddress());
            }
        }
        reportAllLocalRoutes(routeInfo.getAddress().getServerId());
    }

    void processUserRouteInfoMessage(ActorContext context, UserRouteInfoMessage message) {
        UserRouteInfo userRouteInfo = message.getRouteInfo();
        LOG.debug("[{}] Cleanup all route table data related to serverId: {}", userId, userRouteInfo.getServerId());
        routeTable.clearRemoteServerData(userRouteInfo.getServerId());
        if (!RouteOperation.DELETE.equals(userRouteInfo.getRouteOperation())) {
            reportAllLocalRoutes(userRouteInfo.getServerId());
        }
    }

    void processTerminationMessage(ActorContext context, Terminated message) {
        ActorRef terminated = message.actor();
        if (terminated instanceof LocalActorRef) {
            LocalActorRef localActor = (LocalActorRef) terminated;
            String name = getActorPathName(localActor);
            EndpointObjectHash endpoint = endpoints.remove(name);
            if (endpoint != null) {
                boolean stilPresent = false;
                for (EndpointObjectHash existingEndpoint : endpoints.values()) {
                    if (existingEndpoint.equals(endpoint)) {
                        stilPresent = true;
                        break;
                    }
                }
                if (stilPresent) {
                    LOG.debug("[{}] received termination message for endpoint actor [{}], "
                            + "but other actor is still registered for this endpoint.", userId, localActor);
                } else {
                    removeEndpoint(context, endpoint);
                    LOG.debug("[{}] removed endpoint [{}]", userId, localActor);
                }
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    private void registerEndpointForEvents(ActorContext context, EndpointUserConnectMessage message, RouteTableAddress address) {
        List<EventClassFamilyVersion> ecfVersions = message.getEcfVersions();
        if (!ecfVersions.isEmpty()) {
            for (EventClassFamilyVersion ecfVersion : ecfVersions) {
                RouteTableKey key = new RouteTableKey(address.getApplicationToken(), ecfVersion);
                updateRouteTable(context, key, address);
            }
            if (firstConnectRequestToActor) {
                firstConnectRequestToActor = false;
                // report existence of this actor to other operation servers
                eventService.sendUserRouteInfo(new UserRouteInfo(tenantId, userId));
            }
            for (String serverId : routeTable.getRemoteServers()) {
                if (routeTable.isDeliveryRequired(serverId, address)) {
                    LOG.debug("[{}] Sending route info about address {} to server {}", userId, address, serverId);
                    eventService.sendRouteInfo(new RouteInfo(tenantId, userId, address, ecfVersions), serverId);
                }
            }
            versionMap.put(address.getEndpointKey(), message.getEcfVersions());
        }
    }

    protected String getActorPathName(ActorRef actorRef) {
        return actorRef.path().name();
    }

    private void addGlobalRoute(ActorContext context, EndpointUserConnectMessage message, RouteTableAddress address) {
        GlobalRouteInfo route = GlobalRouteInfo.add(tenantId, userId, address, message.getCfVersion(), message.getUcfHash());
        localRoutes.put(address, route);
        sendGlobalRouteUpdate(context, route);
    }

    private void sendGlobalRouteUpdate(ActorContext context, GlobalRouteInfo route) {
        if (eventService.isMainUserNode(userId)) {
            context.parent().tell(new EndpointRouteUpdateMessage(route), context.self());
        } else {
            LOG.debug("[{}] Sending connect message to global actor", userId);
            eventService.sendEndpointRouteInfo(route);
        }
    }

    private void updateRouteTable(ActorContext context, RouteTableKey key, RouteTableAddress address) {
        LOG.debug("[{}] adding to route table key: {} address: {}", userId, key, address);
        routeTable.add(key, address);
        sendPendingEvents(context, key, address);
    }

    private void sendPendingEvents(ActorContext context, RouteTableKey key, RouteTableAddress address) {
        List<EndpointEvent> events = eventStorage.getEvents(key, address);
        if (events.size() > 0) {
            sendEventsToRecepient(context, address, events);
        }
    }

    private void sendEventToRecepients(ActorContext context, EndpointEvent event, Collection<RouteTableAddress> recipients) {
        for (RouteTableAddress recipient : recipients) {
            sendEventsToRecepient(context, recipient, Collections.singletonList(event));
        }
    }

    private void sendEventsToRecepient(ActorContext context, RouteTableAddress recipient, List<EndpointEvent> events) {
        List<EndpointEvent> eventsToSend = new ArrayList<>(events.size());
        for (EndpointEvent event : events) {
            if (!eventDeliveryTable.isDeliveryStarted(event, recipient)) {
                eventsToSend.add(event);
            }
        }

        if (eventsToSend.size() > 0) {
            if (recipient.isLocal()) {
                if (LOG.isTraceEnabled()) {
                    for (EndpointEvent event : eventsToSend) {
                        LOG.trace("[{}] forwarding event {} to local recepient {}", userId, event, recipient);
                    }
                }
                EndpointEventReceiveMessage message = new EndpointEventReceiveMessage(userId, eventsToSend, recipient, context.self());
                sendEventToLocal(context, message);
            } else {
                for (EndpointEvent event : eventsToSend) {
                    LOG.trace("[{}] forwarding event {} to remote recepient {}", userId, event, recipient);
                    RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(tenantId, userId, event, recipient);
                    eventService.sendEvent(remoteEvent);
                }
            }

            for (EndpointEvent event : eventsToSend) {
                LOG.debug("[{}] registering delivery attempt of event {} to recepient {}", userId, event, recipient);
                eventDeliveryTable.registerDeliveryAttempt(event, recipient);
            }
        }
    }

    protected void sendEventToLocal(ActorContext context, EndpointEventReceiveMessage message) {
        context.parent().tell(message, context.self());
    }

    private void processEvent(ActorContext context, EndpointEvent event) {
        String fqn = event.getEventClassFQN();
        LOG.debug("[{}] Processing event {} from {}", userId, event.getId(), event.getSender());

        Integer version;
        if (event.getVersion() == 0) {
            version = lookupVersion(event, fqn);
        } else {
            version = event.getVersion();
        }
        if (version != null && version > 0) {
            event.setVersion(version);
            Set<RouteTableKey> recipientKeys = cacheService.getRouteKeys(new EventClassFqnVersion(tenantId, fqn, version));
            if (!recipientKeys.isEmpty()) {
                LOG.debug("[{}] Put event {} with {} recipient keys to storage", userId, event.getId(), recipientKeys.size());
                eventStorage.put(event, recipientKeys);

                Set<RouteTableAddress> recipients = routeTable.getRoutes(recipientKeys, event.getTarget());

                recipients = filterOutRecipientsByKeyHash(event, recipients);

                if (!recipients.isEmpty()) {
                    sendEventToRecepients(context, event, recipients);
                } else {
                    LOG.debug("[{}] there is no recipients for event with class fqn {} and version {} yet", userId, fqn, version);
                }

                scheduleTimeoutMessage(context, event);
            } else {
                LOG.debug("[{}] event {} is ignored due to it does not have any potential recepients", userId, event.getId());
            }
        }
    }

    protected Integer lookupVersion(EndpointEvent event, String fqn) {
        Integer version;
        LOG.debug("[{}] Lookup event class family id using event class fqn {}", userId, fqn);
        String ecfId = cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(tenantId, fqn));

        LOG.debug("[{}] Lookup event {} version from user's version map using ecfId {} ", userId, fqn, ecfId);
        version = versionMap.get(event.getSender(), ecfId);
        if (version == null) {
            LOG.warn("[{}] Lookup event {} version from user's version map using ecfId {} FAILED!", userId, fqn, ecfId);
        }
        return version;
    }

    protected Set<RouteTableAddress> filterOutRecipientsByKeyHash(EndpointEvent event, Set<RouteTableAddress> recipients) {
        Iterator<RouteTableAddress> recipientsIterator = recipients.iterator();
        while (recipientsIterator.hasNext()) {
            RouteTableAddress recipient = recipientsIterator.next();
            if (recipient.getEndpointKey().equals(event.getSender())) {
                recipientsIterator.remove();
            }
        }
        return recipients;
    }

    protected void removeEndpoint(ActorContext context, EndpointObjectHash endpoint) {
        LOG.debug("[{}] removing endpoint [{}] from route tables", userId, endpoint);
        RouteTableAddress address = routeTable.removeLocal(endpoint);
        versionMap.remove(endpoint);
        for (String serverId : routeTable.getRemoteServers()) {
            LOG.debug("[{}] removing endpoint [{}] from remote route table on server {}", userId, endpoint, serverId);
            eventService.sendRouteInfo(RouteInfo.deleteRouteFromAddress(tenantId, userId, address), serverId);
        }
        // cleanup and notify global route actor
        GlobalRouteInfo route = GlobalRouteInfo.delete(tenantId, userId, address);
        if (eventService.isMainUserNode(userId)) {
            context.parent().tell(new EndpointRouteUpdateMessage(route), context.self());
        } else {
            LOG.debug("[{}] Sending disconnect message to global actor", userId);
            eventService.sendEndpointRouteInfo(route);
        }
    }

    private void reportAllLocalRoutes(String serverId) {
        LOG.debug("[{}] Reporting all local routes to serverId: {}", userId, serverId);
        Set<RouteTableAddress> localAddresses = routeTable.getAllLocalRoutes();
        List<RouteInfo> localRoutes = new ArrayList<>();
        for (RouteTableAddress localAddress : localAddresses) {
            if (routeTable.isDeliveryRequired(serverId, localAddress)) {
                Set<RouteTableKey> routeKeys = routeTable.getLocalRouteTableKeys(localAddress);
                Set<EventClassFamilyVersion> ecfVersions = new HashSet<>();
                for (RouteTableKey routeKey : routeKeys) {
                    ecfVersions.add(routeKey.getEcfVersion());
                }
                localRoutes.add(new RouteInfo(tenantId, userId, localAddress, new ArrayList<>(ecfVersions)));
            } else {
                LOG.debug("[{}] Address {} is already delivered to serverId {} and will not be sent again", userId, localAddress, serverId);
            }
        }

        LOG.debug("[{}] Reporting {}/{} local addresses/routes count", userId, localAddresses.size(), localRoutes.size());
        if (!localRoutes.isEmpty()) {
            eventService.sendRouteInfo(localRoutes, serverId);
            routeTable.registerRouteInfoReport(localAddresses, serverId);
        }
    }

    void scheduleTimeoutMessage(ActorContext context, EndpointEvent event) {
        context.system()
                .scheduler()
                .scheduleOnce(Duration.create(getTTL(event), TimeUnit.MILLISECONDS), context.self(),
                        new EndpointEventTimeoutMessage(event), context.dispatcher(), context.self());
    }
    
    private long getTTL(EndpointEvent event){
        return Math.max(eventTimeout - (System.currentTimeMillis() - event.getCreateTime()), 0L);
    }
}
