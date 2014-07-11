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

package org.kaaproject.kaa.server.operations.service.event;

import java.security.PublicKey;
import java.util.List;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestOperationsService implements OperationsService {

    @Override
    public void setPublicKey(PublicKey publicKey) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.OperationsService#sync(org.kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(SyncRequest request) throws GetDeltaException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyncResponse updateSyncResponse(SyncResponse response, List<NotificationDto> notifications, String unicastNotificationId) {
        // TODO Auto-generated method stub
        return null;
    }


}
