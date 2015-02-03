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

import java.util.Map;

import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;

/**
 * Module that manages endpoint-initiated attaching and detaching endpoints
 * to (from) user.<br>
 * <br>
 * To assign endpoints to user current endpoint has to be already attached,
 * otherwise attach/detach operations will fail.<br>
 * Current endpoint can be attached to user in two ways:
 * <il>
 * <li>By calling {@link #attachUser(String, String, UserAuthResultListener)}</li>
 * <li>Attached from another endpoint</li>
 * </il>
 * <br>
 * Attaching current endpoint to user:
 * <pre>
 * {@code
 * EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();
 * registrationManager.attachUser("userExternalId", "userAccessToken", new UserAuthResultListener() { ... });
 * }
 * </pre>
 * To check if this endpoint is attached to user call {@link #isAttachedToUser()}.
 * Only attached endpoints can send/receive events.<br>
 * <br>
 * Attaching any endpoint to user by its access token:
 * <pre>
 * {@code
 * EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();
 * registrationManager.attachEndpoint(new EndpointAccessToken("accessToken"), new EndpointOperationResultListener() {...});
 * }
 * </pre>
 * Detaching endpoint from user by its {@link EndpointKeyHash}:
 * <pre>
 * {@code
 * EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();
 * registrationManager.detachEndpoint((new EndpointKeyHash("keyHash"), new EndpointOperationResultListener() {...});
 * }
 * </pre>
 * EndpointKeyHash for endpoint can be received with AttachEndpoint operation
 * provided from Operations server. See {@link EndpointOperationResultListener}. <br>
 * <br>
 * If current endpoint is assumed to be attached or detached by another endpoint,
 * specific {@link CurrentEndpointAttachListener} and {@link CurrentEndpointDetachListener}
 * may be specified to receive notification about such event.<br>
 * <br>
 * Manager uses specific {@link UserTransport} to communicate with Operations
 * server in scope of basic functionality and {@link ProfileTransport} when its
 * access token is changed.
 *
 * @author Taras Lemkin
 *
 * @see EndpointAccessToken
 * @see EndpointKeyHash
 * @see EndpointOperationResultListener
 * @see UserAuthResultListener
 * @see AttachedEndpointListChangedListener
 * @see CurrentEndpointAttachListener
 * @see CurrentEndpointDetachListener
 * @see UserTransport
 * @see ProfileTransport
 */
public interface EndpointRegistrationManager {

    /**
     * Generate new access token for a current endpoint
     */
    void regenerateEndpointAccessToken();

    /**
     * Retrieve an access token for a current endpoint
     */
    String getEndpointAccessToken();

    /**
     * Updates with new endpoint attach request<br>
     * <br>
     * {@link EndpointOperationResultListener} is populated with {@link EndpointKeyHash} of an
     * attached endpoint.
     *
     * @param endpointAccessToken Access token of the attaching endpoint
     * @param resultListener Listener to notify about result of the endpoint attaching
     *
     * @see EndpointAccessToken
     * @see EndpointOperationResultListener
     */
    void attachEndpoint(EndpointAccessToken endpointAccessToken, EndpointOperationResultListener resultListener);

    /**
     * Updates with new endpoint detach request
     *
     * @param endpointKeyHash Key hash of the detaching endpoint
     * @param resultListener Listener to notify about result of the enpoint attaching
     *
     * @see EndpointKeyHash
     * @see EndpointOperationResultListener
     */
    void detachEndpoint(EndpointKeyHash endpointKeyHash, EndpointOperationResultListener resultListener);

    /**
     * Creates user attach request using default verifier. Default verifier is selected during SDK generation.
     * If there was no default verifier selected this method will throw runtime exception. 
     *
     * @param userExternalId
     * @param userAccessToken
     * @param callback called when authentication result received
     *
     * @see UserAuthResultListener
     */
    void attachUser(String userExternalId, String userAccessToken, UserAuthResultListener callback);
    
    /**
     * Creates user attach request using specified verifier.
     *
     * @param userVerifierId
     * @param userExternalId
     * @param userAccessToken
     * @param callback called when authentication result received
     *
     * @see UserAuthResultListener
     */
    void attachUser(String userVerifierToken, String userExternalId, String userAccessToken, UserAuthResultListener callback);

    /**
     * Retrieves list of attached endpoints
     *
     * @return list of enpointKeyHash of attached endpoints mapped by their access tokens.
     *
     * @see EndpointAccessToken
     * @see EndpointKeyHash
     */
    Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointList();

    /**
     * Updates with listener for attached endpoint list updates
     *
     * @param listener Attached endpoints list change listener
     * @see AttachedEndpointListChangedListener
     */
    void addAttachedEndpointListChangeListener(AttachedEndpointListChangedListener listener);

    /**
     * Removes listener of attached endpoint list changes
     *
     * @param listener Attached endpoints list change listener
     * @see AttachedEndpointListChangedListener
     */
    void removeAttachedEndpointListChangeListener(AttachedEndpointListChangedListener listener);

    /**
     * Sets the User transport for the current manager to communicate with remote server.
     *
     * @param transport the User transport object which is going to be set.
     * @see UserTransport
     */
    void setUserTransport(UserTransport transport);

    /**
     * Sets the Profile transport for the current manager to communicate with remote server.
     *
     * @param transport the Profile transport object which is going to be set.
     * @see ProfileTransport
     */
    void setProfileTransport(ProfileTransport transport);

    /**
     * Checks if current endpoint is attached to user.
     *
     * @return true if current endpoint is attached to any user, false otherwise.
     */
    boolean isAttachedToUser();

    /**
     * Sets callback for notifications when current endpoint is attached to user
     *
     * @param listener
     *
     * @see CurrentEndpointAttachListener
     */
    void setAttachedListener(CurrentEndpointAttachListener listener);

    /**
     * Sets callback for notifications when current endpoint is detached from user
     *
     * @param listener
     *
     * @see CurrentEndpointDetachListener
     */
    void setDetachedListener(CurrentEndpointDetachListener listener);
}
