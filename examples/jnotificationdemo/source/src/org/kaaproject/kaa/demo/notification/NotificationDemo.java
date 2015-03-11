/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.notification;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDemo.class);
    private KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Notification demo application has started");
        new NotificationDemo().doWork();
        LOG.info("Notification demo application has finished");
    }

    public void doWork() {
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        // Listener, which listens to topic list update
        NotificationTopicListListener topicListListener = new BasicNotificationTopicListListener();
        kaaClient.addTopicListListener(topicListListener);

        // Create basic notification listener, which listens to all notifications
        NotificationListener notificationListener = new BasicNotificationListener();
        kaaClient.addNotificationListener(notificationListener);

        // Start Kaa client
        kaaClient.start();

        // Get available topics
        List<Topic> topicList = kaaClient.getTopics();

        // List available topics
        showTopicList(topicList);

        try {
            // wait for some input before exiting
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // don't listen to topics anymore
        kaaClient.removeTopicListListener(topicListListener);
        kaaClient.stop();
    }

    // Listener, which is used to track notification topic list updates
    // and subscribes a client to each new topic update
    private class BasicNotificationTopicListListener implements NotificationTopicListListener {
        @Override
        public void onListUpdated(List<Topic> list) {
            LOG.info("Topic list was updated:");
            showTopicList(list);
            try {
                kaaClient.subscribeToTopics(extractOptionalTopicIds(list), true);
            } catch (UnavailableTopicException e) {
                LOG.debug("Topic is unavailable, can't subscribe: {}", e.getMessage());
            }
        }
    }

    private class BasicNotificationListener implements NotificationListener {
        @Override
        public void onNotification(String id, SampleNotification sampleNotification) {
            LOG.info("Notification id [{}] received: {}", id, sampleNotification.toString());
        }
    }

    private void showTopicList(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            LOG.info("Topic list is empty");
        } else {
            for (Topic topic : topics) {
                LOG.info("Id: {}, name: {}, type: {}",
                        topic.getId(), topic.getName(), topic.getSubscriptionType());
            }
        }
    }

    private List<String> extractOptionalTopicIds(List<Topic> list) {
        List<String> topicIds = new ArrayList<>();
        for (Topic t : list) {
            if (t.getSubscriptionType() == SubscriptionType.OPTIONAL) topicIds.add(t.getId());
        }
        return topicIds;
    }
}
