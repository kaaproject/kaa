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

package org.kaaproject.kaa.server.operations.service;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.SyncContext;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.security.PublicKeyAware;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.UserClientSync;

/**
 * The interface OperationsService is used to define key operations with
 * Endpoint Node. One can register, update and sync endpoint state.
 *
 * @author Andrew Shvayka
 */
public interface OperationsService extends PublicKeyAware {

    SyncContext syncProfile(SyncContext context, ProfileClientSync request);

    SyncContext processEndpointAttachDetachRequests(SyncContext context, UserClientSync request);

    SyncContext processEventListenerRequests(SyncContext context, EventClientSync request);

    SyncContext syncConfiguration(SyncContext context, ConfigurationClientSync request) throws GetDeltaException;

    SyncContext syncNotification(SyncContext context, NotificationClientSync request);
    
    EndpointProfileDto updateProfile(SyncContext context);

    /**
     * Attaches endpoint to user.
     *
     * @param profile
     *            the endpoint profile
     * @param appToken
     *            the application token
     * @param userExternalId
     *            the user external id
     * @return the updated endpoint profile
     */
    EndpointProfileDto attachEndpointToUser(EndpointProfileDto profile, String appToken, String userExternalId);

    /**
     * Update sync response.
     *
     * @param response
     *            the response
     * @param notifications
     *            the notifications
     * @param unicastNotificationId
     *            the unicast notification id
     * @return the sync response
     */
    ServerSync updateSyncResponse(ServerSync response, List<NotificationDto> notifications, String unicastNotificationId);

    /**
     * Lookup user configuration and return it's hash
     * @param appToken application token
     * @param profile endpoint profile
     * @return user configuration hash, or null if not found;
     */
    byte[] fetchUcfHash(String appToken, EndpointProfileDto profile);

    /**
     * Fetch server endpoint profile and CTL schema id based on endpoint key hash
     * 
     * @param hash - endpoint key hash
     * @return endpoint profile
     */
    public EndpointProfileDto refreshServerEndpointProfile(EndpointObjectHash hash);

}
