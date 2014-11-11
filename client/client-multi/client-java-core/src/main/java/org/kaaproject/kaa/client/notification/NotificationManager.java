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

import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.endpoint.gen.Topic;

/**
 * <p>Interface for the notification delivery system.</p>
 *
 * <p>Responsible for processing received topic/notification updates,
 * subscribing for voluntary topic updates and unsubscribing from them.</p>
 *
 * @author Yaroslav Zeygerman
 * @author Denis Kimcherenko
 *
 * @see AbstractNotificationListener
 * @see NotificationTopicListListener
 * @see NotificationListenerInfo
 *
 */
public interface NotificationManager {
    /**
     * <p>Add listener for all mandatory topics' updates.</p>
     *
     * <p><b>Use {@link #addNotificationListener(NotificationListener)} instead.</b></p>
     *
     * <p>If specific listener is needed for some mandatory topic, use
     * {@link NotificationManager#addNotificationListener(String, NotificationListener)}.</p>
     *
     * @param listener the listener to receive notification.
     *
     * @see AbstractNotificationListener
     *
     */
    @Deprecated
    void addMandatoryTopicsListener(NotificationListener listener);

    /**
     * <p>Remove listener for mandatory topics' updates.</p>
     *
     * <p><b>Use {@link #removeNotificationListener(NotificationListener)} instead.</b></p>
     *
     * @param listener the listener which is no longer needs updates.
     *
     * @see AbstractNotificationListener
     * @see NotificationManager#removeNotificationListener(NotificationListener)
     *
     */
    @Deprecated
    void removeMandatoryTopicsListener(NotificationListener listener);

    /**
     * <p>Add listener for topics' list updates.</p>
     *
     * @param listener the listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    void addTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>Remove listener of topics' list updates.</p>
     *
     * @param listener listener the listener which is no longer needs updates.
     * @see NotificationTopicListListener
     *
     */
    void removeTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>Update (subscribe/unsubscribe) info about topic's subscriptions.</p>
     *
     * <p>Basic usage is to subscribe for voluntary topic updates and unsubscribe
     * from them. More than one listener may be used for the same topic.</p>
     *
     * <p>Also it may be used to add/remove specific listener(s) for some
     * mandatory topic.</p>
     * <pre>
     * {@code
     * // Assume, BasicNotification is a notification class auto-generated according to predefined Avro schema
     * public class UserNotificationListener extends AbstractNotificationListener<BasicNotification> {
     *     public UserNotificationListener() {}
     *     protected Class<BasicNotification> getNotificationClass() {
     *         return BasicNotification.class;
     *     }
     *     public void onNotification(String topicId, BasicNotification notification) {
     *         System.out.println("Got notification: " + notification.toString());
     *     }
     * }
     *
     * // Assume, there are one mandatory topic with id "mand_id" and
     * // one voluntary with id "vol_id".
     * Map<String, List<NotificationListenerInfo>> subscriptions = new HashMap<>();
     *
     * // Add specific listener for "mand_id" topic
     * UserNotificationListener mandatoryListener = new UserNotificationListener();
     * subscriptions.put("mand_id", Arrays.asList(
     *      new NotificationListenerInfo(mandatoryListener, NotificationListenerInfo.Action.ADD)));
     *
     * // Subscribe for voluntary topic updates
     * UserNotificationListener voluntaryListener = new UserNotificationListener();
     * subscriptions.put("vol_id", Arrays.asList(
     *      new NotificationListenerInfo(voluntaryListener, NotificationListenerInfo.Action.ADD)));
     *
     * kaaClient.getNotificationManager().updateTopicSubscriptions(subscriptions);
     * }
     * </pre>
     *
     * @param subscribers collections of pairs topic id/subscriber info.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see #subscribeToTopic(String, boolean)
     * @see #subscribeToTopics(List, boolean)
     * @see #addNotificationListener(NotificationListener)
     * @see #addNotificationListener(String, NotificationListener)
     *
     */
    @Deprecated
    void updateTopicSubscriptions(Map<String, List<NotificationListenerInfo>> subscribers)
                                                            throws UnavailableTopicException;

