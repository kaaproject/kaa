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
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class NotificationUpdateListenerTest {

    @Test
    public void testNotificationUpdateListener() throws IOException {
        NotificationProcessor processor = mock(NotificationProcessor.class);
        NotificationUpdateListener listener = new NotificationUpdateListener(processor);

        SyncResponse response = new SyncResponse();
        listener.onDeltaUpdate(response);

        NotificationSyncResponse notificationResponse = new NotificationSyncResponse();
        response.setNotificationSyncResponse(notificationResponse);
        response.setResponseType(SyncResponseStatus.PROFILE_RESYNC);
        listener.onDeltaUpdate(response);

        notificationResponse.setAvailableTopics(new LinkedList<Topic>());
        notificationResponse.setNotifications(new LinkedList<Notification>());
        listener.onDeltaUpdate(response);

        verify(processor, times(1)).notificationReceived(any(List.class));
        verify(processor, times(1)).topicsListUpdated(any(List.class));
    }

}
