/*
 * Copyright 2015 CyberVision, Inc.
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

/**
 * A demo application that shows how to use the Kaa notifications API.
 */
public class NotificationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Notification demo application has started");
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        // A listener that listens to the notification topic list updates.
        NotificationTopicListListener topicListListener = new BasicNotificationTopicListListener();
        kaaClient.addTopicListListener(topicListListener);

        // Add a notification listener that listens to all notifications.
        kaaClient.addNotificationListener(new NotificationListener() {
            @Override
            public void onNotification(String id, SampleNotification sampleNotification) {
                LOG.info("Notification of topic id [{}], received: {}", id, sampleNotification.getMessage());
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        // Get available notification topics.
        List<Topic> topicList = kaaClient.getTopics();

        // List the obtained notification topics.
        showTopicList(topicList);

        try {
            
            // Wait for some input before exiting.
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Stop listening to the notification topic list updates.
        kaaClient.removeTopicListListener(topicListListener);

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Notification demo application has finished");
    }

    // A listener that tracks the notification topic list updates
    // and subscribes the Kaa client to every new topic available.
    private static class BasicNotificationTopicListListener implements NotificationTopicListListener {
        @Override
        public void onListUpdated(List<Topic> list) {
            LOG.info("Topic list was updated:");
            showTopicList(list);
            try {
                kaaClient.subscribeToTopics(extractOptionalTopicIds(list), true);
                
                // Try to subscribe to all new optional topics, if any.
            } catch (UnavailableTopicException e) {
                LOG.debug("Topic is unavailable, can't subscribe: {}", e.getMessage());
            }
        }
    }

    private static List<String> extractOptionalTopicIds(List<Topic> list) {
        List<String> topicIds = new ArrayList<>();
        for (Topic t : list) {
            if (t.getSubscriptionType() == SubscriptionType.OPTIONAL) {
                topicIds.add(t.getId());
            }
        }
        return topicIds;
    }

    private static void showTopicList(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            LOG.info("Topic list is empty");
        } else {
            for (Topic topic : topics) {
                LOG.info("Topic id: {}, name: {}, type: {}",
                        topic.getId(), topic.getName(), topic.getSubscriptionType());
            }
        }
    }
}
