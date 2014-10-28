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

import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

/**
 * <p>Interface for the notifications delivery system.</p>
 *
 * <p>Responsible for processing received topic/notification updates,
 * subscribing for voluntary topic updates and unsubscribing from them.</p>
 *
 * @author Yaroslav Zeygerman
 *
 * @see AbstractNotificationListener
 * @see NotificationListenerInfo
 * @see NotificationTopicListListener
 *
 */
public interface NotificationManager {

    /**
     * <p>Add listener for all mandatory topics' updates.</p>
     *
     * <p>If specific listener is needed for some mandatory topic, use
     * {@link NotificationManager#updateTopicSubscriptions}.</p>
     *
     * @param listener the listener to receive notification.
     * @see AbstractNotificationListener
     *
     */
    void addMandatoryTopicsListener(NotificationListener listener);

    /**
     * <p>Removes listener of mandatory topics' updates.</p>
     *
     * @param listener the listener which is no longer needs updates.
     * @see AbstractNotificationListener
     *
     */
    void removeMandatoryTopicsListener(NotificationListener listener);

    /**
     * Adds listener of topics' list updates.
     *
     * @param listener the listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    void addTopicListListener(NotificationTopicListListener listener);

    /**
     * Removes listener of topics' list updates.
     *
     * @param listener listener the listener which is no longer needs updates.
     * @see NotificationTopicListListener
     *
     */
    void removeTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>Updates (subscribe/unsubscribe) info about topic's subscriptions.</p>
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
     * @throws KaaException when topic isn't found or bad subscription info
     * was passed (empty id or null subscriber).
     *
     * @param subscribers collections of pairs topic id/subscriber info.
     * @see NotificationListenerInfo
     *
     */
    void updateTopicSubscriptions(Map<String, List<NotificationListenerInfo>> subscribers)
                                                            throws UnavailableTopicException;

    /**
     * Retrieves the current topics' list.
     *
     * @return the list of topics' names.
     * @see Topic
     *
     */
    List<Topic> getTopics();

    /**
     * <p>Retrieves and clears the current list of Subscription commands.</p>
     *
     * <p><b>NOTE:</b>DO NOT call method explicitly.</p>
     *
     * @return the list of Subscription commands.
     * @see SubscriptionCommand
     *
     */
    List<SubscriptionCommand> releaseSubscriptionCommands();

    /**
     * <p>Sets the transport to Notification manager.</p>
     *
     * <p><b>NOTE:</b>DO NOT call method explicitly.</p>
     *
     * @param transport the transport object to be set.
     * @see NotificationTransport
     *
     */
    void setTransport(NotificationTransport transport);
}