    /**
     * <p>Retrieve a list of available topics.</p>
     *
     * @return List of available topics
     *
     */
    List<Topic> getTopics();

    /**
     * <p>Add listener to receive all notifications (both for mandatory and
     * voluntary topics).</p>
     *
     * @param listener Listener to receive notifications
     *
     * @see AbstractNotificationListener
     */
    void addNotificationListener(NotificationListener listener);

    /**
     * <p>Add listener to receive notifications relating to the specified topic.</p>
     *
     * <p>Listener(s) for voluntary topics may be added/removed irrespective to
     * whether subscription was already or not.</p>
     *
     * @param topicId  Id of topic (both mandatory and voluntary).
     * @param listener Listener to receive notifications.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    void addNotificationListener(String topicId, NotificationListener listener)
                                            throws UnavailableTopicException;

    /**
     * <p>Remove listener receiving all notifications (both for mandatory and
     * voluntary topics).</p>
     *
     * @param listener Listener to receive notifications
     *
     * @see AbstractNotificationListener
     */
    void removeNotificationListener(NotificationListener listener);

   /**
    * <p>Remove listener receiving notifications for the specified topic.</p>
    *
    * <p>Listener(s) for voluntary topics may be added/removed irrespective to
    * whether subscription was already or not.</p>
    *
    * @param topicId Id of topic (both mandatory and voluntary).
    * @param listener Listener to receive notifications.
    *
    * @throws UnavailableTopicException Throw if unknown topic id is provided.
    *
    * @see AbstractNotificationListener
    */
   void removeNotificationListener(String topicId, NotificationListener listener)
                                           throws UnavailableTopicException;

    /**
     * <p>Subscribe to notifications relating to the specified voluntary topic.</p>
     *
     * @param topicId Id of a voluntary topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't voluntary.
     *
     * @see #sync()
     */
    void subscribeToTopic(String topicId, boolean forceSync)
                                            throws UnavailableTopicException;

    /**
     * <p>Subscribe to notifications relating to the specified list of
     * voluntary topics.</p>
     *
     * @param topicIds List of voluntary topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't voluntary.
     *
     * @see #sync()
     */
    void subscribeToTopics(List<String> topicIds, boolean forceSync)
                                            throws UnavailableTopicException;

    /**
     * <p>Unsubscribe from notifications relating to the specified voluntary topic.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicId Id of a voluntary topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't voluntary.
     *
     * @see #sync()
     */
    void unsubscribeFromTopic(String topicId, boolean forceSync)
                                            throws UnavailableTopicException;

    /**
     * <p>Unsubscribe from notifications relating to the specified list of
     * voluntary topics.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicIds List of voluntary topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't voluntary.
     *
     * @see #sync()
     */
    void unsubscribeFromTopics(List<String> topicIds, boolean forceSync)
                                            throws UnavailableTopicException;

    /**
     * <p>Accept voluntary subscription changes.</p>
     *
     * <p>Should be used after all {@link #subscribeToTopic(String, boolean)},
     * {@link #subscribeToTopics(List, boolean)}, {@link #unsubscribeFromTopic(String, boolean)},
     * {@link #unsubscribeFromTopics(List, boolean)} calls with parameter
     * {@code forceSync} set to {@code false}.</p>
     *
     * <p>Use it as a convenient way to make different consequent changes in
     * the voluntary subscription:</p>
     * <pre>
     * {@code
     *  NotificationManager notificationManager = kaaClient.getNotificationManager();
     *
     *  // Make subscription changes
     *  notificationManager.subscribeOnTopics(Arrays.asList(
     *          "voluntary_topic1", "voluntary_topic2", "voluntary_topic3"), false);
     *  notificationManager.unsubscribeFromTopic("voluntary_topic4", false);
     *
     *  // Add listeners for voluntary topics (optional)
     *
     *  // Commit changes
     *  notificationManager.sync();
     * }
     * </pre>
     */
    void sync();
}
