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

import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.ENDPOINT_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.LOG_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.TOPIC_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.VERIFIER_DISPATCHER_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.supervision.SupervisionStrategyFactory;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.ServerProfileUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin.EndpointExtMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin.PluginExtMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin.PluginMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin.SdkExtensionKey;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.ApplicationActorStatusResponse;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicSubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage.EventDeliveryStatus;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationRequestMessage;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class ApplicationActor.
 */
public class ApplicationActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationActor.class);

    /** The Akka service context */
    private final AkkaContext context;

    private final String applicationToken;

    /** The endpoint sessions. */
    private final Map<EndpointObjectHash, ActorMetaData> endpointSessions;

    private final Map<String, EndpointObjectHash> endpointActorMap;

    /** The topic actor sessions. */
    private final Map<String, ActorRef> topicSessions;

    /** The log actor sessions. */
    private final Map<String, ActorRef> logsSessions;

    /** The user verifier actor sessions. */
    private final Map<String, ActorRef> userVerifierSessions;

    private final Map<SdkExtensionKey, String> pluginIdMap;

    private ActorRef applicationLogActor;

    private ActorRef userVerifierActor;

    /**
     * Instantiates a new application actor.
     *
     * @param context
     *            the context
     * @param applicationToken
     *            the application token
     */
    private ApplicationActor(AkkaContext context, String applicationToken) {
        this.context = context;
        this.applicationToken = applicationToken;
        this.endpointSessions = new HashMap<>();
        this.endpointActorMap = new HashMap<>();
        this.topicSessions = new HashMap<>();
        this.logsSessions = new HashMap<>();
        this.userVerifierSessions = new HashMap<>();
        this.pluginIdMap = new HashMap<>();
        this.applicationLogActor = getOrCreateLogActor();
        this.userVerifierActor = getOrCreateUserVerifierActor();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return SupervisionStrategyFactory.createApplicationActorStrategy(context);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<ApplicationActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        private final String applicationToken;

        /**
         * Instantiates a new actor creator.
         *
         * @param context
         *            the context
         * @param applicationToken
         *            the application token
         */
        public ActorCreator(AkkaContext context, String applicationToken) {
            super();
            this.context = context;
            this.applicationToken = applicationToken;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public ApplicationActor create() throws Exception {
            return new ApplicationActor(context, applicationToken);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Received: {}", applicationToken, message);
        } else {
            LOG.debug("[{}] Received: {}", applicationToken, message.getClass().getName());
        }
        if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        } else if (message instanceof PluginMsg) {
            processPluginMsg((PluginMsg) message);
        } else if (message instanceof SessionAware) {
            processSessionAwareMessage((SessionAware) message);
        } else if (message instanceof EndpointEventDeliveryMessage) {
            processEndpointEventDeliveryMessage((EndpointEventDeliveryMessage) message);
        } else if (message instanceof Terminated) {
            processTermination((Terminated) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processThriftNotification((ThriftNotificationMessage) message);
        } else if (message instanceof EndpointStopMessage) {
            updateEndpointActor((EndpointStopMessage) message);
        } else if (message instanceof LogEventPackMessage) {
            processLogEventPackMessage((LogEventPackMessage) message);
        } else if (message instanceof UserVerificationRequestMessage) {
            processUserVerificationRequestMessage((UserVerificationRequestMessage) message);
        } else if (message instanceof EndpointUserActionMessage) {
            processEndpointUserActionMessage((EndpointUserActionMessage) message, true);
        } else if (message instanceof EndpointUserActionRouteMessage) {
            processEndpointUserActionMessage(((EndpointUserActionRouteMessage) message).getMessage(), false);
        } else if (message instanceof StatusRequestMessage) {
            processStatusRequest((StatusRequestMessage) message);
        }
    }

    private void processPluginMsg(PluginMsg msg) {
        if (msg instanceof PluginExtMsg) {
            processPluginExtMsg((PluginExtMsg) msg);
        } else {
            processEndpointExtMsg((EndpointExtMsg) msg);
        }
    }

    private void processPluginExtMsg(PluginExtMsg msg) {
        LOG.debug("[{}] Processing plugin message {}", applicationToken, msg);
    }

    private void processEndpointExtMsg(EndpointExtMsg msg) {
        LOG.debug("[{}] Processing extension message {}", applicationToken, msg);
        SdkExtensionKey pluginKey = msg.getExtKey();
        String pluginId = pluginIdMap.get(pluginKey);
        if (pluginId == null) {
            pluginId = context.getCacheService().getPluginInstanceId(pluginKey);
            pluginIdMap.put(pluginKey, pluginId);
        }
        LOG.debug("[{}] Going to forward this message to plugin with id [{}]", applicationToken, pluginId);
        context().parent().tell(new PluginExtMsg(pluginId, msg), context().self());
    }

    /**
     * Process log event pack message.
     *
     * @param message
     *            the message
     */
    private void processLogEventPackMessage(LogEventPackMessage message) {
        LOG.debug("[{}] Processing log event pack message", applicationToken);
        applicationLogActor.tell(message, self());
    }

    private void processUserVerificationRequestMessage(UserVerificationRequestMessage message) {
        LOG.debug("[{}] Processing user verification request message", applicationToken);
        userVerifierActor.tell(message, self());
    }

    private void processLogNotificationMessage(ThriftNotificationMessage message) {
        processThriftNotificationMessage(applicationLogActor, message);
    }

    private void processUserVerifierNotificationMessage(ThriftNotificationMessage message) {
        processThriftNotificationMessage(userVerifierActor, message);
    }

    private void processThriftNotificationMessage(ActorRef actor, ThriftNotificationMessage message) {
        LOG.debug("[{}] Processing thrift notification message {}", applicationToken, message);
        actor.tell(message, self());
    }

    private void processStatusRequest(StatusRequestMessage message) {
        LOG.debug("[{}] Processing status request", message.getId());
        int endpointCount = endpointSessions.size();
        context().parent().tell(new ApplicationActorStatusResponse(message.getId(), endpointCount), ActorRef.noSender());
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
            LOG.debug("[{}] Forwarding message to specific topic", applicationToken);
            sendToSpecificTopic(message);
        } else if (notification.isSetKeyHash()) {
            LOG.debug("[{}] Forwarding message to specific endpoint", applicationToken);
            sendToSpecificEndpoint(message);
        } else if (notification.isSetAppenderId()) {
            LOG.debug("[{}] Forwarding message to application log actor", applicationToken);
            processLogNotificationMessage(message);
        } else if (notification.isSetUserVerifierToken()) {
            LOG.debug("[{}] Forwarding message to application user verifier actor", applicationToken);
            processUserVerifierNotificationMessage(message);
        } else if (notification.getKeyHash() != null) {

        } else {
            LOG.debug("[{}] Broadcasting message to all endpoints", applicationToken);
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
            topicActor = context().actorOf(
                    Props.create(new TopicActor.ActorCreator(context.getNotificationDeltaService())).withDispatcher(TOPIC_DISPATCHER_NAME),
                    buildTopicKey(topicId));
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
        ActorMetaData endpointActor = endpointSessions.get(keyHash);
        if (endpointActor != null) {
            if (message.getNotification().getOp() == Operation.UPDATE_SERVER_PROFILE) {
                endpointActor.actorRef.tell(new ServerProfileUpdateMessage(), self());
            } else {
                endpointActor.actorRef
                        .tell(NotificationMessage.fromUnicastId(message.getNotification().getUnicastNotificationId()), self());
            }
        } else {
            LOG.debug("[{}] Can't find endpoint actor that corresponds to {} ", applicationToken, keyHash);
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
        if (message instanceof TopicSubscriptionMessage) {
            processEndpointTopicRegistration((TopicSubscriptionMessage) message);
        } else if (message instanceof EndpointUserConnectMessage) {
            processEndpointUserRegistration((EndpointUserConnectMessage) message);
        } else if (message instanceof EndpointUserDisconnectMessage) {
            processEndpointUserDeregistration((EndpointUserDisconnectMessage) message);
        } else if (message instanceof EndpointEventSendMessage) {
            processEndpointEventSendMessage((EndpointEventSendMessage) message);
        } else if (message instanceof EndpointEventReceiveMessage) {
            processEndpointEventReceiveMessage((EndpointEventReceiveMessage) message);
        } else {
            processEndpointRequest(message);
        }
    }

    /**
     * Process endpoint aware message.
     *
     * @param message
     *            the message
     */
    private void processSessionAwareMessage(SessionAware message) {
        ActorMetaData endpointMetaData = endpointSessions.get(message.getSessionInfo().getKey());
        if (endpointMetaData != null) {
            endpointMetaData.actorRef.tell(message, self());
        } else {
            LOG.debug("[{}] Can't find endpoint actor that corresponds to {}", applicationToken, message.getSessionInfo().getKey());
        }
    }

    private void processEndpointEventReceiveMessage(EndpointEventReceiveMessage message) {
        ActorMetaData endpointActor = endpointSessions.get(message.getKey());
        if (endpointActor != null) {
            endpointActor.actorRef.tell(message, self());
        } else {
            LOG.debug("[{}] Can't find endpoint actor that corresponds to {}", applicationToken, message.getKey());
            context().parent().tell(new EndpointEventDeliveryMessage(message, EventDeliveryStatus.FAILURE), self());
        }
    }

    private void processEndpointEventSendMessage(EndpointEventSendMessage message) {
        LOG.debug("[{}] Forwarding message to specific user", applicationToken, message.getUserId());
        context().parent().tell(message, self());
    }

    private void processEndpointEventDeliveryMessage(EndpointEventDeliveryMessage message) {
        LOG.debug("[{}] Forwarding message to specific user", applicationToken, message.getUserId());
        context().parent().tell(message, self());
    }

    /**
     * Process endpoint registration.
     *
     * @param message
     *            the message
     */
    private void processEndpointTopicRegistration(TopicSubscriptionMessage message) {
        ActorRef topicActor = getOrCreateTopic(message.getTopicId());
        topicActor.tell(message, self());
    }

    /**
     * Process endpoint registration.
     *
     * @param message
     *            the message
     */
    private void processEndpointUserRegistration(EndpointUserConnectMessage message) {
        context().parent().tell(message, self());
    }

    /**
     * Process endpoint deregistration.
     *
     * @param message
     *            the message
     */
    private void processEndpointUserDeregistration(EndpointUserDisconnectMessage message) {
        context().parent().tell(message, self());
    }

    /**
     * Process session endpoint request.
     *
     * @param message
     *            the message
     */
    private void processEndpointRequest(EndpointAwareMessage message) {
        ActorMetaData endpointMetaData = endpointSessions.get(message.getKey());
        if (endpointMetaData == null) {
            UUID uuid = UUID.randomUUID();
            String endpointActorId = uuid.toString().replaceAll("-", "");
            LOG.debug("[{}] Creating actor with endpointKey: {}", applicationToken, endpointActorId);
            endpointMetaData = new ActorMetaData(context().actorOf(
                    Props.create(new EndpointActor.ActorCreator(context, endpointActorId, message.getAppToken(), message.getKey()))
                            .withDispatcher(ENDPOINT_DISPATCHER_NAME), endpointActorId), endpointActorId);
            endpointSessions.put(message.getKey(), endpointMetaData);
            endpointActorMap.put(endpointActorId, message.getKey());
            context().watch(endpointMetaData.actorRef);
        }
        endpointMetaData.actorRef.tell(message, self());
    }

    private void processEndpointUserActionMessage(EndpointUserActionMessage message, boolean escalate) {
        ActorMetaData endpointMetaData = endpointSessions.get(message.getKey());
        if (endpointMetaData != null) {
            LOG.debug("[{}] Found affected endpoint and forwarding message to it", applicationToken);
            endpointMetaData.actorRef.tell(message, self());
        } else if (escalate) {
            LOG.debug("[{}] Failed to fing affected endpoint in scope of current application. Forwarding message to tenant actor",
                    applicationToken);
            EndpointUserActionRouteMessage routeMessage = new EndpointUserActionRouteMessage(message, applicationToken);
            context().parent().tell(routeMessage, self());
        }
    }

    private void updateEndpointActor(EndpointStopMessage message) {
        String actorKey = message.getActorKey();
        EndpointObjectHash endpointKey = message.getEndpointKey();
        LOG.debug("[{}] Stoping actor [{}] with [{}]", applicationToken, message.getActorKey(), endpointKey);
        ActorMetaData endpointMetaData = endpointSessions.get(endpointKey);
        if (endpointMetaData != null) {
            if (actorKey.equals(endpointMetaData.getEndpointActorId())) {
                endpointSessions.remove(endpointKey);
                LOG.debug("[{}] Removed actor [{}] from endpoint sessions map", applicationToken, actorKey);
            }
        } else {
            LOG.warn("[{}] EndpointSession for actor {} is not found!", applicationToken, endpointKey);
        }
        endpointActorMap.remove(actorKey);
        message.getOriginator().tell(message, self());
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
            EndpointObjectHash endpointHash = endpointActorMap.remove(name);
            if (endpointHash != null) {
                ActorMetaData actorMetaData = endpointSessions.get(endpointHash);
                if (actorMetaData != null && actorMetaData.actorRef.equals(localActor)) {
                    endpointSessions.remove(endpointHash);
                    LOG.debug("[{}] removed endpoint: {}", applicationToken, localActor);
                }
            } else if (topicSessions.remove(name) != null) {
                LOG.debug("[{}] removed topic: {}", applicationToken, localActor);
            } else if (logsSessions.remove(name) != null) {
                LOG.debug("[{}] removed log: {}", applicationToken, localActor);
                applicationLogActor = getOrCreateLogActor(name);
                LOG.debug("[{}] created log: {}", applicationToken, applicationLogActor);
            } else if (userVerifierSessions.remove(name) != null) {
                LOG.debug("[{}] removed log: {}", applicationToken, localActor);
                userVerifierActor = getOrCreateUserVerifierActor(name);
                LOG.debug("[{}] created log: {}", applicationToken, applicationLogActor);
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    private ActorRef getOrCreateLogActor() {
        return getOrCreateLogActor(null);
    }

    private ActorRef getOrCreateLogActor(String name) {
        ActorRef logActor = logsSessions.get(name);
        if (logActor == null) {
            logActor = context().actorOf(
                    Props.create(new ApplicationLogActor.ActorCreator(context, applicationToken)).withDispatcher(LOG_DISPATCHER_NAME));
            context().watch(logActor);
            logsSessions.put(logActor.path().name(), logActor);
        }
        return logActor;
    }

    private ActorRef getOrCreateUserVerifierActor() {
        return getOrCreateUserVerifierActor(null);
    }

    private ActorRef getOrCreateUserVerifierActor(String name) {
        ActorRef userVerifierActor = userVerifierSessions.get(name);
        if (userVerifierActor == null) {
            userVerifierActor = context().actorOf(
                    Props.create(new ApplicationUserVerifierActor.ActorCreator(context, applicationToken)).withDispatcher(
                            VERIFIER_DISPATCHER_NAME));
            context().watch(userVerifierActor);
            userVerifierSessions.put(userVerifierActor.path().name(), userVerifierActor);
        }
        return userVerifierActor;
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
        LOG.info("[{}] Starting ", applicationToken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("[{}] Stoped ", applicationToken);
    }
}
