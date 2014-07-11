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
import java.util.Set;
import java.util.UUID;

import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
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
    private final Map<String, EndpointOperationResultListener> endpointAttachListeners = new HashMap<String, EndpointOperationResultListener>();
    private final Map<String, EndpointOperationResultListener> endpointDetachListeners = new HashMap<String, EndpointOperationResultListener>();

    private final Map<String, EndpointAccessToken> attachEndpointRequests = new HashMap<String, EndpointAccessToken>();
    private final Map<String, EndpointKeyHash> detachEndpointRequests = new HashMap<String, EndpointKeyHash>();

    private UserAttachRequest userAttachRequest;
    private UserAuthResultListener userAuthResultListener;

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
        String requestId;
        synchronized (attachEndpointRequests) {
            requestId = UUID.nameUUIDFromBytes(endpointAccessToken.getToken().getBytes()).toString();
            LOG.info("Going to attach Endpoint by access token: {}", endpointAccessToken);
            attachEndpointRequests.put(requestId, endpointAccessToken);
        }
        if (resultListener != null && requestId != null) {
            endpointAttachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void detachEndpoint(EndpointKeyHash endpointKeyHash, EndpointOperationResultListener resultListener) {
        String requestId;
        synchronized (detachEndpointRequests) {
            requestId = UUID.nameUUIDFromBytes(endpointKeyHash.getKeyHash().getBytes()).toString();
            LOG.info("Going to detach Endpoint by endpoint key hash: {}", endpointKeyHash);
            detachEndpointRequests.put(requestId, endpointKeyHash);
        }
        if (resultListener != null && requestId != null) {
            endpointDetachListeners.put(requestId, resultListener);
        }
        if (userTransport != null) {
            userTransport.sync();
        }
    }

    @Override
    public void attachUser(String userExternalId, String userAccessToken, UserAuthResultListener callback) {
        userAttachRequest = new UserAttachRequest(userExternalId, userAccessToken);
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
            UserAttachResponse userResponse) throws IOException {
        if (userResponse != null) {
            if (userAuthResultListener != null) {
                userAuthResultListener.onAuthResult(userResponse);
                userAuthResultListener = null;
            }
            userAttachRequest = null;
        }

        if (attachResponses != null && !attachResponses.isEmpty()) {
            for (EndpointAttachResponse attached : attachResponses) {
                notifyAttachedListener(attached.getResult(), endpointAttachListeners.remove(attached.getRequestId()));
                attachEndpointRequests.remove(attached.getRequestId());
            }
        }
        if (detachResponses != null && !detachResponses.isEmpty()) {
            for (EndpointDetachResponse detached : detachResponses) {
                notifyDetachedListener(detached.getResult(), endpointDetachListeners.remove(detached.getRequestId()));
                detachEndpointRequests.remove(detached.getRequestId());
            }
        }
        if ((attachResponses != null && !attachResponses.isEmpty())
                || (detachResponses != null && !detachResponses.isEmpty())) {
            notifyListeners();
        }
    }

    private void notifyAttachedListener(org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType result,
            EndpointOperationResultListener resultListener) {
        if(resultListener != null){
            resultListener.sendResponse("endpoint attached", result);
        }
    }

    private void notifyDetachedListener(org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType result,
            EndpointOperationResultListener resultListener) {
        if(resultListener != null){
            resultListener.sendResponse("endpoint detached", result);
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
    public Map<String, EndpointAccessToken> getAttachEndpointRequests() {
        Map<String, EndpointAccessToken> result = new HashMap<>();
        synchronized (attachEndpointRequests) {
            result.putAll(attachEndpointRequests);
        }
        return result;
    }

    @Override
    public Map<String, EndpointKeyHash> getDetachEndpointRequests() {
        Map<String, EndpointKeyHash> result = new HashMap<>();
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

}
