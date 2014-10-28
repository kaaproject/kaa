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
 *  <h5>Getting access to the current topic list</h5>
 *  <pre>
 *  {@code
 *      KaaDesktop kaa = new KaaDesktop();
 *      KaaClient kaaClient = kaa.getClient();
 *
 *      // Kaa initialization (profile container,
 *      // schema and configuration storages etc.)
 *
 *      NotificationManager notificationManager = kaaClient.getNotificationManager();
 *      List<Topic> topics = notificationManager.getTopics();
 *
 *      for (Topic topic : topics) {
 *          System.out.printf("Id: %s, name: %s, type: %s"
 *                  , topic.getId(), topic.getName(), topic.getSubscriptionType());
 *      }
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
 *  <h5>Global listener(s) for mandatory topics</h5>
 *
 *  <p>Below is an example for receiving notifications for all mandatory
 *  topics:</p>
 *
 *  <pre>
 *  {@code
 *      public class BasicNotificationListener extends AbstractNotificationListener<BasicNotification> {
 *          \@Override
 *          public void onNotification(String topicId, BasicNotification notification) {
 *              System.out.println("Got notification: " + notification.toString());
 *          }
 *
 *          \@Override
 *          protected Class<BasicNotification> getNotificationClass() {
 *               return BasicNotification.class;
 *           }
 *      }
 *
 *      ...
 *
 *      BasicNotificationListener listener = new BasicNotificationListener();
 *
 *      // Add listener to receive notifications from all mandatory topics
 *      notificationManager.addMandatoryTopicsListener(listener);
 *
 *      ...
 *
 *      // Remove listener
 *      notificationManager.removeMandatoryTopicsListener(listener);
 *  }
 *  </pre>
 *
 *  <h5>Specific listener(s) for mandatory topic</h5>
 *
 *  <p>To add/remove specific listener(s) for some mandatory topic, do following:</p>
 *
 *  <pre>
 *  {@code
 *      BasicNotificationListener specificListener = new BasicNotificationListener();
 *      Map<String, List<NotificationListenerInfo>> subscriptions = new HashMap<>();
 *
 *      // Add specific listener
 *      subscriptions.put("some_mandatory_topic_id", Arrays.asList(
 *              new NotificationListenerInfo(specificListener, NotificationListenerInfo.Action.ADD)));
 *
 *      // Remove specific listener
 *      subscriptions.put("another_mandatory_topic_id", Arrays.asList(
 *                  new NotificationListenerInfo(anotherSpecificListener
 *                              , NotificationListenerInfo.Action.REMOVE)));
 *
 *      notificationManager.updateTopicSubscriptions(subscriptions);
 *  }
 *  </pre>
 *
 *  <h5>Voluntary topic (un)subscription</h5>
 *
 *  <p>To subscribe on updates for some voluntary topic, at least one listener
 *  should be added for it. Number of listeners for the same topic is unlimited.
 *  To unsubscribe from some voluntary topic, all listeners for it should be
 *  removed.</p>
 *
 *  <p>Steps for (un)subscription are equal to previous example.</p>
 *
 *  @see org.kaaproject.kaa.client.notification.AbstractNotificationListener
 *  @see org.kaaproject.kaa.client.notification.NotificationTopicListListener
 *  @see org.kaaproject.kaa.client.notification.NotificationListenerInfo
 *  @see org.kaaproject.kaa.client.notification.NotificationManager
 *  @see org.kaaproject.kaa.common.endpoint.gen.Topic
 *
 */
package org.kaaproject.kaa.client.notification;




