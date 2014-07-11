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
 * Manager which is responsible for Endpoints attach/detach.
 *
 * @author Taras Lemkin
 *
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
     * Adds new endpoint attach request
     *
     * @param endpointAccessToken Access token of the attaching endpoint
     * @param resultListener Listener to notify about result of the endpoint attaching
     *
     * @see EndpointAccessToken
     * @see EndpointOperationResultListener
     */
    void attachEndpoint(EndpointAccessToken endpointAccessToken, EndpointOperationResultListener resultListener);

    /**
     * Adds new endpoint detach request
     *
     * @param endpointKeyHash Key hash of the detaching endpoint
     * @param resultListener Listener to notify about result of the enpoint attaching
     *
     * @see EndpointKeyHash
     * @see EndpointOperationResultListener
     */
    void detachEndpoint(EndpointKeyHash endpointKeyHash, EndpointOperationResultListener resultListener);

    /**
     * Creates user attach request
     *
     * @param userExternalId
     * @param userAccessToken
     * @param callback called when authentication result received
     *
     * @see UserAuthResultListener
     */
    void attachUser(String userExternalId, String userAccessToken, UserAuthResultListener callback);

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
     * Adds listener for attached endpoint list updates
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
}
