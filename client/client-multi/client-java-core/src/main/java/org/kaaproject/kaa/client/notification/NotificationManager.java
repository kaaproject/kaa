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

package org.kaaproject.kaa.client.notification;

import org.kaaproject.kaa.common.endpoint.gen.Topic;

import java.util.List;

/**
 * <p>
 * Interface for the notification delivery system.
 * </p>
 *
 * <p>
 * Responsible for processing received topic/notification updates, subscribing
 * for optional topic updates and unsubscribing from them.
 * </p>
 *
 * @author Yaroslav Zeygerman
 * @author Denis Kimcherenko
 * @see NotificationTopicListListener
 * @see NotificationListenerInfo
 */
public interface NotificationManager {

  /**
   * <p>
   * Add listener for notification topics' list updates.
   * </p>
   *
   * @param listener the listener to receive updates.
   * @see NotificationTopicListListener
   */
  void addTopicListListener(NotificationTopicListListener listener);

  /**
   * <p>
   * Remove listener of notification topics' list updates.
   * </p>
   *
   * @param listener listener the listener which is no longer needs updates.
   * @see NotificationTopicListListener
   */
  void removeTopicListListener(NotificationTopicListListener listener);

  /**
   * <p>
   * Retrieve a list of available notification topics.
   * </p>
   *
   * @return List of available topics
   */
  List<Topic> getTopics();

  /**
   * <p>
   * Add listener to receive all notifications (both for mandatory and
   * optional topics).
   * </p>
   *
   * @param listener Listener to receive notifications
   */
  void addNotificationListener(NotificationListener listener);

  /**
   * <p>
   * Add listener to receive notifications relating to the specified topic.
   * </p>
   *
   * <p>
   * Listener(s) for optional topics may be added/removed irrespective to
   * whether subscription was already or not.
   * </p>
   *
   * @param topicId  Id of topic (both mandatory and optional).
   * @param listener Listener to receive notifications.
   * @throws UnavailableTopicException Throw if unknown topic id is provided.
   */
  void addNotificationListener(Long topicId, NotificationListener listener)
          throws UnavailableTopicException;

  /**
   * <p>
   * Remove listener receiving all notifications (both for mandatory and
   * optional topics).
   * </p>
   *
   * @param listener Listener to receive notifications
   */
  void removeNotificationListener(NotificationListener listener);

  /**
   * <p>
   * Remove listener receiving notifications for the specified topic.
   * </p>
   *
   * <p>
   * Listener(s) for optional topics may be added/removed irrespective to
   * whether subscription was already or not.
   * </p>
   *
   * @param topicId  Id of topic (both mandatory and optional).
   * @param listener Listener to receive notifications.
   * @throws UnavailableTopicException Throw if unknown topic id is provided.
   */
  void removeNotificationListener(Long topicId, NotificationListener listener)
          throws UnavailableTopicException;

  /**
   * <p>
   * Subscribe to notifications relating to the specified optional topic.
   * </p>
   *
   * @param topicId   Id of a optional topic.
   * @param forceSync Define whether current subscription update should be accepted immediately (see
   *                  {@link #sync()}).
   * @throws UnavailableTopicException Throw if unknown topic id is provided or topic isn't
   *                                   optional.
   * @see #sync()
   */
  void subscribeToTopic(Long topicId, boolean forceSync) throws UnavailableTopicException;

  /**
   * <p>
   * Subscribe to notifications relating to the specified list of optional
   * topics.
   * </p>
   *
   * @param topicIds  List of optional topic id.
   * @param forceSync Define whether current subscription update should be accepted immediately (see
   *                  {@link #sync()}).
   * @throws UnavailableTopicException Throw if unknown topic id is provided or topic isn't
   *                                   optional.
   * @see #sync()
   */
  void subscribeToTopics(List<Long> topicIds, boolean forceSync) throws UnavailableTopicException;

  /**
   * <p>
   * Unsubscribe from notifications relating to the specified optional topic.
   * </p>
   *
   * <p>
   * All previously added listeners will be removed automatically.
   * </p>
   *
   * @param topicId   Id of a optional topic.
   * @param forceSync Define whether current subscription update should be accepted immediately (see
   *                  {@link #sync()}).
   * @throws UnavailableTopicException Throw if unknown topic id is provided or topic isn't
   *                                   optional.
   * @see #sync()
   */
  void unsubscribeFromTopic(Long topicId, boolean forceSync) throws UnavailableTopicException;

  /**
   * <p>
   * Unsubscribe from notifications relating to the specified list of optional
   * topics.
   * </p>
   *
   * <p>
   * All previously added listeners will be removed automatically.
   * </p>
   *
   * @param topicIds  List of optional topic id.
   * @param forceSync Define whether current subscription update should be accepted immediately (see
   *                  {@link #sync()}).
   * @throws UnavailableTopicException Throw if unknown topic id is provided or topic isn't
   *                                   optional.
   * @see #sync()
   */
  void unsubscribeFromTopics(List<Long> topicIds, boolean forceSync)
          throws UnavailableTopicException;

  /**
   * <p> Accept optional subscription changes. </p>
   *
   * <p> Should be used after all {@link #subscribeToTopic(Long, boolean)}, {@link
   * #subscribeToTopics(List, boolean)}, {@link #unsubscribeFromTopic(Long, boolean)}, {@link
   * #unsubscribeFromTopics(List, boolean)} calls with parameter {@code forceSync} set to {@code
   * false}. </p>
   *
   * <p> Use it as a convenient way to make different consequent changes in the optional
   * subscription: </p>
   *
   * <pre>
   * {
   *     &#064;code
   *     NotificationManager notificationManager = kaaClient.getNotificationManager();
   *
   *     // Make subscription changes
   *     notificationManager.subscribeOnTopics(Arrays.asList(&quot;optional_topic1&quot;,
   * &quot;optional_topic2&quot;, &quot;optional_topic3&quot;), false);
   *     notificationManager.unsubscribeFromTopic(&quot;optional_topic4&quot;, false);
   *
   *     // Add listeners for optional topics here
   *
   *     // Commit changes
   *     notificationManager.sync();
   * }
   * </pre>
   */
  void sync();
}
