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
import org.kaaproject.kaa.common.endpoint.gen.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

public class NotificationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDemo.class);
    private static final String SUBSCRIBE = "subscribe";
    private static final String UNSUBSCRIBE = "unsubscribe";
    private static final String LIST = "list";
    private static final String HELP = "help";
    private static final String EXIT = "exit";
    private static final String QUIT = "quit";
    private List<Topic> topicList;

    public static void main(String[] args) {
        LOG.info("Notification demo application has started");
        new NotificationDemo().doWork();
        LOG.info("Notification demo application has finished");
    }

    public void doWork() {
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        // Listener, which listens to topic list update
        NotificationTopicListListener topicListListener = new BasicNotificationTopicListListener();
        kaaClient.addTopicListListener(topicListListener);

        // Create basic notification listener, which listens to all notifications
        NotificationListener notificationListener = new BasicNotificationListener();
        kaaClient.addNotificationListener(notificationListener);

        // Start Kaa client
        kaaClient.start();


        // Get available topics
        topicList = kaaClient.getTopics();
        // List available topics
        showTopicList();

        displayHelpMessage();
        // read input and manage topics until 'exit' is entered
        try (Scanner in = new Scanner(System.in)) {
            String line = "";
            while (in.hasNextLine()) {
                line = in.nextLine();
                String command = extractCommand(line);
                if (command == null || command.isEmpty()) continue;
                if (command.equals(EXIT) || command.equals(QUIT)) break;
                switch (command) {
                    case LIST:
                        showTopicList();
                        break;
                    case SUBSCRIBE:
                        String topicId = getTopicId(line);
                        try {
                            // subscribe to a topic with id topicId with forced synchronization enabled
                            kaaClient.subscribeToTopic(topicId, true);
                        } catch (UnavailableTopicException e) {
                            LOG.error("Subscription [{}]", e.getMessage());
                        }
                        break;
                    case UNSUBSCRIBE:
                        topicId = getTopicId(line);
                        try {
                            // unsubscribe from a topic with id topicId with forced synchronization enabled
                            kaaClient.unsubscribeFromTopic(topicId, true);
                        } catch (UnavailableTopicException e) {
                            LOG.error("Subscription [{}]", e.getMessage());
                        }
                        break;
                    case HELP:
                        displayHelpMessage();
                        break;
                    default:
                        System.out.println("Unknown command '" + command + "'. Print 'help' to see usage explanation");
                        break;
                }
            }
        }

        // don't listen to topics anymore
        kaaClient.removeTopicListListener(topicListListener);
        kaaClient.stop();
    }

    // Listener, which is used to track notification topic list update
    private class BasicNotificationTopicListListener implements NotificationTopicListListener {
        @Override
        public void onListUpdated(List<Topic> list) {
            LOG.info("Topic list was updated:");
            topicList = list;
            showTopicList();
        }
    }

    private class BasicNotificationListener implements NotificationListener {
        @Override
        public void onNotification(String id, SampleNotification sampleNotification) {
            LOG.info("Notification id [" + id + "] received: " + sampleNotification.toString());
        }
    }

    private String getTopicId(String line) {
        if (line.startsWith(SUBSCRIBE)) return line.substring(SUBSCRIBE.length(), line.length()).trim();
        else return line.substring(UNSUBSCRIBE.length(), line.length()).trim();
    }

    private void showTopicList() {
        if (topicList == null || topicList.isEmpty()) System.out.println("Topic list is empty");
        for (Topic topic : topicList) {
            System.out.printf("Id: %s, name: %s, type: %s\n",
                    topic.getId(), topic.getName(), topic.getSubscriptionType());
        }
    }

    private void displayHelpMessage() {
        System.out.println("Usage: \n" +
                "'help' - displays this message\n" +
                "'list' - displays available topics list\n" +
                "'subscribe topic_id' - subscribes to a topic with the specified topic_id\n" +
                "'unsubscribe topic_id' - unsubscribes from an optional topic with the specified topic_id\n" +
                "'exit' - finishes the program");
    }

    private String extractCommand(String line) {
        String[] tokenizedCommand = line.trim().split("\\s+");
        return tokenizedCommand.length == 0 ? null : tokenizedCommand[0];
    }
}
