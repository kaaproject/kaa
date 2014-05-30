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

import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
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
    private OperationsService operationsService;

    /** The notification delta service. */
    private NotificationDeltaService notificationDeltaService;

    /** The applications. */
    private Map<String, ActorRef> applications;

    /**
     * Instantiates a new endpoint server actor.
     * 
     * @param endpointService
     *            the endpoint service
     * @param notificationDeltaService
     *            the notification delta service
     */
    public OperationsServerActor(OperationsService endpointService, NotificationDeltaService notificationDeltaService) {
        super();
        this.operationsService = endpointService;
        this.notificationDeltaService = notificationDeltaService;
        this.applications = new HashMap<String, ActorRef>();
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<OperationsServerActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The endpoint service. */
        private OperationsService endpointService;

        /** The notification delta service. */
        private NotificationDeltaService notificationDeltaService;

        /**
         * Instantiates a new actor creator.
         * 
         * @param endpointService
         *            the endpoint service
         * @param notificationDeltaService
         *            the notification delta service
         */
        public ActorCreator(OperationsService endpointService, NotificationDeltaService notificationDeltaService) {
            super();
            this.endpointService = endpointService;
            this.notificationDeltaService = notificationDeltaService;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public OperationsServerActor create() throws Exception {
            return new OperationsServerActor(endpointService, notificationDeltaService);
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
        } else if (message instanceof ThriftNotificationMessage) {
            processNotificationMessage((ThriftNotificationMessage) message);
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
        ActorRef applicationActor = getOrCreateApplicationActor(message.getAppToken());
        applicationActor.tell(message, self());
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
            applicationActor = context().actorOf(Props.create(new ApplicationActor.ActorCreator(operationsService, notificationDeltaService)), appToken);
            applications.put(appToken, applicationActor);
        }
        return applicationActor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("Starting " + this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("Stoped " + this);
    }
}
