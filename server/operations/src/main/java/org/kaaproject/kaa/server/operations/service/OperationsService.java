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
import org.kaaproject.kaa.common.endpoint.protocol.ClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.security.PublicKeyAware;


/**
 * The interface OperationsService is used to define key operations with Endpoint Node.
 * One can register, update and sync endpoint state.
 *
 * @author ashvayka
 */
public interface OperationsService extends PublicKeyAware{

    /**
     * Sync endpoint state.
     *
     * @param request the request
     * @return the sync response
     * @throws GetDeltaException the get delta exception
     */
    SyncResponseHolder sync(ClientSync request) throws GetDeltaException;

    /**
     * Sync endpoint state.
     *
     * @param request the request
     * @return the sync response
     * @throws GetDeltaException the get delta exception
     */
    SyncResponseHolder sync(ClientSync request, EndpointProfileDto profile) throws GetDeltaException;    
    
    /**
     * Update sync response.
     *
     * @param response the response
     * @param notifications the notifications
     * @param unicastNotificationId the unicast notification id
     * @return the sync response
     */
    ServerSync updateSyncResponse(ServerSync response, List<NotificationDto> notifications, String unicastNotificationId);
}
