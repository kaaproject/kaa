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

package org.kaaproject.kaa.client.notification;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The listener of raw notifications' data.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface NotificationListener {

    /**
     * Called on each new notification.
     *
     * @param topicId the topic's id.
     * @param notification the raw notification's data.
     *
     */
    void onNotificationRaw(String topicId, ByteBuffer notification) throws IOException;

}
