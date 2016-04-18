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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.AkkaServiceStatus;
import org.kaaproject.kaa.server.operations.service.akka.AkkaStatusListener;
import org.kaaproject.kaa.server.operations.service.akka.actors.supervision.SupervisionStrategyFactory;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestState;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.TenantActorStatusResponse;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.TenantAwareMessage;
import org.kaaproject.kaa.server.transport.message.SessionControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class OperationsServerActor.
 */
public class OperationsServerActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OperationsServerActor.class);

    /** The Akka service context */
    private final AkkaContext context;

    /** The tenants id-actor map. */
    private final Map<String, ActorRef> tenants;

    private final Map<UUID, StatusRequestState> statusRequestStatesMap;

    /**
     * Instantiates a new endpoint server actor.
     *
     * @param context
     *            the akka service context
     */
    private OperationsServerActor(AkkaContext context) {
        super();
        this.context = context;
        this.tenants = new HashMap<>();
        this.statusRequestStatesMap = new HashMap<>();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return SupervisionStrategyFactory.createOpsActorStrategy(context);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<OperationsServerActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        /**
         * Instantiates a new actor creator.
         *
         * @param context
         *            - the Akka service context
         */
        public ActorCreator(AkkaContext context) {
            super();
            this.context = context;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public OperationsServerActor create() throws Exception {
            return new OperationsServerActor(context);
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
        if (message instanceof EndpointActorMsg) {
            processEndpointActorMsg((EndpointActorMsg) message);
        } else if (message instanceof EndpointAwareMessage) {
            processEndpointAwareMessage((EndpointAwareMessage) message);
        } else if (message instanceof SessionControlMessage) {
            processSessionControlMessage((SessionControlMessage) message);
        } else if (message instanceof TenantAwareMessage) {
            processTenantAwareMessage((TenantAwareMessage) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processNotificationMessage((ThriftNotificationMessage) message);
        } else if (message instanceof ClusterUpdateMessage) {
            processClusterUpdate((ClusterUpdateMessage) message);
        } else if (message instanceof StatusRequestMessage) {
            processStatusRequest((StatusRequestMessage) message);
        } else if (message instanceof TenantActorStatusResponse) {
            processStatusResponse((TenantActorStatusResponse) message);
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

    private void processEndpointActorMsg(EndpointActorMsg message) {
        ActorRef tenantActor = getOrCreateTenantActorByTokenId(message.getAddress().getTenantId());
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

    private void processSessionControlMessage(SessionControlMessage message) {
        ActorRef tenantActor = getOrCreateTenantActorByAppToken(message.getSessionInfo().getApplicationToken());
        tenantActor.tell(message, self());
    }

    private void processClusterUpdate(ClusterUpdateMessage message) {
        for (ActorRef tenantActor : tenants.values()) {
            tenantActor.tell(message, ActorRef.noSender());
        }
    }

    private void processStatusRequest(StatusRequestMessage message) {
        LOG.debug("[{}] Processing status request", message.getId());
        if (tenants.size() > 0) {
            statusRequestStatesMap.put(message.getId(), new StatusRequestState(message, tenants.size()));
            for (ActorRef tenant : tenants.values()) {
                tenant.tell(new StatusRequestMessage(message.getId()), this.getSelf());
            }
        } else {
            if(message.getListener() != null){
                message.getListener().onStatusUpdate(new AkkaServiceStatus(System.currentTimeMillis(), 0));
            }
        }
    }

    private void processStatusResponse(TenantActorStatusResponse message) {
        StatusRequestState state = statusRequestStatesMap.get(message.getRequestId());
        if (state != null) {
            if (state.processResponse(message)) {
                int endpointCount = state.getEndpontCount();
                AkkaStatusListener listener = state.getOriginator().getListener();
                if (listener != null) {
                    listener.onStatusUpdate(new AkkaServiceStatus(System.currentTimeMillis(), endpointCount));
                } else {
                    LOG.warn("[{}] Calculated state for empty listener", message.getRequestId());
                }
                statusRequestStatesMap.remove(message.getRequestId());
            }
        } else {
            LOG.warn("[{}] State for status request is not found", message.getRequestId());
        }
    }

    /**
     * Gets the or create application actor.
     *
     * @param appToken
     *            the app token
     * @return the or create application actor
     */
    private ActorRef getOrCreateTenantActorByAppToken(String appToken) {
        String tenantId = context.getCacheService().getTenantIdByAppToken(appToken);
        return getOrCreateTenantActorByTokenId(tenantId);
    }

    private ActorRef getOrCreateTenantActorByTokenId(String tenantId) {
        ActorRef tenantActor = tenants.get(tenantId);
        if (tenantActor == null) {
            tenantActor = context().actorOf(
                    Props.create(new TenantActor.ActorCreator(context, tenantId)).withDispatcher(CORE_DISPATCHER_NAME), tenantId);
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
