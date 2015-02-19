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

import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.CORE_DISPATCHER_NAME;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.TenantAwareMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
import org.kaaproject.kaa.server.transport.message.SessionControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class OperationsServerActor.
 */
public class OperationsServerActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OperationsServerActor.class);

    /** The operations service. */
    private final CacheService cacheService;

    /** The operations service. */
    private final OperationsService operationsService;

    /** The notification delta service. */
    private final NotificationDeltaService notificationDeltaService;

    /** The event service. */
    private final EventService eventService;

    /** The tenants id-actor map. */
    private final Map<String, ActorRef> tenants;

    /** The application service. */
    private final ApplicationService applicationService;

    /** The log appender service. */
    private final LogAppenderService logAppenderService;

    /** The endpoint user service. */
    private final EndpointUserService endpointUserService;

    /**
     * Instantiates a new endpoint server actor.
     *
     * @param cacheService
     *            the cache service
     * @param operationsService
     *            the operations service
     * @param notificationDeltaService
     *            the notification delta service
     * @param eventService
     *            the event service
     * @param applicationService
     *            the application service
     * @param logAppenderService
     *            the log appender service
     */
    private OperationsServerActor(CacheService cacheService, OperationsService operationsService,
            NotificationDeltaService notificationDeltaService, EventService eventService, ApplicationService applicationService,
            LogAppenderService logAppenderService, EndpointUserService endpointUserService) {
        super();
        this.tenants = new HashMap<String, ActorRef>();
        this.cacheService = cacheService;
        this.operationsService = operationsService;
        this.notificationDeltaService = notificationDeltaService;
        this.eventService = eventService;
        this.applicationService = applicationService;
        this.logAppenderService = logAppenderService;
        this.endpointUserService = endpointUserService;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<OperationsServerActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The endpoint service. */
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

        /** The endpoint user service. */
        private final EndpointUserService endpointUserService;

        /**
         * Instantiates a new actor creator.
         *
         * @param cacheService
         *            the cache service
         * @param operations
         *            the operations service
         * @param notificationDeltaService
         *            the notification delta service
         * @param eventService
         *            the event service
         * @param applicationService
         *            the application service
         * @param logAppenderService
         *            the log appender service
         */
        public ActorCreator(CacheService cacheService, OperationsService endpointService,
                NotificationDeltaService notificationDeltaService, EventService eventService, ApplicationService applicationService,
                LogAppenderService logAppenderService, EndpointUserService endpointUserService) {
            super();
            this.cacheService = cacheService;
            this.operationsService = endpointService;
            this.notificationDeltaService = notificationDeltaService;
            this.eventService = eventService;
            this.applicationService = applicationService;
            this.logAppenderService = logAppenderService;
            this.endpointUserService = endpointUserService;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public OperationsServerActor create() throws Exception {
            return new OperationsServerActor(cacheService, operationsService, notificationDeltaService, eventService, applicationService,
                    logAppenderService, endpointUserService);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("Received: {}", message);
        if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        } else if (message instanceof SessionControlMessage) {
            processSessionControlMessage((SessionControlMessage) message);
        } else if (message instanceof TenantAwareMessage) {
            processTenantAwareMessage((TenantAwareMessage) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processNotificationMessage((ThriftNotificationMessage) message);
        }
    }

    private void processTenantAwareMessage(TenantAwareMessage message) {
        ActorRef tenantActor = getOrCreateTenantActorByTokenId(message.getTenantId());
        tenantActor.tell(message, self());
    }

    /**
     * Process notification message.
     *
     * @param message
     *            the message
     */
    private void processNotificationMessage(ThriftNotificationMessage message) {
        ActorRef tenantActor = getOrCreateTenantActorByAppToken(message.getAppToken());
        tenantActor.tell(message, self());
    }

    /**
     * Process endpoint aware message.
     *
     * @param message
     *            the message
     */
    private void processEndpointAwareMessage(EndpointAwareMessage message) {
        ActorRef tenantActor = getOrCreateTenantActorByAppToken(message.getAppToken());
        tenantActor.tell(message, self());
    }
    
    private void processSessionControlMessage(SessionControlMessage message){
        ActorRef  tenantActor = getOrCreateTenantActorByAppToken(message.getSessionInfo().getApplicationToken());
        tenantActor.tell(message, self());
    }

    /**
     * Gets the or create application actor.
     *
     * @param appToken
     *            the app token
     * @return the or create application actor
     */
    private ActorRef getOrCreateTenantActorByAppToken(String appToken) {
        String tenantId = cacheService.getTenantIdByAppToken(appToken);
        return getOrCreateTenantActorByTokenId(tenantId);
    }

    private ActorRef getOrCreateTenantActorByTokenId(String tenantId) {
        ActorRef tenantActor = tenants.get(tenantId);
        if (tenantActor == null) {
            tenantActor = context().actorOf(
                    Props.create(new TenantActor.ActorCreator(cacheService, operationsService, notificationDeltaService, eventService,
                            applicationService, logAppenderService, endpointUserService, tenantId)).withDispatcher(CORE_DISPATCHER_NAME), tenantId);
            tenants.put(tenantId, tenantActor);
        }
        return tenantActor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("Starting {}", this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("Stoped {}", this);
    }
}
