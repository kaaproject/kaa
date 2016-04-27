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

package org.kaaproject.kaa.server.sync;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ServerSyncTest {
    private static ProfileServerSync profileServerSync;
    private static ConfigurationServerSync configurationServerSync;
    private static NotificationServerSync notificationServerSync;
    private static UserServerSync userServerSync;
    private static EventServerSync eventServerSync;
    private static RedirectServerSync redirectServerSync;
    private static LogServerSync logServerSync = new LogServerSync();

    @BeforeClass
    public static void setUp() {
        profileServerSync = new ProfileServerSync(SyncResponseStatus.DELTA);

        configurationServerSync = new ConfigurationServerSync(2, SyncResponseStatus.DELTA,

                ByteBuffer.allocate(1), ByteBuffer.allocate(2));
        Notification notification1 = new Notification("12345", NotificationType.CUSTOM,
                "987654", 8838421, ByteBuffer.allocate(5));
        Notification notification2 = new Notification("782153", NotificationType.SYSTEM,
                "6233425", 412234, ByteBuffer.allocate(15));
        List<Notification> notifications = Arrays.asList(notification1, notification2);
        Topic topic1 = new Topic("1234", "TopicName1", SubscriptionType.MANDATORY);
        Topic topic2 = new Topic("62343", "TopicName2", SubscriptionType.OPTIONAL);
        Topic topic3 = new Topic("51515", "TopicName3", SubscriptionType.MANDATORY);
        List<Topic> topics = Arrays.asList(topic1, topic2, topic3);
        notificationServerSync = new NotificationServerSync(SyncResponseStatus.DELTA, notifications, topics);

        UserAttachResponse userAttachResponse = new UserAttachResponse(SyncStatus.FAILURE,
                UserVerifierErrorCode.CONNECTION_ERROR, "Some error");
        UserAttachNotification userAttachNotification = new UserAttachNotification("5123", "6134643");
        UserDetachNotification userDetachNotification = new UserDetachNotification("5115");
        EndpointAttachResponse endpointAttachResponse = new EndpointAttachResponse(7, "ab314fe", SyncStatus.FAILURE);
        List<EndpointAttachResponse> endpointAttachResponses = Arrays.asList(endpointAttachResponse);
        EndpointDetachResponse endpointDetachResponse = new EndpointDetachResponse(8, SyncStatus.FAILURE);
        List<EndpointDetachResponse> endpointDetachResponses = Arrays.asList(endpointDetachResponse);
        userServerSync = new UserServerSync(userAttachResponse, userAttachNotification, userDetachNotification,
                endpointAttachResponses, endpointDetachResponses);

        EventListenersResponse eventListenersResponse = new EventListenersResponse(52353, null, SyncStatus.FAILURE);
        List<EventListenersResponse> eventListenersResponses = Arrays.asList(eventListenersResponse);
        Event event1 = new Event(5215, "some.clazz", ByteBuffer.allocate(16), "5123", "5125");
        Event event2 = new Event(6237, "some.other.clazz", ByteBuffer.allocate(24), "5134", "5616");
        List<Event> events = Arrays.asList(event1, event2);
        eventServerSync = new EventServerSync(new EventSequenceNumberResponse(5252), eventListenersResponses, events);

        redirectServerSync = new RedirectServerSync(5213);

        LogDeliveryStatus deliveryStatus1 = new LogDeliveryStatus(5123, SyncStatus.FAILURE,
                LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
        LogDeliveryStatus deliveryStatus2 = new LogDeliveryStatus(6234, SyncStatus.SUCCESS,
                LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR);
        List<LogDeliveryStatus> logDeliveryStatuses = Arrays.asList(deliveryStatus1, deliveryStatus2);
        logServerSync = new LogServerSync(logDeliveryStatuses);
    }

    @Test
    public void deepCopyNullServerSyncTest() {
        ServerSync copiedServerSync = ServerSync.deepCopy(null);
        Assert.assertNull(copiedServerSync);
    }

    @Test
    public void deepCopyServerSyncTest() {
        ServerSync serverSync = new ServerSync(1, SyncStatus.SUCCESS, profileServerSync, configurationServerSync,
                notificationServerSync, userServerSync, eventServerSync, redirectServerSync, logServerSync);

        ServerSync serverSyncCopy = ServerSync.deepCopy(serverSync);
        Assert.assertEquals(serverSync, serverSyncCopy);
        Assert.assertFalse(serverSync == serverSyncCopy);
    }
}
