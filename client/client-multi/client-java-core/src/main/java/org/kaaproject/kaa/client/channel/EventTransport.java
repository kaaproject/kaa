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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;

/**
 * {@link KaaTransport} for the Event service.
 * It is responsible for updating the Event manager state.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface EventTransport extends KaaTransport {

    /**
     * Creates the Event request.
     *
     * @return new Event request.
     *
     */
    EventSyncRequest createEventRequest();

    /**
     * Updates the state of the Event manager from the given response.
     *
     * @param response the response from the server.
     *
     */
    void onEventResponse(EventSyncResponse response);

    /**
     * Sets the given Event manager to the current transport.
     *
     * @param manager the Event manager which is going to be set.
     *
     */
    void setEventManager(EventManager manager);

}
