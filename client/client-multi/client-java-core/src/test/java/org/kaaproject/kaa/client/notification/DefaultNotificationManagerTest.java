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

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.persistance.KaaClientPropertiesStateTest;
import org.kaaproject.kaa.client.persistence.FilePersistentStorage;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.mockito.Mockito;

public class DefaultNotificationManagerTest {
    @Test
    public void testEmptyTopicList() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        for (Topic t : notificationManager.getTopics()) {
            System.out.println(t);
        }

        Assert.assertTrue(notificationManager.getTopics().isEmpty());
    }

    @Test
    public void testTopicsAfterUpdate() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topics = Arrays.asList(new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
                                         , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topics);

        Assert.assertTrue(notificationManager.getTopics().size() == topics.size());
    }

    @Test
    public void testTopicPersistence() throws IOException {
        KaaClientProperties props = KaaClientPropertiesStateTest.getProperties();
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                                            new FilePersistentStorage(), props);
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topics = Arrays.asList(new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
                                         , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topics);
        state.persist();

        KaaClientPropertiesState newState = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        DefaultNotificationManager newNotificationManager = new DefaultNotificationManager(newState, transport);

        Assert.assertTrue(newNotificationManager.getTopics().size() == topics.size());

        boolean deleted = new File(props.getProperty("state.file_location")).delete();
        Assert.assertTrue(deleted);
    }

    @Test
    public void testTwiceTopicUpdate() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        Topic topic1 = new Topic("id1", "topic_name1", SubscriptionType.MANDATORY);
        Topic topic2 = new Topic("id2", "topic_name1", SubscriptionType.MANDATORY);
        Topic topic3 = new Topic("id3", "topic_name1", SubscriptionType.MANDATORY);

        List<Topic> topicUpdates = new LinkedList<>();
        topicUpdates.add(topic1);
        topicUpdates.add(topic2);

        notificationManager.topicsListUpdated(topicUpdates);

        topicUpdates.remove(topic2);
        topicUpdates.add(topic3);

        notificationManager.topicsListUpdated(topicUpdates);

        List<Topic> newTopics = notificationManager.getTopics();

        Assert.assertTrue(newTopics.size() == topicUpdates.size());
        Assert.assertTrue(newTopics.contains(topic1));
        Assert.assertTrue(newTopics.contains(topic3));
    }

    @Test
    public void testAddTopicUpdateListener() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        final List<Topic> topicUpdates = new LinkedList<>();
        topicUpdates.add(new Topic("id1", "topic_name1", SubscriptionType.MANDATORY));
        topicUpdates.add(new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));
        topicUpdates.add(new Topic("id3", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.addTopicListListener(new NotificationTopicListListener() {
            @Override
            public void onListUpdated(List<Topic> list) {
                Assert.assertArrayEquals(topicUpdates.toArray(), list.toArray());
                topicUpdates.clear();
            }
        });

        notificationManager.topicsListUpdated(topicUpdates);

        Assert.assertTrue(topicUpdates.isEmpty());
    }

    @Test
    public void testRemoveTopicUpdateListener() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        NotificationTopicListListener listener1 = Mockito.mock(NotificationTopicListListener.class);
        NotificationTopicListListener listener2 = Mockito.mock(NotificationTopicListListener.class);

        notificationManager.addTopicListListener(listener1);
        notificationManager.addTopicListListener(listener2);

        List<Topic> topicUpdate = Arrays.asList(new Topic());

        notificationManager.topicsListUpdated(topicUpdate);
        notificationManager.removeTopicListListener(listener2);
        notificationManager.topicsListUpdated(topicUpdate);

        Mockito.verify(listener1, Mockito.times(2)).onListUpdated(topicUpdate);
        Mockito.verify(listener2, Mockito.times(1)).onListUpdated(topicUpdate);
    }

    @Test
    public void testGlobalNotificationListeners() throws IOException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        ByteBuffer notificationBody = ByteBuffer.wrap(new AvroByteArrayConverter<>(Topic.class).toByteArray(
                new Topic("id", "name", SubscriptionType.MANDATORY)));

        notificationManager.topicsListUpdated(topicsUpdate);

        List<Notification> notificationUpdate = Arrays.asList(
                new Notification("id1", NotificationType.CUSTOM, null, 1, notificationBody),
                new Notification("id2", NotificationType.CUSTOM, null, 1, notificationBody));

        NotificationListener mandatoryListener = Mockito.mock(NotificationListener.class);
        NotificationListener globalListener = Mockito.mock(NotificationListener.class);

        notificationManager.addMandatoryTopicsListener(mandatoryListener);
        notificationManager.addNotificationListener(globalListener);

        notificationManager.notificationReceived(notificationUpdate);

        notificationManager.removeNotificationListener(mandatoryListener);

        notificationManager.notificationReceived(notificationUpdate);

        notificationManager.removeMandatoryTopicsListener(globalListener);

        notificationManager.notificationReceived(notificationUpdate);

        Mockito.verify(mandatoryListener, Mockito.times(topicsUpdate.size()))
            .onNotificationRaw(Mockito.anyString(), Mockito.any(ByteBuffer.class));

        Mockito.verify(globalListener, Mockito.times(topicsUpdate.size() * 2))
            .onNotificationRaw(Mockito.anyString(), Mockito.any(ByteBuffer.class));
    }

    @Test
    public void testNotificationListenerOnTopic() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        ByteBuffer notificationBody = ByteBuffer.wrap(new AvroByteArrayConverter<>(Topic.class).toByteArray(
                new Topic("id", "name", SubscriptionType.MANDATORY)));

        notificationManager.topicsListUpdated(topicsUpdate);

        List<Notification> notificationUpdate = Arrays.asList(
                new Notification("id1", NotificationType.CUSTOM, null, 1, notificationBody),
                new Notification("id2", NotificationType.CUSTOM, null, 1, notificationBody));

        NotificationListener globalListener = Mockito.mock(NotificationListener.class);
        NotificationListener topicListener = Mockito.mock(NotificationListener.class);

        notificationManager.addNotificationListener(globalListener);
        notificationManager.addNotificationListener("id2", topicListener);

        notificationManager.notificationReceived(notificationUpdate);
        notificationManager.removeNotificationListener("id2", topicListener);
        notificationManager.notificationReceived(notificationUpdate);

        Mockito.verify(globalListener, Mockito.times(notificationUpdate.size() * 2 - 1))
                .onNotificationRaw(Mockito.anyString(), Mockito.any(ByteBuffer.class));

        Mockito.verify(topicListener, Mockito.times(1))
            .onNotificationRaw(Mockito.anyString(), Mockito.any(ByteBuffer.class));
    }

    @Test(expected=UnavailableTopicException.class)
    public void testAddListenerForUnknownTopic() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);

        NotificationListener listener = Mockito.mock(NotificationListener.class);
        notificationManager.addNotificationListener("unknown_id", listener);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testRemoveListenerForUnknownTopic() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.MANDATORY)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);

        NotificationListener listener = Mockito.mock(NotificationListener.class);
        notificationManager.removeNotificationListener("unknown_id", listener);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testSubscribeOnUnknownTopic1() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.subscribeToTopic("unknown_id", true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testSubscribeOnUnknownTopic2() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.subscribeToTopics(Arrays.asList("id1", "id2", "unknown_id"), true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testUnsubscribeFromUnknownTopic1() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.unsubscribeFromTopic("unknown_id", true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testUnsubscribeFromUnknownTopic2() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.unsubscribeFromTopics(Arrays.asList("id1", "id2", "unknown_id"), true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testSubscribeOnMandatoryTopic1() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.subscribeToTopic("id2", true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testSubscribeOnMandatoryTopic2() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.subscribeToTopics(Arrays.asList("id1", "id2"), true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testUnsubscribeFromMandatoryTopic1() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.unsubscribeFromTopic("id2", true);
    }

    @Test(expected=UnavailableTopicException.class)
    public void testUnsubscribeFromMandatoryTopic2() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.MANDATORY));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.unsubscribeFromTopics(Arrays.asList("id1", "id2"), true);
    }

    @Test
    public void testSuccessSubscriptionToTopic() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id3", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);
        notificationManager.subscribeToTopic("id1", true);

        Mockito.verify(transport, Mockito.times(1)).sync();

        notificationManager.subscribeToTopics(Arrays.asList("id1", "id2"), false);
        notificationManager.unsubscribeFromTopic("id1", false);

        Mockito.verify(transport, Mockito.times(1)).sync();

        notificationManager.sync();

        Mockito.verify(transport, Mockito.times(2)).sync();

        notificationManager.unsubscribeFromTopics(Arrays.asList("id1", "id2"), true);

        Mockito.verify(transport, Mockito.times(3)).sync();
    }

    @Test(expected=UnavailableTopicException.class)
    public void testUpdateSubscriptionUnknownTopic() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id3", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);

        Map<String, List<NotificationListenerInfo>> subscribers = new HashMap<>();
        subscribers.put("unknown_topic_id", Arrays.asList(
                new NotificationListenerInfo(Mockito.mock(NotificationListener.class)
                        , NotificationListenerInfo.Action.ADD)));

        notificationManager.updateTopicSubscriptions(subscribers);
    }

    @Test
    public void testUpdateSubscription() throws IOException, UnavailableTopicException {
        KaaClientPropertiesState state = new KaaClientPropertiesState(
                new FilePersistentStorage(), KaaClientPropertiesStateTest.getProperties());
        NotificationTransport transport = mock(NotificationTransport.class);
        DefaultNotificationManager notificationManager = new DefaultNotificationManager(state, transport);

        List<Topic> topicsUpdate = Arrays.asList(
                new Topic("id1", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id2", "topic_name1", SubscriptionType.OPTIONAL)
              , new Topic("id3", "topic_name1", SubscriptionType.OPTIONAL));

        notificationManager.topicsListUpdated(topicsUpdate);

        NotificationListener listener1 = Mockito.mock(NotificationListener.class);
        NotificationListener listener2 = Mockito.mock(NotificationListener.class);

        Map<String, List<NotificationListenerInfo>> subscribers1 = new HashMap<>();
        subscribers1.put("id1", Arrays.asList(new NotificationListenerInfo(
                            listener1, NotificationListenerInfo.Action.ADD)));

        subscribers1.put("id2", Arrays.asList(new NotificationListenerInfo(
                            listener2, NotificationListenerInfo.Action.ADD)));

        notificationManager.updateTopicSubscriptions(subscribers1);

        List<Notification> notificationUpdate = Arrays.asList(
                new Notification("id1", NotificationType.CUSTOM, null, 1, null),
                new Notification("id2", NotificationType.CUSTOM, null, 1, null));

        notificationManager.notificationReceived(notificationUpdate);

        Map<String, List<NotificationListenerInfo>> subscribers2 = new HashMap<>();
        subscribers2.put("id2", Arrays.asList(new NotificationListenerInfo(
                            listener2, NotificationListenerInfo.Action.REMOVE)));

        notificationManager.updateTopicSubscriptions(subscribers2);
        notificationManager.notificationReceived(notificationUpdate);

        Mockito.verify(listener1, Mockito.times(2)).onNotificationRaw(
                        Mockito.anyString(), Mockito.any(ByteBuffer.class));

        Mockito.verify(listener2, Mockito.times(1)).onNotificationRaw(
                Mockito.anyString(), Mockito.any(ByteBuffer.class));
    }
}
