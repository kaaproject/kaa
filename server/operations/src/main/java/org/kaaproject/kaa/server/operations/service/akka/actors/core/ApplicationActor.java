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

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestProcessedMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.SessionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicRegistrationRequestMessage;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;


/**
 * The Class ApplicationActor.
 */
public class ApplicationActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationActor.class);

    /** The operations service. */
    private OperationsService operationsService;

    /** The notification delta service. */
    private NotificationDeltaService notificationDeltaService;

    /** The endpoint sessions. */
    private Map<String, ActorMetaData> endpointSessions;

    /** The topic sessions. */
    private Map<String, ActorRef> topicSessions;

    /**
     * Instantiates a new application actor.
     * 
     * @param operationsService
     *            the operations service
     * @param notificationDeltaService
     *            the notification delta service
     */
    public ApplicationActor(OperationsService operationsService, NotificationDeltaService notificationDeltaService) {
        this.operationsService = operationsService;
        this.notificationDeltaService = notificationDeltaService;
        this.endpointSessions = new HashMap<>();
        this.topicSessions = new HashMap<>();
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<ApplicationActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The operations service. */
        private OperationsService operationsService;

        /** The notification delta service. */
        private NotificationDeltaService notificationDeltaService;

        /**
         * Instantiates a new actor creator.
         * 
         * @param operationsService
         *            the operations service
         * @param notificationDeltaService
         *            the notification delta service
         */
        public ActorCreator(OperationsService operationsService, NotificationDeltaService notificationDeltaService) {
            super();
            this.operationsService = operationsService;
            this.notificationDeltaService = notificationDeltaService;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public ApplicationActor create() throws Exception {
            return new ApplicationActor(operationsService, notificationDeltaService);
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
        } else if (message instanceof Terminated) {
            processTermination((Terminated) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processThriftNotification((ThriftNotificationMessage) message);
        } else if (message instanceof RequestProcessedMessage){
            updateEndpointActor((RequestProcessedMessage)message); 
        }
    }

    /**
     * Process thrift notification.
     * 
     * @param message
     *            the message
     */
    private void processThriftNotification(ThriftNotificationMessage message) {
        Notification notification = message.getNotification();
        if (notification.isSetNotificationId()) {
            LOG.debug("Forwarding message to specific topic");
            sendToSpecificTopic(message);
        } else if (notification.isSetUnicastNotificationId()) {
            LOG.debug("Forwarding message to specific endpoint");
            sendToSpecificEndpoint(message);
        } else {
            LOG.debug("Broadcasting message to all endpoints");
            broadcastToAllEndpoints(message);
        }
    }

    /**
     * Send to specific topic.
     * 
     * @param message
     *            the message
     */
    private void sendToSpecificTopic(ThriftNotificationMessage message) {
        Notification notification = message.getNotification();
        ActorRef topicActor = getOrCreateTopic(notification.getTopicId());
        topicActor.tell(message, self());
    }

    /**
     * Gets the or create topic.
     * 
     * @param topicId
     *            the topic id
     * @return the or create topic
     */
    private ActorRef getOrCreateTopic(String topicId) {
        ActorRef topicActor = topicSessions.get(topicId);
        if (topicActor == null) {
            topicActor = context().actorOf(Props.create(new TopicActor.ActorCreator(notificationDeltaService)), buildTopicKey(topicId));
            topicSessions.put(topicId, topicActor);
            context().watch(topicActor);
        }
        return topicActor;
    }

    /**
     * Send to specific endpoint.
     * 
     * @param message
     *            the message
     */
    private void sendToSpecificEndpoint(ThriftNotificationMessage message) {
        EndpointObjectHash keyHash = EndpointObjectHash.fromBytes(message.getNotification().getKeyHash());
        String endpointKey = buildEndpointKey(keyHash);
        ActorMetaData endpointActor = endpointSessions.get(endpointKey);
        if (endpointActor != null) {
            endpointActor.actorRef.tell(NotificationMessage.fromUnicastId(message.getNotification().getUnicastNotificationId()), self());
        } else {
            LOG.debug("Can't find endpoint actor that corresponds to {} ", endpointKey);
        }
    }

    /**
     * Broadcast to all endpoints.
     * 
     * @param message
     *            the message
     */
    private void broadcastToAllEndpoints(ThriftNotificationMessage message) {
        for (ActorMetaData endpoint : endpointSessions.values()) {
            endpoint.actorRef.tell(message, self());
        }
    }

    /**
     * Process endpoint aware message.
     * 
     * @param message
     *            the message
     */
    private void processEndpointAwareMessage(EndpointAwareMessage message) {
        if (message instanceof SessionMessage) {
            processEndpointRequest((SessionMessage) message);
        } else if (message instanceof TopicRegistrationRequestMessage) {
            processEndpointRegistration((TopicRegistrationRequestMessage) message);
        } else {
            processEndpointRequest(message);
        }
    }

    /**
     * Process endpoint registration.
     * 
     * @param message
     *            the message
     */
    private void processEndpointRegistration(TopicRegistrationRequestMessage message) {
        ActorRef topicActor = getOrCreateTopic(message.getTopicId());
        topicActor.tell(message, self());
    }

    /**
     * Process session endpoint request.
     * 
     * @param message
     *            the message
     */
    private void processEndpointRequest(EndpointAwareMessage message) {
        String endpointKey = buildEndpointKey(message.getKey());
        ActorMetaData endpointMetaData = endpointSessions.get(endpointKey);
        if (endpointMetaData == null) {
            LOG.debug("Creating actor with endpointKey: {}", endpointKey);
            endpointMetaData = new ActorMetaData(context().actorOf(Props.create(new EndpointActor.ActorCreator(operationsService, endpointKey)), endpointKey));
            endpointSessions.put(endpointKey, endpointMetaData);
            context().watch(endpointMetaData.actorRef);
        }
        endpointMetaData.registerRequest(ActorMetaData.getHash(message));
        endpointMetaData.actorRef.tell(message, self());
    }
    
    private void updateEndpointActor(RequestProcessedMessage message) {
        String endpointKey = message.getEndpointActorKey();
        LOG.debug("Update EndpointSession for actor {}", endpointKey);
        ActorMetaData endpointMetaData = endpointSessions.get(endpointKey);
        if(endpointMetaData != null){
            boolean requestFound = endpointMetaData.registerResponse(message.getRequestHash());
            if(requestFound){
                int pendingRequestSize = endpointMetaData.getPendingRequestsSize();
                if(pendingRequestSize == 0){
                    LOG.debug("No more pending requests for endpointKey {}. Going to stop endpoint actor.", endpointKey);
                    context().stop(endpointMetaData.actorRef);
                    endpointSessions.remove(endpointKey);
                }else{
                    LOG.debug("There is still {} pending requests for endpointKey {}", pendingRequestSize, endpointKey);
                }
            }else{
                LOG.warn("Pending request {} is not found!", message.getRequestHash());
            }            
        }else{
            LOG.warn("EndpointSession for actor {} is not found!", endpointKey);            
        }
        
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
            if (endpointSessions.remove(name) != null) {
                LOG.debug("removed endpoint: {}", localActor);
            } else if (topicSessions.remove(name) != null) {
                LOG.debug("removed topic: {}", localActor);
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    /**
     * Builds the endpoint key.
     * 
     * @param key
     *            the key
     * @return the string
     */
    public static String buildEndpointKey(EndpointObjectHash key) {
        // TODO: Improve;
        return Integer.toString(key.hashCode());
    }

    /**
     * Builds the topic key.
     * 
     * @param topicId
     *            the topic id
     * @return the string
     */
    public static String buildTopicKey(String topicId) {
        // TODO: Improve;
        return topicId;
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
