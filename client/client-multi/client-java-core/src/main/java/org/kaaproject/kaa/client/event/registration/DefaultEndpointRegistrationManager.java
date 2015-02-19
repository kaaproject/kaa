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

package org.kaaproject.kaa.client.event.registration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link EndpointRegistrationManager} implementation.
 *
 * @author Taras Lemkin
 *
 */
public class DefaultEndpointRegistrationManager implements EndpointRegistrationManager, EndpointRegistrationProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEndpointRegistrationManager.class);
    private static final Random RANDOM = new Random();

    private final KaaClientState state;
    private final Map<Integer, OnAttachEndpointOperationCallback> endpointAttachListeners = new ConcurrentHashMap<>();
    private final Map<Integer, OnDetachEndpointOperationCallback> endpointDetachListeners = new ConcurrentHashMap<>();

    private final Map<Integer, EndpointAccessToken> attachEndpointRequests = new ConcurrentHashMap<>();
    private final Map<Integer, EndpointKeyHash> detachEndpointRequests = new ConcurrentHashMap<>();

    private UserAttachRequest userAttachRequest;
    private UserAttachCallback userAttachCallback;

    private AttachEndpointToUserCallback attachEndpointToUserCallback;
    private DetachEndpointFromUserCallback detachEndpointFromUserCallback;

    private volatile UserTransport userTransport;
    private volatile ProfileTransport profileTransport;

    public DefaultEndpointRegistrationManager(KaaClientState state, UserTransport userTransport, ProfileTransport profileTransport) {
        this.userTransport = userTransport;
        this.profileTransport = profileTransport;
        this.state = state;
        String endpointAccessToken = state.getEndpointAccessToken();
        if (endpointAccessToken == null || endpointAccessToken.length() == 0) {
            regenerateEndpointAccessToken();
        }
    }

    private String regenerateEndpointAccessToken() {
        String oldToken = state.getEndpointAccessToken();
        String newEndpointAccessToken = state.refreshEndpointAccessToken();
        LOG.info("New endpoint access token is generated: '{}'", newEndpointAccessToken);
        if (oldToken != null && oldToken.length() > 0) {
            onEndpointAccessTokenChanged(oldToken);
        }
        return newEndpointAccessToken;
    }

    @Override
    public void attachEndpoint(EndpointAccessToken endpointAccessToken, OnAttachEndpointOperationCallback resultListener) {
        int requestId = getRandomInt();
        LOG.info("Going to attach Endpoint by access token: {}", endpointAccessToken);
        attachEndpointRequests.put(requestId, endpointAccessToken);
        if (resultListener != null) {
            endpointAttachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void detachEndpoint(EndpointKeyHash endpointKeyHash, OnDetachEndpointOperationCallback resultListener) {
        int requestId = getRandomInt();
        LOG.info("Going to detach Endpoint by endpoint key hash: {}", endpointKeyHash);
        detachEndpointRequests.put(requestId, endpointKeyHash);
        if (resultListener != null) {
            endpointDetachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void attachUser(String userExternalId, String userAccessToken, UserAttachCallback callback) {
        if (UserVerifierConstants.DEFAULT_USER_VERIFIER_TOKEN != null) {
            attachUser(UserVerifierConstants.DEFAULT_USER_VERIFIER_TOKEN, userExternalId, userAccessToken, callback);
        } else {
            throw new IllegalStateException("Default user verifier was not defined during SDK generation process!");
        }
    }

    @Override
    public void attachUser(String userVerifierToken, String userExternalId, String userAccessToken, UserAttachCallback callback) {
        userAttachRequest = new UserAttachRequest(userVerifierToken, userExternalId, userAccessToken);
        userAttachCallback = callback;
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    public Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointList() {
        return state.getAttachedEndpointsList();
    }

    @Override
    public void onUpdate(List<EndpointAttachResponse> attachResponses,
            List<EndpointDetachResponse> detachResponses,
            UserAttachResponse userResponse,
            UserAttachNotification userAttachNotification,
            UserDetachNotification userDetachNotification) throws IOException {
        if (userResponse != null) {
            if (userAttachCallback != null) {
                userAttachCallback.onAttachResult(userResponse);
                userAttachCallback = null;
            }
            if (userResponse.getResult() == SyncResponseResultType.SUCCESS) {
                state.setAttachedToUser(true);
                if (attachEndpointToUserCallback != null) {
                    attachEndpointToUserCallback.onAttachedToUser(userAttachRequest.getUserExternalId(), state.getEndpointAccessToken());
                }
            }
            userAttachRequest = null;
        }

        if (attachResponses != null && !attachResponses.isEmpty()) {
            for (EndpointAttachResponse attached : attachResponses) {
                notifyAttachedListener(attached.getResult(), endpointAttachListeners.remove(attached.getRequestId())
                        , new EndpointKeyHash(attached.getEndpointKeyHash()));
                attachEndpointRequests.remove(attached.getRequestId());
            }
        }
        if (detachResponses != null && !detachResponses.isEmpty()) {
            for (EndpointDetachResponse detached : detachResponses) {
                notifyDetachedListener(detached.getResult(), endpointDetachListeners.remove(detached.getRequestId()));
                EndpointKeyHash endpointKeyHash = detachEndpointRequests.remove(detached.getRequestId());
                if (endpointKeyHash != null && detached.getResult() == SyncResponseResultType.SUCCESS) {
                    if (endpointKeyHash.equals(state.getEndpointKeyHash())) {
                        state.setAttachedToUser(false);
                    }
                }
            }
        }

        if (userAttachNotification != null) {
            state.setAttachedToUser(true);
            if (attachEndpointToUserCallback != null) {
                attachEndpointToUserCallback.onAttachedToUser(userAttachNotification.getUserExternalId(), userAttachNotification.getEndpointAccessToken());
            }
        }

        if (userDetachNotification != null) {
            state.setAttachedToUser(false);
            if (detachEndpointFromUserCallback != null) {
                detachEndpointFromUserCallback.onDetachedFromUser(userDetachNotification.getEndpointAccessToken());
            }
        }
    }

    private void notifyAttachedListener(SyncResponseResultType result, OnAttachEndpointOperationCallback operationCallback, EndpointKeyHash keyHash) {
        if(operationCallback != null){
            operationCallback.onAttach(result, keyHash);
        }
    }

    private void notifyDetachedListener(SyncResponseResultType result, OnDetachEndpointOperationCallback operationCallback) {
        if(operationCallback != null){
            operationCallback.onDetach(result);
        }
    }

    private void onEndpointAccessTokenChanged(String old) {
        if (profileTransport != null) {
            profileTransport.sync();
        }
        Map<EndpointAccessToken, EndpointKeyHash> container = getAttachedEndpointList();
        EndpointKeyHash hash = container.get(old);
        if (hash != null) {
            detachEndpoint(hash, null);
        }
    }

    @Override
    public Map<Integer, EndpointAccessToken> getAttachEndpointRequests() {
        Map<Integer, EndpointAccessToken> result = new HashMap<>();
        synchronized (attachEndpointRequests) {
            result.putAll(attachEndpointRequests);
        }
        return result;
    }

    @Override
    public Map<Integer, EndpointKeyHash> getDetachEndpointRequests() {
        Map<Integer, EndpointKeyHash> result = new HashMap<>();
        synchronized (detachEndpointRequests) {
            result.putAll(detachEndpointRequests);
        }
        return result;
    }

    @Override
    public UserAttachRequest getUserAttachRequest() {
        return userAttachRequest;
    }

    @Override
    public boolean isAttachedToUser() {
        return state.isAttachedToUser();
    }

    @Override
    public void setAttachedCallback(AttachEndpointToUserCallback listener) {
        this.attachEndpointToUserCallback = listener;
    }

    @Override
    public void setDetachedCallback(DetachEndpointFromUserCallback listener) {
        this.detachEndpointFromUserCallback = listener;
    }

    private synchronized int getRandomInt() {
        return RANDOM.nextInt();
    }
}
