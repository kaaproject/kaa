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

package org.kaaproject.kaa.server.operations.service.event;

import java.security.PublicKey;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.SyncContext;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.UserClientSync;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestOperationsService implements OperationsService {

    @Override
    public void setPublicKey(PublicKey publicKey) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public SyncContext processEndpointAttachDetachRequests(SyncContext context, UserClientSync request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext processEventListenerRequests(SyncContext context, EventClientSync request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext syncConfiguration(SyncContext context, ConfigurationClientSync request) throws GetDeltaException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext syncNotification(SyncContext context, NotificationClientSync request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext syncProfileServerHash(SyncContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndpointProfileDto attachEndpointToUser(EndpointProfileDto profile, String appToken, String userExternalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServerSync updateSyncResponse(ServerSync response, List<NotificationDto> notifications, String unicastNotificationId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] fetchUcfHash(String appToken, EndpointProfileDto profile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndpointProfileDto refreshServerEndpointProfile(EndpointObjectHash hash) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext syncUserConfigurationHash(SyncContext context, byte[] ucfHash) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncContext syncUseConfigurationRawSchema(SyncContext context, boolean useConfigurationRawSchema) {
        return null;
    }

    @Override
    public SyncContext syncClientProfile(SyncContext context, ProfileClientSync request) {
        return null;
    }

    @Override
    public EndpointProfileDto syncServerProfile(String appToken, String endpointKey, EndpointObjectHash key) {
        // TODO Auto-generated method stub
        return null;
    }


}
