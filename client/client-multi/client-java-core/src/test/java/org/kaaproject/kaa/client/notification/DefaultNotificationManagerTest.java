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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.persistance.KaaClientPropertiesStateTest;
import org.kaaproject.kaa.client.persistence.FilePersistentStorage;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.BasicUserNotification;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

public class DefaultNotificationManagerTest {

    @Test
    public void testSubscriptions() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager manager = new DefaultNotificationManager(state, transport);

        boolean isExceptionOccured = false;
        try {
            Map<String, List<NotificationListenerInfo>> fakeSubscribers = new
                    HashMap<String, List<NotificationListenerInfo>>();

            fakeSubscribers.put("topic3", Arrays.asList(
                    new NotificationListenerInfo(null, NotificationListenerInfo.Action.ADD)));

            manager.updateTopicSubscriptions(fakeSubscribers);
        } catch (UnavailableTopicException e) {
            isExceptionOccured = true;
        }

        assertTrue(isExceptionOccured);

        NotificationTopicListListener topicListener1 = mock(NotificationTopicListListener.class);
        manager.addTopicListListener(topicListener1);
        manager.addTopicListListener(topicListener1);
        manager.addTopicListListener(null);

        Topic topic1 = new Topic();
        topic1.setId("1234");
        topic1.setName("topic1");
        topic1.setSubscriptionType(SubscriptionType.MANDATORY);

        Topic topic2 = new Topic();
        topic2.setId("4321");
        topic2.setName("topic2");
        topic2.setSubscriptionType(SubscriptionType.MANDATORY);

        List<Topic> topicList = new LinkedList<Topic>();
        topicList.add(topic1);
        topicList.add(topic2);

        manager.topicsListUpdated(topicList);

        NotificationListener listener1 = mock(NotificationListener.class);
        manager.addMandatoryTopicsListener(listener1);
        manager.addMandatoryTopicsListener(listener1);
        manager.addMandatoryTopicsListener(null);

        NotificationListener listener2 = mock(NotificationListener.class);

        Map<String, List<NotificationListenerInfo>> subscribers = new
                                HashMap<String, List<NotificationListenerInfo>>();

        subscribers.put("4321", Arrays.asList(
                new NotificationListenerInfo(listener2, NotificationListenerInfo.Action.ADD),
                new NotificationListenerInfo(listener2, NotificationListenerInfo.Action.ADD),
                new NotificationListenerInfo(null, NotificationListenerInfo.Action.ADD)));

        manager.updateTopicSubscriptions(subscribers);

        BasicUserNotification basicNotification = new BasicUserNotification();
        basicNotification.setNotificationBody("test");
        basicNotification.setUserNotificationParam(new Integer(5));
        ByteBuffer notificationBody = ByteBuffer.wrap(new AvroByteArrayConverter<>(BasicUserNotification.class).toByteArray(basicNotification));

        Notification notification1 = new Notification();
        notification1.setTopicId("1234");
        notification1.setSeqNumber(1);
        notification1.setType(NotificationType.CUSTOM);
        notification1.setBody(notificationBody);

        Notification notification2 = new Notification();
        notification2.setTopicId("4321");
        notification2.setSeqNumber(2);
        notification2.setType(NotificationType.CUSTOM);
        notification2.setBody(notificationBody);

        List<Notification> notificationList = new LinkedList<Notification>();
        notificationList.add(notification1);
        notificationList.add(notification2);

        Map<String, List<NotificationListenerInfo>> unsubscribers = new
                HashMap<String, List<NotificationListenerInfo>>();

        unsubscribers.put("4321", Arrays.asList(
                new NotificationListenerInfo(listener2, NotificationListenerInfo.Action.REMOVE),
                new NotificationListenerInfo(null, NotificationListenerInfo.Action.REMOVE)));

        manager.notificationReceived(notificationList);
        manager.updateTopicSubscriptions(unsubscribers);
        manager.removeMandatoryTopicsListener(listener1);
        manager.removeMandatoryTopicsListener(null);
        manager.removeTopicListListener(topicListener1);
        manager.removeTopicListListener(null);
        manager.notificationReceived(notificationList);

        List<Topic> expectedTopicList = new LinkedList<>();
        expectedTopicList.add(topic1);
        expectedTopicList.add(topic2);
        assertEquals(expectedTopicList, manager.getTopics());

        topicList.remove(topic1);
        manager.topicsListUpdated(topicList);

        verify(listener1, times(1)).onNotificationRaw(any(String.class), eq(notificationBody));
        verify(listener2, times(1)).onNotificationRaw("4321", notificationBody);
        verify(topicListener1, times(1)).onListUpdated(any(List.class));
        verify(transport, times(2)).sync();

        expectedTopicList.remove(topic1);
        assertEquals(expectedTopicList, manager.getTopics());
    }

}
