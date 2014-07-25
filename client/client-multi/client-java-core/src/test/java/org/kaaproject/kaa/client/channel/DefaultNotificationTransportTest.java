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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultNotificationTransport;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.mockito.Mockito;

public class DefaultNotificationTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        NotificationTransport transport = new DefaultNotificationTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.NOTIFICATION)).thenReturn(channel);

        NotificationTransport transport = new DefaultNotificationTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channel, Mockito.times(1)).sync(TransportType.NOTIFICATION);
    }

    @Test
    public void testCreateRequest() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(clientState.getNotificationSeqNumber()).thenReturn(new Integer(5));

        NotificationTransport transport = new DefaultNotificationTransport();
        transport.createNotificationRequest();
        transport.setClientState(clientState);
        transport.createNotificationRequest();
        transport.setNotificationManager(notificationManager);

        NotificationSyncRequest request = transport.createNotificationRequest();
        Assert.assertEquals(new Integer(5), request.getAppStateSeqNumber());
    }

    @Test
    public void onNotificationResponse() throws Exception {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(clientState.getNotificationSeqNumber()).thenReturn(new Integer(5));


        NotificationSyncResponse response = new NotificationSyncResponse();
        response.setAppStateSeqNumber(5);
        response.setResponseStatus(SyncResponseStatus.DELTA);


        NotificationTransport transport = new DefaultNotificationTransport();
        transport.setNotificationManager(notificationManager);
        transport.onNotificationResponse(response);
        transport.setNotificationProcessor(notificationProcessor);
        transport.onNotificationResponse(response);
        transport.setClientState(clientState);
        transport.onNotificationResponse(response);

        List<Topic> topicList = new ArrayList<>(1);
        topicList.add(new Topic());
        response.setAvailableTopics(topicList);

        List<Notification> notifications = new ArrayList<>(2);
        notifications.add(new Notification("topicId1", NotificationType.CUSTOM, null, 3, ByteBuffer.wrap(new byte [] { 1, 2 , 3})));
        notifications.add(new Notification("topicId2", NotificationType.CUSTOM, "uid", 5, ByteBuffer.wrap(new byte [] { 1, 2 , 3})));
        response.setNotifications(notifications);

        transport.onNotificationResponse(response);

        Mockito.verify(notificationProcessor, Mockito.times(1)).notificationReceived(notifications);
        Mockito.verify(notificationProcessor, Mockito.times(1)).topicsListUpdated(topicList);
        Mockito.verify(clientState, Mockito.times(1)).updateTopicSubscriptionInfo(Mockito.eq("topicId1"), Mockito.eq(3));

        Assert.assertEquals("uid", transport.createNotificationRequest().getAcceptedUnicastNotifications().get(0));
    }

}
