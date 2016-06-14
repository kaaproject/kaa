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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.ENDPOINT_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.LOG_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.TOPIC_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.VERIFIER_DISPATCHER_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global.GlobalEndpointActorCreator;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.LocalEndpointActorCreator;
import org.kaaproject.kaa.server.operations.service.akka.actors.supervision.SupervisionStrategyFactory;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ActorClassifier;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointClusterAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.ApplicationActorStatusResponse;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestMessage;
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

    private final Map<EndpointObjectHash, GlobalEndpointActorMD> globalEndpointSessions;
    /** The endpoint sessions. */
    private final Map<EndpointObjectHash, LocalEndpointActorMD> localEndpointSessions;

    private final Map<String, EndpointObjectHash> endpointActorMap;

    /** The topic sessions. */
    private final Map<String, ActorRef> topicSessions;

    private final String nodeId;

    private final String tenantId;

    private final String appToken;

    private final Map<String, ActorRef> logsSessions;

    private final Map<String, ActorRef> userVerifierSessions;

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
    private ApplicationActor(AkkaContext context, String tenantId, String applicationToken) {
        this.context = context;
        this.nodeId = context.getClusterService().getNodeId();
        this.tenantId = tenantId;
        this.appToken = applicationToken;
        this.globalEndpointSessions = new HashMap<>();
        this.localEndpointSessions = new HashMap<>();
        this.endpointActorMap = new HashMap<>();
        this.topicSessions = new HashMap<>();
        this.logsSessions = new HashMap<>();
        this.userVerifierSessions = new HashMap<>();
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

        private final String tenantId;

        private final String appToken;

        /**
         * Instantiates a new actor creator.
         *
         * @param context
         *            the context
         * @param tenantId
         *            the tenant id
         * @param appToken
         *            the application token
         */
        public ActorCreator(AkkaContext context, String tenantId, String appToken) {
            super();
            this.context = context;
            this.tenantId = tenantId;
            this.appToken = appToken;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public ApplicationActor create() throws Exception {
            return new ApplicationActor(context, tenantId, appToken);
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
            LOG.trace("[{}] Received: {}", appToken, message);
        } else {
            LOG.debug("[{}] Received: {}", appToken, message.getClass().getName());
        }
        if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        }
        if (message instanceof EndpointActorMsg) {
            processEndpointActorMsg((EndpointActorMsg) message);
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
        } else if (message instanceof ClusterUpdateMessage) {
            processClusterUpdate((ClusterUpdateMessage) message);
        } else if (message instanceof RouteMessage<?>) {
            processRouteMessage((RouteMessage<?>) message);
        }
    }

    private void processEndpointActorMsg(EndpointActorMsg message) {
        EndpointAddress address = message.getAddress();
        EndpointObjectHash endpointId = EndpointObjectHash.fromBytes(address.getEntityId());
        ActorClassifier classifier = message.getClassifier();
        if (classifier == ActorClassifier.APPLICATION) {
            boolean processed = false;
            if (message instanceof ThriftEndpointActorMsg<?>) {
                processed = processCommonThriftEndpointActorMsg(endpointId, (ThriftEndpointActorMsg<?>) message);
            }
            if (!processed) {
                LOG.warn("[{}] Failed to lookup processor for endpoint msg {}.", endpointId, message);
            }
        } else {
            EndpointActorMD actorMD = null;
            if (classifier == ActorClassifier.GLOBAL) {
                actorMD = globalEndpointSessions.get(endpointId);
            } else if (classifier == ActorClassifier.LOCAL) {
                actorMD = localEndpointSessions.get(endpointId);
            }
            if (actorMD != null) {
                actorMD.actorRef.tell(message, context().self());
            } else {
                LOG.warn("[{}] Failed to lookup {} actor for endpoint.", endpointId, classifier.name());
            }
        }
    }

    private boolean processCommonThriftEndpointActorMsg(EndpointObjectHash endpointId, ThriftEndpointActorMsg<?> msg) {
        if (msg.getMsg() instanceof ThriftEndpointDeregistrationMessage) {
            forwardMessageQuietly(globalEndpointSessions.get(endpointId), msg);
            forwardMessageQuietly(localEndpointSessions.get(endpointId), msg);
            return true;
        } else {
            return false;
        }
    }
    
    private void forwardMessageQuietly(EndpointActorMD actorMD, Object msg) {
        if (actorMD != null) {
            actorMD.actorRef.tell(msg, context().self());
        }
    }

    private void processClusterUpdate(ClusterUpdateMessage message) {
        for (Entry<EndpointObjectHash, LocalEndpointActorMD> entry : localEndpointSessions.entrySet()) {
            String globalActorNodeId = getGlobalEndpointActorNodeId(entry.getKey());
            if (!globalActorNodeId.equals(entry.getValue().globalActorNodeId)) {
                entry.getValue().globalActorNodeId = globalActorNodeId;
                notifyGlobalEndpointActor(entry.getKey(), globalActorNodeId);
            }
        }
        for (GlobalEndpointActorMD entry : globalEndpointSessions.values()) {
            entry.actorRef.tell(message, context().self());
        }
    }

    /**
     * Process log event pack message.
     *
     * @param message
     *            the message
     */
    private void processLogEventPackMessage(LogEventPackMessage message) {
        LOG.debug("[{}] Processing log event pack message", appToken);
        applicationLogActor.tell(message, self());
    }

    private void processUserVerificationRequestMessage(UserVerificationRequestMessage message) {
        LOG.debug("[{}] Processing user verification request message", appToken);
        userVerifierActor.tell(message, self());
    }

    private void processLogNotificationMessage(ThriftNotificationMessage message) {
        processThriftNotificationMessage(applicationLogActor, message);
    }

    private void processUserVerifierNotificationMessage(ThriftNotificationMessage message) {
        processThriftNotificationMessage(userVerifierActor, message);
    }

    private void processThriftNotificationMessage(ActorRef actor, ThriftNotificationMessage message) {
        LOG.debug("[{}] Processing thrift notification message {}", appToken, message);
        actor.tell(message, self());
    }

    private void processStatusRequest(StatusRequestMessage message) {
        LOG.debug("[{}] Processing status request", message.getId());
        int endpointCount = localEndpointSessions.size();
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
            LOG.debug("[{}] Forwarding message to specific topic", appToken);
            sendToSpecificTopic(message);
        } else if (notification.isSetAppenderId()) {
            LOG.debug("[{}] Forwarding message to application log actor", appToken);
            processLogNotificationMessage(message);
        } else if (notification.isSetUserVerifierToken()) {
            LOG.debug("[{}] Forwarding message to application user verifier actor", appToken);
            processUserVerifierNotificationMessage(message);
        } else {
            LOG.debug("[{}] Broadcasting message to all endpoints", appToken);
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
     * Broadcast to all endpoints.
     *
     * @param message
     *            the message
     */
    private void broadcastToAllEndpoints(ThriftNotificationMessage message) {
        for (LocalEndpointActorMD endpoint : localEndpointSessions.values()) {
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
        LocalEndpointActorMD endpointMetaData = localEndpointSessions.get(message.getSessionInfo().getKey());
        if (endpointMetaData != null) {
            endpointMetaData.actorRef.tell(message, self());
        } else {
            LOG.debug("[{}] Can't find endpoint actor that corresponds to {}", appToken, message.getSessionInfo().getKey());
        }
    }

    private void processEndpointEventReceiveMessage(EndpointEventReceiveMessage message) {
        LocalEndpointActorMD endpointActor = localEndpointSessions.get(message.getKey());
        if (endpointActor != null) {
            endpointActor.actorRef.tell(message, self());
        } else {
            LOG.debug("[{}] Can't find endpoint actor that corresponds to {}", appToken, message.getKey());
            context().parent().tell(new EndpointEventDeliveryMessage(message, EventDeliveryStatus.FAILURE), self());
        }
    }

    private void processEndpointEventSendMessage(EndpointEventSendMessage message) {
        LOG.debug("[{}] Forwarding message to specific user", appToken, message.getUserId());
        context().parent().tell(message, self());
    }

    private void processEndpointEventDeliveryMessage(EndpointEventDeliveryMessage message) {
        LOG.debug("[{}] Forwarding message to specific user", appToken, message.getUserId());
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
        LocalEndpointActorMD actorMD = localEndpointSessions.get(message.getKey());
        if (actorMD == null) {
            EndpointObjectHash endpointKey = message.getKey();
            String endpointActorId = LocalEndpointActorCreator.generateActorKey();
            LOG.debug("[{}] Creating actor with endpointKey: {}", appToken, endpointActorId);
            String globalActorNodeId = getGlobalEndpointActorNodeId(endpointKey);
            actorMD = new LocalEndpointActorMD(context()
                    .actorOf(Props.create(new LocalEndpointActorCreator(context, endpointActorId, message.getAppToken(), message.getKey()))
                            .withDispatcher(ENDPOINT_DISPATCHER_NAME), endpointActorId),
                    endpointActorId, globalActorNodeId);
            localEndpointSessions.put(message.getKey(), actorMD);
            endpointActorMap.put(endpointActorId, message.getKey());
            context().watch(actorMD.actorRef);
            notifyGlobalEndpointActor(endpointKey, globalActorNodeId);
        }
        actorMD.actorRef.tell(message, self());
    }

    private String getGlobalEndpointActorNodeId(EndpointObjectHash endpointKey) {
        return context.getClusterService().getEntityNode(endpointKey);
    }

    private void notifyGlobalEndpointActor(EndpointObjectHash endpointKey, String globalActorNodeId) {
        notifyGlobalEndpointActor(endpointKey, globalActorNodeId, RouteOperation.ADD);
    }

    private void notifyGlobalEndpointActor(EndpointObjectHash endpointKey, String globalActorNodeId, RouteOperation operation) {
        EndpointRouteMessage msg = new EndpointRouteMessage(new EndpointClusterAddress(nodeId, tenantId, appToken, endpointKey), operation);
        if (globalActorNodeId.equals(nodeId)) {
            processEndpointRouteMessage(msg);
        } else {
            context.getClusterService().sendRouteMessage(msg);
        }
    }

    private void processRouteMessage(RouteMessage<?> msg) {
        if (msg instanceof EndpointRouteMessage) {
            processEndpointRouteMessage((EndpointRouteMessage) msg);
        }
    }

    private void processEndpointRouteMessage(EndpointRouteMessage msg) {
        EndpointObjectHash endpointKey = msg.getAddress().getEndpointKey();
        GlobalEndpointActorMD actorMD = globalEndpointSessions.get(endpointKey);
        if (actorMD == null) {
            String endpointActorId = GlobalEndpointActorCreator.generateActorKey();
            LOG.debug("[{}] Creating global endpoint actor for endpointKey: {}", appToken, endpointKey);
            actorMD = new GlobalEndpointActorMD(
                    context().actorOf(Props.create(new GlobalEndpointActorCreator(context, endpointActorId, appToken, endpointKey))
                            .withDispatcher(ENDPOINT_DISPATCHER_NAME), endpointActorId),
                    endpointActorId);
            globalEndpointSessions.put(endpointKey, actorMD);
            context().watch(actorMD.actorRef);
        }
        actorMD.actorRef.tell(msg, self());
    }

    private void processEndpointUserActionMessage(EndpointUserActionMessage message, boolean escalate) {
        LocalEndpointActorMD endpointMetaData = localEndpointSessions.get(message.getKey());
        if (endpointMetaData != null) {
            LOG.debug("[{}] Found affected endpoint and forwarding message to it", appToken);
            endpointMetaData.actorRef.tell(message, self());
        } else if (escalate) {
            LOG.debug("[{}] Failed to fing affected endpoint in scope of current application. Forwarding message to tenant actor", appToken);
            EndpointUserActionRouteMessage routeMessage = new EndpointUserActionRouteMessage(message, appToken);
            context().parent().tell(routeMessage, self());
        }
    }

    private void updateEndpointActor(EndpointStopMessage message) {
        String actorKey = message.getActorKey();
        EndpointObjectHash endpointKey = message.getEndpointKey();
        LOG.debug("[{}] Stoping actor [{}] with [{}]", appToken, message.getActorKey(), endpointKey);
        LocalEndpointActorMD endpointMetaData = localEndpointSessions.get(endpointKey);
        if (endpointMetaData != null) {
            if (actorKey.equals(endpointMetaData.actorId)) {
                localEndpointSessions.remove(endpointKey);
                LOG.debug("[{}] Removed actor [{}] from endpoint sessions map", appToken, actorKey);
            }
        } else {
            LOG.warn("[{}] EndpointSession for actor {} is not found!", appToken, endpointKey);
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
                LocalEndpointActorMD actorMetaData = localEndpointSessions.get(endpointHash);
                if (actorMetaData != null && actorMetaData.actorRef.equals(localActor)) {
                    localEndpointSessions.remove(endpointHash);
                    LOG.debug("[{}] removed endpoint: {}", appToken, localActor);
                    notifyGlobalEndpointActor(endpointHash, actorMetaData.globalActorNodeId, RouteOperation.DELETE);
                }
            } else if (topicSessions.remove(name) != null) {
                LOG.debug("[{}] removed topic: {}", appToken, localActor);
            } else if (logsSessions.remove(name) != null) {
                LOG.debug("[{}] removed log: {}", appToken, localActor);
                applicationLogActor = getOrCreateLogActor(name);
                LOG.debug("[{}] created log: {}", appToken, applicationLogActor);
            } else if (userVerifierSessions.remove(name) != null) {
                LOG.debug("[{}] removed log: {}", appToken, localActor);
                userVerifierActor = getOrCreateUserVerifierActor(name);
                LOG.debug("[{}] created log: {}", appToken, applicationLogActor);
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
                    Props.create(new ApplicationLogActor.ActorCreator(context, appToken)).withDispatcher(LOG_DISPATCHER_NAME));
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
            userVerifierActor = context()
                    .actorOf(
                            Props.create(new ApplicationUserVerifierActor.ActorCreator(context, appToken)).withDispatcher(
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
        LOG.info("[{}] Starting ", appToken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("[{}] Stoped ", appToken);
    }
}
