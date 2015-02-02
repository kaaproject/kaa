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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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

    private final KaaClientState state;
    private final Set<AttachedEndpointListChangedListener> attachedListChangedListeners = new HashSet<AttachedEndpointListChangedListener>();
    private final Map<Integer, EndpointOperationResultListener> endpointAttachListeners = new HashMap<Integer, EndpointOperationResultListener>();
    private final Map<Integer, EndpointOperationResultListener> endpointDetachListeners = new HashMap<Integer, EndpointOperationResultListener>();

    private final Map<Integer, EndpointAccessToken> attachEndpointRequests = new HashMap<Integer, EndpointAccessToken>();
    private final Map<Integer, EndpointKeyHash> detachEndpointRequests = new HashMap<Integer, EndpointKeyHash>();

    private UserAttachRequest userAttachRequest;
    private UserAuthResultListener userAuthResultListener;

    private CurrentEndpointAttachListener currentEndpointAttachListener;
    private CurrentEndpointDetachListener currentEndpointDetachListener;

    private volatile UserTransport userTransport;
    private volatile ProfileTransport profileTransport;

    private String endpointAccessToken = new String();

    public DefaultEndpointRegistrationManager(KaaClientState state, UserTransport userTransport, ProfileTransport profileTransport) {
        this.state = state;
        this.endpointAccessToken = state.getEndpointAccessToken();

        if (endpointAccessToken.isEmpty()) {
            regenerateEndpointAccessToken();
        }
        this.userTransport = userTransport;
        this.profileTransport = profileTransport;
    }

    @Override
    public void regenerateEndpointAccessToken() {
        boolean isRegenerated = !endpointAccessToken.isEmpty();
        String oldToken = endpointAccessToken;

        endpointAccessToken = UUID.randomUUID().toString();

        state.setEndpointAccessToken(endpointAccessToken);

        LOG.info("New endpoint access token is generated: '{}'", endpointAccessToken);

        if (isRegenerated) {
            onEndpointAccessTokenChanged(oldToken);
        }
    }

    @Override
    public String getEndpointAccessToken() {
        return endpointAccessToken;
    }

    @Override
    public void attachEndpoint(EndpointAccessToken endpointAccessToken, EndpointOperationResultListener resultListener) {
        int requestId;
        synchronized (attachEndpointRequests) {
            requestId = new Random().nextInt();
            LOG.info("Going to attach Endpoint by access token: {}", endpointAccessToken);
            attachEndpointRequests.put(requestId, endpointAccessToken);
        }
        if (resultListener != null) {
            endpointAttachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void detachEndpoint(EndpointKeyHash endpointKeyHash, EndpointOperationResultListener resultListener) {
        int requestId;
        synchronized (detachEndpointRequests) {
            requestId = new Random().nextInt();
            LOG.info("Going to detach Endpoint by endpoint key hash: {}", endpointKeyHash);
            detachEndpointRequests.put(requestId, endpointKeyHash);
        }
        if (resultListener != null) {
            endpointDetachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void attachUser(String userExternalId, String userAccessToken, UserAuthResultListener callback) {
        //TODO: make this configurable
        userAttachRequest = new UserAttachRequest("VERIFIER_ID", userExternalId, userAccessToken);
        userAuthResultListener = callback;
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointList() {
        return state.getAttachedEndpointsList();
    }

    @Override
    public void addAttachedEndpointListChangeListener(AttachedEndpointListChangedListener listener) {
        synchronized (attachedListChangedListeners) {
            attachedListChangedListeners.add(listener);
        }
    }

    @Override
    public void removeAttachedEndpointListChangeListener(AttachedEndpointListChangedListener listener) {
        synchronized (attachedListChangedListeners) {
            attachedListChangedListeners.remove(listener);
        }
    }

    @Override
    public void onUpdate(List<EndpointAttachResponse> attachResponses,
            List<EndpointDetachResponse> detachResponses,
            UserAttachResponse userResponse,
            UserAttachNotification userAttachNotification,
            UserDetachNotification userDetachNotification) throws IOException {
        if (userResponse != null) {
            if (userAuthResultListener != null) {
                userAuthResultListener.onAuthResult(userResponse);
                userAuthResultListener = null;
            }
            if (userResponse.getResult() == SyncResponseResultType.SUCCESS) {
                state.setAttachedToUser(true);
                if (currentEndpointAttachListener != null) {
                    currentEndpointAttachListener.onAttachedToUser(userAttachRequest.getUserExternalId(), endpointAccessToken);
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
        if ((attachResponses != null && !attachResponses.isEmpty())
                || (detachResponses != null && !detachResponses.isEmpty())) {
            notifyListeners();
        }

        if (userAttachNotification != null) {
            state.setAttachedToUser(true);
            if (currentEndpointAttachListener != null) {
                currentEndpointAttachListener.onAttachedToUser(userAttachNotification.getUserExternalId(), userAttachNotification.getEndpointAccessToken());
            }
        }

        if (userDetachNotification != null) {
            state.setAttachedToUser(false);
            if (currentEndpointDetachListener != null) {
                currentEndpointDetachListener.onDetachedFromUser(userDetachNotification.getEndpointAccessToken());
            }
        }
    }

    private void notifyAttachedListener(org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType result,
            EndpointOperationResultListener resultListener, EndpointKeyHash keyHash) {
        if(resultListener != null){
            resultListener.sendResponse("endpoint attached", result, keyHash);
        }
    }

    private void notifyDetachedListener(org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType result,
            EndpointOperationResultListener resultListener) {
        if(resultListener != null){
            resultListener.sendResponse("endpoint detached", result, null);
        }
    }

    private void notifyListeners() {
        synchronized (attachedListChangedListeners) {
            for (AttachedEndpointListChangedListener listener : attachedListChangedListeners) {
                listener.onAttachedEndpointListChanged(getAttachedEndpointList());
            }
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
    public void setUserTransport(UserTransport transport) {
        this.userTransport = transport;
    }

    @Override
    public void setProfileTransport(ProfileTransport transport) {
        this.profileTransport = transport;
    }

    @Override
    public boolean isAttachedToUser() {
        return state.isAttachedToUser();
    }

    @Override
    public void setAttachedListener(CurrentEndpointAttachListener listener) {
        currentEndpointAttachListener = listener;
    }

    @Override
    public void setDetachedListener(CurrentEndpointDetachListener listener) {
        currentEndpointDetachListener = listener;
    }

}
