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

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.RegistrationRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.UpdateRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.LongSyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestProcessedMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.SessionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.SessionMessage.SessionAttributes;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicRegistrationRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Creator;


/**
 * The Class EndpointActor.
 */
public class EndpointActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EndpointActor.class);

    /** The operations service. */
    private OperationsService operationsService;

    /** The pending request. */
    private LongSyncRequestMessage pendingRequest;

    /** The pending response. */
    private SyncResponseHolder pendingResponse;

    /** The app token. */
    private String appToken;

    /** The key. */
    private EndpointObjectHash key;
    
    /** The sync time. */
    private long syncTime;
    
    private String endpointActorKey;


    /**
     * Instantiates a new endpoint actor.
     * 
     * @param endpointService
     *            the endpoint service
     */
    public EndpointActor(OperationsService endpointService, String endpointActorKey) {
        this.operationsService = endpointService;
        this.endpointActorKey = endpointActorKey;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<EndpointActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The operations service. */
        private OperationsService operationsService;
        
        private String endpointActorKey;

        /**
         * Instantiates a new actor creator.
         * 
         * @param operationsService
         *            the operations service
         */
        public ActorCreator(OperationsService operationsService, String endpointActorKey) {
            super();
            this.operationsService = operationsService;
            this.endpointActorKey = endpointActorKey;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public EndpointActor create() throws Exception {
            return new EndpointActor(operationsService, endpointActorKey);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("[{}] Received: {}", endpointActorKey, message);
        if (message instanceof EndpointAwareMessage) {
            populateEndpointInfo((EndpointAwareMessage) message);
            if (message instanceof SessionMessage) {
                processEndpointSyncSession((SessionMessage) message);
            } else {
                if (message instanceof RegistrationRequestMessage) {
                    processEndpointRegistration((RegistrationRequestMessage) message);
                } else if (message instanceof UpdateRequestMessage) {
                    processEndpointUpdate((UpdateRequestMessage) message);
                } else if (message instanceof SyncRequestMessage) {
                    processEndpointSync((SyncRequestMessage) message);
                }
            }
        } else {
            if (pendingRequest != null && pendingResponse != null) {
                if (message instanceof ThriftNotificationMessage) {
                    processThriftNotification((ThriftNotificationMessage) message);
                } else if (message instanceof NotificationMessage) {
                    processNotification((NotificationMessage) message);
                } else if (message instanceof RequestTimeoutMessage) {
                    sendReply(pendingRequest, pendingResponse.getResponse());
                }                
            }else{
                LOG.debug("[{}] Message ignored due to pending Request | Response is null", endpointActorKey, message);
            }
        } 
    }

    /**
     * Populate endpoint info.
     * 
     * @param message
     *            the message
     */
    private void populateEndpointInfo(EndpointAwareMessage message) {
        this.appToken = message.getAppToken();
        this.key = message.getKey();
    }

    /**
     * Process notification.
     * 
     * @param message
     *            the message
     */
    private void processNotification(NotificationMessage message) {
        if (pendingResponse.getResponse() != null) {
            SyncResponse syncResponse = operationsService.updateSyncResponse(pendingResponse.getResponse(), message.getNotifications(),
                    message.getUnicastNotificationId());
            sendReply(pendingRequest, syncResponse);
        } else {
            LOG.warn("[{}] Pending response is null during processing of {}!", endpointActorKey, message);
        }
    }

    /**
     * Process thrift notification.
     * 
     * @param message
     *            the message
     */
    private void processThriftNotification(ThriftNotificationMessage message) {
        SyncRequest syncRequest = pendingRequest.getRequest();
        SyncResponse syncResponse = pendingResponse.getResponse();
        LOG.debug("[{}] Change original request {} appSeqNumber from {} to {}", endpointActorKey, syncRequest, syncRequest.getAppStateSeqNumber(), syncResponse.getAppStateSeqNumber());
        syncRequest.setAppStateSeqNumber(syncResponse.getAppStateSeqNumber());
        LOG.debug("[{}] Processing request {}", endpointActorKey, pendingRequest);
        processLongSync(pendingRequest);
    }

    /**
     * Process endpoint sync session.
     * 
     * @param message
     *            the message
     */
    private void processEndpointSyncSession(SessionMessage message) {
        boolean replied = false;
        pendingRequest = (LongSyncRequestMessage) message;
        if (message instanceof LongSyncRequestMessage) {
            replied = processLongSync(pendingRequest);
        }
        if (!replied) {
            subscribeToTopics(pendingResponse);
            scheduleTimeoutMessage(message);
        }
    }

    /**
     * Subscribe to topics.
     * 
     * @param response
     *            the response
     */
    private void subscribeToTopics(SyncResponseHolder response) {
        for (Entry<String, Integer> entry : response.getSubscriptionStates().entrySet()) {
            TopicRegistrationRequestMessage topicSubscriptionMessage = new TopicRegistrationRequestMessage(entry.getKey(), entry.getValue(),
                    response.getSystemNfVersion(), response.getUserNfVersion(), appToken, key, self());
            context().parent().tell(topicSubscriptionMessage, self());
        }
    }

    /**
     * Process long sync.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    private boolean processLongSync(LongSyncRequestMessage message) {
        return doInTemplate(new SyncReponseCalculator<SyncRequest>() {
            @Override
            public SyncResponseHolder getSyncReponse(SyncRequest request) throws GetDeltaException {
                return operationsService.sync(request);
            }
        }, message.getRequest(), message, true);
    }

    /**
     * Require immediate reply.
     * 
     * @param response
     *            the response
     * @return true, if successful
     */
    private boolean requireImmediateReply(SyncResponse response) {
        return response.getResponseType() != SyncResponseStatus.NO_DELTA;
    }

    /**
     * Process endpoint registration.
     * 
     * @param message
     *            the message
     */
    private void processEndpointRegistration(RegistrationRequestMessage message) {
        doInTemplate(new SyncReponseCalculator<EndpointRegistrationRequest>() {
            @Override
            public SyncResponseHolder getSyncReponse(EndpointRegistrationRequest request) throws GetDeltaException {
                return operationsService.registerEndpoint(request);
            }
        }, message.getRequest(), message);
    }

    /**
     * Process endpoint update.
     * 
     * @param message
     *            the message
     */
    private void processEndpointUpdate(UpdateRequestMessage message) {
        doInTemplate(new SyncReponseCalculator<ProfileUpdateRequest>() {
            @Override
            public SyncResponseHolder getSyncReponse(ProfileUpdateRequest request) throws GetDeltaException {
                return operationsService.updateProfile(request);
            }
        }, message.getRequest(), message);
    }

    /**
     * Process endpoint sync.
     * 
     * @param message
     *            the message
     */
    private void processEndpointSync(SyncRequestMessage message) {
        doInTemplate(new SyncReponseCalculator<SyncRequest>() {
            @Override
            public SyncResponseHolder getSyncReponse(SyncRequest request) throws GetDeltaException {
                return operationsService.sync(request);
            }
        }, message.getRequest(), message);
    }

    /**
     * Do in template.
     * 
     * @param <T>
     *            the generic type
     * @param calcualtor
     *            the calcualtor
     * @param request
     *            the request
     * @param message
     *            the message
     * @return true, if successful
     */
    private <T extends SpecificRecordBase> boolean doInTemplate(SyncReponseCalculator<T> calcualtor, T request, EndpointAwareMessage message) {
        return doInTemplate(calcualtor, request, message, false);
    }

    /**
     * Do in template.
     * 
     * @param <T>
     *            the generic type
     * @param calcualtor
     *            the calcualtor
     * @param request
     *            the request
     * @param message
     *            the message
     * @param canPostpone
     *            the can postpone
     * @return true, if successful
     */
    private <T extends SpecificRecordBase> boolean doInTemplate(SyncReponseCalculator<T> calcualtor, T request, EndpointAwareMessage message,
            boolean canPostpone) {
        try {
            LOG.debug("[{}] Processing request from endpoint {}", endpointActorKey, Base64Util.encode(message.getKey().getData()));
            long start = System.currentTimeMillis();
            SyncResponseHolder responseHolder = calcualtor.getSyncReponse(request);
            this.syncTime += System.currentTimeMillis() - start;
            LOG.debug("[{}] SyncResponseHolder {}", endpointActorKey, responseHolder);
            if (canPostpone) {
                if (!requireImmediateReply(responseHolder.getResponse())) {
                    LOG.debug("[{}] reply postponed till request timeout or state change", endpointActorKey);
                    pendingResponse = responseHolder;
                    return false;
                } else {
                    LOG.debug("[{}] Response require immediate reply. Sending reply now!", endpointActorKey);
                    sendReply(message, responseHolder.getResponse());
                    return true;
                }
            } else {
                LOG.debug("[{}] Request require immediate reply. Sending reply now!", endpointActorKey);
                sendReply(message, responseHolder.getResponse());
                return true;
            }
        } catch (GetDeltaException e) {
            SyncResponseMessage responseMessage = new SyncResponseMessage(message.getAppToken(), message.getKey(), null, e);
            LOG.error("[{}] processEndpointRequest", endpointActorKey, e);
            sendReply(message.getOriginator(), message, responseMessage);
            return true;
        }
    }

    /**
     * Send reply.
     * 
     * @param pendingRequest
     *            the pending request
     * @param syncResponse
     *            the sync response
     */
    private void sendReply(EndpointAwareMessage pendingRequest, SyncResponse syncResponse) {
        SyncResponseMessage responseMessage = new SyncResponseMessage(pendingRequest.getAppToken(), pendingRequest.getKey(), syncResponse, null, this.syncTime);
        sendReply(pendingRequest.getOriginator(), pendingRequest, responseMessage);
    }

    /**
     * Send reply.
     * 
     * @param targetActor
     *            the target actor
     * @param requestMessage
     *            the request message            
     * @param responseMessage
     *            the response message
     */
    private void sendReply(ActorRef targetActor, EndpointAwareMessage requestMessage, SyncResponseMessage responseMessage) {
        targetActor.tell(responseMessage, self());
        context().parent().tell(new RequestProcessedMessage(endpointActorKey, ActorMetaData.getHash(requestMessage)), self());
        this.pendingRequest = null;
        this.pendingResponse = null;
    }

    /**
     * Schedule timeout message.
     * 
     * @param message
     *            the message
     */
    private void scheduleTimeoutMessage(SessionMessage message) {
        SessionAttributes attr = message.getSessionAttributes();
        long delay = attr.getTimeout() - (System.currentTimeMillis() - attr.getStart());
        scheduleTimeoutMessage(delay);
    }
    
    /**
     * Schedule timeout message.
     * 
     * @param delay
     *            the delay
     */
    private void scheduleTimeoutMessage(long delay) {
        context().system().scheduler()
                .scheduleOnce(Duration.create(delay, TimeUnit.MILLISECONDS), self(), new RequestTimeoutMessage(), context().dispatcher(), self());
    }    

    /**
     * The Interface SyncReponseCalculator.
     * 
     * @param <T>
     *            the generic type
     */
    private static interface SyncReponseCalculator<T extends SpecificRecordBase> {

        /**
         * Gets the sync reponse.
         * 
         * @param request
         *            the request
         * @return the sync reponse
         * @throws GetDeltaException
         *             the get delta exception
         */
        SyncResponseHolder getSyncReponse(T request) throws GetDeltaException;
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.debug("[{}] Starting", endpointActorKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.debug("[{}] Stoped", endpointActorKey);
    }
}
