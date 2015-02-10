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
 *
 * @see AbstractNotificationListener
 * @see NotificationTopicListListener
 * @see NotificationListenerInfo
 *
 */
public interface NotificationManager {
    /**
     * <p>
     * Add listener for all mandatory topics' updates.
     * </p>
     *
     * <p>
     * <b>Use {@link #addNotificationListener(NotificationListener)}
     * instead.</b>
     * </p>
     *
     * <p>
     * If specific listener is needed for some mandatory topic, use
     * {@link NotificationManager#addNotificationListener(String, NotificationListener)}
     * .
     * </p>
     *
     * @param listener
     *            the listener to receive notification.
     *
     * @see AbstractNotificationListener
     *
     */
    @Deprecated
    void addMandatoryTopicsListener(NotificationListener listener);

    /**
     * <p>
     * Remove listener for mandatory topics' updates.
     * </p>
     *
     * <p>
     * <b>Use {@link #removeNotificationListener(NotificationListener)}
     * instead.</b>
     * </p>
     *
     * @param listener
     *            the listener which is no longer needs updates.
     *
     * @see AbstractNotificationListener
     * @see NotificationManager#removeNotificationListener(NotificationListener)
     *
     */
    @Deprecated
    void removeMandatoryTopicsListener(NotificationListener listener);

    /**
     * <p>
     * Update (subscribe/unsubscribe) info about topic's subscriptions.
     * </p>
     *
     * <p>
     * Basic usage is to subscribe for optional topic updates and unsubscribe
     * from them. More than one listener may be used for the same topic.
     * </p>
     *
     * <p>
     * Also it may be used to add/remove specific listener(s) for some mandatory
     * topic.
     * </p>
     * 
     * <pre>
     * {
     *     &#064;code
     *     // Assume, BasicNotification is a notification class auto-generated
     *     // according to predefined Avro schema
     *     public class UserNotificationListener extends AbstractNotificationListener&lt;BasicNotification&gt; {
     *         public UserNotificationListener() {
     *         }
     * 
     *         protected Class&lt;BasicNotification&gt; getNotificationClass() {
     *             return BasicNotification.class;
     *         }
     * 
     *         public void onNotification(String topicId, BasicNotification notification) {
     *             System.out.println(&quot;Got notification: &quot; + notification.toString());
     *         }
     *     }
     * 
     *     // Assume, there are one mandatory topic with id &quot;mand_id&quot; and
     *     // one optional with id &quot;vol_id&quot;.
     *     Map&lt;String, List&lt;NotificationListenerInfo&gt;&gt; subscriptions = new HashMap&lt;&gt;();
     * 
     *     // Add specific listener for &quot;mand_id&quot; topic
     *     UserNotificationListener mandatoryListener = new UserNotificationListener();
     *     subscriptions.put(&quot;mand_id&quot;, Arrays.asList(new NotificationListenerInfo(mandatoryListener, NotificationListenerInfo.Action.ADD)));
     * 
     *     // Subscribe for optional topic updates
     *     UserNotificationListener optionalListener = new UserNotificationListener();
     *     subscriptions.put(&quot;vol_id&quot;, Arrays.asList(new NotificationListenerInfo(optionalListener, NotificationListenerInfo.Action.ADD)));
     * 
     *     kaaClient.getNotificationManager().updateTopicSubscriptions(subscriptions);
     * }
     * </pre>
     *
     * @param subscribers
     *            collections of pairs topic id/subscriber info.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided.
     *
     * @see #subscribeToTopic(String, boolean)
     * @see #subscribeToTopics(List, boolean)
     * @see #addNotificationListener(NotificationListener)
     * @see #addNotificationListener(String, NotificationListener)
     *
     */
    @Deprecated
    void updateTopicSubscriptions(Map<String, List<NotificationListenerInfo>> subscribers) throws UnavailableTopicException;

    /**
     * <p>
     * Add listener for notification topics' list updates.
     * </p>
     *
     * @param listener
     *            the listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    void addTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>
     * Remove listener of notification topics' list updates.
     * </p>
     *
     * @param listener
     *            listener the listener which is no longer needs updates.
     * @see NotificationTopicListListener
     *
     */
    void removeTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>
     * Retrieve a list of available notification topics.
     * </p>
     *
     * @return List of available topics
     *
     */
    List<Topic> getTopics();

    /**
     * <p>
     * Add listener to receive all notifications (both for mandatory and
     * optional topics).
     * </p>
     *
     * @param listener
     *            Listener to receive notifications
     *
     * @see AbstractNotificationListener
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
     * @param topicId
     *            Id of topic (both mandatory and optional).
     * @param listener
     *            Listener to receive notifications.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    void addNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException;

    /**
     * <p>
     * Remove listener receiving all notifications (both for mandatory and
     * optional topics).
     * </p>
     *
     * @param listener
     *            Listener to receive notifications
     *
     * @see AbstractNotificationListener
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
     * @param topicId
     *            Id of topic (both mandatory and optional).
     * @param listener
     *            Listener to receive notifications.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    void removeNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException;

    /**
     * <p>
     * Subscribe to notifications relating to the specified optional topic.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #sync()
     */
    void subscribeToTopic(String topicId, boolean forceSync) throws UnavailableTopicException;

    /**
     * <p>
     * Subscribe to notifications relating to the specified list of optional
     * topics.
     * </p>
     *
     * @param topicIds
     *            List of optional topic id.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #sync()
     */
    void subscribeToTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException;

    /**
     * <p>
     * Unsubscribe from notifications relating to the specified optional topic.
     * </p>
     *
     * <p>
     * All previously added listeners will be removed automatically.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #sync()
     */
    void unsubscribeFromTopic(String topicId, boolean forceSync) throws UnavailableTopicException;

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
     * @param topicIds
     *            List of optional topic id.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #sync()
     */
    void unsubscribeFromTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException;

    /**
     * <p>
     * Accept optional subscription changes.
     * </p>
     *
     * <p>
     * Should be used after all {@link #subscribeToTopic(String, boolean)},
     * {@link #subscribeToTopics(List, boolean)},
     * {@link #unsubscribeFromTopic(String, boolean)},
     * {@link #unsubscribeFromTopics(List, boolean)} calls with parameter
     * {@code forceSync} set to {@code false}.
     * </p>
     *
     * <p>
     * Use it as a convenient way to make different consequent changes in the
     * optional subscription:
     * </p>
     * 
     * <pre>
     * {
     *     &#064;code
     *     NotificationManager notificationManager = kaaClient.getNotificationManager();
     * 
     *     // Make subscription changes
     *     notificationManager.subscribeOnTopics(Arrays.asList(&quot;optional_topic1&quot;, &quot;optional_topic2&quot;, &quot;optional_topic3&quot;), false);
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
