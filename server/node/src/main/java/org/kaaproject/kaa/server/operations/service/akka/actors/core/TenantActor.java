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

import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.CORE_DISPATCHER_NAME;
import static org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService.USER_DISPATCHER_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.user.GlobalUserActor;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.user.LocalUserActor;
import org.kaaproject.kaa.server.operations.service.akka.actors.supervision.SupervisionStrategyFactory;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.ApplicationActorStatusResponse;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestState;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.TenantActorStatusResponse;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionRouteMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.GlobalUserAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.transport.message.SessionControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class TenantActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TenantActor.class);

    /** The Akka service context */
    private final AkkaContext context;

    /** The applications. */
    private final Map<String, ActorRef> applications;

    /** The local users. */
    private final Map<String, ActorRef> localUsers;

    /** The global users. */
    private final Map<String, ActorRef> globalUsers;

    private final String tenantId;

    private final Map<UUID, StatusRequestState> statusRequestStatesMap;

    private TenantActor(AkkaContext context, String tenantId) {
        super();
        this.context = context;
        this.tenantId = tenantId;
        this.applications = new HashMap<>();
        this.localUsers = new HashMap<>();
        this.globalUsers = new HashMap<>();
        this.statusRequestStatesMap = new HashMap<UUID, StatusRequestState>();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return SupervisionStrategyFactory.createTenantActorStrategy(context);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<TenantActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        private final String tenantId;

        /**
         * Instantiates a new actor creator.
         *
         * @param context the context
         * @param tenantId the tenant id
         */
        public ActorCreator(AkkaContext context, String tenantId) {
            super();
            this.context = context;
            this.tenantId = tenantId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public TenantActor create() throws Exception {
            return new TenantActor(context, tenantId);
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
            LOG.trace("[{}] Received: {}", tenantId, message);
        } else {
            LOG.debug("[{}] Received: {}", tenantId, message.getClass().getName());
        }
        if (message instanceof EndpointActorMsg) {
            processEndpointActorMsg((EndpointActorMsg) message);
        }else if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        } else if (message instanceof GlobalUserAwareMessage) {
            processGlobalUserAwareMessage((GlobalUserAwareMessage) message);
        } else if (message instanceof SessionControlMessage) {
            processSessionControlMessage((SessionControlMessage) message);
        } else if (message instanceof RouteMessage) {
            processRouteMessage((RouteMessage<?>)message);
        } else if (message instanceof UserAwareMessage) {
            processUserAwareMessage((UserAwareMessage) message);
        } else if (message instanceof Terminated) {
            processTermination((Terminated) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processNotificationMessage((ThriftNotificationMessage) message);
        } else if (message instanceof EndpointUserActionRouteMessage) {
            processEndpointUserActionRouteMessage((EndpointUserActionRouteMessage) message);
        } else if (message instanceof ClusterUpdateMessage) {
            processClusterUpdate((ClusterUpdateMessage) message);
        } else if (message instanceof StatusRequestMessage) {
            processStatusRequest((StatusRequestMessage) message);
        } else if (message instanceof ApplicationActorStatusResponse) {
            processStatusResponse((ApplicationActorStatusResponse) message);
        }
    }

    private void processRouteMessage(RouteMessage<?> msg) {
        if(msg.getAppToken() != null){
            ActorRef applicationActor = getOrCreateApplicationActor(msg.getAppToken());
            applicationActor.tell(msg, self());
        }
    }

    private void processClusterUpdate(ClusterUpdateMessage message) {
        for (ActorRef userActor : globalUsers.values()) {
            userActor.tell(message, ActorRef.noSender());
        }
        for (ActorRef userActor : localUsers.values()) {
            userActor.tell(message, ActorRef.noSender());
        }
        for (ActorRef appActor : applications.values()) {
            appActor.tell(message, ActorRef.noSender());
        }
    }

    private void processStatusRequest(StatusRequestMessage message) {
        LOG.debug("[{}] Processing status request", message.getId());
        statusRequestStatesMap.put(message.getId(), new StatusRequestState(message, applications.size()));
        for (ActorRef tenant : applications.values()) {
            tenant.tell(new StatusRequestMessage(message.getId()), this.getSelf());
        }
    }

    private void processStatusResponse(ApplicationActorStatusResponse message) {
        StatusRequestState state = statusRequestStatesMap.get(message.getRequestId());
        if (state != null) {
            if (state.processResponse(message)) {
                int endpointCount = state.getEndpontCount();
                context().parent().tell(new TenantActorStatusResponse(message.getRequestId(), endpointCount), ActorRef.noSender());
            }
        } else {
            LOG.warn("[{}] State for status request is not found", message.getRequestId());
        }
    }

    private void processSessionControlMessage(SessionControlMessage message) {
        ActorRef applicationActor = getOrCreateApplicationActor(message.getSessionInfo().getApplicationToken());
        applicationActor.tell(message, self());
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
    
    private void processEndpointActorMsg(EndpointActorMsg message) {
        ActorRef applicationActor = getOrCreateApplicationActor(message.getAddress().getAppToken());
        applicationActor.tell(message, self());
    }

    /**
     * Process endpoint aware message.
     *
     * @param message
     *            the message
     */
    private void processEndpointAwareMessage(EndpointAwareMessage message) {
        if (message instanceof EndpointUserConnectMessage) {
            processUserAwareMessage((EndpointUserConnectMessage) message);
        } else if (message instanceof EndpointUserDisconnectMessage) {
            processUserAwareMessage((EndpointUserDisconnectMessage) message);
        } else if (message instanceof EndpointEventSendMessage) {
            processUserAwareMessage((EndpointEventSendMessage) message);
        } else {
            ActorRef applicationActor = getOrCreateApplicationActor(message.getAppToken());
            applicationActor.tell(message, self());
        }
    }

    private void processEndpointUserActionRouteMessage(EndpointUserActionRouteMessage message) {
        for (Entry<String, ActorRef> entry : applications.entrySet()) {
            if (!entry.getKey().equals(message.getOriginalApplicationToken())) {
                LOG.debug("[{}] Forwarding message to [{}] application", tenantId, entry.getKey());
                entry.getValue().tell(message, self());
            }
        }
    }

    private void processUserAwareMessage(UserAwareMessage message) {
        ActorRef userActor;
        if (message instanceof RouteInfoMessage || message instanceof UserRouteInfoMessage) {
            LOG.debug("Find user actor by id: {} for message {}", message.getUserId(), message);
            userActor = localUsers.get(toLocal(message.getUserId()));
        } else {
            userActor = getOrCreateUserActor(message.getUserId());
        }
        if (userActor != null) {
            userActor.tell(message, self());
        } else {
            LOG.debug("[{}] user aware message ignored due to no such user actor: [{}]", tenantId, message.getUserId());
        }
    }

    private void processGlobalUserAwareMessage(GlobalUserAwareMessage message) {
        getOrCreateGlobalUserActor(message.getUserId()).tell(message, self());
    }

    private ActorRef getOrCreateUserActor(String userId) {
        String localUserId = toLocal(userId);
        ActorRef userActor = localUsers.get(localUserId);
        if (userActor == null && userId != null) {
            userActor = context().actorOf(
                    Props.create(new LocalUserActor.ActorCreator(context, userId, tenantId)).withDispatcher(USER_DISPATCHER_NAME),
                    localUserId);
            LOG.debug("Create local user actor with id {}", userId);
            localUsers.put(localUserId, userActor);
            context().watch(userActor);
        }
        return userActor;
    }

    private ActorRef getOrCreateGlobalUserActor(String userId) {
        String globalUserId = toGlobal(userId);
        ActorRef userActor = globalUsers.get(globalUserId);
        if (userActor == null && userId != null) {
            userActor = context().actorOf(
                    Props.create(new GlobalUserActor.ActorCreator(context, userId, tenantId)).withDispatcher(USER_DISPATCHER_NAME),
                    globalUserId);
            LOG.debug("Create global user actor with id {}", userId);
            globalUsers.put(globalUserId, userActor);
            context().watch(userActor);
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
            applicationActor = context().actorOf(
                    Props.create(new ApplicationActor.ActorCreator(context, tenantId, appToken)).withDispatcher(CORE_DISPATCHER_NAME), appToken);
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
            } else if (localUsers.remove(name) != null) {
                LOG.debug("[{}] removed local user: {}", tenantId, localActor);
            } else if (globalUsers.remove(name) != null) {
                LOG.debug("[{}] removed global user: {}", tenantId, localActor);
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    private String toLocal(String name) {
        return "LOCAL_" + name;
    }

    private String toGlobal(String name) {
        return "GLOBAL_" + name;
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
