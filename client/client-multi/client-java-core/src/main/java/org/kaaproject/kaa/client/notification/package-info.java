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

/**
 * <p>Provides implementation of a notification management.</p>
 *
 *  <p>The Kaa Notifications subsystem enables messages delivery from the
 *  Kaa cluster to endpoints (EP). It is based on auto-generated classes
 *  according to the topic's notification schema used during SDK generation.</p>
 *
 *  <p>Notification topics can be mandatory or voluntary. Mandatory topic
 *  notifications are delivered in an enforced manner. Voluntary topics require
 *  subscription. It is the responsibility of the client code to register
 *  the topic update listener and subscribe to voluntary topics.</p>
 *
 *  <h3>Topics - usage examples</h3>
 *
 *  <h5>Get access to the available topics</h5>
 *  <pre>
 *  {@code
 *  KaaDesktop kaa = new KaaDesktop();
 *  KaaClient kaaClient = kaa.getClient();
 *
 *  // Kaa initialization (profile container, schema and configuration storages etc.)
 *
 *  NotificationManager notificationManager = kaaClient.getNotificationManager();
 *  List<Topic> topics = notificationManager.getTopics();
 *
 *  for (Topic topic : topics) {
 *  System.out.printf("Id: %s, name: %s, type: %s"
 *              , topic.getId(), topic.getName(), topic.getSubscriptionType());
 *  }
 *  }
 *  </pre>
 *
 *  <h5>Topic update subscription</h5>
 *
 *  <p>If there is need to know about topic list updates, do following
 *  ({@link org.kaaproject.kaa.client.notification.NotificationTopicListListener}):</p>
 *  <pre>
 *  {@code
 *      notificationManager.addTopicListListener(new NotificationTopicListListener() {
 *      \@Override
 *      public void onListUpdated(List<Topic> topics) {
 *          for (Topic topic : topics) {
 *              System.out.printf("Id: %s, name: %s, type: %s"
 *                      , topic.getId(), topic.getName(), topic.getSubscriptionType());
 *          }
 *      }
 *  });
 *  }
 *  </pre>
 *
 *  <p>To remove topic update listener, call
 *  {@link org.kaaproject.kaa.client.notification.NotificationManager#removeTopicListListener(NotificationTopicListListener)}
 *  with an appropriate listener.</p>
 *
 *  <h3>Notifications - usage examples</h3>
 *
 *  <p>In order to receive notifications, both mandatory or voluntary, there
 *  should be add an appropriate listener. The listener may be one for all topics.
 *  Also there is possibility to add listener for specific topic notifications.</p>
 *
 *  <p>Assume, notification schema has the following form:</p>
 *  <pre>
 *  {@code
 *      {
 *          "type": "record",
 *          "name": "BasicNotification",
 *          "namespace": "org.kaaproject.kaa.client.example",
 *          "fields": [
 *              {
 *                  "name": "body",
 *                  "type": "string"
 *              }
 *          ]
 *      }
 *  }
 *  </pre>
 *
 *  <p>As mentioned earlier, there is two kind of topics - mandatory and
 *  voluntary. Further it will be discussed dealing with both of them.</p>
 *
 *  <h5>Notification listener(s) for all topics</h5>
 *
 *  <p>Below is an example of receiving notifications relating to all topics
 *  irrespective whether it is mandatory or voluntary:</p>
 *
 *  <pre>
 *  {@code
 *  public class BasicNotificationListener extends AbstractNotificationListener<BasicNotification> {
 *      \@Override
 *      public void onNotification(String topicId, BasicNotification notification) {
 *          System.out.println("Got notification: " + notification.toString());
 *      }
 *
 *      \@Override
 *      protected Class<BasicNotification> getNotificationClass() {
 *           return BasicNotification.class;
 *       }
 *  }
 *
 *  ...
 *
 *  BasicNotificationListener listener = new BasicNotificationListener();
 *
 *  // Add listener
 *  notificationManager.addNotificationListener(listener);
 *
 *  ...
 *
 *  // Remove listener
 *  notificationManager.removeNotificationListener(listener);
 *  }
 *  </pre>
 *
 *  <h5>Notification listener(s) for a specified topic</h5>
 *
 *  <p>To add/remove listener(s) to receive notifications relating to
 *  the specified topic, do following</p>
 *
 *  <pre>
 *  {@code
 *  BasicNotificationListener specificListener = new BasicNotificationListener();
 *  Map<String, List<NotificationListenerInfo>> subscriptions = new HashMap<>();
 *
 *  // Add listener
 *  notificationManager.addNotificationListener("some_mandatory_topic_id", listener);
 *
 *  ...
 *
 *  // Remove listener
 *  notificationManager.removeNotificationListener("another_mandatory_topic_id", listener);
 *  }
 *  </pre>
 *
 *  <h5>Voluntary topic (un)subscription</h5>
 *
 *  <p>To receive notifications relating to some voluntary topic, firstly you
 *  should to subscribe on this topic:</p>
 *  <pre>
 *  {@code
 *  BasicNotificationListener listener = new BasicNotificationListener();
 *  notificationManager.addNotificationListener("voluntary_topic_id", listener);
 *  notificationManager.subscribeOnTopic("voluntary_topic_id", true);
 *  }
 *  </pre>
 *
 *  <p>To unsubscribe from some voluntary topic, do following:</p>
 *  <pre>
 *  {@code
 *  // All added listeners will be removed automatically
 *  notificationManager.unsubscribeFromTopic("voluntary_topic_id", true);
 *  }
 *  </pre>
 *
 *  <p>There is a similar stuff to deal with a group of voluntary topics -
 *  {@link org.kaaproject.kaa.client.notification.NotificationManager#subscribeOnTopics(java.util.List, boolean)} and
 *  {@link org.kaaproject.kaa.client.notification.NotificationManager#unsubscribeFromTopics(java.util.List, boolean)}.</p>
 *
 *  <h6>Performance</h6>
 *
 *  <p>To increase Kaa performance in case of several subsequent subscription
 *  changes and avoid possible race conditions, we recommend to use following
 *  approach:</p>
 *  <pre>
 *  {@code
 *  // Make subscription changes
 *  notificationManager.subscribeOnTopics(Arrays.asList(
 *          "voluntary_topic1", "voluntary_topic2", "voluntary_topic3"), false);
 *  notificationManager.unsubscribeFromTopic("voluntary_topic4", false);
 *
 *  // Add listeners for voluntary topics (optional)
 *
 *  // Commit changes
 *  notificationManager.sync();
 *  }
 *  </pre>
 *
 *  @see org.kaaproject.kaa.client.notification.NotificationManager
 *  @see org.kaaproject.kaa.client.notification.AbstractNotificationListener
 *  @see org.kaaproject.kaa.client.notification.NotificationTopicListListener
 *
 */
package org.kaaproject.kaa.client.notification;
