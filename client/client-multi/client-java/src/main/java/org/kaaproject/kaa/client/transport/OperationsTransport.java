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

package org.kaaproject.kaa.client.transport;

import java.io.Closeable;

import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

/**
 * Interface for communication with operations server.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface OperationsTransport extends Closeable{

    /**
     * Sends the register request.
     *
     * @param request registration request which is going to be sent.
     * @return response from the server.
     *
     */
    SyncResponse sendRegisterCommand(EndpointRegistrationRequest request) throws TransportException;

    /**
     * Sends the profile update request.
     *
     * @param request request with the updated profile which is going to be sent.
     * @return response from the server.
     *
     */
    SyncResponse sendUpdateCommand(ProfileUpdateRequest request) throws TransportException;

    /**
     * Sends the sync request.
     *
     * @param request sync request which is going to be sent.
     * @return response from the server.
     *
     */
    SyncResponse sendSyncRequest(SyncRequest request) throws TransportException;

    /**
     * Sends the long sync request.
     *
     * @param request sync request which is going to be sent.
     * @return response from the server.
     *
     */
    SyncResponse sendLongSyncRequest(LongSyncRequest request) throws TransportException;

    /**
     * Abort current request.
     */
    void abortRequest();
}
