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

#ifndef INOTIFICATIONMANAGER_HPP_
#define INOTIFICATIONMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include <list>
#include <string>

#include "kaa/notification/INotificationListener.hpp"
#include "kaa/notification/INotificationTopicListListener.hpp"

namespace kaa {

/**
 * <p>Interface for the notification delivery system.</p>
 *
 * <p>Responsible for processing received topic/notification updates,
 * subscription to optional topic updates and unsubscription from them.</p>
 *
 * @author Denis Kimcherenko
 *
 * @see AbstractNotificationListener
 * @see NotificationTopicListListener
 * @see NotificationListenerInfo
 *
 */
class INotificationManager {
public:
    /**
     * <p>Add listener to receive updates of available topics.</p>
     *
     * @param listener The listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void addTopicListListener(INotificationTopicListListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving updates of available topics.</p>
     *
     * @param listener The listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void removeTopicListListener(INotificationTopicListListenerPtr listener) = 0;

    /**
     * <p>Retrieve a list of available topics.</p>
     *
     * @return List of available topics
     *
     */
    virtual Topics getTopics() = 0;

    /**
     * <p>Add listener to receive all notifications (both for mandatory and
     * optional topics).</p>
     *
     * @param listener The listener to receive notifications.
     *
     * @see AbstractNotificationListener
     */
    virtual void addNotificationListener(INotificationListenerPtr listener) = 0;

    /**
     * <p>Add listener to receive notifications relating to the specified topic.</p>
     *
     * <p>Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.</p>
     *
     * @param topicId  Id of topic (either mandatory or optional).
     * @param listener The listener to receive notifications.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    virtual void addNotificationListener(const std::string& topidId, INotificationListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving all notifications (both for mandatory and
     * optional topics).</p>
     *
     * @param listener Listener to receive notifications
     *
     * @see AbstractNotificationListener
     */
    virtual void removeNotificationListener(INotificationListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving notifications for the specified topic.</p>
     *
     * <p>Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.</p>
     *
     * @param topicId Id of topic (either mandatory or optional).
     * @param listener Listener to receive notifications.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    virtual void removeNotificationListener(const std::string& topidId, INotificationListenerPtr listener) = 0;

    /**
     * <p>Subscribe to notifications relating to the specified optional topic.</p>
     *
     * @param topicId Id of a optional topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopic(const std::string& id, bool forceSync) = 0;

    /**
     * <p>Subscribe to notifications relating to the specified list of
     * optional topics.</p>
     *
     * @param topicIds List of optional topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopics(const std::list<std::string>& idList, bool forceSync) = 0;

    /**
     * <p>Unsubscribe from notifications relating to the specified optional topic.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicId Id of a optional topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopic(const std::string& id, bool forceSync) = 0;

    /**
     * <p>Unsubscribe from notifications relating to the specified list of
     * optional topics.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicIds List of optional topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync) = 0;

    /**
     * <p>Accept optional subscription changes.</p>
     *
     * <p>Should be used after all @link subscribeToTopic() @endlink,
     * @link subscribeToTopics() @endlink, @link unsubscribeFromTopic() @endlink,
     * @link unsubscribeFromTopics() @endlink calls with parameter
     * <i>forceSync</i> set to <i>false</i>.</p>
     *
     * <p>Use it as a convenient way to make different consequent changes in
     * the optional subscription:</p>
     * @code
     *  NotificationManager notificationManager = kaaClient.getNotificationManager();
     *
     *  // Make subscription changes
     *  notificationManager.subscribeToTopics(Arrays.asList(
     *          "optional_topic1", "optional_topic2", "optional_topic3"), false);
     *  notificationManager.unsubscribeFromTopic("optional_topic4", false);
     *
     *  // Add listeners for optional topics (optional)
     *
     *  // Commit changes
     *  notificationManager.sync();
     * @endcode
     * </pre>
     */
    virtual void sync() = 0;

    virtual ~INotificationManager()
    {
    }
};

} /* namespace kaa */

#endif

#endif /* INOTIFICATIONMANAGER_HPP_ */
