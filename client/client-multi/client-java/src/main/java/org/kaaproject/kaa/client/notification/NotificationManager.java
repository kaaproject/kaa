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
 * Manager which is responsible for the notifications delivery.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface NotificationManager {

    /**
     * Registers listener for all mandatory topics' updates.
     * If there is no specific listener for some mandatory topic,
     * notifications with that topic will be caught by this handler.
     *
     * @param listener the listener which is going to receive notification.
     *
     */
    void addMandatoryTopicsListener(NotificationListener listener);

    /**
     * Removes listener of mandatory topics' updates.
     *
     * @param listener the listener which is no longer needs updates.
     *
     */
    void removeMandatoryTopicsListener(NotificationListener listener);

    /**
     * Adds listener of topics' list updates.
     *
     * @param listener the listener which is going to receive updates.
     *
     */
    void addTopicListListener(NotificationTopicListListener listener);

    /**
     * Removes listener of topics' list updates.
     *
     * @param listener listener the listener which is no longer needs updates.
     *
     */
    void removeTopicListListener(NotificationTopicListListener listener);

    /**
     * Update (subscribe/unsubscribe) info about topic's subscriptions
     * @throw KaaException when topic isn't found or bad subscription info was passed (empty id or null subscriber).
     * @param subscribers collections of pairs topic id/subscriber info.
     * May consist several subscribers for the same topic.
     */
    void updateTopicSubscriptions(Map<String, List<NotificationListenerInfo>> subscribers) throws UnavailableTopicException;

    /**
     * Retrieves the current topics' list.
     *
     * @return the list of topics' names.
     *
     */
    List<Topic> getTopics();
}
