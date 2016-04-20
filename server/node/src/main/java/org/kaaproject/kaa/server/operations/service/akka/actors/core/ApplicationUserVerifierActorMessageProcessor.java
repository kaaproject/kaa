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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.verifier.UserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode;
import org.kaaproject.kaa.server.common.verifier.UserVerifierLifecycleException;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationResponseMessage;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class ApplicationUserVerifierActorMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUserVerifierActorMessageProcessor.class);

    private final EndpointUserService endpointUserService;

    private final String applicationId;

    /** The log appenders */
    private Map<String, UserVerifier> userVerifiers;

    ApplicationUserVerifierActorMessageProcessor(EndpointUserService endpointUserService, String applicationId) {
        this.applicationId = applicationId;
        this.endpointUserService = endpointUserService;
        initUserVerifiers();
    }

    private void initUserVerifiers() {
        this.userVerifiers = new HashMap<String, UserVerifier>();
        for (UserVerifierDto dto : endpointUserService.findUserVerifiers(applicationId)) {
            try {
                LOG.trace("Initializing user verifier for {}", dto);
                UserVerifier verifier = createUserVerifier(dto);
                LOG.trace("Registering user verifier with token {}", dto.getVerifierToken());
                userVerifiers.put(dto.getVerifierToken(), verifier);
            } catch (Exception e) {
                LOG.error("Failed to create user verifier", e);
            }
        }
    }

    private UserVerifier createUserVerifier(UserVerifierDto verifierDto) throws Exception {
        if (verifierDto == null) {
            throw new IllegalArgumentException("verifier dto can't be null");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<UserVerifier> verifierClass = (Class<UserVerifier>) Class.forName(verifierDto.getPluginClassName());
            UserVerifier userVerifier = verifierClass.newInstance();
            userVerifier.init(new UserVerifierContext(verifierDto));
            userVerifier.start();
            return userVerifier;
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to find custom verifier class {}", verifierDto.getPluginClassName());
            throw e;
        } catch (InstantiationException | IllegalAccessException | UserVerifierLifecycleException e) {
            LOG.error("Unable to instantiate custom verifier from class {}", verifierDto.getPluginClassName());
            throw e;
        }
    }

    public void verifyUser(UserVerificationRequestMessage message) {
        UserVerifier verifier = userVerifiers.get(message.getVerifierId());
        if (verifier != null) {
            verifier.checkAccessToken(message.getUserId(), message.getAccessToken(), new DefaultVerifierCallback(message));
        } else {
            LOG.debug("Failed to find verifier with token {}", message.getVerifierId());
            message.getOriginator().tell(UserVerificationResponseMessage.failure(message.getRequestid(), message.getUserId(), UserVerifierErrorCode.NO_VERIFIER_CONFIGURED),
                    ActorRef.noSender());
        }
    }

    public void processNotification(Notification notification) {
        LOG.debug("Process user verifier notification [{}]", notification);
        String verifierToken = notification.getUserVerifierToken();
        switch (notification.getOp()) {
        case ADD_USER_VERIFIER:
            addUserVerifier(verifierToken);
            break;
        case REMOVE_USER_VERIFIER:
            removeUserVerifier(verifierToken);
            break;
        case UPDATE_USER_VERIFIER:
            removeUserVerifier(verifierToken);
            addUserVerifier(verifierToken);
            break;
        default:
            LOG.debug("[{}][{}] Operation [{}] is not supported.", applicationId, verifierToken, notification.getOp());
        }
    }

    private void addUserVerifier(String verifierToken) {
        LOG.info("[{}] Adding user verifier with token [{}].", applicationId, verifierToken);
        if (!userVerifiers.containsKey(verifierToken)) {
            UserVerifierDto verifierDto = endpointUserService.findUserVerifier(applicationId, verifierToken);
            if (verifierDto != null) {
                try {
                    userVerifiers.put(verifierToken, createUserVerifier(verifierDto));
                    LOG.info("[{}] user verifier [{}] registered.", applicationId, verifierToken);
                } catch (Exception e) {
                    LOG.error("Failed to create user verifier", e);
                }
            }
        } else {
            LOG.info("[{}] User verifier [{}] is already registered.", applicationId, verifierToken);
        }
    }

    private void removeUserVerifier(String verifierToken) {
        if (userVerifiers.containsKey(verifierToken)) {
            LOG.info("[{}] Stopping user verifier with token [{}].", applicationId, verifierToken);
            userVerifiers.remove(verifierToken).stop();
        } else {
            LOG.warn("[{}] Can't remove unregistered user verifier with token [{}]", applicationId, verifierToken);
        }
    }

    void preStart() {
        // Do nothing
    };

    void postStop() {
        for (Entry<String, UserVerifier> verifier : userVerifiers.entrySet()) {
            LOG.info("[{}] Stopping user verifier with id [{}].", applicationId, verifier.getKey());
            verifier.getValue().stop();
        }
    }

    private static class DefaultVerifierCallback implements UserVerifierCallback {
        
        private final ActorRef endpointActor;
        private final UUID requestId;
        private final String userId;

        public DefaultVerifierCallback(UserVerificationRequestMessage message) {
            super();
            this.endpointActor = message.getOriginator();
            this.requestId = message.getRequestid();
            this.userId = message.getUserId();
        }
        
        private void tellFailure(UserVerifierErrorCode errorCode){
            tellFailure(errorCode, null);
        }
        
        private void tellFailure(UserVerifierErrorCode errorCode, String reason){
            endpointActor.tell(UserVerificationResponseMessage.failure(requestId, reason, errorCode, reason), ActorRef.noSender());
        }

        @Override
        public void onSuccess() {
            endpointActor.tell(UserVerificationResponseMessage.success(requestId, userId), ActorRef.noSender());
        }

        @Override
        public void onTokenInvalid() {
            tellFailure(UserVerifierErrorCode.TOKEN_INVALID);
        }

        @Override
        public void onTokenExpired() {
            tellFailure(UserVerifierErrorCode.TOKEN_EXPIRED);
        }

        @Override
        public void onVerificationFailure(String reason) {
            tellFailure(UserVerifierErrorCode.OTHER, reason);
        }

        @Override
        public void onInternalError() {
            tellFailure(UserVerifierErrorCode.INTERNAL_ERROR);
        }

        @Override
        public void onInternalError(String reason) {
            tellFailure(UserVerifierErrorCode.INTERNAL_ERROR, reason);
        }

        @Override
        public void onConnectionError() {
            tellFailure(UserVerifierErrorCode.CONNECTION_ERROR);
        }

        @Override
        public void onConnectionError(String reason) {
            tellFailure(UserVerifierErrorCode.CONNECTION_ERROR, reason);
        }

        @Override
        public void onRemoteError() {
            tellFailure(UserVerifierErrorCode.REMOTE_ERROR);
        }

        @Override
        public void onRemoteError(String reason) {
            tellFailure(UserVerifierErrorCode.REMOTE_ERROR, reason);
        }
    }
}
