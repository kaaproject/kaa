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

import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;

/**
 * {@link KaaTransport} for the Notification service.
 * Updates the Notification manager state.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface NotificationTransport extends KaaTransport {

    /**
     * Creates a new Notification request.
     *
     * @return new Notification request.
     * @see NotificationSyncRequest
     *
     */
    NotificationSyncRequest createNotificationRequest();

    /**
     * Updates the state of the Notification manager according to the given response.
     *
     * @param response the response from the server.
     * @see NotificationSyncResponse
     *
     */
    void onNotificationResponse(NotificationSyncResponse response) throws Exception;

    /**
     * Sets the given Notification processor.
     *
     * @param processor the Notification processor which to be set.
     * @see NotificationProcessor
     *
     */
    void setNotificationProcessor(NotificationProcessor processor);

    /**
     * Sets the given Notification manager.
     *
     * @param manager the Notification manager to be set.
     * @see NotificationManager
     *
     */
    void setNotificationManager(NotificationManager manager);

}
