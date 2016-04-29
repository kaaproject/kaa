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

#ifndef INOTIFICATIONMANAGER_HPP_
#define INOTIFICATIONMANAGER_HPP_

#include <list>
#include <string>

#include "kaa/notification/gen/NotificationDefinitions.hpp"

namespace kaa {

/*
 * Forward declaration.
 */
class INotificationListener;
class INotificationTopicListListener;

/**
 * @brief The public interface to topic subscription and notification delivery subsystems.
 *
 * @see INotificationListener
 * @see INotificationTopicListListener
 *
 */
class INotificationManager {
public:
    /**
     * @brief Adds the listener which receives updates on the list of available topics.
     *
     * @param[in] listener    The listener which receives updates.
     * @see INotificationTopicListListener
     *
     */
    virtual void addTopicListListener(INotificationTopicListListener& listener) = 0;

    /**
     * @brief Removes listener which receives updates on the list of available topics.
     *
     * @param[in] listener    The listener which receives updates.
     * @see INotificationTopicListListener
     *
     */
    virtual void removeTopicListListener(INotificationTopicListListener& listener) = 0;

    /**
     * @brief Retrieves the list of available topics.
     *
     * @return The list of available topics.
     *
     */
    virtual Topics getTopics() = 0;

    /**
     * @brief Adds the listener which receives notifications on all available topics.
     *
     * @param[in] listener    The listener which receives notifications.
     *
     * @see INotificationListener
     */
    virtual void addNotificationListener(INotificationListener& listener) = 0;

    /**
     * @brief Adds the listener which receives notifications on the specified topic.
     *
     * Listener(s) for optional topics may be added/removed irrespective to whether subscription is already done or not.
     *
     * @param[in] topicId     The id of the topic (either mandatory or optional).
     * @param[in] listener    The listener which receives notifications.
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided.
     *
     * @see INotificationListener
     */
    virtual void addNotificationListener(std::int64_t topicId, INotificationListener& listener) = 0;

    /**
     * @brief Removes the listener which receives notifications on all available topics.
     *
     * @param[in] listener    The listener which receives notifications.
     *
     * @see INotificationListener
     */
    virtual void removeNotificationListener(INotificationListener& listener) = 0;

    /**
     * @brief Removes the listener which receives notifications on the specified topic.
     *
     * Listener(s) for optional topics may be added/removed irrespective to whether subscription is already done or not.
     *
     * @param[in] topicId     The id of topic (either mandatory or optional).
     * @param[in] listener    The listener which receives notifications.
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided.
     *
     * @see INotificationListener
     */
    virtual void removeNotificationListener(std::int64_t topicId, INotificationListener& listener) = 0;

    /**
     * @brief Subscribes to the specified optional topic to receive notifications on that topic.
     *
     * @param[in] topicId      The id of the optional topic.
     * @param[in] forceSync    Indicates whether the subscription request should be sent immediately to
     *                         the Operations server. If <i> false </i>, the request postpones to the explicit
     *                         call of @link sync() @endlink or to the first call of @link subscribeToTopic() @endlink,
     *                         @link subscribeToTopics() @endlink, @link unsubscribeFromTopic() @endlink or
     *                         @link unsubscribeFromTopics() @endlink with the <i> true </i> value for the
     *                         @link forceSync @endlink parameter.
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided or the topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopic(std::int64_t id, bool forceSync = true) = 0;

    /**
     * @brief Subscribes to the specified list of optional topics to receive notifications on those topics.
     *
     * @param[in] topicIds     The list of optional topic id-s.
     * @param[in] forceSync    Indicates whether the subscription request should be sent immediately to
     *                         the Operations server. If <i> false </i>, the request postpones to the explicit
     *                         call of @link sync() @endlink or to the first call of @link subscribeToTopic() @endlink,
     *                         @link subscribeToTopics() @endlink, @link unsubscribeFromTopic() @endlink or
     *                         @link unsubscribeFromTopics() @endlink with the <i> true </i> value for the
     *                         @link forceSync @endlink parameter.
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided or the topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopics(const std::list<std::int64_t>& idList, bool forceSync = true) = 0;

    /**
     * @brief Unsubscribes from the specified optional topic to stop receiving notifications on that topic.
     *
     * @param[in] topicId      The id of the optional topic.
     * @param[in] forceSync    Indicates whether the subscription request should be sent immediately to
     *                         the Operations server or postponed. If <i> false </i>, the request is postponed either
     *                         to the explicit
     *                         call of @link sync() @endlink or to the first call of one of the following functions
     *                         with the @link forceSync @endlink parameter set to <i> true </i>:
     *                         @link subscribeToTopic() @endlink, @link subscribeToTopics() @endlink,
     *                         @link unsubscribeFromTopic() @endlink or @link unsubscribeFromTopics() @endlink .
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided or the topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopic(std::int64_t id, bool forceSync = true) = 0;

    /**
     * @brief Unsubscribes from the specified list of optional topics to stop receiving notifications on those topics.
     *
     * @param[in] topicId      The list of optional topic id-s.
     * @param[in] forceSync    Indicates whether the subscription request should be sent immediately to
     *                         the Operations server or postponed. If <i> false </i>, the request is postponed either
     *                         to the explicit
     *                         call of @link sync() @endlink or to the first call of one of the following functions
     *                         with the @link forceSync @endlink parameter set to <i> true </i>:
     *                         @link subscribeToTopic() @endlink, @link subscribeToTopics() @endlink,
     *                         @link unsubscribeFromTopic() @endlink or @link unsubscribeFromTopics() @endlink .
     *
     * @throw UnavailableTopicException Throws if the unknown topic id is provided or the topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopics(const std::list<std::int64_t>& idList, bool forceSync = true) = 0;

    /**
     * @brief Sends subscription request(s) to the Operations server.
     *
     * Use as a convenient way to send several subscription requests at once.
     * @code
     * IKaaClient& kaaClient = Kaa::getKaaClient();
     *
     * // Add listener(s) to receive notifications on topic(s)
     *
     * kaaClient.subscribeToTopics({"optional_topic1_id", "optional_topic2_id"}, false);
     * kaaClient.unsubscribeFromTopic("optional_topic3_id", false);
     *
     * kaaClient.syncTopicSubscriptions();
     * @endcode
     */
    virtual void sync() = 0;

    virtual ~INotificationManager() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONMANAGER_HPP_ */
