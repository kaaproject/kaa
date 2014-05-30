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

package org.kaaproject.kaa.client.update.listeners;

import java.io.IOException;
import java.util.List;

import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.client.update.UpdateListener;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

/**
 * Notification listener ({@link UpdateListener}.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class NotificationUpdateListener implements UpdateListener {
    private final NotificationProcessor processor;

    public NotificationUpdateListener(NotificationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void onDeltaUpdate(SyncResponse response) throws IOException {
        NotificationSyncResponse notificationSync = response.getNotificationSyncResponse();
        if (notificationSync != null) {
            List<Topic> topics = notificationSync.getAvailableTopics();
            if (topics != null) {
                processor.topicsListUpdated(topics);
            }
            List<Notification> notifications = notificationSync.getNotifications();
            if (notifications != null) {
                processor.notificationReceived(notifications);
            }
        }
    }

}
