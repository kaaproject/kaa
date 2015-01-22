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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class TenantActor extends UntypedActor{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TenantActor.class);

    /** The cache service. */
    private final CacheService cacheService;

    /** The operations service. */
    private final OperationsService operationsService;

    /** The notification delta service. */
    private final NotificationDeltaService notificationDeltaService;

    /** The event service. */
    private final EventService eventService;

    /** The application service. */
    private final ApplicationService applicationService;

    /** The log appender service. */
    private final LogAppenderService logAppenderService;

    /** The applications. */
    private final Map<String, ActorRef> applications;

    /** The applications. */
    private final Map<String, ActorRef> users;

    private final String tenantId;

    private TenantActor(CacheService cacheService, OperationsService endpointService, NotificationDeltaService notificationDeltaService, EventService eventService,
            ApplicationService applicationService, LogAppenderService logAppenderService, String tenantId) {
        super();
        this.cacheService = cacheService;
        this.operationsService = endpointService;
        this.notificationDeltaService = notificationDeltaService;
        this.applicationService = applicationService;
        this.logAppenderService = logAppenderService;
        this.eventService = eventService;
        this.tenantId = tenantId;
        this.applications = new HashMap<>();
        this.users = new HashMap<>();
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<TenantActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The cache service. */
        private final CacheService cacheService;

        /** The operations service. */
        private final OperationsService operationsService;

        /** The notification delta service. */
        private final NotificationDeltaService notificationDeltaService;

        /** The event service. */
        private final EventService eventService;

        /** The application service. */
        private final ApplicationService applicationService;

        /** The log appender service. */
        private final LogAppenderService logAppenderService;

        private final String tenantId;

        /**
         * Instantiates a new actor creator.
         *
         * @param endpointService
         *            the endpoint service
         * @param notificationDeltaService
         *            the notification delta service
         * @param eventService
         */
        public ActorCreator(CacheService cacheService, OperationsService operationsService, NotificationDeltaService notificationDeltaService, EventService eventService,
                ApplicationService applicationService, LogAppenderService logAppenderService, String tenantId) {
            super();
            this.cacheService = cacheService;
            this.operationsService = operationsService;
            this.notificationDeltaService = notificationDeltaService;
            this.eventService = eventService;
            this.tenantId = tenantId;
            this.applicationService = applicationService;
            this.logAppenderService = logAppenderService;
        }

        /*
         * (non-Javadoc)
         *
         * @see akka.japi.Creator#create()
         */
        @Override
        public TenantActor create() throws Exception {
            return new TenantActor(cacheService, operationsService, notificationDeltaService, eventService, applicationService, logAppenderService, tenantId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if(LOG.isTraceEnabled()){
            LOG.trace("[{}] Received: {}", tenantId, message);
        }else{
            LOG.debug("[{}] Received: {}", tenantId, message.getClass().getName());
        }
        if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        } else if (message instanceof UserAwareMessage) {
            processUserAwareMessage((UserAwareMessage) message);
        } else if (message instanceof Terminated) {
            processTermination((Terminated) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processNotificationMessage((ThriftNotificationMessage) message);
        } else if (message instanceof EndpointUserActionRouteMessage){
            processEndpointUserActionRouteMessage((EndpointUserActionRouteMessage) message);
        }
    }

    /**
     * Process notification message.
     *
     * @param message
     *            the message
     */
    private void processNotificationMessage(ThriftNotificationMessage message) {
        ActorRef applicationActor = getOrCreateApplicationActor(message.getAppToken());
        applicationActor.tell(message, self());
    }

    /**
     * Process endpoint aware message.
     *
     * @param message
     *            the message
     */
    private void processEndpointAwareMessage(EndpointAwareMessage message) {
        if(message instanceof EndpointUserConnectMessage){
            processUserAwareMessage((EndpointUserConnectMessage) message);
        }else if(message instanceof EndpointUserDisconnectMessage){
            processUserAwareMessage((EndpointUserDisconnectMessage) message);
        }else if(message instanceof EndpointEventSendMessage){
            processUserAwareMessage((EndpointEventSendMessage) message);
        }else{
            ActorRef applicationActor = getOrCreateApplicationActor(message.getAppToken());
            applicationActor.tell(message, self());
        }
    }

    private void processEndpointUserActionRouteMessage(EndpointUserActionRouteMessage message) {
        for(Entry<String, ActorRef> entry : applications.entrySet()){
            if(!entry.getKey().equals(message.getOriginalApplicationToken())){
                LOG.debug("[{}] Forwarding message to [{}] application", tenantId, entry.getKey());
                entry.getValue().tell(message, self());
            }
        }
    }

    private void processUserAwareMessage(UserAwareMessage message) {
        ActorRef userActor;
        if(message instanceof RouteInfoMessage || message instanceof UserRouteInfoMessage){
            LOG.debug("Find user actor by id: {} for message {}", message.getUserId(), message);
            userActor = users.get(message.getUserId());
        }else{
            userActor = getOrCreateUserActor(message.getUserId());
        }
        if(userActor != null){
            userActor.tell(message, self());
        }else{
            LOG.debug("[{}] user aware message ignored due to no such user actor: [{}]", tenantId, message.getUserId());
        }
    }

    private ActorRef getOrCreateUserActor(String userId) {
        ActorRef userActor = users.get(userId);
        if (userActor == null && userId != null) {
            userActor = context().actorOf(Props.create(new UserActor.ActorCreator(cacheService, eventService, userId, tenantId)), userId);
            LOG.debug("Create user actor with id {}", userId);
            users.put(userId, userActor);
        }
        return userActor;
    }

    /**
     * Gets the or create application actor.
     *
     * @param appToken
     *            the app token
     * @return the or create application actor
     */
    private ActorRef getOrCreateApplicationActor(String appToken) {
        ActorRef applicationActor = applications.get(appToken);
        if (applicationActor == null) {
            applicationActor = context().actorOf(Props.create(new ApplicationActor.ActorCreator(operationsService, notificationDeltaService,
                    applicationService, logAppenderService, appToken)), appToken);
            applications.put(appToken, applicationActor);
        }
        return applicationActor;
    }

    /**
     * Process termination.
     *
     * @param message
     *            the message
     */
    private void processTermination(Terminated message) {
        ActorRef terminated = message.actor();
        if (terminated instanceof LocalActorRef) {
            LocalActorRef localActor = (LocalActorRef) terminated;
            String name = localActor.path().name();
            if (applications.remove(name) != null) {
                LOG.debug("[{}] removed application: {}", tenantId, localActor);
            } else if (users.remove(name) != null) {
                LOG.debug("[{}] removed user: {}", tenantId, localActor);
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("[{}] Starting", tenantId);
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("[{}] Stoped", tenantId);
    }

}
